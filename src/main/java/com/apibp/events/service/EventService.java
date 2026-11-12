package com.apibp.events.service;

import com.apibp.events.dto.EventDto;
import com.apibp.events.dto.EventListResponse;
import com.apibp.events.model.Event;
import com.apibp.events.repository.EventRepository;
import com.apibp.events.util.CursorUtil;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Subquery;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EventService {

    private static final int DEFAULT_LIMIT = 20;
    private static final List<String> ALLOWED_SORT = List.of("id", "title", "category", "status");

    private final EventRepository repo;

    public EventListResponse list(
            String status,
            String category,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            String sort,
            String order,
            int limit,
            String after
    ) {
        String sortField = (sort != null && ALLOWED_SORT.contains(sort)) ? sort : "id";
        boolean asc = !"desc".equalsIgnoreCase(order);
        int pageSize = (limit > 0 && limit <= 100) ? limit : DEFAULT_LIMIT;

        Long afterId = (after != null) ? CursorUtil.decodeId(after) : null;

        Specification<Event> spec = buildSpec(status, category, minPrice, maxPrice,
                                               sortField, asc, afterId);

        Sort sortObj = Sort.by(asc ? Sort.Direction.ASC : Sort.Direction.DESC, sortField);
        List<Event> rows = repo.findAll(spec, PageRequest.of(0, pageSize + 1, sortObj)).getContent();

        boolean hasMore = rows.size() > pageSize;
        List<Event> page = hasMore ? rows.subList(0, pageSize) : rows;

        List<EventDto> dtos = page.stream().map(EventDto::from).toList();
        String nextCursor = hasMore ? CursorUtil.encode(sortField, page.getLast().getId()) : null;

        return new EventListResponse(dtos, hasMore, nextCursor);
    }

    private Specification<Event> buildSpec(
            String status,
            String category,
            BigDecimal minPrice, BigDecimal maxPrice,
            String sortField, boolean asc,
            Long afterId
    ) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (status != null){
                predicates.add(cb.equal(root.get("status"), status));
            }
            if (category != null) {
                predicates.add(cb.equal(root.get("category"), category));
            }

            if (minPrice != null || maxPrice != null) {
                // ponytail: subquery price filter — avoids JOIN FETCH on ticket_types for list page
                Subquery<Long> priceQ = query.subquery(Long.class);
                var tt = priceQ.from(com.apibp.events.model.TicketType.class);
                List<Predicate> pricePreds = new ArrayList<>();
                pricePreds.add(cb.equal(tt.get("event"), root));
                if (minPrice != null) pricePreds.add(cb.ge(tt.get("price"), minPrice));
                if (maxPrice != null) pricePreds.add(cb.le(tt.get("price"), maxPrice));
                priceQ.select(tt.get("id")).where(pricePreds.toArray(Predicate[]::new));
                predicates.add(cb.exists(priceQ));
            }

            if (afterId != null) {
                var afterIdLiteral = cb.literal(afterId);
                predicates.add(asc
                    ? cb.greaterThan(root.get("id"), afterIdLiteral)
                    : cb.lessThan(root.get("id"), afterIdLiteral));
            }

            return cb.and(predicates.toArray(Predicate[]::new));
        };
    }
}

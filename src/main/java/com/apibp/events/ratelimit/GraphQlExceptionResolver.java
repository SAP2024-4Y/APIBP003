package com.apibp.events.ratelimit;

import com.apibp.events.exception.DomainException;
import graphql.GraphQLError;
import graphql.GraphqlErrorBuilder;
import graphql.schema.DataFetchingEnvironment;
import org.springframework.graphql.execution.DataFetcherExceptionResolverAdapter;
import org.springframework.graphql.execution.ErrorType;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class GraphQlExceptionResolver extends DataFetcherExceptionResolverAdapter {

    @Override
    protected GraphQLError resolveToSingleError(Throwable ex, DataFetchingEnvironment env) {
        if (ex instanceof DomainException de) {
            return GraphqlErrorBuilder.newError(env)
                    .message(de.getMessage())
                    .errorType(ErrorType.INTERNAL_ERROR)
                    .extensions(Map.of("code", de.getCode().name()))
                    .build();
        }
        return null;
    }
}

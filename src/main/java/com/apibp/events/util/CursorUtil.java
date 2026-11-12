package com.apibp.events.util;

import com.apibp.events.exception.DomainException;
import com.apibp.events.exception.ErrorCode;
import org.springframework.http.HttpStatus;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * Shared opaque cursor encoder used by both REST and GraphQL layers.
 * Format: base64("{sortField}:{id}")
 */
public final class CursorUtil {

    private CursorUtil() {}

    public static String encode(String sortField, long id) {
        return Base64.getEncoder().encodeToString(
            (sortField + ":" + id).getBytes(StandardCharsets.UTF_8));
    }

    public static long decodeId(String cursor) {
        try {
            String raw = new String(Base64.getDecoder().decode(cursor), StandardCharsets.UTF_8);
            return Long.parseLong(raw.substring(raw.indexOf(':') + 1));
        } catch (Exception e) {
            throw new DomainException(ErrorCode.INVALID_CURSOR, HttpStatus.BAD_REQUEST,
                "Malformed cursor token");
        }
    }

    public static String decodeSortField(String cursor) {
        try {
            String raw = new String(Base64.getDecoder().decode(cursor), StandardCharsets.UTF_8);
            return raw.substring(0, raw.indexOf(':'));
        } catch (Exception e) {
            throw new DomainException(ErrorCode.INVALID_CURSOR, HttpStatus.BAD_REQUEST,
                "Malformed cursor token");
        }
    }
}

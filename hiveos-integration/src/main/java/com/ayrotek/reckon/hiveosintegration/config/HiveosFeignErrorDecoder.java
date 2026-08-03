package com.ayrotek.reckon.hiveosintegration.config;

import com.ayrotek.reckon.hiveosintegration.exception.HiveosApiException;
import feign.Response;
import feign.codec.ErrorDecoder;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class HiveosFeignErrorDecoder implements ErrorDecoder {

    private static final int MAX_BODY_LENGTH = 500;

    private final ErrorDecoder defaultDecoder = new Default();

    @Override
    public Exception decode(String methodKey, Response response) {
        if (response.status() >= 400 && response.status() <= 599) {
            String body = readBody(response);
            String message = "HiveOS API error on " + methodKey
                    + (body.isBlank() ? "" : ": " + body);
            return new HiveosApiException(response.status(), message);
        }
        return defaultDecoder.decode(methodKey, response);
    }

    private String readBody(Response response) {
        if (response.body() == null) {
            return "";
        }
        try (InputStream is = response.body().asInputStream()) {
            String body = new String(is.readAllBytes(), StandardCharsets.UTF_8).trim();
            return body.length() > MAX_BODY_LENGTH ? body.substring(0, MAX_BODY_LENGTH) + "..." : body;
        } catch (IOException e) {
            return "";
        }
    }
}

package com.ayrotek.reckon.hiveosintegration.logging;

import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockFilterChain;

import static org.junit.jupiter.api.Assertions.*;

public class CorrelationLoggingTest {

    @Test
    void shouldGenerateIdsWhenMissing() throws Exception {
        CorrelationFilter filter = new CorrelationFilter();
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain filterChain = new MockFilterChain();

        filter.doFilter(request, response, filterChain);

        String correlationId = response.getHeader("X-Correlation-ID");
        String requestId = response.getHeader("X-Request-ID");

        assertNotNull(correlationId);
        assertNotNull(requestId);
        assertFalse(correlationId.isEmpty());
        assertFalse(requestId.isEmpty());
    }

    @Test
    void shouldEchoIdsWhenProvided() throws Exception {
        CorrelationFilter filter = new CorrelationFilter();
        MockHttpServletRequest request = new MockHttpServletRequest();
        String testCorrelationId = "test-correlation-123";
        String testRequestId = "test-request-456";
        request.addHeader("X-Correlation-ID", testCorrelationId);
        request.addHeader("X-Request-ID", testRequestId);
        
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain filterChain = new MockFilterChain();

        filter.doFilter(request, response, filterChain);

        assertEquals(testCorrelationId, response.getHeader("X-Correlation-ID"));
        assertEquals(testRequestId, response.getHeader("X-Request-ID"));
    }

    @Test
    void shouldGenerateNewIdsWhenInvalidProvided() throws Exception {
        CorrelationFilter filter = new CorrelationFilter();
        MockHttpServletRequest request = new MockHttpServletRequest();
        String invalidId = "invalid_id_with_symbols!!!";
        request.addHeader("X-Correlation-ID", invalidId);
        request.addHeader("X-Request-ID", invalidId);

        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain filterChain = new MockFilterChain();

        filter.doFilter(request, response, filterChain);

        String correlationId = response.getHeader("X-Correlation-ID");
        String requestId = response.getHeader("X-Request-ID");

        assertNotNull(correlationId);
        assertNotNull(requestId);
        assertNotEquals(invalidId, correlationId);
        assertNotEquals(invalidId, requestId);
    }
}

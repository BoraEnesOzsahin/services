package com.ayrotek.coldwalletmanagerservice.logging;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link RequestCorrelationFilter}.
 *
 * <p>The filter was updated to use the ECS field name {@code trace.id} as the
 * MDC key, and to accept any non-blank string (not just UUIDs) as a valid
 * correlation ID.  Tests are updated accordingly.
 */
class RequestCorrelationFilterTest {

    private RequestCorrelationFilter filter;
    private MockHttpServletRequest request;
    private MockHttpServletResponse response;
    private FilterChain filterChain;

    @BeforeEach
    void setUp() {
        filter     = new RequestCorrelationFilter();
        request    = new MockHttpServletRequest();
        response   = new MockHttpServletResponse();
        filterChain = mock(FilterChain.class);
        MDC.clear();
    }

    @AfterEach
    void tearDown() {
        MDC.clear();
    }

    @Test
    void shouldGenerateTraceIdWhenHeaderIsMissing() throws ServletException, IOException {
        doAnswer(invocation -> {
            // MDC must be populated during the filter chain
            assertNotNull(MDC.get(RequestCorrelationFilter.MDC_TRACE_ID),
                    "trace.id must be present in MDC during request");
            return null;
        }).when(filterChain).doFilter(request, response);

        filter.doFilterInternal(request, response, filterChain);

        // Response header must contain the generated ID
        String responseId = response.getHeader(RequestCorrelationFilter.CORRELATION_HEADER);
        assertNotNull(responseId, "Response must contain X-Correlation-ID header");
        assertFalse(responseId.isBlank());

        // MDC must be cleared after filter completes
        assertNull(MDC.get(RequestCorrelationFilter.MDC_TRACE_ID),
                "trace.id must be removed from MDC after request");
    }

    @Test
    void shouldPropagateProvidedCorrelationIdAsTraceId() throws ServletException, IOException {
        String incomingId = "test-correlation-123";
        request.addHeader(RequestCorrelationFilter.CORRELATION_HEADER, incomingId);

        doAnswer(invocation -> {
            assertEquals(incomingId, MDC.get(RequestCorrelationFilter.MDC_TRACE_ID),
                    "trace.id in MDC must equal the incoming X-Correlation-ID");
            return null;
        }).when(filterChain).doFilter(request, response);

        filter.doFilterInternal(request, response, filterChain);

        assertEquals(incomingId, response.getHeader(RequestCorrelationFilter.CORRELATION_HEADER),
                "X-Correlation-ID response header must echo the incoming value");
        assertNull(MDC.get(RequestCorrelationFilter.MDC_TRACE_ID),
                "trace.id must be cleared after filter");
    }

    @Test
    void shouldAcceptNonUuidCorrelationId() throws ServletException, IOException {
        // The new filter accepts any non-blank string up to 128 chars
        String customId = "my-service-req-abc-001";
        request.addHeader(RequestCorrelationFilter.CORRELATION_HEADER, customId);

        doAnswer(invocation -> {
            assertEquals(customId, MDC.get(RequestCorrelationFilter.MDC_TRACE_ID));
            return null;
        }).when(filterChain).doFilter(request, response);

        filter.doFilterInternal(request, response, filterChain);

        assertEquals(customId, response.getHeader(RequestCorrelationFilter.CORRELATION_HEADER));
    }

    @Test
    void shouldGenerateNewIdWhenHeaderIsBlank() throws ServletException, IOException {
        request.addHeader(RequestCorrelationFilter.CORRELATION_HEADER, "   ");

        doAnswer(invocation -> {
            String mdcValue = MDC.get(RequestCorrelationFilter.MDC_TRACE_ID);
            assertNotNull(mdcValue);
            assertFalse(mdcValue.isBlank(), "Should have generated a non-blank UUID");
            return null;
        }).when(filterChain).doFilter(request, response);

        filter.doFilterInternal(request, response, filterChain);

        String responseId = response.getHeader(RequestCorrelationFilter.CORRELATION_HEADER);
        assertNotNull(responseId);
        assertNotEquals("   ", responseId, "Blank header should trigger UUID generation");
    }

    @Test
    void shouldClearMdcEvenWhenFilterChainThrows() throws ServletException, IOException {
        doThrow(new RuntimeException("chain error"))
                .when(filterChain).doFilter(request, response);

        assertThrows(RuntimeException.class,
                () -> filter.doFilterInternal(request, response, filterChain));

        assertNull(MDC.get(RequestCorrelationFilter.MDC_TRACE_ID),
                "trace.id must be removed from MDC even when the filter chain throws");
    }
}

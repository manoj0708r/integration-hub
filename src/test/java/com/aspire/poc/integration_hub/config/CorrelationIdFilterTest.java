package com.aspire.poc.integration_hub.config;

import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;

class CorrelationIdFilterTest {

    private final CorrelationIdFilter filter =
            new CorrelationIdFilter();

    @Test
    void shouldGenerateCorrelationIdWhenMissing() throws Exception {

        MockHttpServletRequest request =
                new MockHttpServletRequest();

        MockHttpServletResponse response =
                new MockHttpServletResponse();

        FilterChain filterChain =
                Mockito.mock(FilterChain.class);

        filter.doFilter(
                request,
                response,
                filterChain
        );

        String correlationId =
                (String) request.getAttribute(
                        CorrelationIdFilter.ATTRIBUTE_NAME
                );

        assertNotNull(correlationId);

        verify(filterChain)
                .doFilter(request, response);
    }

    @Test
    void shouldReuseProvidedCorrelationId() throws Exception {

        MockHttpServletRequest request =
                new MockHttpServletRequest();

        request.addHeader(
                CorrelationIdFilter.HEADER_NAME,
                "CORR-001"
        );

        MockHttpServletResponse response =
                new MockHttpServletResponse();

        FilterChain filterChain =
                Mockito.mock(FilterChain.class);

        filter.doFilter(
                request,
                response,
                filterChain
        );

        assertEquals(
                "CORR-001",
                request.getAttribute(
                        CorrelationIdFilter.ATTRIBUTE_NAME
                )
        );

        assertEquals(
                "CORR-001",
                response.getHeader(
                        CorrelationIdFilter.HEADER_NAME
                )
        );
    }
}
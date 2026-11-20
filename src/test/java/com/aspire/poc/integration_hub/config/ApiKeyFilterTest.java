package com.aspire.poc.integration_hub.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ApiKeyFilterTest {

    @Mock
    private ApplicationProperties properties;

    @Mock
    private FilterChain filterChain;

    private ApiKeyFilter filter;

    @BeforeEach
    void setUp() {
        filter = new ApiKeyFilter(properties);
    }

    @Test
    void shouldRejectMissingApiKey() throws Exception {

        MockHttpServletRequest request =
                new MockHttpServletRequest();

        MockHttpServletResponse response =
                new MockHttpServletResponse();

        filter.doFilter(
                request,
                response,
                filterChain
        );

        assert response.getStatus() ==
                HttpServletResponse.SC_UNAUTHORIZED;
    }

    @Test
    void shouldRejectInvalidApiKey() throws Exception {

        when(properties.getApiKey())
                .thenReturn("VALID");

        MockHttpServletRequest request =
                new MockHttpServletRequest();

        request.addHeader(
                "X-API-Key",
                "INVALID"
        );

        MockHttpServletResponse response =
                new MockHttpServletResponse();

        filter.doFilter(
                request,
                response,
                filterChain
        );

        assert response.getStatus() ==
                HttpServletResponse.SC_UNAUTHORIZED;
    }

    @Test
    void shouldAllowValidApiKey() throws Exception {

        when(properties.getApiKey())
                .thenReturn("VALID");

        MockHttpServletRequest request =
                new MockHttpServletRequest();

        request.addHeader(
                "X-API-Key",
                "VALID"
        );

        MockHttpServletResponse response =
                new MockHttpServletResponse();

        filter.doFilter(
                request,
                response,
                filterChain
        );

        verify(filterChain)
                .doFilter(any(), any());
    }
}
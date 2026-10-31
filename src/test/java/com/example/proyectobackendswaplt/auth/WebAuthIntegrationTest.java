package com.example.proyectobackendswaplt.auth;

import com.example.proyectobackendswaplt.support.IntegrationTestSupport;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class WebAuthIntegrationTest extends IntegrationTestSupport {
    private static final String REFRESH_COOKIE_NAME =
            "swaplt_refresh_token";

    private static final String CSRF_COOKIE_NAME =
            "XSRF-TOKEN";

    private static final String CSRF_HEADER_NAME =
            "X-XSRF-TOKEN";

    @Test
    void csrfEndpointProvidesReadableCookie()
            throws Exception {
        mockMvc.perform(get(BASE + "/auth/web/csrf"))
                .andExpect(status().isNoContent())
                .andExpect(cookie().exists(CSRF_COOKIE_NAME))
                .andExpect(cookie().httpOnly(
                        CSRF_COOKIE_NAME,
                        false
                ));
    }

    @Test
    void webAuthenticationRejectsMissingCsrfToken()
            throws Exception {
        mockMvc.perform(post(BASE + "/auth/web/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "user@example.com",
                                  "password": "Password123"
                                }
                                """))
                .andExpect(status().isForbidden());
    }

    @Test
    void webRegisterSetsHttpOnlyCookieAndHidesRefreshToken()
            throws Exception {
        String email =
                "web-register-" + uniqueSuffix() + "@example.com";

        mockMvc.perform(withCsrf(
                        post(BASE + "/auth/web/register")
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content("""
                                        {
                                          "name": "Web User",
                                          "email": "%s",
                                          "password": "Password123"
                                        }
                                        """.formatted(email))
                ))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token", notNullValue()))
                .andExpect(jsonPath("$.userId", notNullValue()))
                .andExpect(jsonPath("$.role", is("USER")))
                .andExpect(jsonPath(
                        "$.refreshToken"
                ).doesNotExist())
                .andExpect(cookie().exists(
                        REFRESH_COOKIE_NAME
                ))
                .andExpect(cookie().httpOnly(
                        REFRESH_COOKIE_NAME,
                        true
                ))
                .andExpect(cookie().secure(
                        REFRESH_COOKIE_NAME,
                        false
                ))
                .andExpect(cookie().path(
                        REFRESH_COOKIE_NAME,
                        BASE + "/auth/web"
                ))
                .andExpect(header().string(
                        HttpHeaders.SET_COOKIE,
                        containsString("SameSite=Lax")
                ));
    }

    @Test
    void webLoginSetsCookieAndHidesRefreshToken()
            throws Exception {
        String email =
                "web-login-" + uniqueSuffix() + "@example.com";

        mockMvc.perform(post(BASE + "/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Login User",
                                  "email": "%s",
                                  "password": "Password123"
                                }
                                """.formatted(email)))
                .andExpect(status().isCreated());

        mockMvc.perform(withCsrf(
                        post(BASE + "/auth/web/login")
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content("""
                                        {
                                          "email": "%s",
                                          "password": "Password123"
                                        }
                                        """.formatted(email))
                ))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token", notNullValue()))
                .andExpect(jsonPath(
                        "$.refreshToken"
                ).doesNotExist())
                .andExpect(cookie().exists(
                        REFRESH_COOKIE_NAME
                ))
                .andExpect(cookie().httpOnly(
                        REFRESH_COOKIE_NAME,
                        true
                ));
    }

    @Test
    void webRefreshRotatesCookieAndRejectsReuse()
            throws Exception {
        Cookie originalCookie = registerWebAndGetCookie();
        String originalValue = originalCookie.getValue();

        MvcResult refreshResult =
                mockMvc.perform(withCsrf(
                                post(BASE + "/auth/web/refresh"),
                                originalCookie
                        ))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath(
                                "$.token",
                                notNullValue()
                        ))
                        .andExpect(jsonPath(
                                "$.refreshToken"
                        ).doesNotExist())
                        .andExpect(cookie().exists(
                                REFRESH_COOKIE_NAME
                        ))
                        .andReturn();

        Cookie replacementCookie = Objects.requireNonNull(
                refreshResult.getResponse().getCookie(
                        REFRESH_COOKIE_NAME
                )
        );

        assertThat(replacementCookie.getValue())
                .isNotEqualTo(originalValue);

        mockMvc.perform(withCsrf(
                        post(BASE + "/auth/web/refresh"),
                        new Cookie(
                                REFRESH_COOKIE_NAME,
                                originalValue
                        )
                ))
                .andExpect(status().isUnauthorized())
                .andExpect(cookie().maxAge(
                        REFRESH_COOKIE_NAME,
                        0
                ));
    }

    @Test
    void webLogoutRevokesTokenAndClearsCookie()
            throws Exception {
        Cookie refreshCookie = registerWebAndGetCookie();

        mockMvc.perform(withCsrf(
                        post(BASE + "/auth/web/logout"),
                        refreshCookie
                ))
                .andExpect(status().isNoContent())
                .andExpect(cookie().maxAge(
                        REFRESH_COOKIE_NAME,
                        0
                ));

        mockMvc.perform(withCsrf(
                        post(BASE + "/auth/web/refresh"),
                        refreshCookie
                ))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void corsAllowsConfiguredWebOriginWithCredentials()
            throws Exception {
        mockMvc.perform(options(BASE + "/auth/web/login")
                        .header(
                                HttpHeaders.ORIGIN,
                                "http://localhost:5173"
                        )
                        .header(
                                HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD,
                                "POST"
                        )
                        .header(
                                HttpHeaders.ACCESS_CONTROL_REQUEST_HEADERS,
                                "Content-Type,X-XSRF-TOKEN"
                        ))
                .andExpect(status().isOk())
                .andExpect(header().string(
                        HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN,
                        "http://localhost:5173"
                ))
                .andExpect(header().string(
                        HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS,
                        "true"
                ))
                .andExpect(header().string(
                        HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS,
                        containsString("POST")
                ));
    }

    private Cookie registerWebAndGetCookie() throws Exception {
        String email =
                "web-cookie-" + uniqueSuffix() + "@example.com";

        MvcResult result =
                mockMvc.perform(withCsrf(
                                post(BASE + "/auth/web/register")
                                        .contentType(
                                                MediaType.APPLICATION_JSON
                                        )
                                        .content("""
                                                {
                                                  "name": "Cookie User",
                                                  "email": "%s",
                                                  "password": "Password123"
                                                }
                                                """.formatted(email))
                        ))
                        .andExpect(status().isCreated())
                        .andReturn();

        return Objects.requireNonNull(
                result.getResponse().getCookie(
                        REFRESH_COOKIE_NAME
                )
        );
    }

    private MockHttpServletRequestBuilder withCsrf(
            MockHttpServletRequestBuilder request,
            Cookie... additionalCookies
    ) throws Exception {
        MvcResult csrfResult =
                mockMvc.perform(get(BASE + "/auth/web/csrf"))
                        .andExpect(status().isNoContent())
                        .andExpect(cookie().exists(
                                CSRF_COOKIE_NAME
                        ))
                        .andReturn();

        Cookie csrfCookie = Objects.requireNonNull(
                csrfResult.getResponse().getCookie(
                        CSRF_COOKIE_NAME
                )
        );

        Cookie[] cookies =
                new Cookie[additionalCookies.length + 1];

        cookies[0] = csrfCookie;

        System.arraycopy(
                additionalCookies,
                0,
                cookies,
                1,
                additionalCookies.length
        );

        return request
                .cookie(cookies)
                .header(
                        CSRF_HEADER_NAME,
                        csrfCookie.getValue()
                );
    }
}

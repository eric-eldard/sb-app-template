package com.your_namespace.your_app.security;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;

import java.net.URI;

import com.your_namespace.your_app.test.BaseMvcIntegrationTest;

/**
 * Smoke test of access to Spring Actuator endpoints, which are all restricted to admins
 */
public class ActuatorAccessIT extends BaseMvcIntegrationTest
{
    @Test
    public void testAdminCanAccessActuatorLogging() throws Exception
    {
        get(makeActuatorUri("loggers"), asAdmin())
            .andExpect(status().isOk());
    }

    @Test
    public void testNonAdminCannotAccessActuatorLogging() throws Exception
    {
        get(makeActuatorUri("loggers"), asUser())
            .andExpect(status().isForbidden());
    }

    @Test
    public void testUnauthenticatedCannotAccessActuatorLogging() throws Exception
    {
        get(makeActuatorUri("loggers"), asUnauthenticated())
            .andExpect(status().isFound())
            .andDo(this::assertRedirectToLogin);
    }

    private URI makeActuatorUri(String path)
    {
        return makeBaseUri()
            .path("/actuator/" + path)
            .build()
            .toUri();
    }
}
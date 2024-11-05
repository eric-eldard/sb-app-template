package com.your_namespace.your_app.controller.app;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;

import java.net.URI;

import com.your_namespace.your_app.test.BaseMvcIntegrationTest;

public class AppControllerIT extends BaseMvcIntegrationTest
{
    @Test
    @SneakyThrows
    public void testAdminCanViewApp()
    {
        get(makeAppUri(), asAdmin())
            .andExpect(status().isOk());
    }

    @Test
    @SneakyThrows
    public void testViewerCanViewApp()
    {
        get(makeAppUri(), asUser())
            .andExpect(status().isOk());
    }

    @Test
    @SneakyThrows
    public void testUnauthenticatedCannotViewApp()
    {
        get(makeAppUri(), asUnauthenticated())
            .andExpect(status().isFound())
            .andDo(this::assertRedirectToLogin);
    }


    private URI makeAppUri()
    {
        return makeBaseUri()
            .path("/your_app") // TODO - set your app's root path
            .build()
            .toUri();
    }
}
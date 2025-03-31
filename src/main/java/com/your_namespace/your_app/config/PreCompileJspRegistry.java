package com.your_namespace.your_app.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.descriptor.web.ServletDef;
import org.apache.tomcat.util.descriptor.web.WebXml;
import org.apache.tomcat.util.descriptor.web.WebXmlParser;
import org.xml.sax.InputSource;

import jakarta.servlet.ServletRegistration;
import java.io.InputStream;
import java.util.Map;

import org.springframework.boot.web.servlet.ServletContextInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Reads pre-compiled JSPs locations out of the web.xml and provides their paths to the servlet.
 * Adapted from <a href="https://stackoverflow.com/a/55231198/1908807">https://stackoverflow.com/a/55231198/1908807</a>
 */
@Slf4j
@Configuration
public class PreCompileJspRegistry
{
    @Bean
    public ServletContextInitializer registerPreCompiledJsps()
    {
        return servletContext ->
        {
            InputStream inputStream = servletContext.getResourceAsStream("/WEB-INF/web.xml");
            if (inputStream == null)
            {
                log.info("Could not read web.xml");
                return;
            }
            try
            {
                WebXmlParser parser = new WebXmlParser(false, false, true);
                WebXml webXml = new WebXml();
                boolean success = parser.parseWebXml(new InputSource(inputStream), webXml, false);

                if (!success)
                {
                    log.error("Error registering precompiled JSPs");
                    return;
                }

                for (ServletDef def : webXml.getServlets().values())
                {
                    log.info("Registering precompiled JSP: {} -> {}", def.getServletName(), def.getServletClass());
                    ServletRegistration.Dynamic reg =
                        servletContext.addServlet(def.getServletName(), def.getServletClass());
                    reg.setInitParameter("development", "false");
                    reg.setLoadOnStartup(99);
                }

                for (Map.Entry<String, String> mapping : webXml.getServletMappings().entrySet())
                {
                    log.info("Mapping servlet: {} -> {}", mapping.getValue(), mapping.getKey());
                    servletContext.getServletRegistration(mapping.getValue()).addMapping(mapping.getKey());
                }
            }
            catch (Exception ex)
            {
                log.error("Error registering precompiled JSPs", ex);
            }
        };
    }
}
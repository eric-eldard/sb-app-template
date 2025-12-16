package com.your_namespace.your_app.config;

import static org.springframework.security.web.header.writers.CrossOriginResourcePolicyHeaderWriter.CrossOriginResourcePolicy.SAME_SITE;

import lombok.AllArgsConstructor;

import jakarta.servlet.DispatcherType;

import org.springframework.boot.web.server.servlet.CookieSameSiteSupplier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AnonymousConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.context.RequestAttributeSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextHolderFilter;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;
import org.springframework.security.web.servletapi.SecurityContextHolderAwareRequestFilter;

import com.your_namespace.your_app.logging.filter.AddUserToMdcFilter;
import com.your_namespace.your_app.security.csrf.CsrfTokenRepository;
import com.your_namespace.your_app.security.filter.JwsFilter;
import com.your_namespace.your_app.service.auth.AuthenticationService;
import com.your_namespace.your_app.service.auth.SecurityContextService;
import com.your_namespace.your_app.service.user.UserService;

/**
 * Master config for security, logging, and beans for which creation order prevents a circular dependency.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@AllArgsConstructor
public class GlobalConfig
{
    private final AuthenticationService authenticationService;

    private final CsrfTokenRepository csrfTokenRepo;

    private final PasswordEncoder passwordEncoder;

    private final SecurityContextService securityContextService;

    private final UserService userService;


    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration)
        throws Exception
    {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public CookieSameSiteSupplier applicationCookieSameSiteSupplier()
    {
        return CookieSameSiteSupplier.ofStrict();
    }

    @Bean
    public SecurityFilterChain appFilterChain(HttpSecurity httpSecurity) throws Exception
    {
        httpSecurity
            .anonymous(AnonymousConfigurer::disable)
            .authorizeHttpRequests(requests -> requests
                // WARNING: order matters, since these paths are hierarchical; putting "/" 1st gives admin access to all
                .requestMatchers("/actuator/**").hasRole("ADMIN")
                .requestMatchers("/your_app/users/**").hasRole("ADMIN") // TODO - set your app's root path
                .requestMatchers("/your_app/**").authenticated() // TODO - set your app's root path
                .requestMatchers(
                    "/",
                    "/login",
                    "/logout",
                    "/public/assets/**",
                    "/favicon.ico"
                ).permitAll()
                .dispatcherTypeMatchers(
                    DispatcherType.ERROR,
                    DispatcherType.FORWARD,
                    DispatcherType.INCLUDE
                ).permitAll()
            )
            .authenticationProvider(
                makeAuthenticationProvider()
            )
            .securityContext(security ->
                // Set security context to expire after the request (context not stored in a session)
                security.securityContextRepository(new RequestAttributeSecurityContextRepository())
            )
            .sessionManagement(sessions ->
                sessions.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            .csrf(csrf ->
                csrf
                    .csrfTokenRepository(csrfTokenRepo)
                    .ignoringRequestMatchers("/login")
            )
            .logout(logout ->
                logout
                    .permitAll()
                    .logoutRequestMatcher(PathPatternRequestMatcher.withDefaults().matcher(HttpMethod.GET, "/logout")) // reinstate GET /logout (removed by CSRF config)
                    .logoutSuccessUrl("/")
                    .addLogoutHandler((request, response, authentication) ->
                        authenticationService.logUserOut(response, authentication))
            )
            .exceptionHandling(ex ->
                ex.authenticationEntryPoint(new LoginUrlAuthenticationEntryPoint("/login"))
            )
            .headers(headers -> headers
                .frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin)
                .crossOriginResourcePolicy(crossOrigin -> crossOrigin.policy(SAME_SITE))
            )
            .addFilterAfter(
                new JwsFilter(authenticationService), SecurityContextHolderFilter.class
            )
            .addFilterAfter(
                new AddUserToMdcFilter(securityContextService), SecurityContextHolderAwareRequestFilter.class
            );

        return httpSecurity.build();
    }

    private AuthenticationProvider makeAuthenticationProvider()
    {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userService);
        provider.setPasswordEncoder(passwordEncoder);
        return provider;
    }
}
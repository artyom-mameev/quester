package com.artyommameev.quester.security;

import com.artyommameev.quester.security.filter.*;
import com.artyommameev.quester.security.user.ActualUser;
import com.artyommameev.quester.security.user.service.CustomOidcUserService;
import com.artyommameev.quester.security.user.service.MyUserDetailsService;
import lombok.val;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.intercept.FilterSecurityInterceptor;
import org.springframework.security.web.authentication.*;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.AnyRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;

import java.util.LinkedHashMap;

/**
 * The Spring Security configuration.
 *
 * @author Artyom Mameev
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    private final CustomOidcUserService customOidcUserService;
    private final ActualUser actualUser;
    private final MyUserDetailsService myUserDetailsService;
    private final PasswordEncoder passwordEncoder;

    /**
     * The constructor, through which dependencies are injected by Spring.
     *
     * @param customOidcUserService a custom {@link OidcUserService}.
     * @param actualUser            the {@link ActualUser} abstraction which
     *                              represents current normal or oAuth2 user.
     * @param myUserDetailsService  a custom {@link UserDetailsService}.
     * @param passwordEncoder       a {@link PasswordEncoder}.
     * @see ActualUser
     */
    public SecurityConfig(CustomOidcUserService customOidcUserService,
                          ActualUser actualUser,
                          MyUserDetailsService myUserDetailsService,
                          PasswordEncoder passwordEncoder) {
        this.customOidcUserService = customOidcUserService;
        this.actualUser = actualUser;
        this.myUserDetailsService = myUserDetailsService;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        final LinkedHashMap<RequestMatcher, AuthenticationEntryPoint>
                entryPoints = new LinkedHashMap<>();

        entryPoints.put(new AntPathRequestMatcher("/api/**"),
                new Http403ForbiddenEntryPoint());
        entryPoints.put(AnyRequestMatcher.INSTANCE,
                new LoginUrlAuthenticationEntryPoint("/login"));

        final AuthenticationEntryPoint authenticationEntryPoint =
                new DelegatingAuthenticationEntryPoint(entryPoints);

        http
                .addFilterAfter(new NotConfirmedUsernameFilter(actualUser),
                        FilterSecurityInterceptor.class)
                .addFilterAfter(new ConfirmUsernamePageFilter(actualUser),
                        FilterSecurityInterceptor.class)
                .addFilterAfter(new EditProfilePageFilter(actualUser),
                        FilterSecurityInterceptor.class)
                .addFilterAfter(new LoginPageFilter(),
                        FilterSecurityInterceptor.class)
                .addFilterAfter(new RegisterPageFilter(),
                        FilterSecurityInterceptor.class)
                .addFilterAfter(new GamesPageFilter(),
                        FilterSecurityInterceptor.class)
                .addFilterAfter(new UserCommentsPageFilter(actualUser),
                        FilterSecurityInterceptor.class)

                .authorizeRequests(a -> a
                        .antMatchers("/", "/error/**", "/webjars/**",
                                "/css/**", "/js/**", "/themes/**", "/login",
                                "/register", "/games", "/games/{gameId:\\d+}",
                                "/games/{gameId:\\d+}/play", "/comments",
                                "/h2-console/**")
                        .permitAll()

                        .antMatchers(HttpMethod.GET,
                                "/api/games/{gameId:\\d+}")
                        .permitAll()

                        .antMatchers(HttpMethod.POST, "/register")
                        .permitAll()

                        .antMatchers(HttpMethod.POST,
                                "/api/users/{userId:\\d+}/ban")
                        .hasAuthority("ROLE_ADMIN")

                        .antMatchers(HttpMethod.DELETE,
                                "/api/users/{userId:\\d+}/ban",
                                "/api/users/{userId:\\d+}/games",
                                "/api/users/{userId:\\d+}/comments",
                                "/api/users/{userId:\\d+}/reviews")
                        .hasAuthority("ROLE_ADMIN")

                        .anyRequest().authenticated())

                .exceptionHandling(e -> e
                        .authenticationEntryPoint(authenticationEntryPoint))

                .formLogin()
                .loginPage("/login")
                .defaultSuccessUrl("/", true)
                .permitAll()
                .and()
                .rememberMe().key("5BL3pNsjoSnRTqVZ8A9YiPIveQAXDBVJ")
                .userDetailsService(myUserDetailsService)
                .and()
                .logout(l -> l
                        .logoutSuccessUrl("/").permitAll()
                        .deleteCookies("JSESSIONID"))

                .csrf(c -> c
                        .csrfTokenRepository(CookieCsrfTokenRepository
                                .withHttpOnlyFalse())
                        .ignoringAntMatchers("/h2-console/**")
                )

                .headers().frameOptions().sameOrigin() //for h2 console

                .and()
                .oauth2Login(o -> o
                        .failureHandler((request, response, exception) -> {
                            request.getSession()
                                    .setAttribute("error.message",
                                            exception.getMessage());
                            authenticationFailureHandler()
                                    .onAuthenticationFailure(request, response,
                                            exception);
                        })
                        .successHandler(new SimpleUrlAuthenticationSuccessHandler(
                                "/confirm-username"))
                        .userInfoEndpoint()
                        .oidcUserService(customOidcUserService));
    }

    protected void configure(final AuthenticationManagerBuilder auth)
            throws Exception {
        auth.userDetailsService(myUserDetailsService);
        auth.authenticationProvider(authProvider());
    }

    /**
     * An authentication provider.
     *
     * @return the {@link DaoAuthenticationProvider} with
     * {@link MyUserDetailsService} service and {@link PasswordEncoder}.
     */
    @Bean
    public AuthenticationProvider authProvider() {
        val authProvider = new DaoAuthenticationProvider();

        authProvider.setUserDetailsService(myUserDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder);

        return authProvider;
    }

    /**
     * An authentication failure handler for a oAuth2 login.
     *
     * @return the simple {@link AuthenticationFailureHandler} that redirects to
     * the '/login' url.
     */
    @Bean
    public AuthenticationFailureHandler authenticationFailureHandler() {
        return new SimpleUrlAuthenticationFailureHandler("/login");
    }
}
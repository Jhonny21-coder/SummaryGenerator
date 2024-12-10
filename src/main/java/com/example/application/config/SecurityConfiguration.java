package com.example.application.config;

import com.example.application.data.DataRecord;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.http.HttpMethod;

/*import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;*/

import com.example.application.views.LoginView;
import com.vaadin.flow.spring.security.VaadinWebSecurity;
import com.vaadin.flow.component.html.Div;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;

import reactor.core.publisher.ReplayProcessor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.UnicastProcessor;

@Configuration
@EnableWebSecurity
public class SecurityConfiguration extends VaadinWebSecurity {
    @Autowired
    private CustomUserDetailsService customUserDetailsService;

    private final String LOGIN_URL = "/login";

    @Override
    protected void configure(HttpSecurity http) throws Exception {
    	http.authorizeHttpRequests(auth -> {
            auth.requestMatchers(
             AntPathRequestMatcher.antMatcher(HttpMethod.GET, "/images/*.png")).permitAll(); /*+*/
            auth.requestMatchers(
             AntPathRequestMatcher.antMatcher(HttpMethod.GET, "/icons/*.svg")).permitAll(); /*+*/
        })
        .oauth2Login(c -> c.loginPage(LOGIN_URL).permitAll()
        );
        super.configure(http);
        setLoginView(http, LoginView.class);
    }

    @Bean
    public AuthenticationManager authManager(HttpSecurity http) throws Exception {
    	AuthenticationManagerBuilder authenticationManagerBuilder =
            http.getSharedObject(AuthenticationManagerBuilder.class);
        return authenticationManagerBuilder.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public Cloudinary cloudinary() {
        return new Cloudinary(ObjectUtils.asMap(
            "cloud_name", "datiflrbo",
            "api_key", "557282944658286",
            "api_secret", "hRQa12rR4RwbtiaGJC3jqd8yDWo"
        ));
    }

    @Bean
    public UnicastProcessor<DataRecord> publisher() {
        return UnicastProcessor.create();
    }

    @Bean
    public Flux<DataRecord> ratingFlux(UnicastProcessor<DataRecord> publisher) {
        return publisher.replay(30).autoConnect();
    }

    /*@Bean
    public ClientRegistrationRepository clientRegistrationRepository() {
    	return new InMemoryClientRegistrationRepository(clientRegistrations());
    }

    private ClientRegistration clientRegistrations() {
    	return ClientRegistration.withRegistrationId("google")
        .clientId("502616097361-fkl1ro7f51khico7fuhepjcg6ibd2o24.apps.googleusercontent.com")
        .clientSecret("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")
        .scope("profile", "email")
        .authorizationUri("https://accounts.google.com/o/oauth2/v2/auth")
        .tokenUri("https://oauth2.googleapis.com/token")
        .userInfoUri("https://www.googleapis.com/oauth2/v3/userinfo")
        .userNameAttributeName("sub")
        .redirectUriTemplate("{baseUrl}/login/oauth2/code/{registrationId}")
        .clientName("Google")
        .build();
    }*/
}

package com.kuke.videomeeting.config.security;

import com.google.common.collect.ImmutableList;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import java.util.Arrays;
import java.util.List;

@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig extends WebSecurityConfigurerAdapter {
    private final JwtTokenProvider jwtTokenProvider;

    @Value("${domain}")
    private String allowedOrigin;

    @Override
    public void configure(WebSecurity web) throws Exception {
        web.ignoring().antMatchers("/v2/api-docs", "/swagger-resources/**"
                , "/swagger-ui.html", "/webjars/**", "/swagger/**");
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .httpBasic().disable() // rest api이므로 기본설정 미사용
                .cors().and() // security cors 허용
                .csrf().disable() // rest api이므로 csrf 보안 미사용
                .formLogin().disable()
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS) // jwt로 인증하므로 세션 미사용
                .and()
                    .authorizeRequests()
                        .antMatchers(HttpMethod.POST, "/api/sign/login", "/api/sign/register", "/api/sign/refresh-token", "/api/sign/send-code-email-for-forgotten-password", "/api/sign/login-by-provider", "/api/sign/register-by-provider").permitAll()
                        .antMatchers(HttpMethod.PUT, "/api/sign/change-forgotten-password").permitAll()
                        .antMatchers(HttpMethod.POST, "/api/social/get-token-by-provider").permitAll()
                        .antMatchers(HttpMethod.GET, "/api/users/me", "/api/friends/me").hasRole("NORMAL")
                        .antMatchers(HttpMethod.GET, "/exception", "/exception/**").permitAll()
                        .antMatchers(HttpMethod.GET, "/api/users", "/api/users/**", "/api/users/nickname",
                                "/api/users/nickname/**", "/api/friends").permitAll()
                        .antMatchers(HttpMethod.GET, "/api/rooms", "/api/rooms/**").permitAll()
                        .antMatchers(HttpMethod.GET, "/kuke-health/health").permitAll()
                        .anyRequest().hasRole("NORMAL")
                .and()
                    .exceptionHandling().accessDeniedHandler(new CustomAccessDeniedHandler())
                .and()
                    .exceptionHandling().authenticationEntryPoint(new CustomAuthenticationEntryPoint())
                .and()
                    .addFilterBefore(new JwtAuthenticationFilter(jwtTokenProvider), UsernamePasswordAuthenticationFilter.class); // jwt 필터 추가

    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        final CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of(allowedOrigin));

        configuration.addAllowedHeader("*");
        configuration.addAllowedMethod("*");
        configuration.setAllowCredentials(true);

        final UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

}

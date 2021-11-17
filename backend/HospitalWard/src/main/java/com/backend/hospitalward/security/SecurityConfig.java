package com.backend.hospitalward.security;

import com.backend.hospitalward.service.AuthService;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@EnableWebSecurity
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@AllArgsConstructor
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    AuthService authService;

    RequestFilter requestFilter;

    @Override
    protected void configure(AuthenticationManagerBuilder auth) {
        auth.authenticationProvider(daoAuthenticationProvider());
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.cors().and()
                .csrf().disable()
                .authorizeRequests()
                .antMatchers(HttpMethod.POST, "/auth")
                .permitAll()
                .antMatchers(HttpMethod.GET, "/refresh")
                .authenticated()
                .antMatchers(HttpMethod.GET, "/accounts")
                .hasAuthority(SecurityConstants.TREATMENT_DIRECTOR)
                .antMatchers(HttpMethod.GET, "/accounts/profile")
                .authenticated()
                .antMatchers(HttpMethod.PUT, "/accounts/password")
                .authenticated()
                .antMatchers(HttpMethod.PUT, "/accounts/activate")
                .hasAuthority(SecurityConstants.TREATMENT_DIRECTOR)
                .antMatchers(HttpMethod.PUT, "/accounts/deactivate")
                .hasAuthority(SecurityConstants.TREATMENT_DIRECTOR)
                .antMatchers(HttpMethod.PUT, "/accounts/office/edit")
                .hasAuthority(SecurityConstants.TREATMENT_DIRECTOR)
                .antMatchers(HttpMethod.PUT, "/accounts/medic/edit")
                .hasAuthority(SecurityConstants.TREATMENT_DIRECTOR)
                .antMatchers(HttpMethod.PUT, "/profile/office/edit")
                .hasAuthority(SecurityConstants.SECRETARY)
                .antMatchers(HttpMethod.PUT, "/profile/medic/edit")
                .hasAnyAuthority(SecurityConstants.TREATMENT_DIRECTOR, SecurityConstants.DOCTOR, SecurityConstants.HEAD_NURSE)
                .antMatchers(HttpMethod.PUT, "/accounts/accessLevel")
                .hasAuthority(SecurityConstants.TREATMENT_DIRECTOR)
                .antMatchers(HttpMethod.PUT, "/accounts/confirm")
                .permitAll()
                .antMatchers(HttpMethod.PUT, "/accounts/edit/email")
                .authenticated()
                .antMatchers(HttpMethod.PUT, "/accounts/password/reset")
                .permitAll()
                .antMatchers(HttpMethod.POST, "/accounts")
                .hasAuthority(SecurityConstants.TREATMENT_DIRECTOR)
                .antMatchers(HttpMethod.POST, "/accounts/password/reset")
                .permitAll()
                .and()
                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS);
        http.addFilterBefore(requestFilter, UsernamePasswordAuthenticationFilter.class);
    }

    @Override
    @Bean
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

    @Bean
    public DaoAuthenticationProvider daoAuthenticationProvider() {
        DaoAuthenticationProvider daoAuthenticationProvider = new DaoAuthenticationProvider();
        daoAuthenticationProvider.setUserDetailsService(authService);
        daoAuthenticationProvider.setPasswordEncoder(NoOpPasswordEncoder.getInstance());
        return daoAuthenticationProvider;
    }

}

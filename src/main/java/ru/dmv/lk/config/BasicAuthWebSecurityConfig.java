package ru.dmv.lk.config;


import org.springframework.boot.actuate.autoconfigure.security.servlet.EndpointRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.server.SecurityWebFilterChain;

@Configuration
@EnableWebSecurity
public class BasicAuthWebSecurityConfig extends WebSecurityConfigurerAdapter {


    private BasicAuthEntryPoint authenticationEntryPoint =new BasicAuthEntryPoint();

     @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.authorizeRequests()
                .antMatchers("/","/index","/errors/**").permitAll()
                .antMatchers("/admin/**","/actuator/**").hasAuthority("ROLE_ADMIN")
                .antMatchers("/apiApp/**","/actuator/**").access("hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_API_USER') ")
                .antMatchers("/user/**","/apitabulator/**").access("hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_USER') ")
                .anyRequest().authenticated()
                .and()
                .httpBasic()
                .authenticationEntryPoint(authenticationEntryPoint)
                .and()
                .exceptionHandling().accessDeniedPage("/403")
                .and()
                //Выключаем csrf защиту только для некоторых Request
                .csrf().ignoringAntMatchers("/apiApp/**");

        //http.addFilterAfter(new CustomFilter(), BasicAuthenticationFilter.class);
/*
                .antMatchers(EndpointRequest.toAnyEndpoint())
                 .authorizeRequests((requests) -> requests.anyRequest().hasRole("ACTUATOR_INFO"))
*/
     }


    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

}

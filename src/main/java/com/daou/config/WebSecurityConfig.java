package com.daou.config;

import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.annotation.web.servlet.configuration.EnableWebMvcSecurity;

@Configuration
@EnableWebMvcSecurity
@EnableGlobalMethodSecurity(prePostEnabled=true)
@Order(SecurityProperties.ACCESS_OVERRIDE_ORDER)
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {
	@Override
	protected void configure(HttpSecurity http) throws Exception {
		http.csrf().disable().authorizeRequests().anyRequest().permitAll();
		
		/*  
	    http
	    	.csrf().disable()
	        .authorizeRequests()
	            .antMatchers("/REST/**", "/inc/*", "/bootstrap-3.3.4-dist/**", "/js/**", "/metrics", "/bootstrap-datetimepicker/**").permitAll()
	            .anyRequest().authenticated()
	            .and()
	        .formLogin()
	            .loginPage("/")
	            .usernameParameter("user_id")
	            .passwordParameter("pwd")
	            .permitAll()
	            .and()
	        .logout()
				.deleteCookies("remember-me")
				.permitAll()
				.and()
			.rememberMe();
			*/
    }
}

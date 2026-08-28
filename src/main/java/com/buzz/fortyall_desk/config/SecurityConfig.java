package com.buzz.fortyall_desk.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

/**
 * 배포 검증용 최소 보안 설정.
 * 실제 인증(관리자 이메일+비밀번호 / 회원 초대링크)은 슬라이스 1에서 만든다.
 */
@Configuration
public class SecurityConfig {

	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
		http
			.csrf(csrf -> csrf.disable())
			.authorizeHttpRequests(auth -> auth
				.requestMatchers("/actuator/health").permitAll()
				.anyRequest().authenticated()
			)
			.httpBasic(basic -> basic.disable())
			.formLogin(form -> form.disable());

		return http.build();
	}
}

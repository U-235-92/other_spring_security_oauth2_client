package com.other.app.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.oidc.OidcScopes;
import org.springframework.security.web.SecurityFilterChain;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class OAuthConfiguration {

	@Bean
	protected SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
		httpSecurity
			.authorizeHttpRequests(requestCustomizer -> requestCustomizer
				.anyRequest().authenticated())
			.oauth2Login(Customizer.withDefaults())
			.oauth2Client(Customizer.withDefaults());
		return httpSecurity.build();
	}
	
	@Bean
	protected ClientRegistrationRepository clientRegistrationRepository() {
		ClientRegistration clientRegistration = ClientRegistration
				.withRegistrationId("message_service")
				.clientId("message_service")
				.clientSecret("123")
				.clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
				.authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
				.redirectUri("http://outh2client:8080/login/oauth2/code/message_service")
				.scope("read_message", "write_message", "delete_message", OidcScopes.OPENID)
				.issuerUri("http://authserver:8081")
				.authorizationUri("http://authserver:8081/oauth2/authorize")
				.tokenUri("http://authserver:8081/oauth2/token")
				.jwkSetUri("http://authserver:8081/oauth2/jwks")
				.userInfoUri("http://authserver:8081/userinfo")
				.build();
		InMemoryClientRegistrationRepository inMemoryClientRegistrationRepository = new InMemoryClientRegistrationRepository(clientRegistration);
		return inMemoryClientRegistrationRepository;
	}
}

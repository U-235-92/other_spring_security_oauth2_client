package com.other.app.controller;

import java.util.List;

import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import com.other.app.dto.MessageDTO;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/app")
public class AppController {

	private final OAuth2AuthorizedClientService oAuth2AuthorizedClientService;
	private final RestTemplate restTemplate;
	
	@ResponseBody
	@GetMapping("/read_messages")
	public List<MessageDTO> readMessages() {
		restTemplate.getInterceptors().add(tokenInterceptor());
		MessageDTO[] messages = restTemplate.getForObject("http://localhost:8082/app/read_messages", MessageDTO[].class);
		return List.of(messages);
	}
	
	private ClientHttpRequestInterceptor tokenInterceptor() {
		return (request, body, execution) -> {
			Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
			String accessToken = oAuth2AuthorizedClientService.loadAuthorizedClient("message_service", authentication.getName()).getAccessToken().getTokenValue();
			request.getHeaders().add("Authorization", "Bearer "  + accessToken);
			return execution.execute(request, body);
		};
	}
}

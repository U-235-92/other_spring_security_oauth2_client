package com.other.app.controller;

import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.Scanner;

import org.springframework.http.HttpMethod;
import org.springframework.http.client.ClientHttpRequest;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;
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
		addBearerTokenToRestTemplate();
		MessageDTO[] messages = restTemplate.getForObject("http://localhost:8082/app/read_messages", MessageDTO[].class);
		return List.of(messages);
	}
	
	@ResponseBody
	@GetMapping("/delete_message/{id}")
	public String deleteMessage(@PathVariable long id) throws IOException {
		addBearerTokenToRestTemplate();
		ClientHttpRequest clientHttpRequest = createClientRequest("http://localhost:8082/app/delete_message/" + id, HttpMethod.DELETE);
		String response = executeRequestAndGetResponse(clientHttpRequest);
		return response;
	}
	
	@ResponseBody
	@GetMapping("/write_message/{text}")
	public String writeMessage(@PathVariable String text) throws IOException {
		addBearerTokenToRestTemplate();
		ClientHttpRequest clientHttpRequest = createClientRequest("http://localhost:8082/app/write_message", HttpMethod.POST);
		clientHttpRequest.getHeaders().add("Content-type", "application/json");
		ObjectMapper objectMapper = new ObjectMapper();
		clientHttpRequest.getBody().write(objectMapper.writeValueAsString(text).getBytes());
		String response = executeRequestAndGetResponse(clientHttpRequest);
		return response;
	}
	
	private void addBearerTokenToRestTemplate() {
		restTemplate.getInterceptors().add(tokenInterceptor());
	}
	
	private ClientHttpRequestInterceptor tokenInterceptor() {
		return (request, body, execution) -> {
			Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
			String accessToken = oAuth2AuthorizedClientService.loadAuthorizedClient("message_service", authentication.getName()).getAccessToken().getTokenValue();
			request.getHeaders().add("Authorization", "Bearer "  + accessToken);
			return execution.execute(request, body);
		};
	}
	
	private ClientHttpRequest createClientRequest(String uri, HttpMethod httpMethod) throws IOException {
		ClientHttpRequestFactory requestFactory = restTemplate.getRequestFactory();
		ClientHttpRequest clientHttpRequest = requestFactory.createRequest(URI.create(uri), httpMethod);
		return clientHttpRequest;
	}
	
	private String executeRequestAndGetResponse(ClientHttpRequest clientHttpRequest) throws IOException {
		ClientHttpResponse clientHttpResponse = clientHttpRequest.execute();
		Scanner scanner = new Scanner(clientHttpResponse.getBody());
		String response = scanner.nextLine();
		scanner.close();
		return response;
	}
	
}

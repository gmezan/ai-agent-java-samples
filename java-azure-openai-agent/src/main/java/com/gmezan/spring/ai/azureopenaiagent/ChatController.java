package com.gmezan.spring.ai.azureopenaiagent;

import com.google.gson.Gson;
import lombok.AllArgsConstructor;
import org.springframework.ai.azure.openai.AzureOpenAiChatModel;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static org.springframework.http.MediaType.APPLICATION_NDJSON_VALUE;


@AllArgsConstructor
@RestController
public class ChatController {
	private final AzureOpenAiChatModel chatModel;
	private final Gson gson = new Gson();

	@GetMapping(value = "/ai/generate", produces = APPLICATION_NDJSON_VALUE)
	public Flux<Object> generate(@RequestParam(value = "message", defaultValue = "Who are you") String message) {
		var chatClient = ChatClient.create(chatModel)
				.prompt(message)
				.tools(new DateTimeTools());

		return chatClient.stream().content()
				.cast(Object.class);
	}
}

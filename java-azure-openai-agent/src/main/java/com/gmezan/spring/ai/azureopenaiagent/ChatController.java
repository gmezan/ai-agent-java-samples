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


@AllArgsConstructor
@RestController
public class ChatController {
	private final AzureOpenAiChatModel chatModel;
	private final Gson gson = new Gson();

	@GetMapping("/ai/generate")
	public Flux<Object> generate(@RequestParam(value = "message", defaultValue = "Tell me a joke") String message) {
		var chatClient = ChatClient.create(chatModel)
				.prompt("What day is tomorrow?")
				.tools(new DateTimeTools());

		return chatClient.stream().chatResponse()
				.cast(Object.class);
	}
}

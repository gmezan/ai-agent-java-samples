package com.gmezan.spring.ai.azureopenaiagent;

import com.google.gson.Gson;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.azure.openai.AzureOpenAiChatModel;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;

import java.util.List;

import static org.springframework.http.MediaType.APPLICATION_NDJSON_VALUE;

@Slf4j
@AllArgsConstructor
@RestController
public class ChatController {
	private final AzureOpenAiChatModel chatModel;
	private final Gson gson = new Gson();

	private static final String A_ROLE = "ASSISTANT";

	@CrossOrigin
	@PostMapping(value = "/api/chat", produces = MediaType.APPLICATION_NDJSON_VALUE)
	public Mono<ResponseEntity<Flux<Object>>> generate(@RequestParam(value = "message", defaultValue = "Who are you") String message) {
		Prompt prompt = new Prompt();

		log.info("Starting, message: {}", message);

		var chatClient = ChatClient.create(chatModel)
				.prompt(message)
				.tools(new DateTimeTools());

		return Mono.just(chatClient.stream().content()
				.index()
				.map(this::mapResponse)
				.cast(Object.class))
				.map(ResponseEntity::ok);
	}

	private ResponseModel mapResponse(Tuple2<Long, String> contents) {

		long order = contents.getT1(); // Extract the Flux order
		String content = contents.getT2();

		return ResponseModel.builder()
				.choices(List.of(ResponseModel.Choice.builder()
						.index((int) order)
						.delta(ResponseModel.Message.builder()
								.content(content)
								.role(A_ROLE)
								.build())
						.message(ResponseModel.Message.builder()
								.content(content)
								.role(A_ROLE)
								.build())
						.build()))
				.build();
	}
}

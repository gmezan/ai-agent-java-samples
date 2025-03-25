package com.gmezan.ai.skagent;

import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@AllArgsConstructor
@RestController
public class ChatController {

	private final ChatService chatService;

	@GetMapping("/ai/generate")
	public Mono<Object> generate(@RequestParam(value = "message", defaultValue = "Tell me a joke") String message) {

		return Mono.defer(() -> chatService.getCompletion(message));
	}

	@GetMapping("/ai/generateStream")
	public Flux<Object> generateStream(@RequestParam(value = "message", defaultValue = "Tell me a joke") String message) {
		return chatService.getCompletionStream(message);
	}
}
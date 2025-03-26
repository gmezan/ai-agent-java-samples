package com.gmezan.ai.skagent.controller;

import com.gmezan.ai.skagent.agent.SearchAgent;
import com.gmezan.ai.skagent.model.web.ChatRequest;
import com.gmezan.ai.skagent.model.web.ResponseModel;
import com.gmezan.ai.skagent.service.ChatService;
import com.microsoft.semantickernel.services.KernelContent;
import com.microsoft.semantickernel.services.chatcompletion.AuthorRole;
import com.microsoft.semantickernel.services.chatcompletion.ChatHistory;
import com.microsoft.semantickernel.services.chatcompletion.ChatMessageContent;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@AllArgsConstructor
@RestController
@CrossOrigin
public class ChatController {
	private final ChatService chatService;
	private final SearchAgent searchAgent;

	@PostMapping(value = "/api/chat", produces = MediaType.APPLICATION_JSON_VALUE,
		consumes = MediaType.APPLICATION_JSON_VALUE)
	public Mono<ResponseModel> generate(@RequestBody ChatRequest request) {
		return Mono.justOrEmpty(request)
				.map(this::toChatHistory)
				.flatMap(chatService::getCompletion)
				.flatMapMany(Flux::fromIterable)
				.last()
				.mapNotNull(ChatMessageContent::getContent)
				.map(content -> this.mapResponse(0, content, buildContext()));
	}

	@PostMapping(value = "/api/chat", produces = MediaType.APPLICATION_NDJSON_VALUE,
			consumes = MediaType.APPLICATION_JSON_VALUE)
	public Flux<ResponseModel> generateStream(@RequestBody ChatRequest request) {

		log.info("Request: {}", request);

		return Mono.justOrEmpty(request)
				.map(this::toChatHistory)
				.flatMapMany(searchAgent::chat)
				.mapNotNull(KernelContent::getContent)
				.onErrorComplete()
				.index()
				.map(tuple -> mapResponse(tuple.getT1() + 1, tuple.getT2(), null))
				.startWith(mapResponse(0,"", buildContext()));
	}

	private ChatHistory toChatHistory(ChatRequest request) {
		ChatHistory chatHistory = new ChatHistory();
		chatHistory.addAll(
				request.getMessages()
						.stream()
						.map(m -> new ChatMessageContent<>(
								AuthorRole.valueOf(m.getRole().toUpperCase()),
								m.getContent()
						))
						.collect(Collectors.toUnmodifiableList())
		);

		return chatHistory;
	}

	private Object buildContext() {
		final Map<String, Object> context = new HashMap<>();

		// system prompt/ chat history?
		context.put("thoughts", "");
		// from search
		context.put("data_points", List.of());

		return context;
	}

	// citations go in `content` in the following format [<file-name>#page=<page-number>]
	private ResponseModel mapResponse(long order, String content, Object context) {

		return ResponseModel.builder()
				.choices(List.of(ResponseModel.Choice.builder()
						.context(context)
						.index((int) order)
						.delta(ResponseModel.Message.builder()
								.content(content)
								.role(AuthorRole.ASSISTANT.name())
								.build())
						.message(ResponseModel.Message.builder()
								.content(content)
								.role(AuthorRole.ASSISTANT.name())
								.build())
						.build()))
				.build();
	}
}
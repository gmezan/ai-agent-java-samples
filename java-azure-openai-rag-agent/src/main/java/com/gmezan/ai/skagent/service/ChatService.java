package com.gmezan.ai.skagent.service;

import com.google.gson.Gson;
import com.microsoft.semantickernel.Kernel;
import com.microsoft.semantickernel.contextvariables.ContextVariableTypeConverter;
import com.microsoft.semantickernel.contextvariables.ContextVariableTypes;
import com.microsoft.semantickernel.orchestration.InvocationContext;
import com.microsoft.semantickernel.orchestration.InvocationReturnMode;
import com.microsoft.semantickernel.orchestration.PromptExecutionSettings;
import com.microsoft.semantickernel.orchestration.ToolCallBehavior;
import com.microsoft.semantickernel.services.chatcompletion.ChatCompletionService;
import com.microsoft.semantickernel.services.chatcompletion.ChatHistory;
import com.microsoft.semantickernel.services.chatcompletion.ChatMessageContent;
import com.microsoft.semantickernel.services.chatcompletion.StreamingChatContent;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
public class ChatService {
	private final ChatCompletionService chatCompletionService;
	private final Kernel kernel;
	private final InvocationContext invocationContext;

	public ChatService(ChatCompletionService chatCompletionService,
										 PromptExecutionSettings promptExecutionSettings,
										 Kernel kernel) {
		this.chatCompletionService = chatCompletionService;
		this.kernel = kernel;

		ContextVariableTypes
				.addGlobalConverter(
						ContextVariableTypeConverter.builder(Object.class)
								.toPromptString(new Gson()::toJson)
								.build());

		this.invocationContext = new InvocationContext.Builder()
				.withPromptExecutionSettings(promptExecutionSettings)
				.withReturnMode(InvocationReturnMode.NEW_MESSAGES_ONLY)
				.withToolCallBehavior(ToolCallBehavior.allowAllKernelFunctions(false))
				.build();
	}

	public Mono<List<ChatMessageContent<?>>> getCompletion(String message) {
		ChatHistory chatHistory = new ChatHistory()
				.addUserMessage(message);
		return chatCompletionService.getChatMessageContentsAsync(chatHistory, kernel, invocationContext);
	}

	public Flux<StreamingChatContent<?>> getCompletionStream(String message) {
		ChatHistory chatHistory = new ChatHistory()
				.addUserMessage(message);

		return chatCompletionService.getStreamingChatMessageContentsAsync(chatHistory, kernel, invocationContext);
	}

	public Mono<List<ChatMessageContent<?>>> getCompletion(ChatHistory chatHistory) {
		return chatCompletionService.getChatMessageContentsAsync(chatHistory, kernel, invocationContext);
	}

	public Flux<StreamingChatContent<?>> getCompletionStream(ChatHistory chatHistory) {

		return chatCompletionService.getStreamingChatMessageContentsAsync(chatHistory, kernel, invocationContext);
	}
}

package com.gmezan.ai.skagent;

import com.azure.ai.openai.OpenAIAsyncClient;
import com.azure.ai.openai.OpenAIClientBuilder;
import com.google.gson.Gson;
import com.microsoft.semantickernel.Kernel;
import com.microsoft.semantickernel.aiservices.openai.chatcompletion.OpenAIChatCompletion;
import com.microsoft.semantickernel.contextvariables.ContextVariableTypeConverter;
import com.microsoft.semantickernel.contextvariables.ContextVariableTypes;
import com.microsoft.semantickernel.orchestration.InvocationContext;
import com.microsoft.semantickernel.orchestration.InvocationReturnMode;
import com.microsoft.semantickernel.orchestration.PromptExecutionSettings;
import com.microsoft.semantickernel.orchestration.ToolCallBehavior;
import com.microsoft.semantickernel.services.chatcompletion.ChatCompletionService;
import com.microsoft.semantickernel.services.chatcompletion.ChatHistory;
import org.springframework.ai.azure.openai.AzureOpenAiChatModel;
import org.springframework.ai.azure.openai.AzureOpenAiChatOptions;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class ChatService {
	private final AzureOpenAiChatModel chatModel;
	private final AzureOpenAiChatOptions options;
	private final OpenAIAsyncClient openAIAsyncClient;
	private final ChatCompletionService chatCompletionService;
	private final Kernel kernel;
	private final InvocationContext invocationContext;

	public ChatService(AzureOpenAiChatModel chatModel,
										 OpenAIClientBuilder openAIClientBuilder) {
		this.chatModel = chatModel;
		this.options = chatModel.getDefaultOptions();
		this.openAIAsyncClient = openAIClientBuilder.buildAsyncClient();

		this.chatCompletionService = OpenAIChatCompletion.builder()
				.withOpenAIAsyncClient(openAIAsyncClient)
				.withModelId(options.getModel())
				.build();
		this.kernel = Kernel.builder()
				.withAIService(ChatCompletionService.class, chatCompletionService)
						.build();

		ContextVariableTypes
				.addGlobalConverter(
						ContextVariableTypeConverter.builder(Object.class)
								.toPromptString(new Gson()::toJson)
								.build());

		this.invocationContext = new InvocationContext.Builder()
				.withPromptExecutionSettings(PromptExecutionSettings.builder()
						.withMaxTokens(options.getMaxTokens())
						.withTemperature(options.getTemperature())
						.build())
				.withReturnMode(InvocationReturnMode.NEW_MESSAGES_ONLY)
				.withToolCallBehavior(ToolCallBehavior.allowAllKernelFunctions(false))
				.build();
	}

	public Mono<Object> getCompletion(String message) {
		ChatHistory chatHistory = new ChatHistory()
				.addUserMessage(message);
		return chatCompletionService.getChatMessageContentsAsync(chatHistory, kernel, invocationContext)
				.cast(Object.class);
	}

	public Flux<Object> getCompletionStream(String message) {
		ChatHistory chatHistory = new ChatHistory()
				.addUserMessage(message);

		return chatCompletionService.getStreamingChatMessageContentsAsync(chatHistory, kernel, invocationContext)
				.cast(Object.class);
	}
}

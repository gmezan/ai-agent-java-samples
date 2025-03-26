package com.gmezan.ai.skagent.agent;

import com.gmezan.ai.skagent.service.SearchService;
import com.microsoft.semantickernel.Kernel;
import com.microsoft.semantickernel.orchestration.InvocationContext;
import com.microsoft.semantickernel.orchestration.InvocationReturnMode;
import com.microsoft.semantickernel.orchestration.PromptExecutionSettings;
import com.microsoft.semantickernel.orchestration.ToolCallBehavior;
import com.microsoft.semantickernel.services.chatcompletion.ChatCompletionService;
import com.microsoft.semantickernel.services.chatcompletion.ChatHistory;
import com.microsoft.semantickernel.services.chatcompletion.ChatMessageContent;
import com.microsoft.semantickernel.services.chatcompletion.StreamingChatContent;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
public class SearchAgent {
	private static final String INITIAL_PROMPT = """
			You are an AI assistant that helps users learn from the information
     found in the source material.
			""";
	private static final String GROUNDED_PROMPT = """
    Answer the user question using only the sources provided below.
    Use bullets if the answer has multiple points.
    If the answer is longer than 3 sentences, provide a summary.
    Answer ONLY with the facts listed in the list of sources below.
     Cite your source when you answer the question using square brackets like this: [<title>]
    If there isn't enough information below, say you don't know.
    Do not generate answers that don't use the sources below.
    Sources:\n{sources}
    """;

	private final Kernel kernel;
	private final ChatCompletionService chat;
	private final InvocationContext invocationContext;
	private final SearchService searchService;

	public SearchAgent(Kernel kernel, ChatCompletionService chat, PromptExecutionSettings promptExecutionSettings, SearchService searchService) {
		this.kernel = kernel;
		this.chat = chat;
		this.searchService = searchService;
		this.invocationContext = new InvocationContext.Builder()
				.withPromptExecutionSettings(promptExecutionSettings)
				.withReturnMode(InvocationReturnMode.NEW_MESSAGES_ONLY)
				.withToolCallBehavior(ToolCallBehavior.allowAllKernelFunctions(false))
				.build();
	}


	public Flux<StreamingChatContent<?>> chat(ChatHistory chatHistory) {
		ChatHistory forSearch = new ChatHistory()
				.addSystemMessage(INITIAL_PROMPT);
		forSearch.addAll(chatHistory);

		return Mono.justOrEmpty(chatHistory.getLastMessage())
				.mapNotNull(ChatMessageContent::getContent)
				.flatMapMany(prompt -> searchService.search(prompt)
						.reduce(new StringBuilder(), (prev, next) -> prev
								.append("TITLE: ").append(next.getTitle()).append(",")
								.append("CONTENT: ").append(next.getChunk()).append(",")
								.append("================\n"))
						.map(sources -> GROUNDED_PROMPT.replace("{sources}",
								sources.toString()))
						.map(sp -> forSearch.addSystemMessage(sp)
								.addUserMessage(prompt))
						.flatMapMany(ch -> chat.getStreamingChatMessageContentsAsync(ch, kernel, invocationContext))
				);
	}
}

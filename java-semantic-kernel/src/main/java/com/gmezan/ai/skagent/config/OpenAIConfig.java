package com.gmezan.ai.skagent.config;

import com.azure.ai.openai.OpenAIAsyncClient;
import com.azure.ai.openai.OpenAIClientBuilder;
import com.azure.core.credential.AzureKeyCredential;
import com.microsoft.semantickernel.Kernel;
import com.microsoft.semantickernel.aiservices.openai.chatcompletion.OpenAIChatCompletion;
import com.microsoft.semantickernel.orchestration.PromptExecutionSettings;
import com.microsoft.semantickernel.services.chatcompletion.ChatCompletionService;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@RefreshScope
@Setter
@Configuration
@ConfigurationProperties(value = "azure.openai")
public class OpenAIConfig {
	private String endpoint;
	private String apiKey;
	private String model;

	@Bean
	public OpenAIAsyncClient openAIAsyncClient() {
		return new OpenAIClientBuilder()
				.credential(new AzureKeyCredential(apiKey))
				.endpoint(endpoint)
				.buildAsyncClient();
	}

	@Bean
	public ChatCompletionService chatCompletionService(OpenAIAsyncClient client) {
		return OpenAIChatCompletion.builder()
				.withOpenAIAsyncClient(client)
				.withModelId(model)
				.build();
	}

	@Bean
	public Kernel kernel(ChatCompletionService chatCompletionService) {
		return Kernel.builder()
				.withAIService(ChatCompletionService.class, chatCompletionService)
				.build();
	}

	@Bean
	public PromptExecutionSettings promptSettings() {
		return PromptExecutionSettings.builder()
				.withMaxTokens(300)
				.withResultsPerPrompt(1)
				.build();
	}
}

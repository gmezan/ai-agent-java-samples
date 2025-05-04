package com.example.openaimsi;

import com.azure.ai.openai.OpenAIClient;
import com.azure.ai.openai.OpenAIClientBuilder;
import com.azure.ai.openai.models.ChatCompletionsOptions;
import com.azure.ai.openai.models.ChatRequestMessage;
import com.azure.ai.openai.models.ChatRequestSystemMessage;
import com.azure.core.credential.TokenCredential;
import com.azure.identity.DefaultAzureCredentialBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class Application implements ApplicationRunner {
	@Value("${azure.openai.endpoint}")
	private String endpoint;
	@Value("${azure.openai.model}")
	private String model;

	@Override
	public void run(ApplicationArguments args) {
		TokenCredential defaultCredential = new DefaultAzureCredentialBuilder().build();

		OpenAIClient client = new OpenAIClientBuilder()
				.credential(defaultCredential)
				.endpoint(endpoint)
				.buildClient();

		List<ChatRequestMessage> chatMessages = new ArrayList<>();
		chatMessages.add(new ChatRequestSystemMessage("Tell me a joke and a random fact"));

		var response = client.getChatCompletions(model, new ChatCompletionsOptions(chatMessages));
		System.out.printf("Model ID=%s is created at %s.%n", response.getId(), response.getCreatedAt());
		response.getChoices()
				.forEach(c -> System.out.println(c.getMessage().getContent()));
	}
}

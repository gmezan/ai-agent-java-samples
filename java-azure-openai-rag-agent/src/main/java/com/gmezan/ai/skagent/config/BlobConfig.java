package com.gmezan.ai.skagent.config;

import com.azure.storage.blob.BlobContainerAsyncClient;
import com.azure.storage.blob.BlobServiceAsyncClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Setter
@Configuration
@ConfigurationProperties("azure.storage")
public class BlobConfig {
	private String connectionString;
	private String container;

	@Bean
	public BlobServiceAsyncClient blobServiceAsyncClient() {
		return new BlobServiceClientBuilder()
				.connectionString(connectionString)
				.buildAsyncClient();
	}

	@Bean
	public BlobContainerAsyncClient blobContainerAsyncClient(BlobServiceAsyncClient blobServiceAsyncClient) {
		return blobServiceAsyncClient.getBlobContainerAsyncClient(container);
	}

}

package com.gmezan.ai.skagent.config;

import com.azure.core.credential.AzureKeyCredential;
import com.azure.search.documents.SearchAsyncClient;
import com.azure.search.documents.SearchClient;
import com.azure.search.documents.SearchClientBuilder;
import com.azure.search.documents.indexes.SearchIndexAsyncClient;
import com.azure.search.documents.indexes.SearchIndexClientBuilder;
import lombok.Data;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@RefreshScope
@Setter
@Slf4j
@Configuration
@ConfigurationProperties(value = "azure.search")
public class SearchConfig {
	private String endpoint;
	private String key;
	private String indexName;

	@Bean
	public SearchIndexAsyncClient searchIndexClient() {
		return new SearchIndexClientBuilder()
				.endpoint(endpoint)
				.credential(new AzureKeyCredential(key))
				.buildAsyncClient();
	}

	@Bean
	public SearchAsyncClient searchAsyncClient() {
		return new SearchClientBuilder()
				.endpoint(endpoint)
				.credential(new AzureKeyCredential(key))
				.indexName(indexName)
				.buildAsyncClient();
	}

	@Bean
	public SearchClient client() {
		return new SearchClientBuilder()
				.endpoint(endpoint)
				.indexName(indexName)
				.credential(new AzureKeyCredential(key))
				.buildClient();
	}
}

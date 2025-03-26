package com.gmezan.ai.skagent.service.impl;


import com.azure.json.JsonProviders;
import com.azure.json.JsonReader;
import com.azure.search.documents.SearchAsyncClient;
import com.azure.search.documents.models.QueryType;
import com.azure.search.documents.models.SearchOptions;
import com.azure.search.documents.models.VectorQuery;
import com.azure.search.documents.models.VectorSearchOptions;
import com.gmezan.ai.skagent.model.search.RagDocument;
import com.gmezan.ai.skagent.service.SearchService;
import com.google.gson.Gson;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

import java.io.IOException;

@Slf4j
@RequiredArgsConstructor
@Service
public class SearchServiceImpl implements SearchService {
	private final SearchAsyncClient searchAsyncClient;
	private final Gson gson = new Gson();

	@Override
	public Flux<RagDocument> search(String query) {
		String vectorQuery = "{\"kind\": \"text\",\"text\": \"*\",\"fields\": \"text_vector\"}";

		JsonReader jsonReader = null;
		try {
			jsonReader = JsonProviders.createReader(vectorQuery);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		try {
			return searchAsyncClient.search(query,
							new SearchOptions()
									.setQueryType(QueryType.SIMPLE)
									.setVectorSearchOptions(
											new VectorSearchOptions()
													.setQueries(VectorQuery.fromJson(jsonReader)))
									.setSelect("title", "chunk")
									.setTop(5)
					)
					.map(t -> t.getDocument(RagDocument.class))
					.subscribeOn(Schedulers.boundedElastic());
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}

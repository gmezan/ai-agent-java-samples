package com.gmezan.ai.skagent.service;

import com.gmezan.ai.skagent.model.search.RagDocument;
import reactor.core.publisher.Flux;

public interface SearchService {
	Flux<RagDocument> search(String query);
}

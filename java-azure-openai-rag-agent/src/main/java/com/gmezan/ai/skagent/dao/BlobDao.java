package com.gmezan.ai.skagent.dao;

import com.azure.core.util.BinaryData;
import com.azure.storage.blob.BlobContainerAsyncClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Slf4j
@RequiredArgsConstructor
@Component
public class BlobDao {
	private final BlobContainerAsyncClient client;

	public Mono<BinaryData> getFile(String fileName) {
		log.info("fileName: {}", fileName);

		return client.listBlobs()
				.collectList()
				.doOnSuccess(f ->
						log.info("listing: {}", f))
				.then(client.getBlobAsyncClient(fileName)
						.downloadContent());
	}

}

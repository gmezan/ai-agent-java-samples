package com.gmezan.ai.skagent.controller;

import com.gmezan.ai.skagent.dao.BlobDao;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.MimeTypeUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import reactor.core.publisher.Mono;

import java.net.URLConnection;

@Slf4j
@RequiredArgsConstructor
@Controller
public class ContentController {
	private final BlobDao blobDao;

	@GetMapping("/api/content/{fileName}")
	public Mono<ResponseEntity<Resource>> getContent(@PathVariable String fileName) {
		log.info("Received request for  content with name [{}] ]", fileName);

		if (!StringUtils.hasText(fileName)) {
			log.warn("file name cannot be null");
			return Mono.justOrEmpty(ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null));
		}

		String mimeType = URLConnection.guessContentTypeFromName(fileName);

		MediaType contentType = new MediaType(MimeTypeUtils.parseMimeType(mimeType));

		return blobDao.getFile(fileName)
				.map(c -> ResponseEntity.ok()
						.header("Content-Disposition", "inline; filename=%s".formatted(fileName))
						.contentType(contentType)
						.body(new ByteArrayResource(c.toBytes())));
	}
}

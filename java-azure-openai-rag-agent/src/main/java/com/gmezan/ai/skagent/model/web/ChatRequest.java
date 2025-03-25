package com.gmezan.ai.skagent.model.web;

import lombok.Data;
import lombok.ToString;

import java.util.List;

@ToString
@Data
public class ChatRequest {
	private List<Message> messages;
	private Object sessionState; // Nullable field

	@Data
	public static class Message {
		private String content;
		private String role;
	}
}
package com.gmezan.spring.ai.azureopenaiagent;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class ResponseModel {
	private List<Choice> choices;

	@AllArgsConstructor
	@NoArgsConstructor
	@Builder
	@Data
	public static class Choice {
		private int index;
		private Message message;
		private Object context; // Can be changed to a specific type if needed
		private Message delta;
	}

	@AllArgsConstructor
	@NoArgsConstructor
	@Builder
	@Data
	public static class Message {
		private String content;
		private String role;
	}

}

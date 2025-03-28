package com.gmezan.ai.skagent.model.search;

import com.azure.search.documents.indexes.SimpleField;
import lombok.Data;
import lombok.ToString;

import java.util.List;

@ToString
@Data
public class RagDocument {
	@SimpleField
	private String parentId;
	@SimpleField
	private String title;
	@SimpleField(isKey = true)
	private String chunkId;
	@SimpleField
	private String chunk;
	@SimpleField
	private List<String> skills;
	@SimpleField
	private List<String> products;
}

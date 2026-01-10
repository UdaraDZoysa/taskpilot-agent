package com.harsha.taskpilot.tools;

import jakarta.annotation.PostConstruct;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.util.List;

@Component
public class RagSearchTool implements Tool {

    private final VectorStore vectorStore;

    public RagSearchTool(EmbeddingModel embeddingModel) {
        this.vectorStore = SimpleVectorStore.builder(embeddingModel).build();
    }

    @PostConstruct
    public void load() throws Exception {
        var resource = new ClassPathResource("knowledge.txt");
        String text = Files.readString(resource.getFile().toPath());
        vectorStore.add(List.of(new Document(text)));
    }

    @Override
    public String name() {
        return "RagSearchTool";
    }

    @Override
    public String execute(String query) {
        var results = vectorStore.similaritySearch(query);
        return results.isEmpty()
                ? "No relevant knowledge found"
                : results.get(0).getText();
    }
}

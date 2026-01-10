package com.harsha.taskpilot.agent;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class AgentMemory {
    private final List<String> history = new ArrayList<>();

    public void remember(String entry) {
        history.add(entry);
    }

    public List<String> recall() {
        return List.copyOf(history);
    }

    public void clear() {
        history.clear();
    }
}

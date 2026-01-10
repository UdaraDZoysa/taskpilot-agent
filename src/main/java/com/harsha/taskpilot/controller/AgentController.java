package com.harsha.taskpilot.controller;

import com.harsha.taskpilot.agent.TaskAgent;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/agent")
public class AgentController {
    private final TaskAgent taskAgent;

    public AgentController(TaskAgent taskAgent) {
        this.taskAgent = taskAgent;
    }

    @PostMapping("/execute")
    public String execute(@RequestBody String goal) {
        return taskAgent.execute(goal);
    }

    @GetMapping("/memory")
    public List<String> memory() {
        return taskAgent.memory();
    }
}

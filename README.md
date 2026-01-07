# TaskPilot — A Goal-Driven AI Agent Built with Spring AI

TaskPilot is a backend-focused AI agent system built using Java, Spring Boot, Spring AI, and Ollama.  
Unlike a basic chatbot, TaskPilot is designed around agent architecture — it can plan tasks, use tools, and retain memory across executions.

This project focuses on engineering discipline and system design, showing how agentic AI can be implemented cleanly in a Java backend.

---

## Project Status

This project is actively under development.

### Currently implemented
- Goal-driven task execution
- LLM-based task planning
- Tool abstraction and execution
- Local LLM integration using Ollama

### Planned
- Agent memory (stateful context)
- Retrieval-Augmented Generation (RAG) as a tool
- Embedding-based document retrieval
- Smarter, LLM-driven tool selection
- Optional persistent memory

---

## Why TaskPilot Exists

Most AI demos stop at “send a prompt, get a response”.  
That approach does not scale to real systems.

TaskPilot explores a more realistic design:

- Separate reasoning from execution
- Keep state and memory in the application layer
- Use LLMs as decision engines, not controllers
- Design the system so new capabilities can be added without refactoring

This mirrors how AI features are built in production backend teams.

---

## What Makes This an AI Agent (Not a Chatbot)

TaskPilot satisfies the core characteristics of an AI agent:

### 01. Goal-driven
The system accepts a high-level goal rather than a fixed command.

### 02. Planning
A dedicated planner component breaks goals into ordered steps.

### 03. Tool usage
The agent can invoke tools to perform real work instead of only generating text.

Memory and knowledge retrieval will be added as dedicated agent capabilities in later steps.

---

## High-Level Architecture

```

  Client (Postman / API)
           |
    AgentController
           |
       TaskAgent
      /         \        
Planner        Tools 
   |
LLM (Ollama)

````

**Key principle:**  
The agent orchestrates behavior.  
The LLM never directly controls the system.

---

## Core Components

### TaskAgent
The central orchestrator responsible for:
- Receiving user goals
- Calling the planner
- Selecting and executing tools
- Recording outcomes in memory

### TaskPlanner
Uses an LLM to convert a user goal into a step-by-step plan.  
It does not execute any actions.

### Tools
Encapsulated units of action with clear responsibilities.

**Current tool:**
- TextAnalysisTool — analyzes Java or text input and suggests improvements

---

## API Endpoints

### Execute an Agent Task
**POST** `/agent/execute`

Request body (raw text):
```text
Analyze this Java code and suggest improvements:

public class Test {
    public void run() {}
}
````

Response includes:

* Generated execution plan
* Tool execution result

---

## Technology Stack

* Java 17
* Spring Boot 3.x
* Spring AI
* Ollama (local LLM runtime)
* qwen2.5:3b (chat model)
* Maven

All components run locally, with no external AI APIs required.

---

## Design Principles Followed

### Separation of concerns

Controllers handle HTTP, agents orchestrate behavior, tools perform actions.

### Constructor-based dependency injection

No field injection or hidden dependencies.

### Explicit state management

Memory is owned by the application, not the LLM.

### Incremental extensibility

New tools and features can be added without rewriting existing logic.

---

## Future Direction

TaskPilot is intentionally structured to support:

* Agent memory for contextual continuity
* Retrieval-Augmented Generation (RAG)
* Knowledge-grounded, explainable responses

These capabilities will be added as agent extensions, preserving the current architecture.

---

## Summary

TaskPilot demonstrates how agentic AI systems can be engineered cleanly in Java using Spring Boot and Spring AI.

It emphasizes:

* Clear architecture
* Responsible LLM usage
* Maintainable, extensible design

This project reflects real backend AI system design, not just prompt-driven demos.

```

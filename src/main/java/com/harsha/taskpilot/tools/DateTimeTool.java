package com.harsha.taskpilot.tools;

import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class DateTimeTool implements Tool{
    @Override
    public String name() {
        return "DateTimeTool";
    }

    @Override
    public String execute(String input) {
        return LocalDateTime.now().toString();
    }
}

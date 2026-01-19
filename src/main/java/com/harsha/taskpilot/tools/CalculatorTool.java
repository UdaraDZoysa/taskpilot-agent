package com.harsha.taskpilot.tools;

import org.springframework.stereotype.Component;

@Component
public class CalculatorTool implements Tool{
    @Override
    public String name() {
        return "CalculatorTool";
    }

    @Override
    public String execute(String input) {
        try {
            return String.valueOf(new javax.script.ScriptEngineManager()
                    .getEngineByName("JavaScript")
                    .eval(input));
        } catch (Exception e) {
            return "Invalid math expression";
        }
    }
}

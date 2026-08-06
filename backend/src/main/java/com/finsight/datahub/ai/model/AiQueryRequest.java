package com.finsight.datahub.ai.model;

import jakarta.validation.constraints.NotBlank;

public class AiQueryRequest {

    @NotBlank(message = "Natural language question cannot be blank")
    private String question;

    public AiQueryRequest() {}

    public AiQueryRequest(String question) {
        this.question = question;
    }

    public String getQuestion() { return question; }
    public void setQuestion(String question) { this.question = question; }
}

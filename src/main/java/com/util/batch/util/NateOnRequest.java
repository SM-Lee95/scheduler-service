package com.util.batch.util;

import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;


@Component
@Setter
public class NateOnRequest {
    private String content;
    @Value("${nateon.baseurl}")
    private String baseUrl;
    @Value("${nateon.webhookuri}")
    private String webhookUri;
    public void setContent(String content, String errorMessage) {
        this.content = content + "\n" + errorMessage;
    }
    public String callAPI() {
        WebClient webClient = WebClient.create(baseUrl);
        return webClient
                .post()
                .uri(webhookUri)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData("content", content))
                .retrieve()
                .bodyToMono(String.class)
                .block();
    }
}

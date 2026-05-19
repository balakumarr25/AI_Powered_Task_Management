package com.taskportal.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "app")
public class AppProperties {

    private Jwt jwt = new Jwt();
    private Cors cors = new Cors();
    private Ai ai = new Ai();

    public Jwt getJwt() { return jwt; }
    public void setJwt(Jwt jwt) { this.jwt = jwt; }
    public Cors getCors() { return cors; }
    public void setCors(Cors cors) { this.cors = cors; }
    public Ai getAi() { return ai; }
    public void setAi(Ai ai) { this.ai = ai; }

    public static class Jwt {
        private String secret;
        private long expirationMs;
        public String getSecret() { return secret; }
        public void setSecret(String secret) { this.secret = secret; }
        public long getExpirationMs() { return expirationMs; }
        public void setExpirationMs(long expirationMs) { this.expirationMs = expirationMs; }
    }

    public static class Cors {
        private String allowedOrigins;
        public String getAllowedOrigins() { return allowedOrigins; }
        public void setAllowedOrigins(String allowedOrigins) { this.allowedOrigins = allowedOrigins; }
    }

    public static class Ai {
        private String provider;
        private String openaiApiKey;
        private String openaiModel;
        private String geminiApiKey;
        private String geminiModel;
        private String huggingfaceApiKey;
        private String huggingfaceModel;

        public String getProvider() { return provider; }
        public void setProvider(String provider) { this.provider = provider; }
        public String getOpenaiApiKey() { return openaiApiKey; }
        public void setOpenaiApiKey(String openaiApiKey) { this.openaiApiKey = openaiApiKey; }
        public String getOpenaiModel() { return openaiModel; }
        public void setOpenaiModel(String openaiModel) { this.openaiModel = openaiModel; }
        public String getGeminiApiKey() { return geminiApiKey; }
        public void setGeminiApiKey(String geminiApiKey) { this.geminiApiKey = geminiApiKey; }
        public String getGeminiModel() { return geminiModel; }
        public void setGeminiModel(String geminiModel) { this.geminiModel = geminiModel; }
        public String getHuggingfaceApiKey() { return huggingfaceApiKey; }
        public void setHuggingfaceApiKey(String huggingfaceApiKey) { this.huggingfaceApiKey = huggingfaceApiKey; }
        public String getHuggingfaceModel() { return huggingfaceModel; }
        public void setHuggingfaceModel(String huggingfaceModel) { this.huggingfaceModel = huggingfaceModel; }
    }
}

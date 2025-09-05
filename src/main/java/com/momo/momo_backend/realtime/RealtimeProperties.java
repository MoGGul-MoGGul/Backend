package com.momo.momo_backend.realtime;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties(prefix = "realtime")
public class RealtimeProperties {
    private String broker = "simple";
    private String endpoint = "/ws";
    private String appPrefix = "/app";
    private String userDestinationPrefix = "/user";
    private Topics topics = new Topics();
    private String schemaVersion = "v1";
    private List<String> corsAllowedOrigins = List.of("http://localhost:3000");

    public String getBroker() { return broker; }
    public void setBroker(String broker) { this.broker = broker; }

    public String getEndpoint() { return endpoint; }
    public void setEndpoint(String endpoint) { this.endpoint = endpoint; }

    public String getAppPrefix() { return appPrefix; }
    public void setAppPrefix(String appPrefix) { this.appPrefix = appPrefix; }

    public String getUserDestinationPrefix() { return userDestinationPrefix; }
    public void setUserDestinationPrefix(String userDestinationPrefix) { this.userDestinationPrefix = userDestinationPrefix; }

    public Topics getTopics() { return topics; }
    public void setTopics(Topics topics) { this.topics = topics; }

    public String getSchemaVersion() { return schemaVersion; }
    public void setSchemaVersion(String schemaVersion) { this.schemaVersion = schemaVersion; }

    public List<String> getCorsAllowedOrigins() { return corsAllowedOrigins; }
    public void setCorsAllowedOrigins(List<String> corsAllowedOrigins) { this.corsAllowedOrigins = corsAllowedOrigins; }

    public static class Topics {
        private String feed = "/topic/feed";
        private String challengeRank = "/topic/challenge/{id}/rank";
        private String userNotificationQueue = "/queue/notifications";

        public String getFeed() { return feed; }
        public void setFeed(String feed) { this.feed = feed; }

        public String getChallengeRank() { return challengeRank; }
        public void setChallengeRank(String challengeRank) { this.challengeRank = challengeRank; }

        public String getUserNotificationQueue() { return userNotificationQueue; }
        public void setUserNotificationQueue(String userNotificationQueue) { this.userNotificationQueue = userNotificationQueue; }
    }
}

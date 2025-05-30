package com.tcc.tccbackend.Model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;

@Data
@Document(collection = "system_logs")
public class SystemLog {

    @Id
    private String id;

    private String level;
    private String message;
    private String service;
    private String methodName;
    private LocalDateTime timestamp;
    private String userId;
    private String ipAddress;
    private String stackTrace;

    public SystemLog() {
        this.timestamp = LocalDateTime.now();
    }

    public static class Builder {
        private final SystemLog log = new SystemLog();

        public Builder level(String level) { log.setLevel(level); return this; }
        public Builder message(String message) { log.setMessage(message); return this; }
        public Builder service(String service) { log.setService(service); return this; }
        public Builder methodName(String methodName) { log.setMethodName(methodName); return this; }
        public Builder userId(String userId) { log.setUserId(userId); return this; }
        public Builder ipAddress(String ipAddress) { log.setIpAddress(ipAddress); return this; }
        public Builder stackTrace(String stackTrace) { log.setStackTrace(stackTrace); return this; }

        public SystemLog build() { return log; }
    }
}

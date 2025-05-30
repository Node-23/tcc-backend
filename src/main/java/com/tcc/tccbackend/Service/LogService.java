package com.tcc.tccbackend.Service;

import com.tcc.tccbackend.Model.SystemLog;
import com.tcc.tccbackend.Repository.SystemLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class LogService {

    private final SystemLogRepository logRepository;

    public LogService(SystemLogRepository logRepository) {
        this.logRepository = logRepository;
    }

    public void log(String level, String message, String service, String methodName, String userId, String ipAddress, String stackTrace) {
        SystemLog logEntry = new SystemLog.Builder()
                .level(level)
                .message(message)
                .service(service)
                .methodName(methodName)
                .userId(userId)
                .ipAddress(ipAddress)
                .stackTrace(stackTrace)
                .build();
        logEntry.setTimestamp(LocalDateTime.now());

        logRepository.save(logEntry);
    }

    public void info(String message, String service, String methodName, String userId) {
        log("INFO", message, service, methodName, userId, null, null);
    }

    public void warn(String message, String service, String methodName, String userId, String ipAddress, String stackTrace) {
        log("WARN", message, service, methodName, userId, ipAddress, stackTrace);
    }

    public void warn(String message, String service, String stackTrace) {
        log("WARN", message, service, "", "", "", stackTrace);
    }

    public void error(String message, String service, String methodName, String userId, String ipAddress, String stackTrace) {
        log("ERROR", message, service, methodName, userId, ipAddress, stackTrace);
    }

    public void error(String message, String service, String stackTrace) {
        log("ERROR", message, service, "", "", "", stackTrace);
    }

    public void debug(String message, String service, String methodName, String userId, String ipAddress) {
        log("DEBUG", message, service, methodName, userId, ipAddress, null);
    }
}
package com.example.selfblog.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class AuditLogService {
    private static final Logger LOGGER = LoggerFactory.getLogger("SECURITY_AUDIT");

    public void authentication(String outcome, String username, String ipAddress) {
        LOGGER.info("event=authentication outcome={} username={} ip={}", safe(outcome), safe(username), safe(ipAddress));
    }

    public void articleAction(String action, Long postId) {
        LOGGER.info("event=article_action action={} postId={}", safe(action), postId);
    }

    private String safe(String value) {
        return value == null ? "-" : value.replaceAll("[\\r\\n\\t]", "_");
    }
}

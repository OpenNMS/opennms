package org.opennms.web.springframework.security;

public interface OpenNMSLoginHandler extends LoginHandler {
    public boolean requiresAdminRole();
}

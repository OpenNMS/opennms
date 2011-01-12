package org.opennms.acl.conf;

import java.io.IOException;
import java.util.Properties;

import org.opennms.core.utils.LogUtils;

public class Config {

    public String getDbDriver() {
        return dbDriver;
    }

    public String getDbUser() {
        return dbUser;
    }

    public String getDbPass() {
        return dbPass;
    }

    public String getDbUrl() {
        return dbUrl;
    }

    public Config() {
        Properties props = new Properties();
        try {
            props.load(Config.class.getResourceAsStream("/org/opennms/acl/conf/config.properties"));
        } catch (final IOException e) {
            LogUtils.warnf(this, e, "Unable to get configuration resource.");
        }

        dbDriver = props.getProperty("jdbc.driver");
        dbUrl = props.getProperty("jdbc.url");
        dbUser = props.getProperty("jdbc.username");
        dbPass = props.getProperty("jdbc.password");
    }

    private String dbDriver;
    private String dbUser;
    private String dbPass;
    private String dbUrl;
}
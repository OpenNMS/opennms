package org.opennms.features.backup.api;

import java.util.Map;

public interface BackupStrategy {

    Config getConfig(String ipAddress, int port, Map<String, String> params);

}

package org.opennms.features.backup.api;

import lombok.Data;
import java.util.Date;

@Data
public class Config {
    private byte[] data;
    private ConfigType type;
    private Date retrievedAt;
    private String message;
}

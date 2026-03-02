package org.opennms.core.messagebus;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class IpcMessage implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String type;
    private final String source;
    private final long timestamp;
    private final Long nodeId;
    private final String interfaceAddress;
    private final Map<String, String> parameters;

    public IpcMessage(String type, String source) {
        this(type, source, System.currentTimeMillis(), null, null, Collections.emptyMap());
    }

    public IpcMessage(String type, String source, Map<String, String> parameters) {
        this(type, source, System.currentTimeMillis(), null, null, parameters);
    }

    public IpcMessage(String type, String source, long timestamp,
                      Long nodeId, String interfaceAddress,
                      Map<String, String> parameters) {
        this.type = Objects.requireNonNull(type, "type");
        this.source = Objects.requireNonNull(source, "source");
        this.timestamp = timestamp;
        this.nodeId = nodeId;
        this.interfaceAddress = interfaceAddress;
        this.parameters = parameters != null
                ? Collections.unmodifiableMap(new HashMap<>(parameters))
                : Collections.emptyMap();
    }

    public String getType() { return type; }
    public String getSource() { return source; }
    public long getTimestamp() { return timestamp; }
    public Long getNodeId() { return nodeId; }
    public String getInterfaceAddress() { return interfaceAddress; }
    public Map<String, String> getParameters() { return parameters; }

    public String getParameter(String key) {
        return parameters.get(key);
    }

    @Override
    public String toString() {
        return "IpcMessage{type='" + type + "', source='" + source + "', nodeId=" + nodeId + "}";
    }
}

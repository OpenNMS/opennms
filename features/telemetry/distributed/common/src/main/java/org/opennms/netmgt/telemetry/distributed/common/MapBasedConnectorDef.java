package org.opennms.netmgt.telemetry.distributed.common;

import org.opennms.netmgt.telemetry.config.api.ConnectorDefinition;
import org.opennms.netmgt.telemetry.config.api.PackageDefinition;

import java.util.List;
import java.util.Map;

public class MapBasedConnectorDef  extends  MapBasedQueueDef implements ConnectorDefinition {
    private final String name;
    private final String className;
    private final String queueName;
    private final String serviceName;
    private final Map<String, String> parameters;

    /**
     * Constructor for Blueprint util:properties or CM Dictionary injection.
     */
    public MapBasedConnectorDef(PropertyTree tree) {
        super(tree);
        this.name = tree.getRequiredString("name");
        this.className = tree.getRequiredString("class-name");
        this.queueName = tree.getRequiredString("queue");
        this.serviceName = tree.getRequiredString("service-name");
        this.parameters = tree.getMap("parameters");

    }

    /**
     * Convenience constructor for Blueprint util:map injection.
     */
    public MapBasedConnectorDef(Map<String, String> flatMap) {
        this(PropertyTree.from(flatMap));
    }

    @Override public String getName()           { return name; }
    @Override public String getClassName()      { return className; }
    @Override public String getQueueName()      { return queueName; }
    @Override public String getServiceName()    { return serviceName; }

    @Override
    public List<? extends PackageDefinition> getPackages() {
        return List.of();
    }

    @Override public Map<String, String> getParameterMap() { return parameters; }

}
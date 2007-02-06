package org.opennms.netmgt.correlation;

import java.util.List;

public interface CorrelationEngineRegistrar {

    public abstract void addCorrelationEngine(CorrelationEngine engine);
    
    public abstract List<CorrelationEngine> getEngines();
    
    public abstract CorrelationEngine findEngineByName(String name);

}
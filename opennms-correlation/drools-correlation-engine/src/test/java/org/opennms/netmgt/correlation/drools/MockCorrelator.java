package org.opennms.netmgt.correlation.drools;

import java.util.LinkedList;
import java.util.List;

import org.opennms.netmgt.correlation.CorrelationEngine;
import org.opennms.netmgt.correlation.CorrelationEngineRegistrar;

public class MockCorrelator implements CorrelationEngineRegistrar {
    
    List<CorrelationEngine> m_engines = new LinkedList<CorrelationEngine>();

    public void addCorrelationEngine(CorrelationEngine engine) {
        m_engines.add(engine);
    }
    
    public CorrelationEngine findEngineByName(String name) {
        for (CorrelationEngine engine : m_engines) {
            if (name.equals(engine.getName())) {
                return engine;
            }
        }
        return null;
    }

    public List<CorrelationEngine> getEngines() {
        return m_engines;
    }

}

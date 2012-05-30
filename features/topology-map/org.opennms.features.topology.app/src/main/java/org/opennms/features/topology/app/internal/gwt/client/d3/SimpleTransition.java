package org.opennms.features.topology.app.internal.gwt.client.d3;

public class SimpleTransition extends D3Behavior {

    private int m_duration;
    private int m_delay;
    private String m_property;
    private double m_value;

    public SimpleTransition(String property, int value, int duration, int delay) {
        m_duration = duration;
        m_delay = delay;
        m_property = property;
        m_value = value;
    }
    
    public SimpleTransition(String property, double d, int duration, int delay) {
        
    }

    @Override
    public D3 run(D3 selection) {
        return selection.transition().duration(m_duration).delay(m_delay).attr(m_property, m_value);
    }

}

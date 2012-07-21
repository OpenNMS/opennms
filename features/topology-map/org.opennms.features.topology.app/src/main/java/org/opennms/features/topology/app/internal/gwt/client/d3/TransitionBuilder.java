package org.opennms.features.topology.app.internal.gwt.client.d3;

public class TransitionBuilder {
    
    D3Behavior m_behavior;
    int m_delay = 0;
    
    public TransitionBuilder fadeOut(int duration) {
        SimpleTransition transition = new SimpleTransition("opacity", 0, duration, m_delay);
        
        m_delay += duration;
        return this; 
    }
    
    public static D3Behavior fadeOut(int duration, int delay) {
        return new SimpleTransition("opacity", 0, duration, delay);
    }
    
    public static D3Behavior fadeIn(int duration, int delay) {
        return new SimpleTransition("opacity", 1, duration, delay);
    }
}

package org.opennms.dashboard.client;

import com.google.gwt.user.client.rpc.IsSerializable;

public class SurveillanceSet implements IsSerializable {

    public static final SurveillanceSet DEFAULT = new DefaultSurveillanceSet();

    public boolean isDefault() { return false; }
    
    
    public static class DefaultSurveillanceSet extends SurveillanceSet {
        
        public boolean isDefault() { return true; }
        
        public String toString() {
            return "All Surveillance Nodes";
        }
    }
    
}
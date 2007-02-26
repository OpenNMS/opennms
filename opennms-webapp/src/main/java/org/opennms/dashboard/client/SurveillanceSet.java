package org.opennms.dashboard.client;

import com.google.gwt.user.client.rpc.IsSerializable;

public abstract class SurveillanceSet implements IsSerializable {

    public static final SurveillanceSet DEFAULT = new DefaultSurveillanceSet();

    public boolean isDefault() { return false; }
    
    public abstract void visit(Visitor v);
    
    public static class DefaultSurveillanceSet extends SurveillanceSet {
        
        public boolean isDefault() { return true; }
        
        public String toString() {
            return "All Surveillance Nodes";
        }
        
        public void visit(Visitor v) {
            v.visitAll();
        }
    }
    
}
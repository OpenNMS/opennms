package org.opennms.netmgt.model.ncs;

public interface NCSComponentVisitor {
    
    public void visitComponent(NCSComponent component);
    
    public void completeComponent(NCSComponent component);

}

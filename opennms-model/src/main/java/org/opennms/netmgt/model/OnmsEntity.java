package org.opennms.netmgt.model;

public abstract class OnmsEntity {
	
	public abstract void visit(EntityVisitor visitor);

}

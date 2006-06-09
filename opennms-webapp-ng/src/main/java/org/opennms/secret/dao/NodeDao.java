package org.opennms.secret.dao;

import java.util.Collection;

import org.opennms.secret.model.Node;

public interface NodeDao {
	
	public abstract void initialize(Object obj);
	
	public abstract Node getNode(Long id);
	
	public abstract void createNode(Node node);

    public abstract Collection getInterfaceCollection(Node node);

    public abstract Collection findAll();

}

/**
 * 
 */
package org.opennms.netmgt.dao.jdbc.node;

import javax.sql.DataSource;

import org.opennms.netmgt.dao.jdbc.Factory;
import org.opennms.netmgt.model.OnmsNode;

public class NodeFactory extends Factory {
	
	public static void register(DataSource dataSource) {
		new NodeFactory(dataSource);
	}

	public NodeFactory(DataSource dataSource) {
        this();
		setDataSource(dataSource);
        afterPropertiesSet();
	}

	public NodeFactory() {
        super(OnmsNode.class);
    }

    protected void assignId(Object obj, Object id) {
		((OnmsNode)obj).setId((Integer)id);
	}

	protected Object create() {
		return new LazyNode(getDataSource());
	}

	
}
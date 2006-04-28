/**
 * 
 */
package org.opennms.netmgt.dao.jdbc.agent;

import javax.sql.DataSource;

import org.opennms.netmgt.dao.jdbc.Factory;
import org.opennms.netmgt.model.OnmsNode;

public class AgentFactory extends Factory {
	
	public static void register(DataSource dataSource) {
		new AgentFactory(dataSource);
	}

	public AgentFactory(DataSource dataSource) {
        this();
		setDataSource(dataSource);
        afterPropertiesSet();
	}

	public AgentFactory() {
        super(OnmsNode.class);
    }

    protected void assignId(Object obj, Object id) {
		((OnmsNode)obj).setId((Integer)id);
	}

	protected Object create() {
		return new LazyAgent(getDataSource());
	}

	
}
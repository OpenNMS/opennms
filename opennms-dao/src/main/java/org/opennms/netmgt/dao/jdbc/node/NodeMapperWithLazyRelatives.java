/**
 * 
 */
package org.opennms.netmgt.dao.jdbc.node;

import java.util.Set;

import javax.sql.DataSource;

import org.opennms.netmgt.dao.jdbc.LazySet;
import org.opennms.netmgt.dao.jdbc.category.FindCategoriesByNode;
import org.opennms.netmgt.dao.jdbc.ipif.FindByNode;
import org.opennms.netmgt.model.OnmsNode;

public class NodeMapperWithLazyRelatives extends NodeMapper {
	private DataSource m_dataSource;

	public NodeMapperWithLazyRelatives(DataSource ds) {
		m_dataSource = ds;
	}

	protected void setSnmpInterfaces(OnmsNode node) {
		final Integer id = node.getId();
		LazySet.Loader snmpIfLoader = new LazySet.Loader() {
			public Set load() {
				return new org.opennms.netmgt.dao.jdbc.snmpif.FindByNode(m_dataSource).findSet(id);
			}
		};
		node.setSnmpInterfaces(new LazySet(snmpIfLoader));
	}

	protected void setCategories(OnmsNode node) {
		final Integer id = node.getId();
		LazySet.Loader catLoader = new LazySet.Loader() {
		    public Set load() {
		        return new FindCategoriesByNode(m_dataSource).findSet(id);
		    }
		};
		node.setCategories(new LazySet(catLoader));
	}

	protected void setIpInterfaces(OnmsNode node) {
		final Integer id = node.getId();
		LazySet.Loader ifLoader = new LazySet.Loader() {
		
			public Set load() {
				return new FindByNode(m_dataSource).findSet(id);
			}
			
		};
		
		node.setIpInterfaces(new LazySet(ifLoader));
	}
}
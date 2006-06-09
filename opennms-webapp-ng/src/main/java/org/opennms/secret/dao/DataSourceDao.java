/**
 * 
 */
package org.opennms.secret.dao;

import java.util.List;

import org.opennms.secret.model.DataSource;
import org.opennms.secret.model.InterfaceService;
import org.opennms.secret.model.Node;
import org.opennms.secret.model.NodeInterface;

/**
 * @author Ted Kaczmarek
 *
 */
public interface DataSourceDao {
	public abstract void inititalize(Object obj);
	public abstract List getDataSourcesByInterface(NodeInterface iface);
	public abstract DataSource getDataSourceByService(InterfaceService service);
	public abstract List getDataSourcesByNode(Node node);
    public abstract DataSource getDataSourceById(String id);
}

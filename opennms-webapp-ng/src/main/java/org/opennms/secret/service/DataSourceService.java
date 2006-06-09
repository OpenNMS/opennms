/**
 * 
 */
package org.opennms.secret.service;

import java.util.List;

import org.opennms.secret.model.DataSource;
import org.opennms.secret.model.InterfaceService;
import org.opennms.secret.model.Node;
import org.opennms.secret.model.NodeInterface;

/**
 * @author Ted Kaczmarek
 * @author DJ Gregor
 * 
 */
public interface DataSourceService {

	public abstract List getDataSourcesByInterface(NodeInterface iface);
    public abstract DataSource getDataSourceByService(InterfaceService service);
	public abstract List getDataSourcesByNode(Node node);
    public abstract DataSource getDataSourceById(String id);
	
}

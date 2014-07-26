package org.opennms.web.bridge;

import java.util.Collection;
import java.util.List;

public interface BridgeElementFactoryInterface {

	List<BridgeElementNode> getBridgeElements(int nodeId);
	
	Collection<BridgeLinkNode> getBridgeLinks(int nodeId);
	
}

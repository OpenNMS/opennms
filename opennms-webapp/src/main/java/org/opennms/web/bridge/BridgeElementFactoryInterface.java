package org.opennms.web.bridge;

import java.util.List;

public interface BridgeElementFactoryInterface {

	List<BridgeElementNode> getBridgeElements(int nodeId);
	
	List<BridgeLinkNode> getBridgeLinks(int nodeId);
	
}

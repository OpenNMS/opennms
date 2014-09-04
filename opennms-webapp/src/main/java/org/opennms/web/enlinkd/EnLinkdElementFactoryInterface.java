package org.opennms.web.enlinkd;

import java.util.Collection;
import java.util.List;

public interface EnLinkdElementFactoryInterface {

	List<BridgeElementNode> getBridgeElements(int nodeId);

	Collection<BridgeLinkNode> getBridgeLinks(int nodeId);

	Collection<NodeLinkBridge> getNodeLinks(int nodeId);

	IsisElementNode getIsisElement(int nodeId);
	
	List<IsisLinkNode> getIsisLinks(int nodeId);

	LldpElementNode getLldpElement(int nodeId);
	
	List<LldpLinkNode> getLldpLinks(int nodeId);
	
	OspfElementNode getOspfElement(int nodeId);
	
	List<OspfLinkNode> getOspfLinks(int nodeId);

}

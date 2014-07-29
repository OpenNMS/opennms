package org.opennms.web.enlinkd;

import java.util.List;

public interface OspfElementFactoryInterface {

	OspfElementNode getOspfElement(int nodeId);
	
	List<OspfLinkNode> getOspfLinks(int nodeId);
	
}

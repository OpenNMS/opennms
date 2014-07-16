package org.opennms.web.ospf;

import java.util.List;

public interface OspfElementFactoryInterface {

	OspfElementNode getOspfElement(int nodeId);
	
	List<OspfLinkNode> getOspfLinks(int nodeId);
	
}

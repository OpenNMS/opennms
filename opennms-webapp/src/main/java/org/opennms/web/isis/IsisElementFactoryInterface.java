package org.opennms.web.isis;

import java.util.List;

public interface IsisElementFactoryInterface {

	IsisElementNode getIsisElement(int nodeId);
	
	List<IsisLinkNode> getIsisLinks(int nodeId);
	
}

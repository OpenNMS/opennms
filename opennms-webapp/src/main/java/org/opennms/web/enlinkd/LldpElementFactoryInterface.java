package org.opennms.web.enlinkd;

import java.util.List;

public interface LldpElementFactoryInterface {

	LldpElementNode getLldpElement(int nodeId);
	
	List<LldpLinkNode> getLldpLinks(int nodeId);
	
}

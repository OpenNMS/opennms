package org.opennms.web.lldp;

import java.util.List;

public interface LldpElementFactoryInterface {

	LldpElementNode getLldpElement(int nodeId);
	
	List<LldpLinkNode> getLldpLinks(int nodeId);
	
}

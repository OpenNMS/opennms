/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.web.enlinkd;

import java.util.List;

public interface EnLinkdElementFactoryInterface {

	List<BridgeElementNode> getBridgeElements(int nodeId);
	List<BridgeLinkNode> getBridgeLinks(int nodeId);

	IsisElementNode getIsisElement(int nodeId);
	List<IsisLinkNode> getIsisLinks(int nodeId);

	LldpElementNode getLldpElement(int nodeId);
	List<LldpLinkNode> getLldpLinks(int nodeId);
	
	OspfElementNode getOspfElement(int nodeId);
	List<OspfLinkNode> getOspfLinks(int nodeId);

    CdpElementNode getCdpElement(int nodeId);
	List<CdpLinkNode> getCdpLinks(int nodeId);

	ElementsAndLinks getAll(int nodeId);

	class ElementsAndLinks {

		public final List<BridgeElementNode> bridgeElements;
		public final List<BridgeLinkNode> bridgeLinks;
		public final IsisElementNode isisElement;
		public final List<IsisLinkNode> isisLinks;
		public final LldpElementNode lldpElement;
		public final List<LldpLinkNode> lldpLinks;
		public final OspfElementNode ospfElement;
		public final List<OspfLinkNode> ospfLinks;
		public final CdpElementNode cdpElement;
		public final List<CdpLinkNode> cdpLinks;

		public ElementsAndLinks(List<BridgeElementNode> bridgeElements, List<BridgeLinkNode> bridgeLinks, IsisElementNode isisElement, List<IsisLinkNode> isisLinks, LldpElementNode lldpElement, List<LldpLinkNode> lldpLinks, OspfElementNode ospfElement, List<OspfLinkNode> ospfLinks, CdpElementNode cdpElement, List<CdpLinkNode> cdpLinks) {
			this.bridgeElements = bridgeElements;
			this.bridgeLinks = bridgeLinks;
			this.isisElement = isisElement;
			this.isisLinks = isisLinks;
			this.lldpElement = lldpElement;
			this.lldpLinks = lldpLinks;
			this.ospfElement = ospfElement;
			this.ospfLinks = ospfLinks;
			this.cdpElement = cdpElement;
			this.cdpLinks = cdpLinks;
		}
	}
}

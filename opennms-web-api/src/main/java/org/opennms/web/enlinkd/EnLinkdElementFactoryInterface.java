/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
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

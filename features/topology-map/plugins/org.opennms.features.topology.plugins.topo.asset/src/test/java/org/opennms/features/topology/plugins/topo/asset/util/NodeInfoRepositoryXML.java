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
package org.opennms.features.topology.plugins.topo.asset.util;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * jaxb definition of nodeInfoRepository. This is used for testing 
 * This class also contains static methods for marshalling and unmarshalling XML representations
 * of the nodeInfoRepository to a nodeInfo type. 
 * nodeInfo is a map with values Map<nodeId, Map<nodeParamLabelKey, nodeParamValue>>
 *     nodeParamLabelKey a node asset parameter key (from those defined in org.opennms.plugins.graphml.asset.LayerIdentifier)
 *     nodeParamValue a node asset value ( e.g. key LayerIdentifier.ASSET_RACK ('asset-rack') value: rack1
 *
 */
@XmlRootElement (name="nodeInfoRepository")
@XmlAccessorType(XmlAccessType.NONE)
public class NodeInfoRepositoryXML {

	@XmlElementWrapper(name="nodeInfoList")
	@XmlElement(name="nodeInfo")
	private List<NodeInfoXML> nodeInfoList =  new ArrayList<>();

	public List<NodeInfoXML> getNodeInfoList() {
		return nodeInfoList;
	}

	public void setNodeInfoList(List<NodeInfoXML> nodeInfo) {
		this.nodeInfoList = nodeInfo;
	}
}

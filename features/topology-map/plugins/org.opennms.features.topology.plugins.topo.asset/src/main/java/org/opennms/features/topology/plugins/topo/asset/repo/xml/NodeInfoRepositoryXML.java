/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2002-2014 The OpenNMS Group, Inc.
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

package org.opennms.features.topology.plugins.topo.asset.repo.xml;

import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
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
 *     nodeParamLabelKey a node asset parameter key (from those defined in org.opennms.plugins.graphml.asset.NodeParamLabels)
 *     nodeParamValue a node asset value ( e.g. key NodeParamLabels.ASSET_RACK ('asset-rack') value: rack1
 *
 */
@XmlRootElement (name="nodeInfoRepository")
@XmlAccessorType(XmlAccessType.NONE)
public class NodeInfoRepositoryXML {


	@XmlElementWrapper(name="nodeInfoList")
	@XmlElement(name="nodeInfo")
	private List<NodeInfoXML> nodeInfoList =  new ArrayList<NodeInfoXML>();

	public List<NodeInfoXML> getNodeInfoList() {
		return nodeInfoList;
	}

	public void setNodeInfoList(List<NodeInfoXML> nodeInfo) {
		this.nodeInfoList = nodeInfo;
	}

	/**
	 * Marshalls nodeInfo object into an xml string
	 * @param nodeInfo nodeInfo nodeInfo map with values Map<nodeId, Map<nodeParamLabelKey, nodeParamValue>>
	 *        nodeParamLabelKey a node asset parameter key (from those defined in org.opennms.plugins.graphml.asset.NodeParamLabels)
	 *        nodeParamValue a node asset value ( e.g. key NodeParamLabels.ASSET_RACK ('asset-rack') value: rack1
	 */
	public static String nodeInfoToXML(Map<String, Map<String, String>> nodeInfo){
		try {

			NodeInfoRepositoryXML nodeInfoRepositoryxml= new NodeInfoRepositoryXML();

			List<NodeInfoXML> nodeinfoListxml = nodeInfoRepositoryxml.getNodeInfoList();

			for (String nodeId : nodeInfo.keySet()){
				NodeInfoXML nodeInfoXML= new NodeInfoXML();
				nodeInfoXML.setNodeId(nodeId);
				nodeinfoListxml.add(nodeInfoXML);

				List<NodeParameterXML> nodeParamListxml = nodeInfoXML.getNodeParamList();

				for( String paramkey : nodeInfo.get(nodeId).keySet()){
					NodeParameterXML nodeParamaterxml=new NodeParameterXML();
					nodeParamaterxml.setParamKey(paramkey);
					String paramValue = nodeInfo.get(nodeId).get(paramkey);
					nodeParamaterxml.setParamValue(paramValue );
					nodeParamListxml.add(nodeParamaterxml);
				}
			}

			// marshal to string and return
			StringWriter sw = new StringWriter();
			JAXBContext jaxbContext = JAXBContext.newInstance(NodeInfoRepositoryXML.class);
			Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
			jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
			jaxbMarshaller.marshal(nodeInfoRepositoryxml, sw);
			return sw.toString();
		} catch (JAXBException e) {
			throw new RuntimeException("cannot marshal NodeInfoRepository. ",e);
		}

	}

	/**
	 * Unmarshalls xmlStr string into a nodeInfo object
	 * @param nodeInfo nodeInfo nodeInfo map with values Map<nodeId, Map<nodeParamLabelKey, nodeParamValue>>
	 *        nodeParamLabelKey a node asset parameter key (from those defined in org.opennms.plugins.graphml.asset.NodeParamLabels)
	 *        nodeParamValue a node asset value ( e.g. key NodeParamLabels.ASSET_RACK ('asset-rack') value: rack1
	 * @param xmlStr xml string to marshall into nodeinfo
	 */
	public static void XMLtoNodeInfo(Map<String, Map<String, String>> nodeInfo, String xmlStr){
		try{

			nodeInfo.clear();

			JAXBContext jaxbContext = JAXBContext.newInstance(NodeInfoRepositoryXML.class);
			Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();

			Reader reader = new StringReader(xmlStr);
			NodeInfoRepositoryXML nodeInfoRepositoryXML = (NodeInfoRepositoryXML) jaxbUnmarshaller.unmarshal(reader);

			List<NodeInfoXML> nodeInfoListxml = nodeInfoRepositoryXML.getNodeInfoList();
			if (nodeInfoListxml!=null){
				for(NodeInfoXML nodeInfoXML:nodeInfoListxml){
					String nodeid = nodeInfoXML.getNodeId();
					if(nodeid!=null){
						Map<String, String> paramvalues = new LinkedHashMap<String, String>();
						for (NodeParameterXML nodeParameterxml:nodeInfoXML.getNodeParamList()){
							String key=nodeParameterxml.getParamKey();
							if(key!=null){
								String value=nodeParameterxml.getParamValue();
								paramvalues.put(key, value);
							}
						}
						nodeInfo.put(nodeid, paramvalues);
					}
				}
			}

		} catch (JAXBException e) {
			throw new RuntimeException("cannot unmarshal NodeInfoRepository. ",e);
		}
	}



}

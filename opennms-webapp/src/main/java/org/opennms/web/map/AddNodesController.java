//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2006 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Modifications:
//
// 2007 Jul 24: Organize imports, remove unused code, Java 5 generics. - dj@opennms.org
//
// Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
//
// For more information contact:
//      OpenNMS Licensing       <license@opennms.org>
//      http://www.opennms.org/
//      http://www.opennms.com/
//
package org.opennms.web.map;

/*
 * Created on 8-giu-2005
 *
 */
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.config.CatFactory;
import org.opennms.netmgt.config.CategoryFactory;
import org.opennms.netmgt.filter.FilterDaoFactory;
import org.opennms.web.WebSecurityUtils;
import org.opennms.web.element.NetworkElementFactory;
import org.opennms.web.element.Node;
import org.opennms.web.map.view.Manager;
import org.opennms.web.map.view.VElement;
import org.opennms.web.map.view.VMap;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.Controller;


/**
 * @author mmigliore
 * 
 * this class provides to create, manage and delete 
 * proper session objects to use when working with maps
 * 
 */
public class AddNodesController implements Controller {
	ThreadCategory log;

	private Manager manager;
	
	
	public Manager getManager() {
		return manager;
	}

	public void setManager(Manager manager) {
		this.manager = manager;
	}

	public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response) throws IOException {
		
		ThreadCategory.setPrefix(MapsConstants.LOG4J_CATEGORY);
		log = ThreadCategory.getInstance(this.getClass());
		String action = request.getParameter("action");
		String elems = request.getParameter("elems");
		log.debug("Adding Nodes action:"+action+", elems="+elems );
		
		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(response.getOutputStream(), "UTF-8"));
		try {
			Integer[] nodeids = null;

			boolean actionfound = false;
			
			if (action.equals(MapsConstants.ADDNODES_ACTION)) {
				log.debug("Adding nodes by id: "+ elems);
				actionfound = true;
				String[] snodeids = elems.split(",");
				nodeids = new Integer[snodeids.length];
				for (int i = 0; i<snodeids.length;i++) {
					nodeids[i] = new Integer(snodeids[i]);
				}
			}
			
			if (action.equals(MapsConstants.ADDNODES_BY_CATEGORY_ACTION)) {
				log.debug("Adding nodes by category: "+ elems);
				actionfound = true;
				String categoryName = elems;
				CategoryFactory.init();
				CatFactory cf = CategoryFactory.getInstance();
				String rule = cf.getEffectiveRule(categoryName);
				List<String> nodeIPs = FilterDaoFactory.getInstance().getIPList(rule);
				log.debug("ips found: "+nodeIPs.toString());
				nodeids = new Integer[nodeIPs.size()];
				for (int i = 0; i<nodeIPs.size();i++) {
					String nodeIp= (String)nodeIPs.get(i);
					List<Integer> ids = NetworkElementFactory.getNodeIdsWithIpLike(nodeIp);
					log.debug("Ids by ipaddress "+nodeIp+": "+ids);
					nodeids[i] = ids.get(0);
				}
			}	
			
			
			if (action.equals(MapsConstants.ADDNODES_BY_LABEL_ACTION)) {
				log.debug("Adding nodes by label: "+ elems);
				actionfound = true;
				Node[] nodes = NetworkElementFactory.getNodesLike(elems);
				nodeids = new Integer[nodes.length];
				for (int i = 0; i<nodes.length;i++) {
					nodeids[i] = new Integer(nodes[i].getNodeId());
				}
			}	

			if (action.equals(MapsConstants.ADDRANGE_ACTION)) {
				log.debug("Adding nodes by range: "+ elems);
				actionfound = true;
				nodeids = (Integer[]) NetworkElementFactory.getNodeIdsWithIpLike(elems).toArray(new Integer[0]);
			}

			if (action.equals(MapsConstants.ADDNODES_NEIG_ACTION)) {
				log.debug("Adding nodes neighbor of:"+ elems);
				actionfound = true;
				nodeids = (Integer[]) NetworkElementFactory.getLinkedNodeIdOnNode(WebSecurityUtils.safeParseInt(elems)).toArray(new Integer[0]);
			}

			if (action.equals(MapsConstants.ADDNODES_WITH_NEIG_ACTION)) {
				log.debug("Adding nodes with neighbor of:"+ elems);
				actionfound = true;
				Set<Integer> linkednodeids = NetworkElementFactory.getLinkedNodeIdOnNode(WebSecurityUtils.safeParseInt(elems));
				linkednodeids.add(new Integer(elems));
				nodeids = linkednodeids.toArray(new Integer[linkednodeids.size()]);
			} 
			
	         VMap map = manager.openMap();
	            if(log.isDebugEnabled())
	                log.debug("Got map from manager "+map);
	            

			List<VElement> velems = new ArrayList<VElement>();
			// response for addElement
			if (actionfound) {
				log.debug("Before Checking map contains elems");
				
				for (int i = 0; i < nodeids.length; i++) {
					int elemId = nodeids[i].intValue();
					if (map.containsElement(elemId, MapsConstants.NODE_TYPE)) {
						log.debug("Action: " + action + " . Map Contains Element: " + elemId+MapsConstants.NODE_TYPE);
						continue;
						
					}

					velems.add(manager.newElement(map.getId(), elemId, MapsConstants.NODE_TYPE));
				} // end for

				//get links and add elements to map
				map = manager.addElements(map, velems);
				log.debug("After getting/adding links");
	
				bw.write(ResponseAssembler.getAddElementResponse(null, velems, map.getLinks().values()));
			}
		} catch (Exception e) {
			log.error("Error while adding nodes for action: "+action,e);
			bw.write(ResponseAssembler.getMapErrorResponse(action));
		} finally {
			bw.close();
		}

		return null;
	}

}
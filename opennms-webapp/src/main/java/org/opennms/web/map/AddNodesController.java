/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2007-2014 The OpenNMS Group, Inc.
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

package org.opennms.web.map;

/*
 * Created on 8-giu-2005
 *
 */
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.opennms.core.utils.InetAddressUtils;
import org.opennms.core.utils.WebSecurityUtils;
import org.opennms.netmgt.config.CategoryFactory;
import org.opennms.netmgt.config.api.CatFactory;
import org.opennms.netmgt.filter.FilterDaoFactory;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.web.element.NetworkElementFactory;
import org.opennms.web.map.view.Manager;
import org.opennms.web.map.view.VElement;
import org.opennms.web.map.view.VMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.ModelAndView;


/**
 * <p>AddNodesController class.</p>
 *
 * @author mmigliore
 *
 * this class provides to create, manage and delete
 * proper session objects to use when working with maps
 * @version $Id: $
 * @since 1.8.1
 */
public class AddNodesController extends MapsLoggingController {
	
	private static final Logger LOG = LoggerFactory.getLogger(AddNodesController.class);


	private Manager manager;
	
	
	/**
	 * <p>Getter for the field <code>manager</code>.</p>
	 *
	 * @return a {@link org.opennms.web.map.view.Manager} object.
	 */
	public Manager getManager() {
		return manager;
	}

	/**
	 * <p>Setter for the field <code>manager</code>.</p>
	 *
	 * @param manager a {@link org.opennms.web.map.view.Manager} object.
	 */
	public void setManager(Manager manager) {
		this.manager = manager;
	}

	/** {@inheritDoc} */
        @Override
	protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws IOException {
		
		String action = request.getParameter("action");
		String elems = request.getParameter("elems");
		LOG.debug("Adding Nodes action:{}, elems={}", action, elems);
		
		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(response.getOutputStream(), "UTF-8"));
		try {
			Integer[] nodeids = null;

			boolean actionfound = false;
			
			if (action.equals(MapsConstants.ADDNODES_ACTION)) {
				LOG.debug("Adding nodes by id: {}", elems);
				actionfound = true;
				String[] snodeids = elems.split(",");
				nodeids = new Integer[snodeids.length];
				for (int i = 0; i<snodeids.length;i++) {
					nodeids[i] = Integer.valueOf(snodeids[i]);
				}
			}
			
			if (action.equals(MapsConstants.ADDNODES_BY_CATEGORY_ACTION)) {
				LOG.debug("Adding nodes by category: {}", elems);
				actionfound = true;
				String categoryName = elems;
				CategoryFactory.init();
				CatFactory cf = CategoryFactory.getInstance();
				cf.getReadLock().lock();
				try {
    				final String rule = cf.getEffectiveRule(categoryName);
    				final List<InetAddress> nodeIPs = FilterDaoFactory.getInstance().getIPAddressList(rule);
    				LOG.debug("ips found: {}", nodeIPs.toString());
    				nodeids = new Integer[nodeIPs.size()];
    				for (int i = 0; i<nodeIPs.size();i++) {
    					final InetAddress nodeIp = nodeIPs.get(i);
    					final List<Integer> ids = NetworkElementFactory.getInstance(getServletContext()).getNodeIdsWithIpLike(InetAddressUtils.str(nodeIp));
    					LOG.debug("Ids by ipaddress {}: {}", nodeIp, ids.toString());
    					nodeids[i] = ids.get(0);
               }
            } finally {
                cf.getReadLock().unlock();
            }
			}	
			
			
			if (action.equals(MapsConstants.ADDNODES_BY_LABEL_ACTION)) {
				LOG.debug("Adding nodes by label: {}", elems);
				actionfound = true;
				List<OnmsNode> nodes = NetworkElementFactory.getInstance(getServletContext()).getAllNodes();
				nodeids = new Integer[nodes.size()];
				for (int i = 0; i<nodes.size();i++) {
					nodeids[i] = nodes.get(i).getId();
				}
			}	

			if (action.equals(MapsConstants.ADDRANGE_ACTION)) {
				LOG.debug("Adding nodes by range: {}", elems);
				actionfound = true;
				nodeids = (Integer[]) NetworkElementFactory.getInstance(getServletContext()).getNodeIdsWithIpLike(elems).toArray(new Integer[0]);
			}

			if (action.equals(MapsConstants.ADDNODES_NEIG_ACTION)) {
				LOG.debug("Adding nodes neighbor of:{}", elems);
				actionfound = true;
				nodeids = (Integer[]) NetworkElementFactory.getInstance(getServletContext()).getLinkedNodeIdOnNode(WebSecurityUtils.safeParseInt(elems)).toArray(new Integer[0]);
			}

			if (action.equals(MapsConstants.ADDNODES_WITH_NEIG_ACTION)) {
				LOG.debug("Adding nodes with neighbor of:{}", elems);
				actionfound = true;
				Set<Integer> linkednodeids = NetworkElementFactory.getInstance(getServletContext()).getLinkedNodeIdOnNode(WebSecurityUtils.safeParseInt(elems));
				linkednodeids.add(Integer.valueOf(elems));
				nodeids = linkednodeids.toArray(new Integer[linkednodeids.size()]);
			} 
			
	         VMap map = manager.openMap();
	                LOG.debug("Got map from manager {}", map);
	            

			List<VElement> velems = new ArrayList<VElement>();
			// response for addElement
			if (actionfound) {
				LOG.debug("Before Checking map contains elems");
				
				for (int i = 0; i < nodeids.length; i++) {
					int elemId = nodeids[i].intValue();
					if (map.containsElement(elemId, MapsConstants.NODE_TYPE)) {
						LOG.debug("Action: {} . Map Contains Element: {}", action, elemId+MapsConstants.NODE_TYPE);
						continue;
						
					}

					velems.add(manager.newElement(map.getId(), elemId, MapsConstants.NODE_TYPE));
				} // end for

				//get links and add elements to map
				map = manager.addElements(map, velems);
				LOG.debug("After getting/adding links");
	
				bw.write(ResponseAssembler.getAddElementResponse(null, velems, map.getLinks()));
			}
		} catch (Throwable e) {
			LOG.error("Error while adding nodes for action: {}", action,e);
			bw.write(ResponseAssembler.getMapErrorResponse(action));
		} finally {
			bw.close();
		}

		return null;
	}


}

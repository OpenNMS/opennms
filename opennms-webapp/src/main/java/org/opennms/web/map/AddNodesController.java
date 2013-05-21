/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2007-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
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
import org.opennms.core.utils.LogUtils;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.core.utils.WebSecurityUtils;
import org.opennms.netmgt.config.CategoryFactory;
import org.opennms.netmgt.config.categories.CatFactory;
import org.opennms.netmgt.filter.FilterDaoFactory;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.web.element.NetworkElementFactory;
import org.opennms.web.map.view.Manager;
import org.opennms.web.map.view.VElement;
import org.opennms.web.map.view.VMap;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;


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
public class AddNodesController extends AbstractController {
	ThreadCategory log;

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
				cf.getReadLock().lock();
				try {
    				final String rule = cf.getEffectiveRule(categoryName);
    				final List<InetAddress> nodeIPs = FilterDaoFactory.getInstance().getIPAddressList(rule);
    				LogUtils.debugf(this, "ips found: %s", nodeIPs.toString());
    				nodeids = new Integer[nodeIPs.size()];
    				for (int i = 0; i<nodeIPs.size();i++) {
    					final InetAddress nodeIp = nodeIPs.get(i);
    					final List<Integer> ids = NetworkElementFactory.getInstance(getServletContext()).getNodeIdsWithIpLike(InetAddressUtils.str(nodeIp));
    					LogUtils.debugf(this, "Ids by ipaddress %s: %s", nodeIp, ids.toString());
    					nodeids[i] = ids.get(0);
               }
            } finally {
                cf.getReadLock().unlock();
            }
			}	
			
			
			if (action.equals(MapsConstants.ADDNODES_BY_LABEL_ACTION)) {
				log.debug("Adding nodes by label: "+ elems);
				actionfound = true;
				List<OnmsNode> nodes = NetworkElementFactory.getInstance(getServletContext()).getAllNodes();
				nodeids = new Integer[nodes.size()];
				for (int i = 0; i<nodes.size();i++) {
					nodeids[i] = nodes.get(i).getId();
				}
			}	

			if (action.equals(MapsConstants.ADDRANGE_ACTION)) {
				log.debug("Adding nodes by range: "+ elems);
				actionfound = true;
				nodeids = (Integer[]) NetworkElementFactory.getInstance(getServletContext()).getNodeIdsWithIpLike(elems).toArray(new Integer[0]);
			}

			if (action.equals(MapsConstants.ADDNODES_NEIG_ACTION)) {
				log.debug("Adding nodes neighbor of:"+ elems);
				actionfound = true;
				nodeids = (Integer[]) NetworkElementFactory.getInstance(getServletContext()).getLinkedNodeIdOnNode(WebSecurityUtils.safeParseInt(elems)).toArray(new Integer[0]);
			}

			if (action.equals(MapsConstants.ADDNODES_WITH_NEIG_ACTION)) {
				log.debug("Adding nodes with neighbor of:"+ elems);
				actionfound = true;
				Set<Integer> linkednodeids = NetworkElementFactory.getInstance(getServletContext()).getLinkedNodeIdOnNode(WebSecurityUtils.safeParseInt(elems));
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
	
				bw.write(ResponseAssembler.getAddElementResponse(null, velems, map.getLinks()));
			}
		} catch (Throwable e) {
			log.error("Error while adding nodes for action: "+action,e);
			bw.write(ResponseAssembler.getMapErrorResponse(action));
		} finally {
			bw.close();
		}

		return null;
	}

    @Override
    protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception {
        return handleRequest(request, response);
    }


}

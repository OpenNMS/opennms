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
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.MDC;
import org.opennms.core.logging.Logging;
import org.opennms.web.map.view.Manager;
import org.opennms.web.map.view.VElement;
import org.opennms.web.map.view.VMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.Controller;


/**
 * <p>DeleteElementsController class.</p>
 *
 * @author mmigliore
 *
 * this class provides to create, manage and delete
 * proper session objects to use when working with maps
 * @version $Id: $
 * @since 1.8.1
 */
public class DeleteElementsController implements Controller {
	
	private static final Logger LOG = LoggerFactory.getLogger(DeleteElementsController.class);


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
            Logging.putPrefix(MapsConstants.LOG4J_CATEGORY);
		
		String action = request.getParameter("action");
		String elems = request.getParameter("elems");
		LOG.debug("Adding elements action:{}, elems={}", action, elems );
		
		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(response.getOutputStream(), "UTF-8"));
		try {
			VMap map = manager.openMap();
				LOG.debug("Got map from manager {}", map);
			
			Integer[] elemeids = null;
			String type = MapsConstants.NODE_TYPE;

            String[] mapids = elems.split(",");
            elemeids = new Integer[mapids.length];
            for (int i = 0; i<mapids.length;i++) {
                elemeids[i] = new Integer(mapids[i]);
            }

			boolean actionfound = false;
			if (action.equals(MapsConstants.DELETENODES_ACTION)) {
				actionfound = true;
			}
			
			if (action.equals(MapsConstants.DELETEMAPS_ACTION)) {
				actionfound = true;
				type = MapsConstants.MAP_TYPE;
			}
			
			List<String> velemsids = new ArrayList<String>();
			if (actionfound) {				
				for (int i = 0; i < elemeids.length; i++) {
					int elemId = elemeids[i].intValue();
					if (map.containsElement(elemId, type)){
						map.removeLinksOnElementList(elemId,type);
						VElement ve = map.removeElement(elemId,type);
						velemsids.add(ve.getId()+ve.getType());
					}
				}
			} 
			bw.write(ResponseAssembler.getDeleteElementsResponse(velemsids));
		} catch (Throwable e) {
			LOG.error("Error while adding nodes for action: {}", action,e);
			bw.write(ResponseAssembler.getMapErrorResponse(action));
		} finally {
			bw.close();
		}

		return null;
	}

}

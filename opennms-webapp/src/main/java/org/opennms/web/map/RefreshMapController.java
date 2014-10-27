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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.opennms.web.map.view.Manager;
import org.opennms.web.map.view.VMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.ModelAndView;


/**
 * <p>RefreshMapController class.</p>
 *
 * @author mmigliore
 *
 * this class provides to create, manage and delete
 * proper session objects to use when working with maps
 * @version $Id: $
 * @since 1.8.1
 */
public class RefreshMapController extends MapsLoggingController {
	
	private static final Logger LOG = LoggerFactory.getLogger(RefreshMapController.class);


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
	public ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws IOException {
		
		String action = request.getParameter("action");
		LOG.debug("Received action={}", action);
		
		
		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(response
				.getOutputStream(), "UTF-8"));
		VMap map = null;
		try {
            map = manager.openMap();
			if (action.equals(MapsConstants.REFRESH_ACTION)) {
				map = manager.refreshMap(map);
			} 
			
			if (action.equals(MapsConstants.RELOAD_ACTION)) {
				map = manager.reloadMap(map);				
			}


			if(map==null){
				throw new MapNotFoundException();
			}else{
				bw.write(ResponseAssembler.getRefreshResponse(map));
			}
		} catch (Throwable e) {
			LOG.error("Error while refreshing map. Action {}", action,e);
			bw.write(ResponseAssembler.getMapErrorResponse(action));
		} finally {
			bw.close();
		}

		return null;
	}

}

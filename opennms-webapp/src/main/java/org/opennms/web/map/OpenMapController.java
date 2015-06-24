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
import java.awt.Dimension;
import java.awt.Point;
import java.awt.geom.Point2D;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.collections15.Transformer;
import org.opennms.core.utils.WebSecurityUtils;
import org.opennms.web.map.view.Manager;
import org.opennms.web.map.view.VElement;
import org.opennms.web.map.view.VLink;
import org.opennms.web.map.view.VMap;
import org.opennms.web.map.view.VMapInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.ModelAndView;

import edu.uci.ics.jung.algorithms.layout.KKLayout;
import edu.uci.ics.jung.graph.SparseGraph;


/**
 * <p>OpenMapController class.</p>
 *
 * @author mmigliore
 * @author <a href="mailto:antonio@opennms.it">Antonio Russo</a>
 *
 * this class provides to create, manage and delete
 * proper session objects to use when working with maps
 * @version $Id: $
 * @since 1.8.1
 */
public class OpenMapController extends MapsLoggingController {
	
	private static final Logger LOG = LoggerFactory.getLogger(OpenMapController.class);
        public static final int ELBOW_ROOM = 200;


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
		

		LOG.debug(request.getQueryString());
		String mapIdStr = request.getParameter("MapId");
		LOG.debug("MapId={}", mapIdStr);
		String mapWidthStr = request.getParameter("MapWidth");
        LOG.debug("MapWidth={}", mapWidthStr);
        String mapHeightStr = request.getParameter("MapHeight");
        LOG.debug("MapHeight={}", mapHeightStr);
        String adminModeStr = request.getParameter("adminMode");
        LOG.debug("adminMode={}", adminModeStr);
		
		String user = request.getRemoteUser();
		
		if ((request.isUserInRole(org.opennms.web.api.Authentication.ROLE_ADMIN))) {
			LOG.info("{} has Admin admin Role", user);
		}					

		float widthFactor = 1;
		float heightFactor =1;
		
		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(response
				.getOutputStream(), "UTF-8"));

		try {
			int mapWidth = WebSecurityUtils.safeParseInt(mapWidthStr);
			int mapHeight = WebSecurityUtils.safeParseInt(mapHeightStr);
			
			LOG.debug("Current mapWidth={} and MapHeight={}", mapWidth, mapHeight);
			VMap map = null;
			if(mapIdStr!=null){
				int mapid = WebSecurityUtils.safeParseInt(mapIdStr);
				LOG.debug("Opening map {} for user {}", mapid, user);
				map = manager.openMap(mapid, user, !(adminModeStr.equals("true")));
			}else{
				LOG.debug("Try to Opening default map");
				VMapInfo defaultmapinfo = manager.getDefaultMapsMenu(user);
				if (defaultmapinfo != null ) {
	                map = manager.openMap(defaultmapinfo.getId(),user,!(adminModeStr.equals("true")));
				} else {
				    map = manager.openMap();
				}
			}
			 

			if(map != null){
				int dbMapWidth = map.getWidth();
				int dbMapHeight = map.getHeight();
				widthFactor = (float) mapWidth / dbMapWidth;
				heightFactor = (float) mapHeight / dbMapHeight;

				LOG.debug("Old saved mapWidth={} and MapHeight={}", dbMapWidth, dbMapHeight);
				LOG.debug("widthFactor={}", widthFactor);
				LOG.debug("heightFactor={}", heightFactor);
				LOG.debug("Setting new width and height to the session map");
				
				map.setHeight(mapHeight);
				map.setWidth(mapWidth);

	                        for (VElement ve : map.getElements().values()) {
	                             ve.setX((int) (ve.getX() * widthFactor));
	                             ve.setY((int) (ve.getY() * heightFactor));
	                        }

				SparseGraph<VElement,VLink> jungGraph = new SparseGraph<VElement, VLink>();
				
				for (VElement ve: map.getElements().values()) {
				    jungGraph.addVertex(ve);
				}
				for (VLink vl: map.getLinks()) {
				    jungGraph.addEdge(vl, map.getElement(vl.getFirst()), map.getElement(vl.getSecond()));
				}
				
			        KKLayout<VElement, VLink> layout = new KKLayout<VElement, VLink>(jungGraph);
			        layout.setInitializer(initializer(map));
			        layout.setSize(selectLayoutSize(map));
			        
		                while(!layout.done()) {
		                     layout.step();
		                }
		                
		                int vertexCount = map.getElements().size();
		                for (VElement ve : map.getElements().values()) {
		                    LOG.debug("---------Element {}---------", ve.getLabel());
		                    LOG.debug("dbcoor: X={} Y={}",ve.getX(),ve.getY());
                                    LOG.debug("kkcoor: X={} Y={}",layout.getX(ve),layout.getY(ve));
                                    LOG.debug("kkcoor: X={} Y={}",(int)layout.getX(ve),(int)layout.getY(ve));
                                    LOG.debug("");
                                    if (vertexCount >= 10) {
                                        ve.setX((int)layout.getX(ve)-100);
                                        ve.setY((int)layout.getY(ve)-100);
                                    } else {
                                        ve.setX((int)layout.getX(ve));
                                        ve.setY((int)layout.getY(ve));
                                    }
                                    LOG.debug("vmspcoor: X={} Y={}",ve.getX(),ve.getY());
                               }
			}
			
			bw.write(ResponseAssembler.getMapResponse(map));

		} catch (Throwable e) {
			LOG.error("Error while opening map with id:{}, for user:{}", mapIdStr, user,e);
			bw.write(ResponseAssembler.getMapErrorResponse(MapsConstants.OPENMAP_ACTION));
		} finally {
			bw.close();
		}

		return null;
	}
        
        protected Transformer<VElement, Point2D> initializer(final VMap layout) {
            return new Transformer<VElement, Point2D>() {
                    @Override
                    public Point2D transform(VElement v) {
                            return new Point(v.getX(), v.getY());
                    }
            };
       }
        
        protected Dimension selectLayoutSize(VMap g) {
            /*
             int vertexCount = g.getElements().size();
            
             double height = 1.5*Math.sqrt(vertexCount)*g.getHeight();
             double width = height*16/9;
             
             return new Dimension((int)width,(int)height);
             */
            double height = 1.1*g.getHeight();
            double width = height*16/9;
            
            return new Dimension((int)width,(int)height);
            
      }



}

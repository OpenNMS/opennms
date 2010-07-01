/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2006 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified 
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 * 
 * Modifications:
 * 
 * 2007 Feb 01: Standardize on successView for the view name and cleanup unused code. - dj@opennms.org
 * 
 * Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 *
 * For more information contact:
 * OpenNMS Licensing       <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 */
package org.opennms.web.controller;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.extremecomponents.table.context.Context;
import org.extremecomponents.table.context.HttpServletRequestContext;
import org.extremecomponents.table.limit.Limit;
import org.extremecomponents.table.limit.LimitFactory;
import org.extremecomponents.table.limit.TableLimit;
import org.extremecomponents.table.limit.TableLimitFactory;
import org.opennms.netmgt.model.OnmsOutage;
import org.opennms.web.svclayer.outage.CurrentOutageParseResponse;
import org.opennms.web.svclayer.outage.OutageListBuilder;
import org.opennms.web.svclayer.outage.OutageService;
import org.opennms.web.svclayer.outage.SuppressOutages;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

/**
 * <p>OutageSuppressedController class.</p>
 *
 * @author ranger
 * @version $Id: $
 * @since 1.8.1
 */
public class OutageSuppressedController extends AbstractController {

	OutageService m_outageService;

	OutageListBuilder m_cview = new OutageListBuilder();

	Collection<OnmsOutage> foundOutages;

	Collection<OnmsOutage> viewOutages;
	
	SuppressOutages m_suppress = new SuppressOutages();

	private String m_successView;

	private static final int ROW_LIMIT = 25;

	/**
	 * <p>setOutageService</p>
	 *
	 * @param service a {@link org.opennms.web.svclayer.outage.OutageService} object.
	 */
	public void setOutageService(OutageService service) {
		m_outageService = service;
	}

	// public Map referenceData(HttpServletRequest request) throws Exception {
	/** {@inheritDoc} */
	@Override
	protected ModelAndView handleRequestInternal(HttpServletRequest request,
			HttpServletResponse reply) throws Exception {

		Context context = new HttpServletRequestContext(request);
		LimitFactory limitFactory = new TableLimitFactory(context, "tabledata");
		Limit limit = new TableLimit(limitFactory);

		CurrentOutageParseResponse.findSelectedOutagesIDs(request,m_outageService);
		
		Map<String, Object> myModel = new HashMap<String, Object>();
		Integer totalRows = m_outageService.getSuppressedOutageCount();
	
		myModel.put("request", limit.toString());

		myModel.put("all_params", request.getParameterNames().toString());
		if (limit.getPage() == 1) {
			// no offset set
			myModel.put("rowStart", 0);
			context.setRequestAttribute("rowStart", 0);
			context.setRequestAttribute("rowEnd", ROW_LIMIT);
			myModel.put("rowEnd", ROW_LIMIT);

			if (limit.getSort().getProperty() == null) {
				foundOutages = m_outageService.getSuppressedOutagesByRange(0,
						ROW_LIMIT, "outages.nodeid", "asc");

			} else {
				foundOutages = m_outageService.getSuppressedOutagesByRange(0,
						ROW_LIMIT, "outages.nodeid,outages." + limit.getSort().getProperty(), limit
								.getSort().getSortOrder());

			}
			myModel.put("begin", 0);
			myModel.put("end", ROW_LIMIT);

		} else {
			
			Integer rowstart = null;
			Integer rowend = null;
			
				
				//quirky situation... - as we started on 0 (zero)
				rowstart = ((limit.getPage() * ROW_LIMIT +1 ) - ROW_LIMIT);
				rowend = ( ROW_LIMIT);
				myModel.put("begin", rowstart);
				myModel.put("end", rowend);
			
			if (limit.getSort().getProperty() == null) {
				foundOutages = m_outageService.getSuppressedOutagesByRange(
						rowstart, rowend, "outages.nodeid", "asc");

			} else {

				foundOutages = m_outageService.getSuppressedOutagesByRange(rowstart,
						rowend, "outages.nodeid,outages." + limit.getSort().getProperty() + " ", limit
								.getSort().getSortOrder());

			}
		}
		
		// Pretty smart to build the collection after any suppressions..... 
		Collection theTable = m_cview.theTable(foundOutages);
		 
		myModel.put("tabledata", theTable);
		myModel.put("totalRows", totalRows);
		
		myModel.put("selected_outages", CurrentOutageParseResponse.findSelectedOutagesIDs(request,m_outageService));
		return new ModelAndView(getSuccessView(), myModel);
	}

        /**
         * <p>setSuccessView</p>
         *
         * @param successView a {@link java.lang.String} object.
         */
        public void setSuccessView(String successView) {
                m_successView = successView;
        }
        
        /**
         * <p>getSuccessView</p>
         *
         * @return a {@link java.lang.String} object.
         */
        public String getSuccessView() {
                return m_successView;
        }
}

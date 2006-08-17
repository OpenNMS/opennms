package org.opennms.web.controller;

//
//This file is part of the OpenNMS(R) Application.
//
//OpenNMS(R) is Copyright (C) 2006 The OpenNMS Group, Inc.  All rights reserved.
//OpenNMS(R) is a derivative work, containing both original code, included code and modified
//code that was published under the GNU General Public License. Copyrights for modified 
//and included code are below.
//
//OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
//Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
//
//This program is free software; you can redistribute it and/or modify
//it under the terms of the GNU General Public License as published by
//the Free Software Foundation; either version 2 of the License, or
//(at your option) any later version.
//
//This program is distributed in the hope that it will be useful,
//but WITHOUT ANY WARRANTY; without even the implied warranty of
//MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//GNU General Public License for more details.
//
//You should have received a copy of the GNU General Public License
//along with this program; if not, write to the Free Software
//Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
//
//For more information contact:
//OpenNMS Licensing       <license@opennms.org>
//  http://www.opennms.org/
//  http://www.opennms.com/
//

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.extremecomponents.table.context.Context;
import org.extremecomponents.table.context.HttpServletRequestContext;
import org.extremecomponents.table.core.TableConstants;
import org.extremecomponents.table.core.TableModel;
import org.extremecomponents.table.core.TableModelImpl;
import org.extremecomponents.table.limit.Filter;
import org.extremecomponents.table.limit.Limit;
import org.extremecomponents.table.limit.LimitFactory;
import org.extremecomponents.table.limit.TableLimit;
import org.extremecomponents.table.limit.TableLimitFactory;
import org.extremecomponents.util.ExtremeUtils;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsMonitoredService;
import org.opennms.netmgt.model.OnmsOutage;
import org.opennms.netmgt.model.OnmsServiceType;
import org.opennms.web.svclayer.outage.OutageService;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

//import org.springframework.web.servlet.mvc.SimpleFormController;
//public class OutageController extends SimpleFormController {
public class OutageCurrentController extends AbstractController {

	OutageService m_outageService;

	Collection<OnmsOutage> foundOutages;

	Collection<OnmsOutage> viewOutages;

	private String successView;

	private int defaultRowsDisplayed;

	private Object outageService;

	// private OutageService outageService;
	// BEAN Setter

	public void setOutageService(OutageService service) {
		m_outageService = service;
	}

	// public Map referenceData(HttpServletRequest request) throws Exception {
	@Override
	protected ModelAndView handleRequestInternal(HttpServletRequest request,
			HttpServletResponse reply) throws Exception {

		Context context = new HttpServletRequestContext(request);
		LimitFactory limitFactory = new TableLimitFactory(context,"tabledata",TableConstants.STATE_PERSIST, null);
		Limit limit = new TableLimit(limitFactory);
		
		
		// rowStart=0,rowEnd=0,currentRowsDisplayed=0,page=1,t
		
		
		
		Map<String, Object> myModel = new HashMap<String, Object>();
		String now = (new java.util.Date()).toString();
		myModel.put("now", now);
		
		myModel.put("page", limit.getPage());
		myModel.put("rows", limit.toString());
		
		// This is so fscked, I can get the page
		// but have no idea as how to get the start and
		// end row.
		// so - page 1 will have to be
		
		// total / rows 
	
		Integer totalRows = m_outageService.getCurrentOutageCount();

		limit.setRowAttributes(totalRows, defaultRowsDisplayed); 
		limit.setRowAttributes(totalRows, 75);
		

		
		
		if (limit.getPage() == 1) {
			// no offset set
			myModel.put("rowStart", 1);
			context.setRequestAttribute("rowStart", 1);
			context.setRequestAttribute("rowEnd", 75);
			myModel.put("rowEnd", 75);
			foundOutages = m_outageService.getCurrentOutagesByRange(1, 75, "iflostservice", "asc");

			myModel.put("begin",1);
			myModel.put("end", 75);
			
		} else {
			
			Integer rowstart = ((limit.getPage()*75 - 75) ) ;
			Integer rowend   = ((limit.getPage()*75));
			myModel.put("begin", rowstart);
			myModel.put("end", rowend);
			
			foundOutages = m_outageService.getCurrentOutagesByRange(rowstart,rowend, "ifregainedsvc", "asc");

		}

		HashMap ips = new HashMap<Integer, String>();
		HashMap nodes = new HashMap<Integer, String>();
		HashMap nodeids = new HashMap<Integer, String>();
		HashMap services = new HashMap<Integer, String>();

		List theTable = new ArrayList();
		SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

		for (Iterator iter = foundOutages.iterator(); iter.hasNext();) {
			OnmsOutage outage = (OnmsOutage) iter.next();
			OnmsMonitoredService monitoredService = outage
					.getMonitoredService();
			OnmsServiceType serviceType = monitoredService.getServiceType();
			OnmsIpInterface ipInterface = monitoredService.getIpInterface();
			ips.put(outage.getId(), ipInterface.getIpAddress());
			nodes.put(outage.getId(), ipInterface.getNode().getLabel());
			nodeids.put(outage.getId(), monitoredService.getNodeId());
			services.put(outage.getId(), serviceType.getName());

			Map outagerow = new HashMap();
			outagerow.put("id", (String) outage.getId().toString());
			outagerow.put("node", ipInterface.getNode().getLabel());
			outagerow.put("nodeid", (String) monitoredService.getNodeId()
					.toString());
			outagerow.put("interface", ipInterface.getIpAddress());
			outagerow.put("interfaceid", ipInterface.getId());
			outagerow.put("service", serviceType.getName());
			outagerow.put("serviceid", serviceType.getId());
			// if (outage.getIfLostService() != null) {
			// outagerow.put("down",
			// formatter.format(outage.getIfLostService()));
			// }

			if (outage.getIfLostService() != null) {
				outagerow.put("down", (Date) outage.getIfLostService());
			}

			if (outage.getIfRegainedService() != null) {
				outagerow.put("up", formatter.format(outage
						.getIfRegainedService()));
			}

			if (outage.getSuppressTime() != null) {
				outagerow.put("suppresstime", outage.getSuppressTime());
			}

			outagerow.put("suppressedby", outage.getSuppressedBy());
			theTable.add(outagerow);
		}


		request.setAttribute("tabledata", theTable);
		request.setAttribute("tabledata_totalRows", new Integer(""+totalRows));
		
		
		myModel.put("tabledata", theTable);
		myModel.put("outages", foundOutages);
		myModel.put("ips", ips);
		myModel.put("nodes", nodes);
		myModel.put("nodeids", nodeids);
		myModel.put("services", services);
		myModel.put("totalRows", totalRows);
		
		
		return new ModelAndView("displayCurrentOutages", myModel);
	}

	public void setSuccessView(String successView) {
		this.successView = successView;
	}

	public void setdisplayCurrentOutages(String successView) {
		this.successView = successView;
	}

	public void setDefaultRowsDisplayed(int defaultRowsDisplayed) {
		this.defaultRowsDisplayed = defaultRowsDisplayed;
	}

}

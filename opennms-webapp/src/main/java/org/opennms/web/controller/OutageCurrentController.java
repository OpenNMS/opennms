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
// OpenNMS Licensing       <license@opennms.org>
//     http://www.opennms.org/
//     http://www.opennms.com/
//

package org.opennms.web.controller;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.opennms.netmgt.model.OnmsOutage;
import org.opennms.web.svclayer.OutageService;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

/**
 * Outages controller.
 * 
 * @author joed
 *
 */
public class OutageCurrentController extends AbstractController {

	OutageService m_outageService;

	Collection<OnmsOutage> foundOutages;

	// private OutageService outageService;
	// BEAN Setter

	public void setOutageService(OutageService service) {
		m_outageService = service;
	}

	// public Map referenceData(HttpServletRequest request) throws Exception {
	@Override
	protected ModelAndView handleRequestInternal(HttpServletRequest arg0,
			HttpServletResponse arg1) throws Exception {

		Integer offset = 1;
		Integer limit = 50;

		foundOutages = m_outageService.getCurrenOutagesByRange(offset, limit);

		Map<String, Object> myModel = new HashMap<String, Object>();
		String now = (new java.util.Date()).toString();
		myModel.put("now", now);
		
		
		
		myModel.put("outages",foundOutages);
		return new ModelAndView("displayCurrentOutages", myModel);
	}

}

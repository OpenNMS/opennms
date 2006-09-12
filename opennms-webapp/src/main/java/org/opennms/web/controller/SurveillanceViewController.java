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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.opennms.web.svclayer.ProgressMonitor;
import org.opennms.web.svclayer.SimpleWebTable;
import org.opennms.web.svclayer.SurveillanceService;
import org.opennms.web.svclayer.SurveillanceTable;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

public class SurveillanceViewController extends AbstractController {
    
    private static SurveillanceService m_service;
    
    public void setService(SurveillanceService svc) {
        m_service = svc;
    }
    
    @Override
    protected ModelAndView handleRequestInternal(HttpServletRequest req, HttpServletResponse resp) throws Exception {
    	
    	final String progressMonitorKey = "surveillanceViewProgressMonitor";

    	HttpSession session = req.getSession();
		ProgressMonitor progressMonitor = (ProgressMonitor) session.getAttribute(progressMonitorKey);
		if (progressMonitor == null) {
			progressMonitor = createProgressMonitor(req.getParameter("viewName"));
			session.setAttribute(progressMonitorKey, progressMonitor);
		}
		
		if (progressMonitor.isError()) {
			session.removeAttribute(progressMonitorKey);
			Throwable t = progressMonitor.getThrowable();
            throw new Exception("SurveillanceView Builder Thread threw exception: [" + t.getClass().getName() + "] " + t.getMessage(), t);
		}
    	
    	if (progressMonitor.isFinished()) {
			session.removeAttribute(progressMonitorKey);
    		SimpleWebTable table = (SimpleWebTable)progressMonitor.getResult();
    		return new ModelAndView("surveillanceView", "webTable", table);
    	}
    	
    	return new ModelAndView("progressBar", "progress", progressMonitor);
    		
    }

	private ProgressMonitor createProgressMonitor(final String viewName) {
		ProgressMonitor progressMonitor;
		final ProgressMonitor monitor = new ProgressMonitor();
		
		
		Thread bgRunner = new Thread("SurveillanceView Builder") {
			
			public void run() {
				try {
					m_service.createSurveillanceTable(viewName, monitor);
				} catch (Throwable t) {
					monitor.errorOccurred(t);
				}
			}
			
		};
		bgRunner.start();
		progressMonitor = monitor;
		return progressMonitor;
	}

}

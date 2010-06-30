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
// 2007 Jul 24: Java 5 generics and comment-out unused code. - dj@opennms.org
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

import org.opennms.netmgt.dao.ServiceInfo;
import org.opennms.web.svclayer.daemonstatus.DaemonStatusService;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.SimpleFormController;

/**
 * <p>DaemonStatusController class.</p>
 *
 * @author SriKumar Kareti
 * @author Tiffani Heeren
 * @version $Id: $
 * @since 1.6.12
 */
public class DaemonStatusController extends SimpleFormController {
	 private DaemonStatusService daemonStatusService;

     /**
      * <p>Setter for the field <code>daemonStatusService</code>.</p>
      *
      * @param daemonStatusService a {@link org.opennms.web.svclayer.daemonstatus.DaemonStatusService} object.
      */
     public void setDaemonStatusService(DaemonStatusService daemonStatusService) {
    	 this.daemonStatusService = daemonStatusService;
     }

	/** {@inheritDoc} */
	@Override
	protected Map<String, Collection<ServiceInfo>> referenceData(HttpServletRequest arg0) throws Exception {
		// TODO Auto-generated method stub
		Map<String, Collection<ServiceInfo>> referenceData = new HashMap<String, Collection<ServiceInfo>>();
		Collection<ServiceInfo> daemons = daemonStatusService.getCurrentDaemonStatusColl();
		logger.debug("number of daemons:" + daemons.size());
		referenceData.put("daemons", daemons);
		return referenceData;
	}

	/** {@inheritDoc} */
	@Override
	protected ModelAndView onSubmit(HttpServletRequest request, HttpServletResponse arg1, Object arg2, BindException arg3) throws Exception {
		// TODO Auto-generated method stub
        // FIXME: This isn't used
//		Map<String, ServiceInfo> daemons = 
//			daemonStatusService.performOperationOnDaemons(ServletRequestUtils.getStringParameter(request, "operation"),
//				ServletRequestUtils.getStringParameters(request, "values"));
		ModelAndView modelAndView = super.onSubmit(request, arg1, arg2, arg3);
		modelAndView.addAllObjects(referenceData(request));
		return modelAndView;
	}
      
	/** {@inheritDoc} */
	@Override
	protected Object formBackingObject(HttpServletRequest arg0) throws Exception {
		// TODO Auto-generated method stub
		return super.formBackingObject(arg0);
	}
}

/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2011 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2011 The OpenNMS Group, Inc.
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
 * @since 1.8.1
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

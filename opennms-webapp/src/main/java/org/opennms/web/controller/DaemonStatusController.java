/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2012 The OpenNMS Group, Inc.
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

package org.opennms.web.controller;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.opennms.core.utils.BeanUtils;
import org.opennms.netmgt.model.ServiceInfo;
import org.opennms.web.svclayer.daemonstatus.DaemonStatusService;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

/**
 * <p>DaemonStatusController class.</p>
 *
 * @author SriKumar Kareti
 * @author Tiffani Heeren
 * @since 1.8.1
 */
//@Controller
//@RequestMapping("/daemonstatus.htm")
public class DaemonStatusController implements InitializingBean {

    @Autowired
	private DaemonStatusService daemonStatusService;

    @Override
    public void afterPropertiesSet() throws Exception {
        BeanUtils.assertAutowiring(this);
    }

	protected Map<String, Collection<ServiceInfo>> referenceData() throws Exception {
		Map<String, Collection<ServiceInfo>> referenceData = new HashMap<String, Collection<ServiceInfo>>();
		Collection<ServiceInfo> daemons = daemonStatusService.getCurrentDaemonStatusColl();
		referenceData.put("daemons", daemons);
		return referenceData;
	}

	@RequestMapping(method = RequestMethod.POST)
	public ModelAndView onSubmit() throws Exception {
        // FIXME: This isn't used
	    /*
		Map<String, ServiceInfo> daemons = 
			daemonStatusService.performOperationOnDaemons(ServletRequestUtils.getStringParameter(request, "operation"),
				ServletRequestUtils.getStringParameters(request, "values"));
		*/
		ModelAndView modelAndView = new ModelAndView();
		modelAndView.addAllObjects(referenceData());
		return modelAndView;
	}
}

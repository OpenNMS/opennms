/*
 * This file is part of the OpenNMS(R) Application.
 *
 * Copyright (C) 2011 The OpenNMS Group, Inc.  All rights reserved.
 * 
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
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
 *      OpenNMS Licensing       <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 */
package org.opennms.web.controller.element;

import org.opennms.core.utils.BeanUtils;
import org.opennms.netmgt.dao.MonitoredServiceDao;
import org.opennms.netmgt.model.OnmsMonitoredService;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

/**
 * 
 * @author <a href="mailto:galea.melania@gmail.com">Melania Galea</a>
 */
@Controller
@RequestMapping("/element/service.htm")
public class ServiceController implements InitializingBean {
	@Autowired
	private MonitoredServiceDao m_monitoredServiceDao;

	@Override
	public void afterPropertiesSet() throws Exception {
	    BeanUtils.assertAutowiring(this);
	}

	@RequestMapping(method = RequestMethod.GET, params = { "ifserviceid" })
	public ModelAndView handleService(@RequestParam("ifserviceid") int ifServiceId) {
		OnmsMonitoredService service = m_monitoredServiceDao.get(ifServiceId);
		if (service == null) {
			return createErrorModelAndView(ifServiceId);
		} else {
			return createSuccessModelAndView(service);
		}
	}

	@RequestMapping(method = RequestMethod.GET, params = { "node", "intf", "service" })
	public ModelAndView handleService(@RequestParam("node") int node, @RequestParam("intf") String intf, @RequestParam("service") int serviceId) {
		OnmsMonitoredService service = m_monitoredServiceDao.get(node, intf, serviceId);
		if (service == null) {
			return createErrorModelAndView(node, intf, serviceId);
		} else {
			return createSuccessModelAndView(service);
		}
	}

	private ModelAndView createErrorModelAndView(int ifServiceId) {
		ModelAndView modelAndView = new ModelAndView("element/errorPageOneKeyService");
		modelAndView.addObject("ifServiceId", ifServiceId);

		return modelAndView;
	}

	private ModelAndView createErrorModelAndView(int node, String intf, int serviceId) {
		ModelAndView modelAndView = new ModelAndView("element/errorPageService");
		modelAndView.addObject("node", node);
		modelAndView.addObject("intf", intf);
		modelAndView.addObject("serviceId", serviceId);

		return modelAndView;
	}

	private ModelAndView createSuccessModelAndView(OnmsMonitoredService service) {
		ModelAndView modelAndView = new ModelAndView("element/service");
		modelAndView.addObject("service", service);

		return modelAndView;
	}
}

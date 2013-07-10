/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2011-2012 The OpenNMS Group, Inc.
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

package org.opennms.web.controller.element;

import org.opennms.core.utils.BeanUtils;
import org.opennms.netmgt.dao.api.MonitoredServiceDao;
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

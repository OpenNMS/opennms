/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.opennms.web.controller.element;

import org.opennms.core.spring.BeanUtils;
import org.opennms.core.utils.InetAddressUtils;
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
		OnmsMonitoredService service = m_monitoredServiceDao.get(node, InetAddressUtils.addr(intf), serviceId);
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

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
package org.opennms.web.controller.outage;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.opennms.web.element.ElementUtil;
import org.opennms.web.element.Service;
import org.opennms.web.filter.Filter;
import org.opennms.web.outage.Outage;
import org.opennms.web.outage.SortStyle;
import org.opennms.web.outage.WebOutageRepository;
import org.opennms.web.outage.filter.InterfaceFilter;
import org.opennms.web.outage.filter.NodeFilter;
import org.opennms.web.outage.filter.OutageCriteria;
import org.opennms.web.outage.filter.PerspectiveLocationFilter;
import org.opennms.web.outage.filter.RecentOutagesFilter;
import org.opennms.web.outage.filter.ServiceFilter;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

/**
 * <p>ServiceOutagesController class.</p>
 *
 * @author ranger
 * @version $Id: $
 * @since 1.8.1
 */
public class ServiceOutagesController extends AbstractController implements InitializingBean {

    private String m_successView;
    private WebOutageRepository m_webOutageRepository;

    /** {@inheritDoc} */
    @Override
    protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception {
        Service service = ElementUtil.getServiceByParams(request, getServletContext());

        Outage[] outages = new Outage[0];

        if (service.getNodeId() > 0 && service.getIpAddress() != null && service.getServiceId() > 0) {
            List<Filter> filters = new ArrayList<>();

            filters.add(new InterfaceFilter(service.getIpAddress()));
            filters.add(new NodeFilter(service.getNodeId(), getServletContext()));
            filters.add(new ServiceFilter(service.getServiceId(), getServletContext()));
            filters.add(new RecentOutagesFilter());
            filters.add(new PerspectiveLocationFilter(null));

            OutageCriteria criteria = new OutageCriteria(filters.toArray(new Filter[0]), SortStyle.ID, null, -1, -1);
            outages = m_webOutageRepository.getMatchingOutages(criteria);
        }

        ModelAndView modelAndView = new ModelAndView(getSuccessView());
        modelAndView.addObject("serviceId", service.getServiceId());
        modelAndView.addObject("outages", outages);
        return modelAndView;
    }

    /**
     * <p>afterPropertiesSet</p>
     *
     * @throws java.lang.Exception if any.
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        Assert.notNull(m_successView, "property successView must be set");
        Assert.notNull(m_webOutageRepository, "webOutageRepository must be set");
    }

    private String getSuccessView() {
        return m_successView;
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
     * <p>setWebOutageRepository</p>
     *
     * @param webOutageRepository a {@link org.opennms.web.outage.WebOutageRepository} object.
     */
    public void setWebOutageRepository(WebOutageRepository webOutageRepository) {
        m_webOutageRepository = webOutageRepository;
    }

}

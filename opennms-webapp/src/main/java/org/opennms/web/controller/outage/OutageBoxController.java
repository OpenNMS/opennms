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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.opennms.core.sysprops.SystemProperties;
import org.opennms.netmgt.model.outage.OutageSummary;
import org.opennms.web.outage.WebOutageRepository;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

/**
 * A controller that handles querying the outages table to create the front-page
 * outage summary box.
 *
 * @author <a href="mailto:ranger@opennms.org">Benjamin Reed</a>
 * @author <a href="http://www.opennms.org/">OpenNMS</a>
 * @author <a href="mailto:ranger@opennms.org">Benjamin Reed</a>
 * @author <a href="http://www.opennms.org/">OpenNMS</a>
 * @version $Id: $
 * @since 1.8.1
 */
public class OutageBoxController extends AbstractController implements InitializingBean {
    public static final int ROWS = 12;

    private WebOutageRepository m_webOutageRepository;
    private String m_successView;
    
    /** {@inheritDoc} */
    @Override
    protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception {
        int rows = SystemProperties.getInteger("opennms.nodesWithOutages.count", ROWS);
        final String parm = request.getParameter("outageCount");
        if (parm != null) {
            try {
                rows = Integer.valueOf(parm);
            } catch (NumberFormatException e) {
                // ignore, and let it fall back to the defaults
            }
        }
        OutageSummary[] summaries = m_webOutageRepository.getCurrentOutages(rows);
        int outagesRemaining = (m_webOutageRepository.countCurrentOutages() - summaries.length);

        ModelAndView modelAndView = new ModelAndView(getSuccessView());
        modelAndView.addObject("summaries", summaries);
        modelAndView.addObject("moreCount", outagesRemaining);
        return modelAndView;

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

    /**
     * <p>afterPropertiesSet</p>
     */
    @Override
    public void afterPropertiesSet() {
        Assert.notNull(m_successView, "property successView must be set");
        Assert.notNull(m_webOutageRepository, "webOutageRepository must be set");
    }

}

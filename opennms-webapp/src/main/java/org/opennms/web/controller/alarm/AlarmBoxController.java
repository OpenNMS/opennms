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
package org.opennms.web.controller.alarm;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.opennms.core.sysprops.SystemProperties;
import org.opennms.netmgt.dao.api.AlarmRepository;
import org.opennms.netmgt.model.alarm.AlarmSummary;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

/**
 * A controller that handles querying the alarm table to create the front-page
 * alarm summary box.
 *
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a>
 */
public class AlarmBoxController extends AbstractController implements InitializingBean {
    public static final int ROWS = 16;

    private AlarmRepository m_webAlarmRepository;
    private String m_successView;

    /** {@inheritDoc} */
    @Override
    protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception {
        int rows = SystemProperties.getInteger("opennms.nodesWithProblems.count", ROWS);
        final String parm = request.getParameter("alarmCount");
        if (parm != null) {
            try {
                rows = Integer.valueOf(parm);
            } catch (NumberFormatException e) {
                // ignore, and let it fall back to the defaults
            }
        }
        List<AlarmSummary> summaries = m_webAlarmRepository.getCurrentNodeAlarmSummaries();
        int moreCount = summaries.size() - rows;

        ModelAndView modelAndView = new ModelAndView(getSuccessView());
        
        if (rows == 0 || summaries.size() < rows) {
            modelAndView.addObject("summaries", summaries);
        } else {
            modelAndView.addObject("summaries", summaries.subList(0, rows));
        }
        modelAndView.addObject("moreCount", moreCount);
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
     * <p>setWebAlarmRepository</p>
     *
     * @param webAlarmRepository a {@link org.opennms.netmgt.dao.api.AlarmRepository} object.
     */
    public void setAlarmRepository(AlarmRepository webAlarmRepository) {
        m_webAlarmRepository = webAlarmRepository;
    }

    /**
     * <p>afterPropertiesSet</p>
     */
    @Override
    public void afterPropertiesSet() {
        Assert.notNull(m_successView, "property successView must be set");
        Assert.notNull(m_webAlarmRepository, "webAlarmRepository must be set");
    }

}

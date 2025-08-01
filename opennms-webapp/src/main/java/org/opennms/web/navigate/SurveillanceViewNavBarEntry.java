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
package org.opennms.web.navigate;

import org.opennms.netmgt.dao.api.SurveillanceViewConfigDao;
import org.opennms.web.api.Util;

/**
 * <p>SurveillanceViewNavBarEntry class.</p>
 *
 * @author ranger
 * @version $Id: $
 * @since 1.8.1
 */
public class SurveillanceViewNavBarEntry extends LocationBasedNavBarEntry {
    private SurveillanceViewConfigDao m_surveillanceViewConfigDao;

    /** {@inheritDoc} */
    @Override
    public DisplayStatus evaluate(MenuContext context) {
        if (m_surveillanceViewConfigDao.getViews().size() > 0 && m_surveillanceViewConfigDao.getDefaultView() != null) {
            setUrl("surveillanceView.htm?viewName=" + Util.htmlify(m_surveillanceViewConfigDao.getDefaultView().getName()));

            return super.evaluate(context);
        } else {
            return DisplayStatus.NO_DISPLAY;
        }
    }

    /**
     * <p>getSurveillanceViewConfigDao</p>
     *
     * @return a {@link org.opennms.netmgt.dao.api.SurveillanceViewConfigDao} object.
     */
    public SurveillanceViewConfigDao getSurveillanceViewConfigDao() {
        return m_surveillanceViewConfigDao;
    }

    /**
     * <p>setSurveillanceViewConfigDao</p>
     *
     * @param surveillanceViewConfigDao a {@link org.opennms.netmgt.dao.api.SurveillanceViewConfigDao} object.
     */
    public void setSurveillanceViewConfigDao(SurveillanceViewConfigDao surveillanceViewConfigDao) {
        m_surveillanceViewConfigDao = surveillanceViewConfigDao;
    }
}

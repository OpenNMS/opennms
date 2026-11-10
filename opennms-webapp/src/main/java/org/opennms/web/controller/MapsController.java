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
package org.opennms.web.controller;

import java.util.LinkedHashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.opennms.web.navigate.DisplayStatus;
import org.opennms.web.navigate.MenuDropdownNavBarEntry;
import org.opennms.web.navigate.NavBarEntry;
import org.opennms.web.navigate.NavBarModel;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

public class MapsController extends AbstractController implements InitializingBean {
    private MenuDropdownNavBarEntry m_mapMenuEntries;

    @Override
    public void afterPropertiesSet() {
        Assert.state(m_mapMenuEntries != null, "mapMenuEntries property has not been set");
    }

    public MenuDropdownNavBarEntry getMapMenuEntries() {
        return m_mapMenuEntries;
    }

    public void setMapMenuEntries(final MenuDropdownNavBarEntry entries) {
        m_mapMenuEntries = entries;
    }

    private NavBarModel createNavBarModel(final HttpServletRequest request) {
        final Map<NavBarEntry, DisplayStatus> navBar = new LinkedHashMap<NavBarEntry, DisplayStatus>();

        for (final NavBarEntry entry : m_mapMenuEntries.getEntries()) {
            navBar.put(entry, entry.evaluate(request));
        }

        return new NavBarModel(request, navBar);
    }


    @Override
    protected ModelAndView handleRequestInternal(final HttpServletRequest request, final HttpServletResponse response) throws Exception {
        return new ModelAndView("maps", "entries", createNavBarModel(request));
    }

}

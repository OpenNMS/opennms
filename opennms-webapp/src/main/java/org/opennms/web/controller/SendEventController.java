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

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.opennms.core.utils.BundleLists;
import org.opennms.core.utils.ConfigFileConstants;
import org.opennms.netmgt.config.api.EventConfDao;
import org.opennms.netmgt.dao.api.EventConfEventDao;
import org.opennms.netmgt.dao.api.EventConfSourceDao;
import org.opennms.netmgt.xml.eventconf.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

import com.google.common.collect.Maps;

/**
 * A controller for the 'Send Event' page based on the previous 'admin/sendevent.jsp'.
 *
 * @author jwhite
 */
public class SendEventController extends AbstractController {
    private static final Logger LOG = LoggerFactory.getLogger(SendEventController.class);
    @Autowired
    private EventConfDao m_eventConfDao;
    private EventConfEventDao m_eventDao;
    private EventConfSourceDao m_sourceDao;

    @Override
    protected ModelAndView handleRequestInternal(HttpServletRequest request,
            HttpServletResponse response) throws Exception {
        return new ModelAndView("/admin/sendevent", "model", createModel());
    }

    private Map<String, Object> createModel() throws FileNotFoundException, IOException {
        // Make sure we have an up-to-date list of events
        m_eventConfDao.reload();

        Map<String, Object> model = Maps.newHashMap();
        model.put("vendorList", m_sourceDao.findAllVendors());
        model.put("eventSelect", buildEventSelect());
        return model;
    }

    private String buildEventSelect() throws IOException, FileNotFoundException {
        List<Event> events = m_eventConfDao.getEventsByLabel();
        final StringBuilder buffer = new StringBuilder();

        List<String> excludeList = getExcludeList();
        TreeMap<String, String> sortedMap = new TreeMap<String, String>();

        for (Event e : events) {

            String uei = e.getUei();
            // System.out.println(uei);

            String label = e.getEventLabel();
            // System.out.println(label);

            String trimmedUei = stripUei(uei);
            // System.out.println(trimmedUei);

            if (!excludeList.contains(trimmedUei)) {
                if (label != null && uei != null ) {
                    sortedMap.put(label, uei);
                } else {
                    LOG.warn("Event configuration with uei {} failed to validate, missing event label", e.getUei());
                }
            }
        }
        for (Map.Entry<String, String> me : sortedMap.entrySet()) {
            buffer.append("<option value=" + me.getValue() + ">" + me.getKey()
                    + "</option>");
        }

        return buffer.toString();
    }

    private String stripUei(String uei) {
        String leftover = uei;

        for (int i = 0; i < 3; i++) {
            leftover = leftover.substring(leftover.indexOf('/') + 1);
        }

        return leftover;
    }

    private List<String> getExcludeList() throws IOException,
            FileNotFoundException {
        List<String> excludes = new ArrayList<>();

        Properties excludeProperties = new Properties();
        excludeProperties.load(new FileInputStream(ConfigFileConstants
                .getFile(ConfigFileConstants.EXCLUDE_UEI_FILE_NAME)));
        String[] ueis = BundleLists.parseBundleList(excludeProperties
                .getProperty("excludes"));

        for (int i = 0; i < ueis.length; i++) {
            excludes.add(ueis[i]);
        }

        return excludes;
    }

    public void setEventConfDao(EventConfDao eventConfDao) {
        m_eventConfDao = eventConfDao;
    }
}

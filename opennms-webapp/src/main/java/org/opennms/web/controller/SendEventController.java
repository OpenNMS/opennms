/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

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
import org.opennms.netmgt.xml.eventconf.Event;
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
    @Autowired
    private EventConfDao m_eventConfDao;

    @Override
    protected ModelAndView handleRequestInternal(HttpServletRequest request,
            HttpServletResponse response) throws Exception {
        return new ModelAndView("/admin/sendevent", "model", createModel());
    }

    private Map<String, Object> createModel() throws FileNotFoundException, IOException {
        // Make sure we have an up-to-date list of events
        m_eventConfDao.reload();

        Map<String, Object> model = Maps.newHashMap();
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
                sortedMap.put(label, uei);
                // System.out.println("sortedMap.put('"+label+"', '"+uei+"')");
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

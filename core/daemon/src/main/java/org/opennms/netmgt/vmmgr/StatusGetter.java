/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2005-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.vmmgr;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StatusGetter {

    private static final Logger LOG = LoggerFactory.getLogger(StatusGetter.class);

    public static enum Status {
        UNKNOWN, RUNNING, PARTIALLY_RUNNING, NOT_RUNNING, CONNECTION_REFUSED
    }

    private Status m_status = Status.UNKNOWN;

    private final Controller m_controller;

    public StatusGetter(Controller controller) {
        m_controller = controller;
    }

    /**
     * <p>getStatus</p>
     *
     * @return a {@link org.opennms.netmgt.vmmgr.StatusGetter.Status} object.
     */
    public Status getStatus() {
        return m_status;
    }

    /**
     * <p>queryStatus</p>
     *
     * @throws java.lang.Exception if any.
     */
    public void queryStatus() throws Exception {

        Pattern p = Pattern.compile("Status: OpenNMS:Name=(\\S+) = (\\S+)");

        LinkedHashMap<String, String> results = new LinkedHashMap<String, String>();

        List<String> statusResults = Collections.emptyList();
        try {
            statusResults = (List<String>)m_controller.doInvokeOperation("status");
        } catch (Throwable e) {
            LOG.debug("Could not fetch status: " + e.getMessage());
            if (m_controller.isVerbose()) {
                // TODO Should this be System.err instead?
                System.out.println("Could not connect to the OpenNMS JVM"
                        + " (OpenNMS might not be running or "
                        + "could be starting up or shutting down): "
                        + e.getMessage());
            }
            m_status = Status.CONNECTION_REFUSED;
            return;
        }

        /*
         * Once we split a status entry, it will look like this:
         * Status: OpenNMS:Name=Eventd = RUNNING
         */
        for (String result : statusResults) {

            Matcher m = p.matcher(result);
            if (!m.matches()) {
                throw new Exception("Result \"" + result
                        + "\" does not match our regular expression");
            }
            results.put(m.group(1), m.group(2));
        }

        /*
         * We want our output to look like this:
         *     OpenNMS.Eventd         : running
         */
        String spaces = "               ";
        int running = 0;
        int services = 0;
        for (Entry<String, String> entry : results.entrySet()) {
            String daemon = entry.getKey();
            String status = entry.getValue().toLowerCase();

            services++;
            if (status.equals("running")) {
                running++;
            }
            if (m_controller.isVerbose()) {
                System.out.println("OpenNMS." + daemon
                        + spaces.substring(daemon.length()) + ": " + status);
            }
        }

        if (services == 0) {
            m_status = Status.NOT_RUNNING;
        } else if (running != services) {
            m_status = Status.PARTIALLY_RUNNING;
        } else {
            m_status = Status.RUNNING;
        }
    }
}

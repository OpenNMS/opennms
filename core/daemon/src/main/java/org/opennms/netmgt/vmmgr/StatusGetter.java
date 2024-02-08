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
package org.opennms.netmgt.vmmgr;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StatusGetter {
    private static final Logger LOG = LoggerFactory.getLogger(StatusGetter.class);
    public static final Pattern SERVICE_STATUS_PATTERN = Pattern.compile("Status: OpenNMS:Name=(\\S+) = (\\S+)");

    public static enum Status {
        UNKNOWN, RUNNING, PARTIALLY_RUNNING, NOT_RUNNING, CONNECTION_REFUSED
    }

    private Status m_status = Status.UNKNOWN;

    private final Controller m_controller;

    public StatusGetter(Controller controller) {
        m_controller = controller;
    }

    public Status getStatus() {
        return m_status;
    }

    public Map<String,String> retrieveStatus() throws IllegalStateException {
        final LinkedHashMap<String, String> results = new LinkedHashMap<String, String>();

        try {
            @SuppressWarnings("unchecked")
            final List<String> statusResults = (List<String>)m_controller.doInvokeOperation("status");

            /*
             * Once we split a status entry, it will look like this:
             * Status: OpenNMS:Name=Eventd = RUNNING
             */
            for (final String result : statusResults) {
                final Matcher m = SERVICE_STATUS_PATTERN.matcher(result);
                if (!m.matches()) {
                    throw new IllegalStateException("Result \"" + result + "\" does not match our regular expression");
                }
                results.put(m.group(1), m.group(2).toLowerCase());
            }
        } catch (final Throwable e) {
            throw new IllegalStateException("Unable to retrieve status from running services.", e);
        }

        return results;
    }

	public void queryStatus() throws Exception {
        Map<String, String> results = Collections.emptyMap();

        try {
            results = this.retrieveStatus();
        } catch (final IllegalStateException e) {
            LOG.debug("Could not fetch status: " + e.getMessage());
            if (m_controller.isVerbose()) {
                System.out.println("Could not connect to the OpenNMS JVM"
                        + " (OpenNMS might not be running or "
                        + "could be starting up or shutting down): "
                        + e.getMessage());
            }
            m_status = Status.CONNECTION_REFUSED;
            return;
        }

        /*
         * We want our output to look like this:
         *     OpenNMS.Eventd         : running
         */
        int running = 0;
        int services = 0;
        for (final Entry<String, String> entry : results.entrySet()) {
            String daemon = entry.getKey();
            String status = entry.getValue();

            services++;
            if (status.equals("running")) {
                running++;
            }
            if (m_controller.isVerbose()) {
                System.out.println(StatusGetter.formatStatusEntry(daemon, status));
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

	static String formatStatusEntry(final String service, final String status) {
	    return String.format("%-35s : %s", service.replace(":", "."), status.toLowerCase());
	}

}

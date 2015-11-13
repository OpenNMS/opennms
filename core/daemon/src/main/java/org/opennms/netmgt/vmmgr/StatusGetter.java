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

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StatusGetter {
    public enum Status {
        UNKNOWN, RUNNING, PARTIALLY_RUNNING, NOT_RUNNING, CONNECTION_REFUSED
    }

    //public static final String DEFAULT_JMX_URL = "service:jmx:rmi:///jndi/rmi://127.0.0.1:1099/jmxrmi";
    public static final String DEFAULT_JMX_URL = System.getProperty("com.sun.management.jmxremote.localConnectorAddress", "service:jmx:rmi:///jndi/rmi://127.0.0.1:1099/jmxrmi");

    private boolean m_verbose = false;

    private String m_jmxUrl = DEFAULT_JMX_URL;

    private Status m_status = Status.UNKNOWN;

    /**
     * <p>Constructor for StatusGetter.</p>
     */
    public StatusGetter() {
    }

    /**
     * <p>isVerbose</p>
     *
     * @return a boolean.
     */
    public boolean isVerbose() {
        return m_verbose;
    }

    /**
     * <p>setVerbose</p>
     *
     * @param verbose a boolean.
     */
    public void setVerbose(boolean verbose) {
        m_verbose = verbose;
    }

    /**
     * <p>getJmxUrl</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getJmxUrl() {
        return m_jmxUrl;
    }

    /**
     * <p>setJmxUrl</p>
     *
     * @param jmxUrl a {@link java.lang.String} object.
     */
    public void setJmxUrl(String jmxUrl) {
        m_jmxUrl = jmxUrl;
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
     * <p>main</p>
     *
     * @param argv an array of {@link java.lang.String} objects.
     * @throws java.lang.Exception if any.
     */
    public static void main(String[] argv) throws Exception {
        StatusGetter statusGetter = new StatusGetter();
        int i;

        for (i = 0; i < argv.length; i++) {
            if (argv[i].equals("-h")) {
                System.out.println("Accepted options:");
                System.out.println("        -v              Verbose mode.");
                System.out.println("        -u <URL>        Alternate JMX URL.");
                System.out.println("The default JMX URL is: "
                        + DEFAULT_JMX_URL);
                statusGetter.setVerbose(true);
            } else if (argv[i].equals("-v")) {
                statusGetter.setVerbose(true);
            } else if (argv[i].equals("-u")) {
                statusGetter.setJmxUrl(argv[i + 1]);
                i++;
            } else {
                throw new Exception("Invalid command-line option: \""
                        + argv[i] + "\"");
            }
        }

        statusGetter.queryStatus();

        if (statusGetter.getStatus() == Status.NOT_RUNNING
                || statusGetter.getStatus() == Status.CONNECTION_REFUSED) {
            System.exit(3); // According to LSB: 3 - service not running
        } else if (statusGetter.getStatus() == Status.PARTIALLY_RUNNING) {
            /*
             * According to LSB: reserved for application So, I say 160 -
             * partially running
             */
            System.exit(160);
        } else if (statusGetter.getStatus() == Status.RUNNING) {
            System.exit(0); // everything should be good and running
        } else {
            throw new Exception("Unknown status returned from "
                    + "statusGetter.getStatus(): " + statusGetter.getStatus());
        }
    }

    /**
     * <p>queryStatus</p>
     *
     * @throws java.lang.Exception if any.
     */
    public void queryStatus() throws Exception {

        Pattern p = Pattern.compile("Status: OpenNMS:Name=(\\S+) = (\\S+)");

        LinkedHashMap<String, String> results = new LinkedHashMap<String, String>();

        List<String> statusResults = (List<String>)Controller.doInvokeOperation(getJmxUrl(), "status");

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
            if (m_verbose) {
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

/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2013-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.provision.service.vmware;

import java.io.File;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.PosixParser;
import org.apache.commons.io.IOUtils;
import org.opennms.core.utils.ConfigFileConstants;
import org.opennms.core.xml.JaxbUtils;
import org.opennms.netmgt.config.vmware.VmwareConfig;
import org.opennms.netmgt.config.vmware.VmwareServer;
import org.opennms.netmgt.provision.persist.requisition.Requisition;

/**
 * The Class VmwareRequisitionTool
 * 
 * @author Alejandro Galue <agalue@opennms.org>
 */
public abstract class VmwareRequisitionTool {

    public static void main(String[] args) throws Exception {
        final Options options = new Options();

        final CommandLineParser parser = new PosixParser();
        final CommandLine cmd = parser.parse(options, args);

        @SuppressWarnings("unchecked")
        List<String> arguments = (List<String>) cmd.getArgList();

        if (arguments.size() < 1) {
            usage(options, cmd);
            System.exit(1);
        }

        String urlString = arguments.remove(0).replaceFirst("vmware", "http"); // Internal trick to avoid confusions.
        URL url = new URL(urlString);

        // Parse vmware-config.xml and retrieve the credentials to avoid initialize Spring
        if ( ! url.getQuery().contains("username") && url.getUserInfo() == null ) {
            File cfg = new File(ConfigFileConstants.getFilePathString(), "vmware-config.xml");
            if (cfg.exists()) {
                String username = null;
                String password = null;
                VmwareConfig config = JaxbUtils.unmarshal(VmwareConfig.class, cfg);
                for (VmwareServer srv : config.getVmwareServerCollection()) {
                    if (srv.getHostname().equals(url.getHost())) {
                        username = srv.getUsername();
                        password = srv.getPassword();
                    }
                }
                if (username == null || password == null) {
                    throw new IllegalArgumentException("Can't retrieve credentials for " + url.getHost() + " from " + cfg);
                }
                // Add credentials to URL
                urlString = urlString + ";username=" + username + ";password=" + password;
                url = new URL(urlString);
            }
        }

        VmwareRequisitionUrlConnection c = new VmwareRequisitionUrlConnection(url) {
            @Override
            protected Requisition getExistingRequisition(String foreignSource) {
                // This is not elegant but it is necessary to avoid booting Spring
                File req = new File(ConfigFileConstants.getFilePathString(), "imports" + File.separator + foreignSource + ".xml");
                if (req.exists()) {
                    return JaxbUtils.unmarshal(Requisition.class, req);
                }
                return null;
            }
        };
        c.connect();
        InputStream is = c.getInputStream();
        if (is == null) {
            System.err.println("Couldn't generate requisition from " +  urlString);
            System.exit(1);
        } else {
            System.out.println(IOUtils.toString(is, StandardCharsets.UTF_8));
        }
    }

    private static void usage(final Options options, final CommandLine cmd, final String error, final Exception e) {
        final HelpFormatter formatter = new HelpFormatter();
        final PrintWriter pw = new PrintWriter(System.out);
        if (error != null) {
            pw.println("An error occurred: " + error + "\n");
        }
        final StringBuilder sb = new StringBuilder();
        sb.append("Usage: VmwareRequisitionTool vmware://username:password@host[/foreign-source]?keyA=valueA;keyB=valueB;...\n");
        sb.append(" Note: in case the credentials are not specified, they should exist on vmware.config.xml\n");

        formatter.printHelp(sb.toString(), options);

        if (e != null) {
            pw.println(e.getMessage());
            e.printStackTrace(pw);
        }

        pw.close();
    }

    private static void usage(final Options options, final CommandLine cmd) {
        usage(options, cmd, null, null);
    }

}

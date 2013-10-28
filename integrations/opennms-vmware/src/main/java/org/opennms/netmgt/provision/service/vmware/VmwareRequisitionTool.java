/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
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
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.URL;
import java.util.List;
import java.util.Properties;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.PosixParser;
import org.apache.commons.io.IOUtils;
import org.opennms.core.utils.ConfigFileConstants;

/**
 * The Class VmwareRequisitionTool
 * 
 * @author Alejandro Galue <agalue@opennms.org>
 */
public class VmwareRequisitionTool {

    public static void main(String args[]) throws Exception {
        final Options options = new Options();

        final CommandLineParser parser = new PosixParser();
        final CommandLine cmd = parser.parse(options, args);

        @SuppressWarnings("unchecked")
        List<String> arguments = (List<String>) cmd.getArgList();

        if (arguments.size() < 1) {
            usage(options, cmd);
            System.exit(1);
        }

        // Load opennms.properties into the system properties
        try {
            Properties opennmsProperties = new Properties();
            File file = new File(ConfigFileConstants.getFilePathString(), File.separator + "opennms.properties");
            opennmsProperties.load(new FileInputStream(file));
            for (Object prop : opennmsProperties.keySet()) {
                String key = (String) prop;
                String val = opennmsProperties.getProperty(key);
                System.setProperty(key, val);
            }
        } catch (Exception e) {
            System.err.println("Can't load OpenNMS Properties.");
            System.exit(1);
        }

        String urlString = arguments.remove(0);
        URL url = new URL(urlString.replaceFirst("vmware", "http")); // Internal trick to avoid confusions.

        VmwareRequisitionUrlConnection c = new VmwareRequisitionUrlConnection(url);
        c.connect();
        InputStream is = c.getInputStream();
        if (is == null) {
            System.err.println("Couldn't generate requisition from " +  urlString);
            System.exit(1);
        } else {
            System.out.println(IOUtils.toString(is, "UTF-8"));
        }
    }

    private static void usage(final Options options, final CommandLine cmd, final String error, final Exception e) {
        final HelpFormatter formatter = new HelpFormatter();
        final PrintWriter pw = new PrintWriter(System.out);
        if (error != null) {
            pw.println("An error occurred: " + error + "\n");
        }

        formatter.printHelp("Usage: VmwareRequisitionTool vmware://username:password@host[/foreign-source]?keyA=valueA;keyB=valueB;...", options);

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

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
import org.opennms.core.mate.api.Interpolator;
import org.opennms.core.mate.api.Scope;
import org.opennms.core.mate.api.SecureCredentialsVaultScope;
import org.opennms.core.spring.BeanUtils;
import org.opennms.core.utils.ConfigFileConstants;
import org.opennms.core.xml.JaxbUtils;
import org.opennms.features.scv.api.SecureCredentialsVault;
import org.opennms.netmgt.config.vmware.VmwareConfig;
import org.opennms.netmgt.config.vmware.VmwareServer;

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

        final SecureCredentialsVault secureCredentialsVault = BeanUtils.getBean("daoContext", "jceksSecureCredentialsVault", SecureCredentialsVault.class);

        String urlString = arguments.remove(0).replaceFirst("vmware", "http"); // Internal trick to avoid confusions.
        URL url = new URL(urlString);

        // Parse vmware-config.xml and retrieve the credentials to avoid initialize Spring
        if (url.getQuery() == null || (!url.getQuery().contains("username") && url.getUserInfo() == null)) {
            File cfg = new File(ConfigFileConstants.getFilePathString(), "vmware-config.xml");
            if (cfg.exists()) {
                String username = null;
                String password = null;
                VmwareConfig config = JaxbUtils.unmarshal(VmwareConfig.class, cfg);
                for (VmwareServer srv : config.getVmwareServerCollection()) {
                    if (srv.getHostname().equals(url.getHost())) {
                        final Scope scvScope = new SecureCredentialsVaultScope(secureCredentialsVault);
                        username = Interpolator.interpolate(srv.getUsername(), scvScope).output;
                        password = Interpolator.interpolate(srv.getPassword(), scvScope).output;
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

        final VmwareRequisitionUrlConnection c = new VmwareRequisitionUrlConnection(url);
        c.connect();
        InputStream is = c.getInputStream();
        if (is == null) {
            System.err.println("Couldn't generate requisition from " +  urlString);
            System.exit(1);
        } else {
            System.out.println(IOUtils.toString(is, StandardCharsets.UTF_8));
        }
        System.exit(0);
    }

    private static void usage(final Options options, final CommandLine cmd, final String error, final Exception e) {
        final HelpFormatter formatter = new HelpFormatter();
        final PrintWriter pw = new PrintWriter(System.out);
        if (error != null) {
            pw.println("An error occurred: " + error + "\n");
        }
        final StringBuilder sb = new StringBuilder();
        sb.append("Usage: VmwareRequisitionTool vmware://host[/foreign-source]?username=foo;password=bar;keyA=valueA;keyB=valueB;...\n");
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

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
package org.opennms.netmgt.snmp.commands;

import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.Option;
import org.apache.karaf.shell.api.action.lifecycle.Reference;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.opennms.core.utils.ConfigFileConstants;
import org.opennms.core.xml.JaxbUtils;
import org.opennms.netmgt.config.api.SnmpAgentConfigFactory;
import org.opennms.netmgt.config.snmp.SnmpConfig;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;

@Command(scope = "opennms", name = "snmp-config-decrypt", description = "Decrypts snmp config and saves it on file system as a separate file")
@Service
public class DecryptSnmpConfigCommand implements Action {

    @Reference
    SnmpAgentConfigFactory snmpAgentConfigFactory;

    @Option(name = "-f", aliases = "--filename", description = "File name where decrypted SNMP")
    String fileName = "snmp-config-decrypt.xml";

    @Override
    public Object execute() throws Exception {

        boolean encryptionEnabled = Boolean.getBoolean("org.opennms.snmp.encryption.enabled");
        if (!encryptionEnabled) {
            System.out.println("Encryption is not enabled, \n" +
                    " Set system property `org.opennms.snmp.encryption.enabled` to true to enable encryption on Snmp.");
            return null;
        }

        if (ConfigFileConstants.getFileName(ConfigFileConstants.SNMP_CONF_FILE_NAME).equals(fileName)) {
            System.out.println("Can't decrypt to original file");
            return null;
        }
        File file = new File(ConfigFileConstants.getHome(), "etc" + File.separator + fileName);
        SnmpConfig snmpConfig = snmpAgentConfigFactory.getSnmpConfig();
        try (StringWriter writer = new StringWriter()) {
            JaxbUtils.marshal(snmpConfig, writer);
            String marshalledConfig = writer.toString();
            FileOutputStream out = new FileOutputStream(file);
            Writer fileWriter = new OutputStreamWriter(out, StandardCharsets.UTF_8);
            fileWriter.write(marshalledConfig);
            fileWriter.flush();
            fileWriter.close();
        } catch (Exception e) {
            System.out.printf("Exception while writing to decrypted file %s , error : %s", fileName, e.getMessage());
        }
        System.out.printf("Decrypted snmp config to file %s", fileName);
        return null;
    }
}

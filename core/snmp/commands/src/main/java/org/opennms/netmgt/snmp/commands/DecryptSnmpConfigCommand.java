/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2022 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2022 The OpenNMS Group, Inc.
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
                    " Set System property `org.opennms.snmp.encryption.enabled` to true to enable encryption on Snmp.");
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

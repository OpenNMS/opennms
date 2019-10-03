/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2019-2019 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2019 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.flows.clazzification.shell;

import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.Completion;
import org.apache.karaf.shell.api.action.Option;
import org.apache.karaf.shell.api.action.lifecycle.Reference;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.opennms.netmgt.flows.classification.ClassificationEngine;
import org.opennms.netmgt.flows.classification.ClassificationRequest;
import org.opennms.netmgt.flows.classification.persistence.api.Protocol;
import org.opennms.netmgt.flows.classification.persistence.api.Protocols;

import com.google.common.base.Strings;

@Command(scope="opennms-classification", name="classify", description = "Verify the classification rules by classifying a request, for example: opennms-classification:classify --protocol tcp --srcAddress 127.0.0.1 --srcPort 55000 --dstAddress 8.8.8.8 --destPort 22 --exporterAddress 127.0.0.1")
@Service
public class ClassificationClassifyCommand implements Action {

    @Reference
    private ClassificationEngine classificationEngine;

    @Option(name = "--protocol", description = "Protocol", required = true)
    @Completion(value=ProtocolCompleter.class)
    private String protocol;

    @Option(name = "--destAddress", aliases = {"--dstAddress"}, description = "Destination Address", required = true)
    private String dstAddress;

    @Option(name = "--destPort", aliases = {"--dstPort"}, description = "Destination Port", required = true)
    private Integer dstPort;

    @Option(name = "--exporterAddress", description = "Exporter Address", required = true)
    private String exporterAddress;

    @Option(name = "--srcAddress", description = "Source Address", required = true)
    private String srcAddress;

    @Option(name = "--srcPort", description = "Source Port", required = true)
    private Integer srcPort;

    @Override
    public Object execute() throws Exception {
        // Determine Protocol
        final Protocol theProtocol = Protocols.getProtocol(protocol);
        if (theProtocol == null) {
            System.err.println("The provided protocol '" + protocol + "' is not supported");
            return null;
        }

        // Create Request
        final ClassificationRequest request = new ClassificationRequest();
        request.setProtocol(theProtocol);
        request.setDstAddress(dstAddress);
        request.setDstPort(dstPort);
        request.setExporterAddress(exporterAddress);
        request.setSrcAddress(srcAddress);
        request.setSrcPort(srcPort);

        // Classify
        final String result = classificationEngine.classify(request);
        if (Strings.isNullOrEmpty(result)) {
            System.out.println("Unknown");
        } else {
            System.out.println(result);
        }
        return null;
    }
}

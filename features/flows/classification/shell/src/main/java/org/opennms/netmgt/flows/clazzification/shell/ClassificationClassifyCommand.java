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

@Command(scope="opennms", name="classify-flow", description = "Verify the classification rules by classifying a request, for example: opennms:classify-flow --protocol tcp --srcAddress 127.0.0.1 --srcPort 55000 --dstAddress 8.8.8.8 --destPort 22 --exporterAddress 127.0.0.1")
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

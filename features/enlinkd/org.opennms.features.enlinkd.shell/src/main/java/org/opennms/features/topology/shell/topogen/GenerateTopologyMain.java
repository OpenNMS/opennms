/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2016 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2016 The OpenNMS Group, Inc.
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

package org.opennms.features.topology.shell.topogen;


import static org.kohsuke.args4j.OptionHandlerFilter.ALL;

import java.io.IOException;
import java.sql.SQLException;
import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Argument;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import org.slf4j.Logger;

@Command(scope="enlinkd", name="generate-topology", description="Creates a linkd topology")
@Service
public class GenerateTopologyMain {

    @Option(name="--nodes",usage="generate <N> OmnsNodes")
    private Integer amountNodes;

    @Option(name="--elements",usage="generate <N> CdpElements")
    private Integer amountElements;

    @Option(name="--links",usage="generate <N> CdpLinks")
    private Integer amountLinks;

    @Option(name="--snmpinterfaces",usage="generate <N> SnmpInterfaces but not more than amount nodes")
    private Integer amountSnmpInterfaces;

    @Option(name="--ipinterfaces",usage="generate <N> IpInterfaces but not more than amount snmp interfaces")
    private Integer amountIpInterfaces;

    @Option(name="--topology",usage="type of topology (complete | ring | random)")
    private String topology = "random";

    @Option(name="--protocol",usage="type of protocol (cdp | isis | lldp | ospf)")
    private String protocol = "cdp";

    @Option(name="--delete", usage = "delete existing toplogogy (all OnmsNodes, CdpElements and CdpLinks)")
    private boolean deleteExistingTolology = false;

    private void invokeGenerator() throws SQLException, IOException {
        TopologyGenerator generator = TopologyGenerator.builder()
            .amountElements(this.amountElements)
            .amountIpInterfaces(this.amountIpInterfaces)
            .amountLinks(this.amountLinks)
            .amountNodes(this.amountNodes)
            .amountElements(this.amountElements)
            .amountSnmpInterfaces(amountSnmpInterfaces)
            .deleteExistingTolology(this.deleteExistingTolology)
            .protocol(TopologyGenerator.Protocol.valueOf(this.protocol))
            .topology(TopologyGenerator.Topology.valueOf(this.topology))
            .persister(new TopologyPersister())
            .build();
        generator.createNetwork();
    }

//    private Integer minusOneToNull() {
//      // we ned to do this workaround
//
//    }

    /** Execute via Standalone Java program. */
    public static void main(String args[]) throws Exception {
        GenerateTopologyMain main = new GenerateTopologyMain();
        main.setupMainArguments(args);
        main.invokeGenerator();
    }

    private void setupMainArguments(String[] args) {

        CmdLineParser parser = new CmdLineParser(this);
        try {
            parser.parseArgument(args);
        } catch( CmdLineException e ) {
            System.err.println(e.getMessage());
            System.err.println("java TopologyGenerator [options...]");
            parser.printUsage(System.err);
            System.err.println();
            System.err.println("  Example: java TopologyGenerator"+parser.printExample(ALL));
        }
    }

}

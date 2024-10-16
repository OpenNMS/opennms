/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2023 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2023 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.provision.requisition.command;

import java.util.ArrayList;
import java.util.List;

import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.Option;
import org.apache.karaf.shell.api.action.lifecycle.Reference;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.apache.karaf.shell.api.console.Terminal;
import org.apache.karaf.shell.support.table.ShellTable;
import org.opennms.core.criteria.Criteria;
import org.opennms.core.criteria.restrictions.RegExpRestriction;
import org.opennms.core.spring.BeanUtils;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsMetaData;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.provision.SnmpMetadataException;
import org.opennms.netmgt.provision.SnmpMetadataProvisioningAdapter;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionOperations;

import com.google.common.primitives.Booleans;

@Command(scope = "opennms", name = "snmp-metadata-provisioning-adapter", description = "Trigger SnmpMetadataProvisioningAdapter for a set of nodes")
@Service
public class SnmpMetadataProvisioningAdapterCommand implements Action {
    @Reference
    public TransactionOperations transactionOperations;

    @Reference
    private NodeDao nodeDao;

    @Reference
    Terminal terminal;

    @Option(name = "-i", aliases = "--id", description = "Node Id to process", required = false, multiValued = false)
    Integer id;

    @Option(name = "-a", aliases = "--all", description = "Process all nodes", required = false, multiValued = false)
    Boolean all;

    @Option(name = "-r", aliases = "--regexp", description = "Process nodes by label (regular expression)", required = false, multiValued = false)
    String regexp;

    @Option(name = "-p", aliases = "--persist", description = "Persist data instead of displaying it", required = false, multiValued = false)
    Boolean persist = false;


    @Override
    public Object execute() {
        final int optionsSet = Booleans.countTrue(id != null, regexp != null, all != null);
        if (optionsSet == 0) {
            System.out.println("Please provide one of the options --id/-i, --all/-a or --regexp/-r");
        } else {
            if (optionsSet > 1) {
                System.out.println("Please provide only one option (--id, --all, --regexp)");
            } else {
                transactionOperations.execute(new TransactionCallback<Void>() {
                    @Override
                    public Void doInTransaction(TransactionStatus transactionStatus) {
                        final List<OnmsNode> onmsNodes = new ArrayList<>();

                        if (all != null) {
                            onmsNodes.addAll(nodeDao.findAll());
                        }

                        if (regexp != null) {
                            final Criteria criteria = new Criteria(OnmsNode.class);
                            criteria.addRestriction(new RegExpRestriction("label", regexp));
                            onmsNodes.addAll(nodeDao.findMatching(criteria));
                        }

                        if (id != null) {
                            onmsNodes.add(nodeDao.get(id));
                        }

                        processNodes(onmsNodes, persist);

                        return null;
                    }
                });
            }
        }

        return null;
    }


    private void processNodes(final List<OnmsNode> onmsNodes, final boolean persist) {
        final SnmpMetadataProvisioningAdapter snmpMetadataProvisioningAdapter = BeanUtils.getBean("provisiondContext", "snmpMetadataProvisioningAdapter", SnmpMetadataProvisioningAdapter.class);

        System.out.println("Processing " + onmsNodes.size() + " node(s)...");

        for (final OnmsNode onmsNode : onmsNodes) {
            System.out.printf("\nProcessing node '%s' (id=%d)...\n", onmsNode.getLabel(), onmsNode.getId());

            if (onmsNode == null) {
                System.out.println("Error: node is null.");
                continue;
            }

            final OnmsIpInterface onmsIpInterface = onmsNode.getPrimaryInterface();

            if (onmsIpInterface == null) {
                System.out.println("Error: primary interface is null.");
                continue;
            }

            final List<OnmsMetaData> results;

            try {
                if (persist) {
                    System.out.printf("Calling SnmpMetadataProvisioingAdapter.doUpdateNode(%d)...\n", onmsNode.getId());
                    snmpMetadataProvisioningAdapter.doUpdateNode(onmsNode.getId());
                    continue;
                } else {
                    results = snmpMetadataProvisioningAdapter.createMetadata(onmsNode, onmsNode.getPrimaryInterface());
                }
            } catch (SnmpMetadataException e) {
                System.out.println("Error: provisioning adapter threw exception");
                e.printStackTrace(System.out);
                continue;
            }

            if (results == null) {
                System.out.println("Error: node don't support SNMP (provisioning adapter returned null).");
                continue;
            }

            if (results.size() == 0) {
                System.out.println("Provisioning adapter returned no data.");
                continue;
            } else {
                System.out.printf("Provisioning adapter returned %d metadata entries.\n\n", results.size());
            }

            final ShellTable table = new ShellTable();
            table.size(terminal.getWidth() - 1);
            table.column("Context");
            table.column("Key");
            table.column("Value");


            for (final OnmsMetaData onmsMetaData : results) {
                table.addRow().addContent(onmsMetaData.getContext(), onmsMetaData.getKey(), onmsMetaData.getValue());
            }

            table.print(System.out);
        }
    }
}

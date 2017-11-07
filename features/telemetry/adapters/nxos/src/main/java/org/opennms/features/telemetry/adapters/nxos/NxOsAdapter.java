/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2017-2017 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2017 The OpenNMS Group, Inc.
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

package org.opennms.features.telemetry.adapters.nxos;

import java.io.File;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Optional;

import org.opennms.features.telemetry.adapters.nxos.proto.TelemetryBis;
import org.opennms.netmgt.collection.api.CollectionAgent;
import org.opennms.netmgt.collection.api.CollectionAgentFactory;
import org.opennms.netmgt.collection.api.CollectionSet;
import org.opennms.netmgt.dao.api.InterfaceToNodeCache;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.telemetry.adapters.api.TelemetryMessage;
import org.opennms.netmgt.telemetry.adapters.api.TelemetryMessageLog;
import org.opennms.netmgt.telemetry.adapters.collection.AbstractPersistingAdapter;
import org.opennms.netmgt.telemetry.adapters.collection.CollectionSetWithAgent;
import org.opennms.netmgt.telemetry.adapters.collection.ScriptedCollectionSetBuilder;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionOperations;

import com.google.common.collect.Iterables;
import com.google.protobuf.ExtensionRegistry;

public class NxOsAdapter extends AbstractPersistingAdapter {

    private static final Logger LOG = LoggerFactory.getLogger(NxOsAdapter.class);

    private static final ExtensionRegistry s_registry = ExtensionRegistry.newInstance();

    static {
        TelemetryBis.registerAllExtensions(s_registry);
    }

    @Autowired
    private CollectionAgentFactory collectionAgentFactory;

    @Autowired
    private InterfaceToNodeCache interfaceToNodeCache;

    @Autowired
    private NodeDao nodeDao;

    @Autowired
    private TransactionOperations transactionTemplate;
    
    private BundleContext bundleContext;

    private String script;

    private final ThreadLocal<ScriptedCollectionSetBuilder> scriptedCollectionSetBuilders = new ThreadLocal<ScriptedCollectionSetBuilder>() {
        @Override
        protected ScriptedCollectionSetBuilder initialValue() {
            try {
                return new ScriptedCollectionSetBuilder(new File(script), bundleContext);
            } catch (Exception e) {
                LOG.error("Failed to create builder for script '{}'.", script, e);
                return null;
            }
        }
    };

    public String getScript() {
        return script;
    }

    public void setScript(String script) {
        this.script = script;
    }
    
    @Override
    public Optional<CollectionSetWithAgent> handleMessage(TelemetryMessage message, TelemetryMessageLog messageLog) throws Exception {
    
        final TelemetryBis.Telemetry msg = TelemetryBis.Telemetry.parseFrom(message.getByteArray(), s_registry);

        CollectionAgent agent = null;
        try {
            // Attempt to resolve the systemId to an InetAddress
            final InetAddress inetAddress = InetAddress.getByName(msg.getNodeIdStr());
            final Optional<Integer> nodeId = interfaceToNodeCache.getFirstNodeId(messageLog.getLocation(), inetAddress);
            if (nodeId.isPresent()) {
                // NOTE: This will throw a IllegalArgumentException if the nodeId/inetAddress pair does not exist in the database
                agent = collectionAgentFactory.createCollectionAgent(Integer.toString(nodeId.get()), inetAddress);
            }
        } catch (UnknownHostException e) {
            LOG.debug("Could not convert system id to address: {}", msg.getNodeIdStr());
        }

        if (agent == null) {
            // We were unable to build the agent by resolving the systemId, try finding
            // a node with a matching label
            agent = transactionTemplate.execute(new TransactionCallback<CollectionAgent>() {
                @Override
                public CollectionAgent doInTransaction(TransactionStatus status) {
                    final OnmsNode node = Iterables.getFirst(nodeDao.findByLabel(msg.getNodeIdStr()), null);
                    if (node != null) {
                        final OnmsIpInterface primaryInterface = node.getPrimaryInterface();
                        return collectionAgentFactory.createCollectionAgent(primaryInterface);
                    }
                    return null;
                }
            });
        }

        if (agent == null) {
            LOG.warn("Unable to find node and inteface for system id: {}", msg.getNodeIdStr());
            return Optional.empty();
        }

        final ScriptedCollectionSetBuilder builder = scriptedCollectionSetBuilders.get();
        if (builder == null) {
            throw new Exception(String.format("Error compiling script '%s'. See logs for details.", script));
        }
        final CollectionSet collectionSet = builder.build(agent, msg);
        return Optional.of(new CollectionSetWithAgent(agent, collectionSet));
    }

    public void setCollectionAgentFactory(CollectionAgentFactory collectionAgentFactory) {
        this.collectionAgentFactory = collectionAgentFactory;
    }

    public void setInterfaceToNodeCache(InterfaceToNodeCache interfaceToNodeCache) {
        this.interfaceToNodeCache = interfaceToNodeCache;
    }

    public void setNodeDao(NodeDao nodeDao) {
        this.nodeDao = nodeDao;
    }

    public void setTransactionTemplate(TransactionOperations transactionTemplate) {
        this.transactionTemplate = transactionTemplate;
    }

    public void setBundleContext(BundleContext bundleContext) {
        this.bundleContext = bundleContext;
    }

}

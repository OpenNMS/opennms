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

package org.opennms.netmgt.telemetry.adapters.nxos;

import java.io.File;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Optional;

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

/**
 * The Class AbstractNxosAdapter.
 * 
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a>
 */
public abstract class AbstractNxosAdapter extends AbstractPersistingAdapter {

    /** The Constant LOG. */
    private static final Logger LOG = LoggerFactory.getLogger(AbstractNxosAdapter.class);

    /** The collection agent factory. */
    @Autowired
    private CollectionAgentFactory collectionAgentFactory;

    /** The interface to node cache. */
    @Autowired
    private InterfaceToNodeCache interfaceToNodeCache;

    /** The node DAO. */
    @Autowired
    private NodeDao nodeDao;

    /** The transaction template. */
    @Autowired
    private TransactionOperations transactionTemplate;

    /** The bundle context. */
    private BundleContext bundleContext;

    /** The script. */
    private String script;

    /** The scripted collection set builders. */
    protected final ThreadLocal<ScriptedCollectionSetBuilder> scriptedCollectionSetBuilders = new ThreadLocal<ScriptedCollectionSetBuilder>() {
        @Override
        protected ScriptedCollectionSetBuilder initialValue() {
            try {
                if (bundleContext != null) {
                    return new ScriptedCollectionSetBuilder(new File(script), bundleContext);
                } else {
                    return new ScriptedCollectionSetBuilder(new File(script));
                }
            } catch (Exception e) {
                LOG.error("Failed to create builder for script '{}'.", script, e);
                return null;
            }
        }
    };

    /**
     * Gets the script.
     *
     * @return the script
     */
    public String getScript() {
        return script;
    }

    /**
     * Sets the script.
     *
     * @param script the new script
     */
    public void setScript(String script) {
        this.script = script;
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.telemetry.adapters.collection.AbstractPersistingAdapter#handleMessage(org.opennms.netmgt.telemetry.adapters.api.TelemetryMessage, org.opennms.netmgt.telemetry.adapters.api.TelemetryMessageLog)
     */
    abstract public Optional<CollectionSetWithAgent> handleMessage(TelemetryMessage message, TelemetryMessageLog messageLog) throws Exception;

    /**
     * Gets the collection agent.
     *
     * @param messageLog the message log
     * @param nodeIdStr the node id string
     * @return the collection agent
     */
    protected CollectionAgent getCollectionAgent(TelemetryMessageLog messageLog, final String nodeIdStr) {
        CollectionAgent agent = null;
        try {
            final InetAddress inetAddress = InetAddress.getByName(nodeIdStr);
            final Optional<Integer> nodeId = interfaceToNodeCache.getFirstNodeId(messageLog.getLocation(), inetAddress);
            if (nodeId.isPresent()) {
                // NOTE: This will throw a IllegalArgumentException if the nodeId/inetAddress pair does not exist in the database
                agent = collectionAgentFactory.createCollectionAgent(Integer.toString(nodeId.get()), inetAddress);
            }
        } catch (UnknownHostException e) {
            LOG.debug("Could not convert system id to address: {}", nodeIdStr);
        }

        if (agent == null) {
            // We were unable to build the agent by resolving the systemId, try finding
            // a node with a matching label
            agent = transactionTemplate.execute(new TransactionCallback<CollectionAgent>() {
                @Override
                public CollectionAgent doInTransaction(TransactionStatus status) {
                    final OnmsNode node = Iterables.getFirst(nodeDao.findByLabel(nodeIdStr), null);
                    if (node != null) {
                        final OnmsIpInterface primaryInterface = node.getPrimaryInterface();
                        return collectionAgentFactory.createCollectionAgent(primaryInterface);
                    }
                    return null;
                }
            });
        }
        return agent;
    }

    /**
     * Gets the collection set with agent.
     *
     * @param agent the agent
     * @param msg the message object
     * @return the collection set with agent
     * @throws Exception the exception
     */
    protected Optional<CollectionSetWithAgent> getCollectionSetWithAgent(CollectionAgent agent, Object msg) throws Exception {
        final ScriptedCollectionSetBuilder builder = scriptedCollectionSetBuilders.get();
        if (builder == null) {
            throw new Exception(String.format("Error compiling script '%s'. See logs for details.", script));
        }
        final CollectionSet collectionSet = builder.build(agent, msg);
        return Optional.of(new CollectionSetWithAgent(agent, collectionSet));
    }

    /**
     * Sets the collection agent factory.
     *
     * @param collectionAgentFactory the new collection agent factory
     */
    public void setCollectionAgentFactory(CollectionAgentFactory collectionAgentFactory) {
        this.collectionAgentFactory = collectionAgentFactory;
    }

    /**
     * Sets the interface to node cache.
     *
     * @param interfaceToNodeCache the new interface to node cache
     */
    public void setInterfaceToNodeCache(InterfaceToNodeCache interfaceToNodeCache) {
        this.interfaceToNodeCache = interfaceToNodeCache;
    }

    /**
     * Sets the node DAO.
     *
     * @param nodeDao the new node DAO
     */
    public void setNodeDao(NodeDao nodeDao) {
        this.nodeDao = nodeDao;
    }

    /**
     * Sets the transaction template.
     *
     * @param transactionTemplate the new transaction template
     */
    public void setTransactionTemplate(TransactionOperations transactionTemplate) {
        this.transactionTemplate = transactionTemplate;
    }

    /**
     * Sets the bundle context.
     *
     * @param bundleContext the new bundle context
     */
    public void setBundleContext(BundleContext bundleContext) {
        this.bundleContext = bundleContext;
    }

}

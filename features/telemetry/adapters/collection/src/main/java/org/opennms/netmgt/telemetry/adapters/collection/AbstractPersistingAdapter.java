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

package org.opennms.netmgt.telemetry.adapters.collection;

import org.opennms.netmgt.collection.api.CollectionAgent;
import org.opennms.netmgt.collection.api.CollectionSet;
import org.opennms.netmgt.collection.api.Persister;
import org.opennms.netmgt.collection.api.PersisterFactory;
import org.opennms.netmgt.collection.api.ServiceParameters;
import org.opennms.netmgt.filter.api.FilterDao;
import org.opennms.netmgt.rrd.RrdRepository;
import org.opennms.netmgt.telemetry.adapters.api.Adapter;
import org.opennms.netmgt.telemetry.config.model.Package;
import org.opennms.netmgt.telemetry.config.model.Protocol;
import org.opennms.netmgt.telemetry.ipc.TelemetryProtos;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;
import java.util.Collections;
import java.util.Optional;

public abstract class AbstractPersistingAdapter implements Adapter {
    private final Logger LOG = LoggerFactory.getLogger(AbstractPersistingAdapter.class);

    private static final ServiceParameters EMPTY_SERVICE_PARAMETERS = new ServiceParameters(Collections.emptyMap());

    @Autowired
    private FilterDao filterDao;

    @Autowired
    private PersisterFactory persisterFactory;

    private Protocol protocol;

    /**
     * Build a collection set from the given message.
     *
     * The message log is also provided in case the log contains additional meta-data
     * required.
     *
     * IMPORTANT: Implementations of this method must be thread-safe.
     *
     * @param message message to be converted into a collection set
     * @param messageLog message log to which the message belongs
     * @return a {@link CollectionSetWithAgent} or an empty value if nothing should be persisted
     * @throws Exception if an error occured while generating the collection set
     */
    public abstract Optional<CollectionSetWithAgent> handleMessage(TelemetryProtos.TelemetryMessage message, TelemetryProtos.TelemetryMessageLog messageLog) throws Exception;

    @Override
    public void handleMessageLog(TelemetryProtos.TelemetryMessageLog messageLog) {
        for (TelemetryProtos.TelemetryMessage message : messageLog.getMessageList()) {
            final Optional<CollectionSetWithAgent> result;
            try {
                result = handleMessage(message, messageLog);
            } catch (Exception e) {
                LOG.warn("Failed to build a collection set from message: {}. Dropping.", message, e);
                return;
            }

            if (!result.isPresent()) {
                LOG.debug("No collection set was returned when processing message: {}. Dropping.", message);
                return;
            }

            // Locate the matching package definition
            final Package pkg = getPackageFor(protocol, result.get().getAgent());
            if (pkg == null) {
                LOG.warn("No matching package found for message: {}. Dropping.", message);
                return;
            }

            // Build the repository from the package definition
            final RrdRepository repository = new RrdRepository();
            repository.setStep(pkg.getRrd().getStep());
            repository.setHeartBeat(repository.getStep() * 2);
            repository.setRraList(pkg.getRrd().getRras());
            repository.setRrdBaseDir(new File(pkg.getRrd().getBaseDir()));

            // Persist!
            final CollectionSet collectionSet = result.get().getCollectionSet();
            LOG.trace("Persisting collection set: {} for message: {}", collectionSet, message);
            final Persister persister = persisterFactory.createPersister(EMPTY_SERVICE_PARAMETERS, repository);
            collectionSet.visit(persister);
        }
    }

    @Override
    public void setProtocol(Protocol protocol) {
        this.protocol = protocol;
    }

    private Package getPackageFor(Protocol protocol, CollectionAgent agent) {
        for (Package pkg : protocol.getPackages()) {
            if (pkg.getFilter() == null || pkg.getFilter().getContent() == null) {
                // No filter specified, always match
                return pkg;
            }
            final String filterRule = pkg.getFilter().getContent();
            // TODO: This is really inefficient, since it actually retrieves *all*
            // IP addresses that match the filter, and then checks of the given address
            // is in the set. See HZN-1161.
            // NOTE: The location of the host address is not taken into account.
            if (filterDao.isValid(agent.getHostAddress(), filterRule)) {
                return pkg;
            }
        }
        return null;
    }

}

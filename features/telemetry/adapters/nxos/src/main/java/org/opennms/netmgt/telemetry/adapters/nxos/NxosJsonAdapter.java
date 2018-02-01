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

import java.util.Arrays;
import java.util.Optional;

import org.json.JSONObject;
import org.opennms.netmgt.collection.api.CollectionAgent;
import org.opennms.netmgt.telemetry.adapters.api.TelemetryMessage;
import org.opennms.netmgt.telemetry.adapters.api.TelemetryMessageLog;
import org.opennms.netmgt.telemetry.adapters.collection.CollectionSetWithAgent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Class NxosJsonAdapter.
 * 
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a>
 */
public class NxosJsonAdapter extends AbstractNxosAdapter {

    /** The Constant LOG. */
    private static final Logger LOG = LoggerFactory.getLogger(NxosJsonAdapter.class);


    /* (non-Javadoc)
     * @see org.opennms.netmgt.telemetry.adapters.collection.AbstractPersistingAdapter#handleMessage(org.opennms.netmgt.telemetry.adapters.api.TelemetryMessage, org.opennms.netmgt.telemetry.adapters.api.TelemetryMessageLog)
     */
    @Override
    public Optional<CollectionSetWithAgent> handleMessage(TelemetryMessage message, TelemetryMessageLog messageLog) throws Exception {
        final String jsonText = new String(Arrays.copyOfRange(message.getByteArray(), 6, message.getByteArray().length));
        LOG.debug("Received JSON message: {}", jsonText);

        final JSONObject json = new JSONObject(jsonText);
        final String nodeIdStr = json.getString("node_id_str");
        final CollectionAgent agent = getCollectionAgent(messageLog, nodeIdStr);

        if (agent == null) {
            LOG.warn("Unable to find node and inteface for system id: {}", nodeIdStr);
            return Optional.empty();
        }
        return getCollectionSetWithAgent(agent, json);
    }

}

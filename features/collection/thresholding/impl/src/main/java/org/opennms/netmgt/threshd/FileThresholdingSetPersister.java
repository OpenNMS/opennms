/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2009-2018 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2018 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.threshd;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.JsonIOException;

/**
 * File based implementation of a {@link ThresholdingSetPersister}. Prototype for a forthcoming distributed DB (i.e. Cassandra) implementation
 */
public class FileThresholdingSetPersister implements ThresholdingSetPersister
{

    private Map<ThresholdingSessionKey, ThresholdingSetImpl> thresholdingSets = new HashMap<>();

    private Gson gson = new Gson();

    @Override
    public void persistSet(ThresholdingVisitor visitor) {
        // TODO Write the Thresholding set to disk
        ThresholdingSessionKey key = ((ThresholdingSessionImpl) visitor.getSession()).getKey();
        ThresholdingSet set = visitor.getSet();
        // start with file key of sessionKey + timestamp
        // retrieval can then be find all starrting with sessionKey and taking latest
        wrtieSetToDisk(String.valueOf(key.hashCode()), set);
    }

    @Override
    public void persistSet(ThresholdingSession session, ThresholdingSet set) {
        thresholdingSets.put(((ThresholdingSessionImpl) session).getKey(), (ThresholdingSetImpl) set);
    }

    @Override
    public ThresholdingSet getThresholdingSet(ThresholdingSession session, ThresholdingEventProxy eventProxy) throws ThresholdInitializationException {
        ThresholdingSessionKey key = ((ThresholdingSessionImpl) session).getKey();
        ThresholdingSetImpl tSet = thresholdingSets.get(key);
        if (tSet == null) {
            tSet = new ThresholdingSetImpl(key.getNodeId(), key.getLocation(), key.getServiceName(), ((ThresholdingSessionImpl) session).getRrdRepository(),
                                           ((ThresholdingSessionImpl) session).getServiceParameters(), ((ThresholdingSessionImpl) session).getResourceDao(), eventProxy);
            thresholdingSets.put(key, tSet);
        }
        return tSet;
    }

    @Override
    public void reinitializeThresholdingSets() {
        thresholdingSets.values().forEach(set -> set.reinitialize());
    }

    @Override
    public void clear(ThresholdingSession session) {
        ThresholdingSessionKey key = ((ThresholdingSessionImpl) session).getKey();
        thresholdingSets.remove(key);
    }

    private void wrtieSetToDisk(String key, ThresholdingSet set) {
        String now = String.valueOf(new Date().getTime());
        String filePath = key + "-" + now;
        try {
            gson.toJson(set, new FileWriter(filePath));
        } catch (JsonIOException | IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

}

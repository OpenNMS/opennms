/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.nrtg.nrtbroker.local.internal;

import org.opennms.nrtg.api.NrtBroker;
import org.opennms.nrtg.api.ProtocolCollector;
import org.opennms.nrtg.api.model.CollectionJob;
import org.opennms.nrtg.api.model.MeasurementSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * @author Markus Neumann
 * @author Christian Pape
 */

public class NrtBrokerLocal implements NrtBroker, NrtBrokerLocalMBean {

    private class TimedOutMap {
        private Map<String, List<MeasurementSet>> m_measurementSets = new HashMap<String, List<MeasurementSet>>();
        private Map<String, Date> m_lastAccess = new HashMap<String, Date>();

        public List<MeasurementSet> getAndRemove(String key) {
            synchronized (m_measurementSets) {
                m_lastAccess.put(key, new Date());
                List<MeasurementSet> measurementSetList = m_measurementSets.get(key);
                m_measurementSets.put(key, new ArrayList<MeasurementSet>());

                return measurementSetList;
            }
        }

        public void addMeasurementSets(Map<String, MeasurementSet> measurementSets) {
            for (Map.Entry<String, MeasurementSet> entry : measurementSets.entrySet()) {

                String arr[] = entry.getKey().split(",");

                for (String destination : arr) {
                    addMeasurementSet(destination.trim(), entry.getValue());
                }
            }

            doHousekeeping();
        }

        public void addMeasurementSet(String key, MeasurementSet measurementSet) {
            synchronized (m_measurementSets) {
                if (!m_measurementSets.containsKey(key)) {
                    m_measurementSets.put(key, new ArrayList<MeasurementSet>());
                }

                List<MeasurementSet> measurementSetList = m_measurementSets.get(key);
                measurementSetList.add(measurementSet);
            }
        }

        private void doHousekeeping() {
            synchronized (m_measurementSets) {
                for (String key : m_lastAccess.keySet()) {
                    Date lastAccess = m_lastAccess.get(key);
                    Date now = new Date();
                    if (now.getTime() - lastAccess.getTime() > 120000) {
                        m_lastAccess.remove(key);
                        m_measurementSets.remove(key);

                        logger.warn("Timed out object removed '{}'", key);
                    }
                }
            }
        }

        private Integer getAmountOfMeasurementSets() {
            return m_measurementSets.size();
        }
    }

    private static Logger logger = LoggerFactory.getLogger("OpenNMS.WEB." + NrtBrokerLocal.class);

    private List<ProtocolCollector> m_protocolCollectors;
    private TimedOutMap m_measurementSets = new TimedOutMap();

    @Override
    public void publishCollectionJob(CollectionJob collectionJob) {
        ProtocolCollector protocolCollector = getProtocolCollector(collectionJob.getService());

        if (protocolCollector != null) {
            collectionJob = protocolCollector.collect(collectionJob);
            collectionJob.setFinishedTimestamp(new Date());
            m_measurementSets.addMeasurementSets(collectionJob.getMeasurementSetsByDestination());
        } else {
            logger.warn("Cannot find collector for protocol {}", collectionJob.getService());
        }
    }

    @Override
    public List<MeasurementSet> receiveMeasurementSets(String destination) {
        return m_measurementSets.getAndRemove(destination);
    }

    public ProtocolCollector getProtocolCollector(String protocol) {
        for (ProtocolCollector protocolCollector : m_protocolCollectors) {
            if (protocolCollector.getProtcol().equals(protocol)) {
                return protocolCollector;
            }
        }

        return null;
    }

    public void setProtocolCollectors(List<ProtocolCollector> protocolCollectors) {
        this.m_protocolCollectors = protocolCollectors;
    }

    public List<ProtocolCollector> getProtocolCollectors() {
        return m_protocolCollectors;
    }

    @Override
    public Integer getMeasurementSetSize() {
        return m_measurementSets.getAmountOfMeasurementSets();
    }
}

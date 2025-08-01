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
package org.opennms.nrtg.nrtbroker.local.internal;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.opennms.nrtg.api.NrtBroker;
import org.opennms.nrtg.api.ProtocolCollector;
import org.opennms.nrtg.api.model.CollectionJob;
import org.opennms.nrtg.api.model.MeasurementSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Markus Neumann
 * @author Christian Pape
 */

public class NrtBrokerLocal implements NrtBroker, NrtBrokerLocalMBean {

    private static class TimedOutMap {
        private Map<String, List<MeasurementSet>> m_measurementSets = new HashMap<String, List<MeasurementSet>>();
        private Map<String, Date> m_lastAccess = new HashMap<String, Date>();

        public synchronized List<MeasurementSet> getAndRemove(final String key) {
            m_lastAccess.put(key, new Date());
            final List<MeasurementSet> measurementSetList = m_measurementSets.get(key);
            m_measurementSets.put(key, new ArrayList<MeasurementSet>());

            return measurementSetList;
        }

        public synchronized void addMeasurementSets(final Map<String, MeasurementSet> measurementSets) {
            for (final Map.Entry<String, MeasurementSet> entry : measurementSets.entrySet()) {
                String[] arr = entry.getKey().split(",");

                for (String destination : arr) {
                    addMeasurementSet(destination.trim(), entry.getValue());
                }
            }

            doHousekeeping();
        }

        public synchronized void addMeasurementSet(String key, MeasurementSet measurementSet) {
            if (!m_measurementSets.containsKey(key)) {
                m_measurementSets.put(key, new ArrayList<MeasurementSet>());
            }

            List<MeasurementSet> measurementSetList = m_measurementSets.get(key);
            measurementSetList.add(measurementSet);
        }

        private synchronized void doHousekeeping() {
            final Iterator<Entry<String,Date>> it = m_lastAccess.entrySet().iterator();
            while (it.hasNext()) {
                final Entry<String,Date> entry = it.next();
                final Date lastAccess = entry.getValue();
                final Date now = new Date();
                if (now.getTime() - lastAccess.getTime() > 120000) {
                    it.remove(); // removes entry from m_lastAccess to avoid ConcurrentModificationException
                    m_measurementSets.remove(entry.getKey());

                    logger.warn("Timed out object removed '{}'", entry.getKey());
                }
            }
        }

        private synchronized Integer getAmountOfMeasurementSets() {
            return m_measurementSets.size();
        }
    }

    private static Logger logger = LoggerFactory.getLogger(NrtBrokerLocal.class);

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

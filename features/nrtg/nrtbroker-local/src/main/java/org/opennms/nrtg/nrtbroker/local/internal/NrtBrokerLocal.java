package org.opennms.nrtg.nrtbroker.local.internal;

import org.opennms.nrtg.api.NrtBroker;
import org.opennms.nrtg.api.ProtocolCollector;
import org.opennms.nrtg.api.model.CollectionJob;
import org.opennms.nrtg.api.model.MeasurementSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class NrtBrokerLocal implements NrtBroker {

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
                addMeasurementSet(entry.getKey(), entry.getValue());
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
    }

    private List<ProtocolCollector> m_protocolCollectors = new ArrayList<ProtocolCollector>();
    private static Logger logger = LoggerFactory.getLogger("OpenNMS.WEB." + NrtBrokerLocal.class);
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
}

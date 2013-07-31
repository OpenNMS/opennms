/**
 * *****************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012 The OpenNMS Group, Inc. OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with OpenNMS(R). If not, see:
 * http://www.gnu.org/licenses/
 *
 * For more information contact: OpenNMS(R) Licensing <license@opennms.org> http://www.opennms.org/ http://www.opennms.com/
 * *****************************************************************************
 */
package org.opennms.nrtg.web.internal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.servlet.http.HttpSession;

import org.opennms.netmgt.config.SnmpAgentConfigFactory;
import org.opennms.netmgt.dao.api.GraphDao;
import org.opennms.netmgt.dao.api.ResourceDao;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsResource;
import org.opennms.netmgt.model.PrefabGraph;
import org.opennms.netmgt.model.RrdGraphAttribute;
import org.opennms.netmgt.rrd.RrdUtils;
import org.opennms.netmgt.snmp.SnmpAgentConfig;
import org.opennms.nrtg.api.NrtBroker;
import org.opennms.nrtg.api.model.CollectionJob;
import org.opennms.nrtg.api.model.DefaultCollectionJob;
import org.opennms.nrtg.api.model.MeasurementSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Markus Neumann
 * @author Christian Pape
 */
public class NrtController {

    private static Logger logger = LoggerFactory.getLogger("OpenNMS.WEB." + NrtController.class);
    private GraphDao m_graphDao;
    private ResourceDao m_resourceDao;
    private SnmpAgentConfigFactory m_snmpAgentConfigFactory;
    private NrtBroker m_nrtBroker;

    protected class MetricTuple {

        private String m_metricId;
        private String m_onmsLogicMetricId;

        public MetricTuple(String metricId, String onmsLogicMetricId) {
            m_onmsLogicMetricId = onmsLogicMetricId;
            m_metricId = metricId;
        }

        public String getMetricId() {
            return m_metricId;
        }

        public String getOnmsLogicMetricId() {
            return m_onmsLogicMetricId;
        }

        @Override
        public boolean equals(Object object) {
            if (object instanceof MetricTuple) {
                return getOnmsLogicMetricId().equals(((MetricTuple) object).getOnmsLogicMetricId());
            } else {
                return false;
            }
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 37 * hash + (this.m_metricId != null ? this.m_metricId.hashCode() : 0);
            return hash;
        }
    }

    public ModelAndView nrtStart(String resourceId, String report, HttpSession httpSession) {

        assert (resourceId != null);
        logger.debug("resourceId: '{}'", resourceId);

        assert (report != null);
        logger.debug("report: '{}'", report);

        OnmsResource reportResource = m_resourceDao.getResourceById(resourceId);

        PrefabGraph prefabGraph = m_graphDao.getPrefabGraph(report);

        String nrtCollectionTaskId = "NrtCollectionTaskId_" + System.currentTimeMillis();

        List<CollectionJob> collectionJobs = createCollectionJobs(reportResource, prefabGraph, nrtCollectionTaskId);
        for (CollectionJob collectionJob : collectionJobs) {
            m_nrtBroker.publishCollectionJob(collectionJob);
            getCollectionJobMap(httpSession, true).put(nrtCollectionTaskId, collectionJob);
        }

        ModelAndView modelAndView = new ModelAndView("nrt/realtime");
        modelAndView.addObject("nrtCollectionTaskId", nrtCollectionTaskId);

        modelAndView.addObject("graphTitle", prefabGraph.getTitle());
        modelAndView.addObject("graphName", prefabGraph.getName());
        modelAndView.addObject("graphDescription", prefabGraph.getDescription());

        NrtHelper nrtHelper = new NrtHelper();
        modelAndView.addObject("rrdGraphString", nrtHelper.cleanUpRrdGraphStringForWebUi(prefabGraph, getRequiredExternalPropertyAttributes(reportResource, prefabGraph), getRequiredStringPropertyAttributes(reportResource, prefabGraph)));

        Set<RrdGraphAttribute> relevantRrdGraphAttributes = getRequiredRrdGraphAttributes(reportResource, prefabGraph);
        Map<String, String> rrdGraphAttributesMetaData = getMetaDataForReport(relevantRrdGraphAttributes);
        Map<String, String> rrdGraphAttributesToMetricIds = getRrdGraphAttributesToMetricIds(rrdGraphAttributesMetaData);

        modelAndView.addObject("metricsMapping", nrtHelper.generateJsMappingObject(prefabGraph.getCommand(), rrdGraphAttributesToMetricIds));

        return modelAndView;
    }

    /**
     * Will be called by the JS-Graphing-Frontend as http/GET Publishes the CollectionJob corresponding to the
     * nrtCollectionTaskId.
     *
     * @param nrtCollectionTaskId
     * @param httpSession
     */
    public void nrtCollectionJobTrigger(String nrtCollectionTaskId, HttpSession httpSession) {
        logger.debug("Republish CollectionJobTrigger for '{}'", nrtCollectionTaskId);

        Map<String, CollectionJob> nrtCollectionTasks = getCollectionJobMap(httpSession, false);

        if (nrtCollectionTasks != null) {
            CollectionJob collectionJob = nrtCollectionTasks.get(nrtCollectionTaskId);
            logger.debug("CollectionJob is '{}'", collectionJob);
            if (collectionJob != null) {
                m_nrtBroker.publishCollectionJob(collectionJob);
                logger.debug("collectionJob was sent!");
            } else {
                logger.debug("collectionJob for collectionTask not found in session '{}'", nrtCollectionTaskId);
            }
        } else {
            logger.debug("No CollectionTasks map in session found.");
        }
    }

    /**
     * Will be called by the JS-Graphing-Frontend as http/GET Get Measurements from NrtBroker, transform them into Json and return
     * them to the JS-Graphing-Frontend
     *
     * @param nrtCollectionTaskId
     * @return Json Representation of MeasurementeSets for the given nrtCollectionTaskId
     */
    public String getMeasurementSetsForDestination(String nrtCollectionTaskId) {
        List<MeasurementSet> measurementSets = m_nrtBroker.receiveMeasurementSets(nrtCollectionTaskId);

        StringBuffer buffer = new StringBuffer();

        for (MeasurementSet measurementSet : measurementSets) {
            if (buffer.length() > 0) {
                buffer.append(", ");
            }
            buffer.append(measurementSet.toString());
        }

        return "{\"measurement_sets\":[" + buffer.toString() + "]}";
    }

    /**
     * Provides all CollectionJobs resolved by nrtCollectionTaskId from the Session
     *
     * @param httpSession
     * @param create
     * @return Map of nrtCollectionTaskId to CollectionJob
     */
    private Map<String, CollectionJob> getCollectionJobMap(HttpSession httpSession, boolean create) {
        if (create && httpSession.getAttribute("NrtCollectionTasks") == null) {
            httpSession.setAttribute("NrtCollectionTasks", new HashMap<String, CollectionJob>());
        }
        try {
            return (Map<String, CollectionJob>) httpSession.getAttribute("NrtCollectionTasks");
        } catch (Exception e) {
            logger.error("Session contains incompatible datastructure for NrtCollectionTasks attribute '{}'", e);
            return null;
        }
    }

    private List<CollectionJob> createCollectionJobs(OnmsResource reportResource, PrefabGraph prefabGraph, String nrtCollectionTaskId) {
        List<CollectionJob> collectionJobs = new ArrayList<CollectionJob>();

        OnmsResource nodeResource = reportResource.getParent();
        OnmsNode node = (OnmsNode) nodeResource.getEntity();
        Integer nodeId = node.getId();
        Date createTimestamp = new Date();

        //What protocols are involved?
        //For each protocol build a new CollectionJob
        Set<RrdGraphAttribute> relevantRrdGraphAttributes = getRequiredRrdGraphAttributes(reportResource, prefabGraph);
        Map<String, String> rrdGraphAttributesMetaData = getMetaDataForReport(relevantRrdGraphAttributes);

        Map<String, List<MetricTuple>> metricsByProtocol = getMetricIdsByProtocol(rrdGraphAttributesMetaData);

        //Destinations for MeasurementSets
        Set<String> resultDestinations = new HashSet<String>();
        resultDestinations.add(nrtCollectionTaskId);
        //resultDestinations.add("NrtPersistMe");

        for (String protocol : metricsByProtocol.keySet()) {
            CollectionJob collectionJob = new DefaultCollectionJob();
            collectionJob.setService(protocol);
            collectionJob.setNodeId(nodeId);
            collectionJob.setCreationTimestamp(createTimestamp);

            for (MetricTuple metricTuple : metricsByProtocol.get(protocol)) {
                collectionJob.addMetric(metricTuple.getMetricId(), resultDestinations, metricTuple.getOnmsLogicMetricId());
            }
            //I know....
            if (protocol.equals("SNMP") || protocol.equals("TCA")) {
                collectionJob.setNetInterface(protocol);
                SnmpAgentConfig snmpAgentConfig = m_snmpAgentConfigFactory.getAgentConfig(node.getPrimaryInterface().getIpAddress());
                collectionJob.setProtocolConfiguration(snmpAgentConfig.toProtocolConfigString());
                collectionJob.setNetInterface(node.getPrimaryInterface().getIpAddress().getHostAddress());
                collectionJobs.add(collectionJob);
            } else {
                logger.error("Protocol '{}' is not supported yet. CollectionJob will be ignorred.", protocol);
            }
        }

        return collectionJobs;
    }

    public Set<RrdGraphAttribute> getRequiredRrdGraphAttributes(OnmsResource reportResource, PrefabGraph prefabGraph) {
        Map<String, RrdGraphAttribute> available = reportResource.getRrdGraphAttributes();
        Set<RrdGraphAttribute> reqAttrs = new LinkedHashSet<RrdGraphAttribute>();
        for (String attrName : prefabGraph.getColumns()) {
            RrdGraphAttribute attr = available.get(attrName);
            if (attr != null) {
                reqAttrs.add(attr);
            }
        }
        return reqAttrs;
    }

    public Map<String,String> getRequiredExternalPropertyAttributes(final OnmsResource reportResource, final PrefabGraph prefabGraph) {
        final Map<String,String> attributes = reportResource.getExternalValueAttributes();
        if (attributes == null) return Collections.emptyMap();

        final Map<String,String> reqAttributes = new HashMap<String,String>();
        for (final String attrName : prefabGraph.getExternalValues()) {
            if (attributes.containsKey(attrName)) {
                reqAttributes.put(attrName, attributes.get(attrName));
            }
        }
        
        return reqAttributes;
    }

    public Map<String,String> getRequiredStringPropertyAttributes(final OnmsResource reportResource, final PrefabGraph prefabGraph) {
        final Map<String,String> attributes = reportResource.getStringPropertyAttributes();
        if (attributes == null) return Collections.emptyMap();

        final Map<String,String> reqAttributes = new HashMap<String,String>();
        for (final String attrName : prefabGraph.getPropertiesValues()) {
            if (attributes.containsKey(attrName)) {
                reqAttributes.put(attrName, attributes.get(attrName));
            }
        }
        
        return reqAttributes;
    }

    private Map<String, String> getRrdGraphAttributesToMetricIds(Map<String, String> onmsResourceNamesToMetaDataLines) {
        Map<String, String> rrdGraphAttributesToMetricIds = new HashMap<String, String>();
        for (String onmsResouceName : onmsResourceNamesToMetaDataLines.keySet()) {
            String rrdGraphAttributeName = onmsResouceName.toString().substring(onmsResouceName.lastIndexOf(".") +1);
            rrdGraphAttributesToMetricIds.put(rrdGraphAttributeName, getMetricIdFromMetaDataLine(onmsResourceNamesToMetaDataLines.get(onmsResouceName)));
        }
        return rrdGraphAttributesToMetricIds;
    }
    
    private Map<String, String> getMetaDataForReport(final Set<RrdGraphAttribute> rrdGraphAttributes) {
        Map<String, String> metaData = new HashMap<String, String>();

        logger.debug("getMetaDataForReport: " + rrdGraphAttributes);

        //get all metaData for RrdGraphAttributes from the meta files next to the RRD/JRobin files
        for (final RrdGraphAttribute attr : rrdGraphAttributes) {
            final String rrdRelativePath = attr.getRrdRelativePath();
            final String rrdName = rrdRelativePath.substring(0, rrdRelativePath.lastIndexOf('.'));

            final Set<Entry<String, String>> metaDataEntrySet = RrdUtils.readMetaDataFile(m_resourceDao.getRrdDirectory().getPath(), rrdName).entrySet();
            if (metaDataEntrySet == null) continue;

            final String attrName = attr.getName();
            final String attrString = attr.toString();

            for (final Map.Entry<String,String> entry : metaDataEntrySet) {
                final String line = entry.getKey() + '=' + entry.getValue();
                if (line.endsWith(attrName)) {
                    metaData.put(attrString, line);
                }
            };
        }
        
        return metaData;
    }

    private final String PROTOCOLDELIMITER = "_";
    private final String METRICID_DELIMITER = "=";
    
    private String getProtocolFromMetaDataLine(String metaDataLine) {
        String protocol = metaDataLine.substring(0, metaDataLine.indexOf(PROTOCOLDELIMITER));
        return protocol;
    }

    private String getMetricIdFromMetaDataLine(String metaDataLine) {
        String metricId = metaDataLine.substring(metaDataLine.indexOf(PROTOCOLDELIMITER) + 1, metaDataLine.lastIndexOf(METRICID_DELIMITER));
        return metricId;
    }

    /**
     * Provides a Map that provides Lists of MetricIds by protocols.
     *
     * @param rrdGraphAttributesMetaData String-key is the RrdGraphArrtibute the String-value is the MetaDataLine
     * @return a Map of Protocols as String-keys and a List of MetricTuples
     */
    protected Map<String, List<MetricTuple>> getMetricIdsByProtocol(Map<String, String> rrdGraphAttributesMetaData) {
        Map<String, List<MetricTuple>> metricIdsByProtocol = new HashMap<String, List<MetricTuple>>();

        //Protocol_metricId=RrdGraphAttribute
        //SNMP_.1.3.6.1.2.1.5.7.0=icmpInRedirects
        //TCA_.1.3.6.1.4.1.27091.3.1.6.1.2.171.19.37.60_inboundJitter=inboundJitter
        for (Map.Entry<String, String> entry : rrdGraphAttributesMetaData.entrySet()) {
            String protocol = getProtocolFromMetaDataLine(entry.getValue());
            String metricId = getMetricIdFromMetaDataLine(entry.getValue());

            if (!metricIdsByProtocol.containsKey(protocol)) {
                metricIdsByProtocol.put(protocol, new ArrayList<MetricTuple>());
            }
            metricIdsByProtocol.get(protocol).add(new MetricTuple(metricId, entry.getKey()));
        }
        return metricIdsByProtocol;
    }

    public void setGraphDao(GraphDao graphDao) {
        m_graphDao = graphDao;
    }

    public void setResourceDao(ResourceDao resourceDao) {
        m_resourceDao = resourceDao;
    }

    public void setSnmpAgentConfigFactory(SnmpAgentConfigFactory snmpAgentConfigFactory) {
        m_snmpAgentConfigFactory = snmpAgentConfigFactory;
    }

    public NrtBroker getNrtBroker() {
        return m_nrtBroker;
    }

    public void setNrtBroker(NrtBroker nrtBroker) {
        this.m_nrtBroker = nrtBroker;
    }
}

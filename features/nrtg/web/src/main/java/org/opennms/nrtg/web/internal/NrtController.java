/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2010-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2011 The OpenNMS Group, Inc.
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

package org.opennms.nrtg.web.internal;

import org.opennms.netmgt.config.SnmpAgentConfigFactory;
import org.opennms.netmgt.dao.GraphDao;
import org.opennms.netmgt.dao.ResourceDao;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsResource;
import org.opennms.netmgt.model.PrefabGraph;
import org.opennms.netmgt.model.RrdGraphAttribute;
import org.opennms.netmgt.snmp.SnmpAgentConfig;
import org.opennms.nrtg.api.model.CollectionJob;
import org.opennms.nrtg.api.model.DefaultCollectionJob;
import org.opennms.nrtg.api.model.MeasurementSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.support.converter.MessageConversionException;
import org.springframework.jms.support.converter.SimpleMessageConverter;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.ModelAndView;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.servlet.http.HttpSession;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

/**
 * @author Markus Neumann
 */
@Controller
@RequestMapping("/nrt/starter.htm")
public class NrtController {

    private static Logger logger = LoggerFactory.getLogger("OpenNMS.WEB." + NrtController.class);
    private JmsTemplate m_jmsTemplate;
    private GraphDao m_graphDao;
    private ResourceDao m_resourceDao;
    private SnmpAgentConfigFactory m_snmpAgentConfigFactory;

    private final SimpleMessageConverter simpleMessageConverter = new SimpleMessageConverter();

    HashMap<String, Vector<String>> m_messageStore = new HashMap<String, Vector<String>>();
    HashMap<String, Date> m_lastMessagePolled = new HashMap<String, Date>();

    @RequestMapping(method = RequestMethod.GET, params = {"nrtCollectionTaskId"})
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public void nrtCollectionJobTrigger(String nrtCollectionTaskId, HttpSession httpSession) {
        logger.debug("Republish CollectionJobTrigger for '{}'", nrtCollectionTaskId);

        Map<String, CollectionJob> nrtCollectionTasks = getCollectionJobMap(httpSession, false);

        if (nrtCollectionTasks != null) {
            CollectionJob collectionJob = nrtCollectionTasks.get(nrtCollectionTaskId);
            logger.debug("CollectionJob is '{}'", collectionJob);
            if (collectionJob != null) {
                publishCollectionJobViaJms(collectionJob);
                logger.debug("collectionJob was sent!");
            } else {
                logger.debug("collectionJob for collectionTask not found in session '{}'", nrtCollectionTaskId);
            }
        } else {
            logger.debug("No CollectionTasks map in session found.");
        }

        receiveMessagesForDestination(nrtCollectionTaskId);

        housekeeping();
    }

    @SuppressWarnings("unchecked")
    private Map<String, CollectionJob> getCollectionJobMap(HttpSession httpSession, boolean create) {
        if (create && httpSession.getAttribute("NrtCollectionTasks") == null) {
            httpSession.setAttribute("NrtCollectionTasks", new HashMap<String, CollectionJob>());
        }
        try {
            return (Map<String, CollectionJob>) httpSession.getAttribute("NrtCollectionTasks");
        } catch (Exception e) {
            logger.error("Session contains incompatible datastructure for NrtCollectionTasks attribute '{}'", e.getMessage());
            return null;
        }
    }

    @RequestMapping(method = RequestMethod.GET, params = {"resourceId", "report"})
    public ModelAndView nrtStart(String resourceId, String report, HttpSession httpSession) {

        logger.debug("JmsTemplate '{}'", m_jmsTemplate);

        assert (resourceId != null);
        logger.debug("resourceId: '{}'", resourceId);

        assert (report != null);
        logger.debug("report: '{}'", report);

        //Todo Tak there is you session to manage the CollectionSessions
        logger.debug(httpSession.toString());

        OnmsResource reportResource = m_resourceDao.getResourceById(resourceId);
        OnmsResource topResource = reportResource.getParent();
        OnmsNode node = (OnmsNode) topResource.getEntity();

        SnmpAgentConfig snmpAgentConfig = m_snmpAgentConfigFactory.getAgentConfig(node.getPrimaryInterface().getIpAddress());
        logger.debug("SnmpAgentConfig '{}' communityString '{}'", snmpAgentConfig, snmpAgentConfig.getReadCommunity());

        PrefabGraph prefabGraph = m_graphDao.getPrefabGraph(report);
        //TODO Tak graph service is able to check is a graph is propper for a given resource, check that later.
        lookUpMetricsForColumnsOfPrefabGraphs(prefabGraph, reportResource);

        String nrtCollectionTaskId = "NrtCollectionTaskId_" + System.currentTimeMillis();

        CollectionJob collectionJob = new DefaultCollectionJob();
        collectionJob.setService("SNMP");
        collectionJob.setProtocolConfiguration(snmpAgentConfig.toProtocolConfigString());

        collectionJob.setNodeId(node.getId());
        String netInterface = node.getPrimaryInterface().getIpAddress().getHostAddress();
        logger.debug("netInterface '{}'", netInterface);
        collectionJob.setNetInterface(netInterface);

        Set<String> resultDestinations = new HashSet<String>();
        resultDestinations.add(nrtCollectionTaskId);

        for (int i = 0; i < prefabGraph.getColumns().length; i++) {
            logger.debug("Adding Metric '{}' with MetricId '{}' to collectionJob", prefabGraph.getColumns()[i], prefabGraph.getMetricIds()[i]);
            collectionJob.addMetric(prefabGraph.getMetricIds()[i], resultDestinations);
        }
        logger.debug("CollectionJob '{}'", collectionJob.toString());
        this.publishCollectionJobViaJms(collectionJob);

        //Check if there is a NrtCollectionTasks map in the session already 
        getCollectionJobMap(httpSession, true).put(nrtCollectionTaskId, collectionJob);

        ModelAndView modelAndView = new ModelAndView("nrt/realtime");
        modelAndView.addObject("nrtCollectionTaskId", nrtCollectionTaskId);

        modelAndView.addObject("graphTitle", prefabGraph.getTitle());
        modelAndView.addObject("graphName", prefabGraph.getName());
        modelAndView.addObject("graphDescription", prefabGraph.getDescription());

        NrtRrdCommandFormatter rrdFormatter = new NrtRrdCommandFormatter(prefabGraph);
        modelAndView.addObject("rrdGraphString", rrdFormatter.getRrdGraphString());
        modelAndView.addObject("metricsMapping", rrdFormatter.getRrdMetricsMapping());

        return modelAndView;
    }

    /**
     * <p>getRequiredRrGraphdAttributes</p>
     *
     * @return a {@link java.util.Collection} object.
     */
    public Collection<RrdGraphAttribute> getRequiredRrdGraphAttributes(OnmsResource onmsResource, PrefabGraph prefabGraph) {
        Map<String, RrdGraphAttribute> available = onmsResource.getRrdGraphAttributes();
        Set<RrdGraphAttribute> reqAttrs = new LinkedHashSet<RrdGraphAttribute>();
        for (String attrName : prefabGraph.getColumns()) {
            RrdGraphAttribute attr = available.get(attrName);
            if (attr != null) {
                reqAttrs.add(attr);
            }
        }
        return reqAttrs;
    }

    /**
     * Adds the Metrics corresponding to the columns into the prefabGraph. Based on a meta file lookup. The files to check for
     * metric to column name mappings are provided by the filesToPromote. At the moment this method will check for the filenames
     * in the list and expects to file a file with this name and a .meta ending.
     */
    public void lookUpMetricsForColumnsOfPrefabGraphs(PrefabGraph prefabGraph, OnmsResource onmsResource) {
        //Build a Hashmap with all columns to metrics from the files
        Map<String, String> columnsToMetrics = new HashMap<String, String>();

        //get all metrics to columns mappings from the files in to hashmap
        for (RrdGraphAttribute attr : getRequiredRrdGraphAttributes(onmsResource, prefabGraph)) {
            String fileName = null;
            BufferedReader bf = null;
            try {
                fileName = m_resourceDao.getRrdDirectory() + File.separator + attr.getRrdRelativePath();

                //get meta files instead of rrd or jrb
                fileName = fileName.substring(0, fileName.lastIndexOf("."));
                fileName = fileName.concat(".meta");
                bf = new BufferedReader(new FileReader(fileName));

                String mappingLine = "";
                while (mappingLine != null) {
                    mappingLine = bf.readLine();
                    String metric = mappingLine.substring(0, mappingLine.lastIndexOf("="));
                    String column = mappingLine.substring(mappingLine.lastIndexOf("=") + 1);
                    columnsToMetrics.put(column, metric);
                }
            } catch (Exception ex) {
                logger.error("Problem by looking up metrics for cloumns in context of prefabgraphs from meta file '{}' '{}'", fileName, ex.getMessage());
            } finally {
                if (bf != null) {
                    try {
                        bf.close();
                    } catch (IOException ex) {
                        logger.warn("problem by reader close", ex);
                    }
                }
            }
        }
        /**
         * put the metrics from the columnsToMetrics map into the metrics array of the prefabGraph.
         */
        String[] metrics = new String[prefabGraph.getColumns().length];
        for (int i = 0; i < prefabGraph.getColumns().length; i++) {
            metrics[i] = columnsToMetrics.get(prefabGraph.getColumns()[i]);
        }
        prefabGraph.setMetricIds(metrics);
    }

    private void publishCollectionJobViaJms(CollectionJob collectionJob) {
        logger.debug("JmsTemplate '{}'", m_jmsTemplate);
        m_jmsTemplate.convertAndSend("NrtCollectMe", collectionJob);
//        logger.error("Jms publishing of CollectionJobs not implemented yet: '{}'", collectionJob);
    }

    public void setGraphDao(GraphDao graphDao) {
        m_graphDao = graphDao;
    }

    public void setResourceDao(ResourceDao resourceDao) {
        m_resourceDao = resourceDao;
    }

    public void setSnmpAgentConfigFactory(
            SnmpAgentConfigFactory snmpAgentConfigFactory) {
        m_snmpAgentConfigFactory = snmpAgentConfigFactory;
    }

    public void setJmsTemplate(JmsTemplate jmsTemplate) {
        m_jmsTemplate = jmsTemplate;
    }

    public String getMessagesForDestination(String destination) {
        StringBuffer buffer = new StringBuffer("");

        synchronized (m_messageStore) {
            if (m_messageStore.containsKey(destination)) {
                Vector<String> vector = m_messageStore.get(destination);

                for (String message : vector) {
                    if (buffer.length() > 0)
                        buffer.append(", ");

                    buffer.append(message);
                }

                m_messageStore.put(destination, new Vector<String>());

                return "[" + buffer.toString() + "]";
            } else {
                return "[]";
            }
        }
    }

    public void housekeeping() {
        Date now = new Date();
        for (String destination : m_messageStore.keySet()) {
            if (!m_lastMessagePolled.containsKey(destination)) {
                // this should not happen
                m_lastMessagePolled.put(destination, new Date());
            } else {
                Date lastMessage = m_lastMessagePolled.get(destination);
                long diff = now.getTime() - lastMessage.getTime();
                if (diff > 600000) {
                    synchronized (m_messageStore) {
                        m_messageStore.remove(destination);
                        m_lastMessagePolled.remove(destination);
                    }
                }
            }
        }
    }

    public void receiveMessagesForDestination(String destination) {
        synchronized (m_messageStore) {
            if (!m_messageStore.containsKey(destination)) {
                m_messageStore.put(destination, new Vector<String>());
                m_lastMessagePolled.put(destination, new Date());
            }
        }

        m_jmsTemplate.setReceiveTimeout(125);

        Message message = m_jmsTemplate.receive(destination);

        while (message != null) {
            addMessageForDestination(message, destination);
            message = m_jmsTemplate.receive(destination);
        }

        m_lastMessagePolled.put(destination, new Date());

        /*
        for (String destination : m_destinationSet) {
            Message message = m_jmsTemplate.receive(destination);

            while (message != null) {
                addMessageForDestination(message, destination);
                message = m_jmsTemplate.receive(destination);
            }
        }
        */
    }

    // @Override
    public void addMessageForDestination(Message message, String destination) {
        try {
            MeasurementSet measurementSet = (MeasurementSet) simpleMessageConverter.fromMessage(message);

            synchronized (m_messageStore) {
                if (!m_messageStore.containsKey(destination))
                    m_messageStore.put(destination, new Vector<String>());

                Vector<String> vector = m_messageStore.get(destination);

                vector.add(measurementSet.toString());
            }
        } catch (JMSException ex) {
            logger.error(ex.getMessage());
            // FIXME react, don't continue
        } catch (MessageConversionException ex) {
            logger.error(ex.getMessage());
            // FIXME react, don't continue
        }
    }
}

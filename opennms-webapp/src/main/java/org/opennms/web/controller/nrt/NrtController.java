package org.opennms.web.controller.nrt;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.Date;

import javax.servlet.http.HttpSession;
import javax.xml.ws.soap.Addressing;
import org.opennms.netmgt.config.SnmpAgentConfigFactory;

import org.opennms.netmgt.dao.*;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsResource;
import org.opennms.netmgt.model.PrefabGraph;
import org.opennms.netmgt.model.RrdGraphAttribute;
import org.opennms.netmgt.snmp.SnmpAgentConfig;
import org.opennms.nrtcollector.api.model.CollectionJob;
import org.opennms.nrtcollector.protocolcollector.snmp.SnmpCollectionJob;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

/**
 *
 * @author Markus Neumann
 */
@Controller
@RequestMapping("/nrt/starter.htm")
public class NrtController {

    private static Logger logger = LoggerFactory.getLogger("OpenNMS.WEB." + NrtController.class);

    @Autowired
    private JmsTemplate m_jmsTemplate;

    @Autowired
    private GraphDao m_graphDao;

    @Autowired
    private NodeDao m_nodeDao;

    @Autowired
    private ResourceDao m_resourceDao;
    
    @Autowired
    @Qualifier("snmpPeerFactory")
    private SnmpAgentConfigFactory m_snmpAgentConfigFactory;

    //ToDo Tak, böser draft!
    @RequestMapping(method = RequestMethod.GET, params = {"collectionTask"})
    public void nrtCollectionJobTrigger(String collectionTask, HttpSession httpSession) {
        logger.debug("Republish CollectionJobTrigger for '{}'", collectionTask);
        logger.debug("CollectionJob is '{}'", httpSession.getAttribute(collectionTask));

        Map<String, CollectionJob> nrtCollectoinTasks = null;
        try {
            nrtCollectoinTasks = (Map<String, CollectionJob>) httpSession.getAttribute("NrtCollectoinTasks");
        } catch (Exception e) {
            logger.error("Session contains incompatibl datastructure for NrtCollectoinTasks attribute '{}'", e.getMessage());
        }
        if (nrtCollectoinTasks != null) {
            CollectionJob collectionJob = nrtCollectoinTasks.get(collectionTask);
            if (collectionJob != null) {
                m_jmsTemplate.convertAndSend("NrtCollectMe", httpSession.getAttribute(collectionTask));
                logger.debug("collectionJob was send!");
            } else {
                logger.debug("collectionJob for collectionTask not found in session '{}'", collectionTask);
            }
        } else {
            logger.debug("No CollectionTasks map in session found.");
        }
    }

    @RequestMapping(method = RequestMethod.GET, params = {"resourceId", "report"})
    public ModelAndView nrtStart(String resourceId, String report, HttpSession httpSession) {
        
        logger.debug("JmsTemplate '{}'", m_jmsTemplate.toString());

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
        logger.debug("SnmpAgendConfig '{}' communityString '{}'", snmpAgentConfig, snmpAgentConfig.getReadCommunity());
        
        PrefabGraph prefabGraph = m_graphDao.getPrefabGraph(report);
        //TODO Tak graph service is able to check is a graph is propper for a given resource, check that later.
        lookUpMetricsForColumnsOfPrefabGraphs(prefabGraph, reportResource);

        String collectionTask = "CollectionTaksId_" + new Date();

        SnmpCollectionJob collectionJob = new SnmpCollectionJob();
        collectionJob.setSnmpAgentConfig(snmpAgentConfig);
        collectionJob.setService("SNMP");

        collectionJob.setNodeId(node.getId());
        String netInterface = node.getPrimaryInterface().getIpAddress().getHostAddress();
        logger.debug("netInterface '{}'", netInterface);
        collectionJob.setNetInterface(netInterface);

        Set<String> resultDestinations = new HashSet<String>();
        resultDestinations.add(collectionTask);

        for (int i = 0; i < prefabGraph.getColumns().length; i++) {
            logger.debug("Adding Metric '{}' with MetricId '{}' to collectionJob", prefabGraph.getColumns()[i], prefabGraph.getMetricIds()[i]);
            collectionJob.addMetric(prefabGraph.getMetricIds()[i], resultDestinations);
        }
        logger.debug("CollectionJob '{}'", collectionJob.toString());
        this.publishCollectionJobViaJms(collectionJob);

        //Check if there is a NrtCollectionTasks map in the session already 
        if (httpSession.getAttribute("NrtCollectoinTasks") == null) {
            httpSession.setAttribute("NrtCollectoinTasks", new HashMap<String, CollectionJob>());
        }
        ((Map<String, CollectionJob>) httpSession.getAttribute("NrtCollectoinTasks")).put(collectionTask, collectionJob);

        httpSession.setAttribute(collectionTask, collectionJob);

        ModelAndView modelAndView = new ModelAndView("nrt/realtime");
        modelAndView.addObject("collectionTask", collectionTask);
        modelAndView.addObject("graphCommand", prefabGraph.getCommand());
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
     * Adds the Metrics corresponding to the columns into the prefabGraph. Based
     * on a meta file lookup. The files to check for metric to column name
     * mappings are provided by the filesToPromote. At the moment this method
     * will check for the filenames in the list and expects to file a file with
     * this name and a .meta ending.
     */
    public void lookUpMetricsForColumnsOfPrefabGraphs(PrefabGraph prefabGraph, OnmsResource onmsResource) {
        //Build a Hashmap with all columns to metrics from the files
        Map<String, String> columnsToMetrics = new HashMap<String, String>();

        //get all metrics to columns mappings from the files in to hashmap
        for (RrdGraphAttribute attr : getRequiredRrdGraphAttributes(onmsResource, prefabGraph)) {
            String fileName = null;

            try {
                fileName = m_resourceDao.getRrdDirectory() + File.separator + attr.getRrdRelativePath();

                //get meta files instead of rrd or jrb
                fileName = fileName.substring(0, fileName.lastIndexOf("."));
                fileName = fileName.concat(".meta");
                BufferedReader bf = new BufferedReader(new FileReader(fileName));

                String mappingLine = "";
                while (mappingLine != null) {
                    mappingLine = bf.readLine();
                    String metric = mappingLine.substring(0, mappingLine.lastIndexOf("="));
                    String column = mappingLine.substring(mappingLine.lastIndexOf("=") + 1);
                    columnsToMetrics.put(column, metric);
                }
            } catch (Exception ex) {
                logger.error("Problem by looking up metrics for cloumns in context of prefabgraphs from meta file '{}' '{}'", fileName, ex.getMessage());
            }
        }
        /**
         * put the metrics from the columnsToMetrics map into the metrics array
         * of the prefabGraph.
         */
        String[] metrics = new String[prefabGraph.getColumns().length];
        for (int i = 0; i < prefabGraph.getColumns().length; i++) {
            metrics[i] = columnsToMetrics.get(prefabGraph.getColumns()[i]);
        }
        prefabGraph.setMetricIds(metrics);
    }

    private void publishCollectionJobViaJms(CollectionJob collectionJob) {
        logger.debug("JmsTemplate '{}'", m_jmsTemplate.toString());
        m_jmsTemplate.convertAndSend("NrtCollectMe", collectionJob);
//        logger.error("Jms publishing of CollectionJobs not implemented yet: '{}'", collectionJob);
    }
}

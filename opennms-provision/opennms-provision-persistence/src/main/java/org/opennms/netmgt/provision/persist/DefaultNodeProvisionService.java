package org.opennms.netmgt.provision.persist;

import java.util.Set;
import java.util.TreeSet;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Category;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.EventConstants;
import org.opennms.netmgt.config.SnmpEventInfo;
import org.opennms.netmgt.config.SnmpPeerFactory;
import org.opennms.netmgt.dao.CategoryDao;
import org.opennms.netmgt.dao.DistPollerDao;
import org.opennms.netmgt.dao.NodeDao;
import org.opennms.netmgt.dao.ServiceTypeDao;
import org.opennms.netmgt.model.OnmsAssetRecord;
import org.opennms.netmgt.model.OnmsDistPoller;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsMonitoredService;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsServiceType;
import org.opennms.netmgt.model.OnmsIpInterface.PrimaryType;
import org.opennms.netmgt.model.events.EventProxy;
import org.opennms.netmgt.model.events.EventProxyException;
import org.opennms.netmgt.provision.persist.requisition.Requisition;
import org.opennms.netmgt.provision.persist.requisition.RequisitionAsset;
import org.opennms.netmgt.provision.persist.requisition.RequisitionCategory;
import org.opennms.netmgt.provision.persist.requisition.RequisitionInterface;
import org.opennms.netmgt.provision.persist.requisition.RequisitionMonitoredService;
import org.opennms.netmgt.provision.persist.requisition.RequisitionNode;
import org.opennms.netmgt.xml.event.Event;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.web.servlet.ModelAndView;

public class DefaultNodeProvisionService implements NodeProvisionService {

    private EventProxy m_eventProxy;
    private CategoryDao m_categoryDao;
    private NodeDao m_nodeDao;
    
    @Autowired
    private DistPollerDao m_distPollerDao;
    
    private ServiceTypeDao m_serviceTypeDao;
    private ForeignSourceRepository m_foreignSourceRepository;
    private SnmpPeerFactory m_snmpPeerFactory;

    public ModelAndView getModelAndView(HttpServletRequest request) {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.addObject("foreignSources", m_foreignSourceRepository.getForeignSources());
        modelAndView.addObject("requisitions", m_foreignSourceRepository.getRequisitions());
        modelAndView.addObject("categories", m_categoryDao.getAllCategoryNames());
        modelAndView.addObject("success", Boolean.parseBoolean(request.getParameter("success")));
        modelAndView.addObject("foreignSource", request.getParameter("foreignSource"));
        return modelAndView;
    }
    
    @Transactional
    public boolean provisionNode(String foreignSource, String foreignId, String nodeLabel, String ipAddress,
            String[] categories, String snmpCommunity, String snmpVersion,
            String deviceUsername, String devicePassword, String enablePassword,
            String accessMethod, String autoEnable) throws NodeProvisionException {

        if (log().isDebugEnabled()) {
            log().debug(String.format("adding SNMP community %s (%s)", snmpCommunity, snmpVersion));
        }
        // Set the SNMP community name (if necessary)
        if (snmpCommunity != null && snmpVersion != null) {
            try {
                SnmpEventInfo info = new SnmpEventInfo();
                info.setCommunityString(snmpCommunity);
                info.setFirstIPAddress(ipAddress);
                info.setVersion(snmpVersion);
                m_snmpPeerFactory.define(info);
                SnmpPeerFactory.saveCurrent();
            } catch (Exception e) {
                throw new NodeProvisionException("unable to add SNMP community information", e);
            }
        }

        log().debug("creating requisition node");
        // Create a requisition node based on the form input
        RequisitionInterface reqIface = new RequisitionInterface();
        reqIface.setIpAddr(ipAddress);
        reqIface.setManaged(true);
        reqIface.setSnmpPrimary("P");
        reqIface.setStatus(1);

        reqIface.putMonitoredService(new RequisitionMonitoredService("ICMP"));
        reqIface.putMonitoredService(new RequisitionMonitoredService("SNMP"));
        
        RequisitionNode reqNode = new RequisitionNode();
        reqNode.setNodeLabel(nodeLabel);
        reqNode.setForeignId(foreignId);
        reqNode.putInterface(reqIface);

        for (String category : categories) {
            reqNode.putCategory(new RequisitionCategory(category));
        }

        if (deviceUsername != null) {
            reqNode.putAsset(new RequisitionAsset("username", deviceUsername));
        }
        if (devicePassword != null) {
            reqNode.putAsset(new RequisitionAsset("password", devicePassword));
        }
        if (enablePassword != null) {
            reqNode.putAsset(new RequisitionAsset("enable", enablePassword));
        }
        if (accessMethod != null) {
            reqNode.putAsset(new RequisitionAsset("connection", accessMethod));
        }
        if (autoEnable != null) {
            reqNode.putAsset(new RequisitionAsset("autoenable", autoEnable));
        }

        // Now save it to the requisition
        try {
            Requisition req = m_foreignSourceRepository.getRequisition(foreignSource);
            req.putNode(reqNode);
            log().debug("saving requisition node");
            m_foreignSourceRepository.save(req);
        } catch (ForeignSourceRepositoryException e) {
            throw new RuntimeException("unable to retrieve foreign source '" + foreignSource + "'", e);
        }
        
        log().debug("creating database node");
        // Create the basic node
        
        OnmsDistPoller dp = m_distPollerDao.get("localhost");
        OnmsNode node = new OnmsNode(dp);
        node.setType("A");
        node.setForeignSource(foreignSource);
        node.setForeignId(foreignId);
        node.setLabel(nodeLabel);
        
        node.getAssetRecord().setAutoenable(autoEnable);
        node.getAssetRecord().setConnection(accessMethod);
        node.getAssetRecord().setEnable(enablePassword);
        node.getAssetRecord().setUsername(deviceUsername);
        node.getAssetRecord().setPassword(devicePassword);
        
        OnmsIpInterface iface = new OnmsIpInterface(ipAddress, node);
        iface.setIsManaged("M");
        iface.setIsSnmpPrimary(new PrimaryType('P'));

        // these are automatically added to the interface by the constructor
        new OnmsMonitoredService(iface, getServiceType("ICMP"));
        new OnmsMonitoredService(iface, getServiceType("SNMP"));
        
        log().debug("saving database node");
        m_nodeDao.save(node);
        
        OnmsNode savedNode = m_nodeDao.findByForeignId(foreignSource, foreignId);
        
        Assert.notNull(savedNode, "Failed to save node to database");
        
        try {
            log().debug("sending event for new node ID " + savedNode.getId());
            Event e = new Event();
            e.setUei(EventConstants.NODE_ADDED_EVENT_UEI);
            e.setNodeid(savedNode.getId());
            e.setSource(getClass().getName());
            e.setTime(EventConstants.formatToString(new java.util.Date()));
            m_eventProxy.send(e);
            
            e = new Event();
            e.setUei(EventConstants.NODE_GAINED_INTERFACE_EVENT_UEI);
            e.setNodeid(savedNode.getId());
            e.setInterface(ipAddress);
            e.setSource(getClass().getName());
            e.setTime(EventConstants.formatToString(new java.util.Date()));
            m_eventProxy.send(e);

            e = new Event();
            e.setUei(EventConstants.NODE_GAINED_SERVICE_EVENT_UEI);
            e.setNodeid(savedNode.getId());
            e.setInterface(ipAddress);
            e.setService("ICMP");
            e.setService("SNMP");
            e.setSource(getClass().getName());
            e.setTime(EventConstants.formatToString(new java.util.Date()));
            m_eventProxy.send(e);
        } catch (EventProxyException ex) {
            throw new NodeProvisionException("Unable to send node events", ex);
        }

        return true;
    }
    
    private OnmsServiceType getServiceType(String string) {
        return m_serviceTypeDao.findByName(string);
    }

    public void setForeignSourceRepository(ForeignSourceRepository repository) {
        m_foreignSourceRepository = repository;
    }

    public void setCategoryDao(CategoryDao dao) {
        m_categoryDao = dao;
    }

    public void setSnmpPeerFactory(SnmpPeerFactory pf) {
        m_snmpPeerFactory = pf;
    }

    public void setNodeDao(NodeDao dao) {
        m_nodeDao = dao;
    }

    public void setServiceTypeDao(ServiceTypeDao dao) {
        m_serviceTypeDao = dao;
    }
    
    public void setEventProxy(EventProxy proxy) {
        m_eventProxy = proxy;
    }

    protected Category log() {
        return ThreadCategory.getInstance(getClass());
    }
}

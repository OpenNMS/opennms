package org.opennms.web.rest;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.core.Response.Status;

import org.hibernate.criterion.Restrictions;
import org.opennms.netmgt.EventConstants;
import org.opennms.netmgt.dao.NodeDao;
import org.opennms.netmgt.dao.SnmpInterfaceDao;
import org.opennms.netmgt.model.OnmsCriteria;
import org.opennms.netmgt.model.OnmsEntity;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsSnmpInterface;
import org.opennms.netmgt.model.OnmsSnmpInterfaceList;
import org.opennms.netmgt.model.events.EventBuilder;
import org.opennms.netmgt.model.events.EventProxy;
import org.opennms.netmgt.model.events.EventProxyException;
import org.opennms.netmgt.xml.event.Event;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.sun.jersey.spi.resource.PerRequest;

@Component
@PerRequest
@Scope("prototype")
@Transactional
public class OnmsSnmpInterfaceResource extends OnmsRestService {

    @Autowired
    private NodeDao m_nodeDao;
    
    @Autowired
    private SnmpInterfaceDao m_snmpInterfaceDao;
    
    @Autowired
    private EventProxy m_eventProxy;
    
    @Context 
    UriInfo m_uriInfo;
    
    @GET
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public OnmsSnmpInterfaceList getSnmpInterfaces(@PathParam("nodeCriteria") String nodeCriteria) {
        OnmsNode node = m_nodeDao.get(nodeCriteria);
        
        MultivaluedMap<String,String> params = m_uriInfo.getQueryParameters();
        OnmsCriteria criteria = new OnmsCriteria(OnmsSnmpInterface.class);
        setLimitOffset(params, criteria, 20, true);
        addOrdering(params, criteria, true);
        
        addFiltersToCriteria(params, criteria, OnmsSnmpInterface.class);
        
        criteria.createCriteria("node").add(Restrictions.eq("id", node.getId()));
        OnmsSnmpInterfaceList snmpList = new OnmsSnmpInterfaceList(m_snmpInterfaceDao.findMatching(criteria));
        
        OnmsCriteria crit = new OnmsCriteria(OnmsSnmpInterface.class);
        crit.createCriteria("node").add(Restrictions.eq("id", node.getId()));
        addFiltersToCriteria(params, crit, OnmsSnmpInterface.class);
        snmpList.setTotalCount(m_snmpInterfaceDao.countMatching(crit));
        return snmpList;
    }

    @GET
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Path("{ifIndex}")
    public OnmsEntity getSnmpInterface(@PathParam("nodeCriteria") String nodeCriteria, @PathParam("ifIndex") int ifIndex) {
        OnmsNode node = m_nodeDao.get(nodeCriteria);
        return node.getSnmpInterfaceWithIfIndex(ifIndex);
    }
    
    @POST
    @Consumes(MediaType.APPLICATION_XML)
    public Response addSnmpInterface(@PathParam("nodeCriteria") String nodeCriteria, OnmsSnmpInterface snmpInterface) {
        OnmsNode node = m_nodeDao.get(nodeCriteria);
        if (node == null) {
            throwException(Status.BAD_REQUEST, "addSnmpInterface: can't find node " + nodeCriteria);
        }
        if (snmpInterface == null) {
            throwException(Status.BAD_REQUEST, "addSnmpInterface: snmp interface object cannot be null");
        }
        log().debug("addSnmpInterface: adding interface " + snmpInterface);
        node.addSnmpInterface(snmpInterface);
        if (snmpInterface.getIpAddress() != null) {
            OnmsIpInterface iface = node.getIpInterfaceByIpAddress(snmpInterface.getIpAddress());
            iface.setSnmpInterface(snmpInterface);
            // TODO Add important events here
        }
        m_snmpInterfaceDao.save(snmpInterface);
        return Response.ok().build();
    }
    
    @DELETE
    @Path("{ifIndex}")
    public Response deleteSnmpInterface(@PathParam("nodeCriteria") String nodeCriteria, @PathParam("ifIndex") int ifIndex) {
        OnmsNode node = m_nodeDao.get(nodeCriteria);
        if (node == null) {
            throwException(Status.BAD_REQUEST, "deleteSnmpInterface: can't find node " + nodeCriteria);
        }
        OnmsEntity snmpInterface = node.getSnmpInterfaceWithIfIndex(ifIndex);
        if (snmpInterface == null) {
            throwException(Status.BAD_REQUEST, "deleteSnmpInterface: can't find snmp interface with ifIndex " + ifIndex + " for node " + nodeCriteria);
        }
        log().debug("deletSnmpInterface: deleting interface with ifIndex " + ifIndex + " from node " + nodeCriteria);
        node.getSnmpInterfaces().remove(snmpInterface);
        m_nodeDao.saveOrUpdate(node);
        // TODO Add important events here
        return Response.ok().build();
    }
    
    @PUT
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Path("{ifIndex}")
    public Response updateSnmpInterface(@PathParam("nodeCriteria") String nodeCriteria, @PathParam("ifIndex") int ifIndex, MultivaluedMapImpl params) {
        
        OnmsNode node = m_nodeDao.get(nodeCriteria);
        if (node == null) {
            throwException(Status.BAD_REQUEST, "deleteSnmpInterface: can't find node " + nodeCriteria);
        }
        OnmsSnmpInterface snmpInterface = node.getSnmpInterfaceWithIfIndex(ifIndex);
        if (snmpInterface == null) {
            throwException(Status.BAD_REQUEST, "deleteSnmpInterface: can't find snmp interface with ifIndex " + ifIndex + " for node " + nodeCriteria);
        }
        log().debug("updateSnmpInterface: updating snmp interface " + snmpInterface);
        BeanWrapper wrapper = new BeanWrapperImpl(snmpInterface);
        for(String key : params.keySet()) {
            if (wrapper.isWritableProperty(key)) {
                String stringValue = params.getFirst(key);
                Object value = wrapper.convertIfNecessary(stringValue, wrapper.getPropertyType(key));
                wrapper.setPropertyValue(key, value);
            }
        }
        
        
        Event e = null;
        if (params.containsKey("collect")) {
            // we've updated the collection flag so we need to send an event to redo collection
            EventBuilder bldr = new EventBuilder(EventConstants.REINITIALIZE_PRIMARY_SNMP_INTERFACE_EVENT_UEI, "OpenNMS.Webapp");
            bldr.setNode(node);
            bldr.setInterface(node.getPrimaryInterface().getIpAddress());
            e = bldr.getEvent();
        }
        log().debug("updateSnmpInterface: snmp interface " + snmpInterface + " updated");
        m_snmpInterfaceDao.saveOrUpdate(snmpInterface);
        
        if (e != null) {
            try {
                m_eventProxy.send(e);
            } catch (EventProxyException ex) {
                return throwException(Response.Status.INTERNAL_SERVER_ERROR, "Exception occurred sending event: "+ex.getMessage());
            }
        }
        return Response.ok().build();
    }

}

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

import org.opennms.netmgt.EventConstants;
import org.opennms.netmgt.dao.NodeDao;
import org.opennms.netmgt.model.OnmsCriteria;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsNodeList;
import org.opennms.netmgt.utils.EventProxy;
import org.opennms.netmgt.utils.EventProxyException;
import org.opennms.netmgt.xml.event.Event;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.sun.jersey.api.core.ResourceContext;
import com.sun.jersey.spi.resource.PerRequest;

@Component
@PerRequest
@Scope("prototype")
@Path("nodes")
@Transactional
public class NodeRestService extends OnmsRestService {
    
    private static final int LIMIT=10;
	
    @Autowired
    private NodeDao m_nodeDao;
    
    @Autowired
    private EventProxy m_eventProxy;
    
    @Context 
    UriInfo m_uriInfo;
    
    @Context
    ResourceContext m_context;

    @GET
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public OnmsNodeList getNodes() {
        OnmsCriteria criteria = getQueryFilters();
        return new OnmsNodeList(m_nodeDao.findMatching(criteria));
    }

    @GET
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Path("{nodeId}")
    public OnmsNode getNode(@PathParam("nodeId") int nodeId) {
        return m_nodeDao.get(nodeId);
    }

    @POST
    @Consumes(MediaType.APPLICATION_XML)
    public Response addNode(OnmsNode node) {
        log().debug("addNode: Adding node " + node);
        m_nodeDao.save(node);
        try {
            sendEvent(EventConstants.NODE_ADDED_EVENT_UEI, node.getId());
        } catch (EventProxyException ex) {
            throwException(Status.BAD_REQUEST, ex.getMessage());
        }
        return Response.ok(node).build();
    }
    
    @PUT
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Path("{nodeId}")
    public Response updateNode(@PathParam("nodeId") int nodeId, MultivaluedMapImpl params) {
        OnmsNode node = m_nodeDao.get(nodeId);
        if (node == null)
            throwException(Status.BAD_REQUEST, "updateNode: Can't find node with id " + nodeId);
        log().debug("updateNode: updating node " + node);
        BeanWrapper wrapper = new BeanWrapperImpl(node);
        for(String key : params.keySet()) {
            if (wrapper.isWritableProperty(key)) {
                String stringValue = params.getFirst(key);
                Object value = wrapper.convertIfNecessary(stringValue, wrapper.getPropertyType(key));
                wrapper.setPropertyValue(key, value);
            }
        }
        log().debug("updateNode: node " + node + " updated");
        m_nodeDao.saveOrUpdate(node);
        return Response.ok(node).build();
    }
    
    @DELETE
    @Path("{nodeId}")
    public Response deleteNode(@PathParam("nodeId") int nodeId) {
        OnmsNode node = m_nodeDao.get(nodeId);
        if (node == null)
            throwException(Status.BAD_REQUEST, "deleteNode: Can't find node with id " + nodeId);
        log().debug("deleteNode: deleting node " + nodeId);
        m_nodeDao.delete(node);
        try {
            sendEvent(EventConstants.NODE_DELETED_EVENT_UEI, nodeId);
        } catch (EventProxyException ex) {
            throwException(Status.BAD_REQUEST, ex.getMessage());
        }
        return Response.ok().build();
    }

    @Path("{nodeId}/ipinterfaces")
    public OnmsIpInterfaceResource getIpInterfaceResource() {
        return m_context.getResource(OnmsIpInterfaceResource.class);
    }

    @Path("{nodeId}/snmpinterfaces")
    public OnmsSnmpInterfaceResource getSnmpInterfaceResource() {
        return m_context.getResource(OnmsSnmpInterfaceResource.class);
    }

    @Path("{nodeId}/categories")
    public OnmsCategoryResource getCategoryResource() {
        return m_context.getResource(OnmsCategoryResource.class);
    }

    private OnmsCriteria getQueryFilters() {
        MultivaluedMap<String,String> params = m_uriInfo.getQueryParameters();
        OnmsCriteria criteria = new OnmsCriteria(OnmsNode.class);

    	setLimitOffset(params, criteria, LIMIT);
    	addFiltersToCriteria(params, criteria, OnmsNode.class);
        
        return criteria;
    }
    
    private void sendEvent(String uei, int nodeId) throws EventProxyException {
        Event e = new Event();
        e.setUei(EventConstants.NODE_DELETED_EVENT_UEI);
        e.setNodeid(nodeId);
        e.setSource(getClass().getName());
        e.setTime(EventConstants.formatToString(new java.util.Date()));
        m_eventProxy.send(e);
    }
}

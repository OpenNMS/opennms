package org.opennms.web.rest;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.opennms.netmgt.EventConstants;
import org.opennms.netmgt.dao.NodeDao;
import org.opennms.netmgt.model.OnmsAssetRecord;
import org.opennms.netmgt.model.OnmsNode;
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
/**
 * <p>AssetRecordResource class.</p>
 *
 * @author ranger
 * @version $Id: $
 * @since 1.8.1
 */
@PerRequest
@Scope("prototype")
@Path("assetRecord")
@Transactional
public class AssetRecordResource extends OnmsRestService {
    
    @Autowired
    private NodeDao m_nodeDao;    
    
    @Autowired
    private EventProxy m_eventProxy;
    
    /**
     * <p>getAssetRecord</p>
     *
     * @param nodeCriteria a {@link java.lang.String} object.
     * @return a {@link org.opennms.netmgt.model.OnmsAssetRecord} object.
     */
    @GET
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public OnmsAssetRecord getAssetRecord(@PathParam("nodeCriteria") String nodeCriteria) {
        OnmsNode node = m_nodeDao.get(nodeCriteria);
        if (node == null) {
            throwException(Status.BAD_REQUEST, "getCategories: Can't find node " + nodeCriteria);
        }
        return getAssetRecord(node);
    }
    
    /**
     * <p>updateAssetRecord</p>
     *
     * @param nodeCriteria a {@link java.lang.String} object.
     * @param params a {@link org.opennms.web.rest.MultivaluedMapImpl} object.
     * @return a {@link javax.ws.rs.core.Response} object.
     */
    @PUT
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response updateAssetRecord(@PathParam("nodeCriteria") String nodeCriteria,  MultivaluedMapImpl params) {
        OnmsNode node = m_nodeDao.get(nodeCriteria);
        if (node == null) {
            throwException(Status.BAD_REQUEST, "updateAssetRecord: Can't find node " + nodeCriteria);
        }
        
        OnmsAssetRecord assetRecord = getAssetRecord(node);
        if (assetRecord == null) {
            throwException(Status.BAD_REQUEST, "updateAssetRecord: Node " + node  + " could not update ");
        }
        log().debug("updateAssetRecord: updating category " + assetRecord);
        BeanWrapper wrapper = new BeanWrapperImpl(assetRecord);
        for(String key : params.keySet()) {
            if (wrapper.isWritableProperty(key)) {
                String stringValue = params.getFirst(key);
                Object value = wrapper.convertIfNecessary(stringValue, wrapper.getPropertyType(key));
                wrapper.setPropertyValue(key, value);
            }
        }
   
        log().debug("updateAssetRecord: assetRecord " + assetRecord + " updated");
        m_nodeDao.saveOrUpdate(node);
        
        try {
            sendEvent(EventConstants.ASSET_INFO_CHANGED_EVENT_UEI, node.getId());
        } catch (EventProxyException e) {
            throwException(Status.BAD_REQUEST, e.getMessage());
        }
        
        return Response.ok().build();
    
    }

    private OnmsAssetRecord getAssetRecord(OnmsNode node) {
        return node.getAssetRecord();
    }
    
    
    private void sendEvent(String uei, int nodeId) throws EventProxyException {
        Event e = new Event();
        e.setUei(uei);
        e.setNodeid(nodeId);
        e.setSource(getClass().getName());
        e.setTime(EventConstants.formatToString(new java.util.Date()));
        m_eventProxy.send(e);
    }

}

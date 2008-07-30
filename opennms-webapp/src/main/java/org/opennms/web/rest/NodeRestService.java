package org.opennms.web.rest;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

import org.opennms.netmgt.dao.NodeDao;
import org.opennms.netmgt.model.OnmsNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.sun.jersey.spi.resource.PerRequest;

@Component
@PerRequest
@Scope("prototype")
@Path("node/")
public class NodeRestService {
    
    @Autowired
    private NodeDao m_nodeDao;
    
    @GET
    @Produces("text/xml")
    @Path("{nodeId}")
    @Transactional
    public OnmsNode getNode(@PathParam("nodeId") String nodeId) {
        return m_nodeDao.get(new Integer(nodeId));
    }

}

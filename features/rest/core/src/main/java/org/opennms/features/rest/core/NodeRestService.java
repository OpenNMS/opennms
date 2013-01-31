package org.opennms.features.rest.core;

import java.util.List;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.opennms.netmgt.dao.NodeDao;
import org.opennms.netmgt.model.OnmsNode;

@Path("/nodes")
@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
public class NodeRestService {

    private NodeDao nodeDao;

    @GET
    public List<OnmsNode> getNodes() {
        return nodeDao.findAll();
    }

    @GET
    @Path("{nodeId}")
    public OnmsNode sayItAgain(@PathParam("nodeId") final String nodeId) {
        return nodeDao.get(nodeId);
    }

    public void setNodeDao(NodeDao nodeDao) {
        this.nodeDao = nodeDao;
    }
}

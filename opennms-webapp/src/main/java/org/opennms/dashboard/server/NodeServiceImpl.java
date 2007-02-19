package org.opennms.dashboard.server;

import org.opennms.dashboard.client.NodeService;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

public class NodeServiceImpl extends RemoteServiceServlet implements
        NodeService {

    public String[] getNodeNames() {
        return new String[] {
                "node1",
                "node2",
                "node3"
        };
    }

}

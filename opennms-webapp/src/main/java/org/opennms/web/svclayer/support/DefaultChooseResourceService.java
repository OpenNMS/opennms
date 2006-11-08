package org.opennms.web.svclayer.support;

import org.opennms.netmgt.dao.NodeDao;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.web.Util;
import org.opennms.web.performance.PerformanceModel;
import org.opennms.web.svclayer.ChooseResourceService;

public class DefaultChooseResourceService implements ChooseResourceService {

    public PerformanceModel m_performanceModel;
    public NodeDao m_nodeDao;

    public ChooseResourceModel findChildResources(String resourceType,
            String resource, String endUrl) {
        assertProperitesSet();
        
        if (resourceType == null) {
            throw new IllegalArgumentException("resourceType parameter may not be null");
        }

        if (resource == null) {
            throw new IllegalArgumentException("resource parameter may not be null");
        }
        
        if (endUrl == null) {
            throw new IllegalArgumentException("endUrl parameter may not be null");
        }

        ChooseResourceModel model = new ChooseResourceModel();
        model.setEndUrl(endUrl);

        if ("node".equals(resourceType)) {
            int nodeId;
            try {
                nodeId = Integer.parseInt(resource);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("resource parameter '"
                                                   + resource + "' for "
                                                   + "resource type of node "
                                                   + "could not be parsed as "
                                                   + "an integer");
            }
            OnmsNode n = m_nodeDao.get(nodeId);
            if (n == null) {
                throw new IllegalArgumentException("could find node with a "
                                                   + "nodeId of " + nodeId);
            }
            model.setResourceTypes(m_performanceModel.getResourceForNode(nodeId));
            model.setResourceTypeName("node");
            model.setResourceTypeLabel("Node");
            model.setResourceLabel(n.getLabel());
            model.setResourceLink("element/node.jsp?node=" + nodeId);
        } else if ("domain".equals(resourceType)) {
            model.setResourceTypes(m_performanceModel.getResourceForDomain(resource));
            model.setResourceTypeName("domain");
            model.setResourceTypeLabel("Domain");
            model.setResourceLabel(resource);
            model.setResourceLink("performance/chooseresource.jsp?domain=" + Util.encode(resource) + "&endUrl=performance%2Fchoosereportanddate.jsp");
        } else {
            throw new IllegalArgumentException("resourceType of '"
                                               + resourceType + "' is not "
                                               + "supported. Must be one of: "
                                               + "node, domain");
        }

        return model;
    }

    private void assertProperitesSet() {
        if (m_performanceModel == null) {
            throw new IllegalStateException("performanceModel property not set");
        }
        
        if (m_nodeDao == null) {
            throw new IllegalStateException("nodeDao property not set");
        }
    }

    public NodeDao getNodeDao() {
        return m_nodeDao;
    }

    public void setNodeDao(NodeDao nodeDao) {
        m_nodeDao = nodeDao;
    }

    public PerformanceModel getPerformanceModel() {
        return m_performanceModel;
    }

    public void setPerformanceModel(PerformanceModel performanceModel) {
        m_performanceModel = performanceModel;
    }

}

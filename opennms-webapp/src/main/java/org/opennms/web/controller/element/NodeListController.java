package org.opennms.web.controller.element;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.opennms.netmgt.dao.CategoryDao;
import org.opennms.netmgt.dao.NodeDao;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.web.command.NodeListCommand;
import org.opennms.web.element.Interface;
import org.opennms.web.element.NetworkElementFactory;
import org.opennms.web.element.Node;
import org.opennms.web.outage.OutageModel;
import org.opennms.web.svclayer.SiteStatusViewService;
import org.opennms.web.svclayer.support.NodeListModel;
import org.opennms.web.svclayer.support.NodeListModel.NodeModel;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractCommandController;

/**
 * FIXME: This class currently uses non-Hibernate queries and it calls DAOs directly.
 * 
 * @author <a href="dj@opennms.org">DJ Gregor</a>
 *
 */
public class NodeListController extends AbstractCommandController implements InitializingBean {

    private OutageModel m_outageModel;
    private NodeDao m_nodeDao;
    private CategoryDao m_categoryDao;
    private TransactionTemplate m_transactionTemplate;
    private SiteStatusViewService m_siteStatusViewService;
    private String m_successView;
    
    @Override
    protected ModelAndView handle(HttpServletRequest request, HttpServletResponse response, Object cmd, BindException errors) throws Exception {
        NodeListCommand command = (NodeListCommand) cmd;

        Node[] nodes = null;
        if (command.hasNodename()) {
            nodes = NetworkElementFactory.getNodesLike(command.getNodename());
        } else if (command.hasIplike()) {
            nodes = NetworkElementFactory.getNodesWithIpLike(command.getIplike());
        } else if (command.hasService()) {
            nodes = NetworkElementFactory.getNodesWithService(command.getService());
        } else if (command.hasMaclike()) {
            nodes = NetworkElementFactory.getNodesWithPhysAddr(command.getMaclike());
        } else if (command.hasIfAlias()) {
            nodes = NetworkElementFactory.getNodesWithIfAlias(command.getIfAlias());
        } else if (command.hasCategory1() && command.hasCategory2()) {
            nodes = NetworkElementFactory.getNodesWithCategories(m_transactionTemplate, m_nodeDao, m_categoryDao, command.getCategory1(), command.getCategory2(), command.getNodesWithDownAggregateStatus());
        } else if (command.hasCategory1()) {
            nodes = NetworkElementFactory.getNodesWithCategories(m_transactionTemplate, m_nodeDao, m_categoryDao, command.getCategory1(), command.getCategory2(), command.getNodesWithDownAggregateStatus());
        } else if (command.hasStatusViewName() && command.hasStatusSite() && command.hasStatusRowLabel()) {
            Collection<OnmsNode> onmsNodes;
            if (command.getNodesWithDownAggregateStatus()) {
                onmsNodes = m_siteStatusViewService.getAggregateStatus(command.getStatusViewName(), command.getStatusSite(), command.getStatusRowLabel()).getDownNodes();
            } else {
                onmsNodes = m_siteStatusViewService.getNodes(command.getStatusViewName(), command.getStatusSite(), command.getStatusRowLabel());
            }
            nodes = NetworkElementFactory.convertOnmsNodeCollectionToNodeArray(onmsNodes);
        } else {
            nodes = NetworkElementFactory.getAllNodes();
        }

        if (command.getNodesWithOutages()) {
            nodes = m_outageModel.filterNodesWithCurrentOutages(nodes);
        }

        int interfaceCount = 0;
        List<NodeModel> displayNodes = new LinkedList<NodeModel>();
        for (Node node : nodes) {
            List<Interface> displayInterfaces = new LinkedList<Interface>();
            if (command.getListInterfaces()) {
                if (command.hasIfAlias()) {
                    Interface[] interfaces = NetworkElementFactory.getInterfacesWithIfAlias(node.getNodeId(), command.getIfAlias());
                    for (Interface intf : interfaces) {
                        if (intf.getSnmpIfAlias() != null && !intf.getSnmpIfAlias().equals("")) {
                            interfaceCount++;

                            displayInterfaces.add(intf);                    
                        }
                    }
                } else {
                    Interface[] interfaces = NetworkElementFactory.getActiveInterfacesOnNode(node.getNodeId());
                    for (Interface intf : interfaces) {
                        if (!"0.0.0.0".equals(intf.getIpAddress())) { 
                            interfaceCount++;
                            displayInterfaces.add(intf);
                        }
                    }
                }
            }
            displayNodes.add(new NodeListModel.NodeModel(node, displayInterfaces));
        }

        NodeListModel model = new NodeListModel(displayNodes, interfaceCount);
        ModelAndView modelAndView = new ModelAndView(getSuccessView(), "model", model);
        modelAndView.addObject(getCommandName(), command);
        return modelAndView;
    }
    

    public void afterPropertiesSet() throws Exception {
        if (m_outageModel == null) {
            throw new IllegalStateException("outageModel property cannot be null");
        }

        if (m_nodeDao == null) {
            throw new IllegalStateException("nodeDao property cannot be null");
        }

        if (m_categoryDao == null) {
            throw new IllegalStateException("categoryDao property cannot be null");
        }

        if (m_siteStatusViewService == null) {
            throw new IllegalStateException("siteStatusViewService property cannot be null");
        }
        
        if (m_successView == null) {
            throw new IllegalStateException("successView property cannot be null");
        }
    }

    public CategoryDao getCategoryDao() {
        return m_categoryDao;
    }

    public void setCategoryDao(CategoryDao categoryDao) {
        m_categoryDao = categoryDao;
    }

    public NodeDao getNodeDao() {
        return m_nodeDao;
    }

    public void setNodeDao(NodeDao nodeDao) {
        m_nodeDao = nodeDao;
    }

    public OutageModel getOutageModel() {
        return m_outageModel;
    }

    public void setOutageModel(OutageModel outageModel) {
        m_outageModel = outageModel;
    }

    public SiteStatusViewService getSiteStatusViewService() {
        return m_siteStatusViewService;
    }

    public void setSiteStatusViewService(SiteStatusViewService siteStatusViewService) {
        m_siteStatusViewService = siteStatusViewService;
    }
    
    public String getSuccessView() {
        return m_successView;
    }

    public void setSuccessView(String successView) {
        m_successView = successView;
    }

    public TransactionTemplate getTransactionTemplate() {
        return m_transactionTemplate;
    }

    public void setTransactionTemplate(TransactionTemplate transactionTemplate) {
        m_transactionTemplate = transactionTemplate;
    }
}

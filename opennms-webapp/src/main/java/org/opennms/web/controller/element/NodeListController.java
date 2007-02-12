package org.opennms.web.controller.element;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.opennms.web.command.NodeListCommand;
import org.opennms.web.svclayer.NodeListService;
import org.opennms.web.svclayer.support.NodeListModel;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractCommandController;

/**
 * Node list controller.
 * 
 * @author <a href="dj@opennms.org">DJ Gregor</a>
 *
 */
public class NodeListController extends AbstractCommandController implements InitializingBean {

    private String m_successView;
    private NodeListService m_nodeListService;
    
    @Override
    protected ModelAndView handle(HttpServletRequest request, HttpServletResponse response, Object cmd, BindException errors) throws Exception {
        NodeListCommand command = (NodeListCommand) cmd;
    
        NodeListModel model = m_nodeListService.createNodeList(command);
        ModelAndView modelAndView = new ModelAndView(getSuccessView(), "model", model);
        modelAndView.addObject(getCommandName(), command);
        return modelAndView;
    }

    public void afterPropertiesSet() throws Exception {
        Assert.state(m_successView != null, "successView property cannot be null");
        Assert.state(m_nodeListService != null, "nodeListService property cannot be null");
    }
    
    public String getSuccessView() {
        return m_successView;
    }

    public void setSuccessView(String successView) {
        m_successView = successView;
    }

    public NodeListService getNodeListService() {
        return m_nodeListService;
    }

    public void setNodeListService(NodeListService nodeListService) {
        m_nodeListService = nodeListService;
    }
}

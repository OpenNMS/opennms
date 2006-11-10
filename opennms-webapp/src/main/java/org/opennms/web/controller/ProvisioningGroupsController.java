package org.opennms.web.controller;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.opennms.web.svclayer.ManualProvisioningService;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.SimpleFormController;

public class ProvisioningGroupsController extends SimpleFormController {

    private ManualProvisioningService m_provisioningService;

    public void setProvisioningService(ManualProvisioningService provisioningService) {
        m_provisioningService = provisioningService;
    }
    
    public static class GroupAction {
        private String m_action = "show";
        private String m_groupName;
        
        public String getAction() {
            return m_action;
        }
        public void setAction(String action) {
            m_action = action;
        }
        public String getGroupName() {
            return m_groupName;
        }
        public void setGroupName(String groupName) {
            m_groupName = groupName;
        }
        
        
    }
    
    public ProvisioningGroupsController() {
        setCommandClass(GroupAction.class);
    }
    
    @Override
    protected ModelAndView onSubmit(HttpServletRequest request, HttpServletResponse response, Object cmd, BindException errors) throws Exception {
        GroupAction command = (GroupAction)cmd;
        String action = command.getAction();
        
        System.err.println("Action is "+action);
        
        if (action == null || "show".equalsIgnoreCase(action)) {
            return doShow(request, response, command, errors);
        } else if ("addGroup".equalsIgnoreCase(action)) {
            return doAddGroup(request, response, command, errors);
        } else if ("deleteNodes".equalsIgnoreCase(action)) {
            return doDeleteNodes(request, response, command, errors);
        } else if ("import".equalsIgnoreCase(action)) {
            return doImport(request, response, command, errors);
        } else if ("deleteGroup".equalsIgnoreCase(action)) {
            return doDeleteGroup(request, response, command, errors);
        } else {
            errors.reject("Unrecognized action: "+action);
            return super.onSubmit(request, response, command, errors);
        }
        
    }

    private ModelAndView doShow(HttpServletRequest request, HttpServletResponse response, GroupAction command, BindException errors) throws Exception {
        return showForm(request, response, errors);
    }

    private ModelAndView doDeleteGroup(HttpServletRequest request, HttpServletResponse response, GroupAction command, BindException errors) throws Exception {
        m_provisioningService.deleteProvisioningGroup(command.getGroupName());
        return showForm(request, response, errors);
    }

    private ModelAndView doImport(HttpServletRequest request, HttpServletResponse response, GroupAction command, BindException errors) throws Exception {
        m_provisioningService.importProvisioningGroup(command.getGroupName());
        return showForm(request, response, errors);
    }

    private ModelAndView doDeleteNodes(HttpServletRequest request, HttpServletResponse response, GroupAction command, BindException errors) throws Exception {
        m_provisioningService.deleteAllNodes(command.getGroupName());
        return showForm(request, response, errors);
    }

    private ModelAndView doAddGroup(HttpServletRequest request, HttpServletResponse response, GroupAction command, BindException errors) throws Exception {
        m_provisioningService.createProvisioningGroup(command.getGroupName());
        return showForm(request, response, errors);
    }

    @Override
    protected Map referenceData(HttpServletRequest request) throws Exception {
        Map<String, Object> refData = new HashMap<String, Object>();
        refData.put("groups", m_provisioningService.getAllGroups());
        refData.put("dbNodeCounts", m_provisioningService.getGroupDbNodeCounts());
        
        
        return refData;
    }

    
}

package org.opennms.web.controller;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.opennms.netmgt.config.modelimport.ModelImport;
import org.opennms.web.svclayer.ManualProvisioningService;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.SimpleFormController;

public class ImportController extends SimpleFormController {

    public static class TreeCommand {
        private String m_formPath;
        private String m_action;
        private Object m_formData;
        private String m_currentNode;
        
        public String getAction() {
            return m_action;
        }
        public void setAction(String action) {
            m_action = action;
        }
        public Object getFormData() {
            return m_formData;
        }
        public void setFormData(Object importData) {
            m_formData = importData;
        }
        public String getFormPath() {
            return m_formPath;
        }
        public void setFormPath(String target) {
            m_formPath = target;
        }
        public String getCurrentNode() {
            return m_currentNode;
        }
        public void setCurrentNode(String node) {
            m_currentNode = node;
        }
        public String getDataPath() {
            return m_formPath.substring("formData.".length());
        }
        public void setDataPath(String path) {
            m_formPath = "formData."+path;
        }
    }

    private ManualProvisioningService m_provisioningService;

    public ImportController() {
        super.setCommandName("nodeEditForm");
    }
    
    public void setProvisioningService(ManualProvisioningService provisioningService) {
        m_provisioningService = provisioningService;
    }
    
    @Override
    protected ModelAndView onSubmit(HttpServletRequest request, HttpServletResponse response, Object command, BindException errors) throws Exception {
        TreeCommand treeCmd = (TreeCommand)command;
        String action = treeCmd.getAction();
        if (action == null) {
            errors.reject("Unrecognized action: "+action);
            return super.onSubmit(request, response, command, errors);
        } else if ("addNode".equalsIgnoreCase(action)) {
            return doAddNode(request, response, command, errors);
        } else if ("addInterface".equalsIgnoreCase(action)) {
            return doAddInterface(request, response, command, errors);
        } else if ("addService".equalsIgnoreCase(action)) {
            return doAddService(request, response, command, errors);
        } else if ("addCategory".equalsIgnoreCase(action)) {
            return doAddCategory(request, response, command, errors);
        } else if ("save".equalsIgnoreCase(action)) {
            return doSave(request, response, command, errors);
        } else if ("edit".equalsIgnoreCase(action)) {
            return doEdit(request, response, command, errors);
        } else if ("cancel".equalsIgnoreCase(action)) {
            return doCancel(request, response, command, errors);
        } else if ("delete".equalsIgnoreCase(action)) {
            return doDelete(request, response, command, errors);
        } else if ("import".equalsIgnoreCase(action)) {
            return doImport(request, response, command, errors);
        } else {
            errors.reject("Unrecognized action: "+action);
            return super.onSubmit(request, response, command, errors);
        }
        
    }

    private ModelAndView doCancel(HttpServletRequest request, HttpServletResponse response, Object command, BindException errors) throws Exception {
        TreeCommand treeCmd = (TreeCommand)command;

        ModelImport formData = m_provisioningService.getProvisioningGroup("manual");
        treeCmd.setFormData(formData);
        
        treeCmd.setCurrentNode("");
        
        return showForm(request, response, errors);
    }

    private ModelAndView doImport(HttpServletRequest request, HttpServletResponse response, Object command, BindException errors) throws Exception {
        m_provisioningService.importProvisioningGroup("manual");
        return super.showForm(request, response, errors);
        
    }

    private ModelAndView doDelete(HttpServletRequest request, HttpServletResponse response, Object command, BindException errors) throws Exception {
        TreeCommand treeCmd = (TreeCommand)command;
        
        ModelImport formData = m_provisioningService.deletePath("manual", treeCmd.getDataPath());
        treeCmd.setFormData(formData);
        
        return showForm(request, response, errors);
    }

    private ModelAndView doAddCategory(HttpServletRequest request, HttpServletResponse response, Object command, BindException errors) throws Exception {
        TreeCommand treeCmd = (TreeCommand)command;
        
        ModelImport formData = m_provisioningService.addCategoryToNode("manual", treeCmd.getDataPath(), "New Category");
        treeCmd.setFormData(formData);
        
        treeCmd.setCurrentNode(treeCmd.getFormPath()+".category[0]");
        
        
        return showForm(request, response, errors);
    }

    private ModelAndView doEdit(HttpServletRequest request, HttpServletResponse response, Object command, BindException errors) throws Exception {
        TreeCommand treeCmd = (TreeCommand)command;
        
        treeCmd.setCurrentNode(treeCmd.getFormPath());
        
        return showForm(request, response, errors);
    }

    private ModelAndView doSave(HttpServletRequest request, HttpServletResponse response, Object command, BindException errors) throws Exception {
        TreeCommand treeCmd = (TreeCommand)command;
        ModelImport formData = m_provisioningService.saveProvisioningGroup("manual", (ModelImport)treeCmd.getFormData());
        treeCmd.setFormData(formData);
        treeCmd.setCurrentNode("");
        return showForm(request, response, errors);
    }

    private ModelAndView doAddService(HttpServletRequest request, HttpServletResponse response, Object command, BindException errors) throws Exception {
        TreeCommand treeCmd = (TreeCommand)command;

        ModelImport formData = m_provisioningService.addServiceToInterface("manual", treeCmd.getDataPath(), "SVC");
        treeCmd.setFormData(formData);
        
        treeCmd.setCurrentNode(treeCmd.getFormPath()+".monitoredService[0]");
        
        
        return showForm(request, response, errors);
    }

    private ModelAndView doAddInterface(HttpServletRequest request, HttpServletResponse response, Object command, BindException errors) throws Exception {
        TreeCommand treeCmd = (TreeCommand)command;
        
        ModelImport formData = m_provisioningService.addInterfaceToNode("manual", treeCmd.getDataPath(), "1.1.1.1");
        treeCmd.setFormData(formData);
        
        treeCmd.setCurrentNode(treeCmd.getFormPath()+".interface[0]");
        
        
        return showForm(request, response, errors);
    }

    private ModelAndView doAddNode(HttpServletRequest request, HttpServletResponse response, Object command, BindException errors) throws Exception {
        TreeCommand treeCmd = (TreeCommand)command;
        treeCmd.setFormData(m_provisioningService.addNewNodeToGroup("manual", "New Node"));
        
        treeCmd.setCurrentNode("formData.node[0]");

        return showForm(request, response, errors);
    }

    @Override
    protected Object formBackingObject(HttpServletRequest arg0) throws Exception {
        TreeCommand formCommand = new TreeCommand();
        formCommand.setFormData(formDataObject());
        return formCommand;
    }

    private Object formDataObject() throws Exception {
        ModelImport importData = m_provisioningService.getProvisioningGroup("manual");
        if (importData == null) {
            importData = m_provisioningService.createProvisioningGroup("manual");
        }
        return importData;
    }

    @Override
    protected Map referenceData(HttpServletRequest request) throws Exception {
        Map map = new HashMap();
        
        String[] choices = { "P", "S", "C", "N" };
        
        map.put("snmpPrimaryChoices", choices);
        return map;
    }
    
}

/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.web.controller;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.hibernate.validator.constraints.NotEmpty;
import org.opennms.netmgt.model.PrimaryType;
import org.opennms.netmgt.provision.persist.requisition.Requisition;
import org.opennms.web.svclayer.ManualProvisioningService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.servlet.ModelAndView;

/**
 * TODO: Add validation
 */
@Controller
@RequestMapping("/admin/editProvisioningGroup.htm")
@SessionAttributes({ "freeFormEditing" })
public class EditProvisioningGroupController {

    private static final Logger LOG = LoggerFactory.getLogger(EditProvisioningGroupController.class);

    public static final String COMMAND_NAME = "nodeEditForm";

    public static class TreeCommand {

        private String m_formPath;

        private String m_action;

        @Valid
        private Requisition m_formData;

        @NotNull
        private String m_currentNode;

        @NotEmpty
        private String m_groupName = "hardcoded";

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
        public Requisition getFormData() {
            return m_formData;
        }
        public void setFormData(final Requisition importData) {
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
            //added nodeEditForm. to the formData. because somehow we are getting that attached a prefix as well. 
            return m_formPath.substring((COMMAND_NAME + ".formData.").length());
        }
        public void setDataPath(String path) {
            //added nodeEditForm. to the formData. because somehow we are getting that attached a prefix as well.
            m_formPath = COMMAND_NAME + ".formData." + path;
        }

        @Override
        public String toString() {
            return new ToStringBuilder(this)
            .append("action", getAction())
            .append("currentNode", getCurrentNode())
            .append("dataPath", getDataPath())
            .append("formData", getFormData())
            .append("formPath", getFormPath())
            .append("groupName", getGroupName())
            .toString();
        }
    }

    @Autowired
    private ManualProvisioningService m_provisioningService;

    /**
     * @return View for this controller action
     */
    @RequestMapping(method=RequestMethod.POST)
    public ModelAndView onSubmit(@ModelAttribute(COMMAND_NAME) @Valid TreeCommand treeCmd, BindingResult errors, HttpSession session) {

        if (errors.hasErrors()) {
            return new ModelAndView("/admin/editProvisioningGroup", COMMAND_NAME, treeCmd);
        }

        String action = treeCmd.getAction();
        if (action == null) {
            // Do nothing
        } else if ("toggleFreeForm".equalsIgnoreCase(action)) {
            Boolean isFreeForm = (Boolean)session.getAttribute("freeFormEditing");
            if (isFreeForm == null) {
                isFreeForm = false;
            }
            session.setAttribute("freeFormEditing", !isFreeForm);
        } else if ("addNode".equalsIgnoreCase(action)) {
            doAddNode(treeCmd);
        } else if ("addInterface".equalsIgnoreCase(action)) {
            doAddInterface(treeCmd);
        } else if ("addService".equalsIgnoreCase(action)) {
            doAddService(treeCmd);
        } else if ("addCategory".equalsIgnoreCase(action)) {
            doAddCategory(treeCmd);
        } else if ("addAssetField".equalsIgnoreCase(action)) {
            doAddAssetField(treeCmd);
        } else if ("save".equalsIgnoreCase(action)) {
            doSave(treeCmd, errors);
        } else if ("edit".equalsIgnoreCase(action)) {
            doEdit(treeCmd);
        } else if ("cancel".equalsIgnoreCase(action)) {
            doCancel(treeCmd);
        } else if ("delete".equalsIgnoreCase(action)) {
            doDelete(treeCmd);
        } else if ("import".equalsIgnoreCase(action)) {
            doImport(treeCmd);
        } else if ("done".equalsIgnoreCase(action)) {
            return done(treeCmd);
        } else {
            errors.reject("Unrecognized action: "+action);
        }

        return new ModelAndView("/admin/editProvisioningGroup", COMMAND_NAME, treeCmd);
    }

    private ModelAndView done(TreeCommand treeCmd) {
        return new ModelAndView("redirect:/admin/provisioningGroups.htm");
    }

    private void doCancel(TreeCommand treeCmd) {
        Requisition formData = m_provisioningService.getProvisioningGroup(treeCmd.getGroupName());
        treeCmd.setFormData(formData);
        treeCmd.setCurrentNode("");
    }

    private void doImport(TreeCommand treeCmd) {
        m_provisioningService.importProvisioningGroup(treeCmd.getGroupName());
    }

    private void doDelete(TreeCommand treeCmd) {
        Requisition formData = m_provisioningService.deletePath(treeCmd.getGroupName(), treeCmd.getDataPath());
        treeCmd.setFormData(formData);
    }

    private void doAddCategory(TreeCommand treeCmd) {
        Requisition formData = m_provisioningService.addCategoryToNode(treeCmd.getGroupName(), treeCmd.getDataPath(), "New Category");
        treeCmd.setFormData(formData);
        treeCmd.setCurrentNode(treeCmd.getFormPath()+".category[0]");
    }

    private void doAddAssetField(TreeCommand treeCmd) {
        Requisition formData = m_provisioningService.addAssetFieldToNode(treeCmd.getGroupName(), treeCmd.getDataPath(), "key", "value");
        treeCmd.setFormData(formData);
        treeCmd.setCurrentNode(treeCmd.getFormPath()+".asset[0]");
    }

    private void doEdit(TreeCommand treeCmd) {
        treeCmd.setCurrentNode(treeCmd.getFormPath());
    }

    private void doSave(TreeCommand treeCmd, BindingResult errors) {
        try {
            LOG.debug("treeCmd = {}", treeCmd);
            treeCmd.getFormData().validate();
            final Requisition formData = m_provisioningService.saveProvisioningGroup(treeCmd.getGroupName(), treeCmd.getFormData());
            treeCmd.setFormData(formData);
            treeCmd.setCurrentNode("");
        } catch (final Throwable t) {
            errors.reject(t.getMessage());
        }
    }

    private void doAddService(TreeCommand treeCmd) {
        Requisition formData = m_provisioningService.addServiceToInterface(treeCmd.getGroupName(), treeCmd.getDataPath(), "SVC");
        treeCmd.setFormData(formData);
        treeCmd.setCurrentNode(treeCmd.getFormPath()+".monitoredService[0]");
    }

    private void doAddInterface(TreeCommand treeCmd) {
        Requisition formData = m_provisioningService.addInterfaceToNode(treeCmd.getGroupName(), treeCmd.getDataPath(), "");
        treeCmd.setFormData(formData);
        treeCmd.setCurrentNode(treeCmd.getFormPath()+".interface[0]");
    }

    private void doAddNode(TreeCommand treeCmd) {
        treeCmd.setFormData(m_provisioningService.addNewNodeToGroup(treeCmd.getGroupName(), "New Node"));
        treeCmd.setCurrentNode(treeCmd.getFormPath()+".node[0]");
    }

    /**
     * Attach the current requisition to the model during each request.
     * 
     * @param foreignSourceName
     * @return
     */
    @ModelAttribute(COMMAND_NAME)
    private TreeCommand formBackingObject(@RequestParam String groupName) {

        if (groupName == null) {
            throw new IllegalArgumentException("groupName required");
        }

        TreeCommand formCommand = new TreeCommand();
        Requisition formData = m_provisioningService.getProvisioningGroup(groupName);
        // If we're trying to edit a requisition that doesn't yet exist, create it
        if (formData == null) {
            formData = m_provisioningService.createProvisioningGroup(groupName);
        }
        formCommand.setFormData(formData);
        return formCommand;
    }

    /**
     * Enrich the model using {@link ModelMap} with extra attributes necessary to 
     * display the page.
     * 
     * @param map
     * @param groupName
     */
    @ModelAttribute
    private void referenceData(ModelMap map, @RequestParam String groupName) {

        // Fetch the list of possible values out of the Castor enumeration
        List<String> choices = new ArrayList<String>();
        for (PrimaryType type : PrimaryType.getAllTypes()) {
            choices.add(type.getCode());
        }
        map.put("snmpPrimaryChoices", choices);

        List<String> status = new ArrayList<String>();
        status.add("1");
        status.add("3");
        map.put("statusChoices", status);

        List<String> services = new ArrayList<String>(m_provisioningService.getServiceTypeNames(groupName));
        Collections.sort(services);
        map.put("services",  services);

        List<String> categories = new ArrayList<String>(m_provisioningService.getNodeCategoryNames());
        Collections.sort(categories);
        map.put("categories", categories);

        List<String> assetFields = new ArrayList<String>(m_provisioningService.getAssetFieldNames());
        Collections.sort(assetFields);
        map.put("assetFields", assetFields);
    }
}

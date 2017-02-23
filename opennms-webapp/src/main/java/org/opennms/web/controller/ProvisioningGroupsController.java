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

import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.opennms.netmgt.provision.persist.ForeignSourceService;
import org.opennms.netmgt.provision.persist.foreignsource.ForeignSource;
import org.opennms.netmgt.provision.persist.requisition.Requisition;
import org.opennms.web.svclayer.ManualProvisioningService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * TODO: Add validation
 */
@Controller
@RequestMapping("/admin/provisioningGroups.htm")
public class ProvisioningGroupsController {

    public static final String DEFAULT_PROVISIONING_GROUP_NAME = "default";

    @Autowired
    private ManualProvisioningService m_provisioningService;

    @Autowired
    private ForeignSourceService m_foreignSourceService;

    public static class GroupAction {
        private String m_groupName;
        private String m_action = "show";
        private String m_actionTarget;
        
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
        
        public String getActionTarget() {
            return m_actionTarget;
        }
        public void setActionTarget(String target) {
            m_actionTarget = target;
        }
    }

    /**
     * Take action based on {@link GroupAction} form binding. Uses the default name 
     * "groupAction" for the command name.
     * 
     * @return View for this controller action
     */
    @RequestMapping(method=RequestMethod.POST)
    public String onSubmit(GroupAction command, BindingResult result) {
        String action = command.getAction();

        if (action == null || "show".equalsIgnoreCase(action)) {
            doShow(command);
        } else if ("addGroup".equalsIgnoreCase(action)) {
            doAddGroup(command);
        } else if ("deleteNodes".equalsIgnoreCase(action)) {
            doDeleteNodes(command);
        } else if ("import".equalsIgnoreCase(action)) {
            doImport(command);
        } else if ("deleteGroup".equalsIgnoreCase(action)) {
            doDeleteGroup(command);
        } else if ("cloneForeignSource".equalsIgnoreCase(action)) {
            doCloneForeignSource(command);
        } else if ("resetDefaultForeignSource".equalsIgnoreCase(action)) {
            doResetDefaultForeignSource(command);
        } else {
            result.reject("Unrecognized action: " + action);
        }

        return "redirect:/admin/provisioningGroups.htm";
    }

    private void doShow(GroupAction command) {
        // Just go to the view
    }

    private void doDeleteGroup(GroupAction command) {
        m_provisioningService.deleteProvisioningGroup(command.getGroupName());
        m_foreignSourceService.deleteForeignSource(command.getGroupName());
        //return "redirect:/admin/provisioningGroups.htm";
    }

    private void doImport(GroupAction command) {
        m_provisioningService.importProvisioningGroup(command.getGroupName());
        try { Thread.sleep(500); } catch (InterruptedException e) {}
        //return "redirect:/admin/provisioningGroups.htm";
    }

    private void doDeleteNodes(GroupAction command) {
        m_provisioningService.deleteAllNodes(command.getGroupName());
        //return "redirect:/admin/provisioningGroups.htm";
    }

    private void doAddGroup(GroupAction command) {
        String groupName = command.getGroupName();
        if (groupName.equals(DEFAULT_PROVISIONING_GROUP_NAME) || groupName.equals("")) {
            //return "redirect:/admin/provisioningGroups.htm";
        } else if (m_provisioningService.getProvisioningGroup(groupName) != null) {
            // Requisition already exists; don't clobber it!
            //return "redirect:/admin/provisioningGroups.htm";
        } else {
            m_provisioningService.createProvisioningGroup(command.getGroupName());
        }
        //return "redirect:/admin/provisioningGroups.htm";
    }

    private void doCloneForeignSource(GroupAction command) {
        m_foreignSourceService.cloneForeignSource(command.getGroupName(), command.getActionTarget());
        //return "redirect:/admin/provisioningGroups.htm";
    }

    private void doResetDefaultForeignSource(GroupAction command) {
        m_foreignSourceService.deleteForeignSource(DEFAULT_PROVISIONING_GROUP_NAME);
        //return "redirect:/admin/provisioningGroups.htm";
    }

    /**
     * Enrich the model using {@link ModelMap} with extra attributes necessary to 
     * display the page.
     * 
     * @param map
     */
    @RequestMapping(method=RequestMethod.GET)
    public void referenceData(ModelMap map) {

        Set<String>               names          = new TreeSet<String>();
        Map<String,Requisition>   groups         = new TreeMap<String,Requisition>();
        Map<String,ForeignSource> foreignSources = new TreeMap<String,ForeignSource>();

        for (Requisition mi : m_provisioningService.getAllGroups()) {
            if(mi != null){
                names.add(mi.getForeignSource());
                groups.put(mi.getForeignSource(), mi);
            }
        }

        for (ForeignSource fs : m_foreignSourceService.getAllForeignSources()) {
            if (!fs.isDefault()) {
                names.add(fs.getName());
                foreignSources.put(fs.getName(), fs);
            }
        }

        map.put("foreignSourceNames", names);
        map.put("groups", groups);
        map.put("foreignSources", foreignSources);
        map.put("dbNodeCounts", m_provisioningService.getGroupDbNodeCounts());
    }
}

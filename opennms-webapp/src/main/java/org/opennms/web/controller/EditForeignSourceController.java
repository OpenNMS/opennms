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

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.hibernate.validator.constraints.NotEmpty;
import org.joda.time.Duration;
import org.opennms.core.spring.PropertyPath;
import org.opennms.netmgt.provision.persist.ForeignSourceService;
import org.opennms.netmgt.provision.persist.StringIntervalPropertyEditor;
import org.opennms.netmgt.provision.persist.foreignsource.ForeignSource;
import org.opennms.netmgt.provision.persist.foreignsource.PluginConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

/**
 * TODO: Add validation
 */
@Controller
@RequestMapping("/admin/editForeignSource.htm")
public class EditForeignSourceController {

    private static final Logger LOG = LoggerFactory.getLogger(EditForeignSourceController.class);

    public static final String COMMAND_NAME = "foreignSourceEditForm";

    @Autowired
    private ForeignSourceService m_foreignSourceService;

    //private static final Map<String,Set<String>> m_pluginParameters = new HashMap<String,Set<String>>();

    public static class TreeCommand {

        private String m_formPath;

        private String m_action;

        @NotNull @Valid
        private ForeignSource m_formData;

        @NotNull
        private String m_currentNode;

        @NotEmpty
        private String m_foreignSourceName = "hardcoded";
        
        public String getAction() {
            return m_action;
        }
        public void setAction(String action) {
            m_action = action;
        }
        public String getForeignSourceName() {
            return m_foreignSourceName;
        }
        public void setForeignSourceName(String foreignSourceName) {
            m_foreignSourceName = foreignSourceName;
        }
        public ForeignSource getFormData() {
            return m_formData;
        }
        public void setFormData(ForeignSource importData) {
            m_formData = importData;
        }
        public static String getDefaultFormPath() {
            return COMMAND_NAME + ".formData";
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
            return m_formPath.substring((COMMAND_NAME + ".formData.").length());
        }
        public void setDataPath(String path) {
            m_formPath = COMMAND_NAME + ".formData." + path;
        }
        
        @Override
        public String toString() {
            return new ToStringBuilder(this)
                .append("foreign source", m_foreignSourceName)
                .append("form path", m_formPath)
                .append("action", m_action)
                .append("form data", m_formData)
                .append("current node", m_currentNode)
                .toString();
        }
    }

    @InitBinder
    public void initBinder(ServletRequestDataBinder binder) {
        binder.registerCustomEditor(Duration.class, new StringIntervalPropertyEditor());
    }

    /**
     * @return View for this controller action
     */
    @RequestMapping(method=RequestMethod.POST)
    public ModelAndView onSubmit(@ModelAttribute(COMMAND_NAME) @Valid TreeCommand treeCmd, BindingResult errors) {

        LOG.debug("treeCmd = {}", treeCmd);

        if (errors.hasErrors()) {
            return new ModelAndView("/admin/editForeignSource", COMMAND_NAME, treeCmd);
        }

        String action = treeCmd.getAction();
        if (action == null) {
            // Do nothing
        } else if ("addDetector".equalsIgnoreCase(action)) {
            doAddDetector(treeCmd);
        } else if ("addPolicy".equalsIgnoreCase(action)) {
            doAddPolicy(treeCmd);
        } else if ("addParameter".equalsIgnoreCase(action)) {
            doAddParameter(treeCmd);
        } else if ("save".equalsIgnoreCase(action)) {
            doSave(treeCmd);
        } else if ("edit".equalsIgnoreCase(action)) {
            doEdit(treeCmd);
        } else if ("cancel".equalsIgnoreCase(action)) {
            doCancel(treeCmd);
        } else if ("delete".equalsIgnoreCase(action)) {
            doDelete(treeCmd);
        } else if ("done".equalsIgnoreCase(action)) {
            return done(treeCmd);
        } else {
            errors.reject("Unrecognized action: " + action);
        }

        return new ModelAndView("/admin/editForeignSource", COMMAND_NAME, treeCmd);
    }

    private ModelAndView done(TreeCommand treeCmd) {
        // Save the foreign source
        m_foreignSourceService.saveForeignSource(treeCmd.getForeignSourceName(), treeCmd.getFormData());
        // Go back to the provisioning groups list
        return new ModelAndView("redirect:/admin/provisioningGroups.htm");
    }

    private void doAddDetector(TreeCommand treeCmd) {
        ForeignSource fs = m_foreignSourceService.addDetectorToForeignSource(treeCmd.getForeignSourceName(), "New Detector");
        treeCmd.setFormData(fs);
        treeCmd.setCurrentNode(treeCmd.getFormPath()+".detectors["+ (fs.getDetectors().size()-1) +"]");
    }

    private void doAddPolicy(TreeCommand treeCmd) {
        ForeignSource fs = m_foreignSourceService.addPolicyToForeignSource(treeCmd.getForeignSourceName(), "New Policy");
        treeCmd.setFormData(fs);
        treeCmd.setCurrentNode(treeCmd.getFormPath()+".policies["+ (fs.getPolicies().size()-1) +"]");
    }

    private void doAddParameter(TreeCommand treeCmd) {
        ForeignSource fs = m_foreignSourceService.addParameter(treeCmd.getForeignSourceName(), treeCmd.getDataPath());
        treeCmd.setFormData(fs);
        PropertyPath path = new PropertyPath(treeCmd.getDataPath());
        PluginConfig obj = (PluginConfig)path.getValue(fs);
        int numParameters = (obj.getParameters().size() - 1);
        treeCmd.setCurrentNode(treeCmd.getFormPath()+".parameters[" + numParameters + "]");
    }
    
    private void doSave(TreeCommand treeCmd) {
        ForeignSource fs = m_foreignSourceService.saveForeignSource(treeCmd.getForeignSourceName(), treeCmd.getFormData());
        treeCmd.setFormData(fs);
        treeCmd.setCurrentNode("");
    }

    private void doEdit(TreeCommand treeCmd) {
        treeCmd.setCurrentNode(treeCmd.getFormPath());
    }

    private void doCancel(TreeCommand treeCmd) {
        ForeignSource fs = m_foreignSourceService.getForeignSource(treeCmd.getForeignSourceName());
        treeCmd.setFormData(fs);
        treeCmd.setCurrentNode("");
    }

    private void doDelete(TreeCommand treeCmd) {
        ForeignSource fs = m_foreignSourceService.deletePath(treeCmd.getForeignSourceName(), treeCmd.getDataPath());
        treeCmd.setFormData(fs);
    }

    /**
     * Attach the current foreignSource to the model during each request.
     * 
     * @param foreignSourceName
     * @return
     */
    @ModelAttribute(COMMAND_NAME)
    public TreeCommand formBackingObject(@RequestParam String foreignSourceName) {
        TreeCommand formCommand = new TreeCommand();
        if (foreignSourceName == null) {
            throw new IllegalArgumentException("foreignSourceName required");
        }
        ForeignSource fs = m_foreignSourceService.getForeignSource(foreignSourceName);
        formCommand.setFormData(fs);
        return formCommand;
    }

    /**
     * Add additional form information to the model.
     */
    @ModelAttribute
    private void referenceData(ModelMap map) {

        int classFieldWidth = 20;
        final int valueFieldWidth = 20;

        //final Map<String,Set<String>> classParameters = new TreeMap<String,Set<String>>();

        final Map<String,String> detectorTypes = m_foreignSourceService.getDetectorTypes();
        map.put("detectorTypes", detectorTypes);
        for (String key : detectorTypes.keySet()) {
            //classParameters.put(key, getParametersForClass(key));
            classFieldWidth = Math.max(classFieldWidth, key.length());
        }

        final Map<String, String> policyTypes = m_foreignSourceService.getPolicyTypes();
        map.put("policyTypes", policyTypes);
        for (String key : policyTypes.keySet()) {
            //classParameters.put(key, getParametersForClass(key));
            classFieldWidth = Math.max(classFieldWidth, key.length());
        }

        map.put("pluginInfo", m_foreignSourceService.getWrappers());
        map.put("classFieldWidth", classFieldWidth);
        map.put("valueFieldWidth", valueFieldWidth);
    }

    /*
    private static Set<String> getParametersForClass(String clazz) {
        if (m_pluginParameters.containsKey(clazz)) {
            return m_pluginParameters.get(clazz);
        }
        Set<String> parameters = new TreeSet<String>();
        try {
            final BeanWrapper wrapper = new BeanWrapperImpl(Class.forName(clazz));
            wrapper.registerCustomEditor(XMLGregorianCalendar.class, new StringXmlCalendarPropertyEditor());
            wrapper.registerCustomEditor(java.net.InetAddress.class, new InetAddressTypeEditor());
            wrapper.registerCustomEditor(OnmsSeverity.class, new OnmsSeverityEditor());
            wrapper.registerCustomEditor(PrimaryType.class, new PrimaryTypeEditor());
            for (final PropertyDescriptor pd : wrapper.getPropertyDescriptors()) {
                if (!"class".equals(pd.getName())) {
                    parameters.add(pd.getName());
                }
            }
            m_pluginParameters.put(clazz, parameters);
            return parameters;
        } catch (ClassNotFoundException e) {
            LOG.warn("unable to wrap class {}", clazz, e);
        }
        return null;
    }
    */
}

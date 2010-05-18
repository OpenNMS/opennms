//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2009 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
//
// For more information contact:
//      OpenNMS Licensing       <license@opennms.org>
//      http://www.opennms.org/
//      http://www.opennms.com/
//
package org.opennms.web.controller;

import java.beans.PropertyDescriptor;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.log4j.Category;
import org.joda.time.Duration;
import org.opennms.core.utils.PropertyPath;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.provision.persist.ForeignSourceService;
import org.opennms.netmgt.provision.persist.StringIntervalPropertyEditor;
import org.opennms.netmgt.provision.persist.foreignsource.ForeignSource;
import org.opennms.netmgt.provision.persist.foreignsource.PluginConfig;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindException;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.SimpleFormController;

@Controller
public class EditForeignSourceController extends SimpleFormController {

    private ForeignSourceService m_foreignSourceService;
    private static final Map<String,Set<String>> m_pluginParameters = new HashMap<String,Set<String>>();
    
    public void setForeignSourceService(ForeignSourceService fss) {
        m_foreignSourceService = fss;
    }
    
    public static class TreeCommand {
        private String m_formPath;
        private String m_action;
        private ForeignSource m_formData;
        private String m_currentNode;
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
//            System.err.println("setFormData:");
//            System.err.println(importData);
            m_formData = importData;
        }
        public String getDefaultFormPath() {
            return "foreignSourceEditForm.formData";
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
            return m_formPath.substring("foreignSourceEditForm.formData.".length());
        }
        public void setDataPath(String path) {
            m_formPath = "foreignSourceEditForm.formData."+path;
        }
        
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

    @Override
    protected void initBinder(HttpServletRequest req, ServletRequestDataBinder binder) throws Exception {
        binder.registerCustomEditor(Duration.class, new StringIntervalPropertyEditor());
    }

    @Override
    protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception {
        return super.handleRequestInternal(request, response);
    }

    @Override
    protected ModelAndView onSubmit(HttpServletRequest request, HttpServletResponse response, Object command, BindException errors) throws Exception {
        TreeCommand treeCmd = (TreeCommand)command;
        System.err.println("treeCmd = " + treeCmd);
        String action = treeCmd.getAction();
        if (action == null) {
            return doShow(request, response, treeCmd, errors);
        } else if ("addDetector".equalsIgnoreCase(action)) {
            return doAddDetector(request, response, treeCmd, errors);
        } else if ("addPolicy".equalsIgnoreCase(action)) {
            return doAddPolicy(request, response, treeCmd, errors);
        } else if ("addParameter".equalsIgnoreCase(action)) {
            return doAddParameter(request, response, treeCmd, errors);
        } else if ("save".equalsIgnoreCase(action)) {
            return doSave(request, response, treeCmd, errors);
        } else if ("edit".equalsIgnoreCase(action)) {
            return doEdit(request, response, treeCmd, errors);
        } else if ("cancel".equalsIgnoreCase(action)) {
            return doCancel(request, response, treeCmd, errors);
        } else if ("delete".equalsIgnoreCase(action)) {
            return doDelete(request, response, treeCmd, errors);
        } else if ("done".equalsIgnoreCase(action)) {
            return done(request, response, treeCmd, errors);
        } else {
            errors.reject("Unrecognized action: "+action);
            return showForm(request, response, errors);
        }
        
    }

    private ModelAndView done(HttpServletRequest request, HttpServletResponse response, TreeCommand treeCmd, BindException errors) throws Exception {
        m_foreignSourceService.saveForeignSource(treeCmd.getForeignSourceName(), treeCmd.getFormData());
        return new ModelAndView(getSuccessView());
    }

    private ModelAndView doShow(HttpServletRequest request, HttpServletResponse response, TreeCommand treeCmd, BindException errors) throws Exception {
        return showForm(request, response, errors);
    }

    private ModelAndView doAddDetector(HttpServletRequest request, HttpServletResponse response, TreeCommand treeCmd, BindException errors) throws Exception {
        ForeignSource fs = m_foreignSourceService.addDetectorToForeignSource(treeCmd.getForeignSourceName(), "New Detector");
        treeCmd.setFormData(fs);
        treeCmd.setCurrentNode(treeCmd.getFormPath()+".detectors["+ (fs.getDetectors().size()-1) +"]");
        return showForm(request, response, errors);
    }

    private ModelAndView doAddPolicy(HttpServletRequest request, HttpServletResponse response, TreeCommand treeCmd, BindException errors) throws Exception {
        ForeignSource fs = m_foreignSourceService.addPolicyToForeignSource(treeCmd.getForeignSourceName(), "New Policy");
        treeCmd.setFormData(fs);
        treeCmd.setCurrentNode(treeCmd.getFormPath()+".policies["+ (fs.getPolicies().size()-1) +"]");
        return showForm(request, response, errors);
    }

    private ModelAndView doAddParameter(HttpServletRequest request, HttpServletResponse response, TreeCommand treeCmd, BindException errors) throws Exception {
        ForeignSource fs = m_foreignSourceService.addParameter(treeCmd.getForeignSourceName(), treeCmd.getDataPath());
        treeCmd.setFormData(fs);
        PropertyPath path = new PropertyPath(treeCmd.getDataPath());
        PluginConfig obj = (PluginConfig)path.getValue(fs);
        int numParameters = (obj.getParameters().size() - 1);
        treeCmd.setCurrentNode(treeCmd.getFormPath()+".parameters[" + numParameters + "]");
        return showForm(request, response, errors);
    }
    
    private ModelAndView doSave(HttpServletRequest request, HttpServletResponse response, TreeCommand treeCmd, BindException errors) throws Exception {
        ForeignSource fs = m_foreignSourceService.saveForeignSource(treeCmd.getForeignSourceName(), treeCmd.getFormData());
        treeCmd.setFormData(fs);
        treeCmd.setCurrentNode("");
        return showForm(request, response, errors);
    }

    private ModelAndView doEdit(HttpServletRequest request, HttpServletResponse response, TreeCommand treeCmd, BindException errors) throws Exception {
        treeCmd.setCurrentNode(treeCmd.getFormPath());
        return showForm(request, response, errors);
    }

    private ModelAndView doCancel(HttpServletRequest request, HttpServletResponse response, TreeCommand treeCmd, BindException errors) throws Exception {
        ForeignSource fs = m_foreignSourceService.getForeignSource(treeCmd.getForeignSourceName());
        treeCmd.setFormData(fs);
        treeCmd.setCurrentNode("");
        return showForm(request, response, errors);
    }

    private ModelAndView doDelete(HttpServletRequest request, HttpServletResponse response, TreeCommand treeCmd, BindException errors) throws Exception {
        
        ForeignSource fs = m_foreignSourceService.deletePath(treeCmd.getForeignSourceName(), treeCmd.getDataPath());
        treeCmd.setFormData(fs);
        return showForm(request, response, errors);
    }

    @Override
    protected Object formBackingObject(HttpServletRequest request) throws Exception {
        TreeCommand formCommand = new TreeCommand();
        String foreignSourceName = request.getParameter("foreignSourceName");
        if (foreignSourceName == null) {
            throw new IllegalArgumentException("foreignSourceName required");
        }
        ForeignSource fs = m_foreignSourceService.getForeignSource(foreignSourceName);
        formCommand.setFormData(fs);
        return formCommand;
    }

    @SuppressWarnings("unchecked")
    @Override
    protected Map referenceData(HttpServletRequest request) throws Exception {
        final Map<String, Object> map = new HashMap<String, Object>();
        int classFieldWidth = 20;
        int valueFieldWidth = 20;

        final Map<String,Set<String>> classParameters = new TreeMap<String,Set<String>>();

        final Map<String,String> detectorTypes = m_foreignSourceService.getDetectorTypes();
        map.put("detectorTypes", detectorTypes);
        for (String key : detectorTypes.keySet()) {
            classParameters.put(key, getParametersForClass(key));
            classFieldWidth = Math.max(classFieldWidth, key.length());
        }

        final Map<String, String> policyTypes = m_foreignSourceService.getPolicyTypes();
        map.put("policyTypes", policyTypes);
        for (String key : policyTypes.keySet()) {
            classParameters.put(key, getParametersForClass(key));
            classFieldWidth = Math.max(classFieldWidth, key.length());
        }

        map.put("pluginInfo", m_foreignSourceService.getWrappers());
        map.put("classFieldWidth", classFieldWidth);
        map.put("valueFieldWidth", valueFieldWidth);
        
        return map;
    }

    private Set<String> getParametersForClass(String clazz) {
        if (m_pluginParameters.containsKey(clazz)) {
            return m_pluginParameters.get(clazz);
        }
        Set<String> parameters = new TreeSet<String>();
        try {
            BeanWrapper wrapper = new BeanWrapperImpl(Class.forName(clazz));
            for (PropertyDescriptor pd : wrapper.getPropertyDescriptors()) {
                if (!pd.getName().equals("class")) {
                    parameters.add(pd.getName());
                }
            }
            m_pluginParameters.put(clazz, parameters);
            return parameters;
        } catch (ClassNotFoundException e) {
            log().warn("unable to wrap class " + clazz, e);
        }
        return null;
    }

    private ThreadCategory log() {
        return ThreadCategory.getInstance(EditForeignSourceController.class);
    }
    
}

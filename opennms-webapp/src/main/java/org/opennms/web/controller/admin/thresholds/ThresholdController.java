/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2007 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Modifications:
 * 
 * Created: March 14, 2007
 *
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 *
 * For more information contact:
 *      OpenNMS Licensing       <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 */
package org.opennms.web.controller.admin.thresholds;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.opennms.netmgt.EventConstants;
import org.opennms.netmgt.config.EventconfFactory;
import org.opennms.netmgt.config.ThresholdingConfigFactory;
import org.opennms.netmgt.config.threshd.Basethresholddef;
import org.opennms.netmgt.config.threshd.Expression;
import org.opennms.netmgt.config.threshd.Group;
import org.opennms.netmgt.config.threshd.ResourceFilter;
import org.opennms.netmgt.config.threshd.Threshold;
import org.opennms.netmgt.dao.ResourceDao;
import org.opennms.netmgt.dao.support.GenericIndexResourceType;
import org.opennms.netmgt.model.OnmsResourceType;
import org.opennms.netmgt.model.events.EventBuilder;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.netmgt.xml.eventconf.Logmsg;
import org.opennms.web.Util;
import org.opennms.web.WebSecurityUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

import com.google.common.collect.Multimap;
import com.google.common.collect.TreeMultimap;

/**
 * <p>ThresholdController class.</p>
 *
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 * @author <a href="mailto:jeffg@opennms.org">Jeff Gehlbach</a>
 * @author <a href="mailto:cmiskell@opennms.org">Craig Miskell</a>
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a>
 * @author <a href="mailto:tarus@opennms.org">Tarus Balog</a>
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 * @author <a href="mailto:jeffg@opennms.org">Jeff Gehlbach</a>
 * @author <a href="mailto:cmiskell@opennms.org">Craig Miskell</a>
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a>
 * @author <a href="mailto:tarus@opennms.org">Tarus Balog</a>
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 * @author <a href="mailto:jeffg@opennms.org">Jeff Gehlbach</a>
 * @author <a href="mailto:cmiskell@opennms.org">Craig Miskell</a>
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a>
 * @author <a href="mailto:tarus@opennms.org">Tarus Balog</a>
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 * @author <a href="mailto:jeffg@opennms.org">Jeff Gehlbach</a>
 * @author <a href="mailto:cmiskell@opennms.org">Craig Miskell</a>
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a>
 * @author <a href="mailto:tarus@opennms.org">Tarus Balog</a>
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 * @author <a href="mailto:jeffg@opennms.org">Jeff Gehlbach</a>
 * @author <a href="mailto:cmiskell@opennms.org">Craig Miskell</a>
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a>
 * @author <a href="mailto:tarus@opennms.org">Tarus Balog</a>
 * @version $Id: $
 * @since 1.8.1
 */
public class ThresholdController extends AbstractController implements InitializingBean {

    private static final String SAVE_BUTTON_TITLE="Save";
    private static final String CANCEL_BUTTON_TITLE="Cancel";
    private static final String ADDFILTER_BUTTON_TITLE="Add";
    private static final String EDIT_BUTTON_TITLE="Edit";
    private static final String DELETE_BUTTON_TITLE="Delete";
    private static final String UPDATE_BUTTON_TITLE="Update";
    private static final String MOVEUP_BUTTON_TITLE="Up";
    private static final String MOVEDOWN_BUTTON_TITLE="Down";
    
    private ResourceDao m_resourceDao;
    
    private boolean eventConfChanged=false; 
    /** {@inheritDoc} */
    @Override
    protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception {
        ModelAndView modelAndView;
        ThresholdingConfigFactory.init();
        EventconfFactory.init();
        String editGroup = request.getParameter("editGroup");
        String deleteThreshold = request.getParameter("deleteThreshold");
        String editThreshold = request.getParameter("editThreshold");
        String newThreshold = request.getParameter("newThreshold");
        String finishThresholdEdit = request.getParameter("finishThresholdEdit");
        String deleteExpression = request.getParameter("deleteExpression");
        String editExpression = request.getParameter("editExpression");
        String newExpression = request.getParameter("newExpression");
        String finishExpressionEdit = request.getParameter("finishExpressionEdit");
        String groupName=request.getParameter("groupName");
       
        if(editGroup!=null) {
            modelAndView=gotoGroupEdit(groupName);
        } else if (newThreshold!=null) {
            modelAndView=gotoNewThreshold(groupName);
        } else if (editThreshold!=null) {
            String thresholdIndexString=request.getParameter("thresholdIndex");
            modelAndView=gotoEditThreshold(thresholdIndexString,groupName);
         } else if (deleteThreshold!=null) {
             String thresholdIndexString=request.getParameter("thresholdIndex");
             modelAndView=deleteThreshold(thresholdIndexString, groupName);
         } else if (finishThresholdEdit != null) {
             modelAndView=finishThresholdEdit(request);
         } else if (newExpression!=null) {
             modelAndView=gotoNewExpression(groupName);
         } else if (editExpression!=null) {
             String expressionIndexString=request.getParameter("expressionIndex");
             modelAndView=gotoEditExpression(expressionIndexString,groupName);
          } else if (deleteExpression!=null) {
              String expressionIndexString=request.getParameter("expressionIndex");
              modelAndView=deleteExpression(expressionIndexString, groupName);
          } else if (finishExpressionEdit != null) {
              modelAndView=finishExpressionEdit(request);
         
         } else {
             modelAndView=gotoGroupList();
        }
        return modelAndView;
    }
    
    private ModelAndView gotoGroupEdit(String groupName) {
        ThresholdingConfigFactory configFactory=ThresholdingConfigFactory.getInstance();
        ModelAndView modelAndView=new ModelAndView("admin/thresholds/editGroup");
        modelAndView.addObject("group",configFactory.getGroup(groupName));
        return modelAndView;
    }
    
    private void addStandardEditingBits(ModelAndView modelAndView) {
        Map<String,String> dsTypes=new LinkedHashMap<String,String>();
        dsTypes.put("node", "Node");
        dsTypes.put("if", "Interface"); // "interface" is a wrong word
        
        Collection<OnmsResourceType> resourceTypes = m_resourceDao.getResourceTypes();
        Multimap<String,String> genericDsTypes = TreeMultimap.create();
        for (OnmsResourceType resourceType : resourceTypes) {
            if (resourceType instanceof GenericIndexResourceType)
                // Put these in by label to sort them, we'll get them out in a moment
                genericDsTypes.put(resourceType.getLabel(), resourceType.getName());
        }

        // Now get the resource types out of the TreeMultimap
        for (String rtLabel : genericDsTypes.keys()) {
            Collection<String> rtNames = genericDsTypes.get(rtLabel);
            for (String rtName : rtNames) {
                if (rtNames.size() > 1)
                    dsTypes.put(rtName, rtLabel + " [" + rtName + "]");
                else
                    dsTypes.put(rtName, rtLabel);
            }
        }

        // Finally, set the sorted resource types into the model
        modelAndView.addObject("dsTypes",dsTypes);

        Collection<String> thresholdTypes=new ArrayList<String>();
        thresholdTypes.add("high");
        thresholdTypes.add("low");
        thresholdTypes.add("relativeChange");
        thresholdTypes.add("absoluteChange");
        thresholdTypes.add("rearmingAbsoluteChange");
        modelAndView.addObject("thresholdTypes",thresholdTypes);
      
        modelAndView.addObject("saveButtonTitle", SAVE_BUTTON_TITLE);
        modelAndView.addObject("cancelButtonTitle", CANCEL_BUTTON_TITLE);
        modelAndView.addObject("addFilterButtonTitle", ADDFILTER_BUTTON_TITLE);
        modelAndView.addObject("editButtonTitle", EDIT_BUTTON_TITLE);
        modelAndView.addObject("deleteButtonTitle", DELETE_BUTTON_TITLE);
        modelAndView.addObject("updateButtonTitle", UPDATE_BUTTON_TITLE);
        modelAndView.addObject("moveUpButtonTitle", MOVEUP_BUTTON_TITLE);
        modelAndView.addObject("moveDownButtonTitle", MOVEDOWN_BUTTON_TITLE);
    }
    
    private ModelAndView gotoNewThreshold(String groupName) {
        ThresholdingConfigFactory configFactory=ThresholdingConfigFactory.getInstance();
        
        Group group=configFactory.getGroup(groupName);

        //We're assuming that adding a threshold puts it at the end of the current list (i.e. that the Group implementation
        // uses a simple List structure, probably ArrayList).  We can be a bit cleverer later on and check though, so we should
        int thresholdIndex=group.getThresholdCount();
        
        //Check if last threshold has dsName. If not, we assume that is a new definition (not saved yet on thresholds.xml)
        Threshold threshold = null;
        if (thresholdIndex > 0) {
            threshold=group.getThreshold(thresholdIndex-1);
            if (threshold.getDsName() == null || threshold.getDsName().equals("")) {
            	thresholdIndex--;
            } else {
            	threshold = null;
            }
        }
        
        // create a new threshold object
        if (threshold == null) {
            threshold=new Threshold();
            //Set the two default values which need to be set for the UI to work properly
            threshold.setDsType("node");
            threshold.setType("high"); 
            threshold.setTrigger(1); //Default to 1 - 0 will give an error, so we may as well be helpful
            group.addThreshold(threshold);
        }
        
        //Double check the guess index, just in case:
        if(threshold!=group.getThreshold(thresholdIndex)) {
            //Ok, our guesses on indexing were completely wrong.  Failover and check each threshold in the group
            for(int i=0; i<group.getThresholdCount(); i++) {
                if(threshold==group.getThreshold(i)) {
                    thresholdIndex=i;
                    break; //out of the for loop
                }
            }
        }
        
        ModelAndView modelAndView;
        modelAndView=new ModelAndView("admin/thresholds/editThreshold");
        modelAndView.addObject("threshold",threshold);
        modelAndView.addObject("thresholdIndex", thresholdIndex );
        modelAndView.addObject("groupName", groupName);
        modelAndView.addObject("isNew", true);
        
        addStandardEditingBits(modelAndView);

        return modelAndView;
    }
  
    private ModelAndView gotoNewExpression(String groupName) {
        ThresholdingConfigFactory configFactory=ThresholdingConfigFactory.getInstance();
        
        Group group=configFactory.getGroup(groupName);

        //We're assuming that adding a expression puts it at the end of the current list (i.e. that the Group implementation
        // uses a simple List structure, probably ArrayList).  We can be a bit cleverer later on and check though, so we should
        int expressionIndex=group.getExpressionCount();
        
        //Check if last expression has expression def. If not, we assume that is a new definition (not saved yet on thresholds.xml)
        Expression expression = null;
        if (expressionIndex > 0) {
            expression = group.getExpression(expressionIndex-1);
            if (expression.getExpression() == null || expression.getExpression().equals("")) {
	            expressionIndex--;
            } else {
            	expression = null;
            }
        }
        
        // create a new expression object
        if (expression == null) {
            expression=new Expression();
            //Set the two default values which need to be set for the UI to work properly
            expression.setDsType("node");
            expression.setType("high");
            expression.setTrigger(1); //Default to 1 - 0 will give an error, so we may as well be helpful
            group.addExpression(expression);
        }
    
        //Double check the guess index, just in case:
        if(expression!=group.getExpression(expressionIndex)) {
            //Ok, our guesses on indexing were completely wrong.  Failover and check each threshold in the group
            for(int i=0; i<group.getExpressionCount(); i++) {
                if(expression==group.getExpression(i)) {
                    expressionIndex=i;
                    break; //out of the for loop
                }
            }
        }
        
        ModelAndView modelAndView;
        modelAndView=new ModelAndView("admin/thresholds/editExpression");
        modelAndView.addObject("expression",expression);
        modelAndView.addObject("expressionIndex", expressionIndex );
        modelAndView.addObject("groupName", groupName);
        modelAndView.addObject("isNew", true);
        
        addStandardEditingBits(modelAndView);

        return modelAndView;
    }
    
    private ModelAndView gotoEditThreshold(String thresholdIndexString, String groupName) throws ServletException {
        ThresholdingConfigFactory configFactory=ThresholdingConfigFactory.getInstance();
        ModelAndView modelAndView;
        if(thresholdIndexString==null) {
            throw new ServletException("thresholdIndex parameter required to edit a threshold");
        }
        int thresholdIndex=WebSecurityUtils.safeParseInt(thresholdIndexString);

        Threshold threshold=configFactory.getGroup(groupName).getThreshold(thresholdIndex);
        modelAndView=new ModelAndView("admin/thresholds/editThreshold");
        
        modelAndView.addObject("threshold", threshold);
        modelAndView.addObject("thresholdIndex", thresholdIndex);
        modelAndView.addObject("groupName", groupName);
        modelAndView.addObject("isNew", false);
        addStandardEditingBits(modelAndView);
        
        return modelAndView;
    }

    private void moveThresholdFilter(Basethresholddef threshold, int oldPos, int newPos) {
        if (newPos >= 0 && newPos < threshold.getResourceFilterCount()) {
            ResourceFilter oldFilter = (ResourceFilter)threshold.getResourceFilterCollection().get(oldPos);
            ResourceFilter newFilter = (ResourceFilter)threshold.getResourceFilterCollection().get(newPos);
            threshold.getResourceFilterCollection().set(newPos, oldFilter);
            threshold.getResourceFilterCollection().set(oldPos, newFilter);
        }
    }
    
    @SuppressWarnings("unchecked")
    private List<ResourceFilter> getFilterList(HttpServletRequest request, boolean create) {
        return (List<ResourceFilter>)request.getSession(create).getAttribute("savedFilters");
    }

    private void setFilterList(HttpServletRequest request, List<ResourceFilter> filters) {
        if (filters == null) {
            request.getSession(false).removeAttribute("savedFilters");
        } else {
            request.getSession(false).setAttribute("savedFilters", filters);
        }
    }

    private ModelAndView finishThresholdFilterEdit(HttpServletRequest request, Basethresholddef threshold) throws ServletException {
        
        boolean isExpression = threshold instanceof Expression;
        
        int thresholdIndex;
        if (isExpression) {
            thresholdIndex = WebSecurityUtils.safeParseInt(request.getParameter("expressionIndex"));
        } else {
            thresholdIndex = WebSecurityUtils.safeParseInt(request.getParameter("thresholdIndex"));

        }

        ModelAndView modelAndView;        
        if (isExpression) {
            modelAndView = new ModelAndView("admin/thresholds/editExpression");
        } else {
            modelAndView = new ModelAndView("admin/thresholds/editThreshold");
        }
        
        List<ResourceFilter> saved = getFilterList(request, true);
        if (saved == null || saved.size() == 0) {
            saved = new ArrayList<ResourceFilter>(threshold.getResourceFilterCollection());
            setFilterList(request, saved);
        }

        String stringIndex = request.getParameter("filterSelected");
        int filterIndex = stringIndex != null && !stringIndex.equals("") ? WebSecurityUtils.safeParseInt(stringIndex) - 1 : 0;

        /*
         * Save Threshold Filters on HTTP Session in order to restore the original list if user clicks on "Cancel"
         */
        String submitAction = request.getParameter("submitAction");
        if (ADDFILTER_BUTTON_TITLE.equals(submitAction)) {
            String field = request.getParameter("filterField");
            String content = request.getParameter("filterRegexp");
            if (field != null && !field.equals("") && content != null && !content.equals("")) {
                ResourceFilter filter = new ResourceFilter();
                filter.setField(field);
                filter.setContent(content);
                threshold.addResourceFilter(filter);
            }
        } else if (DELETE_BUTTON_TITLE.equals(submitAction)) {
            threshold.getResourceFilterCollection().remove(filterIndex);
        } else if (EDIT_BUTTON_TITLE.equals(submitAction)) {
            modelAndView.addObject("filterSelected", request.getParameter("filterSelected"));
        } else if (UPDATE_BUTTON_TITLE.equals(submitAction)) {
            ResourceFilter filter = (ResourceFilter)threshold.getResourceFilterCollection().get(filterIndex);
            filter.setField(request.getParameter("updateFilterField"));
            filter.setContent(request.getParameter("updateFilterRegexp"));            
        } else if (MOVEUP_BUTTON_TITLE.equals(submitAction)) {
            moveThresholdFilter(threshold, filterIndex, filterIndex - 1);
        } else if (MOVEDOWN_BUTTON_TITLE.equals(submitAction)) {
            moveThresholdFilter(threshold, filterIndex, filterIndex + 1);
        }
        
        commonFinishEdit(request, threshold);
        if (isExpression) {
        	((Expression)threshold).setExpression(request.getParameter("expression"));        	
        } else {
        	((Threshold)threshold).setDsName(request.getParameter("dsName"));
        }
        
        String isNew=request.getParameter("isNew");
        if("true".equals(isNew))
            modelAndView.addObject("isNew", true);

        if (isExpression) {
        	modelAndView.addObject("expression", threshold);
            modelAndView.addObject("expressionIndex", thresholdIndex);
        } else {
        	modelAndView.addObject("threshold", threshold);
            modelAndView.addObject("thresholdIndex", thresholdIndex);
        }
        modelAndView.addObject("groupName", request.getParameter("groupName"));
        addStandardEditingBits(modelAndView);
        
        return modelAndView;
    }

    private ModelAndView gotoEditExpression(String expressionIndexString, String groupName) throws ServletException {
        ThresholdingConfigFactory configFactory=ThresholdingConfigFactory.getInstance();
        ModelAndView modelAndView;
        if(expressionIndexString==null) {
            throw new ServletException("expressionIndex parameter required to edit a threshold");
        }
        int expressionIndex=WebSecurityUtils.safeParseInt(expressionIndexString);

        Expression expression=configFactory.getGroup(groupName).getExpression(expressionIndex);
        modelAndView=new ModelAndView("admin/thresholds/editExpression");
        
        modelAndView.addObject("expression", expression);
        modelAndView.addObject("expressionIndex", expressionIndex);
        modelAndView.addObject("groupName", groupName);
        modelAndView.addObject("isNew", false);
        addStandardEditingBits(modelAndView);
        
        return modelAndView;
    }
    
    private EventBuilder createEventBuilder(String uei) {
        EventBuilder ebldr = new EventBuilder(uei, "Web UI");
        try {
            ebldr.setHost(InetAddress.getLocalHost().getHostName());
        } catch (UnknownHostException uhE) {
            ebldr.setHost("unresolved.host");
        }
        return ebldr;
    }
    private void sendNotifEvent(Event event) throws ServletException {
        try {
            Util.createEventProxy().send(event);
        } catch (Exception e) {
            throw new ServletException("Could not send event " + event.getUei(), e);
        }

    }
    private void saveChanges() throws ServletException {
        ThresholdingConfigFactory configFactory=ThresholdingConfigFactory.getInstance();
        try {
            configFactory.saveCurrent();
            EventBuilder ebldr = createEventBuilder(EventConstants.RELOAD_DAEMON_CONFIG_UEI);
            ebldr.addParam(EventConstants.PARM_DAEMON_NAME, "Threshd");
            ebldr.addParam(EventConstants.PARM_CONFIG_FILE_NAME, "thresholds.xml");
            sendNotifEvent(ebldr.getEvent());
        } catch (Exception e) {
            throw new ServletException("Could not save the changes to the threshold because "+e.getMessage(),e);
        }
        
        if(eventConfChanged) {
            try {
                EventconfFactory.getInstance().saveCurrent();
                sendNotifEvent(createEventBuilder(EventConstants.EVENTSCONFIG_CHANGED_EVENT_UEI).getEvent());
            } catch (Exception e) {
                throw new ServletException("Could not save the changes to the event configuration because "+e.getMessage(),e);
            }
            eventConfChanged=false;
        }

    }
    private ModelAndView deleteThreshold(String thresholdIndexString, String groupName) throws ServletException {
        ThresholdingConfigFactory configFactory=ThresholdingConfigFactory.getInstance();
        ModelAndView modelAndView;
        if(thresholdIndexString==null) {
            throw new ServletException("thresholdIndex parameter required to delete a threshold");
        }
        int thresholdIndex=WebSecurityUtils.safeParseInt(thresholdIndexString);
        Group group=configFactory.getGroup(groupName);
        group.removeThreshold(group.getThreshold(thresholdIndex));
        //and setup the group view again
        modelAndView=new ModelAndView("admin/thresholds/editGroup");
        modelAndView.addObject("group",configFactory.getGroup(groupName));
        saveChanges();
        return modelAndView;
    }
    
    private ModelAndView deleteExpression(String expressionIndexString, String groupName) throws ServletException {
        ThresholdingConfigFactory configFactory=ThresholdingConfigFactory.getInstance();
        ModelAndView modelAndView;
        if(expressionIndexString==null) {
            throw new ServletException("expressionIndex parameter required to delete a threshold");
        }
        int expressionIndex=WebSecurityUtils.safeParseInt(expressionIndexString);
        Group group=configFactory.getGroup(groupName);
        group.removeExpression(group.getExpression(expressionIndex));
        saveChanges();
        
        //and setup the group view again
        modelAndView=new ModelAndView("admin/thresholds/editGroup");
        modelAndView.addObject("group",configFactory.getGroup(groupName));
        return modelAndView;
    }
    private void commonFinishEdit(HttpServletRequest request, Basethresholddef baseDef) {
        String dsLabel=request.getParameter("dsLabel");
        if(dsLabel==null || "".equals(dsLabel)) {
            baseDef.setDsLabel(null); //Must set null in correct circumstances - empty string isn't quite the same thing
        } else {
            baseDef.setDsLabel(dsLabel);
        }
       
        String triggeredUEI=request.getParameter("triggeredUEI");
        if(triggeredUEI==null || "".equals(triggeredUEI)) {
            baseDef.setTriggeredUEI(null); //Must set null in correct circumstances - empty string isn't quite the same thing
        } else {
            baseDef.setTriggeredUEI(triggeredUEI);
            this.ensureUEIInEventConf(triggeredUEI, "exceeded");
        }
  
        String rearmedUEI=request.getParameter("rearmedUEI");
        if(rearmedUEI==null || "".equals(rearmedUEI)) {
            baseDef.setRearmedUEI(null); //Must set null in correct circumstances - empty string isn't quite the same thing
        } else {
            baseDef.setRearmedUEI(rearmedUEI);
            this.ensureUEIInEventConf(rearmedUEI, "rearmed");
        }
        baseDef.setDsType(request.getParameter("dsType"));
        baseDef.setType(request.getParameter("type"));
        baseDef.setRearm(WebSecurityUtils.safeParseDouble(request.getParameter("rearm")));
        baseDef.setTrigger(WebSecurityUtils.safeParseInt(request.getParameter("trigger")));
        baseDef.setValue(WebSecurityUtils.safeParseDouble(request.getParameter("value")));
 
    }
    
    private void ensureUEIInEventConf(String uei, String typeDesc) {
        List<org.opennms.netmgt.xml.eventconf.Event> eventsForUEI=EventconfFactory.getInstance().getEvents(uei);
        if(eventsForUEI==null || eventsForUEI.size()==0) {
            //UEI doesn't exist.  Add it
            org.opennms.netmgt.xml.eventconf.Event event=new org.opennms.netmgt.xml.eventconf.Event();
            event.setUei(uei);
            event.setEventLabel("User-defined threshold event "+uei);
            event.setDescr("Threshold "+typeDesc+" for %service% datasource " +
                        "%parm[ds]% on interface %interface%, parms: %parm[all]");
            Logmsg logmsg=new Logmsg();
            logmsg.setDest("logndisplay");
            logmsg.setContent("Threshold "+typeDesc+" for %service% datasource %parm[ds]% on interface %interface%, parms: %parm[all]%");
            event.setLogmsg(logmsg);
            event.setSeverity("Warning");
            EventconfFactory.getInstance().addEventToProgrammaticStore(event);
            eventConfChanged=true;
        }
    }

    private ModelAndView finishThresholdEdit(HttpServletRequest request) throws ServletException {
        ThresholdingConfigFactory configFactory=ThresholdingConfigFactory.getInstance();
        ModelAndView modelAndView;
        String groupName=request.getParameter("groupName");
        String submitAction=request.getParameter("submitAction");
        Group group=configFactory.getGroup(groupName);
        String thresholdIndexString=request.getParameter("thresholdIndex");
        if(thresholdIndexString==null) {
            throw new ServletException("thresholdIndex parameter required to delete a threshold");
        }
        int thresholdIndex=WebSecurityUtils.safeParseInt(thresholdIndexString);
        Threshold threshold=group.getThreshold(thresholdIndex);
        
        if(SAVE_BUTTON_TITLE.equals(submitAction)) {
            this.commonFinishEdit(request, threshold);
            String dsName = request.getParameter("dsName");
            if (dsName == null || dsName.equals("")) {
            	throw new ServletException("ds-name cannot be null or empty string");
            }
            threshold.setDsName(request.getParameter("dsName"));
            saveChanges();
         } else if (CANCEL_BUTTON_TITLE.equals(submitAction)) {
            String isNew=request.getParameter("isNew");
            if("true".equals(isNew)) {
                //It was a new Threshold, but the user hit cancel.  Remove the new threshold from the group
                group.removeThreshold(threshold);
            } else {
                List<ResourceFilter> filters = getFilterList(request, false);
                if (filters != null)
                	threshold.setResourceFilter(filters);
            }
        } else {
            return finishThresholdFilterEdit(request, threshold);
        }
        // Remove Filters from Session
        setFilterList(request, null);
        
        //and got back to the editGroup page
        modelAndView=new ModelAndView("admin/thresholds/editGroup");
        modelAndView.addObject("group",configFactory.getGroup(groupName));
        return modelAndView;
    }
    
    private ModelAndView finishExpressionEdit(HttpServletRequest request) throws ServletException {
        ThresholdingConfigFactory configFactory=ThresholdingConfigFactory.getInstance();
        ModelAndView modelAndView;
        String groupName=request.getParameter("groupName");
        String submitAction=request.getParameter("submitAction");
        Group group=configFactory.getGroup(groupName);
        String expressionIndexString=request.getParameter("expressionIndex");
        if(expressionIndexString==null) {
            throw new ServletException("expressionIndex parameter required to delete a threshold");
        }
        int expressionIndex=WebSecurityUtils.safeParseInt(expressionIndexString);
        Expression expression=group.getExpression(expressionIndex);
        
        if(SAVE_BUTTON_TITLE.equals(submitAction)) {
            this.commonFinishEdit(request, expression);
            String expDef = request.getParameter("expression");
            if (expDef == null || expDef.equals("")) {
            	throw new ServletException("expression content cannot be null or empty string");
            }
            expression.setExpression(expDef);
            saveChanges();
         } else if (CANCEL_BUTTON_TITLE.equals(submitAction)) {
            String isNew=request.getParameter("isNew");
            if("true".equals(isNew)) {
                //It was a new Threshold, but the user hit cancel.  Remove the new threshold from the group
                group.removeExpression(expression);
            } else {
                List<ResourceFilter> filters = getFilterList(request, false);
                if (filters != null)
                	expression.setResourceFilter(filters);
            }
        } else {
            return finishThresholdFilterEdit(request, expression);
        }
        // Remove Filters from Session
        setFilterList(request, null);

        //and got back to the editGroup page
        modelAndView=new ModelAndView("admin/thresholds/editGroup");
        modelAndView.addObject("group",configFactory.getGroup(groupName));
        return modelAndView;
    }   
    private ModelAndView gotoGroupList() throws ServletException {
        //Always reload to get a consistent view of the thresholds before we start editing.  
        //Otherwise we'll be dealing with questions on the mailing lists for the rest of our lives
        try {
             ThresholdingConfigFactory.reload();
        } catch (Exception e) {
            throw new ServletException("Could not reload ThresholdingConfigFactory because "+e.getMessage(), e);
        }
        ThresholdingConfigFactory configFactory=ThresholdingConfigFactory.getInstance();
        ModelAndView modelAndView= new ModelAndView("admin/thresholds/list");

        Map<String, Group> groupMap=new HashMap<String,Group>();
        for(String aName:configFactory.getGroupNames()) {
            groupMap.put(aName, configFactory.getGroup(aName));
        }

        modelAndView.addObject("groupMap", groupMap);
        return modelAndView;
    }
 
    
    /**
     * <p>afterPropertiesSet</p>
     *
     * @throws java.lang.Exception if any.
     */
    public void afterPropertiesSet() throws Exception {
        //Check all properties set (see example if needed)
        /*if (m_resourceService == null) {
            throw new IllegalStateException(
                                            "property resourceService must be set");
        }*/

    }

    /**
     * <p>setResourceDao</p>
     *
     * @param resourceDao a {@link org.opennms.netmgt.dao.ResourceDao} object.
     */
    public void setResourceDao(ResourceDao resourceDao) {
        m_resourceDao = resourceDao;
    }

}

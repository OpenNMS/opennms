/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2007-2014 The OpenNMS Group, Inc.
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

package org.opennms.web.controller.admin.thresholds;

import com.google.common.collect.Multimap;
import com.google.common.collect.TreeMultimap;

import org.opennms.core.utils.InetAddressUtils;
import org.opennms.core.utils.WebSecurityUtils;
import org.opennms.netmgt.config.ThresholdingConfigFactory;
import org.opennms.netmgt.config.api.EventConfDao;
import org.opennms.netmgt.config.threshd.*;
import org.opennms.netmgt.dao.api.ResourceDao;
import org.opennms.netmgt.dao.support.GenericIndexResourceType;
import org.opennms.netmgt.events.api.EventConstants;
import org.opennms.netmgt.model.OnmsResourceType;
import org.opennms.netmgt.model.events.EventBuilder;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.netmgt.xml.eventconf.AlarmData;
import org.opennms.netmgt.xml.eventconf.Logmsg;
import org.opennms.web.api.Util;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.util.*;

/**
 * <p>ThresholdController class.</p>
 *
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 * @author <a href="mailto:jeffg@opennms.org">Jeff Gehlbach</a>
 * @author <a href="mailto:cmiskell@opennms.org">Craig Miskell</a>
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a>
 * @author <a href="mailto:tarus@opennms.org">Tarus Balog</a>
 */
public class ThresholdController extends AbstractController implements InitializingBean {

    private static final String SAVE_BUTTON_TITLE = "Save";
    private static final String CANCEL_BUTTON_TITLE = "Cancel";
    private static final String ADDFILTER_BUTTON_TITLE = "Add";
    private static final String EDIT_BUTTON_TITLE = "Edit";
    private static final String DELETE_BUTTON_TITLE = "Delete";
    private static final String UPDATE_BUTTON_TITLE = "Update";
    private static final String MOVEUP_BUTTON_TITLE = "Up";
    private static final String MOVEDOWN_BUTTON_TITLE = "Down";

    private ResourceDao m_resourceDao;

    private EventConfDao m_eventConfDao;

    private boolean eventConfChanged = false;

    /**
     * {@inheritDoc}
     */
    @Override
    protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception {
        ModelAndView modelAndView;
        ThresholdingConfigFactory.init();
        String editGroup = request.getParameter("editGroup");
        String deleteThreshold = request.getParameter("deleteThreshold");
        String editThreshold = request.getParameter("editThreshold");
        String newThreshold = request.getParameter("newThreshold");
        String finishThresholdEdit = request.getParameter("finishThresholdEdit");
        String deleteExpression = request.getParameter("deleteExpression");
        String editExpression = request.getParameter("editExpression");
        String newExpression = request.getParameter("newExpression");
        String finishExpressionEdit = request.getParameter("finishExpressionEdit");
        String groupName = request.getParameter("groupName");
        String reloadThreshdConfig = request.getParameter("reloadThreshdConfig");

        if (editGroup != null) {
            modelAndView = gotoGroupEdit(groupName);
        }
        else if (newThreshold != null) {
            modelAndView = gotoNewThreshold(groupName);
        }
        else if (editThreshold != null) {
            String thresholdIndexString = request.getParameter("thresholdIndex");
            modelAndView = gotoEditThreshold(thresholdIndexString, groupName);
        }
        else if (deleteThreshold != null) {
            String thresholdIndexString = request.getParameter("thresholdIndex");
            modelAndView = deleteThreshold(thresholdIndexString, groupName);
        }
        else if (finishThresholdEdit != null) {
            modelAndView = finishThresholdEdit(request);
        }
        else if (newExpression != null) {
            modelAndView = gotoNewExpression(groupName);
        }
        else if (editExpression != null) {
            String expressionIndexString = request.getParameter("expressionIndex");
            modelAndView = gotoEditExpression(expressionIndexString, groupName);
        }
        else if (deleteExpression != null) {
            String expressionIndexString = request.getParameter("expressionIndex");
            modelAndView = deleteExpression(expressionIndexString, groupName);
        }
        else if (finishExpressionEdit != null) {
            modelAndView = finishExpressionEdit(request);
        }
        else if (reloadThreshdConfig != null) {
            modelAndView = reloadThreshdConfig();

        }
        else {
            modelAndView = gotoGroupList();
        }
        return modelAndView;
    }

    private ModelAndView gotoGroupEdit(String groupName) {
        ThresholdingConfigFactory configFactory = ThresholdingConfigFactory.getInstance();
        ModelAndView modelAndView = new ModelAndView("admin/thresholds/editGroup");
        modelAndView.addObject("group", configFactory.getGroup(groupName));
        return modelAndView;
    }

    private void addStandardEditingBits(ModelAndView modelAndView) {
        Map<String, String> dsTypes = new LinkedHashMap<String, String>();
        dsTypes.put("node", "Node");
        dsTypes.put("if", "Interface"); // "interface" is a wrong word

        Collection<OnmsResourceType> resourceTypes = m_resourceDao.getResourceTypes();
        Multimap<String, String> genericDsTypes = TreeMultimap.create();
        for (OnmsResourceType resourceType : resourceTypes) {
            if (resourceType instanceof GenericIndexResourceType)
            // Put these in by label to sort them, we'll get them out in a moment
            {
                genericDsTypes.put(resourceType.getLabel(), resourceType.getName());
            }
        }

        // Now get the resource types out of the TreeMultimap
        for (String rtLabel : genericDsTypes.keys()) {
            Collection<String> rtNames = genericDsTypes.get(rtLabel);
            for (String rtName : rtNames) {
                if (rtNames.size() > 1) {
                    dsTypes.put(rtName, rtLabel + " [" + rtName + "]");
                }
                else {
                    dsTypes.put(rtName, rtLabel);
                }
            }
        }

        // Finally, set the sorted resource types into the model
        modelAndView.addObject("dsTypes", dsTypes);

        Collection<String> thresholdTypes = new ArrayList<String>();
        thresholdTypes.add("high");
        thresholdTypes.add("low");
        thresholdTypes.add("relativeChange");
        thresholdTypes.add("absoluteChange");
        thresholdTypes.add("rearmingAbsoluteChange");
        modelAndView.addObject("thresholdTypes", thresholdTypes);

        Collection<String> filterOperators = new ArrayList<String>();
        filterOperators.add("and");
        filterOperators.add("or");
        modelAndView.addObject("filterOperators", filterOperators);

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
        ThresholdingConfigFactory configFactory = ThresholdingConfigFactory.getInstance();

        Group group = configFactory.getGroup(groupName);

        //We're assuming that adding a threshold puts it at the end of the current list (i.e. that the Group implementation
        // uses a simple List structure, probably ArrayList).  We can be a bit cleverer later on and check though, so we should
        int thresholdIndex = group.getThresholdCount();

        //Check if last threshold has dsName. If not, we assume that is a new definition (not saved yet on thresholds.xml)
        Threshold threshold = null;
        if (thresholdIndex > 0) {
            threshold = group.getThreshold(thresholdIndex - 1);
            if (threshold.getDsName() == null || threshold.getDsName().equals("")) {
                thresholdIndex--;
            }
            else {
                threshold = null;
            }
        }

        // create a new threshold object
        if (threshold == null) {
            threshold = new Threshold();
            //Set the two default values which need to be set for the UI to work properly
            threshold.setDsType("node");
            threshold.setType("high");
            threshold.setTrigger(1); //Default to 1 - 0 will give an error, so we may as well be helpful
            group.addThreshold(threshold);
        }

        //Double check the guess index, just in case:
        if (threshold != group.getThreshold(thresholdIndex)) {
            //Ok, our guesses on indexing were completely wrong.  Failover and check each threshold in the group
            for (int i = 0; i < group.getThresholdCount(); i++) {
                if (threshold == group.getThreshold(i)) {
                    thresholdIndex = i;
                    break; //out of the for loop
                }
            }
        }

        ModelAndView modelAndView;
        modelAndView = new ModelAndView("admin/thresholds/editThreshold");
        modelAndView.addObject("threshold", threshold);
        modelAndView.addObject("thresholdIndex", thresholdIndex);
        modelAndView.addObject("groupName", groupName);
        modelAndView.addObject("isNew", true);

        addStandardEditingBits(modelAndView);

        return modelAndView;
    }

    private ModelAndView gotoNewExpression(String groupName) {
        ThresholdingConfigFactory configFactory = ThresholdingConfigFactory.getInstance();

        Group group = configFactory.getGroup(groupName);

        //We're assuming that adding a expression puts it at the end of the current list (i.e. that the Group implementation
        // uses a simple List structure, probably ArrayList).  We can be a bit cleverer later on and check though, so we should
        int expressionIndex = group.getExpressionCount();

        //Check if last expression has expression def. If not, we assume that is a new definition (not saved yet on thresholds.xml)
        Expression expression = null;
        if (expressionIndex > 0) {
            expression = group.getExpression(expressionIndex - 1);
            if (expression.getExpression() == null || expression.getExpression().equals("")) {
                expressionIndex--;
            }
            else {
                expression = null;
            }
        }

        // create a new expression object
        if (expression == null) {
            expression = new Expression();
            //Set the two default values which need to be set for the UI to work properly
            expression.setDsType("node");
            expression.setType("high");
            expression.setTrigger(1); //Default to 1 - 0 will give an error, so we may as well be helpful
            group.addExpression(expression);
        }

        //Double check the guess index, just in case:
        if (expression != group.getExpression(expressionIndex)) {
            //Ok, our guesses on indexing were completely wrong.  Failover and check each threshold in the group
            for (int i = 0; i < group.getExpressionCount(); i++) {
                if (expression == group.getExpression(i)) {
                    expressionIndex = i;
                    break; //out of the for loop
                }
            }
        }

        ModelAndView modelAndView;
        modelAndView = new ModelAndView("admin/thresholds/editExpression");
        modelAndView.addObject("expression", expression);
        modelAndView.addObject("expressionIndex", expressionIndex);
        modelAndView.addObject("groupName", groupName);
        modelAndView.addObject("isNew", true);

        addStandardEditingBits(modelAndView);

        return modelAndView;
    }

    private ModelAndView gotoEditThreshold(String thresholdIndexString, String groupName) throws ServletException {
        ThresholdingConfigFactory configFactory = ThresholdingConfigFactory.getInstance();
        ModelAndView modelAndView;
        if (thresholdIndexString == null) {
            throw new ServletException("thresholdIndex parameter required to edit a threshold");
        }
        int thresholdIndex = WebSecurityUtils.safeParseInt(thresholdIndexString);

        Threshold threshold = configFactory.getGroup(groupName).getThreshold(thresholdIndex);
        modelAndView = new ModelAndView("admin/thresholds/editThreshold");

        modelAndView.addObject("threshold", threshold);
        modelAndView.addObject("thresholdIndex", thresholdIndex);
        modelAndView.addObject("groupName", groupName);
        modelAndView.addObject("isNew", false);
        addStandardEditingBits(modelAndView);

        return modelAndView;
    }

    private void moveThresholdFilter(Basethresholddef threshold, int oldPos, int newPos) {
        if (newPos >= 0 && newPos < threshold.getResourceFilterCount()) {
            ResourceFilter oldFilter = (ResourceFilter) threshold.getResourceFilterCollection().get(oldPos);
            ResourceFilter newFilter = (ResourceFilter) threshold.getResourceFilterCollection().get(newPos);
            threshold.getResourceFilterCollection().set(newPos, oldFilter);
            threshold.getResourceFilterCollection().set(oldPos, newFilter);
        }
    }

    @SuppressWarnings("unchecked")
    private List<ResourceFilter> getFilterList(HttpServletRequest request, boolean create) {
        return (List<ResourceFilter>) request.getSession(create).getAttribute("savedFilters");
    }

    private void setFilterList(HttpServletRequest request, List<ResourceFilter> filters) {
        if (filters == null) {
            request.getSession(false).removeAttribute("savedFilters");
        }
        else {
            request.getSession(false).setAttribute("savedFilters", filters);
        }
    }

    private ModelAndView finishThresholdFilterEdit(HttpServletRequest request, Basethresholddef threshold) throws ServletException {

        boolean isExpression = threshold instanceof Expression;

        int thresholdIndex;
        if (isExpression) {
            thresholdIndex = WebSecurityUtils.safeParseInt(request.getParameter("expressionIndex"));
        }
        else {
            thresholdIndex = WebSecurityUtils.safeParseInt(request.getParameter("thresholdIndex"));

        }

        ModelAndView modelAndView;
        if (isExpression) {
            modelAndView = new ModelAndView("admin/thresholds/editExpression");
        }
        else {
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
        }
        else if (DELETE_BUTTON_TITLE.equals(submitAction)) {
            threshold.getResourceFilterCollection().remove(filterIndex);
        }
        else if (EDIT_BUTTON_TITLE.equals(submitAction)) {
            modelAndView.addObject("filterSelected", request.getParameter("filterSelected"));
        }
        else if (UPDATE_BUTTON_TITLE.equals(submitAction)) {
            ResourceFilter filter = (ResourceFilter) threshold.getResourceFilterCollection().get(filterIndex);
            filter.setField(request.getParameter("updateFilterField"));
            filter.setContent(request.getParameter("updateFilterRegexp"));
        }
        else if (MOVEUP_BUTTON_TITLE.equals(submitAction)) {
            moveThresholdFilter(threshold, filterIndex, filterIndex - 1);
        }
        else if (MOVEDOWN_BUTTON_TITLE.equals(submitAction)) {
            moveThresholdFilter(threshold, filterIndex, filterIndex + 1);
        }

        commonFinishEdit(request, threshold);
        if (isExpression) {
            ((Expression) threshold).setExpression(request.getParameter("expression"));
        }
        else {
            ((Threshold) threshold).setDsName(request.getParameter("dsName"));
        }

        String isNew = request.getParameter("isNew");
        if ("true".equals(isNew)) {
            modelAndView.addObject("isNew", true);
        }

        if (isExpression) {
            modelAndView.addObject("expression", threshold);
            modelAndView.addObject("expressionIndex", thresholdIndex);
        }
        else {
            modelAndView.addObject("threshold", threshold);
            modelAndView.addObject("thresholdIndex", thresholdIndex);
        }
        modelAndView.addObject("groupName", request.getParameter("groupName"));
        addStandardEditingBits(modelAndView);

        return modelAndView;
    }

    private ModelAndView gotoEditExpression(String expressionIndexString, String groupName) throws ServletException {
        ThresholdingConfigFactory configFactory = ThresholdingConfigFactory.getInstance();
        ModelAndView modelAndView;
        if (expressionIndexString == null) {
            throw new ServletException("expressionIndex parameter required to edit a threshold");
        }
        int expressionIndex = WebSecurityUtils.safeParseInt(expressionIndexString);

        Expression expression = configFactory.getGroup(groupName).getExpression(expressionIndex);
        modelAndView = new ModelAndView("admin/thresholds/editExpression");

        modelAndView.addObject("expression", expression);
        modelAndView.addObject("expressionIndex", expressionIndex);
        modelAndView.addObject("groupName", groupName);
        modelAndView.addObject("isNew", false);
        addStandardEditingBits(modelAndView);

        return modelAndView;
    }

    private EventBuilder createEventBuilder(String uei) {
        EventBuilder ebldr = new EventBuilder(uei, "Web UI");
        ebldr.setHost(InetAddressUtils.getLocalHostName());
        return ebldr;
    }

    private void sendNotifEvent(Event event) throws ServletException {
        try {
            Util.createEventProxy().send(event);
        } catch (Throwable e) {
            throw new ServletException("Could not send event " + event.getUei(), e);
        }

    }

    private void saveChanges() throws ServletException {
        ThresholdingConfigFactory configFactory = ThresholdingConfigFactory.getInstance();
        try {
            configFactory.saveCurrent();
            EventBuilder ebldr = createEventBuilder(EventConstants.RELOAD_DAEMON_CONFIG_UEI);
            ebldr.addParam(EventConstants.PARM_DAEMON_NAME, "Threshd");
            ebldr.addParam(EventConstants.PARM_CONFIG_FILE_NAME, "thresholds.xml");
            sendNotifEvent(ebldr.getEvent());
        } catch (Throwable e) {
            throw new ServletException("Could not save the changes to the threshold because " + e.getMessage(), e);
        }

        if (eventConfChanged) {
            try {
                m_eventConfDao.saveCurrent();
                sendNotifEvent(createEventBuilder(EventConstants.EVENTSCONFIG_CHANGED_EVENT_UEI).getEvent());
            } catch (Throwable e) {
                throw new ServletException("Could not save the changes to the event configuration because " + e.getMessage(), e);
            }
            eventConfChanged = false;
        }

    }

    private ModelAndView deleteThreshold(String thresholdIndexString, String groupName) throws ServletException {
        ThresholdingConfigFactory configFactory = ThresholdingConfigFactory.getInstance();
        ModelAndView modelAndView;
        if (thresholdIndexString == null) {
            throw new ServletException("thresholdIndex parameter required to delete a threshold");
        }
        int thresholdIndex = WebSecurityUtils.safeParseInt(thresholdIndexString);
        Group group = configFactory.getGroup(groupName);
        group.removeThreshold(group.getThreshold(thresholdIndex));
        //and setup the group view again
        modelAndView = new ModelAndView("admin/thresholds/editGroup");
        modelAndView.addObject("group", configFactory.getGroup(groupName));
        saveChanges();
        return modelAndView;
    }

    private ModelAndView reloadThreshdConfig() throws ServletException {
        try {
            EventBuilder ebldr = createEventBuilder(EventConstants.RELOAD_DAEMON_CONFIG_UEI);
            ebldr.addParam(EventConstants.PARM_DAEMON_NAME, "Threshd");
            ebldr.addParam(EventConstants.PARM_CONFIG_FILE_NAME, "threshd-configuration.xml");
            sendNotifEvent(ebldr.getEvent());
        } catch (Throwable e) {
            throw new ServletException("Could not reload threshd-configuration.xml because " + e.getMessage(), e);
        }
        return gotoGroupList();
    }

    private ModelAndView deleteExpression(String expressionIndexString, String groupName) throws ServletException {
        ThresholdingConfigFactory configFactory = ThresholdingConfigFactory.getInstance();
        ModelAndView modelAndView;
        if (expressionIndexString == null) {
            throw new ServletException("expressionIndex parameter required to delete a threshold expression");
        }
        int expressionIndex = WebSecurityUtils.safeParseInt(expressionIndexString);
        Group group = configFactory.getGroup(groupName);
        group.removeExpression(group.getExpression(expressionIndex));
        saveChanges();

        //and setup the group view again
        modelAndView = new ModelAndView("admin/thresholds/editGroup");
        modelAndView.addObject("group", configFactory.getGroup(groupName));
        return modelAndView;
    }

    private void commonFinishEdit(HttpServletRequest request, Basethresholddef baseDef) {
        String dsLabel = request.getParameter("dsLabel");
        if (dsLabel == null || "".equals(dsLabel)) {
            baseDef.setDsLabel(null); //Must set null in correct circumstances - empty string isn't quite the same thing
        }
        else {
            baseDef.setDsLabel(dsLabel);
        }

        String description = request.getParameter("description");
        if (description == null || "".equals(description)) {
            baseDef.setDescription(null);
        }
        else {
            baseDef.setDescription(description);
        }

        baseDef.setDsType(request.getParameter("dsType"));
        baseDef.setType(request.getParameter("type"));
        baseDef.setRearm(WebSecurityUtils.safeParseDouble(request.getParameter("rearm")));
        baseDef.setTrigger(WebSecurityUtils.safeParseInt(request.getParameter("trigger")));
        baseDef.setValue(WebSecurityUtils.safeParseDouble(request.getParameter("value")));

        String clearKey = null;

        String triggeredUEI = request.getParameter("triggeredUEI");
        if (triggeredUEI == null || "".equals(triggeredUEI)) {
            baseDef.setTriggeredUEI(null); //Must set null in correct circumstances - empty string isn't quite the same thing
        }
        else {
            org.opennms.netmgt.xml.eventconf.Event source = getSourceEvent(baseDef.getType(), true);
            if (source != null && source.getAlarmData() != null && source.getAlarmData().getReductionKey() != null) {
                clearKey = source.getAlarmData().getReductionKey().replace("%uei%", triggeredUEI);
            }
            baseDef.setTriggeredUEI(triggeredUEI);
            this.ensureUEIInEventConf(source, triggeredUEI, baseDef.getType(), null, true);
        }

        String rearmedUEI = request.getParameter("rearmedUEI");
        if (rearmedUEI == null || "".equals(rearmedUEI) || baseDef.getType().equals("relativeChange") || baseDef.getType().equals("absoluteChange")) { // It doesn't make sense a rearm UEI for relativeChange or absoluteChange
            baseDef.setRearmedUEI(null); //Must set null in correct circumstances - empty string isn't quite the same thing
        }
        else {
            org.opennms.netmgt.xml.eventconf.Event source = getSourceEvent(baseDef.getType(), false);
            baseDef.setRearmedUEI(rearmedUEI);
            this.ensureUEIInEventConf(source, rearmedUEI, baseDef.getType(), clearKey, false);
        }
    }
    
    private  org.opennms.netmgt.xml.eventconf.Event getSourceEvent(String thresholdType, boolean isTrigger) {
        String sourceUei = null;
        switch(thresholdType) {
        case "high":
            sourceUei = isTrigger ? EventConstants.HIGH_THRESHOLD_EVENT_UEI : EventConstants.HIGH_THRESHOLD_REARM_EVENT_UEI;
            break;
        case "low":
            sourceUei = isTrigger ? EventConstants.LOW_THRESHOLD_EVENT_UEI : EventConstants.LOW_THRESHOLD_REARM_EVENT_UEI;
            break;
        case "relativeChange":
                sourceUei = isTrigger ? EventConstants.RELATIVE_CHANGE_THRESHOLD_EVENT_UEI : null;
            break;
        case "absoluteChange":
            sourceUei = isTrigger ? EventConstants.ABSOLUTE_CHANGE_THRESHOLD_EVENT_UEI : null;
            break;
        case "rearmingAbsoluteChange":
            sourceUei = isTrigger ? EventConstants.REARMING_ABSOLUTE_CHANGE_EXCEEDED_EVENT_UEI : EventConstants.REARMING_ABSOLUTE_CHANGE_REARM_EVENT_UEI;
            break;
        }
        if (sourceUei == null) {
            return null;
        }
        List<org.opennms.netmgt.xml.eventconf.Event> eventsForUEI = m_eventConfDao.getEvents(sourceUei);
        if (eventsForUEI != null && eventsForUEI.size() > 0) {
            return eventsForUEI.get(0);
        }
        return null;
    }

    private void ensureUEIInEventConf(org.opennms.netmgt.xml.eventconf.Event source, String targetUei, String thresholdType, String clearKey, boolean isTrigger) {
        List<org.opennms.netmgt.xml.eventconf.Event> eventsForUEI = m_eventConfDao.getEvents(targetUei);
        if (eventsForUEI == null || eventsForUEI.size() == 0) {
            String typeDesc = isTrigger ? "exceeded" : "rearmed";
            org.opennms.netmgt.xml.eventconf.Event event = new org.opennms.netmgt.xml.eventconf.Event();
            event.setUei(targetUei);
            event.setEventLabel("User-defined " + thresholdType + " threshold event " + typeDesc + ": " + targetUei);
            Logmsg logmsg = new Logmsg();
            event.setLogmsg(logmsg);
            if (source == null) {
                event.setDescr("Threshold " + typeDesc + " for %service% datasource %parm[ds]% on interface %interface%, parms: %parm[all]%");
                logmsg.setDest("logndisplay");
                logmsg.setContent("Threshold " + typeDesc + " for %service% datasource %parm[ds]% on interface %interface%, parms: %parm[all]%");
                event.setLogmsg(logmsg);
                event.setSeverity("Warning");
            } else {
                event.setDescr(source.getDescr());
                event.setSeverity(source.getSeverity());
                event.setOperinstruct(source.getOperinstruct());
                logmsg.setDest(source.getLogmsg().getDest());
                logmsg.setContent(source.getLogmsg().getContent());
                if (source.getAlarmData() != null) {
                    AlarmData alarmData = new AlarmData();
                    alarmData.setAlarmType(source.getAlarmData().getAlarmType());
                    alarmData.setAutoClean(source.getAlarmData().getAutoClean());
                    alarmData.setReductionKey(source.getAlarmData().getReductionKey());
                    if (!isTrigger && clearKey != null) {
                        alarmData.setClearKey(clearKey);
                    }
                    event.setAlarmData(alarmData);
                }
            }
            m_eventConfDao.addEventToProgrammaticStore(event);
            eventConfChanged = true;
        }
    }

    private ModelAndView finishThresholdEdit(HttpServletRequest request) throws ServletException {
        ThresholdingConfigFactory configFactory = ThresholdingConfigFactory.getInstance();
        ModelAndView modelAndView;
        String groupName = request.getParameter("groupName");
        String submitAction = request.getParameter("submitAction");
        Group group = configFactory.getGroup(groupName);
        String thresholdIndexString = request.getParameter("thresholdIndex");
        if (thresholdIndexString == null) {
            throw new ServletException("thresholdIndex parameter required to modify or delete a threshold");
        }
        int thresholdIndex = WebSecurityUtils.safeParseInt(thresholdIndexString);
        Threshold threshold = group.getThreshold(thresholdIndex); // TODO: NMS-4249, maybe a try/catch and add default on exception?

        if (SAVE_BUTTON_TITLE.equals(submitAction)) {
            this.commonFinishEdit(request, threshold);
            String dsName = request.getParameter("dsName");
            if (dsName == null || dsName.equals("")) {
                throw new ServletException("ds-name cannot be null or empty string");
            }
            threshold.setDsName(dsName);
            threshold.setFilterOperator(request.getParameter("filterOperator"));
            saveChanges();
        }
        else if (CANCEL_BUTTON_TITLE.equals(submitAction)) {
            String isNew = request.getParameter("isNew");
            if ("true".equals(isNew)) {
                //It was a new Threshold, but the user hit cancel.  Remove the new threshold from the group
                group.removeThreshold(threshold);
            }
            else {
                List<ResourceFilter> filters = getFilterList(request, false);
                if (filters != null) {
                    threshold.setResourceFilter(filters);
                }
            }
        }
        else {
            return finishThresholdFilterEdit(request, threshold);
        }
        // Remove Filters from Session
        setFilterList(request, null);

        //and got back to the editGroup page
        modelAndView = new ModelAndView("admin/thresholds/editGroup");
        modelAndView.addObject("group", group);
        return modelAndView;
    }

    private ModelAndView finishExpressionEdit(HttpServletRequest request) throws ServletException {
        ThresholdingConfigFactory configFactory = ThresholdingConfigFactory.getInstance();
        ModelAndView modelAndView;
        String groupName = request.getParameter("groupName");
        String submitAction = request.getParameter("submitAction");
        Group group = configFactory.getGroup(groupName);
        String expressionIndexString = request.getParameter("expressionIndex");
        if (expressionIndexString == null) {
            throw new ServletException("expressionIndex parameter required to modify or delete a threshold expression");
        }
        int expressionIndex = WebSecurityUtils.safeParseInt(expressionIndexString);
        Expression expression = group.getExpression(expressionIndex);

        if (SAVE_BUTTON_TITLE.equals(submitAction)) {
            this.commonFinishEdit(request, expression);
            String expDef = request.getParameter("expression");
            if (expDef == null || expDef.equals("")) {
                throw new ServletException("expression content cannot be null or empty string");
            }
            expression.setExpression(expDef);
            expression.setFilterOperator(request.getParameter("filterOperator"));
            saveChanges();
        }
        else if (CANCEL_BUTTON_TITLE.equals(submitAction)) {
            String isNew = request.getParameter("isNew");
            if ("true".equals(isNew)) {
                //It was a new Threshold, but the user hit cancel.  Remove the new threshold from the group
                group.removeExpression(expression);
            }
            else {
                List<ResourceFilter> filters = getFilterList(request, false);
                if (filters != null) {
                    expression.setResourceFilter(filters);
                }
            }
        }
        else {
            return finishThresholdFilterEdit(request, expression);
        }
        // Remove Filters from Session
        setFilterList(request, null);

        //and got back to the editGroup page
        modelAndView = new ModelAndView("admin/thresholds/editGroup");
        modelAndView.addObject("group", configFactory.getGroup(groupName));
        return modelAndView;
    }

    private ModelAndView gotoGroupList() throws ServletException {
        //Always reload to get a consistent view of the thresholds before we start editing.  
        //Otherwise we'll be dealing with questions on the mailing lists for the rest of our lives
        try {
            ThresholdingConfigFactory.reload();
        } catch (Throwable e) {
            throw new ServletException("Could not reload ThresholdingConfigFactory because " + e.getMessage(), e);
        }
        ThresholdingConfigFactory configFactory = ThresholdingConfigFactory.getInstance();
        ModelAndView modelAndView = new ModelAndView("admin/thresholds/list");

        Map<String, Group> groupMap = new TreeMap<String, Group>();
        for (String aName : configFactory.getGroupNames()) {
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
    @Override
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
     * @param resourceDao a {@link org.opennms.netmgt.dao.api.ResourceDao} object.
     */
    public void setResourceDao(ResourceDao resourceDao) {
        m_resourceDao = resourceDao;
    }

    /**
     * <p>setEventConfDao</p>
     *
     * @param eventConfDao a {@link EventConfDao} object.
     */
    public void setEventConfDao(EventConfDao eventConfDao) {
        m_eventConfDao = eventConfDao;
    }

}

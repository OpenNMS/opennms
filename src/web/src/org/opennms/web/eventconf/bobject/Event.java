//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2002-2003 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the// GNU General Public License for more details.
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

package org.opennms.web.eventconf.bobject;

import java.util.ArrayList;
import java.util.List;

/**
 * This is a data class for storing event configuration information as parsed
 * from the eventconf.xml file
 * 
 * @author <A HREF="mailto:jason@opennms.org">Jason Johns </A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS </A>
 * 
 * @version 1.1.1.1
 * 
 * @deprecated Replaced by a Castor-generated implementation.
 * 
 * @see org.opennms.netmgt.xml.eventconf.Event
 * 
 */
public class Event implements Cloneable {
    /**
     */
    private List m_maskElements;

    /**
     */
    private String m_uei;

    /**
     */
    private Snmp m_snmp;

    /**
     */
    private String m_description;

    /**
     */
    private String m_logMessage;

    /**
     */
    private String m_logMessageDest;

    /**
     */
    private String m_severity;

    /**
     */
    private Correlation m_correlation;

    /**
     */
    private String m_operInstruct;

    /**
     */
    private List m_autoActions;

    /**
     */
    private List m_operActions;

    /**
     */
    private String m_autoAcknowledge;

    /**
     */
    private String m_autoAcknowledgeState;

    /**
     */
    private List m_logGroups;

    /**
     */
    private List m_notifications;

    /**
     */
    private String m_tticket;

    /**
     */
    private String m_tticketState;

    /**
     */
    private List m_forwards;

    /**
     */
    private String m_mouseOverText;

    /**
     * The different destination values for the log message. If this array
     * changes please update the LOGMSG_DEST_DEFAULT_INDEX member if needed.
     */
    public static final String LOGMSG_DEST_VALUES[] = { "suppress", "logonly", "displayonly", "logndisplay" };

    /**
     * The index into the LOGMSG_DEST_VALUES array indicating the default
     * destination of the log message . If the values array changes please
     * update this index if needed.
     */
    public static final int LOGMSG_DEST_DEFAULT_INDEX = 3;

    /**
     * A generic list of values for various states. If this array changes please
     * update any of the XXX_STATE_DEFAULT_INDEX members if needed.
     */
    public static final String CONFIGURATION_STATES[] = { "on", "off" };

    /**
     * The index into the CONFIGURATION_STATES array indicating the default
     * state auto acknowlege. If the values array changes please update this
     * index if needed.
     */
    public static final int AUTOACKNOWLEDGE_STATE_DEFAULT_INDEX = 0;

    /**
     * The index into the CONFIGURATION_STATES array indicating the default
     * state of an trouble ticket. If the values array changes please update
     * this index if needed.
     */
    public static final int TTICKET_STATE_DEFAULT_INDEX = 0;

    /**
     * Default constructor, intializes the member variables.
     */
    public Event() {
        // initialize the lists
        m_maskElements = new ArrayList();
        m_autoActions = new ArrayList();
        m_operActions = new ArrayList();
        m_logGroups = new ArrayList();
        m_forwards = new ArrayList();
        m_notifications = new ArrayList();

        // initialize default indexes
        m_logMessageDest = LOGMSG_DEST_VALUES[LOGMSG_DEST_DEFAULT_INDEX];
        m_autoAcknowledgeState = CONFIGURATION_STATES[AUTOACKNOWLEDGE_STATE_DEFAULT_INDEX];
        m_tticketState = CONFIGURATION_STATES[TTICKET_STATE_DEFAULT_INDEX];
    }

    /**
     */
    public Object clone() {
        try {
            super.clone();
        } catch (CloneNotSupportedException e) {
            return null;
        }

        Event newConf = new Event();

        newConf.setUei(m_uei);

        if (m_snmp != null) {
            newConf.setSnmp((Snmp) m_snmp.clone());
        }

        newConf.setDescription(m_description);
        newConf.setLogMessage(m_logMessage);
        newConf.setLogMessageDest(m_logMessageDest);
        newConf.setSeverity(m_severity);

        if (m_correlation != null) {
            newConf.setCorrelation((Correlation) m_correlation.clone());
        }

        newConf.setOperInstruct(m_operInstruct);
        newConf.setAutoAcknowledge(m_autoAcknowledge);
        newConf.setAutoAcknowledgeState(m_autoAcknowledgeState);
        newConf.setTticket(m_tticket);
        newConf.setTticketState(m_tticketState);
        newConf.setMouseOverText(m_mouseOverText);

        // copy the mask elements
        for (int i = 0; i < m_maskElements.size(); i++) {
            MaskElement oldElement = (MaskElement) m_maskElements.get(i);
            newConf.addMask((MaskElement) oldElement.clone());
        }

        // copy the auto actions
        for (int i = 0; i < m_autoActions.size(); i++) {
            AutoAction oldAction = (AutoAction) m_autoActions.get(i);
            newConf.addAutoAction((AutoAction) oldAction.clone());
        }

        // copy the operator actions
        for (int i = 0; i < m_operActions.size(); i++) {
            OperatorAction oldAction = (OperatorAction) m_operActions.get(i);
            newConf.addOperatorAction((OperatorAction) oldAction.clone());
        }

        // copy the log groups
        for (int i = 0; i < m_logGroups.size(); i++) {
            newConf.addLogGroup((String) m_logGroups.get(i));
        }

        // copy the forwards
        for (int i = 0; i < m_forwards.size(); i++) {
            Forward oldForward = (Forward) m_forwards.get(i);
            newConf.addForward((Forward) oldForward.clone());
        }

        // copy the notifications
        for (int i = 0; i < m_notifications.size(); i++) {
            newConf.addNotification((String) m_notifications.get(i));
        }

        return newConf;
    }

    /**
     */
    public void addMask(MaskElement element) {
        m_maskElements.add(element);
    }

    /**
     */
    public List getMask() {
        return m_maskElements;
    }

    /**
     */
    public void clearMask() {
        m_maskElements.clear();
    }

    /**
     */
    public void removeMaskAt(int index) {
        m_maskElements.remove(index);
    }

    /**
     */
    public void setUei(String uei) {
        m_uei = uei;
    }

    /**
     */
    public String getUei() {
        return m_uei;
    }

    /**
     */
    public void setSnmp(Snmp snmp) {
        m_snmp = snmp;
    }

    /**
     */
    public Snmp getSnmp() {
        return m_snmp;
    }

    /**
     */
    public void setDescription(String description) {
        m_description = description;
    }

    /**
     */
    public String getDescription() {
        return m_description;
    }

    /**
     */
    public void setLogMessage(String logMessage) {
        m_logMessage = logMessage;
    }

    /**
     */
    public String getLogMessage() {
        return m_logMessage;
    }

    /**
     */
    public void setLogMessageDest(String dest) {
        /*
         * if (index < 0 || index > LOGMSG_DEST_VALUES.length) throw new
         * InvalidParameterException("The log message destination
         * index("+index+") must be >= 0 and <= " + LOGMSG_DEST_VALUES.length);
         */
        m_logMessageDest = dest;
    }

    /**
     */
    public String getLogMessageDestination() {
        return m_logMessageDest;
    }

    /**
     */
    public void setSeverity(String severity) {
        m_severity = severity;
    }

    /**
     */
    public String getSeverity() {
        return m_severity;
    }

    /**
     */
    public void setCorrelation(Correlation correlation) {
        m_correlation = correlation;
    }

    /**
     */
    public Correlation getCorrelation() {
        return m_correlation;
    }

    /**
     */
    public void setOperInstruct(String operInstruct) {
        m_operInstruct = operInstruct;
    }

    /**
     */
    public String getOperInstruct() {
        return m_operInstruct;
    }

    /**
     */
    public void setAutoAcknowledge(String autoAcknowledge) {
        m_autoAcknowledge = autoAcknowledge;
    }

    /**
     */
    public String getAutoAcknowledge() {
        return m_autoAcknowledge;
    }

    /**
     */
    public void setAutoAcknowledgeState(String state) {
        /*
         * if (index < 0 || index > CONFIGURATION_STATES.length) throw new
         * InvalidParameterException("The auto acknowlege state index("+index+")
         * must be >= 0 and <= " + CONFIGURATION_STATES.length);
         */
        m_autoAcknowledgeState = state;
    }

    /**
     */
    public String getAutoAcknowledgeState() {
        return m_autoAcknowledgeState;
    }

    /**
     */
    public void setTticket(String tticket) {
        m_tticket = tticket;
    }

    /**
     */
    public String getTTicket() {
        return m_tticket;
    }

    /**
     */
    public void setTticketState(String state) {
        /*
         * if (index < 0 || index > CONFIGURATION_STATES.length) throw new
         * InvalidParameterException("The trouble ticket state index("+index+")
         * must be >= 0 and <= " + CONFIGURATION_STATES.length);
         */
        m_tticketState = state;
    }

    /**
     */
    public String getTTicketState() {
        return m_tticketState;
    }

    /**
     */
    public void setMouseOverText(String text) {
        m_mouseOverText = text;
    }

    /**
     */
    public String getMouseOverText() {
        return m_mouseOverText;
    }

    /**
     */
    public void addAutoAction(AutoAction action) {
        m_autoActions.add(action);
    }

    /**
     */
    public List getAutoActions() {
        return m_autoActions;
    }

    /**
     */
    public void clearAutoActions() {
        m_autoActions.clear();
    }

    /**
     */
    public void addOperatorAction(OperatorAction action) {
        m_operActions.add(action);
    }

    /**
     */
    public List getOperatorActions() {
        return m_operActions;
    }

    /**
     */
    public void clearOperatorActions() {
        m_operActions.clear();
    }

    /**
     */
    public void addForward(Forward forward) {
        m_forwards.add(forward);
    }

    /**
     */
    public List getForwards() {
        return m_forwards;
    }

    /**
     */
    public void clearForwards() {
        m_forwards.clear();
    }

    /**
     */
    public void addLogGroup(String logGroup) {
        m_logGroups.add(logGroup);
    }

    /**
     */
    public List getLogGroups() {
        return m_logGroups;
    }

    /**
     */
    public void clearLogGroups() {
        m_logGroups.clear();
    }

    /**
     */
    public void addNotification(String notification) {
        m_notifications.add(notification);
    }

    /**
     */
    public List getNotifications() {
        return m_notifications;
    }

    /**
     */
    public void removeNoticeContaining(String matchString) {
        int index = getNoticeIndex(matchString);

        if (index != -1) {
            m_notifications.remove(index);
        }
    }

    /**
     */
    public int getNoticeIndex(String matchString) {
        int index = -1;

        // need to search each notice string for the notice name

        for (int i = 0; i < m_notifications.size(); i++) {
            String curNotice = (String) m_notifications.get(i);

            if (curNotice.indexOf(matchString) != -1) {
                index = i;
                break;
            }
        }

        return index;
    }

    /**
     */
    public void clearNotifications() {
        m_notifications.clear();
    }
}

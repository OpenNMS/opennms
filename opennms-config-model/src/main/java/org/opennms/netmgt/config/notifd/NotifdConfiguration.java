/*******************************************************************************
 * This file is part of OpenNMS(R).
 * 
 * Copyright (C) 2017-2017 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2017 The OpenNMS Group, Inc.
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
 *     http://www.gnu.org/licenses/
 * 
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.config.notifd;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Top-level element for the notifd-configuration.xml
 *  configuration file.
 * 
 * @version $Revision$ $Date$
 */
@XmlRootElement(name = "notifd-configuration")
@XmlAccessorType(XmlAccessType.FIELD)

@SuppressWarnings("all") public class NotifdConfiguration implements java.io.Serializable {

    private static final String DEFAULT_PAGES_SENT = "SELECT * FROM notifications";
    private static final String DEFAULT_NEXT_NOTIFID = "SELECT nextval('notifynxtid')";
    private static final String DEFAULT_NEXT_USER_NOTIFID = "SELECT nextval('userNotifNxtId')";
    private static final String DEFAULT_NEXT_GROUP_ID = "SELECT nextval('notifygrpid')";
    private static final String DEFAULT_SERVICEID_SQL = "SELECT serviceID from service where serviceName = ?";
    private static final String DEFAULT_OUTSTANDING_NOTICES_SQL = "SELECT notifyid FROM notifications where notifyId = ? AND respondTime is not null";
    private static final String DEFAULT_ACKNOWLEDGEID_SQL = "SELECT notifyid FROM notifications WHERE eventuei=? AND nodeid=? AND interfaceid=? AND serviceid=?";
    private static final String DEFAULT_ACKNOWLEDGE_UPDATE_SQL = "UPDATE notifications SET answeredby=?, respondtime=? WHERE notifyId=?";
    private static final String DEFAULT_EMAIL_ADDRESS_COMMAND = "javaEmail";

    @XmlAttribute(name = "status", required = true)
    private String status;

    @XmlAttribute(name = "pages-sent")
    private String pagesSent;

    @XmlAttribute(name = "next-notif-id")
    private String nextNotifId;

    @XmlAttribute(name = "next-user-notif-id")
    private String nextUserNotifId;

    @XmlAttribute(name = "next-group-id")
    private String nextGroupId;

    @XmlAttribute(name = "service-id-sql")
    private String serviceIdSql;

    @XmlAttribute(name = "outstanding-notices-sql")
    private String outstandingNoticesSql;

    @XmlAttribute(name = "acknowledge-id-sql")
    private String acknowledgeIdSql;

    @XmlAttribute(name = "acknowledge-update-sql")
    private String acknowledgeUpdateSql;

    @XmlAttribute(name = "match-all", required = true)
    private Boolean matchAll;

    @XmlAttribute(name = "email-address-command")
    private String emailAddressCommand;

    @XmlAttribute(name = "numeric-skip-resolution-prefix")
    private Boolean numericSkipResolutionPrefix;

    @XmlElement(name = "auto-acknowledge-alarm")
    private AutoAcknowledgeAlarm autoAcknowledgeAlarm;

    @XmlElement(name = "auto-acknowledge")
    private List<AutoAcknowledge> autoAcknowledgeList = new ArrayList<>();

    @XmlElement(name = "queue", required = true)
    private List<Queue> queueList = new ArrayList<>();

    @XmlElement(name = "outage-calendar")
    private List<String> outageCalendarList = new ArrayList<>();

    public NotifdConfiguration() { }

    /**
     * 
     * 
     * @param vAutoAcknowledge
     * @throws IndexOutOfBoundsException if the index given is outside
     * the bounds of the collection
     */
    public void addAutoAcknowledge(final AutoAcknowledge vAutoAcknowledge) throws IndexOutOfBoundsException {
        this.autoAcknowledgeList.add(vAutoAcknowledge);
    }

    /**
     * 
     * 
     * @param index
     * @param vAutoAcknowledge
     * @throws IndexOutOfBoundsException if the index given is outside
     * the bounds of the collection
     */
    public void addAutoAcknowledge(final int index, final AutoAcknowledge vAutoAcknowledge) throws IndexOutOfBoundsException {
        this.autoAcknowledgeList.add(index, vAutoAcknowledge);
    }

    /**
     * 
     * 
     * @param vOutageCalendar
     * @throws IndexOutOfBoundsException if the index given is outside
     * the bounds of the collection
     */
    public void addOutageCalendar(final String vOutageCalendar) throws IndexOutOfBoundsException {
        this.outageCalendarList.add(vOutageCalendar);
    }

    /**
     * 
     * 
     * @param index
     * @param vOutageCalendar
     * @throws IndexOutOfBoundsException if the index given is outside
     * the bounds of the collection
     */
    public void addOutageCalendar(final int index, final String vOutageCalendar) throws IndexOutOfBoundsException {
        this.outageCalendarList.add(index, vOutageCalendar);
    }

    /**
     * 
     * 
     * @param vQueue
     * @throws IndexOutOfBoundsException if the index given is outside
     * the bounds of the collection
     */
    public void addQueue(final Queue vQueue) throws IndexOutOfBoundsException {
        this.queueList.add(vQueue);
    }

    /**
     * 
     * 
     * @param index
     * @param vQueue
     * @throws IndexOutOfBoundsException if the index given is outside
     * the bounds of the collection
     */
    public void addQueue(final int index, final Queue vQueue) throws IndexOutOfBoundsException {
        this.queueList.add(index, vQueue);
    }

    /**
     */
    public void deleteMatchAll() {
        this.matchAll= null;
    }

    /**
     */
    public void deleteNumericSkipResolutionPrefix() {
        this.numericSkipResolutionPrefix= null;
    }

    /**
     * Method enumerateAutoAcknowledge.
     * 
     * @return an Enumeration over all possible elements of this collection
     */
    public Enumeration<AutoAcknowledge> enumerateAutoAcknowledge() {
        return Collections.enumeration(this.autoAcknowledgeList);
    }

    /**
     * Method enumerateOutageCalendar.
     * 
     * @return an Enumeration over all possible elements of this collection
     */
    public Enumeration<String> enumerateOutageCalendar() {
        return Collections.enumeration(this.outageCalendarList);
    }

    /**
     * Method enumerateQueue.
     * 
     * @return an Enumeration over all possible elements of this collection
     */
    public Enumeration<Queue> enumerateQueue() {
        return Collections.enumeration(this.queueList);
    }

    /**
     * Overrides the Object.equals method.
     * 
     * @param obj
     * @return true if the objects are equal.
     */
    @Override
    public boolean equals(final Object obj) {
        if ( this == obj ) {
            return true;
        }
        
        if (obj instanceof NotifdConfiguration) {
            NotifdConfiguration temp = (NotifdConfiguration)obj;
            boolean equals = Objects.equals(temp.status, status)
                && Objects.equals(temp.pagesSent, pagesSent)
                && Objects.equals(temp.nextNotifId, nextNotifId)
                && Objects.equals(temp.nextUserNotifId, nextUserNotifId)
                && Objects.equals(temp.nextGroupId, nextGroupId)
                && Objects.equals(temp.serviceIdSql, serviceIdSql)
                && Objects.equals(temp.outstandingNoticesSql, outstandingNoticesSql)
                && Objects.equals(temp.acknowledgeIdSql, acknowledgeIdSql)
                && Objects.equals(temp.acknowledgeUpdateSql, acknowledgeUpdateSql)
                && Objects.equals(temp.matchAll, matchAll)
                && Objects.equals(temp.emailAddressCommand, emailAddressCommand)
                && Objects.equals(temp.numericSkipResolutionPrefix, numericSkipResolutionPrefix)
                && Objects.equals(temp.autoAcknowledgeAlarm, autoAcknowledgeAlarm)
                && Objects.equals(temp.autoAcknowledgeList, autoAcknowledgeList)
                && Objects.equals(temp.queueList, queueList)
                && Objects.equals(temp.outageCalendarList, outageCalendarList);
            return equals;
        }
        return false;
    }

    /**
     * Returns the value of field 'acknowledgeIdSql'.
     * 
     * @return the value of field 'AcknowledgeIdSql'.
     */
    public String getAcknowledgeIdSql() {
        return this.acknowledgeIdSql != null ? this.acknowledgeIdSql : DEFAULT_ACKNOWLEDGEID_SQL;
    }

    /**
     * Returns the value of field 'acknowledgeUpdateSql'.
     * 
     * @return the value of field 'AcknowledgeUpdateSql'.
     */
    public String getAcknowledgeUpdateSql() {
        return this.acknowledgeUpdateSql != null ? this.acknowledgeUpdateSql : DEFAULT_ACKNOWLEDGE_UPDATE_SQL;
    }

    /**
     * Method getAutoAcknowledge.
     * 
     * @param index
     * @throws IndexOutOfBoundsException if the index given is outside
     * the bounds of the collection
     * @return the value of the AutoAcknowledge
     * at the given index
     */
    public AutoAcknowledge getAutoAcknowledge(final int index) throws IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this.autoAcknowledgeList.size()) {
            throw new IndexOutOfBoundsException("getAutoAcknowledge: Index value '" + index + "' not in range [0.." + (this.autoAcknowledgeList.size() - 1) + "]");
        }
        
        return (AutoAcknowledge) autoAcknowledgeList.get(index);
    }

    /**
     * Method getAutoAcknowledge.Returns the contents of the collection in an
     * Array.  <p>Note:  Just in case the collection contents are changing in
     * another thread, we pass a 0-length Array of the correct type into the API
     * call.  This way we <i>know</i> that the Array returned is of exactly the
     * correct length.
     * 
     * @return this collection as an Array
     */
    public AutoAcknowledge[] getAutoAcknowledge() {
        AutoAcknowledge[] array = new AutoAcknowledge[0];
        return (AutoAcknowledge[]) this.autoAcknowledgeList.toArray(array);
    }

    /**
     * Returns the value of field 'autoAcknowledgeAlarm'.
     * 
     * @return the value of field 'AutoAcknowledgeAlarm'.
     */
    public Optional<AutoAcknowledgeAlarm> getAutoAcknowledgeAlarm() {
        return Optional.ofNullable(this.autoAcknowledgeAlarm);
    }

    /**
     * Method getAutoAcknowledgeCollection.Returns a reference to
     * 'autoAcknowledgeList'. No type checking is performed on any modifications
     * to the Vector.
     * 
     * @return a reference to the Vector backing this class
     */
    public List<AutoAcknowledge> getAutoAcknowledgeCollection() {
        return this.autoAcknowledgeList;
    }

    /**
     * Method getAutoAcknowledgeCount.
     * 
     * @return the size of this collection
     */
    public int getAutoAcknowledgeCount() {
        return this.autoAcknowledgeList.size();
    }

    /**
     * Returns the value of field 'emailAddressCommand'.
     * 
     * @return the value of field 'EmailAddressCommand'.
     */
    public String getEmailAddressCommand() {
        return this.emailAddressCommand != null ? this.emailAddressCommand : DEFAULT_EMAIL_ADDRESS_COMMAND;
    }

    /**
     * Returns the value of field 'matchAll'.
     * 
     * @return the value of field 'MatchAll'.
     */
    public Boolean getMatchAll() {
        return this.matchAll;
    }

    /**
     * Returns the value of field 'nextGroupId'.
     * 
     * @return the value of field 'NextGroupId'.
     */
    public String getNextGroupId() {
        return this.nextGroupId != null ? this.nextGroupId : DEFAULT_NEXT_GROUP_ID;
    }

    /**
     * Returns the value of field 'nextNotifId'.
     * 
     * @return the value of field 'NextNotifId'.
     */
    public String getNextNotifId() {
        return this.nextNotifId != null ? this.nextNotifId : DEFAULT_NEXT_NOTIFID;
    }

    /**
     * Returns the value of field 'nextUserNotifId'.
     * 
     * @return the value of field 'NextUserNotifId'.
     */
    public String getNextUserNotifId() {
        return this.nextUserNotifId != null ? this.nextUserNotifId : DEFAULT_NEXT_USER_NOTIFID;
    }

    /**
     * Returns the value of field 'numericSkipResolutionPrefix'.
     * 
     * @return the value of field 'NumericSkipResolutionPrefix'.
     */
    public Boolean getNumericSkipResolutionPrefix() {
        return this.numericSkipResolutionPrefix != null ? this.numericSkipResolutionPrefix : Boolean.valueOf("false");
    }

    /**
     * Method getOutageCalendar.
     * 
     * @param index
     * @throws IndexOutOfBoundsException if the index given is outside
     * the bounds of the collection
     * @return the value of the String at the given index
     */
    public String getOutageCalendar(final int index) throws IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this.outageCalendarList.size()) {
            throw new IndexOutOfBoundsException("getOutageCalendar: Index value '" + index + "' not in range [0.." + (this.outageCalendarList.size() - 1) + "]");
        }
        
        return (String) outageCalendarList.get(index);
    }

    /**
     * Method getOutageCalendar.Returns the contents of the collection in an
     * Array.  <p>Note:  Just in case the collection contents are changing in
     * another thread, we pass a 0-length Array of the correct type into the API
     * call.  This way we <i>know</i> that the Array returned is of exactly the
     * correct length.
     * 
     * @return this collection as an Array
     */
    public String[] getOutageCalendar() {
        String[] array = new String[0];
        return (String[]) this.outageCalendarList.toArray(array);
    }

    /**
     * Method getOutageCalendarCollection.Returns a reference to
     * 'outageCalendarList'. No type checking is performed on any modifications to
     * the Vector.
     * 
     * @return a reference to the Vector backing this class
     */
    public List<String> getOutageCalendarCollection() {
        return this.outageCalendarList;
    }

    /**
     * Method getOutageCalendarCount.
     * 
     * @return the size of this collection
     */
    public int getOutageCalendarCount() {
        return this.outageCalendarList.size();
    }

    /**
     * Returns the value of field 'outstandingNoticesSql'.
     * 
     * @return the value of field 'OutstandingNoticesSql'.
     */
    public String getOutstandingNoticesSql() {
        return this.outstandingNoticesSql != null ? this.outstandingNoticesSql : DEFAULT_OUTSTANDING_NOTICES_SQL;
    }

    /**
     * Returns the value of field 'pagesSent'.
     * 
     * @return the value of field 'PagesSent'.
     */
    public String getPagesSent() {
        return this.pagesSent != null ? this.pagesSent : DEFAULT_PAGES_SENT;
    }

    /**
     * Method getQueue.
     * 
     * @param index
     * @throws IndexOutOfBoundsException if the index given is outside
     * the bounds of the collection
     * @return the value of the Queue at the
     * given index
     */
    public Queue getQueue(final int index) throws IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this.queueList.size()) {
            throw new IndexOutOfBoundsException("getQueue: Index value '" + index + "' not in range [0.." + (this.queueList.size() - 1) + "]");
        }
        
        return (Queue) queueList.get(index);
    }

    /**
     * Method getQueue.Returns the contents of the collection in an Array. 
     * <p>Note:  Just in case the collection contents are changing in another
     * thread, we pass a 0-length Array of the correct type into the API call. 
     * This way we <i>know</i> that the Array returned is of exactly the correct
     * length.
     * 
     * @return this collection as an Array
     */
    public Queue[] getQueue() {
        Queue[] array = new Queue[0];
        return (Queue[]) this.queueList.toArray(array);
    }

    /**
     * Method getQueueCollection.Returns a reference to 'queueList'. No type
     * checking is performed on any modifications to the Vector.
     * 
     * @return a reference to the Vector backing this class
     */
    public List<Queue> getQueueCollection() {
        return this.queueList;
    }

    /**
     * Method getQueueCount.
     * 
     * @return the size of this collection
     */
    public int getQueueCount() {
        return this.queueList.size();
    }

    /**
     * Returns the value of field 'serviceIdSql'.
     * 
     * @return the value of field 'ServiceIdSql'.
     */
    public String getServiceIdSql() {
        return this.serviceIdSql != null ? this.serviceIdSql : DEFAULT_SERVICEID_SQL;
    }

    /**
     * Returns the value of field 'status'.
     * 
     * @return the value of field 'Status'.
     */
    public String getStatus() {
        return this.status;
    }

    /**
     * Method hasNumericSkipResolutionPrefix.
     * 
     * @return true if at least one NumericSkipResolutionPrefix has been added
     */
    public boolean hasNumericSkipResolutionPrefix() {
        return this.numericSkipResolutionPrefix != null;
    }

    /**
     * Method hashCode.
     * 
     * @return a hash code value for the object.
     */
    @Override
    public int hashCode() {
        int hash = Objects.hash(
            status, 
            pagesSent, 
            nextNotifId, 
            nextUserNotifId, 
            nextGroupId, 
            serviceIdSql, 
            outstandingNoticesSql, 
            acknowledgeIdSql, 
            acknowledgeUpdateSql, 
            matchAll, 
            emailAddressCommand, 
            numericSkipResolutionPrefix, 
            autoAcknowledgeAlarm, 
            autoAcknowledgeList, 
            queueList, 
            outageCalendarList);
        return hash;
    }

    /**
     * Returns the value of field 'numericSkipResolutionPrefix'.
     * 
     * @return the value of field 'NumericSkipResolutionPrefix'.
     */
    public Boolean isNumericSkipResolutionPrefix() {
        return this.numericSkipResolutionPrefix != null ? this.numericSkipResolutionPrefix : Boolean.valueOf("false");
    }

    /**
     * Method iterateAutoAcknowledge.
     * 
     * @return an Iterator over all possible elements in this collection
     */
    public Iterator<AutoAcknowledge> iterateAutoAcknowledge() {
        return this.autoAcknowledgeList.iterator();
    }

    /**
     * Method iterateOutageCalendar.
     * 
     * @return an Iterator over all possible elements in this collection
     */
    public Iterator<String> iterateOutageCalendar() {
        return this.outageCalendarList.iterator();
    }

    /**
     * Method iterateQueue.
     * 
     * @return an Iterator over all possible elements in this collection
     */
    public Iterator<Queue> iterateQueue() {
        return this.queueList.iterator();
    }

    /**
     */
    public void removeAllAutoAcknowledge() {
        this.autoAcknowledgeList.clear();
    }

    /**
     */
    public void removeAllOutageCalendar() {
        this.outageCalendarList.clear();
    }

    /**
     */
    public void removeAllQueue() {
        this.queueList.clear();
    }

    /**
     * Method removeAutoAcknowledge.
     * 
     * @param vAutoAcknowledge
     * @return true if the object was removed from the collection.
     */
    public boolean removeAutoAcknowledge(final AutoAcknowledge vAutoAcknowledge) {
        boolean removed = autoAcknowledgeList.remove(vAutoAcknowledge);
        return removed;
    }

    /**
     * Method removeAutoAcknowledgeAt.
     * 
     * @param index
     * @return the element removed from the collection
     */
    public AutoAcknowledge removeAutoAcknowledgeAt(final int index) {
        Object obj = this.autoAcknowledgeList.remove(index);
        return (AutoAcknowledge) obj;
    }

    /**
     * Method removeOutageCalendar.
     * 
     * @param vOutageCalendar
     * @return true if the object was removed from the collection.
     */
    public boolean removeOutageCalendar(final String vOutageCalendar) {
        boolean removed = outageCalendarList.remove(vOutageCalendar);
        return removed;
    }

    /**
     * Method removeOutageCalendarAt.
     * 
     * @param index
     * @return the element removed from the collection
     */
    public String removeOutageCalendarAt(final int index) {
        Object obj = this.outageCalendarList.remove(index);
        return (String) obj;
    }

    /**
     * Method removeQueue.
     * 
     * @param vQueue
     * @return true if the object was removed from the collection.
     */
    public boolean removeQueue(final Queue vQueue) {
        boolean removed = queueList.remove(vQueue);
        return removed;
    }

    /**
     * Method removeQueueAt.
     * 
     * @param index
     * @return the element removed from the collection
     */
    public Queue removeQueueAt(final int index) {
        Object obj = this.queueList.remove(index);
        return (Queue) obj;
    }

    /**
     * Sets the value of field 'acknowledgeIdSql'.
     * 
     * @param acknowledgeIdSql the value of field 'acknowledgeIdSql'.
     */
    public void setAcknowledgeIdSql(final String acknowledgeIdSql) {
        this.acknowledgeIdSql = acknowledgeIdSql;
    }

    /**
     * Sets the value of field 'acknowledgeUpdateSql'.
     * 
     * @param acknowledgeUpdateSql the value of field 'acknowledgeUpdateSql'.
     */
    public void setAcknowledgeUpdateSql(final String acknowledgeUpdateSql) {
        this.acknowledgeUpdateSql = acknowledgeUpdateSql;
    }

    /**
     * 
     * 
     * @param index
     * @param vAutoAcknowledge
     * @throws IndexOutOfBoundsException if the index given is outside
     * the bounds of the collection
     */
    public void setAutoAcknowledge(final int index, final AutoAcknowledge vAutoAcknowledge) throws IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this.autoAcknowledgeList.size()) {
            throw new IndexOutOfBoundsException("setAutoAcknowledge: Index value '" + index + "' not in range [0.." + (this.autoAcknowledgeList.size() - 1) + "]");
        }
        
        this.autoAcknowledgeList.set(index, vAutoAcknowledge);
    }

    /**
     * 
     * 
     * @param vAutoAcknowledgeArray
     */
    public void setAutoAcknowledge(final AutoAcknowledge[] vAutoAcknowledgeArray) {
        //-- copy array
        autoAcknowledgeList.clear();
        
        for (int i = 0; i < vAutoAcknowledgeArray.length; i++) {
                this.autoAcknowledgeList.add(vAutoAcknowledgeArray[i]);
        }
    }

    /**
     * Sets the value of 'autoAcknowledgeList' by copying the given Vector. All
     * elements will be checked for type safety.
     * 
     * @param vAutoAcknowledgeList the Vector to copy.
     */
    public void setAutoAcknowledge(final List<AutoAcknowledge> vAutoAcknowledgeList) {
        // copy vector
        this.autoAcknowledgeList.clear();
        
        this.autoAcknowledgeList.addAll(vAutoAcknowledgeList);
    }

    /**
     * Sets the value of field 'autoAcknowledgeAlarm'.
     * 
     * @param autoAcknowledgeAlarm the value of field 'autoAcknowledgeAlarm'.
     */
    public void setAutoAcknowledgeAlarm(final AutoAcknowledgeAlarm autoAcknowledgeAlarm) {
        this.autoAcknowledgeAlarm = autoAcknowledgeAlarm;
    }

    /**
     * Sets the value of 'autoAcknowledgeList' by setting it to the given Vector.
     * No type checking is performed.
     * @deprecated
     * 
     * @param autoAcknowledgeList the Vector to set.
     */
    public void setAutoAcknowledgeCollection(final List<AutoAcknowledge> autoAcknowledgeList) {
        this.autoAcknowledgeList = autoAcknowledgeList == null? new ArrayList<>() : autoAcknowledgeList;
    }

    /**
     * Sets the value of field 'emailAddressCommand'.
     * 
     * @param emailAddressCommand the value of field 'emailAddressCommand'.
     */
    public void setEmailAddressCommand(final String emailAddressCommand) {
        this.emailAddressCommand = emailAddressCommand;
    }

    /**
     * Sets the value of field 'matchAll'.
     * 
     * @param matchAll the value of field 'matchAll'.
     */
    public void setMatchAll(final Boolean matchAll) {
        if (matchAll == null) {
            throw new IllegalArgumentException("match-all is a required field!");
        }
        this.matchAll = matchAll;
    }

    /**
     * Sets the value of field 'nextGroupId'.
     * 
     * @param nextGroupId the value of field 'nextGroupId'.
     */
    public void setNextGroupId(final String nextGroupId) {
        this.nextGroupId = nextGroupId;
    }

    /**
     * Sets the value of field 'nextNotifId'.
     * 
     * @param nextNotifId the value of field 'nextNotifId'.
     */
    public void setNextNotifId(final String nextNotifId) {
        this.nextNotifId = nextNotifId;
    }

    /**
     * Sets the value of field 'nextUserNotifId'.
     * 
     * @param nextUserNotifId the value of field 'nextUserNotifId'.
     */
    public void setNextUserNotifId(final String nextUserNotifId) {
        this.nextUserNotifId = nextUserNotifId;
    }

    /**
     * Sets the value of field 'numericSkipResolutionPrefix'.
     * 
     * @param numericSkipResolutionPrefix the value of field
     * 'numericSkipResolutionPrefix'.
     */
    public void setNumericSkipResolutionPrefix(final Boolean numericSkipResolutionPrefix) {
        this.numericSkipResolutionPrefix = numericSkipResolutionPrefix;
    }

    /**
     * 
     * 
     * @param index
     * @param vOutageCalendar
     * @throws IndexOutOfBoundsException if the index given is outside
     * the bounds of the collection
     */
    public void setOutageCalendar(final int index, final String vOutageCalendar) throws IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this.outageCalendarList.size()) {
            throw new IndexOutOfBoundsException("setOutageCalendar: Index value '" + index + "' not in range [0.." + (this.outageCalendarList.size() - 1) + "]");
        }
        
        this.outageCalendarList.set(index, vOutageCalendar);
    }

    /**
     * 
     * 
     * @param vOutageCalendarArray
     */
    public void setOutageCalendar(final String[] vOutageCalendarArray) {
        //-- copy array
        outageCalendarList.clear();
        
        for (int i = 0; i < vOutageCalendarArray.length; i++) {
                this.outageCalendarList.add(vOutageCalendarArray[i]);
        }
    }

    /**
     * Sets the value of 'outageCalendarList' by copying the given Vector. All
     * elements will be checked for type safety.
     * 
     * @param vOutageCalendarList the Vector to copy.
     */
    public void setOutageCalendar(final List<String> vOutageCalendarList) {
        // copy vector
        this.outageCalendarList.clear();
        
        this.outageCalendarList.addAll(vOutageCalendarList);
    }

    /**
     * Sets the value of 'outageCalendarList' by setting it to the given Vector.
     * No type checking is performed.
     * @deprecated
     * 
     * @param outageCalendarList the Vector to set.
     */
    public void setOutageCalendarCollection(final List<String> outageCalendarList) {
        this.outageCalendarList = outageCalendarList == null? new ArrayList<>() : outageCalendarList;
    }

    /**
     * Sets the value of field 'outstandingNoticesSql'.
     * 
     * @param outstandingNoticesSql the value of field 'outstandingNoticesSql'.
     */
    public void setOutstandingNoticesSql(final String outstandingNoticesSql) {
        this.outstandingNoticesSql = outstandingNoticesSql;
    }

    /**
     * Sets the value of field 'pagesSent'.
     * 
     * @param pagesSent the value of field 'pagesSent'.
     */
    public void setPagesSent(final String pagesSent) {
        this.pagesSent = pagesSent;
    }

    /**
     * 
     * 
     * @param index
     * @param vQueue
     * @throws IndexOutOfBoundsException if the index given is outside
     * the bounds of the collection
     */
    public void setQueue(final int index, final Queue vQueue) throws IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this.queueList.size()) {
            throw new IndexOutOfBoundsException("setQueue: Index value '" + index + "' not in range [0.." + (this.queueList.size() - 1) + "]");
        }
        
        this.queueList.set(index, vQueue);
    }

    /**
     * 
     * 
     * @param vQueueArray
     */
    public void setQueue(final Queue[] vQueueArray) {
        //-- copy array
        queueList.clear();
        
        for (int i = 0; i < vQueueArray.length; i++) {
                this.queueList.add(vQueueArray[i]);
        }
    }

    /**
     * Sets the value of 'queueList' by copying the given Vector. All elements
     * will be checked for type safety.
     * 
     * @param vQueueList the Vector to copy.
     */
    public void setQueue(final List<Queue> vQueueList) {
        // copy vector
        this.queueList.clear();
        
        this.queueList.addAll(vQueueList);
    }

    /**
     * Sets the value of 'queueList' by setting it to the given Vector. No type
     * checking is performed.
     * @deprecated
     * 
     * @param queueList the Vector to set.
     */
    public void setQueueCollection(final List<Queue> queueList) {
        this.queueList = queueList == null? new ArrayList<>() : queueList;
    }

    /**
     * Sets the value of field 'serviceIdSql'.
     * 
     * @param serviceIdSql the value of field 'serviceIdSql'.
     */
    public void setServiceIdSql(final String serviceIdSql) {
        this.serviceIdSql = serviceIdSql;
    }

    /**
     * Sets the value of field 'status'.
     * 
     * @param status the value of field 'status'.
     */
    public void setStatus(final String status) {
        if (status == null) {
            throw new IllegalArgumentException("status is a required field!");
        }
        this.status = status;
    }

}

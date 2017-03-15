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

package org.opennms.netmgt.config.notifications;

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
 * Class Notification.
 * 
 * @version $Revision$ $Date$
 */
@XmlRootElement(name = "notification")
@XmlAccessorType(XmlAccessType.FIELD)
public class Notification implements java.io.Serializable {
    private static final long serialVersionUID = 1L;

    private static final String DEFAULT_WRITEABLE = "yes";

    @XmlAttribute(name = "name", required = true)
    private String name;

    @XmlAttribute(name = "status", required = true)
    private String status;

    @XmlAttribute(name = "writeable")
    private String writeable;

    @XmlElement(name = "uei", required = true)
    private String uei;

    @XmlElement(name = "description")
    private String description;

    @XmlElement(name = "rule", required = true)
    private String rule;

    @XmlElement(name = "notice-queue")
    private String noticeQueue;

    @XmlElement(name = "destinationPath", required = true)
    private String destinationPath;

    @XmlElement(name = "text-message", required = true)
    private String textMessage;

    @XmlElement(name = "subject")
    private String subject;

    @XmlElement(name = "numeric-message")
    private String numericMessage;

    @XmlElement(name = "event-severity")
    private String eventSeverity;

    @XmlElement(name = "parameter")
    private List<Parameter> parameterList = new ArrayList<>();

    /**
     * The varbind element
     */
    @XmlElement(name = "varbind")
    private Varbind varbind;

    public Notification() { }

    /**
     * 
     * 
     * @param vParameter
     * @throws IndexOutOfBoundsException if the index given is outside
     * the bounds of the collection
     */
    public void addParameter(final Parameter vParameter) throws IndexOutOfBoundsException {
        this.parameterList.add(vParameter);
    }

    /**
     * 
     * 
     * @param index
     * @param vParameter
     * @throws IndexOutOfBoundsException if the index given is outside
     * the bounds of the collection
     */
    public void addParameter(final int index, final Parameter vParameter) throws IndexOutOfBoundsException {
        this.parameterList.add(index, vParameter);
    }

    /**
     * Method enumerateParameter.
     * 
     * @return an Enumeration over all possible elements of this collection
     */
    public Enumeration<Parameter> enumerateParameter() {
        return Collections.enumeration(this.parameterList);
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
        
        if (obj instanceof Notification) {
            Notification temp = (Notification)obj;
            boolean equals = Objects.equals(temp.name, name)
                && Objects.equals(temp.status, status)
                && Objects.equals(temp.writeable, writeable)
                && Objects.equals(temp.uei, uei)
                && Objects.equals(temp.description, description)
                && Objects.equals(temp.rule, rule)
                && Objects.equals(temp.noticeQueue, noticeQueue)
                && Objects.equals(temp.destinationPath, destinationPath)
                && Objects.equals(temp.textMessage, textMessage)
                && Objects.equals(temp.subject, subject)
                && Objects.equals(temp.numericMessage, numericMessage)
                && Objects.equals(temp.eventSeverity, eventSeverity)
                && Objects.equals(temp.parameterList, parameterList)
                && Objects.equals(temp.varbind, varbind);
            return equals;
        }
        return false;
    }

    /**
     * Returns the value of field 'description'.
     * 
     * @return the value of field 'Description'.
     */
    public Optional<String> getDescription() {
        return Optional.ofNullable(this.description);
    }

    /**
     * Returns the value of field 'destinationPath'.
     * 
     * @return the value of field 'DestinationPath'.
     */
    public String getDestinationPath() {
        return this.destinationPath;
    }

    /**
     * Returns the value of field 'eventSeverity'.
     * 
     * @return the value of field 'EventSeverity'.
     */
    public Optional<String> getEventSeverity() {
        return Optional.ofNullable(this.eventSeverity);
    }

    /**
     * Returns the value of field 'name'.
     * 
     * @return the value of field 'Name'.
     */
    public String getName() {
        return this.name;
    }

    /**
     * Returns the value of field 'noticeQueue'.
     * 
     * @return the value of field 'NoticeQueue'.
     */
    public Optional<String> getNoticeQueue() {
        return Optional.ofNullable(this.noticeQueue);
    }

    /**
     * Returns the value of field 'numericMessage'.
     * 
     * @return the value of field 'NumericMessage'.
     */
    public Optional<String> getNumericMessage() {
        return Optional.ofNullable(this.numericMessage);
    }

    /**
     * Method getParameter.
     * 
     * @param index
     * @throws IndexOutOfBoundsException if the index given is outside
     * the bounds of the collection
     * @return the value of the Parameter
     * at the given index
     */
    public Parameter getParameter(final int index) throws IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this.parameterList.size()) {
            throw new IndexOutOfBoundsException("getParameter: Index value '" + index + "' not in range [0.." + (this.parameterList.size() - 1) + "]");
        }
        
        return (Parameter) parameterList.get(index);
    }

    /**
     * Method getParameter.Returns the contents of the collection in an Array. 
     * <p>Note:  Just in case the collection contents are changing in another
     * thread, we pass a 0-length Array of the correct type into the API call. 
     * This way we <i>know</i> that the Array returned is of exactly the correct
     * length.
     * 
     * @return this collection as an Array
     */
    public Parameter[] getParameter() {
        Parameter[] array = new Parameter[0];
        return (Parameter[]) this.parameterList.toArray(array);
    }

    /**
     * Method getParameterCollection.Returns a reference to 'parameterList'. No
     * type checking is performed on any modifications to the Vector.
     * 
     * @return a reference to the Vector backing this class
     */
    public List<Parameter> getParameterCollection() {
        return this.parameterList;
    }

    /**
     * Method getParameterCount.
     * 
     * @return the size of this collection
     */
    public int getParameterCount() {
        return this.parameterList.size();
    }

    /**
     * Returns the value of field 'rule'.
     * 
     * @return the value of field 'Rule'.
     */
    public String getRule() {
        return this.rule;
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
     * Returns the value of field 'subject'.
     * 
     * @return the value of field 'Subject'.
     */
    public Optional<String> getSubject() {
        return Optional.ofNullable(this.subject);
    }

    /**
     * Returns the value of field 'textMessage'.
     * 
     * @return the value of field 'TextMessage'.
     */
    public String getTextMessage() {
        return this.textMessage;
    }

    /**
     * Returns the value of field 'uei'.
     * 
     * @return the value of field 'Uei'.
     */
    public String getUei() {
        return this.uei;
    }

    /**
     * Returns the value of field 'varbind'. The field 'varbind' has the following
     * description: The varbind element
     * 
     * @return the value of field 'Varbind'.
     */
    public Varbind getVarbind() {
        return this.varbind;
    }

    /**
     * Returns the value of field 'writeable'.
     * 
     * @return the value of field 'Writeable'.
     */
    public String getWriteable() {
        return this.writeable != null ? this.writeable : DEFAULT_WRITEABLE;
    }

    /**
     * Method hashCode.
     * 
     * @return a hash code value for the object.
     */
    @Override
    public int hashCode() {
        int hash = Objects.hash(
            name, 
            status, 
            writeable, 
            uei, 
            description, 
            rule, 
            noticeQueue, 
            destinationPath, 
            textMessage, 
            subject, 
            numericMessage, 
            eventSeverity, 
            parameterList, 
            varbind);
        return hash;
    }

    /**
     * Method iterateParameter.
     * 
     * @return an Iterator over all possible elements in this collection
     */
    public Iterator<Parameter> iterateParameter() {
        return this.parameterList.iterator();
    }

    /**
     */
    public void removeAllParameter() {
        this.parameterList.clear();
    }

    /**
     * Method removeParameter.
     * 
     * @param vParameter
     * @return true if the object was removed from the collection.
     */
    public boolean removeParameter(final Parameter vParameter) {
        boolean removed = parameterList.remove(vParameter);
        return removed;
    }

    /**
     * Method removeParameterAt.
     * 
     * @param index
     * @return the element removed from the collection
     */
    public Parameter removeParameterAt(final int index) {
        Object obj = this.parameterList.remove(index);
        return (Parameter) obj;
    }

    /**
     * Sets the value of field 'description'.
     * 
     * @param description the value of field 'description'.
     */
    public void setDescription(final String description) {
        this.description = description;
    }

    /**
     * Sets the value of field 'destinationPath'.
     * 
     * @param destinationPath the value of field 'destinationPath'.
     */
    public void setDestinationPath(final String destinationPath) {
        checkNotNull(destinationPath, "Destination path is a required field!");
        this.destinationPath = destinationPath;
    }

    /**
     * Sets the value of field 'eventSeverity'.
     * 
     * @param eventSeverity the value of field 'eventSeverity'.
     */
    public void setEventSeverity(final String eventSeverity) {
        this.eventSeverity = eventSeverity;
    }

    /**
     * Sets the value of field 'name'.
     * 
     * @param name the value of field 'name'.
     */
    public void setName(final String name) {
        checkNotNull(name, "Name is a required field!");
        this.name = name;
    }

    /**
     * Sets the value of field 'noticeQueue'.
     * 
     * @param noticeQueue the value of field 'noticeQueue'.
     */
    public void setNoticeQueue(final String noticeQueue) {
        this.noticeQueue = noticeQueue;
    }

    /**
     * Sets the value of field 'numericMessage'.
     * 
     * @param numericMessage the value of field 'numericMessage'.
     */
    public void setNumericMessage(final String numericMessage) {
        this.numericMessage = numericMessage;
    }

    /**
     * 
     * 
     * @param index
     * @param vParameter
     * @throws IndexOutOfBoundsException if the index given is outside
     * the bounds of the collection
     */
    public void setParameter(final int index, final Parameter vParameter) throws IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this.parameterList.size()) {
            throw new IndexOutOfBoundsException("setParameter: Index value '" + index + "' not in range [0.." + (this.parameterList.size() - 1) + "]");
        }
        
        this.parameterList.set(index, vParameter);
    }

    /**
     * 
     * 
     * @param vParameterArray
     */
    public void setParameter(final Parameter[] vParameterArray) {
        //-- copy array
        parameterList.clear();
        
        for (int i = 0; i < vParameterArray.length; i++) {
                this.parameterList.add(vParameterArray[i]);
        }
    }

    /**
     * Sets the value of 'parameterList' by copying the given Vector. All elements
     * will be checked for type safety.
     * 
     * @param vParameterList the Vector to copy.
     */
    public void setParameter(final List<Parameter> vParameterList) {
        // copy vector
        this.parameterList.clear();
        
        this.parameterList.addAll(vParameterList);
    }

    /**
     * Sets the value of 'parameterList' by setting it to the given Vector. No
     * type checking is performed.
     * @deprecated
     * 
     * @param parameterList the Vector to set.
     */
    public void setParameterCollection(final List<Parameter> parameterList) {
        this.parameterList = parameterList == null? new ArrayList<>() : parameterList;
    }

    /**
     * Sets the value of field 'rule'.
     * 
     * @param rule the value of field 'rule'.
     */
    public void setRule(final String rule) {
        checkNotNull(rule, "Rule is a required field!");
        this.rule = rule;
    }

    /**
     * Sets the value of field 'status'.
     * 
     * @param status the value of field 'status'.
     */
    public void setStatus(final String status) {
        checkNotNull(status, "Status is a required field!");
        this.status = status;
    }

    /**
     * Sets the value of field 'subject'.
     * 
     * @param subject the value of field 'subject'.
     */
    public void setSubject(final String subject) {
        this.subject = subject;
    }

    /**
     * Sets the value of field 'textMessage'.
     * 
     * @param textMessage the value of field 'textMessage'.
     */
    public void setTextMessage(final String textMessage) {
        checkNotNull(textMessage, "Text message is a required field!");
        this.textMessage = textMessage;
    }

    /**
     * Sets the value of field 'uei'.
     * 
     * @param uei the value of field 'uei'.
     */
    public void setUei(final String uei) {
        checkNotNull(uei, "UEI is a required field!");
        this.uei = uei;
    }

    /**
     * Sets the value of field 'varbind'. The field 'varbind' has the following
     * description: The varbind element
     * 
     * @param varbind the value of field 'varbind'.
     */
    public void setVarbind(final Varbind varbind) {
        this.varbind = varbind;
    }

    /**
     * Sets the value of field 'writeable'.
     * 
     * @param writeable the value of field 'writeable'.
     */
    public void setWriteable(final String writeable) {
        this.writeable = writeable;
    }

    private void checkNotNull(final Object value, final String warning) {
        if (value == null) {
            throw new IllegalArgumentException(warning);
        }
    }
}

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

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Top-level element for the notifications.xml configuration
 *  file.
 * 
 * @version $Revision$ $Date$
 */
@XmlRootElement(name = "notifications")
@XmlAccessorType(XmlAccessType.FIELD)
public class Notifications implements java.io.Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * Header containing information about this configuration
     *  file.
     */
    @XmlElement(name = "header", required = true)
    private Header header;

    @XmlElement(name = "notification", required = true)
    private List<Notification> notificationList = new ArrayList<>();

    public Notifications() { }

    /**
     * 
     * 
     * @param vNotification
     * @throws IndexOutOfBoundsException if the index given is outside
     * the bounds of the collection
     */
    public void addNotification(final Notification vNotification) throws IndexOutOfBoundsException {
        this.notificationList.add(vNotification);
    }

    /**
     * 
     * 
     * @param index
     * @param vNotification
     * @throws IndexOutOfBoundsException if the index given is outside
     * the bounds of the collection
     */
    public void addNotification(final int index, final Notification vNotification) throws IndexOutOfBoundsException {
        this.notificationList.add(index, vNotification);
    }

    /**
     * Method enumerateNotification.
     * 
     * @return an Enumeration over all possible elements of this collection
     */
    public Enumeration<Notification> enumerateNotification() {
        return Collections.enumeration(this.notificationList);
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

        if (obj instanceof Notifications) {
            Notifications temp = (Notifications)obj;
            boolean equals = Objects.equals(temp.header, header)
                && Objects.equals(temp.notificationList, notificationList);
            return equals;
        }
        return false;
    }

    /**
     * Returns the value of field 'header'. The field 'header' has the following
     * description: Header containing information about this configuration
     *  file.
     * 
     * @return the value of field 'Header'.
     */
    public Header getHeader() {
        return this.header;
    }

    /**
     * Method getNotification.
     * 
     * @param index
     * @throws IndexOutOfBoundsException if the index given is outside
     * the bounds of the collection
     * @return the value of the
     * Notification at the given index
     */
    public Notification getNotification(final int index) throws IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this.notificationList.size()) {
            throw new IndexOutOfBoundsException("getNotification: Index value '" + index + "' not in range [0.." + (this.notificationList.size() - 1) + "]");
        }
        
        return (Notification) notificationList.get(index);
    }

    /**
     * Method getNotification.Returns the contents of the collection in an Array. 
     * <p>Note:  Just in case the collection contents are changing in another
     * thread, we pass a 0-length Array of the correct type into the API call. 
     * This way we <i>know</i> that the Array returned is of exactly the correct
     * length.
     * 
     * @return this collection as an Array
     */
    public Notification[] getNotification() {
        Notification[] array = new Notification[0];
        return (Notification[]) this.notificationList.toArray(array);
    }

    /**
     * Method getNotificationCollection.Returns a reference to 'notificationList'.
     * No type checking is performed on any modifications to the Vector.
     * 
     * @return a reference to the Vector backing this class
     */
    public List<Notification> getNotificationCollection() {
        return this.notificationList;
    }

    /**
     * Method getNotificationCount.
     * 
     * @return the size of this collection
     */
    public int getNotificationCount() {
        return this.notificationList.size();
    }

    /**
     * Method hashCode.
     * 
     * @return a hash code value for the object.
     */
    @Override
    public int hashCode() {
        int hash = Objects.hash(
            header, 
            notificationList);
        return hash;
    }

    /**
     * Method iterateNotification.
     * 
     * @return an Iterator over all possible elements in this collection
     */
    public Iterator<Notification> iterateNotification() {
        return this.notificationList.iterator();
    }

    /**
     */
    public void removeAllNotification() {
        this.notificationList.clear();
    }

    /**
     * Method removeNotification.
     * 
     * @param vNotification
     * @return true if the object was removed from the collection.
     */
    public boolean removeNotification(final Notification vNotification) {
        boolean removed = notificationList.remove(vNotification);
        return removed;
    }

    /**
     * Method removeNotificationAt.
     * 
     * @param index
     * @return the element removed from the collection
     */
    public Notification removeNotificationAt(final int index) {
        Object obj = this.notificationList.remove(index);
        return (Notification) obj;
    }

    /**
     * Sets the value of field 'header'. The field 'header' has the following
     * description: Header containing information about this configuration
     *  file.
     * 
     * @param header the value of field 'header'.
     */
    public void setHeader(final Header header) {
        this.header = header;
    }

    /**
     * 
     * 
     * @param index
     * @param vNotification
     * @throws IndexOutOfBoundsException if the index given is outside
     * the bounds of the collection
     */
    public void setNotification(final int index, final Notification vNotification) throws IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this.notificationList.size()) {
            throw new IndexOutOfBoundsException("setNotification: Index value '" + index + "' not in range [0.." + (this.notificationList.size() - 1) + "]");
        }
        
        this.notificationList.set(index, vNotification);
    }

    /**
     * 
     * 
     * @param vNotificationArray
     */
    public void setNotification(final Notification[] vNotificationArray) {
        //-- copy array
        notificationList.clear();
        
        for (int i = 0; i < vNotificationArray.length; i++) {
                this.notificationList.add(vNotificationArray[i]);
        }
    }

    /**
     * Sets the value of 'notificationList' by copying the given Vector. All
     * elements will be checked for type safety.
     * 
     * @param vNotificationList the Vector to copy.
     */
    public void setNotification(final List<Notification> vNotificationList) {
        // copy vector
        this.notificationList.clear();
        
        this.notificationList.addAll(vNotificationList);
    }

    /**
     * Sets the value of 'notificationList' by setting it to the given Vector. No
     * type checking is performed.
     * @deprecated
     * 
     * @param notificationList the Vector to set.
     */
    public void setNotificationCollection(final List<Notification> notificationList) {
        this.notificationList = notificationList == null? new ArrayList<>() : notificationList;
    }

}

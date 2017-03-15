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

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Class AutoAcknowledgeAlarm.
 * 
 * @version $Revision$ $Date$
 */
@XmlRootElement(name = "auto-acknowledge-alarm")
@XmlAccessorType(XmlAccessType.FIELD)
public class AutoAcknowledgeAlarm implements java.io.Serializable {
    private static final long serialVersionUID = 1L;

    private static final String DEFAULT_RESOLUTION_PREFIX = "RESOLVED: ";

    @XmlAttribute(name = "resolution-prefix")
    private String resolutionPrefix;

    @XmlAttribute(name = "notify")
    private Boolean notify;

    @XmlElement(name = "uei")
    private List<String> ueiList = new ArrayList<>();

    public AutoAcknowledgeAlarm() { }

    /**
     * 
     * 
     * @param vUei
     * @throws IndexOutOfBoundsException if the index given is outside
     * the bounds of the collection
     */
    public void addUei(final String vUei) throws IndexOutOfBoundsException {
        this.ueiList.add(vUei);
    }

    /**
     * 
     * 
     * @param index
     * @param vUei
     * @throws IndexOutOfBoundsException if the index given is outside
     * the bounds of the collection
     */
    public void addUei(final int index, final String vUei) throws IndexOutOfBoundsException {
        this.ueiList.add(index, vUei);
    }

    /**
     */
    public void deleteNotify() {
        this.notify= null;
    }

    /**
     * Method enumerateUei.
     * 
     * @return an Enumeration over all possible elements of this collection
     */
    public Enumeration<String> enumerateUei() {
        return Collections.enumeration(this.ueiList);
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
        
        if (obj instanceof AutoAcknowledgeAlarm) {
            AutoAcknowledgeAlarm temp = (AutoAcknowledgeAlarm)obj;
            boolean equals = Objects.equals(temp.resolutionPrefix, resolutionPrefix)
                && Objects.equals(temp.notify, notify)
                && Objects.equals(temp.ueiList, ueiList);
            return equals;
        }
        return false;
    }

    /**
     * Returns the value of field 'notify'.
     * 
     * @return the value of field 'Notify'.
     */
    public Boolean getNotify() {
        return this.notify != null ? this.notify : Boolean.valueOf("true");
    }

    /**
     * Returns the value of field 'resolutionPrefix'.
     * 
     * @return the value of field 'ResolutionPrefix'.
     */
    public String getResolutionPrefix() {
        return this.resolutionPrefix != null ? this.resolutionPrefix : DEFAULT_RESOLUTION_PREFIX;
    }

    /**
     * Method getUei.
     * 
     * @param index
     * @throws IndexOutOfBoundsException if the index given is outside
     * the bounds of the collection
     * @return the value of the String at the given index
     */
    public String getUei(final int index) throws IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this.ueiList.size()) {
            throw new IndexOutOfBoundsException("getUei: Index value '" + index + "' not in range [0.." + (this.ueiList.size() - 1) + "]");
        }
        
        return (String) ueiList.get(index);
    }

    /**
     * Method getUei.Returns the contents of the collection in an Array.  <p>Note:
     *  Just in case the collection contents are changing in another thread, we
     * pass a 0-length Array of the correct type into the API call.  This way we
     * <i>know</i> that the Array returned is of exactly the correct length.
     * 
     * @return this collection as an Array
     */
    public String[] getUei() {
        String[] array = new String[0];
        return (String[]) this.ueiList.toArray(array);
    }

    /**
     * Method getUeiCollection.Returns a reference to 'ueiList'. No type checking
     * is performed on any modifications to the Vector.
     * 
     * @return a reference to the Vector backing this class
     */
    public List<String> getUeiCollection() {
        return this.ueiList;
    }

    /**
     * Method getUeiCount.
     * 
     * @return the size of this collection
     */
    public int getUeiCount() {
        return this.ueiList.size();
    }

    /**
     * Method hasNotify.
     * 
     * @return true if at least one Notify has been added
     */
    public boolean hasNotify() {
        return this.notify != null;
    }

    /**
     * Method hashCode.
     * 
     * @return a hash code value for the object.
     */
    @Override
    public int hashCode() {
        int hash = Objects.hash(
            resolutionPrefix, 
            notify, 
            ueiList);
        return hash;
    }

    /**
     * Returns the value of field 'notify'.
     * 
     * @return the value of field 'Notify'.
     */
    public Boolean isNotify() {
        return this.notify != null ? this.notify : Boolean.valueOf("true");
    }

    /**
     * Method iterateUei.
     * 
     * @return an Iterator over all possible elements in this collection
     */
    public Iterator<String> iterateUei() {
        return this.ueiList.iterator();
    }

    /**
     */
    public void removeAllUei() {
        this.ueiList.clear();
    }

    /**
     * Method removeUei.
     * 
     * @param vUei
     * @return true if the object was removed from the collection.
     */
    public boolean removeUei(final String vUei) {
        boolean removed = ueiList.remove(vUei);
        return removed;
    }

    /**
     * Method removeUeiAt.
     * 
     * @param index
     * @return the element removed from the collection
     */
    public String removeUeiAt(final int index) {
        Object obj = this.ueiList.remove(index);
        return (String) obj;
    }

    /**
     * Sets the value of field 'notify'.
     * 
     * @param notify the value of field 'notify'.
     */
    public void setNotify(final Boolean notify) {
        this.notify = notify;
    }

    /**
     * Sets the value of field 'resolutionPrefix'.
     * 
     * @param resolutionPrefix the value of field 'resolutionPrefix'.
     */
    public void setResolutionPrefix(final String resolutionPrefix) {
        this.resolutionPrefix = resolutionPrefix;
    }

    /**
     * 
     * 
     * @param index
     * @param vUei
     * @throws IndexOutOfBoundsException if the index given is outside
     * the bounds of the collection
     */
    public void setUei(final int index, final String vUei) throws IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this.ueiList.size()) {
            throw new IndexOutOfBoundsException("setUei: Index value '" + index + "' not in range [0.." + (this.ueiList.size() - 1) + "]");
        }
        
        this.ueiList.set(index, vUei);
    }

    /**
     * 
     * 
     * @param vUeiArray
     */
    public void setUei(final String[] vUeiArray) {
        //-- copy array
        ueiList.clear();
        
        for (int i = 0; i < vUeiArray.length; i++) {
                this.ueiList.add(vUeiArray[i]);
        }
    }

    /**
     * Sets the value of 'ueiList' by copying the given Vector. All elements will
     * be checked for type safety.
     * 
     * @param vUeiList the Vector to copy.
     */
    public void setUei(final List<String> vUeiList) {
        // copy vector
        this.ueiList.clear();
        
        this.ueiList.addAll(vUeiList);
    }

    /**
     * Sets the value of 'ueiList' by setting it to the given Vector. No type
     * checking is performed.
     * @deprecated
     * 
     * @param ueiList the Vector to set.
     */
    public void setUeiCollection(final List<String> ueiList) {
        this.ueiList = ueiList == null? new ArrayList<>() : ueiList;
    }

}

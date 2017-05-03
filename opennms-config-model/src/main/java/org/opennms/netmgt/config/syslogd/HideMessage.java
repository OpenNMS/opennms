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

package org.opennms.netmgt.config.syslogd;


import java.util.Objects;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Class HideMessage.
 * 
 * @version $Revision$ $Date$
 */
@XmlRootElement(name = "hideMessage")
@XmlAccessorType(XmlAccessType.FIELD)
public class HideMessage implements java.io.Serializable {
    private static final long serialVersionUID = 1L;


    /**
     * List of substrings or regexes that, when matched, signal
     *  that the message has sensitive contents and should
     *  therefore be hidden
     *  
     */
    @XmlElement(name = "hideMatch")
    private java.util.List<org.opennms.netmgt.config.syslogd.HideMatch> hideMatchList;

    public HideMessage() {
        this.hideMatchList = new java.util.ArrayList<org.opennms.netmgt.config.syslogd.HideMatch>();
    }

    /**
     * 
     * 
     * @param vHideMatch
     * @throws IndexOutOfBoundsException if the index given is outside
     * the bounds of the collection
     */
    public void addHideMatch(final org.opennms.netmgt.config.syslogd.HideMatch vHideMatch) throws IndexOutOfBoundsException {
        this.hideMatchList.add(vHideMatch);
    }

    /**
     * 
     * 
     * @param index
     * @param vHideMatch
     * @throws IndexOutOfBoundsException if the index given is outside
     * the bounds of the collection
     */
    public void addHideMatch(final int index, final org.opennms.netmgt.config.syslogd.HideMatch vHideMatch) throws IndexOutOfBoundsException {
        this.hideMatchList.add(index, vHideMatch);
    }

    /**
     * Method enumerateHideMatch.
     * 
     * @return an Enumeration over all possible elements of this collection
     */
    public java.util.Enumeration<org.opennms.netmgt.config.syslogd.HideMatch> enumerateHideMatch() {
        return java.util.Collections.enumeration(this.hideMatchList);
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
        
        if (obj instanceof HideMessage) {
            HideMessage temp = (HideMessage)obj;
            boolean equals = Objects.equals(temp.hideMatchList, hideMatchList);
            return equals;
        }
        return false;
    }

    /**
     * Method getHideMatch.
     * 
     * @param index
     * @throws IndexOutOfBoundsException if the index given is outside
     * the bounds of the collection
     * @return the value of the org.opennms.netmgt.config.syslogd.types.HideMatch
     * at the given index
     */
    public org.opennms.netmgt.config.syslogd.HideMatch getHideMatch(final int index) throws IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this.hideMatchList.size()) {
            throw new IndexOutOfBoundsException("getHideMatch: Index value '" + index + "' not in range [0.." + (this.hideMatchList.size() - 1) + "]");
        }
        
        return (org.opennms.netmgt.config.syslogd.HideMatch) hideMatchList.get(index);
    }

    /**
     * Method getHideMatch.Returns the contents of the collection in an Array. 
     * <p>Note:  Just in case the collection contents are changing in another
     * thread, we pass a 0-length Array of the correct type into the API call. 
     * This way we <i>know</i> that the Array returned is of exactly the correct
     * length.
     * 
     * @return this collection as an Array
     */
    public org.opennms.netmgt.config.syslogd.HideMatch[] getHideMatch() {
        org.opennms.netmgt.config.syslogd.HideMatch[] array = new org.opennms.netmgt.config.syslogd.HideMatch[0];
        return (org.opennms.netmgt.config.syslogd.HideMatch[]) this.hideMatchList.toArray(array);
    }

    /**
     * Method getHideMatchCollection.Returns a reference to 'hideMatchList'. No
     * type checking is performed on any modifications to the Vector.
     * 
     * @return a reference to the Vector backing this class
     */
    public java.util.List<org.opennms.netmgt.config.syslogd.HideMatch> getHideMatchCollection() {
        return this.hideMatchList;
    }

    /**
     * Method getHideMatchCount.
     * 
     * @return the size of this collection
     */
    public int getHideMatchCount() {
        return this.hideMatchList.size();
    }

    /**
     * Method hashCode.
     * 
     * @return a hash code value for the object.
     */
    @Override
    public int hashCode() {
        int hash = Objects.hash(
            hideMatchList);
        return hash;
    }

    /**
     * Method iterateHideMatch.
     * 
     * @return an Iterator over all possible elements in this collection
     */
    public java.util.Iterator<org.opennms.netmgt.config.syslogd.HideMatch> iterateHideMatch() {
        return this.hideMatchList.iterator();
    }

    /**
     */
    public void removeAllHideMatch() {
        this.hideMatchList.clear();
    }

    /**
     * Method removeHideMatch.
     * 
     * @param vHideMatch
     * @return true if the object was removed from the collection.
     */
    public boolean removeHideMatch(final org.opennms.netmgt.config.syslogd.HideMatch vHideMatch) {
        boolean removed = hideMatchList.remove(vHideMatch);
        return removed;
    }

    /**
     * Method removeHideMatchAt.
     * 
     * @param index
     * @return the element removed from the collection
     */
    public org.opennms.netmgt.config.syslogd.HideMatch removeHideMatchAt(final int index) {
        Object obj = this.hideMatchList.remove(index);
        return (org.opennms.netmgt.config.syslogd.HideMatch) obj;
    }

    /**
     * 
     * 
     * @param index
     * @param vHideMatch
     * @throws IndexOutOfBoundsException if the index given is outside
     * the bounds of the collection
     */
    public void setHideMatch(final int index, final org.opennms.netmgt.config.syslogd.HideMatch vHideMatch) throws IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this.hideMatchList.size()) {
            throw new IndexOutOfBoundsException("setHideMatch: Index value '" + index + "' not in range [0.." + (this.hideMatchList.size() - 1) + "]");
        }
        
        this.hideMatchList.set(index, vHideMatch);
    }

    /**
     * 
     * 
     * @param vHideMatchArray
     */
    public void setHideMatch(final org.opennms.netmgt.config.syslogd.HideMatch[] vHideMatchArray) {
        //-- copy array
        hideMatchList.clear();
        
        for (int i = 0; i < vHideMatchArray.length; i++) {
                this.hideMatchList.add(vHideMatchArray[i]);
        }
    }

    /**
     * Sets the value of 'hideMatchList' by copying the given Vector. All elements
     * will be checked for type safety.
     * 
     * @param vHideMatchList the Vector to copy.
     */
    public void setHideMatch(final java.util.List<org.opennms.netmgt.config.syslogd.HideMatch> vHideMatchList) {
        // copy vector
        this.hideMatchList.clear();
        
        this.hideMatchList.addAll(vHideMatchList);
    }

    /**
     * Sets the value of 'hideMatchList' by setting it to the given Vector. No
     * type checking is performed.
     * @deprecated
     * 
     * @param hideMatchList the Vector to set.
     */
    public void setHideMatchCollection(final java.util.List<org.opennms.netmgt.config.syslogd.HideMatch> hideMatchList) {
        this.hideMatchList = hideMatchList;
    }

}

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
 * Class AutoAcknowledge.
 * 
 * @version $Revision$ $Date$
 */
@XmlRootElement(name = "auto-acknowledge")
@XmlAccessorType(XmlAccessType.FIELD)
public class AutoAcknowledge implements java.io.Serializable {
    private static final long serialVersionUID = 1L;

    private static final String DEFAULT_RESOLUTION_PREFIX = "RESOLVED: ";

    @XmlAttribute(name = "resolution-prefix")
    private String resolutionPrefix;

    @XmlAttribute(name = "uei", required = true)
    private String uei;

    @XmlAttribute(name = "acknowledge", required = true)
    private String acknowledge;

    @XmlAttribute(name = "notify")
    private Boolean notify;

    @XmlElement(name = "match", required = true)
    private List<String> matchList = new ArrayList<>();

    public AutoAcknowledge() { }

    /**
     * 
     * 
     * @param vMatch
     * @throws IndexOutOfBoundsException if the index given is outside
     * the bounds of the collection
     */
    public void addMatch(final String vMatch) throws IndexOutOfBoundsException {
        this.matchList.add(vMatch);
    }

    /**
     * 
     * 
     * @param index
     * @param vMatch
     * @throws IndexOutOfBoundsException if the index given is outside
     * the bounds of the collection
     */
    public void addMatch(final int index, final String vMatch) throws IndexOutOfBoundsException {
        this.matchList.add(index, vMatch);
    }

    /**
     */
    public void deleteNotify() {
        this.notify= null;
    }

    /**
     * Method enumerateMatch.
     * 
     * @return an Enumeration over all possible elements of this collection
     */
    public Enumeration<String> enumerateMatch() {
        return Collections.enumeration(this.matchList);
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
        
        if (obj instanceof AutoAcknowledge) {
            AutoAcknowledge temp = (AutoAcknowledge)obj;
            boolean equals = Objects.equals(temp.resolutionPrefix, resolutionPrefix)
                && Objects.equals(temp.uei, uei)
                && Objects.equals(temp.acknowledge, acknowledge)
                && Objects.equals(temp.notify, notify)
                && Objects.equals(temp.matchList, matchList);
            return equals;
        }
        return false;
    }

    /**
     * Returns the value of field 'acknowledge'.
     * 
     * @return the value of field 'Acknowledge'.
     */
    public String getAcknowledge() {
        return this.acknowledge;
    }

    /**
     * Method getMatch.
     * 
     * @param index
     * @throws IndexOutOfBoundsException if the index given is outside
     * the bounds of the collection
     * @return the value of the String at the given index
     */
    public String getMatch(final int index) throws IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this.matchList.size()) {
            throw new IndexOutOfBoundsException("getMatch: Index value '" + index + "' not in range [0.." + (this.matchList.size() - 1) + "]");
        }
        
        return (String) matchList.get(index);
    }

    /**
     * Method getMatch.Returns the contents of the collection in an Array. 
     * <p>Note:  Just in case the collection contents are changing in another
     * thread, we pass a 0-length Array of the correct type into the API call. 
     * This way we <i>know</i> that the Array returned is of exactly the correct
     * length.
     * 
     * @return this collection as an Array
     */
    public String[] getMatch() {
        String[] array = new String[0];
        return (String[]) this.matchList.toArray(array);
    }

    /**
     * Method getMatchCollection.Returns a reference to 'matchList'. No type
     * checking is performed on any modifications to the Vector.
     * 
     * @return a reference to the Vector backing this class
     */
    public List<String> getMatchCollection() {
        return this.matchList;
    }

    /**
     * Method getMatchCount.
     * 
     * @return the size of this collection
     */
    public int getMatchCount() {
        return this.matchList.size();
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
     * Returns the value of field 'uei'.
     * 
     * @return the value of field 'Uei'.
     */
    public String getUei() {
        return this.uei;
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
            uei, 
            acknowledge, 
            notify, 
            matchList);
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
     * Method iterateMatch.
     * 
     * @return an Iterator over all possible elements in this collection
     */
    public Iterator<String> iterateMatch() {
        return this.matchList.iterator();
    }

    /**
     */
    public void removeAllMatch() {
        this.matchList.clear();
    }

    /**
     * Method removeMatch.
     * 
     * @param vMatch
     * @return true if the object was removed from the collection.
     */
    public boolean removeMatch(final String vMatch) {
        boolean removed = matchList.remove(vMatch);
        return removed;
    }

    /**
     * Method removeMatchAt.
     * 
     * @param index
     * @return the element removed from the collection
     */
    public String removeMatchAt(final int index) {
        Object obj = this.matchList.remove(index);
        return (String) obj;
    }

    /**
     * Sets the value of field 'acknowledge'.
     * 
     * @param acknowledge the value of field 'acknowledge'.
     */
    public void setAcknowledge(final String acknowledge) {
        if (uei == acknowledge) {
            throw new IllegalArgumentException("acknowledge is a required field!");
        }
        this.acknowledge = acknowledge;
    }

    /**
     * 
     * 
     * @param index
     * @param vMatch
     * @throws IndexOutOfBoundsException if the index given is outside
     * the bounds of the collection
     */
    public void setMatch(final int index, final String vMatch) throws IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this.matchList.size()) {
            throw new IndexOutOfBoundsException("setMatch: Index value '" + index + "' not in range [0.." + (this.matchList.size() - 1) + "]");
        }
        
        this.matchList.set(index, vMatch);
    }

    /**
     * 
     * 
     * @param vMatchArray
     */
    public void setMatch(final String[] vMatchArray) {
        //-- copy array
        matchList.clear();
        
        for (int i = 0; i < vMatchArray.length; i++) {
                this.matchList.add(vMatchArray[i]);
        }
    }

    /**
     * Sets the value of 'matchList' by copying the given Vector. All elements
     * will be checked for type safety.
     * 
     * @param vMatchList the Vector to copy.
     */
    public void setMatch(final List<String> vMatchList) {
        // copy vector
        this.matchList.clear();
        
        this.matchList.addAll(vMatchList);
    }

    /**
     * Sets the value of 'matchList' by setting it to the given Vector. No type
     * checking is performed.
     * @deprecated
     * 
     * @param matchList the Vector to set.
     */
    public void setMatchCollection(final List<String> matchList) {
        this.matchList = matchList == null? new ArrayList<>() : matchList;
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
     * Sets the value of field 'uei'.
     * 
     * @param uei the value of field 'uei'.
     */
    public void setUei(final String uei) {
        if (uei == null) {
            throw new IllegalArgumentException("UEI is a required field!");
        }
        this.uei = uei;
    }

}

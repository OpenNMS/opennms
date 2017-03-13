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

package org.opennms.netmgt.config.microblog;

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
 * Microblog configuration groups
 *  
 * 
 * @version $Revision$ $Date$
 */
@XmlRootElement(name = "microblog-configuration")
@XmlAccessorType(XmlAccessType.FIELD)
public class MicroblogConfiguration implements java.io.Serializable {
    private static final long serialVersionUID = 1L;

    @XmlAttribute(name = "default-microblog-profile-name", required = true)
    private String defaultMicroblogProfileName;

    /**
     * This entity defines the parameters for a microblog service.
     *  
     */
    @XmlElement(name = "microblog-profile", required = true)
    private List<MicroblogProfile> microblogProfileList = new ArrayList<>();

    /**
     * 
     * 
     * @param vMicroblogProfile
     * @throws IndexOutOfBoundsException if the index given is outside
     * the bounds of the collection
     */
    public void addMicroblogProfile(final MicroblogProfile vMicroblogProfile) throws IndexOutOfBoundsException {
        this.microblogProfileList.add(vMicroblogProfile);
    }

    /**
     * 
     * 
     * @param index
     * @param vMicroblogProfile
     * @throws IndexOutOfBoundsException if the index given is outside
     * the bounds of the collection
     */
    public void addMicroblogProfile(final int index, final MicroblogProfile vMicroblogProfile) throws IndexOutOfBoundsException {
        this.microblogProfileList.add(index, vMicroblogProfile);
    }

    /**
     * Method enumerateMicroblogProfile.
     * 
     * @return an Enumeration over all possible elements of this collection
     */
    public Enumeration<MicroblogProfile> enumerateMicroblogProfile() {
        return Collections.enumeration(this.microblogProfileList);
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
        
        if (obj instanceof MicroblogConfiguration) {
            MicroblogConfiguration temp = (MicroblogConfiguration)obj;
            boolean equals = Objects.equals(temp.defaultMicroblogProfileName, defaultMicroblogProfileName)
                && Objects.equals(temp.microblogProfileList, microblogProfileList);
            return equals;
        }
        return false;
    }

    /**
     * Returns the value of field 'defaultMicroblogProfileName'.
     * 
     * @return the value of field 'DefaultMicroblogProfileName'.
     */
    public String getDefaultMicroblogProfileName() {
        return this.defaultMicroblogProfileName;
    }

    /**
     * Method getMicroblogProfile.
     * 
     * @param index
     * @throws IndexOutOfBoundsException if the index given is outside
     * the bounds of the collection
     * @return the value of the
     * MicroblogProfile at the given index
     */
    public MicroblogProfile getMicroblogProfile(final int index) throws IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this.microblogProfileList.size()) {
            throw new IndexOutOfBoundsException("getMicroblogProfile: Index value '" + index + "' not in range [0.." + (this.microblogProfileList.size() - 1) + "]");
        }
        
        return (MicroblogProfile) microblogProfileList.get(index);
    }

    /**
     * Method getMicroblogProfile.Returns the contents of the collection in an
     * Array.  <p>Note:  Just in case the collection contents are changing in
     * another thread, we pass a 0-length Array of the correct type into the API
     * call.  This way we <i>know</i> that the Array returned is of exactly the
     * correct length.
     * 
     * @return this collection as an Array
     */
    public MicroblogProfile[] getMicroblogProfile() {
        MicroblogProfile[] array = new MicroblogProfile[0];
        return (MicroblogProfile[]) this.microblogProfileList.toArray(array);
    }

    /**
     * Method getMicroblogProfileCollection.Returns a reference to
     * 'microblogProfileList'. No type checking is performed on any modifications
     * to the Vector.
     * 
     * @return a reference to the Vector backing this class
     */
    public List<MicroblogProfile> getMicroblogProfileCollection() {
        return this.microblogProfileList;
    }

    /**
     * Method getMicroblogProfileCount.
     * 
     * @return the size of this collection
     */
    public int getMicroblogProfileCount() {
        return this.microblogProfileList.size();
    }

    /**
     * Method hashCode.
     * 
     * @return a hash code value for the object.
     */
    @Override
    public int hashCode() {
        int hash = Objects.hash(
            defaultMicroblogProfileName, 
            microblogProfileList);
        return hash;
    }

    /**
     * Method iterateMicroblogProfile.
     * 
     * @return an Iterator over all possible elements in this collection
     */
    public Iterator<MicroblogProfile> iterateMicroblogProfile() {
        return this.microblogProfileList.iterator();
    }

    /**
     */
    public void removeAllMicroblogProfile() {
        this.microblogProfileList.clear();
    }

    /**
     * Method removeMicroblogProfile.
     * 
     * @param vMicroblogProfile
     * @return true if the object was removed from the collection.
     */
    public boolean removeMicroblogProfile(final MicroblogProfile vMicroblogProfile) {
        boolean removed = microblogProfileList.remove(vMicroblogProfile);
        return removed;
    }

    /**
     * Method removeMicroblogProfileAt.
     * 
     * @param index
     * @return the element removed from the collection
     */
    public MicroblogProfile removeMicroblogProfileAt(final int index) {
        Object obj = this.microblogProfileList.remove(index);
        return (MicroblogProfile) obj;
    }

    /**
     * Sets the value of field 'defaultMicroblogProfileName'.
     * 
     * @param defaultMicroblogProfileName the value of field
     * 'defaultMicroblogProfileName'.
     */
    public void setDefaultMicroblogProfileName(final String defaultMicroblogProfileName) {
        this.defaultMicroblogProfileName = defaultMicroblogProfileName;
    }

    /**
     * 
     * 
     * @param index
     * @param vMicroblogProfile
     * @throws IndexOutOfBoundsException if the index given is outside
     * the bounds of the collection
     */
    public void setMicroblogProfile(final int index, final MicroblogProfile vMicroblogProfile) throws IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this.microblogProfileList.size()) {
            throw new IndexOutOfBoundsException("setMicroblogProfile: Index value '" + index + "' not in range [0.." + (this.microblogProfileList.size() - 1) + "]");
        }
        
        this.microblogProfileList.set(index, vMicroblogProfile);
    }

    /**
     * 
     * 
     * @param vMicroblogProfileArray
     */
    public void setMicroblogProfile(final MicroblogProfile[] vMicroblogProfileArray) {
        //-- copy array
        microblogProfileList.clear();
        
        for (int i = 0; i < vMicroblogProfileArray.length; i++) {
                this.microblogProfileList.add(vMicroblogProfileArray[i]);
        }
    }

    /**
     * Sets the value of 'microblogProfileList' by copying the given Vector. All
     * elements will be checked for type safety.
     * 
     * @param vMicroblogProfileList the Vector to copy.
     */
    public void setMicroblogProfile(final List<MicroblogProfile> vMicroblogProfileList) {
        // copy vector
        this.microblogProfileList.clear();
        
        this.microblogProfileList.addAll(vMicroblogProfileList);
    }

    /**
     * Sets the value of 'microblogProfileList' by setting it to the given Vector.
     * No type checking is performed.
     * @deprecated
     * 
     * @param microblogProfileList the Vector to set.
     */
    public void setMicroblogProfileCollection(final List<MicroblogProfile> microblogProfileList) {
        this.microblogProfileList = microblogProfileList;
    }

}

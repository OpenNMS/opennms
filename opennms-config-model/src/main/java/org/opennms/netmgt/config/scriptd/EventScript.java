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

package org.opennms.netmgt.config.scriptd;

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
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.eclipse.persistence.oxm.annotations.XmlPath;

/**
 * Class EventScript.
 * 
 * @version $Revision$ $Date$
 */
@XmlRootElement(name = "event-script")
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "", propOrder = {
        "language",
        "ueiList",
        "content"
    })
public class EventScript implements java.io.Serializable {
    private static final long serialVersionUID = 1L;

    private static final String DEFAULT_CONTENT = "";

    @XmlAttribute(name = "language", required = true)
    private String language;

    @XmlElement(name = "uei")
    private List<Uei> ueiList = new ArrayList<>();

    /**
     * internal content storage
     *
     * Work-around for capture the @XmlValue along side @XmlElements
     */
    @XmlPath(".")
    @XmlJavaTypeAdapter(MixedContentAdapter.class)
    private String content;

    /**
     * 
     * 
     * @param vUei
     * @throws IndexOutOfBoundsException if the index given is outside
     * the bounds of the collection
     */
    public void addUei(final Uei vUei) throws IndexOutOfBoundsException {
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
    public void addUei(final int index, final Uei vUei) throws IndexOutOfBoundsException {
        this.ueiList.add(index, vUei);
    }

    /**
     * Method enumerateUei.
     * 
     * @return an Enumeration over all possible elements of this collection
     */
    public Enumeration<Uei> enumerateUei() {
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
        
        if (obj instanceof EventScript) {
            EventScript temp = (EventScript)obj;
            boolean equals = Objects.equals(temp.language, language)
                && Objects.equals(temp.content, content)
                && Objects.equals(temp.ueiList, ueiList);
            return equals;
        }
        return false;
    }

    /**
     * Returns the value of field 'content'. The field 'content' has the following
     * description: internal content storage
     * 
     * @return the value of field 'Content'.
     */
    public String getContent() {
        return content != null ? content : DEFAULT_CONTENT;
    }

    /**
     * Returns the value of field 'language'.
     * 
     * @return the value of field 'Language'.
     */
    public String getLanguage() {
        return this.language;
    }

    /**
     * Method getUei.
     * 
     * @param index
     * @throws IndexOutOfBoundsException if the index given is outside
     * the bounds of the collection
     * @return the value of the Uei at the given
     * index
     */
    public Uei getUei(final int index) throws IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this.ueiList.size()) {
            throw new IndexOutOfBoundsException("getUei: Index value '" + index + "' not in range [0.." + (this.ueiList.size() - 1) + "]");
        }
        
        return (Uei) ueiList.get(index);
    }

    /**
     * Method getUei.Returns the contents of the collection in an Array.  <p>Note:
     *  Just in case the collection contents are changing in another thread, we
     * pass a 0-length Array of the correct type into the API call.  This way we
     * <i>know</i> that the Array returned is of exactly the correct length.
     * 
     * @return this collection as an Array
     */
    public Uei[] getUei() {
        Uei[] array = new Uei[0];
        return (Uei[]) this.ueiList.toArray(array);
    }

    /**
     * Method getUeiCollection.Returns a reference to 'ueiList'. No type checking
     * is performed on any modifications to the Vector.
     * 
     * @return a reference to the Vector backing this class
     */
    public List<Uei> getUeiCollection() {
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
     * Method hashCode.
     * 
     * @return a hash code value for the object.
     */
    @Override
    public int hashCode() {
        int hash = Objects.hash(
            language, 
            content, 
            ueiList);
        return hash;
    }

    /**
     * Method iterateUei.
     * 
     * @return an Iterator over all possible elements in this collection
     */
    public Iterator<Uei> iterateUei() {
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
    public boolean removeUei(final Uei vUei) {
        boolean removed = ueiList.remove(vUei);
        return removed;
    }

    /**
     * Method removeUeiAt.
     * 
     * @param index
     * @return the element removed from the collection
     */
    public Uei removeUeiAt(final int index) {
        Object obj = this.ueiList.remove(index);
        return (Uei) obj;
    }

    /**
     * Sets the value of field 'content'. The field 'content' has the following
     * description: internal content storage
     * 
     * @param content the value of field 'content'.
     */
    public void setContent(final String content) {
        this.content = content;
    }

    /**
     * Sets the value of field 'language'.
     * 
     * @param language the value of field 'language'.
     */
    public void setLanguage(final String language) {
        this.language = language;
    }

    /**
     * 
     * 
     * @param index
     * @param vUei
     * @throws IndexOutOfBoundsException if the index given is outside
     * the bounds of the collection
     */
    public void setUei(final int index, final Uei vUei) throws IndexOutOfBoundsException {
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
    public void setUei(final Uei[] vUeiArray) {
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
    public void setUei(final List<Uei> vUeiList) {
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
    public void setUeiCollection(final List<Uei> ueiList) {
        this.ueiList = ueiList;
    }

}

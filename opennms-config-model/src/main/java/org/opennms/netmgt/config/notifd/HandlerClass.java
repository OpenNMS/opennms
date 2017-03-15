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
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Class HandlerClass.
 * 
 * @version $Revision$ $Date$
 */
@XmlRootElement(name = "handler-class")
@XmlAccessorType(XmlAccessType.FIELD)
public class HandlerClass implements java.io.Serializable {
    private static final long serialVersionUID = 1L;

    @XmlElement(name = "name", required = true)
    private String name;

    @XmlElement(name = "init-params")
    private List<InitParams> initParamsList = new ArrayList<>();

    public HandlerClass() { }

    /**
     * 
     * 
     * @param vInitParams
     * @throws IndexOutOfBoundsException if the index given is outside
     * the bounds of the collection
     */
    public void addInitParams(final InitParams vInitParams) throws IndexOutOfBoundsException {
        this.initParamsList.add(vInitParams);
    }

    /**
     * 
     * 
     * @param index
     * @param vInitParams
     * @throws IndexOutOfBoundsException if the index given is outside
     * the bounds of the collection
     */
    public void addInitParams(final int index, final InitParams vInitParams) throws IndexOutOfBoundsException {
        this.initParamsList.add(index, vInitParams);
    }

    /**
     * Method enumerateInitParams.
     * 
     * @return an Enumeration over all possible elements of this collection
     */
    public Enumeration<InitParams> enumerateInitParams() {
        return Collections.enumeration(this.initParamsList);
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
        
        if (obj instanceof HandlerClass) {
            HandlerClass temp = (HandlerClass)obj;
            boolean equals = Objects.equals(temp.name, name)
                && Objects.equals(temp.initParamsList, initParamsList);
            return equals;
        }
        return false;
    }

    /**
     * Method getInitParams.
     * 
     * @param index
     * @throws IndexOutOfBoundsException if the index given is outside
     * the bounds of the collection
     * @return the value of the InitParams at the
     * given index
     */
    public InitParams getInitParams(final int index) throws IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this.initParamsList.size()) {
            throw new IndexOutOfBoundsException("getInitParams: Index value '" + index + "' not in range [0.." + (this.initParamsList.size() - 1) + "]");
        }
        
        return (InitParams) initParamsList.get(index);
    }

    /**
     * Method getInitParams.Returns the contents of the collection in an Array. 
     * <p>Note:  Just in case the collection contents are changing in another
     * thread, we pass a 0-length Array of the correct type into the API call. 
     * This way we <i>know</i> that the Array returned is of exactly the correct
     * length.
     * 
     * @return this collection as an Array
     */
    public InitParams[] getInitParams() {
        InitParams[] array = new InitParams[0];
        return (InitParams[]) this.initParamsList.toArray(array);
    }

    /**
     * Method getInitParamsCollection.Returns a reference to 'initParamsList'. No
     * type checking is performed on any modifications to the Vector.
     * 
     * @return a reference to the Vector backing this class
     */
    public List<InitParams> getInitParamsCollection() {
        return this.initParamsList;
    }

    /**
     * Method getInitParamsCount.
     * 
     * @return the size of this collection
     */
    public int getInitParamsCount() {
        return this.initParamsList.size();
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
     * Method hashCode.
     * 
     * @return a hash code value for the object.
     */
    @Override
    public int hashCode() {
        int hash = Objects.hash(
            name, 
            initParamsList);
        return hash;
    }

    /**
     * Method iterateInitParams.
     * 
     * @return an Iterator over all possible elements in this collection
     */
    public Iterator<InitParams> iterateInitParams() {
        return this.initParamsList.iterator();
    }

    /**
     */
    public void removeAllInitParams() {
        this.initParamsList.clear();
    }

    /**
     * Method removeInitParams.
     * 
     * @param vInitParams
     * @return true if the object was removed from the collection.
     */
    public boolean removeInitParams(final InitParams vInitParams) {
        boolean removed = initParamsList.remove(vInitParams);
        return removed;
    }

    /**
     * Method removeInitParamsAt.
     * 
     * @param index
     * @return the element removed from the collection
     */
    public InitParams removeInitParamsAt(final int index) {
        Object obj = this.initParamsList.remove(index);
        return (InitParams) obj;
    }

    /**
     * 
     * 
     * @param index
     * @param vInitParams
     * @throws IndexOutOfBoundsException if the index given is outside
     * the bounds of the collection
     */
    public void setInitParams(final int index, final InitParams vInitParams) throws IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this.initParamsList.size()) {
            throw new IndexOutOfBoundsException("setInitParams: Index value '" + index + "' not in range [0.." + (this.initParamsList.size() - 1) + "]");
        }
        
        this.initParamsList.set(index, vInitParams);
    }

    /**
     * 
     * 
     * @param vInitParamsArray
     */
    public void setInitParams(final InitParams[] vInitParamsArray) {
        //-- copy array
        initParamsList.clear();
        
        for (int i = 0; i < vInitParamsArray.length; i++) {
                this.initParamsList.add(vInitParamsArray[i]);
        }
    }

    /**
     * Sets the value of 'initParamsList' by copying the given Vector. All
     * elements will be checked for type safety.
     * 
     * @param vInitParamsList the Vector to copy.
     */
    public void setInitParams(final List<InitParams> vInitParamsList) {
        // copy vector
        this.initParamsList.clear();
        
        this.initParamsList.addAll(vInitParamsList);
    }

    /**
     * Sets the value of 'initParamsList' by setting it to the given Vector. No
     * type checking is performed.
     * @deprecated
     * 
     * @param initParamsList the Vector to set.
     */
    public void setInitParamsCollection(final List<InitParams> initParamsList) {
        this.initParamsList = initParamsList == null? new ArrayList<>() : initParamsList;
    }

    /**
     * Sets the value of field 'name'.
     * 
     * @param name the value of field 'name'.
     */
    public void setName(final String name) {
        if (name == null) {
            throw new IllegalArgumentException("name is a required field!");
        }
        this.name = name;
    }

}

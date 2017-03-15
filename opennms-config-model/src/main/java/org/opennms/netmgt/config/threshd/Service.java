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

package org.opennms.netmgt.config.threshd;


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
 * Service for which thresholding is to be performed for
 *  addresses in this package
 * 
 * @version $Revision$ $Date$
 */
@XmlRootElement(name = "service")
@XmlAccessorType(XmlAccessType.FIELD)
public class Service implements java.io.Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * Service name
     */
    @XmlAttribute(name = "name", required = true)
    private String name;

    /**
     * Interval at which the service is to be threshold
     *  checked
     */
    @XmlAttribute(name = "interval", required = true)
    private Long interval;

    /**
     * Specifies if this is a user-defined service. Used
     *  specifically for UI purposes.
     */
    @XmlAttribute(name = "user-defined")
    private String userDefined;

    /**
     * Thresholding status for this service. Service is
     *  checked against thresholds only if set to 'on'.
     */
    @XmlAttribute(name = "status")
    private String status;

    /**
     * Parameters to be used for threshold checking this
     *  service. Parameters are specfic to the service
     *  thresholder.
     */
    @XmlElement(name = "parameter")
    private List<Parameter> parameterList;

    public Service() {
        this.parameterList = new ArrayList<Parameter>();
    }

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
     */
    public void deleteInterval() {
        this.interval= null;
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
        
        if (obj instanceof Service) {
            Service temp = (Service)obj;
            boolean equals = Objects.equals(temp.name, name)
                && Objects.equals(temp.interval, interval)
                && Objects.equals(temp.userDefined, userDefined)
                && Objects.equals(temp.status, status)
                && Objects.equals(temp.parameterList, parameterList);
            return equals;
        }
        return false;
    }

    /**
     * Returns the value of field 'interval'. The field 'interval' has the
     * following description: Interval at which the service is to be threshold
     *  checked
     * 
     * @return the value of field 'Interval'.
     */
    public Long getInterval() {
        return this.interval;
    }

    /**
     * Returns the value of field 'name'. The field 'name' has the following
     * description: Service name
     * 
     * @return the value of field 'Name'.
     */
    public String getName() {
        return this.name;
    }

    /**
     * Method getParameter.
     * 
     * @param index
     * @throws IndexOutOfBoundsException if the index given is outside
     * the bounds of the collection
     * @return the value of the Parameter at the
     * given index
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
     * Returns the value of field 'status'. The field 'status' has the following
     * description: Thresholding status for this service. Service is
     *  checked against thresholds only if set to 'on'.
     * 
     * @return the value of field 'Status'.
     */
    public String getStatus() {
        return this.status;
    }

    /**
     * Returns the value of field 'userDefined'. The field 'userDefined' has the
     * following description: Specifies if this is a user-defined service. Used
     *  specifically for UI purposes.
     * 
     * @return the value of field 'UserDefined'.
     */
    public String getUserDefined() {
        return this.userDefined;
    }

    /**
     * Method hasInterval.
     * 
     * @return true if at least one Interval has been added
     */
    public boolean hasInterval() {
        return this.interval != null;
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
            interval, 
            userDefined, 
            status, 
            parameterList);
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
     * Sets the value of field 'interval'. The field 'interval' has the following
     * description: Interval at which the service is to be threshold
     *  checked
     * 
     * @param interval the value of field 'interval'.
     */
    public void setInterval(final Long interval) {
        this.interval = interval;
    }

    /**
     * Sets the value of field 'name'. The field 'name' has the following
     * description: Service name
     * 
     * @param name the value of field 'name'.
     */
    public void setName(final String name) {
        this.name = name;
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
        this.parameterList = parameterList;
    }

    /**
     * Sets the value of field 'status'. The field 'status' has the following
     * description: Thresholding status for this service. Service is
     *  checked against thresholds only if set to 'on'.
     * 
     * @param status the value of field 'status'.
     */
    public void setStatus(final String status) {
        this.status = status;
    }

    /**
     * Sets the value of field 'userDefined'. The field 'userDefined' has the
     * following description: Specifies if this is a user-defined service. Used
     *  specifically for UI purposes.
     * 
     * @param userDefined the value of field 'userDefined'.
     */
    public void setUserDefined(final String userDefined) {
        this.userDefined = userDefined;
    }

}

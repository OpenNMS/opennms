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

package org.opennms.netmgt.config.statsd;


import java.util.ArrayList;
import java.util.Objects;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Report to be generated for nodes matching this
 *  package
 * 
 * @version $Revision$ $Date$
 */
@XmlRootElement(name = "packageReport")
@XmlAccessorType(XmlAccessType.FIELD)

@SuppressWarnings("all") public class PackageReport implements java.io.Serializable {


    /**
     * The report name. This is used internally to
     *  reference a configured report class.
     */
    @XmlAttribute(name = "name", required = true)
    private String name;

    /**
     * The report description. This is shown in the web
     *  UI.
     */
    @XmlAttribute(name = "description", required = true)
    private String description;

    /**
     * the schedule at which the report is to be
     *  generated
     */
    @XmlAttribute(name = "schedule", required = true)
    private String schedule;

    /**
     * the amount of time after which this report has been
     *  created that it can be purged.
     */
    @XmlAttribute(name = "retainInterval", required = true)
    private String retainInterval;

    /**
     * status of the report; report is generated only if
     *  on
     */
    @XmlAttribute(name = "status", required = true)
    private PackageReportStatus status;

    /**
     * Package-specific parameters (if any) to be used
     *  for this report
     */
    @XmlElement(name = "parameter")
    private java.util.List<Parameter> parameterList = new ArrayList<>();

    public PackageReport() { }

    public void addParameter(String key, String value) {
        final Parameter p = new Parameter(key, value);
        parameterList.add(p);
    }

    /**
     * 
     * 
     * @param vParameter
     * @throws IndexOutOfBoundsException if the index given is outside
     * the bounds of the collection
     */
    public void addParameter(final org.opennms.netmgt.config.statsd.Parameter vParameter) throws IndexOutOfBoundsException {
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
    public void addParameter(final int index, final org.opennms.netmgt.config.statsd.Parameter vParameter) throws IndexOutOfBoundsException {
        this.parameterList.add(index, vParameter);
    }

    /**
     * Method enumerateParameter.
     * 
     * @return an Enumeration over all possible elements of this collection
     */
    public java.util.Enumeration<org.opennms.netmgt.config.statsd.Parameter> enumerateParameter() {
        return java.util.Collections.enumeration(this.parameterList);
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
        
        if (obj instanceof PackageReport) {
            PackageReport temp = (PackageReport)obj;
            boolean equals = Objects.equals(temp.name, name)
                && Objects.equals(temp.description, description)
                && Objects.equals(temp.schedule, schedule)
                && Objects.equals(temp.retainInterval, retainInterval)
                && Objects.equals(temp.status, status)
                && Objects.equals(temp.parameterList, parameterList);
            return equals;
        }
        return false;
    }

    /**
     * Returns the value of field 'description'. The field 'description' has the
     * following description: The report description. This is shown in the web
     *  UI.
     * 
     * @return the value of field 'Description'.
     */
    public String getDescription() {
        return this.description;
    }

    /**
     * Returns the value of field 'name'. The field 'name' has the following
     * description: The report name. This is used internally to
     *  reference a configured report class.
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
     * @return the value of the org.opennms.netmgt.config.statsd.Parameter at the
     * given index
     */
    public org.opennms.netmgt.config.statsd.Parameter getParameter(final int index) throws IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this.parameterList.size()) {
            throw new IndexOutOfBoundsException("getParameter: Index value '" + index + "' not in range [0.." + (this.parameterList.size() - 1) + "]");
        }
        
        return (org.opennms.netmgt.config.statsd.Parameter) parameterList.get(index);
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
    public org.opennms.netmgt.config.statsd.Parameter[] getParameter() {
        org.opennms.netmgt.config.statsd.Parameter[] array = new org.opennms.netmgt.config.statsd.Parameter[0];
        return (org.opennms.netmgt.config.statsd.Parameter[]) this.parameterList.toArray(array);
    }

    /**
     * Method getParameterCollection.Returns a reference to 'parameterList'. No
     * type checking is performed on any modifications to the Vector.
     * 
     * @return a reference to the Vector backing this class
     */
    public java.util.List<org.opennms.netmgt.config.statsd.Parameter> getParameterCollection() {
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
     * Returns the value of field 'retainInterval'. The field 'retainInterval' has
     * the following description: the amount of time after which this report has
     * been
     *  created that it can be purged.
     * 
     * @return the value of field 'RetainInterval'.
     */
    public String getRetainInterval() {
        return this.retainInterval;
    }

    /**
     * Returns the value of field 'schedule'. The field 'schedule' has the
     * following description: the schedule at which the report is to be
     *  generated
     * 
     * @return the value of field 'Schedule'.
     */
    public String getSchedule() {
        return this.schedule;
    }

    /**
     * Returns the value of field 'status'. The field 'status' has the following
     * description: status of the report; report is generated only if
     *  on
     * 
     * @return the value of field 'Status'.
     */
    public PackageReportStatus getStatus() {
        return this.status;
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
            description, 
            schedule, 
            retainInterval, 
            status, 
            parameterList);
        return hash;
    }

    /**
     * Method iterateParameter.
     * 
     * @return an Iterator over all possible elements in this collection
     */
    public java.util.Iterator<org.opennms.netmgt.config.statsd.Parameter> iterateParameter() {
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
    public boolean removeParameter(final org.opennms.netmgt.config.statsd.Parameter vParameter) {
        boolean removed = parameterList.remove(vParameter);
        return removed;
    }

    /**
     * Method removeParameterAt.
     * 
     * @param index
     * @return the element removed from the collection
     */
    public org.opennms.netmgt.config.statsd.Parameter removeParameterAt(final int index) {
        Object obj = this.parameterList.remove(index);
        return (org.opennms.netmgt.config.statsd.Parameter) obj;
    }

    /**
     * Sets the value of field 'description'. The field 'description' has the
     * following description: The report description. This is shown in the web
     *  UI.
     * 
     * @param description the value of field 'description'.
     */
    public void setDescription(final String description) {
        this.description = description;
    }

    /**
     * Sets the value of field 'name'. The field 'name' has the following
     * description: The report name. This is used internally to
     *  reference a configured report class.
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
    public void setParameter(final int index, final org.opennms.netmgt.config.statsd.Parameter vParameter) throws IndexOutOfBoundsException {
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
    public void setParameter(final org.opennms.netmgt.config.statsd.Parameter[] vParameterArray) {
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
    public void setParameter(final java.util.List<org.opennms.netmgt.config.statsd.Parameter> vParameterList) {
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
    public void setParameterCollection(final java.util.List<org.opennms.netmgt.config.statsd.Parameter> parameterList) {
        this.parameterList = parameterList;
    }

    /**
     * Sets the value of field 'retainInterval'. The field 'retainInterval' has
     * the following description: the amount of time after which this report has
     * been
     *  created that it can be purged.
     * 
     * @param retainInterval the value of field 'retainInterval'.
     */
    public void setRetainInterval(final String retainInterval) {
        this.retainInterval = retainInterval;
    }

    /**
     * Sets the value of field 'schedule'. The field 'schedule' has the following
     * description: the schedule at which the report is to be
     *  generated
     * 
     * @param schedule the value of field 'schedule'.
     */
    public void setSchedule(final String schedule) {
        this.schedule = schedule;
    }

    /**
     * Sets the value of field 'status'. The field 'status' has the following
     * description: status of the report; report is generated only if
     *  on
     * 
     * @param status the value of field 'status'.
     */
    public void setStatus(final PackageReportStatus status) {
        this.status = status;
    }

}

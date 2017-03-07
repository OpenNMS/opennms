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
 * Class Basethresholddef.
 * 
 * @version $Revision$ $Date$
 */
@XmlRootElement(name = "basethresholddef")
@XmlAccessorType(XmlAccessType.FIELD)
public abstract class Basethresholddef implements java.io.Serializable {
    private static final long serialVersionUID = 1L;

    private static final String DEFAULT_FILTER_OPERATOR = "or";

    /**
     * An optional flag to tell the threshold processor to evaluate the expression
     * even if there are unknown values.
     *  This can be useful when processing expressions with conditionals. Default:
     * false
     *  
     */
    @XmlAttribute(name = "relaxed")
    private Boolean relaxed;

    /**
     * An optional description for the threshold, to help identify what is their
     * purpose.
     *  
     */
    @XmlAttribute(name = "description")
    private String description;

    /**
     * Threshold type. "high" to trigger if the value exceeds the threshold,
     *  "low" to trigger if the value drops below the threshold,
     *  "relativeChange" to trigger if the value changes more than the proportion
     * represented by the threshold, or
     *  "absoluteChange" to trigger if the value changes by more than the
     * threshold value
     *  
     */
    @XmlAttribute(name = "type", required = true)
    private String type;

    /**
     * RRD datasource type. "node" indicates a node level datasource.
     *  "if" indicates an interface level datasource.
     *  
     */
    @XmlAttribute(name = "ds-type", required = true)
    private String dsType;

    /**
     * Threshold value. If the datasource value rises above this
     *  value, in the case of a "high" threshold, or drops below this
     *  value, in the case of a "low" threshold the threshold is
     *  considered to have been exceeded and the exceeded count will
     *  be incremented. Any time that the datasource value drops below
     *  this value, in the case of a "high" threshold, or rises above
     *  this value, in the case of a "low" threshold the exceeded
     *  count is reset back to zero. Whenever the exceeded count
     *  reaches the trigger value then a threshold event is generated.
     *  
     */
    @XmlAttribute(name = "value", required = true)
    private Double value;

    /**
     * Rearm value. Identifies the value that the datasource must
     *  fall below, in the case of a "high" threshold or rise above,
     *  in the case of a "low" threshold, before the threshold will
     *  rearm, and once again be eligible to generate an event.
     *  
     */
    @XmlAttribute(name = "rearm", required = true)
    private Double rearm;

    /**
     * Trigger value. Identifies the number of consecutive polls that
     *  the datasource value must exceed the defined threshold value
     *  before a threshold event is generated.
     *  
     */
    @XmlAttribute(name = "trigger", required = true)
    private Integer trigger;

    /**
     * Value to retrieve from strings.properties to label this
     *  datasource.
     *  
     */
    @XmlAttribute(name = "ds-label")
    private String dsLabel;

    /**
     * The UEI to send when this threshold is triggered. If not
     *  specified, defaults to standard threshold UEIs
     *  
     */
    @XmlAttribute(name = "triggeredUEI")
    private String triggeredUEI;

    /**
     * The UEI to send when this threshold is re-armed. If not
     *  specified, defaults to standard threshold UEIs
     *  
     */
    @XmlAttribute(name = "rearmedUEI")
    private String rearmedUEI;

    /**
     * The operator to be used when applying filters. The
     *  default is "or". If you want to match all filters,
     *  you should specify "and";
     *  
     */
    @XmlAttribute(name = "filterOperator")
    private String filterOperator;

    /**
     * The filter used to select the ds by a string
     */
    @XmlElement(name = "resource-filter")
    private List<ResourceFilter> resourceFilterList = new ArrayList<>();

    public Basethresholddef() { }

    /**
     * 
     * 
     * @param vResourceFilter
     * @throws IndexOutOfBoundsException if the index given is outside
     * the bounds of the collection
     */
    public void addResourceFilter(final ResourceFilter vResourceFilter) throws IndexOutOfBoundsException {
        this.resourceFilterList.add(vResourceFilter);
    }

    /**
     * 
     * 
     * @param index
     * @param vResourceFilter
     * @throws IndexOutOfBoundsException if the index given is outside
     * the bounds of the collection
     */
    public void addResourceFilter(final int index, final ResourceFilter vResourceFilter) throws IndexOutOfBoundsException {
        this.resourceFilterList.add(index, vResourceFilter);
    }

    /**
     */
    public void deleteRearm() {
        this.rearm= null;
    }

    /**
     */
    public void deleteRelaxed() {
        this.relaxed= null;
    }

    /**
     */
    public void deleteTrigger() {
        this.trigger= null;
    }

    /**
     */
    public void deleteValue() {
        this.value= null;
    }

    /**
     * Method enumerateResourceFilter.
     * 
     * @return an Enumeration over all possible elements of this collection
     */
    public Enumeration<ResourceFilter> enumerateResourceFilter() {
        return Collections.enumeration(this.resourceFilterList);
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
        
        if (obj instanceof Basethresholddef) {
            Basethresholddef temp = (Basethresholddef)obj;
            boolean equals = Objects.equals(temp.relaxed, relaxed)
                && Objects.equals(temp.description, description)
                && Objects.equals(temp.type, type)
                && Objects.equals(temp.dsType, dsType)
                && Objects.equals(temp.value, value)
                && Objects.equals(temp.rearm, rearm)
                && Objects.equals(temp.trigger, trigger)
                && Objects.equals(temp.dsLabel, dsLabel)
                && Objects.equals(temp.triggeredUEI, triggeredUEI)
                && Objects.equals(temp.rearmedUEI, rearmedUEI)
                && Objects.equals(temp.filterOperator, filterOperator)
                && Objects.equals(temp.resourceFilterList, resourceFilterList);
            return equals;
        }
        return false;
    }

    /**
     * Returns the value of field 'description'. The field 'description' has the
     * following description: An optional description for the threshold, to help
     * identify what is their purpose.
     *  
     * 
     * @return the value of field 'Description'.
     */
    public String getDescription() {
        return this.description;
    }

    /**
     * Returns the value of field 'dsLabel'. The field 'dsLabel' has the following
     * description: Value to retrieve from strings.properties to label this
     *  datasource.
     *  
     * 
     * @return the value of field 'DsLabel'.
     */
    public String getDsLabel() {
        return this.dsLabel;
    }

    /**
     * Returns the value of field 'dsType'. The field 'dsType' has the following
     * description: RRD datasource type. "node" indicates a node level datasource.
     *  "if" indicates an interface level datasource.
     *  
     * 
     * @return the value of field 'DsType'.
     */
    public String getDsType() {
        return this.dsType;
    }

    /**
     * Returns the value of field 'filterOperator'. The field 'filterOperator' has
     * the following description: The operator to be used when applying filters.
     * The
     *  default is "or". If you want to match all filters,
     *  you should specify "and";
     *  
     * 
     * @return the value of field 'FilterOperator'.
     */
    public String getFilterOperator() {
        return this.filterOperator != null ? this.filterOperator : DEFAULT_FILTER_OPERATOR;
    }

    /**
     * Returns the value of field 'rearm'. The field 'rearm' has the following
     * description: Rearm value. Identifies the value that the datasource must
     *  fall below, in the case of a "high" threshold or rise above,
     *  in the case of a "low" threshold, before the threshold will
     *  rearm, and once again be eligible to generate an event.
     *  
     * 
     * @return the value of field 'Rearm'.
     */
    public Double getRearm() {
        return this.rearm;
    }

    /**
     * Returns the value of field 'rearmedUEI'. The field 'rearmedUEI' has the
     * following description: The UEI to send when this threshold is re-armed. If
     * not
     *  specified, defaults to standard threshold UEIs
     *  
     * 
     * @return the value of field 'RearmedUEI'.
     */
    public String getRearmedUEI() {
        return this.rearmedUEI;
    }

    /**
     * Returns the value of field 'relaxed'. The field 'relaxed' has the following
     * description: An optional flag to tell the threshold processor to evaluate
     * the expression even if there are unknown values.
     *  This can be useful when processing expressions with conditionals. Default:
     * false
     *  
     * 
     * @return the value of field 'Relaxed'.
     */
    public Boolean getRelaxed() {
        return this.relaxed != null ? this.relaxed : Boolean.valueOf("false");
    }

    /**
     * Method getResourceFilter.
     * 
     * @param index
     * @throws IndexOutOfBoundsException if the index given is outside
     * the bounds of the collection
     * @return the value of the ResourceFilter
     * at the given index
     */
    public ResourceFilter getResourceFilter(final int index) throws IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this.resourceFilterList.size()) {
            throw new IndexOutOfBoundsException("getResourceFilter: Index value '" + index + "' not in range [0.." + (this.resourceFilterList.size() - 1) + "]");
        }
        
        return (ResourceFilter) resourceFilterList.get(index);
    }

    /**
     * Method getResourceFilter.Returns the contents of the collection in an
     * Array.  <p>Note:  Just in case the collection contents are changing in
     * another thread, we pass a 0-length Array of the correct type into the API
     * call.  This way we <i>know</i> that the Array returned is of exactly the
     * correct length.
     * 
     * @return this collection as an Array
     */
    public ResourceFilter[] getResourceFilter() {
        ResourceFilter[] array = new ResourceFilter[0];
        return (ResourceFilter[]) this.resourceFilterList.toArray(array);
    }

    /**
     * Method getResourceFilterCollection.Returns a reference to
     * 'resourceFilterList'. No type checking is performed on any modifications to
     * the Vector.
     * 
     * @return a reference to the Vector backing this class
     */
    public List<ResourceFilter> getResourceFilterCollection() {
        return this.resourceFilterList;
    }

    /**
     * Method getResourceFilterCount.
     * 
     * @return the size of this collection
     */
    public int getResourceFilterCount() {
        return this.resourceFilterList.size();
    }

    /**
     * Returns the value of field 'trigger'. The field 'trigger' has the following
     * description: Trigger value. Identifies the number of consecutive polls that
     *  the datasource value must exceed the defined threshold value
     *  before a threshold event is generated.
     *  
     * 
     * @return the value of field 'Trigger'.
     */
    public Integer getTrigger() {
        return this.trigger;
    }

    /**
     * Returns the value of field 'triggeredUEI'. The field 'triggeredUEI' has the
     * following description: The UEI to send when this threshold is triggered. If
     * not
     *  specified, defaults to standard threshold UEIs
     *  
     * 
     * @return the value of field 'TriggeredUEI'.
     */
    public String getTriggeredUEI() {
        return this.triggeredUEI;
    }

    /**
     * Returns the value of field 'type'. The field 'type' has the following
     * description: Threshold type. "high" to trigger if the value exceeds the
     * threshold,
     *  "low" to trigger if the value drops below the threshold,
     *  "relativeChange" to trigger if the value changes more than the proportion
     * represented by the threshold, or
     *  "absoluteChange" to trigger if the value changes by more than the
     * threshold value
     *  
     * 
     * @return the value of field 'Type'.
     */
    public String getType() {
        return this.type;
    }

    /**
     * Returns the value of field 'value'. The field 'value' has the following
     * description: Threshold value. If the datasource value rises above this
     *  value, in the case of a "high" threshold, or drops below this
     *  value, in the case of a "low" threshold the threshold is
     *  considered to have been exceeded and the exceeded count will
     *  be incremented. Any time that the datasource value drops below
     *  this value, in the case of a "high" threshold, or rises above
     *  this value, in the case of a "low" threshold the exceeded
     *  count is reset back to zero. Whenever the exceeded count
     *  reaches the trigger value then a threshold event is generated.
     *  
     * 
     * @return the value of field 'Value'.
     */
    public Double getValue() {
        return this.value;
    }

    /**
     * Method hasRearm.
     * 
     * @return true if at least one Rearm has been added
     */
    public boolean hasRearm() {
        return this.rearm != null;
    }

    /**
     * Method hasRelaxed.
     * 
     * @return true if at least one Relaxed has been added
     */
    public boolean hasRelaxed() {
        return this.relaxed != null;
    }

    /**
     * Method hasTrigger.
     * 
     * @return true if at least one Trigger has been added
     */
    public boolean hasTrigger() {
        return this.trigger != null;
    }

    /**
     * Method hasValue.
     * 
     * @return true if at least one Value has been added
     */
    public boolean hasValue() {
        return this.value != null;
    }

    /**
     * Method hashCode.
     * 
     * @return a hash code value for the object.
     */
    @Override
    public int hashCode() {
        int hash = Objects.hash(
            relaxed, 
            description, 
            type, 
            dsType, 
            value, 
            rearm, 
            trigger, 
            dsLabel, 
            triggeredUEI, 
            rearmedUEI, 
            filterOperator, 
            resourceFilterList);
        return hash;
    }

    /**
     * Returns the value of field 'relaxed'. The field 'relaxed' has the following
     * description: An optional flag to tell the threshold processor to evaluate
     * the expression even if there are unknown values.
     *  This can be useful when processing expressions with conditionals. Default:
     * false
     *  
     * 
     * @return the value of field 'Relaxed'.
     */
    public Boolean isRelaxed() {
        return this.relaxed != null ? this.relaxed : Boolean.valueOf("false");
    }

    /**
     * Method iterateResourceFilter.
     * 
     * @return an Iterator over all possible elements in this collection
     */
    public Iterator<ResourceFilter> iterateResourceFilter() {
        return this.resourceFilterList.iterator();
    }

    /**
     */
    public void removeAllResourceFilter() {
        this.resourceFilterList.clear();
    }

    /**
     * Method removeResourceFilter.
     * 
     * @param vResourceFilter
     * @return true if the object was removed from the collection.
     */
    public boolean removeResourceFilter(final ResourceFilter vResourceFilter) {
        boolean removed = resourceFilterList.remove(vResourceFilter);
        return removed;
    }

    /**
     * Method removeResourceFilterAt.
     * 
     * @param index
     * @return the element removed from the collection
     */
    public ResourceFilter removeResourceFilterAt(final int index) {
        Object obj = this.resourceFilterList.remove(index);
        return (ResourceFilter) obj;
    }

    /**
     * Sets the value of field 'description'. The field 'description' has the
     * following description: An optional description for the threshold, to help
     * identify what is their purpose.
     *  
     * 
     * @param description the value of field 'description'.
     */
    public void setDescription(final String description) {
        this.description = description;
    }

    /**
     * Sets the value of field 'dsLabel'. The field 'dsLabel' has the following
     * description: Value to retrieve from strings.properties to label this
     *  datasource.
     *  
     * 
     * @param dsLabel the value of field 'dsLabel'.
     */
    public void setDsLabel(final String dsLabel) {
        this.dsLabel = dsLabel;
    }

    /**
     * Sets the value of field 'dsType'. The field 'dsType' has the following
     * description: RRD datasource type. "node" indicates a node level datasource.
     *  "if" indicates an interface level datasource.
     *  
     * 
     * @param dsType the value of field 'dsType'.
     */
    public void setDsType(final String dsType) {
        this.dsType = dsType;
    }

    /**
     * Sets the value of field 'filterOperator'. The field 'filterOperator' has
     * the following description: The operator to be used when applying filters.
     * The
     *  default is "or". If you want to match all filters,
     *  you should specify "and";
     *  
     * 
     * @param filterOperator the value of field 'filterOperator'.
     */
    public void setFilterOperator(final String filterOperator) {
        this.filterOperator = filterOperator;
    }

    /**
     * Sets the value of field 'rearm'. The field 'rearm' has the following
     * description: Rearm value. Identifies the value that the datasource must
     *  fall below, in the case of a "high" threshold or rise above,
     *  in the case of a "low" threshold, before the threshold will
     *  rearm, and once again be eligible to generate an event.
     *  
     * 
     * @param rearm the value of field 'rearm'.
     */
    public void setRearm(final Double rearm) {
        this.rearm = rearm;
    }

    /**
     * Sets the value of field 'rearmedUEI'. The field 'rearmedUEI' has the
     * following description: The UEI to send when this threshold is re-armed. If
     * not
     *  specified, defaults to standard threshold UEIs
     *  
     * 
     * @param rearmedUEI the value of field 'rearmedUEI'.
     */
    public void setRearmedUEI(final String rearmedUEI) {
        this.rearmedUEI = rearmedUEI;
    }

    /**
     * Sets the value of field 'relaxed'. The field 'relaxed' has the following
     * description: An optional flag to tell the threshold processor to evaluate
     * the expression even if there are unknown values.
     *  This can be useful when processing expressions with conditionals. Default:
     * false
     *  
     * 
     * @param relaxed the value of field 'relaxed'.
     */
    public void setRelaxed(final Boolean relaxed) {
        this.relaxed = relaxed;
    }

    /**
     * 
     * 
     * @param index
     * @param vResourceFilter
     * @throws IndexOutOfBoundsException if the index given is outside
     * the bounds of the collection
     */
    public void setResourceFilter(final int index, final ResourceFilter vResourceFilter) throws IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this.resourceFilterList.size()) {
            throw new IndexOutOfBoundsException("setResourceFilter: Index value '" + index + "' not in range [0.." + (this.resourceFilterList.size() - 1) + "]");
        }
        
        this.resourceFilterList.set(index, vResourceFilter);
    }

    /**
     * 
     * 
     * @param vResourceFilterArray
     */
    public void setResourceFilter(final ResourceFilter[] vResourceFilterArray) {
        //-- copy array
        resourceFilterList.clear();
        
        for (int i = 0; i < vResourceFilterArray.length; i++) {
                this.resourceFilterList.add(vResourceFilterArray[i]);
        }
    }

    /**
     * Sets the value of 'resourceFilterList' by copying the given Vector. All
     * elements will be checked for type safety.
     * 
     * @param vResourceFilterList the Vector to copy.
     */
    public void setResourceFilter(final List<ResourceFilter> vResourceFilterList) {
        // copy vector
        this.resourceFilterList.clear();
        
        this.resourceFilterList.addAll(vResourceFilterList);
    }

    /**
     * Sets the value of 'resourceFilterList' by setting it to the given Vector.
     * No type checking is performed.
     * @deprecated
     * 
     * @param resourceFilterList the Vector to set.
     */
    public void setResourceFilterCollection(final List<ResourceFilter> resourceFilterList) {
        this.resourceFilterList = resourceFilterList;
    }

    /**
     * Sets the value of field 'trigger'. The field 'trigger' has the following
     * description: Trigger value. Identifies the number of consecutive polls that
     *  the datasource value must exceed the defined threshold value
     *  before a threshold event is generated.
     *  
     * 
     * @param trigger the value of field 'trigger'.
     */
    public void setTrigger(final Integer trigger) {
        this.trigger = trigger;
    }

    /**
     * Sets the value of field 'triggeredUEI'. The field 'triggeredUEI' has the
     * following description: The UEI to send when this threshold is triggered. If
     * not
     *  specified, defaults to standard threshold UEIs
     *  
     * 
     * @param triggeredUEI the value of field 'triggeredUEI'.
     */
    public void setTriggeredUEI(final String triggeredUEI) {
        this.triggeredUEI = triggeredUEI;
    }

    /**
     * Sets the value of field 'type'. The field 'type' has the following
     * description: Threshold type. "high" to trigger if the value exceeds the
     * threshold,
     *  "low" to trigger if the value drops below the threshold,
     *  "relativeChange" to trigger if the value changes more than the proportion
     * represented by the threshold, or
     *  "absoluteChange" to trigger if the value changes by more than the
     * threshold value
     *  
     * 
     * @param type the value of field 'type'.
     */
    public void setType(final String type) {
        this.type = type;
    }

    /**
     * Sets the value of field 'value'. The field 'value' has the following
     * description: Threshold value. If the datasource value rises above this
     *  value, in the case of a "high" threshold, or drops below this
     *  value, in the case of a "low" threshold the threshold is
     *  considered to have been exceeded and the exceeded count will
     *  be incremented. Any time that the datasource value drops below
     *  this value, in the case of a "high" threshold, or rises above
     *  this value, in the case of a "low" threshold the exceeded
     *  count is reset back to zero. Whenever the exceeded count
     *  reaches the trigger value then a threshold event is generated.
     *  
     * 
     * @param value the value of field 'value'.
     */
    public void setValue(final Double value) {
        this.value = value;
    }

}

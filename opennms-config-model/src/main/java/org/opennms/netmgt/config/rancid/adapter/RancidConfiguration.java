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

package org.opennms.netmgt.config.rancid.adapter;


import java.util.Objects;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Top-level element for the rancid-configuration.xml configuration file.
 * 
 * @version $Revision$ $Date$
 */
@XmlRootElement(name = "rancid-configuration")
@XmlAccessorType(XmlAccessType.FIELD)
public class RancidConfiguration implements java.io.Serializable {
    private static final long serialVersionUID = 1L;

    private static final String DEFAULT_DEFAULT_TYPE = "cisco";

    /**
     * The time in sec to wait before trying
     *  to set the download flag to up in router.db.
     *  If schedule is there then it is verified if you are able
     *  to write to router.db in rancid.
     *  Otherwise you wait until schedule let you write on rancid.
     *  
     */
    @XmlAttribute(name = "delay", required = true)
    private Long delay;

    /**
     * The maximum number of retry before
     *  sending a failure.
     */
    @XmlAttribute(name = "retries", required = true)
    private Integer retries;

    /**
     * If you want to use opennms categories
     *  to match rancid device type.
     */
    @XmlAttribute(name = "useCategories")
    private Boolean useCategories;

    /**
     * The Default Rancid type, it is used when no device type
     *  for provisioned node is found.
     *  
     */
    @XmlAttribute(name = "default-type")
    private String defaultType;

    /**
     * Configuration of Policy
     *  functionality
     */
    @XmlElement(name = "policies")
    private Policies policies;

    /**
     * A map from sysoids masks and rancid device type.
     */
    @XmlElement(name = "mapping")
    private java.util.List<Mapping> mappingList;

    public RancidConfiguration() {
        this.mappingList = new java.util.ArrayList<Mapping>();
    }

    /**
     * 
     * 
     * @param vMapping
     * @throws IndexOutOfBoundsException if the index given is outside
     * the bounds of the collection
     */
    public void addMapping(final Mapping vMapping) throws IndexOutOfBoundsException {
        this.mappingList.add(vMapping);
    }

    /**
     * 
     * 
     * @param index
     * @param vMapping
     * @throws IndexOutOfBoundsException if the index given is outside
     * the bounds of the collection
     */
    public void addMapping(final int index, final Mapping vMapping) throws IndexOutOfBoundsException {
        this.mappingList.add(index, vMapping);
    }

    /**
     */
    public void deleteDelay() {
        this.delay= null;
    }

    /**
     */
    public void deleteRetries() {
        this.retries= null;
    }

    /**
     */
    public void deleteUseCategories() {
        this.useCategories= null;
    }

    /**
     * Method enumerateMapping.
     * 
     * @return an Enumeration over all possible elements of this collection
     */
    public java.util.Enumeration<Mapping> enumerateMapping() {
        return java.util.Collections.enumeration(this.mappingList);
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
        
        if (obj instanceof RancidConfiguration) {
            RancidConfiguration temp = (RancidConfiguration)obj;
            boolean equals = Objects.equals(temp.delay, delay)
                && Objects.equals(temp.retries, retries)
                && Objects.equals(temp.useCategories, useCategories)
                && Objects.equals(temp.defaultType, defaultType)
                && Objects.equals(temp.policies, policies)
                && Objects.equals(temp.mappingList, mappingList);
            return equals;
        }
        return false;
    }

    /**
     * Returns the value of field 'defaultType'. The field 'defaultType' has the
     * following description: The Default Rancid type, it is used when no device
     * type
     *  for provisioned node is found.
     *  
     * 
     * @return the value of field 'DefaultType'.
     */
    public String getDefaultType() {
        return this.defaultType != null ? this.defaultType : DEFAULT_DEFAULT_TYPE;
    }

    /**
     * Returns the value of field 'delay'. The field 'delay' has the following
     * description: The time in sec to wait before trying
     *  to set the download flag to up in router.db.
     *  If schedule is there then it is verified if you are able
     *  to write to router.db in rancid.
     *  Otherwise you wait until schedule let you write on rancid.
     *  
     * 
     * @return the value of field 'Delay'.
     */
    public Long getDelay() {
        return this.delay;
    }

    /**
     * Method getMapping.
     * 
     * @param index
     * @throws IndexOutOfBoundsException if the index given is outside
     * the bounds of the collection
     * @return the value of the Mapping
     * at the given index
     */
    public Mapping getMapping(final int index) throws IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this.mappingList.size()) {
            throw new IndexOutOfBoundsException("getMapping: Index value '" + index + "' not in range [0.." + (this.mappingList.size() - 1) + "]");
        }
        
        return (Mapping) mappingList.get(index);
    }

    /**
     * Method getMapping.Returns the contents of the collection in an Array. 
     * <p>Note:  Just in case the collection contents are changing in another
     * thread, we pass a 0-length Array of the correct type into the API call. 
     * This way we <i>know</i> that the Array returned is of exactly the correct
     * length.
     * 
     * @return this collection as an Array
     */
    public Mapping[] getMapping() {
        Mapping[] array = new Mapping[0];
        return (Mapping[]) this.mappingList.toArray(array);
    }

    /**
     * Method getMappingCollection.Returns a reference to 'mappingList'. No type
     * checking is performed on any modifications to the Vector.
     * 
     * @return a reference to the Vector backing this class
     */
    public java.util.List<Mapping> getMappingCollection() {
        return this.mappingList;
    }

    /**
     * Method getMappingCount.
     * 
     * @return the size of this collection
     */
    public int getMappingCount() {
        return this.mappingList.size();
    }

    /**
     * Returns the value of field 'policies'. The field 'policies' has the
     * following description: Configuration of Policy
     *  functionality
     * 
     * @return the value of field 'Policies'.
     */
    public Policies getPolicies() {
        return this.policies;
    }

    /**
     * Returns the value of field 'retries'. The field 'retries' has the following
     * description: The maximum number of retry before
     *  sending a failure.
     * 
     * @return the value of field 'Retries'.
     */
    public Integer getRetries() {
        return this.retries;
    }

    /**
     * Returns the value of field 'useCategories'. The field 'useCategories' has
     * the following description: If you want to use opennms categories
     *  to match rancid device type.
     * 
     * @return the value of field 'UseCategories'.
     */
    public Boolean getUseCategories() {
        return this.useCategories != null ? this.useCategories : Boolean.valueOf("false");
    }

    /**
     * Method hasDelay.
     * 
     * @return true if at least one Delay has been added
     */
    public boolean hasDelay() {
        return this.delay != null;
    }

    /**
     * Method hasRetries.
     * 
     * @return true if at least one Retries has been added
     */
    public boolean hasRetries() {
        return this.retries != null;
    }

    /**
     * Method hasUseCategories.
     * 
     * @return true if at least one UseCategories has been added
     */
    public boolean hasUseCategories() {
        return this.useCategories != null;
    }

    /**
     * Method hashCode.
     * 
     * @return a hash code value for the object.
     */
    @Override
    public int hashCode() {
        int hash = Objects.hash(
            delay, 
            retries, 
            useCategories, 
            defaultType, 
            policies, 
            mappingList);
        return hash;
    }

    /**
     * Returns the value of field 'useCategories'. The field 'useCategories' has
     * the following description: If you want to use opennms categories
     *  to match rancid device type.
     * 
     * @return the value of field 'UseCategories'.
     */
    public Boolean isUseCategories() {
        return this.useCategories != null ? this.useCategories : Boolean.valueOf("false");
    }

    /**
     * Method iterateMapping.
     * 
     * @return an Iterator over all possible elements in this collection
     */
    public java.util.Iterator<Mapping> iterateMapping() {
        return this.mappingList.iterator();
    }

    /**
     */
    public void removeAllMapping() {
        this.mappingList.clear();
    }

    /**
     * Method removeMapping.
     * 
     * @param vMapping
     * @return true if the object was removed from the collection.
     */
    public boolean removeMapping(final Mapping vMapping) {
        boolean removed = mappingList.remove(vMapping);
        return removed;
    }

    /**
     * Method removeMappingAt.
     * 
     * @param index
     * @return the element removed from the collection
     */
    public Mapping removeMappingAt(final int index) {
        Object obj = this.mappingList.remove(index);
        return (Mapping) obj;
    }

    /**
     * Sets the value of field 'defaultType'. The field 'defaultType' has the
     * following description: The Default Rancid type, it is used when no device
     * type
     *  for provisioned node is found.
     *  
     * 
     * @param defaultType the value of field 'defaultType'.
     */
    public void setDefaultType(final String defaultType) {
        this.defaultType = defaultType;
    }

    /**
     * Sets the value of field 'delay'. The field 'delay' has the following
     * description: The time in sec to wait before trying
     *  to set the download flag to up in router.db.
     *  If schedule is there then it is verified if you are able
     *  to write to router.db in rancid.
     *  Otherwise you wait until schedule let you write on rancid.
     *  
     * 
     * @param delay the value of field 'delay'.
     */
    public void setDelay(final Long delay) {
        this.delay = delay;
    }

    /**
     * 
     * 
     * @param index
     * @param vMapping
     * @throws IndexOutOfBoundsException if the index given is outside
     * the bounds of the collection
     */
    public void setMapping(final int index, final Mapping vMapping) throws IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this.mappingList.size()) {
            throw new IndexOutOfBoundsException("setMapping: Index value '" + index + "' not in range [0.." + (this.mappingList.size() - 1) + "]");
        }
        
        this.mappingList.set(index, vMapping);
    }

    /**
     * 
     * 
     * @param vMappingArray
     */
    public void setMapping(final Mapping[] vMappingArray) {
        //-- copy array
        mappingList.clear();
        
        for (int i = 0; i < vMappingArray.length; i++) {
                this.mappingList.add(vMappingArray[i]);
        }
    }

    /**
     * Sets the value of 'mappingList' by copying the given Vector. All elements
     * will be checked for type safety.
     * 
     * @param vMappingList the Vector to copy.
     */
    public void setMapping(final java.util.List<Mapping> vMappingList) {
        // copy vector
        this.mappingList.clear();
        
        this.mappingList.addAll(vMappingList);
    }

    /**
     * Sets the value of 'mappingList' by setting it to the given Vector. No type
     * checking is performed.
     * @deprecated
     * 
     * @param mappingList the Vector to set.
     */
    public void setMappingCollection(final java.util.List<Mapping> mappingList) {
        this.mappingList = mappingList;
    }

    /**
     * Sets the value of field 'policies'. The field 'policies' has the following
     * description: Configuration of Policy
     *  functionality
     * 
     * @param policies the value of field 'policies'.
     */
    public void setPolicies(final Policies policies) {
        this.policies = policies;
    }

    /**
     * Sets the value of field 'retries'. The field 'retries' has the following
     * description: The maximum number of retry before
     *  sending a failure.
     * 
     * @param retries the value of field 'retries'.
     */
    public void setRetries(final Integer retries) {
        this.retries = retries;
    }

    /**
     * Sets the value of field 'useCategories'. The field 'useCategories' has the
     * following description: If you want to use opennms categories
     *  to match rancid device type.
     * 
     * @param useCategories the value of field 'useCategories'.
     */
    public void setUseCategories(final Boolean useCategories) {
        this.useCategories = useCategories;
    }

}

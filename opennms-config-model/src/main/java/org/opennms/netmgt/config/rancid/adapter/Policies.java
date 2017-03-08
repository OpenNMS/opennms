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
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Configuration of Policy
 *  functionality
 * 
 * @version $Revision$ $Date$
 */
@XmlRootElement(name = "policies")
@XmlAccessorType(XmlAccessType.FIELD)
public class Policies implements java.io.Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * This represents a policy to manage a provisioned node
     *  if matched a node will be added updated or deleted using
     *  the element attribute definitions .
     */
    @XmlElement(name = "policy-manage", required = true)
    private java.util.List<PolicyManage> policyManageList;

    public Policies() {
        this.policyManageList = new java.util.ArrayList<PolicyManage>();
    }

    /**
     * 
     * 
     * @param vPolicyManage
     * @throws IndexOutOfBoundsException if the index given is outside
     * the bounds of the collection
     */
    public void addPolicyManage(final PolicyManage vPolicyManage) throws IndexOutOfBoundsException {
        this.policyManageList.add(vPolicyManage);
    }

    /**
     * 
     * 
     * @param index
     * @param vPolicyManage
     * @throws IndexOutOfBoundsException if the index given is outside
     * the bounds of the collection
     */
    public void addPolicyManage(final int index, final PolicyManage vPolicyManage) throws IndexOutOfBoundsException {
        this.policyManageList.add(index, vPolicyManage);
    }

    /**
     * Method enumeratePolicyManage.
     * 
     * @return an Enumeration over all possible elements of this collection
     */
    public java.util.Enumeration<PolicyManage> enumeratePolicyManage() {
        return java.util.Collections.enumeration(this.policyManageList);
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
        
        if (obj instanceof Policies) {
            Policies temp = (Policies)obj;
            boolean equals = Objects.equals(temp.policyManageList, policyManageList);
            return equals;
        }
        return false;
    }

    /**
     * Method getPolicyManage.
     * 
     * @param index
     * @throws IndexOutOfBoundsException if the index given is outside
     * the bounds of the collection
     * @return the value of the
     * PolicyManage at the given index
     */
    public PolicyManage getPolicyManage(final int index) throws IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this.policyManageList.size()) {
            throw new IndexOutOfBoundsException("getPolicyManage: Index value '" + index + "' not in range [0.." + (this.policyManageList.size() - 1) + "]");
        }
        
        return (PolicyManage) policyManageList.get(index);
    }

    /**
     * Method getPolicyManage.Returns the contents of the collection in an Array. 
     * <p>Note:  Just in case the collection contents are changing in another
     * thread, we pass a 0-length Array of the correct type into the API call. 
     * This way we <i>know</i> that the Array returned is of exactly the correct
     * length.
     * 
     * @return this collection as an Array
     */
    public PolicyManage[] getPolicyManage() {
        PolicyManage[] array = new PolicyManage[0];
        return (PolicyManage[]) this.policyManageList.toArray(array);
    }

    /**
     * Method getPolicyManageCollection.Returns a reference to 'policyManageList'.
     * No type checking is performed on any modifications to the Vector.
     * 
     * @return a reference to the Vector backing this class
     */
    public java.util.List<PolicyManage> getPolicyManageCollection() {
        return this.policyManageList;
    }

    /**
     * Method getPolicyManageCount.
     * 
     * @return the size of this collection
     */
    public int getPolicyManageCount() {
        return this.policyManageList.size();
    }

    /**
     * Method hashCode.
     * 
     * @return a hash code value for the object.
     */
    @Override
    public int hashCode() {
        int hash = Objects.hash(
            policyManageList);
        return hash;
    }

    /**
     * Method iteratePolicyManage.
     * 
     * @return an Iterator over all possible elements in this collection
     */
    public java.util.Iterator<PolicyManage> iteratePolicyManage() {
        return this.policyManageList.iterator();
    }

    /**
     */
    public void removeAllPolicyManage() {
        this.policyManageList.clear();
    }

    /**
     * Method removePolicyManage.
     * 
     * @param vPolicyManage
     * @return true if the object was removed from the collection.
     */
    public boolean removePolicyManage(final PolicyManage vPolicyManage) {
        boolean removed = policyManageList.remove(vPolicyManage);
        return removed;
    }

    /**
     * Method removePolicyManageAt.
     * 
     * @param index
     * @return the element removed from the collection
     */
    public PolicyManage removePolicyManageAt(final int index) {
        Object obj = this.policyManageList.remove(index);
        return (PolicyManage) obj;
    }

    /**
     * 
     * 
     * @param index
     * @param vPolicyManage
     * @throws IndexOutOfBoundsException if the index given is outside
     * the bounds of the collection
     */
    public void setPolicyManage(final int index, final PolicyManage vPolicyManage) throws IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this.policyManageList.size()) {
            throw new IndexOutOfBoundsException("setPolicyManage: Index value '" + index + "' not in range [0.." + (this.policyManageList.size() - 1) + "]");
        }
        
        this.policyManageList.set(index, vPolicyManage);
    }

    /**
     * 
     * 
     * @param vPolicyManageArray
     */
    public void setPolicyManage(final PolicyManage[] vPolicyManageArray) {
        //-- copy array
        policyManageList.clear();
        
        for (int i = 0; i < vPolicyManageArray.length; i++) {
                this.policyManageList.add(vPolicyManageArray[i]);
        }
    }

    /**
     * Sets the value of 'policyManageList' by copying the given Vector. All
     * elements will be checked for type safety.
     * 
     * @param vPolicyManageList the Vector to copy.
     */
    public void setPolicyManage(final java.util.List<PolicyManage> vPolicyManageList) {
        // copy vector
        this.policyManageList.clear();
        
        this.policyManageList.addAll(vPolicyManageList);
    }

    /**
     * Sets the value of 'policyManageList' by setting it to the given Vector. No
     * type checking is performed.
     * @deprecated
     * 
     * @param policyManageList the Vector to set.
     */
    public void setPolicyManageCollection(final java.util.List<PolicyManage> policyManageList) {
        this.policyManageList = policyManageList;
    }

}

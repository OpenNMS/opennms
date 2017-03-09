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

package org.opennms.netmgt.config.wmi.agent;

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
 * This is the top-level element for wmi-config.xml
 *  
 * 
 * @version $Revision$ $Date$
 */
@XmlRootElement(name = "wmi-config")
@XmlAccessorType(XmlAccessType.FIELD)
public class WmiConfig implements java.io.Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * Default timeout (in milliseconds).
     *  
     */
    @XmlAttribute(name = "timeout")
    private Integer timeout;

    /**
     * Default number of retries.
     *  
     */
    @XmlAttribute(name = "retry")
    private Integer retry;

    /**
     * Default username.
     */
    @XmlAttribute(name = "username")
    private String username;

    /**
     * Default Windows Domain.
     *  
     */
    @XmlAttribute(name = "domain")
    private String domain;

    /**
     * Default user password.
     *  
     */
    @XmlAttribute(name = "password")
    private String password;

    /**
     * Maps IP addresses to specific SNMP parmeters
     *  (retries, timeouts...)
     */
    @XmlElement(name = "definition")
    private List<Definition> definitionList = new ArrayList<>();

    /**
     * 
     * 
     * @param vDefinition
     * @throws IndexOutOfBoundsException if the index given is outside
     * the bounds of the collection
     */
    public void addDefinition(final Definition vDefinition) throws IndexOutOfBoundsException {
        this.definitionList.add(vDefinition);
    }

    /**
     * 
     * 
     * @param index
     * @param vDefinition
     * @throws IndexOutOfBoundsException if the index given is outside
     * the bounds of the collection
     */
    public void addDefinition(final int index, final Definition vDefinition) throws IndexOutOfBoundsException {
        this.definitionList.add(index, vDefinition);
    }

    /**
     */
    public void deleteRetry() {
        this.retry= null;
    }

    /**
     */
    public void deleteTimeout() {
        this.timeout= null;
    }

    /**
     * Method enumerateDefinition.
     * 
     * @return an Enumeration over all possible elements of this collection
     */
    public Enumeration<Definition> enumerateDefinition() {
        return Collections.enumeration(this.definitionList);
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
        
        if (obj instanceof WmiConfig) {
            WmiConfig temp = (WmiConfig)obj;
            boolean equals = Objects.equals(temp.timeout, timeout)
                && Objects.equals(temp.retry, retry)
                && Objects.equals(temp.username, username)
                && Objects.equals(temp.domain, domain)
                && Objects.equals(temp.password, password)
                && Objects.equals(temp.definitionList, definitionList);
            return equals;
        }
        return false;
    }

    /**
     * Method getDefinition.
     * 
     * @param index
     * @throws IndexOutOfBoundsException if the index given is outside
     * the bounds of the collection
     * @return the value of the Definition at
     * the given index
     */
    public Definition getDefinition(final int index) throws IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this.definitionList.size()) {
            throw new IndexOutOfBoundsException("getDefinition: Index value '" + index + "' not in range [0.." + (this.definitionList.size() - 1) + "]");
        }
        
        return (Definition) definitionList.get(index);
    }

    /**
     * Method getDefinition.Returns the contents of the collection in an Array. 
     * <p>Note:  Just in case the collection contents are changing in another
     * thread, we pass a 0-length Array of the correct type into the API call. 
     * This way we <i>know</i> that the Array returned is of exactly the correct
     * length.
     * 
     * @return this collection as an Array
     */
    public Definition[] getDefinition() {
        Definition[] array = new Definition[0];
        return (Definition[]) this.definitionList.toArray(array);
    }

    /**
     * Method getDefinitionCollection.Returns a reference to 'definitionList'. No
     * type checking is performed on any modifications to the Vector.
     * 
     * @return a reference to the Vector backing this class
     */
    public List<Definition> getDefinitionCollection() {
        return this.definitionList;
    }

    /**
     * Method getDefinitionCount.
     * 
     * @return the size of this collection
     */
    public int getDefinitionCount() {
        return this.definitionList.size();
    }

    /**
     * Returns the value of field 'domain'. The field 'domain' has the following
     * description: Default Windows Domain.
     *  
     * 
     * @return the value of field 'Domain'.
     */
    public String getDomain() {
        return this.domain;
    }

    /**
     * Returns the value of field 'password'. The field 'password' has the
     * following description: Default user password.
     *  
     * 
     * @return the value of field 'Password'.
     */
    public String getPassword() {
        return this.password;
    }

    /**
     * Returns the value of field 'retry'. The field 'retry' has the following
     * description: Default number of retries.
     *  
     * 
     * @return the value of field 'Retry'.
     */
    public Integer getRetry() {
        return this.retry;
    }

    /**
     * Returns the value of field 'timeout'. The field 'timeout' has the following
     * description: Default timeout (in milliseconds).
     *  
     * 
     * @return the value of field 'Timeout'.
     */
    public Integer getTimeout() {
        return this.timeout;
    }

    /**
     * Returns the value of field 'username'. The field 'username' has the
     * following description: Default username.
     * 
     * @return the value of field 'Username'.
     */
    public String getUsername() {
        return this.username;
    }

    /**
     * Method hasRetry.
     * 
     * @return true if at least one Retry has been added
     */
    public boolean hasRetry() {
        return this.retry != null;
    }

    /**
     * Method hasTimeout.
     * 
     * @return true if at least one Timeout has been added
     */
    public boolean hasTimeout() {
        return this.timeout != null;
    }

    /**
     * Method hashCode.
     * 
     * @return a hash code value for the object.
     */
    @Override
    public int hashCode() {
        int hash = Objects.hash(
            timeout, 
            retry, 
            username, 
            domain, 
            password, 
            definitionList);
        return hash;
    }

    /**
     * Method iterateDefinition.
     * 
     * @return an Iterator over all possible elements in this collection
     */
    public Iterator<Definition> iterateDefinition() {
        return this.definitionList.iterator();
    }

    /**
     */
    public void removeAllDefinition() {
        this.definitionList.clear();
    }

    /**
     * Method removeDefinition.
     * 
     * @param vDefinition
     * @return true if the object was removed from the collection.
     */
    public boolean removeDefinition(final Definition vDefinition) {
        boolean removed = definitionList.remove(vDefinition);
        return removed;
    }

    /**
     * Method removeDefinitionAt.
     * 
     * @param index
     * @return the element removed from the collection
     */
    public Definition removeDefinitionAt(final int index) {
        Object obj = this.definitionList.remove(index);
        return (Definition) obj;
    }

    /**
     * 
     * 
     * @param index
     * @param vDefinition
     * @throws IndexOutOfBoundsException if the index given is outside
     * the bounds of the collection
     */
    public void setDefinition(final int index, final Definition vDefinition) throws IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this.definitionList.size()) {
            throw new IndexOutOfBoundsException("setDefinition: Index value '" + index + "' not in range [0.." + (this.definitionList.size() - 1) + "]");
        }
        
        this.definitionList.set(index, vDefinition);
    }

    /**
     * 
     * 
     * @param vDefinitionArray
     */
    public void setDefinition(final Definition[] vDefinitionArray) {
        //-- copy array
        definitionList.clear();
        
        for (int i = 0; i < vDefinitionArray.length; i++) {
                this.definitionList.add(vDefinitionArray[i]);
        }
    }

    /**
     * Sets the value of 'definitionList' by copying the given Vector. All
     * elements will be checked for type safety.
     * 
     * @param vDefinitionList the Vector to copy.
     */
    public void setDefinition(final List<Definition> vDefinitionList) {
        // copy vector
        this.definitionList.clear();
        
        this.definitionList.addAll(vDefinitionList);
    }

    /**
     * Sets the value of 'definitionList' by setting it to the given Vector. No
     * type checking is performed.
     * @deprecated
     * 
     * @param definitionList the Vector to set.
     */
    public void setDefinitionCollection(final List<Definition> definitionList) {
        this.definitionList = definitionList;
    }

    /**
     * Sets the value of field 'domain'. The field 'domain' has the following
     * description: Default Windows Domain.
     *  
     * 
     * @param domain the value of field 'domain'.
     */
    public void setDomain(final String domain) {
        this.domain = domain;
    }

    /**
     * Sets the value of field 'password'. The field 'password' has the following
     * description: Default user password.
     *  
     * 
     * @param password the value of field 'password'.
     */
    public void setPassword(final String password) {
        this.password = password;
    }

    /**
     * Sets the value of field 'retry'. The field 'retry' has the following
     * description: Default number of retries.
     *  
     * 
     * @param retry the value of field 'retry'.
     */
    public void setRetry(final Integer retry) {
        this.retry = retry;
    }

    /**
     * Sets the value of field 'timeout'. The field 'timeout' has the following
     * description: Default timeout (in milliseconds).
     *  
     * 
     * @param timeout the value of field 'timeout'.
     */
    public void setTimeout(final Integer timeout) {
        this.timeout = timeout;
    }

    /**
     * Sets the value of field 'username'. The field 'username' has the following
     * description: Default username.
     * 
     * @param username the value of field 'username'.
     */
    public void setUsername(final String username) {
        this.username = username;
    }

}

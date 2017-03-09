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

import org.opennms.netmgt.config.wmi.WmiAgentConfig;

/**
 * Provides a mechanism for associating one or more
 *  specific IP addresses and/or IP address ranges with a
 *  set of WMI parms which will be used in place of the
 *  default values during WMI data collection.
 *  
 * 
 * @version $Revision$ $Date$
 */
@XmlRootElement(name = "definition")
@XmlAccessorType(XmlAccessType.FIELD)
public class Definition implements java.io.Serializable {
    private static final long serialVersionUID = 1L;

    @XmlAttribute(name = "retry")
    private Integer retry;

    @XmlAttribute(name = "timeout")
    private Integer timeout;

    @XmlAttribute(name = "username")
    private String username;

    @XmlAttribute(name = "domain")
    private String domain;

    @XmlAttribute(name = "password")
    private String password;

    /**
     * IP address range to which this definition
     *  applies.
     *  
     */
    @XmlElement(name = "range")
    private List<Range> rangeList = new ArrayList<>();

    /**
     * Specific IP address to which this definition
     *  applies.
     *  
     */
    @XmlElement(name = "specific")
    private List<String> specificList = new ArrayList<>();

    /**
     * Match Octets (as in IPLIKE)
     *  
     */
    @XmlElement(name = "ip-match")
    private List<String> ipMatchList = new ArrayList<>();

    /**
     * 
     * 
     * @param vIpMatch
     * @throws IndexOutOfBoundsException if the index given is outside
     * the bounds of the collection
     */
    public void addIpMatch(final String vIpMatch) throws IndexOutOfBoundsException {
        this.ipMatchList.add(vIpMatch);
    }

    /**
     * 
     * 
     * @param index
     * @param vIpMatch
     * @throws IndexOutOfBoundsException if the index given is outside
     * the bounds of the collection
     */
    public void addIpMatch(final int index, final String vIpMatch) throws IndexOutOfBoundsException {
        this.ipMatchList.add(index, vIpMatch);
    }

    /**
     * 
     * 
     * @param vRange
     * @throws IndexOutOfBoundsException if the index given is outside
     * the bounds of the collection
     */
    public void addRange(final Range vRange) throws IndexOutOfBoundsException {
        this.rangeList.add(vRange);
    }

    /**
     * 
     * 
     * @param index
     * @param vRange
     * @throws IndexOutOfBoundsException if the index given is outside
     * the bounds of the collection
     */
    public void addRange(final int index, final Range vRange) throws IndexOutOfBoundsException {
        this.rangeList.add(index, vRange);
    }

    /**
     * 
     * 
     * @param vSpecific
     * @throws IndexOutOfBoundsException if the index given is outside
     * the bounds of the collection
     */
    public void addSpecific(final String vSpecific) throws IndexOutOfBoundsException {
        this.specificList.add(vSpecific);
    }

    /**
     * 
     * 
     * @param index
     * @param vSpecific
     * @throws IndexOutOfBoundsException if the index given is outside
     * the bounds of the collection
     */
    public void addSpecific(final int index, final String vSpecific) throws IndexOutOfBoundsException {
        this.specificList.add(index, vSpecific);
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
     * Method enumerateIpMatch.
     * 
     * @return an Enumeration over all possible elements of this collection
     */
    public Enumeration<String> enumerateIpMatch() {
        return Collections.enumeration(this.ipMatchList);
    }

    /**
     * Method enumerateRange.
     * 
     * @return an Enumeration over all possible elements of this collection
     */
    public Enumeration<Range> enumerateRange() {
        return Collections.enumeration(this.rangeList);
    }

    /**
     * Method enumerateSpecific.
     * 
     * @return an Enumeration over all possible elements of this collection
     */
    public Enumeration<String> enumerateSpecific() {
        return Collections.enumeration(this.specificList);
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
        
        if (obj instanceof Definition) {
            Definition temp = (Definition)obj;
            boolean equals = Objects.equals(temp.retry, retry)
                && Objects.equals(temp.timeout, timeout)
                && Objects.equals(temp.username, username)
                && Objects.equals(temp.domain, domain)
                && Objects.equals(temp.password, password)
                && Objects.equals(temp.rangeList, rangeList)
                && Objects.equals(temp.specificList, specificList)
                && Objects.equals(temp.ipMatchList, ipMatchList);
            return equals;
        }
        return false;
    }

    /**
     * Returns the value of field 'domain'.
     * 
     * @return the value of field 'Domain'.
     */
    public String getDomain() {
        return this.domain;
    }

    /**
     * Method getIpMatch.
     * 
     * @param index
     * @throws IndexOutOfBoundsException if the index given is outside
     * the bounds of the collection
     * @return the value of the String at the given index
     */
    public String getIpMatch(final int index) throws IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this.ipMatchList.size()) {
            throw new IndexOutOfBoundsException("getIpMatch: Index value '" + index + "' not in range [0.." + (this.ipMatchList.size() - 1) + "]");
        }
        
        return (String) ipMatchList.get(index);
    }

    /**
     * Method getIpMatch.Returns the contents of the collection in an Array. 
     * <p>Note:  Just in case the collection contents are changing in another
     * thread, we pass a 0-length Array of the correct type into the API call. 
     * This way we <i>know</i> that the Array returned is of exactly the correct
     * length.
     * 
     * @return this collection as an Array
     */
    public String[] getIpMatch() {
        String[] array = new String[0];
        return (String[]) this.ipMatchList.toArray(array);
    }

    /**
     * Method getIpMatchCollection.Returns a reference to 'ipMatchList'. No type
     * checking is performed on any modifications to the Vector.
     * 
     * @return a reference to the Vector backing this class
     */
    public List<String> getIpMatchCollection() {
        return this.ipMatchList;
    }

    /**
     * Method getIpMatchCount.
     * 
     * @return the size of this collection
     */
    public int getIpMatchCount() {
        return this.ipMatchList.size();
    }

    /**
     * Returns the value of field 'password'.
     * 
     * @return the value of field 'Password'.
     */
    public String getPassword() {
        return this.password;
    }

    /**
     * Method getRange.
     * 
     * @param index
     * @throws IndexOutOfBoundsException if the index given is outside
     * the bounds of the collection
     * @return the value of the Range at the
     * given index
     */
    public Range getRange(final int index) throws IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this.rangeList.size()) {
            throw new IndexOutOfBoundsException("getRange: Index value '" + index + "' not in range [0.." + (this.rangeList.size() - 1) + "]");
        }
        
        return (Range) rangeList.get(index);
    }

    /**
     * Method getRange.Returns the contents of the collection in an Array. 
     * <p>Note:  Just in case the collection contents are changing in another
     * thread, we pass a 0-length Array of the correct type into the API call. 
     * This way we <i>know</i> that the Array returned is of exactly the correct
     * length.
     * 
     * @return this collection as an Array
     */
    public Range[] getRange() {
        Range[] array = new Range[0];
        return (Range[]) this.rangeList.toArray(array);
    }

    /**
     * Method getRangeCollection.Returns a reference to 'rangeList'. No type
     * checking is performed on any modifications to the Vector.
     * 
     * @return a reference to the Vector backing this class
     */
    public List<Range> getRangeCollection() {
        return this.rangeList;
    }

    /**
     * Method getRangeCount.
     * 
     * @return the size of this collection
     */
    public int getRangeCount() {
        return this.rangeList.size();
    }

    /**
     * Returns the value of field 'retry'.
     * 
     * @return the value of field 'Retry'.
     */
    public Integer getRetry() {
        return this.retry != null ? this.retry : WmiAgentConfig.DEFAULT_RETRIES;
    }

    /**
     * Method getSpecific.
     * 
     * @param index
     * @throws IndexOutOfBoundsException if the index given is outside
     * the bounds of the collection
     * @return the value of the String at the given index
     */
    public String getSpecific(final int index) throws IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this.specificList.size()) {
            throw new IndexOutOfBoundsException("getSpecific: Index value '" + index + "' not in range [0.." + (this.specificList.size() - 1) + "]");
        }
        
        return (String) specificList.get(index);
    }

    /**
     * Method getSpecific.Returns the contents of the collection in an Array. 
     * <p>Note:  Just in case the collection contents are changing in another
     * thread, we pass a 0-length Array of the correct type into the API call. 
     * This way we <i>know</i> that the Array returned is of exactly the correct
     * length.
     * 
     * @return this collection as an Array
     */
    public String[] getSpecific() {
        String[] array = new String[0];
        return (String[]) this.specificList.toArray(array);
    }

    /**
     * Method getSpecificCollection.Returns a reference to 'specificList'. No type
     * checking is performed on any modifications to the Vector.
     * 
     * @return a reference to the Vector backing this class
     */
    public List<String> getSpecificCollection() {
        return this.specificList;
    }

    /**
     * Method getSpecificCount.
     * 
     * @return the size of this collection
     */
    public int getSpecificCount() {
        return this.specificList.size();
    }

    /**
     * Returns the value of field 'timeout'.
     * 
     * @return the value of field 'Timeout'.
     */
    public Integer getTimeout() {
        return this.timeout != null ? this.timeout : WmiAgentConfig.DEFAULT_TIMEOUT;
    }

    /**
     * Returns the value of field 'username'.
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
            retry, 
            timeout, 
            username, 
            domain, 
            password, 
            rangeList, 
            specificList, 
            ipMatchList);
        return hash;
    }

    /**
     * Method iterateIpMatch.
     * 
     * @return an Iterator over all possible elements in this collection
     */
    public Iterator<String> iterateIpMatch() {
        return this.ipMatchList.iterator();
    }

    /**
     * Method iterateRange.
     * 
     * @return an Iterator over all possible elements in this collection
     */
    public Iterator<Range> iterateRange() {
        return this.rangeList.iterator();
    }

    /**
     * Method iterateSpecific.
     * 
     * @return an Iterator over all possible elements in this collection
     */
    public Iterator<String> iterateSpecific() {
        return this.specificList.iterator();
    }

    /**
     */
    public void removeAllIpMatch() {
        this.ipMatchList.clear();
    }

    /**
     */
    public void removeAllRange() {
        this.rangeList.clear();
    }

    /**
     */
    public void removeAllSpecific() {
        this.specificList.clear();
    }

    /**
     * Method removeIpMatch.
     * 
     * @param vIpMatch
     * @return true if the object was removed from the collection.
     */
    public boolean removeIpMatch(final String vIpMatch) {
        boolean removed = ipMatchList.remove(vIpMatch);
        return removed;
    }

    /**
     * Method removeIpMatchAt.
     * 
     * @param index
     * @return the element removed from the collection
     */
    public String removeIpMatchAt(final int index) {
        Object obj = this.ipMatchList.remove(index);
        return (String) obj;
    }

    /**
     * Method removeRange.
     * 
     * @param vRange
     * @return true if the object was removed from the collection.
     */
    public boolean removeRange(final Range vRange) {
        boolean removed = rangeList.remove(vRange);
        return removed;
    }

    /**
     * Method removeRangeAt.
     * 
     * @param index
     * @return the element removed from the collection
     */
    public Range removeRangeAt(final int index) {
        Object obj = this.rangeList.remove(index);
        return (Range) obj;
    }

    /**
     * Method removeSpecific.
     * 
     * @param vSpecific
     * @return true if the object was removed from the collection.
     */
    public boolean removeSpecific(final String vSpecific) {
        boolean removed = specificList.remove(vSpecific);
        return removed;
    }

    /**
     * Method removeSpecificAt.
     * 
     * @param index
     * @return the element removed from the collection
     */
    public String removeSpecificAt(final int index) {
        Object obj = this.specificList.remove(index);
        return (String) obj;
    }

    /**
     * Sets the value of field 'domain'.
     * 
     * @param domain the value of field 'domain'.
     */
    public void setDomain(final String domain) {
        this.domain = domain;
    }

    /**
     * 
     * 
     * @param index
     * @param vIpMatch
     * @throws IndexOutOfBoundsException if the index given is outside
     * the bounds of the collection
     */
    public void setIpMatch(final int index, final String vIpMatch) throws IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this.ipMatchList.size()) {
            throw new IndexOutOfBoundsException("setIpMatch: Index value '" + index + "' not in range [0.." + (this.ipMatchList.size() - 1) + "]");
        }
        
        this.ipMatchList.set(index, vIpMatch);
    }

    /**
     * 
     * 
     * @param vIpMatchArray
     */
    public void setIpMatch(final String[] vIpMatchArray) {
        //-- copy array
        ipMatchList.clear();
        
        for (int i = 0; i < vIpMatchArray.length; i++) {
                this.ipMatchList.add(vIpMatchArray[i]);
        }
    }

    /**
     * Sets the value of 'ipMatchList' by copying the given Vector. All elements
     * will be checked for type safety.
     * 
     * @param vIpMatchList the Vector to copy.
     */
    public void setIpMatch(final List<String> vIpMatchList) {
        // copy vector
        this.ipMatchList.clear();
        
        this.ipMatchList.addAll(vIpMatchList);
    }

    /**
     * Sets the value of 'ipMatchList' by setting it to the given Vector. No type
     * checking is performed.
     * @deprecated
     * 
     * @param ipMatchList the Vector to set.
     */
    public void setIpMatchCollection(final List<String> ipMatchList) {
        this.ipMatchList = ipMatchList;
    }

    /**
     * Sets the value of field 'password'.
     * 
     * @param password the value of field 'password'.
     */
    public void setPassword(final String password) {
        this.password = password;
    }

    /**
     * 
     * 
     * @param index
     * @param vRange
     * @throws IndexOutOfBoundsException if the index given is outside
     * the bounds of the collection
     */
    public void setRange(final int index, final Range vRange) throws IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this.rangeList.size()) {
            throw new IndexOutOfBoundsException("setRange: Index value '" + index + "' not in range [0.." + (this.rangeList.size() - 1) + "]");
        }
        
        this.rangeList.set(index, vRange);
    }

    /**
     * 
     * 
     * @param vRangeArray
     */
    public void setRange(final Range[] vRangeArray) {
        //-- copy array
        rangeList.clear();
        
        for (int i = 0; i < vRangeArray.length; i++) {
                this.rangeList.add(vRangeArray[i]);
        }
    }

    /**
     * Sets the value of 'rangeList' by copying the given Vector. All elements
     * will be checked for type safety.
     * 
     * @param vRangeList the Vector to copy.
     */
    public void setRange(final List<Range> vRangeList) {
        // copy vector
        this.rangeList.clear();
        
        this.rangeList.addAll(vRangeList);
    }

    /**
     * Sets the value of 'rangeList' by setting it to the given Vector. No type
     * checking is performed.
     * @deprecated
     * 
     * @param rangeList the Vector to set.
     */
    public void setRangeCollection(final List<Range> rangeList) {
        this.rangeList = rangeList;
    }

    /**
     * Sets the value of field 'retry'.
     * 
     * @param retry the value of field 'retry'.
     */
    public void setRetry(final Integer retry) {
        this.retry = retry;
    }

    /**
     * 
     * 
     * @param index
     * @param vSpecific
     * @throws IndexOutOfBoundsException if the index given is outside
     * the bounds of the collection
     */
    public void setSpecific(final int index, final String vSpecific) throws IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this.specificList.size()) {
            throw new IndexOutOfBoundsException("setSpecific: Index value '" + index + "' not in range [0.." + (this.specificList.size() - 1) + "]");
        }
        
        this.specificList.set(index, vSpecific);
    }

    /**
     * 
     * 
     * @param vSpecificArray
     */
    public void setSpecific(final String[] vSpecificArray) {
        //-- copy array
        specificList.clear();
        
        for (int i = 0; i < vSpecificArray.length; i++) {
                this.specificList.add(vSpecificArray[i]);
        }
    }

    /**
     * Sets the value of 'specificList' by copying the given Vector. All elements
     * will be checked for type safety.
     * 
     * @param vSpecificList the Vector to copy.
     */
    public void setSpecific(final List<String> vSpecificList) {
        // copy vector
        this.specificList.clear();
        
        this.specificList.addAll(vSpecificList);
    }

    /**
     * Sets the value of 'specificList' by setting it to the given Vector. No type
     * checking is performed.
     * @deprecated
     * 
     * @param specificList the Vector to set.
     */
    public void setSpecificCollection(final List<String> specificList) {
        this.specificList = specificList;
    }

    /**
     * Sets the value of field 'timeout'.
     * 
     * @param timeout the value of field 'timeout'.
     */
    public void setTimeout(final Integer timeout) {
        this.timeout = timeout;
    }

    /**
     * Sets the value of field 'username'.
     * 
     * @param username the value of field 'username'.
     */
    public void setUsername(final String username) {
        this.username = username;
    }

}

/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2013-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
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
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.config.ami;

//---------------------------------/
//- Imported classes and packages -/
//---------------------------------/

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Provides a mechanism for associating one or more specific IP addresses
 * and/or IP address ranges with a set of AMI parms which will be used in
 * place of the default values during AMI operations.
 */
@XmlRootElement(name = "definition")
@XmlAccessorType(XmlAccessType.FIELD)
public class Definition implements Serializable {
    private static final long serialVersionUID = -5866885462906545340L;

    // --------------------------/
    // - Class/Member Variables -/
    // --------------------------/

    /**
     * Field _port.
     */
    @XmlAttribute(name = "port")
    private Integer _port;

    /**
     * Field _useSsl.
     */
    @XmlAttribute(name = "use-ssl")
    private Boolean _useSsl;

    /**
     * Field _timeout.
     */
    @XmlAttribute(name = "timeout")
    private Integer _timeout;

    /**
     * Field _retry.
     */
    @XmlAttribute(name = "retry")
    private Integer _retry;

    /**
     * Field _username.
     */
    @XmlAttribute(name = "username")
    private String _username;

    /**
     * Field _password.
     */
    @XmlAttribute(name = "password")
    private String _password;

    /**
     * IP address range to which this definition applies.
     *
     */
    @XmlElement(name = "range")
    private List<Range> _rangeList = new ArrayList<Range>(0);

    /**
     * Specific IP address to which this definition applies.
     *
     */
    @XmlElement(name = "specific")
    private List<String> _specificList = new ArrayList<String>(0);

    /**
     * Match Octets (as in IPLIKE)
     *
     */
    @XmlElement(name = "ip-match")
    private List<String> _ipMatchList = new ArrayList<String>(0);

    // ----------------/
    // - Constructors -/
    // ----------------/

    public Definition() {
        super();
    }

    public Definition(final int port, final boolean useSsl,
            final int timeout, final int retry, final String username,
            final String password, final List<Range> ranges,
            List<String> specifics, List<String> ipMatches) {
        super();
        setPort(port);
        setUseSsl(useSsl);
        setTimeout(timeout);
        setRetry(retry);
        setUsername(username);
        setPassword(password);
        setRange(ranges);
        setSpecific(specifics);
        setIpMatch(ipMatches);
    }

    // -----------/
    // - Methods -/
    // -----------/

    /**
     *
     *
     * @param vIpMatch
     * @throws IndexOutOfBoundsException
     *             if the index given is outside the bounds of the collection
     */
    public void addIpMatch(final String vIpMatch)
            throws IndexOutOfBoundsException {
        this._ipMatchList.add(vIpMatch);
    }

    /**
     *
     *
     * @param index
     * @param vIpMatch
     * @throws IndexOutOfBoundsException
     *             if the index given is outside the bounds of the collection
     */
    public void addIpMatch(final int index, final String vIpMatch)
            throws IndexOutOfBoundsException {
        this._ipMatchList.add(index, vIpMatch);
    }

    /**
     *
     *
     * @param vRange
     * @throws IndexOutOfBoundsException
     *             if the index given is outside the bounds of the collection
     */
    public void addRange(final Range vRange) throws IndexOutOfBoundsException {
        this._rangeList.add(vRange);
    }

    /**
     *
     *
     * @param index
     * @param vRange
     * @throws IndexOutOfBoundsException
     *             if the index given is outside the bounds of the collection
     */
    public void addRange(final int index, final Range vRange)
            throws IndexOutOfBoundsException {
        this._rangeList.add(index, vRange);
    }

    /**
     *
     *
     * @param vSpecific
     * @throws IndexOutOfBoundsException
     *             if the index given is outside the bounds of the collection
     */
    public void addSpecific(final String vSpecific)
            throws IndexOutOfBoundsException {
        this._specificList.add(vSpecific);
    }

    /**
     *
     *
     * @param index
     * @param vSpecific
     * @throws IndexOutOfBoundsException
     *             if the index given is outside the bounds of the collection
     */
    public void addSpecific(final int index, final String vSpecific)
            throws IndexOutOfBoundsException {
        this._specificList.add(index, vSpecific);
    }

    /**
     * Method enumerateIpMatch.
     *
     * @return an Enumeration over all possible elements of this collection
     */
    public Enumeration<String> enumerateIpMatch() {
        return Collections.enumeration(this._ipMatchList);
    }

    /**
     * Method enumerateRange.
     *
     * @return an Enumeration over all possible elements of this collection
     */
    public Enumeration<Range> enumerateRange() {
        return Collections.enumeration(this._rangeList);
    }

    /**
     * Method enumerateSpecific.
     *
     * @return an Enumeration over all possible elements of this collection
     */
    public Enumeration<String> enumerateSpecific() {
        return Collections.enumeration(this._specificList);
    }

    /**
     * Overrides the Object.equals method.
     *
     * @param obj
     * @return true if the objects are equal.
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Definition other = (Definition) obj;
        if (_ipMatchList == null) {
            if (other._ipMatchList != null)
                return false;
        } else if (!_ipMatchList.equals(other._ipMatchList))
            return false;
        if (_password == null) {
            if (other._password != null)
                return false;
        } else if (!_password.equals(other._password))
            return false;
        if (_port == null) {
            if (other._port != null)
                return false;
        } else if (!_port.equals(other._port))
            return false;
        if (_rangeList == null) {
            if (other._rangeList != null)
                return false;
        } else if (!_rangeList.equals(other._rangeList))
            return false;
        if (_retry == null) {
            if (other._retry != null)
                return false;
        } else if (!_retry.equals(other._retry))
            return false;
        if (_specificList == null) {
            if (other._specificList != null)
                return false;
        } else if (!_specificList.equals(other._specificList))
            return false;
        if (_timeout == null) {
            if (other._timeout != null)
                return false;
        } else if (!_timeout.equals(other._timeout))
            return false;
        if (_useSsl == null) {
            if (other._useSsl != null)
                return false;
        } else if (!_useSsl.equals(other._useSsl))
            return false;
        if (_username == null) {
            if (other._username != null)
                return false;
        } else if (!_username.equals(other._username))
            return false;
        return true;
    }

    /**
     * Method getIpMatch.
     *
     * @param index
     * @throws IndexOutOfBoundsException
     *             if the index given is outside the bounds of the collection
     * @return the value of the String at the given index
     */
    public String getIpMatch(final int index) throws IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this._ipMatchList.size()) {
            throw new IndexOutOfBoundsException("getIpMatch: Index value '"
                    + index + "' not in range [0.."
                    + (this._ipMatchList.size() - 1) + "]");
        }

        return (String) _ipMatchList.get(index);
    }

    /**
     * Method getIpMatch.Returns the contents of the collection in an Array.
     * <p>
     * Note: Just in case the collection contents are changing in another
     * thread, we pass a 0-length Array of the correct type into the API call.
     * This way we <i>know</i> that the Array returned is of exactly the
     * correct length.
     *
     * @return this collection as an Array
     */
    public String[] getIpMatch() {
        String[] array = new String[0];
        return (String[]) this._ipMatchList.toArray(array);
    }

    /**
     * Method getIpMatchCollection.Returns a reference to '_ipMatchList'. No
     * type checking is performed on any modifications to the Vector.
     *
     * @return a reference to the Vector backing this class
     */
    public java.util.List<String> getIpMatchCollection() {
        return this._ipMatchList;
    }

    /**
     * Method getIpMatchCount.
     *
     * @return the size of this collection
     */
    public Integer getIpMatchCount() {
        return this._ipMatchList.size();
    }

    /**
     * Returns the value of field 'password'.
     *
     * @return the value of field 'Password'.
     */
    public Optional<String> getPassword() {
        return Optional.ofNullable(this._password);
    }

    /**
     * Returns the value of field 'port'.
     *
     * @return the value of field 'Port'.
     */
    public Optional<Integer> getPort() {
        return Optional.ofNullable(this._port);
    }

    /**
     * Method getRange.
     *
     * @param index
     * @throws IndexOutOfBoundsException
     *             if the index given is outside the bounds of the collection
     * @return the value of the Range at the given index
     */
    public Range getRange(final int index) throws IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this._rangeList.size()) {
            throw new IndexOutOfBoundsException("getRange: Index value '"
                    + index + "' not in range [0.."
                    + (this._rangeList.size() - 1) + "]");
        }

        return (Range) _rangeList.get(index);
    }

    /**
     * Method getRange.Returns the contents of the collection in an Array.
     * <p>
     * Note: Just in case the collection contents are changing in another
     * thread, we pass a 0-length Array of the correct type into the API call.
     * This way we <i>know</i> that the Array returned is of exactly the
     * correct length.
     *
     * @return this collection as an Array
     */
    public Range[] getRange() {
        Range[] array = new Range[0];
        return (Range[]) this._rangeList.toArray(array);
    }

    /**
     * Method getRangeCollection.Returns a reference to '_rangeList'. No type
     * checking is performed on any modifications to the Vector.
     *
     * @return a reference to the Vector backing this class
     */
    public List<Range> getRangeCollection() {
        return this._rangeList;
    }

    /**
     * Method getRangeCount.
     *
     * @return the size of this collection
     */
    public Integer getRangeCount() {
        return this._rangeList.size();
    }

    /**
     * Returns the value of field 'retry'.
     *
     * @return the value of field 'Retry'.
     */
    public Optional<Integer> getRetry() {
        return Optional.ofNullable(this._retry);
    }

    /**
     * Method getSpecific.
     *
     * @param index
     * @throws IndexOutOfBoundsException
     *             if the index given is outside the bounds of the collection
     * @return the value of the String at the given index
     */
    public String getSpecific(final int index)
            throws IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this._specificList.size()) {
            throw new IndexOutOfBoundsException("getSpecific: Index value '"
                    + index + "' not in range [0.."
                    + (this._specificList.size() - 1) + "]");
        }

        return (String) _specificList.get(index);
    }

    /**
     * Method getSpecific.Returns the contents of the collection in an Array.
     * <p>
     * Note: Just in case the collection contents are changing in another
     * thread, we pass a 0-length Array of the correct type into the API call.
     * This way we <i>know</i> that the Array returned is of exactly the
     * correct length.
     *
     * @return this collection as an Array
     */
    public String[] getSpecific() {
        String[] array = new String[0];
        return (String[]) this._specificList.toArray(array);
    }

    /**
     * Method getSpecificCollection.Returns a reference to '_specificList'. No
     * type checking is performed on any modifications to the Vector.
     *
     * @return a reference to the Vector backing this class
     */
    public List<String> getSpecificCollection() {
        return this._specificList;
    }

    /**
     * Method getSpecificCount.
     *
     * @return the size of this collection
     */
    public Integer getSpecificCount() {
        return this._specificList.size();
    }

    /**
     * Returns the value of field 'timeout'.
     *
     * @return the value of field 'Timeout'.
     */
    public Optional<Integer> getTimeout() {
        return Optional.ofNullable(this._timeout);
    }

    /**
     * Returns the value of field 'useSsl'.
     *
     * @return the value of field 'UseSsl'.
     */
    public boolean getUseSsl() {
        return this._useSsl == null? false : this._useSsl;
    }

    /**
     * Returns the value of field 'username'.
     *
     * @return the value of field 'Username'.
     */
    public Optional<String> getUsername() {
        return Optional.ofNullable(this._username);
    }

    /**
     * Method hasPort.
     *
     * @return true if at least one Port has been added
     */
    public boolean hasPort() {
        return _port != null;
    }

    /**
     * Method hasRetry.
     *
     * @return true if at least one Retry has been added
     */
    public boolean hasRetry() {
        return _retry != null;
    }

    /**
     * Method hasTimeout.
     *
     * @return true if at least one Timeout has been added
     */
    public boolean hasTimeout() {
        return _timeout != null;
    }

    /**
     * Method hasUseSsl.
     *
     * @return true if at least one UseSsl has been added
     */
    public boolean hasUseSsl() {
        return _useSsl != null;
    }

    /**
     * Overrides the Object.hashCode method.
     * <p>
     * The following steps came from <b>Effective Java Programming Language
     * Guide</b> by Joshua Bloch, Chapter 3
     *
     * @return a hash code value for the object.
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((_ipMatchList == null) ? 0 : _ipMatchList.hashCode());
        result = prime * result
                + ((_password == null) ? 0 : _password.hashCode());
        result = prime * result + ((_port == null) ? 0 : _port.hashCode());
        result = prime * result
                + ((_rangeList == null) ? 0 : _rangeList.hashCode());
        result = prime * result + ((_retry == null) ? 0 : _retry.hashCode());
        result = prime * result
                + ((_specificList == null) ? 0 : _specificList.hashCode());
        result = prime * result
                + ((_timeout == null) ? 0 : _timeout.hashCode());
        result = prime * result
                + ((_useSsl == null) ? 0 : _useSsl.hashCode());
        result = prime * result
                + ((_username == null) ? 0 : _username.hashCode());
        return result;
    }

    /**
     * Returns the value of field 'useSsl'.
     *
     * @return the value of field 'UseSsl'.
     */
    public boolean isUseSsl() {
        return this._useSsl == null? false : this._useSsl;
    }

    /**
     * Method iterateIpMatch.
     *
     * @return an Iterator over all possible elements in this collection
     */
    public Iterator<String> iterateIpMatch() {
        return this._ipMatchList.iterator();
    }

    /**
     * Method iterateRange.
     *
     * @return an Iterator over all possible elements in this collection
     */
    public Iterator<Range> iterateRange() {
        return this._rangeList.iterator();
    }

    /**
     * Method iterateSpecific.
     *
     * @return an Iterator over all possible elements in this collection
     */
    public Iterator<String> iterateSpecific() {
        return this._specificList.iterator();
    }

    /**
     */
    public void removeAllIpMatch() {
        this._ipMatchList.clear();
    }

    /**
     */
    public void removeAllRange() {
        this._rangeList.clear();
    }

    /**
     */
    public void removeAllSpecific() {
        this._specificList.clear();
    }

    /**
     * Method removeIpMatch.
     *
     * @param vIpMatch
     * @return true if the object was removed from the collection.
     */
    public boolean removeIpMatch(final String vIpMatch) {
        return _ipMatchList.remove(vIpMatch);
    }

    /**
     * Method removeIpMatchAt.
     *
     * @param index
     * @return the element removed from the collection
     */
    public String removeIpMatchAt(final int index) {
        return this._ipMatchList.remove(index);
    }

    /**
     * Method removeRange.
     *
     * @param vRange
     * @return true if the object was removed from the collection.
     */
    public boolean removeRange(final Range vRange) {
        return _rangeList.remove(vRange);
    }

    /**
     * Method removeRangeAt.
     *
     * @param index
     * @return the element removed from the collection
     */
    public Range removeRangeAt(final int index) {
        return this._rangeList.remove(index);
    }

    /**
     * Method removeSpecific.
     *
     * @param vSpecific
     * @return true if the object was removed from the collection.
     */
    public boolean removeSpecific(final String vSpecific) {
        return _specificList.remove(vSpecific);
    }

    /**
     * Method removeSpecificAt.
     *
     * @param index
     * @return the element removed from the collection
     */
    public String removeSpecificAt(final int index) {
        return this._specificList.remove(index);
    }

    /**
     *
     *
     * @param index
     * @param vIpMatch
     * @throws IndexOutOfBoundsException
     *             if the index given is outside the bounds of the collection
     */
    public void setIpMatch(final int index, final String vIpMatch)
            throws IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this._ipMatchList.size()) {
            throw new IndexOutOfBoundsException("setIpMatch: Index value '"
                    + index + "' not in range [0.."
                    + (this._ipMatchList.size() - 1) + "]");
        }

        this._ipMatchList.set(index, vIpMatch);
    }

    /**
     *
     *
     * @param vIpMatchArray
     */
    public void setIpMatch(final String[] vIpMatchArray) {
        // -- copy array
        _ipMatchList.clear();

        for (int i = 0; i < vIpMatchArray.length; i++) {
            this._ipMatchList.add(vIpMatchArray[i]);
        }
    }

    /**
     * Sets the value of '_ipMatchList' by copying the given Vector. All
     * elements will be checked for type safety.
     *
     * @param vIpMatchList
     *            the Vector to copy.
     */
    public void setIpMatch(final List<String> vIpMatchList) {
        // copy vector
        this._ipMatchList.clear();

        this._ipMatchList.addAll(vIpMatchList);
    }

    /**
     * Sets the value of field 'password'.
     *
     * @param password
     *            the value of field 'password'.
     */
    public void setPassword(final String password) {
        this._password = password;
    }

    /**
     * Sets the value of field 'port'.
     *
     * @param port
     *            the value of field 'port'.
     */
    public void setPort(final int port) {
        this._port = port;
    }

    /**
     *
     *
     * @param index
     * @param vRange
     * @throws IndexOutOfBoundsException
     *             if the index given is outside the bounds of the collection
     */
    public void setRange(final int index, final Range vRange)
            throws IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this._rangeList.size()) {
            throw new IndexOutOfBoundsException("setRange: Index value '"
                    + index + "' not in range [0.."
                    + (this._rangeList.size() - 1) + "]");
        }

        this._rangeList.set(index, vRange);
    }

    /**
     *
     *
     * @param vRangeArray
     */
    public void setRange(final Range[] vRangeArray) {
        // -- copy array
        _rangeList.clear();

        for (int i = 0; i < vRangeArray.length; i++) {
            this._rangeList.add(vRangeArray[i]);
        }
    }

    /**
     * Sets the value of '_rangeList' by copying the given Vector. All
     * elements will be checked for type safety.
     *
     * @param vRangeList
     *            the Vector to copy.
     */
    public void setRange(final List<Range> vRangeList) {
        // copy vector
        this._rangeList.clear();

        this._rangeList.addAll(vRangeList);
    }

    /**
     * Sets the value of field 'retry'.
     *
     * @param retry
     *            the value of field 'retry'.
     */
    public void setRetry(final int retry) {
        this._retry = retry;
    }

    /**
     *
     *
     * @param index
     * @param vSpecific
     * @throws IndexOutOfBoundsException
     *             if the index given is outside the bounds of the collection
     */
    public void setSpecific(final int index, final String vSpecific)
            throws IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this._specificList.size()) {
            throw new IndexOutOfBoundsException("setSpecific: Index value '"
                    + index + "' not in range [0.."
                    + (this._specificList.size() - 1) + "]");
        }

        this._specificList.set(index, vSpecific);
    }

    /**
     *
     *
     * @param vSpecificArray
     */
    public void setSpecific(final String[] vSpecificArray) {
        // -- copy array
        _specificList.clear();

        for (int i = 0; i < vSpecificArray.length; i++) {
            this._specificList.add(vSpecificArray[i]);
        }
    }

    /**
     * Sets the value of '_specificList' by copying the given Vector. All
     * elements will be checked for type safety.
     *
     * @param vSpecificList
     *            the Vector to copy.
     */
    public void setSpecific(final List<String> vSpecificList) {
        // copy vector
        this._specificList.clear();

        this._specificList.addAll(vSpecificList);
    }

    /**
     * Sets the value of field 'timeout'.
     *
     * @param timeout
     *            the value of field 'timeout'.
     */
    public void setTimeout(final int timeout) {
        this._timeout = timeout;
    }

    /**
     * Sets the value of field 'useSsl'.
     *
     * @param useSsl
     *            the value of field 'useSsl'.
     */
    public void setUseSsl(final boolean useSsl) {
        this._useSsl = useSsl;
    }

    /**
     * Sets the value of field 'username'.
     *
     * @param username
     *            the value of field 'username'.
     */
    public void setUsername(final String username) {
        this._username = username;
    }
}

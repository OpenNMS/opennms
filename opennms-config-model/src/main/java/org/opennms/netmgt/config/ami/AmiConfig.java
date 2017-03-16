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
 * This is the top-level element for ami-config.xml, which configures access
 * parameters for the Asterisk Manager Interface (AMI).
 *
 *
 * @version $Revision$ $Date$
 */
@XmlRootElement(name = "ami-config")
@XmlAccessorType(XmlAccessType.FIELD)
public class AmiConfig implements Serializable {
    private static final long serialVersionUID = -7864880053573062376L;

    // --------------------------/
    // - Class/Member Variables -/
    // --------------------------/

    /**
     * Default port.
     *
     */
    @XmlAttribute(name = "port")
    private Integer _port;

    /**
     * Field _useSsl.
     */
    @XmlAttribute(name = "use-ssl")
    private Boolean _useSsl;

    /**
     * Default connection timeout (in milliseconds).
     *
     */
    @XmlAttribute(name = "timeout")
    private Integer _timeout;

    /**
     * Default connection retries. Not currently used.
     *
     */
    @XmlAttribute(name = "retry")
    private Integer _retry;

    /**
     * Default AMI username (Name in brackets in Asterisk's manager.conf).
     *
     */
    @XmlAttribute(name = "username")
    private String _username;

    /**
     * Default AMI password.
     *
     */
    @XmlAttribute(name = "password")
    private String _password;

    /**
     * Maps IP addresses to specific AMI parameters (username, password,
     * port...)
     *
     */
    @XmlElement(name = "definition")
    private List<Definition> _definitionList = new ArrayList<Definition>(0);

    // ----------------/
    // - Constructors -/
    // ----------------/

    public AmiConfig() {
        super();
    }

    public AmiConfig(final int port, final boolean useSsl, final int timeout,
            final int retry, final String username, final String password,
            final List<Definition> definitions) {
        super();
        setPort(port);
        setUseSsl(useSsl);
        setTimeout(timeout);
        setRetry(retry);
        setUsername(username);
        setPassword(password);
        setDefinition(definitions);
    }

    // -----------/
    // - Methods -/
    // -----------/

    /**
     *
     *
     * @param vDefinition
     * @throws java.lang.IndexOutOfBoundsException
     *             if the index given is outside the bounds of the collection
     */
    public void addDefinition(final Definition vDefinition)
            throws IndexOutOfBoundsException {
        this._definitionList.add(vDefinition);
    }

    /**
     *
     *
     * @param index
     * @param vDefinition
     * @throws java.lang.IndexOutOfBoundsException
     *             if the index given is outside the bounds of the collection
     */
    public void addDefinition(final int index, final Definition vDefinition)
            throws IndexOutOfBoundsException {
        this._definitionList.add(index, vDefinition);
    }

    /**
     * Method enumerateDefinition.
     *
     * @return an Enumeration over all possible elements of this collection
     */
    public Enumeration<Definition> enumerateDefinition() {
        return Collections.enumeration(this._definitionList);
    }

    /**
     * Overrides the java.lang.Object.equals method.
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
        AmiConfig other = (AmiConfig) obj;
        if (_definitionList == null) {
            if (other._definitionList != null)
                return false;
        } else if (!_definitionList.equals(other._definitionList))
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
        if (_retry == null) {
            if (other._retry != null)
                return false;
        } else if (!_retry.equals(other._retry))
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
     * Method getDefinition.
     *
     * @param index
     * @throws java.lang.IndexOutOfBoundsException
     *             if the index given is outside the bounds of the collection
     * @return the value of the org.opennms.netmgt.config.ami.Definition at
     *         the given index
     */
    public Definition getDefinition(final int index)
            throws IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this._definitionList.size()) {
            throw new IndexOutOfBoundsException(
                                                "getDefinition: Index value '"
                                                        + index
                                                        + "' not in range [0.."
                                                        + (this._definitionList.size() - 1)
                                                        + "]");
        }

        return (Definition) _definitionList.get(index);
    }

    /**
     * Method getDefinition.Returns the contents of the collection in an
     * Array.
     * <p>
     * Note: Just in case the collection contents are changing in another
     * thread, we pass a 0-length Array of the correct type into the API call.
     * This way we <i>know</i> that the Array returned is of exactly the
     * correct length.
     *
     * @return this collection as an Array
     */
    public Definition[] getDefinition() {
        Definition[] array = new Definition[0];
        return (Definition[]) this._definitionList.toArray(array);
    }

    /**
     * Method getDefinitionCollection.Returns a reference to
     * '_definitionList'. No type checking is performed on any modifications
     * to the Vector.
     *
     * @return a reference to the Vector backing this class
     */
    public List<Definition> getDefinitionCollection() {
        return this._definitionList;
    }

    /**
     * Method getDefinitionCount.
     *
     * @return the size of this collection
     */
    public int getDefinitionCount() {
        return this._definitionList.size();
    }

    /**
     * Returns the value of field 'password'. The field 'password' has the
     * following description: Default AMI password.
     *
     *
     * @return the value of field 'Password'.
     */
    public Optional<String> getPassword() {
        return Optional.ofNullable(this._password);
    }

    /**
     * Returns the value of field 'port'. The field 'port' has the following
     * description: Default port.
     *
     *
     * @return the value of field 'Port'.
     */
    public Integer getPort() {
        return _port == null ? 5038 : _port;
    }

    /**
     * Returns the value of field 'retry'. The field 'retry' has the following
     * description: Default connection retries. Not currently used.
     *
     *
     * @return the value of field 'Retry'.
     */
    public Integer getRetry() {
        return _retry == null ? 0 : _retry;
    }

    /**
     * Returns the value of field 'timeout'. The field 'timeout' has the
     * following description: Default connection timeout (in milliseconds).
     *
     *
     * @return the value of field 'Timeout'.
     */
    public Integer getTimeout() {
        return _timeout == null ? 3000 : _timeout;
    }

    /**
     * Returns the value of field 'useSsl'.
     *
     * @return the value of field 'UseSsl'.
     */
    public Boolean getUseSsl() {
        return _useSsl == null ? false : _useSsl;
    }

    /**
     * Returns the value of field 'username'. The field 'username' has the
     * following description: Default AMI username (Name in brackets in
     * Asterisk's manager.conf).
     *
     *
     * @return the value of field 'Username'.
     */
    public Optional<String> getUsername() {
        return Optional.ofNullable(this._username);
    }

    /**
     * Overrides the java.lang.Object.hashCode method.
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
        result = prime
                * result
                + ((_definitionList == null) ? 0 : _definitionList.hashCode());
        result = prime * result
                + ((_password == null) ? 0 : _password.hashCode());
        result = prime * result + ((_port == null) ? 0 : _port.hashCode());
        result = prime * result + ((_retry == null) ? 0 : _retry.hashCode());
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
        return this._useSsl;
    }

    /**
     * Method iterateDefinition.
     *
     * @return an Iterator over all possible elements in this collection
     */
    public Iterator<Definition> iterateDefinition() {
        return this._definitionList.iterator();
    }

    /**
     */
    public void removeAllDefinition() {
        this._definitionList.clear();
    }

    /**
     * Method removeDefinition.
     *
     * @param vDefinition
     * @return true if the object was removed from the collection.
     */
    public boolean removeDefinition(final Definition vDefinition) {
    	return _definitionList.remove(vDefinition);
    }

    /**
     * Method removeDefinitionAt.
     *
     * @param index
     * @return the element removed from the collection
     */
    public Definition removeDefinitionAt(final int index) {
    	return (Definition) this._definitionList.remove(index);
    }

    /**
     *
     *
     * @param index
     * @param vDefinition
     * @throws java.lang.IndexOutOfBoundsException
     *             if the index given is outside the bounds of the collection
     */
    public void setDefinition(final int index, final Definition vDefinition)
            throws IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this._definitionList.size()) {
            throw new IndexOutOfBoundsException(
                                                "setDefinition: Index value '"
                                                        + index
                                                        + "' not in range [0.."
                                                        + (this._definitionList.size() - 1)
                                                        + "]");
        }

        this._definitionList.set(index, vDefinition);
    }

    /**
     *
     *
     * @param vDefinitionArray
     */
    public void setDefinition(final Definition[] vDefinitionArray) {
        // -- copy array
        _definitionList.clear();

        for (int i = 0; i < vDefinitionArray.length; i++) {
            this._definitionList.add(vDefinitionArray[i]);
        }
    }

    /**
     * Sets the value of '_definitionList' by copying the given Vector. All
     * elements will be checked for type safety.
     *
     * @param vDefinitionList
     *            the Vector to copy.
     */
    public void setDefinition(final List<Definition> vDefinitionList) {
        // copy vector
        this._definitionList.clear();

        this._definitionList.addAll(vDefinitionList);
    }

    /**
     * Sets the value of field 'password'. The field 'password' has the
     * following description: Default AMI password.
     *
     *
     * @param password
     *            the value of field 'password'.
     */
    public void setPassword(final String password) {
        this._password = password;
    }

    /**
     * Sets the value of field 'port'. The field 'port' has the following
     * description: Default port (in milliseconds).
     *
     *
     * @param port
     *            the value of field 'port'.
     */
    public void setPort(final Integer port) {
        this._port = port;
    }

    /**
     * Sets the value of field 'retry'. The field 'retry' has the following
     * description: Default connection retries. Not currently used.
     *
     *
     * @param retry
     *            the value of field 'retry'.
     */
    public void setRetry(final Integer retry) {
        this._retry = retry;
    }

    /**
     * Sets the value of field 'timeout'. The field 'timeout' has the
     * following description: Default connection timeout (in milliseconds).
     *
     *
     * @param timeout
     *            the value of field 'timeout'.
     */
    public void setTimeout(final Integer timeout) {
        this._timeout = timeout;
    }

    /**
     * Sets the value of field 'useSsl'.
     *
     * @param useSsl
     *            the value of field 'useSsl'.
     */
    public void setUseSsl(final Boolean useSsl) {
        this._useSsl = useSsl;
    }

    /**
     * Sets the value of field 'username'. The field 'username' has the
     * following description: Default AMI username (Name in brackets in
     * Asterisk's manager.conf).
     *
     *
     * @param username
     *            the value of field 'username'.
     */
    public void setUsername(final String username) {
        this._username = username;
    }
}

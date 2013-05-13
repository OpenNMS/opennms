/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

/**
 * This class was original generated with Castor, but is no longer.
 */
package org.opennms.netmgt.config.snmp;

import java.io.IOException;
import java.io.Reader;
import java.io.Serializable;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.Marshaller;
import org.exolab.castor.xml.Unmarshaller;
import org.exolab.castor.xml.ValidationException;
import org.exolab.castor.xml.Validator;
import org.xml.sax.ContentHandler;

/**
 * Top-level element for the snmp-config.xml configuration file.
 */

@XmlRootElement(name="snmp-config")
@XmlAccessorType(XmlAccessType.FIELD)
public class SnmpConfig extends Configuration implements Serializable {
	private static final long serialVersionUID = 3139857316489386441L;

	private static final Definition[] EMPTY_DEFINITION = new Definition[0];

	/**
     * Maps IP addresses to specific SNMP parameters (retries, timeouts...)
     */
	 @XmlElement(name="definition")
    private List<Definition> _definitionList = new ArrayList<Definition>();

    public SnmpConfig() {
        super();
    }

    public SnmpConfig(
    		final Integer port,
    		final Integer retry,
    		final Integer timeout,
    		final String readCommunity,
    		final String writeCommunity,
			final String proxyHost,
			final String version,
			final Integer maxVarsPerPdu,
			final Integer maxRepetitions,
			final Integer maxRequestSize,
			final String securityName,
			final Integer securityLevel,
			final String authPassphrase,
			final String authProtocol,
			final String engineId,
			final String contextEngineId,
			final String contextName,
			final String privacyPassphrase,
			final String privacyProtocol,
			final String enterpriseId,
			final List<Definition> definitionList) {
    	super(port, retry, timeout, readCommunity, writeCommunity, proxyHost, version, maxVarsPerPdu, maxRepetitions, maxRequestSize,
    			securityName, securityLevel, authPassphrase, authProtocol, engineId, contextEngineId, contextName, privacyPassphrase,
    			privacyProtocol, enterpriseId);
    	setDefinition(definitionList);
	}


	/**
     * 
     * 
     * @param vDefinition
     * @throws IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void addDefinition(final Definition vDefinition) throws IndexOutOfBoundsException {
        this._definitionList.add(vDefinition);
    }

    /**
     * 
     * 
     * @param index
     * @param vDefinition
     * @throws IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void addDefinition(final int index, final Definition vDefinition) throws IndexOutOfBoundsException {
        this._definitionList.add(index, vDefinition);
    }

    /**
     * Method enumerateDefinition.
     * 
     * @return an Enumeration over all possible elements of this
     * collection
     */
    public java.util.Enumeration<Definition> enumerateDefinition() {
        return Collections.enumeration(this._definitionList);
    }

    /**
     * Overrides the Object.equals method.
     * 
     * @param obj
     * @return true if the objects are equal.
     */
    @Override()
    public boolean equals(final Object obj) {
		if (obj instanceof Configuration == false) return false;
		if (this == obj) return true;

		final SnmpConfig temp = (SnmpConfig)obj;

		return new EqualsBuilder()
			.appendSuper(super.equals(obj))
			.append(getDefinitionCollection(), temp.getDefinitionCollection())
			.isEquals();
    }

    /**
     * Method getDefinition.
     * 
     * @param index
     * @throws IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     * @return the value of the
     * Definition at the given index
     */
    public Definition getDefinition(final int index) throws IndexOutOfBoundsException {
        if (index < 0 || index >= this._definitionList.size()) {
            throw new IndexOutOfBoundsException("getDefinition: Index value '" + index + "' not in range [0.." + (this._definitionList.size() - 1) + "]");
        }
        
        return _definitionList.get(index);
    }

    /**
     * Method getDefinition.Returns the contents of the collection
     * in an Array.  <p>Note:  Just in case the collection contents
     * are changing in another thread, we pass a 0-length Array of
     * the correct type into the API call.  This way we <i>know</i>
     * that the Array returned is of exactly the correct length.
     * 
     * @return this collection as an Array
     */
    public Definition[] getDefinition() {
        return this._definitionList.toArray(EMPTY_DEFINITION);
    }

    /**
     * Method getDefinitionCollection.Returns a reference to
     * '_definitionList'. No type checking is performed on any
     * modifications to the Vector.
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
     * Overrides the Object.hashCode method.
     * <p>
     * The following steps came from <b>Effective Java Programming
     * Language Guide</b> by Joshua Bloch, Chapter 3
     * 
     * @return a hash code value for the object.
     */
        @Override
    public int hashCode() {
    	final int result = 17;
        
        if (_definitionList != null) {
        	return 37 * result + _definitionList.hashCode();
        }
        
        return result;
    }

    /**
     * Method isValid.
     * 
     * @return true if this object is valid according to the schema
     */
        @Override
    public boolean isValid() {
        try {
            validate();
        } catch (final ValidationException vex) {
            return false;
        }
        return true;
    }

    /**
     * Method iterateDefinition.
     * 
     * @return an Iterator over all possible elements in this
     * collection
     */
    public java.util.Iterator<Definition> iterateDefinition() {
        return this._definitionList.iterator();
    }

    /**
     * 
     * 
     * @param out
     * @throws MarshalException if object is
     * null or if any SAXException is thrown during marshaling
     * @throws ValidationException if this
     * object is an invalid instance according to the schema
     */
        @Override
    public void marshal(final Writer out) throws MarshalException, ValidationException {
        Marshaller.marshal(this, out);
    }

    /**
     * 
     * 
     * @param handler
     * @throws IOException if an IOException occurs during
     * marshaling
     * @throws ValidationException if this
     * object is an invalid instance according to the schema
     * @throws MarshalException if object is
     * null or if any SAXException is thrown during marshaling
     */
        @Override
    public void marshal(final ContentHandler handler) throws IOException, MarshalException, ValidationException {
        Marshaller.marshal(this, handler);
    }

    /**
     */
    public void removeAllDefinition(
    ) {
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
        return this._definitionList.remove(index);
    }

    /**
     * 
     * 
     * @param index
     * @param vDefinition
     * @throws IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void setDefinition(final int index, final Definition vDefinition) throws IndexOutOfBoundsException {
        if (index < 0 || index >= this._definitionList.size()) {
            throw new IndexOutOfBoundsException("setDefinition: Index value '" + index + "' not in range [0.." + (this._definitionList.size() - 1) + "]");
        }
        
        this._definitionList.set(index, vDefinition);
    }

    /**
     * 
     * 
     * @param vDefinitionArray
     */
    public void setDefinition(final Definition[] vDefinitionArray) {
        _definitionList.clear();
        
        for (int i = 0; i < vDefinitionArray.length; i++) {
                this._definitionList.add(vDefinitionArray[i]);
        }
    }

    /**
     * Sets the value of '_definitionList' by copying the given
     * Vector. All elements will be checked for type safety.
     * 
     * @param vDefinitionList the Vector to copy.
     */
    public void setDefinition(final List<Definition> vDefinitionList) {
        this._definitionList.clear();
        
        this._definitionList.addAll(vDefinitionList);
    }

    /**
     * Sets the value of '_definitionList' by setting it to the
     * given Vector. No type checking is performed.
     * @deprecated
     * 
     * @param definitionList the Vector to set.
     */
    public void setDefinitionCollection(final List<Definition> definitionList) {
        this._definitionList = definitionList;
    }

    /**
     * Method unmarshal.
     * 
     * @param reader
     * @throws MarshalException if object is
     * null or if any SAXException is thrown during marshaling
     * @throws ValidationException if this
     * object is an invalid instance according to the schema
     * @return the unmarshaled
     * Configuration
     */
    public static Configuration unmarshal(final Reader reader) throws MarshalException, ValidationException {
        return (Configuration) Unmarshaller.unmarshal(org.opennms.netmgt.config.snmp.SnmpConfig.class, reader);
    }

    /**
     * 
     * 
     * @throws ValidationException if this
     * object is an invalid instance according to the schema
     */
        @Override
    public void validate() throws ValidationException {
        new Validator().validate(this);
    }

    @Override
    public String toString() {
    	return new ToStringBuilder(this)
    		.appendSuper(super.toString())
    		.append("definitions", getDefinitionCollection())
    		.toString();
    }
}

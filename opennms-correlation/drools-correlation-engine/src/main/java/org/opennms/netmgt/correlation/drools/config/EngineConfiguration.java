/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012-2014 The OpenNMS Group, Inc.
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

/*
 * This class was automatically generated with 
 * <a href="http://www.castor.org">Castor 1.1.2.1</a>, using an XML
 * Schema.
 * $Id$
 */

package org.opennms.netmgt.correlation.drools.config;

  //---------------------------------/
 //- Imported classes and packages -/
//---------------------------------/

import java.io.IOException;
import java.io.Reader;
import java.io.Serializable;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.Marshaller;
import org.exolab.castor.xml.Unmarshaller;
import org.exolab.castor.xml.ValidationException;
import org.exolab.castor.xml.Validator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.opennms.netmgt.correlation.CorrelationEngine;
import org.opennms.netmgt.events.api.EventIpcManager;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource;
import org.xml.sax.ContentHandler;

import com.codahale.metrics.MetricRegistry;


/**
 * The top-level element of the drools-engine.xml configuration
 *  file.
 * 
 * @version $Revision$ $Date$
 */

@SuppressWarnings("all") 
@XmlRootElement(name="engine-configuration")
@XmlAccessorType(XmlAccessType.FIELD)
public class EngineConfiguration implements Serializable {
    private static final Logger LOG = LoggerFactory.getLogger(EngineConfiguration.class);


      //--------------------------/
     //- Class/Member Variables -/
    //--------------------------/

	/**
     * Field _ruleSetList.
     */
	@XmlElement(name="rule-set")
    private List<RuleSet> _ruleSetList;


      //----------------/
     //- Constructors -/
    //----------------/

    public EngineConfiguration() {
        this._ruleSetList = new ArrayList<RuleSet>();
    }


      //-----------/
     //- Methods -/
    //-----------/

    /**
     * 
     * 
     * @param vRuleSet
     * @throws IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void addRuleSet(final RuleSet vRuleSet) throws IndexOutOfBoundsException {
        this._ruleSetList.add(vRuleSet);
    }

    /**
     * 
     * 
     * @param index
     * @param vRuleSet
     * @throws IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void addRuleSet(final int index, final RuleSet vRuleSet) throws IndexOutOfBoundsException {
        this._ruleSetList.add(index, vRuleSet);
    }

    /**
     * Method enumerateRuleSet.
     * 
     * @return an Enumeration over all possible elements of this
     * collection
     */
    public Enumeration<RuleSet> enumerateRuleSet() {
        return Collections.enumeration(this._ruleSetList);
    }

    /**
     * Method getRuleSet.
     * 
     * @param index
     * @throws IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     * @return the value of the
     * RuleSet at the
     * given index
     */
    public RuleSet getRuleSet(final int index) throws IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this._ruleSetList.size()) {
            throw new IndexOutOfBoundsException("getRuleSet: Index value '" + index + "' not in range [0.." + (this._ruleSetList.size() - 1) + "]");
        }
        
        return (RuleSet) _ruleSetList.get(index);
    }

    /**
     * Method getRuleSet.Returns the contents of the collection in
     * an Array.  <p>Note:  Just in case the collection contents
     * are changing in another thread, we pass a 0-length Array of
     * the correct type into the API call.  This way we <i>know</i>
     * that the Array returned is of exactly the correct length.
     * 
     * @return this collection as an Array
     */
    public RuleSet[] getRuleSet() {
        RuleSet[] array = new RuleSet[0];
        return (RuleSet[]) this._ruleSetList.toArray(array);
    }

    /**
     * Method getRuleSetCollection.Returns a reference to
     * '_ruleSetList'. No type checking is performed on any
     * modifications to the Vector.
     * 
     * @return a reference to the Vector backing this class
     */
    public List<RuleSet> getRuleSetCollection() {
        return this._ruleSetList;
    }

    /**
     * Method getRuleSetCount.
     * 
     * @return the size of this collection
     */
    public int getRuleSetCount() {
        return this._ruleSetList.size();
    }

    /**
     * Method isValid.
     * 
     * @return true if this object is valid according to the schema
     */
    public boolean isValid() {
        try {
            validate();
        } catch (ValidationException vex) {
            return false;
        }
        return true;
    }

    /**
     * Method iterateRuleSet.
     * 
     * @return an Iterator over all possible elements in this
     * collection
     */
    public Iterator<RuleSet> iterateRuleSet() {
        return this._ruleSetList.iterator();
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
    public void marshal(final ContentHandler handler) throws IOException, MarshalException, ValidationException {
        Marshaller.marshal(this, handler);
    }

    /**
     */
    public void removeAllRuleSet() {
        this._ruleSetList.clear();
    }

    /**
     * Method removeRuleSet.
     * 
     * @param vRuleSet
     * @return true if the object was removed from the collection.
     */
    public boolean removeRuleSet(final RuleSet vRuleSet) {
        return _ruleSetList.remove(vRuleSet);
    }

    /**
     * Method removeRuleSetAt.
     * 
     * @param index
     * @return the element removed from the collection
     */
    public RuleSet removeRuleSetAt(final int index) {
        return this._ruleSetList.remove(index);
    }

    /**
     * 
     * 
     * @param index
     * @param vRuleSet
     * @throws IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void setRuleSet(final int index, final RuleSet vRuleSet) throws IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this._ruleSetList.size()) {
            throw new IndexOutOfBoundsException("setRuleSet: Index value '" + index + "' not in range [0.." + (this._ruleSetList.size() - 1) + "]");
        }
        
        this._ruleSetList.set(index, vRuleSet);
    }

    /**
     * 
     * 
     * @param vRuleSetArray
     */
    public void setRuleSet(final RuleSet[] vRuleSetArray) {
    	this.setRuleSet(Arrays.asList(vRuleSetArray));
    }

    /**
     * Sets the value of '_ruleSetList' by copying the given
     * Vector. All elements will be checked for type safety.
     * 
     * @param vRuleSetList the Vector to copy.
     */
    public void setRuleSet(final List<RuleSet> vRuleSetList) {
        // copy vector
        this._ruleSetList.clear();
        
        this._ruleSetList.addAll(vRuleSetList);
    }

    /**
     * Sets the value of '_ruleSetList' by setting it to the given
     * Vector. No type checking is performed.
     * @deprecated
     * 
     * @param ruleSetList the Vector to set.
     */
    public void setRuleSetCollection(final List<RuleSet> ruleSetList) {
        this._ruleSetList = ruleSetList;
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
     * EngineConfiguration
     */
    public static EngineConfiguration unmarshal(final Reader reader) throws MarshalException, ValidationException {
        return (EngineConfiguration) Unmarshaller.unmarshal(EngineConfiguration.class, reader);
    }

    /**
     * 
     * 
     * @throws ValidationException if this
     * object is an invalid instance according to the schema
     */
    public void validate() throws ValidationException {
        Validator validator = new Validator();
        validator.validate(this);
    }

    @Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((_ruleSetList == null) ? 0 : _ruleSetList.hashCode());
		return result;
	}


	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		EngineConfiguration other = (EngineConfiguration) obj;
		if (_ruleSetList == null) {
			if (other._ruleSetList != null)
				return false;
		} else if (!_ruleSetList.equals(other._ruleSetList))
			return false;
		return true;
	}


	public CorrelationEngine[] constructEngines(Resource basePath, ApplicationContext appContext, EventIpcManager eventIpcManager, MetricRegistry metricRegistry) {
		
		LOG.info("Creating drools engins for configuration {}.", basePath);

		List<CorrelationEngine> engineList = new ArrayList<CorrelationEngine>();
		for (final RuleSet ruleSet : getRuleSet()) {
			LOG.debug("Constucting engind for ruleset {} in configuration {}.", ruleSet.getName(), basePath);
			engineList.add(ruleSet.constructEngine(basePath, appContext, eventIpcManager, metricRegistry));
	    }
	    
	    return engineList.toArray(new CorrelationEngine[0]);
	}

}

/**
 * *****************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012-2014 The OpenNMS Group, Inc. OpenNMS(R) is Copyright (C)
 * 1999-2014 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R). If not, see: http://www.gnu.org/licenses/
 *
 * For more information contact: OpenNMS(R) Licensing <license@opennms.org>
 * http://www.opennms.org/ http://www.opennms.com/
 ******************************************************************************
 */
package org.opennms.netmgt.correlation.drools.config;

import java.io.IOException;
import java.io.Reader;
import java.io.Serializable;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledThreadPoolExecutor;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.Marshaller;
import org.exolab.castor.xml.Unmarshaller;
import org.exolab.castor.xml.ValidationException;
import org.exolab.castor.xml.Validator;
import org.opennms.core.utils.PropertiesUtils;
import org.opennms.netmgt.correlation.CorrelationEngine;
import org.opennms.netmgt.correlation.drools.ConfigFileApplicationContext;
import org.opennms.netmgt.correlation.drools.DroolsCorrelationEngine;
import org.opennms.netmgt.events.api.EventIpcManager;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.xml.sax.ContentHandler;

import com.codahale.metrics.MetricRegistry;

/**
 * Class RuleSet.
 *
 * @version $Revision$ $Date$
 */
@SuppressWarnings("all")
@XmlRootElement(name = "rule-set")
@XmlAccessorType(XmlAccessType.FIELD)
public class RuleSet implements Serializable {

    @XmlAttribute(name = "name")
    private String _name;

    @XmlAttribute(name = "assert-behaviour")
    private String _assertBehaviour;

    @XmlAttribute(name = "event-processing-mode")
    private String _eventProcessingMode;
    
    @XmlElement(name = "rule-file")
    private List<String> _ruleFileList;

    @XmlElement(name = "event")
    private List<String> _eventList;

    @XmlElement(name = "app-context")
    private String _appContext;

    @XmlElement(name = "global")
    private List<Global> _globalList;

    public RuleSet() {
        super();
        this._ruleFileList = new ArrayList<>();
        this._eventList = new ArrayList<>();
        this._globalList = new ArrayList<>();
    }

    public String getAssertBehaviour() {
        return _assertBehaviour == null ? "identity" : _assertBehaviour;
    }
    
    public void setAssertBehaviour(String assertBehaviour) {
        this._assertBehaviour = assertBehaviour;
    }

    public String getEventProcessingMode() {
        return _eventProcessingMode == null ? "cloud" : _eventProcessingMode;
    }
    
    public void setEventProcessingMode(String eventProcessingMode) {
        this._eventProcessingMode = eventProcessingMode;
    }
    
    /**
     *
     *
     * @param vEvent
     * @throws IndexOutOfBoundsException if the index given is outside the
     * bounds of the collection
     */
    public void addEvent(
            final String vEvent)
            throws IndexOutOfBoundsException {
        this._eventList.add(vEvent);
    }

    /**
     *
     *
     * @param index
     * @param vEvent
     * @throws IndexOutOfBoundsException if the index given is outside the
     * bounds of the collection
     */
    public void addEvent(
            final int index,
            final String vEvent)
            throws IndexOutOfBoundsException {
        this._eventList.add(index, vEvent);
    }

    /**
     *
     *
     * @param vGlobal
     * @throws IndexOutOfBoundsException if the index given is outside the
     * bounds of the collection
     */
    public void addGlobal(
            final Global vGlobal)
            throws IndexOutOfBoundsException {
        this._globalList.add(vGlobal);
    }

    /**
     *
     *
     * @param index
     * @param vGlobal
     * @throws IndexOutOfBoundsException if the index given is outside the
     * bounds of the collection
     */
    public void addGlobal(
            final int index,
            final Global vGlobal)
            throws IndexOutOfBoundsException {
        this._globalList.add(index, vGlobal);
    }

    /**
     *
     *
     * @param vRuleFile
     * @throws IndexOutOfBoundsException if the index given is outside the
     * bounds of the collection
     */
    public void addRuleFile(
            final String vRuleFile)
            throws IndexOutOfBoundsException {
        this._ruleFileList.add(vRuleFile);
    }

    /**
     *
     *
     * @param index
     * @param vRuleFile
     * @throws IndexOutOfBoundsException if the index given is outside the
     * bounds of the collection
     */
    public void addRuleFile(
            final int index,
            final String vRuleFile)
            throws IndexOutOfBoundsException {
        this._ruleFileList.add(index, vRuleFile);
    }

    /**
     * Method enumerateEvent.
     *
     * @return an Enumeration over all possible elements of this collection
     */
    public Enumeration<String> enumerateEvent() {
        return Collections.enumeration(this._eventList);
    }

    /**
     * Method enumerateGlobal.
     *
     * @return an Enumeration over all possible elements of this collection
     */
    public Enumeration<Global> enumerateGlobal() {
        return Collections.enumeration(this._globalList);
    }

    /**
     * Method enumerateRuleFile.
     *
     * @return an Enumeration over all possible elements of this collection
     */
    public Enumeration<String> enumerateRuleFile() {
        return Collections.enumeration(this._ruleFileList);
    }

    /**
     * Returns the value of field 'appContext'.
     *
     * @return the value of field 'AppContext'.
     */
    public String getAppContext() {
        return this._appContext;
    }

    /**
     * Method getEvent.
     *
     * @param index
     * @throws IndexOutOfBoundsException if the index given is outside the
     * bounds of the collection
     * @return the value of the String at the given index
     */
    public String getEvent(
            final int index)
            throws IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this._eventList.size()) {
            throw new IndexOutOfBoundsException("getEvent: Index value '" + index + "' not in range [0.." + (this._eventList.size() - 1) + "]");
        }

        return (String) _eventList.get(index);
    }

    /**
     * Method getEvent.Returns the contents of the collection in an Array.
     * <p>
     * Note: Just in case the collection contents are changing in another
     * thread, we pass a 0-length Array of the correct type into the API call.
     * This way we <i>know</i>
     * that the Array returned is of exactly the correct length.
     *
     * @return this collection as an Array
     */
    public String[] getEvent() {
        String[] array = new String[0];
        return (String[]) this._eventList.toArray(array);
    }

    /**
     * Method getEventCollection.Returns a reference to '_eventList'. No type
     * checking is performed on any modifications to the Vector.
     *
     * @return a reference to the Vector backing this class
     */
    public List<String> getEventCollection() {
        return this._eventList;
    }

    /**
     * Method getEventCount.
     *
     * @return the size of this collection
     */
    public int getEventCount() {
        return this._eventList.size();
    }

    /**
     * Method getGlobal.
     *
     * @param index
     * @throws IndexOutOfBoundsException if the index given is outside the
     * bounds of the collection
     * @return the value of the Global at the given index
     */
    public Global getGlobal(
            final int index)
            throws IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this._globalList.size()) {
            throw new IndexOutOfBoundsException("getGlobal: Index value '" + index + "' not in range [0.." + (this._globalList.size() - 1) + "]");
        }

        return (Global) _globalList.get(index);
    }

    /**
     * Method getGlobal.Returns the contents of the collection in an Array.
     * <p>
     * Note: Just in case the collection contents are changing in another
     * thread, we pass a 0-length Array of the correct type into the API call.
     * This way we <i>know</i>
     * that the Array returned is of exactly the correct length.
     *
     * @return this collection as an Array
     */
    public Global[] getGlobal() {
        Global[] array = new Global[0];
        return (Global[]) this._globalList.toArray(array);
    }

    /**
     * Method getGlobalCollection.Returns a reference to '_globalList'. No type
     * checking is performed on any modifications to the Vector.
     *
     * @return a reference to the Vector backing this class
     */
    public List<Global> getGlobalCollection() {
        return this._globalList;
    }

    /**
     * Method getGlobalCount.
     *
     * @return the size of this collection
     */
    public int getGlobalCount() {
        return this._globalList.size();
    }

    /**
     * Returns the value of field 'name'.
     *
     * @return the value of field 'Name'.
     */
    public String getName() {
        return this._name;
    }

    /**
     * Method getRuleFile.
     *
     * @param index
     * @throws IndexOutOfBoundsException if the index given is outside the
     * bounds of the collection
     * @return the value of the String at the given index
     */
    public String getRuleFile(
            final int index)
            throws IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this._ruleFileList.size()) {
            throw new IndexOutOfBoundsException("getRuleFile: Index value '" + index + "' not in range [0.." + (this._ruleFileList.size() - 1) + "]");
        }

        return (String) _ruleFileList.get(index);
    }

    /**
     * Method getRuleFile.Returns the contents of the collection in an Array.
     * <p>
     * Note: Just in case the collection contents are changing in another
     * thread, we pass a 0-length Array of the correct type into the API call.
     * This way we <i>know</i>
     * that the Array returned is of exactly the correct length.
     *
     * @return this collection as an Array
     */
    public String[] getRuleFile() {
        String[] array = new String[0];
        return (String[]) this._ruleFileList.toArray(array);
    }

    /**
     * Method getRuleFileCollection.Returns a reference to '_ruleFileList'. No
     * type checking is performed on any modifications to the Vector.
     *
     * @return a reference to the Vector backing this class
     */
    public List<String> getRuleFileCollection() {
        return this._ruleFileList;
    }

    /**
     * Method getRuleFileCount.
     *
     * @return the size of this collection
     */
    public int getRuleFileCount() {
        return this._ruleFileList.size();
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
     * Method iterateEvent.
     *
     * @return an Iterator over all possible elements in this collection
     */
    public Iterator<String> iterateEvent() {
        return this._eventList.iterator();
    }

    /**
     * Method iterateGlobal.
     *
     * @return an Iterator over all possible elements in this collection
     */
    public Iterator<Global> iterateGlobal() {
        return this._globalList.iterator();
    }

    /**
     * Method iterateRuleFile.
     *
     * @return an Iterator over all possible elements in this collection
     */
    public Iterator<String> iterateRuleFile() {
        return this._ruleFileList.iterator();
    }

    /**
     *
     *
     * @param out
     * @throws MarshalException if object is null or if any SAXException is
     * thrown during marshaling
     * @throws ValidationException if this object is an invalid instance
     * according to the schema
     */
    public void marshal(
            final Writer out)
            throws MarshalException, ValidationException {
        Marshaller.marshal(this, out);
    }

    /**
     *
     *
     * @param handler
     * @throws IOException if an IOException occurs during marshaling
     * @throws ValidationException if this object is an invalid instance
     * according to the schema
     * @throws MarshalException if object is null or if any SAXException is
     * thrown during marshaling
     */
    public void marshal(
            final ContentHandler handler)
            throws IOException, MarshalException, ValidationException {
        Marshaller.marshal(this, handler);
    }

    /**
     */
    public void removeAllEvent() {
        this._eventList.clear();
    }

    /**
     */
    public void removeAllGlobal() {
        this._globalList.clear();
    }

    /**
     */
    public void removeAllRuleFile() {
        this._ruleFileList.clear();
    }

    /**
     * Method removeEvent.
     *
     * @param vEvent
     * @return true if the object was removed from the collection.
     */
    public boolean removeEvent(
            final String vEvent) {
        boolean removed = _eventList.remove(vEvent);
        return removed;
    }

    /**
     * Method removeEventAt.
     *
     * @param index
     * @return the element removed from the collection
     */
    public String removeEventAt(
            final int index) {
        Object obj = this._eventList.remove(index);
        return (String) obj;
    }

    /**
     * Method removeGlobal.
     *
     * @param vGlobal
     * @return true if the object was removed from the collection.
     */
    public boolean removeGlobal(
            final Global vGlobal) {
        boolean removed = _globalList.remove(vGlobal);
        return removed;
    }

    /**
     * Method removeGlobalAt.
     *
     * @param index
     * @return the element removed from the collection
     */
    public Global removeGlobalAt(
            final int index) {
        Object obj = this._globalList.remove(index);
        return (Global) obj;
    }

    /**
     * Method removeRuleFile.
     *
     * @param vRuleFile
     * @return true if the object was removed from the collection.
     */
    public boolean removeRuleFile(
            final String vRuleFile) {
        boolean removed = _ruleFileList.remove(vRuleFile);
        return removed;
    }

    /**
     * Method removeRuleFileAt.
     *
     * @param index
     * @return the element removed from the collection
     */
    public String removeRuleFileAt(
            final int index) {
        Object obj = this._ruleFileList.remove(index);
        return (String) obj;
    }

    /**
     * Sets the value of field 'appContext'.
     *
     * @param appContext the value of field 'appContext'.
     */
    public void setAppContext(
            final String appContext) {
        this._appContext = appContext;
    }

    /**
     *
     *
     * @param index
     * @param vEvent
     * @throws IndexOutOfBoundsException if the index given is outside the
     * bounds of the collection
     */
    public void setEvent(
            final int index,
            final String vEvent)
            throws IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this._eventList.size()) {
            throw new IndexOutOfBoundsException("setEvent: Index value '" + index + "' not in range [0.." + (this._eventList.size() - 1) + "]");
        }

        this._eventList.set(index, vEvent);
    }

    /**
     *
     *
     * @param vEventArray
     */
    public void setEvent(final String[] vEventArray) {
        _eventList.clear();
        this._eventList.addAll(Arrays.asList(vEventArray));
    }

    /**
     * Sets the value of '_eventList' by copying the given Vector. All elements
     * will be checked for type safety.
     *
     * @param vEventList the Vector to copy.
     */
    public void setEvent(final List<String> vEventList) {
        this._eventList.clear();
        this._eventList.addAll(vEventList);
    }

    /**
     * Sets the value of '_eventList' by setting it to the given Vector. No type
     * checking is performed.
     *
     * @deprecated
     *
     * @param eventList the Vector to set.
     */
    public void setEventCollection(final List<String> eventList) {
        this._eventList = eventList;
    }

    /**
     *
     *
     * @param index
     * @param vGlobal
     * @throws IndexOutOfBoundsException if the index given is outside the
     * bounds of the collection
     */
    public void setGlobal(final int index, final Global vGlobal)
            throws IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this._globalList.size()) {
            throw new IndexOutOfBoundsException("setGlobal: Index value '" + index + "' not in range [0.." + (this._globalList.size() - 1) + "]");
        }

        this._globalList.set(index, vGlobal);
    }

    /**
     *
     *
     * @param vGlobalArray
     */
    public void setGlobal(final Global[] vGlobalArray) {
        _globalList.clear();
        this._globalList.addAll(Arrays.asList(vGlobalArray));
    }

    /**
     * Sets the value of '_globalList' by copying the given Vector. All elements
     * will be checked for type safety.
     *
     * @param vGlobalList the Vector to copy.
     */
    public void setGlobal(final List<Global> vGlobalList) {
        this._globalList.clear();
        this._globalList.addAll(vGlobalList);
    }

    /**
     * Sets the value of '_globalList' by setting it to the given Vector. No
     * type checking is performed.
     *
     * @deprecated
     *
     * @param globalList the Vector to set.
     */
    public void setGlobalCollection(final List<Global> globalList) {
        this._globalList = globalList;
    }

    /**
     * Sets the value of field 'name'.
     *
     * @param name the value of field 'name'.
     */
    public void setName(final String name) {
        this._name = name;
    }

    /**
     *
     *
     * @param index
     * @param vRuleFile
     * @throws IndexOutOfBoundsException if the index given is outside the
     * bounds of the collection
     */
    public void setRuleFile(final int index, final String vRuleFile)
            throws IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this._ruleFileList.size()) {
            throw new IndexOutOfBoundsException("setRuleFile: Index value '" + index + "' not in range [0.." + (this._ruleFileList.size() - 1) + "]");
        }

        this._ruleFileList.set(index, vRuleFile);
    }

    /**
     *
     *
     * @param vRuleFileArray
     */
    public void setRuleFile(final String[] vRuleFileArray) {
        _ruleFileList.clear();
        this._ruleFileList.addAll(Arrays.asList(vRuleFileArray));
    }

    /**
     * Sets the value of '_ruleFileList' by copying the given Vector. All
     * elements will be checked for type safety.
     *
     * @param vRuleFileList the Vector to copy.
     */
    public void setRuleFile(final List<String> vRuleFileList) {
        this._ruleFileList.clear();
        this._ruleFileList.addAll(vRuleFileList);
    }

    /**
     * Sets the value of '_ruleFileList' by setting it to the given Vector. No
     * type checking is performed.
     *
     * @deprecated
     *
     * @param ruleFileList the Vector to set.
     */
    public void setRuleFileCollection(final List<String> ruleFileList) {
        this._ruleFileList = ruleFileList;
    }

    /**
     * Method unmarshal.
     *
     * @param reader
     * @throws MarshalException if object is null or if any SAXException is
     * thrown during marshaling
     * @throws ValidationException if this object is an invalid instance
     * according to the schema
     * @return the unmarshaled RuleSet
     */
    public static RuleSet unmarshal(final Reader reader) throws MarshalException, ValidationException {
        return (RuleSet) Unmarshaller.unmarshal(RuleSet.class, reader);
    }

    /**
     *
     *
     * @throws ValidationException if this object is an invalid instance
     * according to the schema
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
                + ((_appContext == null) ? 0 : _appContext.hashCode());
        result = prime * result
                + ((_eventList == null) ? 0 : _eventList.hashCode());
        result = prime * result
                + ((_globalList == null) ? 0 : _globalList.hashCode());
        result = prime * result + ((_name == null) ? 0 : _name.hashCode());
        result = prime * result
                + ((_ruleFileList == null) ? 0 : _ruleFileList.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        RuleSet other = (RuleSet) obj;
        if (_appContext == null) {
            if (other._appContext != null) {
                return false;
            }
        } else if (!_appContext.equals(other._appContext)) {
            return false;
        }
        if (_eventList == null) {
            if (other._eventList != null) {
                return false;
            }
        } else if (!_eventList.equals(other._eventList)) {
            return false;
        }
        if (_globalList == null) {
            if (other._globalList != null) {
                return false;
            }
        } else if (!_globalList.equals(other._globalList)) {
            return false;
        }
        if (_name == null) {
            if (other._name != null) {
                return false;
            }
        } else if (!_name.equals(other._name)) {
            return false;
        }
        if (_ruleFileList == null) {
            if (other._ruleFileList != null) {
                return false;
            }
        } else if (!_ruleFileList.equals(other._ruleFileList)) {
            return false;
        }
        return true;
    }

    public CorrelationEngine constructEngine(Resource basePath, ApplicationContext appContext, EventIpcManager eventIpcManager, MetricRegistry metricRegistry) {
        final ApplicationContext configContext = new ConfigFileApplicationContext(basePath, getConfigLocation(), appContext);

        final DroolsCorrelationEngine engine = new DroolsCorrelationEngine(getName(), metricRegistry);
        engine.setAssertBehaviour(getAssertBehaviour());
        engine.setEventProcessingMode(getEventProcessingMode());
        engine.setEventIpcManager(eventIpcManager);
        engine.setScheduler(new ScheduledThreadPoolExecutor(1));
        engine.setInterestingEvents(getInterestingEvents());
        engine.setRulesResources(getRuleResources(configContext));
        engine.setGlobals(getGlobals(configContext));
        try {
            engine.initialize();
            return engine;
        } catch (final Throwable e) {
            throw new RuntimeException("Unable to initialize Drools engine " + getName(), e);
        }
    }

    public Map<String, Object> getGlobals(final ApplicationContext context) {
        final Map<String, Object> globals = new HashMap<>();

        for (final Global global : getGlobal()) {
            globals.put(global.getName(), global.constructValue(context));
        }

        return globals;
    }

    public List<String> getInterestingEvents() {
        return Arrays.asList(getEvent());
    }

    public Resource getResource(final ResourceLoader resourceLoader, final String resourcePath) {
        return resourceLoader.getResource(PropertiesUtils.substitute(resourcePath, System.getProperties()));
    }

    public List<Resource> getRuleResources(final ResourceLoader resourceLoader) {
        final List<Resource> resources = new LinkedList<>();
        for (final String resourcePath : getRuleFile()) {
            resources.add(getResource(resourceLoader, resourcePath));
        }

        return resources;
    }

    public String getConfigLocation() {
        return PropertiesUtils.substitute(getAppContext(), System.getProperties());
    }

}

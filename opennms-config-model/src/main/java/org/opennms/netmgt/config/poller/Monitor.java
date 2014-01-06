/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2013 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2013 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.config.poller;

import java.io.IOException;
import java.io.Reader;
import java.io.Serializable;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;

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
import org.xml.sax.ContentHandler;

/**
 * Monitor for a service
 */

@XmlRootElement(name="monitor")
@XmlAccessorType(XmlAccessType.FIELD)
public class Monitor implements Serializable {
    private static final long serialVersionUID = 8732234575300944324L;

    private static final Parameter[] EMPTY_PARAMETER_LIST = new Parameter[0];

    /**
     * Service name
     */
    @XmlAttribute(name="service")
    private String m_service;

    /**
     * Java cass used to monitor/poll the service. The class
     *  must implement the
     * org.opennms.netmgt.poller.monitors.ServiceMonitor
     *  interface.
     */
    @XmlAttribute(name="class-name")
    private String m_className;

    /**
     * Parameters to be used for polling this service. E.g.: for
     *  polling HTTP, the URL to hit is configurable via a
     * parameter. Parameters
     *  are specfic to the service monitor.
     */
    @XmlElement(name="parameter")
    private List<Parameter> m_parameters = new ArrayList<Parameter>();


      //----------------/
     //- Constructors -/
    //----------------/

    public Monitor() {
        super();
    }

    public Monitor(final String service, final String className) {
        this();
        setService(service);
        setClassName(className);
    }



      //-----------/
     //- Methods -/
    //-----------/

    /**
     * 
     * 
     * @param parameter
     * @throws IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void addParameter(final Parameter parameter) throws IndexOutOfBoundsException {
        m_parameters.add(parameter);
    }

    /**
     * 
     * 
     * @param index
     * @param parameter
     * @throws IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void addParameter(final int index, final Parameter parameter) throws IndexOutOfBoundsException {
        m_parameters.add(index, parameter);
    }

    /**
     * Method enumerateParameter.
     * 
     * @return an Enumeration over all possible elements of this
     * collection
     */
    public Enumeration<Parameter> enumerateParameter() {
        return Collections.enumeration(m_parameters);
    }

    /**
     * Overrides the Object.equals method.
     * 
     * @param obj
     * @return true if the objects are equal.
     */
    @Override()
    public boolean equals(final Object obj) {
        if ( this == obj ) return true;
        
        if (obj instanceof Monitor) {
            final Monitor temp = (Monitor)obj;
            if (m_service != null) {
                if (temp.m_service == null) {
                    return false;
                } else if (!(m_service.equals(temp.m_service))) {
                    return false;
                }
            } else if (temp.m_service != null) {
                return false;
            }
            if (m_className != null) {
                if (temp.m_className == null) {
                    return false;
                } else if (!(m_className.equals(temp.m_className))) {
                    return false;
                }
            } else if (temp.m_className != null) {
                return false;
            }
            if (m_parameters != null) {
                if (temp.m_parameters == null) {
                    return false;
                } else if (!(m_parameters.equals(temp.m_parameters))) {
                    return false;
                }
            } else if (temp.m_parameters != null) {
                return false;
            }
            return true;
        }
        return false;
    }

    /**
     * Returns the value of field 'className'. The field
     * 'className' has the following description: Java cass used to
     * monitor/poll the service. The class
     *  must implement the
     * org.opennms.netmgt.poller.monitors.ServiceMonitor
     *  interface.
     * 
     * @return the value of field 'ClassName'.
     */
    public String getClassName() {
        return m_className;
    }

    /**
     * Method getParameter.
     * 
     * @param index
     * @throws IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     * @return the value of the
     * Parameter at the given index
     */
    public Parameter getParameter(final int index) throws IndexOutOfBoundsException {
        return m_parameters.get(index);
    }

    /**
     * Method getParameter.Returns the contents of the collection
     * in an Array.  <p>Note:  Just in case the collection contents
     * are changing in another thread, we pass a 0-length Array of
     * the correct type into the API call.  This way we <i>know</i>
     * that the Array returned is of exactly the correct length.
     * 
     * @return this collection as an Array
     */
    public Parameter[] getParameter() {
        return m_parameters.toArray(EMPTY_PARAMETER_LIST);
    }

    /**
     * Method getParameterCollection.Returns a reference to
     * 'm_parameters'. No type checking is performed on any
     * modifications to the Vector.
     * 
     * @return a reference to the Vector backing this class
     */
    public List<Parameter> getParameterCollection() {
        return new ArrayList<Parameter>(m_parameters);
    }

    /**
     * Method getParameterCount.
     * 
     * @return the size of this collection
     */
    public int getParameterCount() {
        return m_parameters.size();
    }

    /**
     * Returns the value of field 'service'. The field 'service'
     * has the following description: Service name
     * 
     * @return the value of field 'Service'.
     */
    public String getService() {
        return m_service;
    }

    /**
     * Overrides the Object.hashCode method.
     * <p>
     * The following steps came from <b>Effective Java Programming
     * Language Guide</b> by Joshua Bloch, Chapter 3
     * 
     * @return a hash code value for the object.
     */
    public int hashCode() {
        int result = 17;
        
        if (m_service != null) {
           result = 37 * result + m_service.hashCode();
        }
        if (m_className != null) {
           result = 37 * result + m_className.hashCode();
        }
        if (m_parameters != null) {
           result = 37 * result + m_parameters.hashCode();
        }
        
        return result;
    }

    /**
     * Method isValid.
     * 
     * @return true if this object is valid according to the schema
     */
    public boolean isValid() {
        try {
            validate();
        } catch (final ValidationException vex) {
            return false;
        }
        return true;
    }

    /**
     * Method iterateParameter.
     * 
     * @return an Iterator over all possible elements in this
     * collection
     */
    public Iterator<Parameter> iterateParameter() {
        return m_parameters.iterator();
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
    public void removeAllParameter() {
        m_parameters.clear();
    }

    /**
     * Method removeParameter.
     * 
     * @param parameter
     * @return true if the object was removed from the collection.
     */
    public boolean removeParameter(final Parameter parameter) {
        return m_parameters.remove(parameter);
    }

    /**
     * Method removeParameterAt.
     * 
     * @param index
     * @return the element removed from the collection
     */
    public Parameter removeParameterAt(final int index) {
        return m_parameters.remove(index);
    }

    /**
     * Sets the value of field 'className'. The field 'className'
     * has the following description: Java cass used to
     * monitor/poll the service. The class
     *  must implement the
     * org.opennms.netmgt.poller.monitors.ServiceMonitor
     *  interface.
     * 
     * @param className the value of field 'className'.
     */
    public void setClassName(final String className) {
        m_className = className;
    }

    /**
     * 
     * 
     * @param index
     * @param parameter
     * @throws IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void setParameter(final int index, final Parameter parameter) throws IndexOutOfBoundsException {
        m_parameters.set(index, parameter);
    }

    /**
     * 
     * 
     * @param parameters
     */
    public void setParameter(final Parameter[] parameters) {
        m_parameters.clear();
        for (final Parameter parameter : parameters) {
            m_parameters.add(parameter);
        }
    }

    /**
     * Sets the value of 'm_parameters' by copying the given
     * Vector. All elements will be checked for type safety.
     * 
     * @param parameters the Vector to copy.
     */
    public void setParameter(final List<Parameter> parameters) {
        if (parameters != m_parameters) {
            m_parameters.clear();
            m_parameters.addAll(parameters);
        }
    }

    /**
     * Sets the value of 'm_parameters' by setting it to the
     * given Vector. No type checking is performed.
     * @deprecated
     * 
     * @param parameters the Vector to set.
     */
    public void setParameterCollection(final List<Parameter> parameters) {
        m_parameters = new ArrayList<Parameter>(parameters);
    }

    /**
     * Sets the value of field 'service'. The field 'service' has
     * the following description: Service name
     * 
     * @param service the value of field 'service'.
     */
    public void setService(final String service) {
        m_service = service;
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
     * Monitor
     */
    public static Monitor unmarshal(final Reader reader) throws MarshalException, ValidationException {
        return (Monitor) Unmarshaller.unmarshal(Monitor.class, reader);
    }

    /**
     * 
     * 
     * @throws ValidationException if this
     * object is an invalid instance according to the schema
     */
    public void validate() throws ValidationException {
        new Validator().validate(this);
    }

    @Override
    public String toString() {
        return "Monitor[service=" + m_service + ",className=" + m_className + ",parameters=" + m_parameters + "]";
    }
}

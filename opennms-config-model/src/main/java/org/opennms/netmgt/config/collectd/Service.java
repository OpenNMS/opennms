/*
 * This class was automatically generated with 
 * <a href="http://www.castor.org">Castor 1.1.2.1</a>, using an XML
 * Schema.
 * $Id$
 */

package org.opennms.netmgt.config.collectd;

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
 * Service to be collected for addresses in this
 *  package
 */

@XmlRootElement(name="service")
@XmlAccessorType(XmlAccessType.FIELD)
public class Service implements Serializable {
    private static final long serialVersionUID = 5108483987658881166L;

    private static final Parameter[] EMPTY_PARAMETER_LIST = new Parameter[0];

    /**
     * the service name
     */
    @XmlAttribute(name="name")
    private String m_name;

    /**
     * the interval at which the service is to be
     *  collected
     */
    @XmlAttribute(name="interval")
    private Long m_interval;

    /**
     * marker to say if service is user defined, used
     *  specifically for UI purposes
     */
    @XmlAttribute(name="user-defined")
    private String m_userDefined;

    /**
     * status of the service, service is collected only if
     *  on
     */
    @XmlAttribute(name="status")
    private String m_status;

    /**
     * Parameters to be used for collecting data via this service.
     *  "collection": name of data collection in
     * datacollection-config.xml ("SNMP" specific);
     *  SNMP parameters ("read-community", "version", etc) will
     * override defaults set in snmp-config.xml
     */
    @XmlElement(name="parameter")
    private List<Parameter> m_parameters = new ArrayList<Parameter>();

    public Service() {
        super();
    }

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

    public void addParameter(final String key, final String value) {
        m_parameters.add(new Parameter(key, value));
    }

    /**
     */
    public void deleteInterval() {
        m_interval = null;
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
     * Returns the value of field 'interval'. The field 'interval'
     * has the following description: the interval at which the
     * service is to be
     *  collected
     * 
     * @return the value of field 'Interval'.
     */
    public Long getInterval() {
        return m_interval == null? 0 : m_interval;
    }

    /**
     * Returns the value of field 'name'. The field 'name' has the
     * following description: the service name
     * 
     * @return the value of field 'Name'.
     */
    public String getName() {
        return m_name;
    }

    /**
     * Method getParameter.
     * 
     * @param index
     * @throws IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     * @return the value of the
     * Parameter at the given
     * index
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
     * Returns the value of field 'status'. The field 'status' has
     * the following description: status of the service, service is
     * collected only if
     *  on
     * 
     * @return the value of field 'Status'.
     */
    public String getStatus() {
        return m_status;
    }

    /**
     * Returns the value of field 'userDefined'. The field
     * 'userDefined' has the following description: marker to say
     * if service is user defined, used
     *  specifically for UI purposes
     * 
     * @return the value of field 'UserDefined'.
     */
    public String getUserDefined() {
        return m_userDefined;
    }

    /**
     * Method hasInterval.
     * 
     * @return true if at least one Interval has been added
     */
    public boolean hasInterval() {
        return m_interval != null;
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
     * Sets the value of field 'interval'. The field 'interval' has
     * the following description: the interval at which the service
     * is to be
     *  collected
     * 
     * @param interval the value of field 'interval'.
     */
    public void setInterval(final Long interval) {
        m_interval = interval;
    }

    /**
     * Sets the value of field 'name'. The field 'name' has the
     * following description: the service name
     * 
     * @param name the value of field 'name'.
     */
    public void setName(final String name) {
        m_name = name;
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
     * Sets the value of field 'status'. The field 'status' has the
     * following description: status of the service, service is
     * collected only if
     *  on
     * 
     * @param status the value of field 'status'.
     */
    public void setStatus(final String status) {
        m_status = status;
    }

    /**
     * Sets the value of field 'userDefined'. The field
     * 'userDefined' has the following description: marker to say
     * if service is user defined, used
     *  specifically for UI purposes
     * 
     * @param userDefined the value of field 'userDefined'.
     */
    public void setUserDefined(final String userDefined) {
        m_userDefined = userDefined;
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
     * Service
     */
    public static Service unmarshal(final Reader reader) throws MarshalException, ValidationException {
        return (Service) Unmarshaller.unmarshal(Service.class, reader);
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
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((m_interval == null) ? 0 : m_interval.hashCode());
        result = prime * result + ((m_name == null) ? 0 : m_name.hashCode());
        result = prime * result + ((m_parameters == null) ? 0 : m_parameters.hashCode());
        result = prime * result + ((m_status == null) ? 0 : m_status.hashCode());
        result = prime * result + ((m_userDefined == null) ? 0 : m_userDefined.hashCode());
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof Service)) {
            return false;
        }
        final Service other = (Service) obj;
        if (m_interval == null) {
            if (other.m_interval != null) {
                return false;
            }
        } else if (!m_interval.equals(other.m_interval)) {
            return false;
        }
        if (m_name == null) {
            if (other.m_name != null) {
                return false;
            }
        } else if (!m_name.equals(other.m_name)) {
            return false;
        }
        if (m_parameters == null) {
            if (other.m_parameters != null) {
                return false;
            }
        } else if (!m_parameters.equals(other.m_parameters)) {
            return false;
        }
        if (m_status == null) {
            if (other.m_status != null) {
                return false;
            }
        } else if (!m_status.equals(other.m_status)) {
            return false;
        }
        if (m_userDefined == null) {
            if (other.m_userDefined != null) {
                return false;
            }
        } else if (!m_userDefined.equals(other.m_userDefined)) {
            return false;
        }
        return true;
    }

}

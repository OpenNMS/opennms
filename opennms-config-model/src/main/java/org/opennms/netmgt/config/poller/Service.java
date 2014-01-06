/*
 * This class was automatically generated with 
 * <a href="http://www.castor.org">Castor 1.1.2.1</a>, using an XML
 * Schema.
 * $Id$
 */

package org.opennms.netmgt.config.poller;

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
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.Marshaller;
import org.exolab.castor.xml.Unmarshaller;
import org.exolab.castor.xml.ValidationException;
import org.exolab.castor.xml.Validator;
import org.xml.sax.ContentHandler;

/**
 * Service to be polled for addresses in this
 *  package.
 */

@XmlRootElement(name="service")
@XmlAccessorType(XmlAccessType.FIELD)
public class Service implements Serializable {
    private static final long serialVersionUID = -3255960558817703784L;

    private static final Parameter[] EMPTY_PARAMETER_LIST = new Parameter[0];

    /**
     * Service name
     */
    @XmlAttribute(name="name")
    private String m_name;

    /**
     * Interval at which the service is to be
     *  polled
     */
    @XmlAttribute(name="interval")
    private Long m_interval;

    /**
     * Specifies if the service is user defined. Used
     *  specifically for UI purposes.
     */
    @XmlAttribute(name="user-defined")
    private String m_userDefined = "false";

    /**
     * Status of the service. The service is polled only if
     *  this is set to 'on'.
     */
    @XmlAttribute(name="status")
    private String m_status = "on";

    /**
     * Parameters to be used for polling this service. E.g.: for
     *  polling HTTP, the URL to hit is configurable via a
     * parameter. Parameters
     *  are specfic to the service monitor.
     */
    @XmlElement(name="parameter")
    private List<Parameter> m_parameters = new ArrayList<Parameter>();


    public Service() {
        super();
        setUserDefined("false");
        setStatus("on");
    }

    public Service(final String name, final long interval, final String userDefined, final String status, final String... parameters) {
        this();
        setName(name);
        setInterval(interval);
        setUserDefined(userDefined);
        setStatus(status);
        
        if (parameters != null && parameters.length > 0) {
            final List<String> params = Arrays.asList(parameters);
            final Iterator<String> paramIterator = params.iterator();
            while (paramIterator.hasNext()) {
                final String key = paramIterator.next();
                if (!paramIterator.hasNext()) {
                    throw new IllegalArgumentException("Odd number of key/value pairs passed to new Service()!");
                }
                final String value = paramIterator.next();
                addParameter(key, value);
            }
        }
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
        final Parameter parameter = new Parameter();
        parameter.setKey(key);
        parameter.setValue(value);
        addParameter(parameter);
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
     * Overrides the Object.equals method.
     * 
     * @param obj
     * @return true if the objects are equal.
     */
    @Override()
    public boolean equals(final Object obj) {
        if ( this == obj ) return true;
        
        if (obj instanceof Service) {
            final Service temp = (Service)obj;
            if (m_name != null) {
                if (temp.m_name == null) {
                    return false;
                } else if (!(m_name.equals(temp.m_name))) {
                    return false;
                }
            } else if (temp.m_name != null) {
                return false;
            }
            if (m_interval != null) {
                if (temp.m_interval == null) {
                    return false;
                } else if (!(m_interval.equals(temp.m_interval))) {
                    return false;
                }
            } else if (m_interval != null) {
                return false;
            }
            if (m_userDefined != null) {
                if (temp.m_userDefined == null) {
                    return false;
                } else if (!(m_userDefined.equals(temp.m_userDefined))) {
                    return false;
                }
            } else if (temp.m_userDefined != null) {
                return false;
            }
            if (m_status != null) {
                if (temp.m_status == null) {
                    return false;
                } else if (!(m_status.equals(temp.m_status))) {
                    return false;
                }
            } else if (temp.m_status != null) {
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
     * Returns the value of field 'interval'. The field 'interval'
     * has the following description: Interval at which the service
     * is to be
     *  polled
     * 
     * @return the value of field 'Interval'.
     */
    public Long getInterval() {
        return m_interval == null? 0 : m_interval;
    }

    /**
     * Returns the value of field 'name'. The field 'name' has the
     * following description: Service name
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
     * Returns the value of field 'status'. The field 'status' has
     * the following description: Status of the service. The
     * service is polled only if
     *  this is set to 'on'.
     * 
     * @return the value of field 'Status'.
     */
    public String getStatus() {
        return m_status == null? "on" : m_status;
    }

    /**
     * Returns the value of field 'userDefined'. The field
     * 'userDefined' has the following description: Specifies if
     * the service is user defined. Used
     *  specifically for UI purposes.
     * 
     * @return the value of field 'UserDefined'.
     */
    public String getUserDefined() {
        return m_userDefined == null? "false" : m_userDefined;
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
     * Overrides the Object.hashCode method.
     * <p>
     * The following steps came from <b>Effective Java Programming
     * Language Guide</b> by Joshua Bloch, Chapter 3
     * 
     * @return a hash code value for the object.
     */
    public int hashCode(
    ) {
        int result = 17;
        
        if (m_name != null) {
           result = 37 * result + m_name.hashCode();
        }
        if (m_interval != null) {
            result = 37 * result + m_interval.hashCode();
        }
        if (m_userDefined != null) {
           result = 37 * result + m_userDefined.hashCode();
        }
        if (m_status != null) {
           result = 37 * result + m_status.hashCode();
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
     * Sets the value of field 'interval'. The field 'interval' has
     * the following description: Interval at which the service is
     * to be
     *  polled
     * 
     * @param interval the value of field 'interval'.
     */
    public void setInterval(final Long interval) {
        m_interval = interval;
    }

    @XmlTransient
    public void setInterval(final Integer interval) {
        m_interval = interval == null? null : interval.longValue();
    }

    /**
     * Sets the value of field 'name'. The field 'name' has the
     * following description: Service name
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
     * following description: Status of the service. The service is
     * polled only if
     *  this is set to 'on'.
     * 
     * @param status the value of field 'status'.
     */
    public void setStatus(final String status) {
        m_status = status;
    }

    /**
     * Sets the value of field 'userDefined'. The field
     * 'userDefined' has the following description: Specifies if
     * the service is user defined. Used
     *  specifically for UI purposes.
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
    public String toString() {
        return "Service[name=" + m_name +
                ",interval=" + m_interval +
                ",userDefined=" + m_userDefined +
                ",status=" + m_status +
                ",parameters=" + m_parameters +
                "]";
    }
}

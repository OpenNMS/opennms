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
 * RRD parameters
 */

@XmlRootElement(name="rrd")
@XmlAccessorType(XmlAccessType.FIELD)
public class Rrd implements Serializable {
    private static final long serialVersionUID = 7051996578582311328L;

    private static final String[] EMPTY_STRING_LIST = new String[0];

    /**
     * Step size for the RRD, in seconds.
     */
    @XmlAttribute(name="step")
    private Integer m_step;

    /**
     * Round Robin Archive definitions
     */
    @XmlElement(name="rra")
    private List<String> m_rras = new ArrayList<String>();

    public Rrd() {
        super();
    }

    public Rrd(int step, final String... rras) {
        this();
        setStep(step);
        if (rras != null) {
            for (final String rra : rras) {
                addRra(rra);
            }
        }
    }


    /**
     * 
     * 
     * @param rra
     * @throws IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void addRra(final String rra) throws IndexOutOfBoundsException {
        m_rras.add(rra);
    }

    /**
     * 
     * 
     * @param index
     * @param rra
     * @throws IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void addRra(final int index, final String rra) throws IndexOutOfBoundsException {
        m_rras.add(index, rra);
    }

    /**
     */
    public void deleteStep() {
        m_step = null;
    }

    /**
     * Method enumerateRra.
     * 
     * @return an Enumeration over all possible elements of this
     * collection
     */
    public Enumeration<String> enumerateRra() {
        return Collections.enumeration(m_rras);
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
        
        if (obj instanceof Rrd) {
            final Rrd temp = (Rrd)obj;
            if (m_step != null) {
                if (temp.m_step == null) {
                    return false;
                } else if (!(m_step.equals(temp.m_step))) {
                    return false;
                }
            } else if (temp.m_step != null) {
                return false;
            }
            if (m_rras != null) {
                if (temp.m_rras == null) {
                    return false;
                } else if (!(m_rras.equals(temp.m_rras))) {
                    return false;
                }
            } else if (temp.m_rras != null) {
                return false;
            }
            return true;
        }
        return false;
    }

    /**
     * Method getRra.
     * 
     * @param index
     * @throws IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     * @return the value of the String at the given index
     */
    public String getRra(final int index) throws IndexOutOfBoundsException {
        return m_rras.get(index);
    }

    /**
     * Method getRra.Returns the contents of the collection in an
     * Array.  <p>Note:  Just in case the collection contents are
     * changing in another thread, we pass a 0-length Array of the
     * correct type into the API call.  This way we <i>know</i>
     * that the Array returned is of exactly the correct length.
     * 
     * @return this collection as an Array
     */
    public String[] getRra() {
        return m_rras.toArray(EMPTY_STRING_LIST);
    }

    /**
     * Method getRraCollection.Returns a reference to 'm_rras'.
     * No type checking is performed on any modifications to the
     * Vector.
     * 
     * @return a reference to the Vector backing this class
     */
    public List<String> getRraCollection() {
        return new ArrayList<String>(m_rras);
    }

    /**
     * Method getRraCount.
     * 
     * @return the size of this collection
     */
    public int getRraCount() {
        return m_rras.size();
    }

    /**
     * Returns the value of field 'step'. The field 'step' has the
     * following description: Step size for the RRD, in seconds.
     * 
     * @return the value of field 'Step'.
     */
    public Integer getStep() {
        return m_step == null? 0 : m_step;
    }

    /**
     * Method hasStep.
     * 
     * @return true if at least one Step has been added
     */
    public boolean hasStep() {
        return m_step != null;
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
        
        if (m_step != null) {
            result = 37 * result + m_step.hashCode();
        }
        if (m_rras != null) {
           result = 37 * result + m_rras.hashCode();
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
     * Method iterateRra.
     * 
     * @return an Iterator over all possible elements in this
     * collection
     */
    public Iterator<String> iterateRra() {
        return m_rras.iterator();
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
    public void removeAllRra() {
        m_rras.clear();
    }

    /**
     * Method removeRra.
     * 
     * @param rra
     * @return true if the object was removed from the collection.
     */
    public boolean removeRra(final String rra) {
        return m_rras.remove(rra);
    }

    /**
     * Method removeRraAt.
     * 
     * @param index
     * @return the element removed from the collection
     */
    public String removeRraAt(final int index) {
        return m_rras.remove(index);
    }

    /**
     * 
     * 
     * @param index
     * @param rra
     * @throws IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void setRra(final int index, final String rra) throws IndexOutOfBoundsException {
        m_rras.set(index, rra);
    }

    /**
     * 
     * 
     * @param rras
     */
    public void setRra(final String[] rras) {
        m_rras.clear();
        for (final String rra : rras) {
            m_rras.add(rra);
        }
    }

    /**
     * Sets the value of 'm_rras' by copying the given Vector.
     * All elements will be checked for type safety.
     * 
     * @param rras the Vector to copy.
     */
    public void setRra(final List<String> rras) {
        if (rras != m_rras) {
            m_rras.clear();
            m_rras.addAll(rras);
        }
    }

    /**
     * Sets the value of 'm_rras' by setting it to the given
     * Vector. No type checking is performed.
     * @deprecated
     * 
     * @param rras the Vector to set.
     */
    public void setRraCollection(final List<String> rras) {
        m_rras = new ArrayList<String>(rras);
    }

    /**
     * Sets the value of field 'step'. The field 'step' has the
     * following description: Step size for the RRD, in seconds.
     * 
     * @param step the value of field 'step'.
     */
    public void setStep(final Integer step) {
        m_step = step;
    }

    /**
     * Method unmarshal.
     * 
     * @param reader
     * @throws MarshalException if object is
     * null or if any SAXException is thrown during marshaling
     * @throws ValidationException if this
     * object is an invalid instance according to the schema
     * @return the unmarshaled Rrd
     */
    public static Rrd unmarshal(final Reader reader) throws MarshalException, ValidationException {
        return (Rrd) Unmarshaller.unmarshal(Rrd.class, reader);
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
        return "Rrd[step=" + m_step + ",rras=" + m_rras + "]";
    }
}

/*
 * This class was automatically generated with 
 * <a href="http://www.castor.org">Castor 1.1.2.1</a>, using an XML
 * Schema.
 * $Id$
 */

package org.opennms.netmgt.config.pagesequence;

  //---------------------------------/
 //- Imported classes and packages -/
//---------------------------------/

import java.io.IOException;
import java.io.Reader;
import java.io.Serializable;
import java.io.Writer;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.Marshaller;
import org.exolab.castor.xml.Unmarshaller;
import org.exolab.castor.xml.ValidationException;
import org.exolab.castor.xml.Validator;

/**
 * Assign the value of a regex match group to a
 *  session variable with a user-defined name. The
 *  match group is identified by number and must
 *  be zero or greater.
 */

@XmlRootElement(name="session-variable")
@XmlAccessorType(XmlAccessType.FIELD)
public class SessionVariable implements Serializable {
    private static final long serialVersionUID = 5173622778411856405L;

    /**
     * Field m_matchGroup.
     */
    private Integer m_matchGroup;

    /**
     * Field m_name.
     */
    private String m_name;

    public SessionVariable() {
        super();
    }

    public void deleteMatchGroup() {
        m_matchGroup = null;
    }

    /**
     * Overrides the Object.equals method.
     * 
     * @param obj
     * @return true if the objects are equal.
     */
    @Override()
    public boolean equals(
            final Object obj) {
        if ( this == obj )
            return true;
        
        if (obj instanceof SessionVariable) {
        
            SessionVariable temp = (SessionVariable)obj;
            if (m_matchGroup != null) {
                if (temp.m_matchGroup == null) {
                    return false;
                } else if (!(m_matchGroup.equals(temp.m_matchGroup))) {
                    return false;
                }
            } else if (temp.m_matchGroup != null) {
                return false;
            }
            if (m_name != null) {
                if (temp.m_name == null) {
                    return false;
                } else if (!(m_name.equals(temp.m_name))) {
                    return false;
                }
            } else if (temp.m_name != null) {
                return false;
            }
            return true;
        }
        return false;
    }

    /**
     * Returns the value of field 'matchGroup'.
     * 
     * @return the value of field 'MatchGroup'.
     */
    public Integer getMatchGroup() {
        return m_matchGroup == null? 0 : m_matchGroup;
    }

    /**
     * Returns the value of field 'name'.
     * 
     * @return the value of field 'Name'.
     */
    public String getName() {
        return m_name;
    }

    /**
     * Method hasMatchGroup.
     * 
     * @return true if at least one MatchGroup has been added
     */
    public boolean hasMatchGroup() {
        return m_matchGroup != null;
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
        
        if (m_matchGroup != null) {
            result = 37 * result + m_matchGroup.hashCode();
         }
        if (m_name != null) {
           result = 37 * result + m_name.hashCode();
        }
        
        return result;
    }

    /**
     * Method isValid.
     * 
     * @return true if this object is valid according to the schema
     */
    public boolean isValid(
    ) {
        try {
            validate();
        } catch (ValidationException vex) {
            return false;
        }
        return true;
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
    public void marshal(
            final Writer out)
    throws MarshalException, ValidationException {
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
    public void marshal(
            final org.xml.sax.ContentHandler handler)
    throws IOException, MarshalException, ValidationException {
        Marshaller.marshal(this, handler);
    }

    /**
     * Sets the value of field 'matchGroup'.
     * 
     * @param matchGroup the value of field 'matchGroup'.
     */
    public void setMatchGroup(final Integer matchGroup) {
        m_matchGroup = matchGroup;
    }

    /**
     * Sets the value of field 'name'.
     * 
     * @param name the value of field 'name'.
     */
    public void setName(final String name) {
        m_name = name;
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
     * SessionVariable
     */
    public static SessionVariable unmarshal(final Reader reader) throws MarshalException, ValidationException {
        return (SessionVariable) Unmarshaller.unmarshal(SessionVariable.class, reader);
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

}

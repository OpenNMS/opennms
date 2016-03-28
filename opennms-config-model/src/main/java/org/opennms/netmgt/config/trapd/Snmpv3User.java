/*
 * This class was automatically generated with 
 * <a href="http://www.castor.org">Castor 1.1.2.1</a>, using an XML
 * Schema.
 * $Id$
 */

package org.opennms.netmgt.config.trapd;

  //---------------------------------/
 //- Imported classes and packages -/
//---------------------------------/

import org.exolab.castor.xml.Marshaller;
import org.exolab.castor.xml.Unmarshaller;

/**
 * SNMPv3 User Configuration.
 * 
 * @version $Revision$ $Date$
 */

@SuppressWarnings("all") public class Snmpv3User implements java.io.Serializable {


      //--------------------------/
     //- Class/Member Variables -/
    //--------------------------/

    /**
     * SNMPv3 Application Engine ID
     */
    private java.lang.String _engineId;

    /**
     * SNMPv3 Security Name (User Name)
     */
    private java.lang.String _securityName;

    /**
     * SNMPv3 Security Level (noAuthNoPriv, authNoPriv, authPriv)
     */
    private int _securityLevel;

    /**
     * keeps track of state for field: _securityLevel
     */
    private boolean _has_securityLevel;

    /**
     * SNMPv3 Authentication Protocol
     */
    private java.lang.String _authProtocol;

    /**
     * SNMPv3 Authentication Password Phrase
     */
    private java.lang.String _authPassphrase;

    /**
     * SNMPv3 Privacy Protocol
     */
    private java.lang.String _privacyProtocol;

    /**
     * SNMPv3 Privacy Password Phrase
     */
    private java.lang.String _privacyPassphrase;


      //----------------/
     //- Constructors -/
    //----------------/

    public Snmpv3User() {
        super();
    }


      //-----------/
     //- Methods -/
    //-----------/

    /**
     */
    public void deleteSecurityLevel(
    ) {
        this._has_securityLevel= false;
    }

    /**
     * Overrides the java.lang.Object.equals method.
     * 
     * @param obj
     * @return true if the objects are equal.
     */
    @Override()
    public boolean equals(
            final java.lang.Object obj) {
        if ( this == obj )
            return true;
        
        if (obj instanceof Snmpv3User) {
        
            Snmpv3User temp = (Snmpv3User)obj;
            if (this._engineId != null) {
                if (temp._engineId == null) return false;
                else if (!(this._engineId.equals(temp._engineId))) 
                    return false;
            }
            else if (temp._engineId != null)
                return false;
            if (this._securityName != null) {
                if (temp._securityName == null) return false;
                else if (!(this._securityName.equals(temp._securityName))) 
                    return false;
            }
            else if (temp._securityName != null)
                return false;
            if (this._securityLevel != temp._securityLevel)
                return false;
            if (this._has_securityLevel != temp._has_securityLevel)
                return false;
            if (this._authProtocol != null) {
                if (temp._authProtocol == null) return false;
                else if (!(this._authProtocol.equals(temp._authProtocol))) 
                    return false;
            }
            else if (temp._authProtocol != null)
                return false;
            if (this._authPassphrase != null) {
                if (temp._authPassphrase == null) return false;
                else if (!(this._authPassphrase.equals(temp._authPassphrase))) 
                    return false;
            }
            else if (temp._authPassphrase != null)
                return false;
            if (this._privacyProtocol != null) {
                if (temp._privacyProtocol == null) return false;
                else if (!(this._privacyProtocol.equals(temp._privacyProtocol))) 
                    return false;
            }
            else if (temp._privacyProtocol != null)
                return false;
            if (this._privacyPassphrase != null) {
                if (temp._privacyPassphrase == null) return false;
                else if (!(this._privacyPassphrase.equals(temp._privacyPassphrase))) 
                    return false;
            }
            else if (temp._privacyPassphrase != null)
                return false;
            return true;
        }
        return false;
    }

    /**
     * Returns the value of field 'authPassphrase'. The field
     * 'authPassphrase' has the following description: SNMPv3
     * Authentication Password Phrase
     * 
     * @return the value of field 'AuthPassphrase'.
     */
    public java.lang.String getAuthPassphrase(
    ) {
        return this._authPassphrase;
    }

    /**
     * Returns the value of field 'authProtocol'. The field
     * 'authProtocol' has the following description: SNMPv3
     * Authentication Protocol
     * 
     * @return the value of field 'AuthProtocol'.
     */
    public java.lang.String getAuthProtocol(
    ) {
        return this._authProtocol;
    }

    /**
     * Returns the value of field 'engineId'. The field 'engineId'
     * has the following description: SNMPv3 Application Engine ID
     * 
     * @return the value of field 'EngineId'.
     */
    public java.lang.String getEngineId(
    ) {
        return this._engineId;
    }

    /**
     * Returns the value of field 'privacyPassphrase'. The field
     * 'privacyPassphrase' has the following description: SNMPv3
     * Privacy Password Phrase
     * 
     * @return the value of field 'PrivacyPassphrase'.
     */
    public java.lang.String getPrivacyPassphrase(
    ) {
        return this._privacyPassphrase;
    }

    /**
     * Returns the value of field 'privacyProtocol'. The field
     * 'privacyProtocol' has the following description: SNMPv3
     * Privacy Protocol
     * 
     * @return the value of field 'PrivacyProtocol'.
     */
    public java.lang.String getPrivacyProtocol(
    ) {
        return this._privacyProtocol;
    }

    /**
     * Returns the value of field 'securityLevel'. The field
     * 'securityLevel' has the following description: SNMPv3
     * Security Level (noAuthNoPriv, authNoPriv, authPriv)
     * 
     * @return the value of field 'SecurityLevel'.
     */
    public int getSecurityLevel(
    ) {
        return this._securityLevel;
    }

    /**
     * Returns the value of field 'securityName'. The field
     * 'securityName' has the following description: SNMPv3
     * Security Name (User Name)
     * 
     * @return the value of field 'SecurityName'.
     */
    public java.lang.String getSecurityName(
    ) {
        return this._securityName;
    }

    /**
     * Method hasSecurityLevel.
     * 
     * @return true if at least one SecurityLevel has been added
     */
    public boolean hasSecurityLevel(
    ) {
        return this._has_securityLevel;
    }

    /**
     * Overrides the java.lang.Object.hashCode method.
     * <p>
     * The following steps came from <b>Effective Java Programming
     * Language Guide</b> by Joshua Bloch, Chapter 3
     * 
     * @return a hash code value for the object.
     */
    public int hashCode(
    ) {
        int result = 17;
        
        long tmp;
        if (_engineId != null) {
           result = 37 * result + _engineId.hashCode();
        }
        if (_securityName != null) {
           result = 37 * result + _securityName.hashCode();
        }
        result = 37 * result + _securityLevel;
        if (_authProtocol != null) {
           result = 37 * result + _authProtocol.hashCode();
        }
        if (_authPassphrase != null) {
           result = 37 * result + _authPassphrase.hashCode();
        }
        if (_privacyProtocol != null) {
           result = 37 * result + _privacyProtocol.hashCode();
        }
        if (_privacyPassphrase != null) {
           result = 37 * result + _privacyPassphrase.hashCode();
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
        } catch (org.exolab.castor.xml.ValidationException vex) {
            return false;
        }
        return true;
    }

    /**
     * 
     * 
     * @param out
     * @throws org.exolab.castor.xml.MarshalException if object is
     * null or if any SAXException is thrown during marshaling
     * @throws org.exolab.castor.xml.ValidationException if this
     * object is an invalid instance according to the schema
     */
    public void marshal(
            final java.io.Writer out)
    throws org.exolab.castor.xml.MarshalException, org.exolab.castor.xml.ValidationException {
        Marshaller.marshal(this, out);
    }

    /**
     * 
     * 
     * @param handler
     * @throws java.io.IOException if an IOException occurs during
     * marshaling
     * @throws org.exolab.castor.xml.ValidationException if this
     * object is an invalid instance according to the schema
     * @throws org.exolab.castor.xml.MarshalException if object is
     * null or if any SAXException is thrown during marshaling
     */
    public void marshal(
            final org.xml.sax.ContentHandler handler)
    throws java.io.IOException, org.exolab.castor.xml.MarshalException, org.exolab.castor.xml.ValidationException {
        Marshaller.marshal(this, handler);
    }

    /**
     * Sets the value of field 'authPassphrase'. The field
     * 'authPassphrase' has the following description: SNMPv3
     * Authentication Password Phrase
     * 
     * @param authPassphrase the value of field 'authPassphrase'.
     */
    public void setAuthPassphrase(
            final java.lang.String authPassphrase) {
        this._authPassphrase = authPassphrase;
    }

    /**
     * Sets the value of field 'authProtocol'. The field
     * 'authProtocol' has the following description: SNMPv3
     * Authentication Protocol
     * 
     * @param authProtocol the value of field 'authProtocol'.
     */
    public void setAuthProtocol(
            final java.lang.String authProtocol) {
        this._authProtocol = authProtocol;
    }

    /**
     * Sets the value of field 'engineId'. The field 'engineId' has
     * the following description: SNMPv3 Application Engine ID
     * 
     * @param engineId the value of field 'engineId'.
     */
    public void setEngineId(
            final java.lang.String engineId) {
        this._engineId = engineId;
    }

    /**
     * Sets the value of field 'privacyPassphrase'. The field
     * 'privacyPassphrase' has the following description: SNMPv3
     * Privacy Password Phrase
     * 
     * @param privacyPassphrase the value of field
     * 'privacyPassphrase'.
     */
    public void setPrivacyPassphrase(
            final java.lang.String privacyPassphrase) {
        this._privacyPassphrase = privacyPassphrase;
    }

    /**
     * Sets the value of field 'privacyProtocol'. The field
     * 'privacyProtocol' has the following description: SNMPv3
     * Privacy Protocol
     * 
     * @param privacyProtocol the value of field 'privacyProtocol'.
     */
    public void setPrivacyProtocol(
            final java.lang.String privacyProtocol) {
        this._privacyProtocol = privacyProtocol;
    }

    /**
     * Sets the value of field 'securityLevel'. The field
     * 'securityLevel' has the following description: SNMPv3
     * Security Level (noAuthNoPriv, authNoPriv, authPriv)
     * 
     * @param securityLevel the value of field 'securityLevel'.
     */
    public void setSecurityLevel(
            final int securityLevel) {
        this._securityLevel = securityLevel;
        this._has_securityLevel = true;
    }

    /**
     * Sets the value of field 'securityName'. The field
     * 'securityName' has the following description: SNMPv3
     * Security Name (User Name)
     * 
     * @param securityName the value of field 'securityName'.
     */
    public void setSecurityName(
            final java.lang.String securityName) {
        this._securityName = securityName;
    }

    /**
     * Method unmarshal.
     * 
     * @param reader
     * @throws org.exolab.castor.xml.MarshalException if object is
     * null or if any SAXException is thrown during marshaling
     * @throws org.exolab.castor.xml.ValidationException if this
     * object is an invalid instance according to the schema
     * @return the unmarshaled
     * org.opennms.netmgt.config.trapd.Snmpv3User
     */
    public static org.opennms.netmgt.config.trapd.Snmpv3User unmarshal(
            final java.io.Reader reader)
    throws org.exolab.castor.xml.MarshalException, org.exolab.castor.xml.ValidationException {
        return (org.opennms.netmgt.config.trapd.Snmpv3User) Unmarshaller.unmarshal(org.opennms.netmgt.config.trapd.Snmpv3User.class, reader);
    }

    /**
     * 
     * 
     * @throws org.exolab.castor.xml.ValidationException if this
     * object is an invalid instance according to the schema
     */
    public void validate(
    )
    throws org.exolab.castor.xml.ValidationException {
        org.exolab.castor.xml.Validator validator = new org.exolab.castor.xml.Validator();
        validator.validate(this);
    }

}

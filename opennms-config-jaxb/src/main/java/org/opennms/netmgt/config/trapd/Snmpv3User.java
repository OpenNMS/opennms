/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2016-2016 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2016 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.config.trapd;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

/**
 * SNMPv3 User Configuration.
 * 
 * @version $Revision$ $Date$
 */
@XmlRootElement(name = "snmpv3-user")
@XmlAccessorType(XmlAccessType.NONE)
@SuppressWarnings("all") 
public class Snmpv3User implements java.io.Serializable {
	private static final long serialVersionUID = 61220221955256341L;

      //--------------------------/
     //- Class/Member Variables -/
    //--------------------------/
	

	/**
     * SNMPv3 Application Engine ID
     */
	@XmlAttribute(name="engine-id",required=false)
    private java.lang.String _engineId;

    /**
     * SNMPv3 Security Name (User Name)
     */
	@XmlAttribute(name="security-name",required=false)
    private java.lang.String _securityName;

    /**
     * SNMPv3 Security Level (noAuthNoPriv, authNoPriv, authPriv)
     */
	@XmlAttribute(name="security-level",required=false)
    private Integer _securityLevel;

    /**
     * SNMPv3 Authentication Protocol
     */
	@XmlAttribute(name="auth-protocol",required=false)
    private java.lang.String _authProtocol;

    /**
     * SNMPv3 Authentication Password Phrase
     */
	@XmlAttribute(name="auth-passphrase",required=false)
    private java.lang.String _authPassphrase;

    /**
     * SNMPv3 Privacy Protocol
     */
	@XmlAttribute(name="privacy-protocol",required=false)
    private java.lang.String _privacyProtocol;

    /**
     * SNMPv3 Privacy Password Phrase
     */
	@XmlAttribute(name="privacy-passphrase",required=false)
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
    public Integer getSecurityLevel(
    ) {
        return this._securityLevel;

        //return this._securityLevel == null ? 0 : this._securityLevel;
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
        result = 37 * result + (_securityLevel == null ? 0 : _securityLevel);
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
    public void setSecurityLevel(final Integer securityLevel) {
        this._securityLevel = securityLevel;
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

}

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
    private java.lang.String engineId;

    /**
     * SNMPv3 Security Name (User Name)
     */
	@XmlAttribute(name="security-name",required=false)
    private java.lang.String securityName;

    /**
     * SNMPv3 Security Level (noAuthNoPriv, authNoPriv, authPriv)
     */
	@XmlAttribute(name="security-level",required=false)
    private Integer securityLevel;

    /**
     * SNMPv3 Authentication Protocol
     */
	@XmlAttribute(name="auth-protocol",required=false)
    private java.lang.String authProtocol;

    /**
     * SNMPv3 Authentication Password Phrase
     */
	@XmlAttribute(name="auth-passphrase",required=false)
    private java.lang.String authPassphrase;

    /**
     * SNMPv3 Privacy Protocol
     */
	@XmlAttribute(name="privacy-protocol",required=false)
    private java.lang.String privacyProtocol;

    /**
     * SNMPv3 Privacy Password Phrase
     */
	@XmlAttribute(name="privacy-passphrase",required=false)
    private java.lang.String privacyPassphrase;


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
            if (this.engineId != null) {
                if (temp.engineId == null) return false;
                else if (!(this.engineId.equals(temp.engineId))) 
                    return false;
            }
            else if (temp.engineId != null)
                return false;
            if (this.securityName != null) {
                if (temp.securityName == null) return false;
                else if (!(this.securityName.equals(temp.securityName))) 
                    return false;
            }
            else if (temp.securityName != null)
                return false;
            if (this.securityLevel != temp.securityLevel)
                return false;
            if (this.authProtocol != null) {
                if (temp.authProtocol == null) return false;
                else if (!(this.authProtocol.equals(temp.authProtocol))) 
                    return false;
            }
            else if (temp.authProtocol != null)
                return false;
            if (this.authPassphrase != null) {
                if (temp.authPassphrase == null) return false;
                else if (!(this.authPassphrase.equals(temp.authPassphrase))) 
                    return false;
            }
            else if (temp.authPassphrase != null)
                return false;
            if (this.privacyProtocol != null) {
                if (temp.privacyProtocol == null) return false;
                else if (!(this.privacyProtocol.equals(temp.privacyProtocol))) 
                    return false;
            }
            else if (temp.privacyProtocol != null)
                return false;
            if (this.privacyPassphrase != null) {
                if (temp.privacyPassphrase == null) return false;
                else if (!(this.privacyPassphrase.equals(temp.privacyPassphrase))) 
                    return false;
            }
            else if (temp.privacyPassphrase != null)
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
        return this.authPassphrase;
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
        return this.authProtocol;
    }

    /**
     * Returns the value of field 'engineId'. The field 'engineId'
     * has the following description: SNMPv3 Application Engine ID
     * 
     * @return the value of field 'EngineId'.
     */
    public java.lang.String getEngineId(
    ) {
        return this.engineId;
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
        return this.privacyPassphrase;
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
        return this.privacyProtocol;
    }

    /**
     * Returns the value of field 'securityLevel'. The field
     * 'securityLevel' has the following description: SNMPv3
     * Security Level (noAuthNoPriv, authNoPriv, authPriv)
     * 
     * @return the value of field 'SecurityLevel'.
     */
    public Integer getSecurityLevel() {
        return this.securityLevel;
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
        return this.securityName;
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
        if (this.engineId != null) {
           result = 37 * result + this.engineId.hashCode();
        }
        if (this.securityName != null) {
           result = 37 * result + this.securityName.hashCode();
        }
        result = 37 * result + (this.securityLevel == null ? 0 : this.securityLevel);
        if (this.authProtocol != null) {
           result = 37 * result + this.authProtocol.hashCode();
        }
        if (this.authPassphrase != null) {
           result = 37 * result + this.authPassphrase.hashCode();
        }
        if (this.privacyProtocol != null) {
           result = 37 * result + this.privacyProtocol.hashCode();
        }
        if (this.privacyPassphrase != null) {
           result = 37 * result + this.privacyPassphrase.hashCode();
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
        this.authPassphrase = authPassphrase;
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
        this.authProtocol = authProtocol;
    }

    /**
     * Sets the value of field 'engineId'. The field 'engineId' has
     * the following description: SNMPv3 Application Engine ID
     * 
     * @param engineId the value of field 'engineId'.
     */
    public void setEngineId(
            final java.lang.String engineId) {
        this.engineId = engineId;
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
        this.privacyPassphrase = privacyPassphrase;
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
        this.privacyProtocol = privacyProtocol;
    }

    /**
     * Sets the value of field 'securityLevel'. The field
     * 'securityLevel' has the following description: SNMPv3
     * Security Level (noAuthNoPriv, authNoPriv, authPriv)
     * 
     * @param securityLevel the value of field 'securityLevel'.
     */
    public void setSecurityLevel(final Integer securityLevel) {
        this.securityLevel = securityLevel;
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
        this.securityName = securityName;
    }

}

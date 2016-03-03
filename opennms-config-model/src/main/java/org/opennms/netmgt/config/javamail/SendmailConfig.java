/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2002-2016 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.config.javamail;

import java.io.Serializable;
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

/**
 * The Class SendmailConfig.
 * 
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a>
 */
@XmlRootElement(name="sendmail-config", namespace="http://xmlns.opennms.org/xsd/config/javamail-configuration")
@XmlAccessorType(XmlAccessType.FIELD)
public class SendmailConfig implements Serializable {

    //--------------------------/
    //- Class/Member Variables -/
    //--------------------------/

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 2586518402195556647L;

    /** The debug flag. */
    @XmlAttribute(name="debug")
    private Boolean _debug;

    /** The use authentication flag. */
    @XmlAttribute(name="use-authentication")
    private Boolean _useAuthentication;

    /** The use JMTA flag. */
    @XmlAttribute(name="use-jmta")
    private Boolean _useJmta;

    /** The attempt interval. */
    @XmlAttribute(name="attempt-interval")
    private Long _attemptInterval;

    /** The name. */
    @XmlAttribute(name="name")
    private String _name;

    /**
     * Use these name value pairs to configure free-form properties from the JavaMail class.
     */
    @XmlElement(name="javamail-property")
    private List<JavamailProperty> _javamailPropertyList;

    /** Configuration for a sendmail host. */
    @XmlElement(name="sendmail-host")
    private SendmailHost _sendmailHost;

    /**
     * Basically attributes that help setup the javamailer's confusion set of properties.
     */
    @XmlElement(name="sendmail-protocol")
    private SendmailProtocol _sendmailProtocol;

    /**
     * Define the To, From, Subject, and body of a message. If not defined, one will be defined for your benefit (or confusion ;-)
     */
    @XmlElement(name="sendmail-message")
    private SendmailMessage _sendmailMessage;

    /**
     * Configure user based authentication.
     */
    @XmlElement(name="user-auth")
    private UserAuth _userAuth;

    //----------------/
    //- Constructors -/
    //----------------/

    /**
     * Instantiates a new sendmail configuration.
     */
    public SendmailConfig() {
        super();
        this._javamailPropertyList = new ArrayList<JavamailProperty>();
    }

    //-----------/
    //- Methods -/
    //-----------/

    /**
     * Adds the javamail property.
     *
     * @param vJavamailProperty the javamail property
     * @throws IndexOutOfBoundsException if the index given is outside the bounds of the collection
     */
    public void addJavamailProperty(final JavamailProperty vJavamailProperty) throws IndexOutOfBoundsException {
        this._javamailPropertyList.add(vJavamailProperty);
    }

    /**
     * Adds the javamail property.
     *
     * @param index the index
     * @param vJavamailProperty the javamail property
     * @throws IndexOutOfBoundsException if the index given is outside the bounds of the collection
     */
    public void addJavamailProperty(final int index, final JavamailProperty vJavamailProperty) throws IndexOutOfBoundsException {
        this._javamailPropertyList.add(index, vJavamailProperty);
    }

    /**
     * Method enumerateJavamailProperty.
     * 
     * @return an Enumeration over all possible elements of this
     * collection
     */
    public Enumeration<JavamailProperty> enumerateJavamailProperty() {
        return Collections.enumeration(this._javamailPropertyList);
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override()
    public boolean equals(final Object obj) {
        if ( this == obj )
            return true;
        if (obj instanceof SendmailConfig) {
            SendmailConfig temp = (SendmailConfig)obj;
            if (this._debug != temp._debug)
                return false;
            if (this._useAuthentication != temp._useAuthentication)
                return false;
            if (this._useJmta != temp._useJmta)
                return false;
            if (this._attemptInterval != temp._attemptInterval)
                return false;
            if (this._name != null) {
                if (temp._name == null) return false;
                else if (!(this._name.equals(temp._name))) 
                    return false;
            }
            else if (temp._name != null)
                return false;
            if (this._javamailPropertyList != null) {
                if (temp._javamailPropertyList == null) return false;
                else if (!(this._javamailPropertyList.equals(temp._javamailPropertyList))) 
                    return false;
            }
            else if (temp._javamailPropertyList != null)
                return false;
            if (this._sendmailHost != null) {
                if (temp._sendmailHost == null) return false;
                else if (!(this._sendmailHost.equals(temp._sendmailHost))) 
                    return false;
            }
            else if (temp._sendmailHost != null)
                return false;
            if (this._sendmailProtocol != null) {
                if (temp._sendmailProtocol == null) return false;
                else if (!(this._sendmailProtocol.equals(temp._sendmailProtocol))) 
                    return false;
            }
            else if (temp._sendmailProtocol != null)
                return false;
            if (this._sendmailMessage != null) {
                if (temp._sendmailMessage == null) return false;
                else if (!(this._sendmailMessage.equals(temp._sendmailMessage))) 
                    return false;
            }
            else if (temp._sendmailMessage != null)
                return false;
            if (this._userAuth != null) {
                if (temp._userAuth == null) return false;
                else if (!(this._userAuth.equals(temp._userAuth))) 
                    return false;
            }
            else if (temp._userAuth != null)
                return false;
            return true;
        }
        return false;
    }

    /**
     * Returns the value of field 'attemptInterval'.
     * 
     * @return the value of field 'AttemptInterval'.
     */
    public Long getAttemptInterval() {
        return this._attemptInterval == null ? 3000 : this._attemptInterval;
    }

    /**
     * Returns the value of field 'debug'.
     * 
     * @return the value of field 'Debug'.
     */
    public Boolean isDebug() {
        return this._debug == null ? Boolean.TRUE : this._debug;
    }

    /**
     * Method getJavamailProperty.
     *
     * @param index the index
     * @return the value of the JavamailProperty at the given index
     * @throws IndexOutOfBoundsException if the index given is outside the bounds of the collection
     */
    public JavamailProperty getJavamailProperty(final int index) throws IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this._javamailPropertyList.size()) {
            throw new IndexOutOfBoundsException("getJavamailProperty: Index value '" + index + "' not in range [0.." + (this._javamailPropertyList.size() - 1) + "]");
        }
        return _javamailPropertyList.get(index);
    }

    /**
     * Method getJavamailProperty.Returns the contents of the collection in an Array
     * <p>Note:  Just in case the  collection contents are changing in another thread, we pass a 0-length Array of the correct type into the API call. 
     * This way we <i>know</i> that the Array returned is of exactly the correct length.</p>
     * 
     * @return this collection as an Array
     */
    public JavamailProperty[] getJavamailProperty() {
        JavamailProperty[] array = new JavamailProperty[0];
        return this._javamailPropertyList.toArray(array);
    }

    /**
     * Method getJavamailPropertyCollection.Returns a reference to '_javamailPropertyList'. No type checking is performed on
     * any modifications to the Vector.
     * 
     * @return a reference to the Vector backing this class
     */
    public List<JavamailProperty> getJavamailPropertyCollection() {
        return this._javamailPropertyList;
    }

    /**
     * Method getJavamailPropertyCount.
     * 
     * @return the size of this collection
     */
    public int getJavamailPropertyCount() {
        return this._javamailPropertyList.size();
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
     * Returns the value of field 'sendmailHost'. The field 'sendmailHost' has the following description: Configuration
     * for a sendmail host
     * 
     * @return the value of field 'SendmailHost'.
     */
    public SendmailHost getSendmailHost() {
        return this._sendmailHost;
    }

    /**
     * Returns the value of field 'sendmailMessage'. The field 'sendmailMessage' has the following description: Define the
     * To, From, Subject, and body of a message. If not defined, one will be defined for your benefit (or confusion ;-)
     * 
     * @return the value of field 'SendmailMessage'.
     */
    public SendmailMessage getSendmailMessage() {
        return this._sendmailMessage;
    }

    /**
     * Returns the value of field 'sendmailProtocol'. The field 'sendmailProtocol' has the following description: Basically
     * attributes that help setup the javamailer's confusion set of properties.
     *  
     * @return the value of field 'SendmailProtocol'.
     */
    public SendmailProtocol getSendmailProtocol() {
        return this._sendmailProtocol;
    }

    /**
     * Returns the value of field 'useAuthentication'.
     * 
     * @return the value of field 'UseAuthentication'.
     */
    public Boolean isUseAuthentication() {
        return this._useAuthentication == null ? Boolean.FALSE : this._useAuthentication;
    }

    /**
     * Returns the value of field 'useJmta'.
     * 
     * @return the value of field 'UseJmta'.
     */
    public Boolean isUseJmta() {
        return this._useJmta == null ? Boolean.TRUE : this._useJmta;
    }

    /**
     * Returns the value of field 'userAuth'. The field 'userAuth' has the following description: Configure user based authentication.
     *  
     * @return the value of field 'UserAuth'.
     */
    public UserAuth getUserAuth() {
        return this._userAuth;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override()
    public int hashCode() {
        int result = 17;
        result = 37 * result + (_debug?0:1);
        result = 37 * result + (_useAuthentication?0:1);
        result = 37 * result + (_useJmta?0:1);
        result = 37 * result + (int)(_attemptInterval^(_attemptInterval>>>32));
        if (_name != null) {
            result = 37 * result + _name.hashCode();
        }
        if (_javamailPropertyList != null) {
            result = 37 * result + _javamailPropertyList.hashCode();
        }
        if (_sendmailHost != null) {
            result = 37 * result + _sendmailHost.hashCode();
        }
        if (_sendmailProtocol != null) {
            result = 37 * result + _sendmailProtocol.hashCode();
        }
        if (_sendmailMessage != null) {
            result = 37 * result + _sendmailMessage.hashCode();
        }
        if (_userAuth != null) {
            result = 37 * result + _userAuth.hashCode();
        }
        return result;
    }

    /**
     * Method iterateJavamailProperty.
     * 
     * @return an Iterator over all possible elements in this collection
     */
    public Iterator<JavamailProperty> iterateJavamailProperty() {
        return this._javamailPropertyList.iterator();
    }

    /**
     * Removes the all javamail property.
     */
    public void removeAllJavamailProperty() {
        this._javamailPropertyList.clear();
    }

    /**
     * Method removeJavamailProperty.
     *
     * @param vJavamailProperty the javamail property
     * @return true if the object was removed from the collection.
     */
    public boolean removeJavamailProperty(final JavamailProperty vJavamailProperty) {
        return _javamailPropertyList.remove(vJavamailProperty);
    }

    /**
     * Method removeJavamailPropertyAt.
     *
     * @param index the index
     * @return the element removed from the collection
     */
    public JavamailProperty removeJavamailPropertyAt(final int index) {
        return this._javamailPropertyList.remove(index);
    }

    /**
     * Sets the value of field 'attemptInterval'.
     * 
     * @param attemptInterval the value of field 'attemptInterval'.
     */
    public void setAttemptInterval(final Long attemptInterval) {
        this._attemptInterval = attemptInterval;
    }

    /**
     * Sets the value of field 'debug'.
     * 
     * @param debug the value of field 'debug'.
     */
    public void setDebug(final Boolean debug) {
        this._debug = debug;
    }

    /**
     * Sets the javamail property.
     *
     * @param index the index
     * @param vJavamailProperty the avamail property
     * @throws IndexOutOfBoundsException if the index given is outside the bounds of the collection
     */
    public void setJavamailProperty(final int index, final JavamailProperty vJavamailProperty) throws IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this._javamailPropertyList.size()) {
            throw new IndexOutOfBoundsException("setJavamailProperty: Index value '" + index + "' not in range [0.." + (this._javamailPropertyList.size() - 1) + "]");
        }
        this._javamailPropertyList.set(index, vJavamailProperty);
    }

    /**
     * Sets the javamail property.
     *
     * @param vJavamailPropertyArray the new javamail property
     */
    public void setJavamailProperty(final JavamailProperty[] vJavamailPropertyArray) {
        //-- copy array
        _javamailPropertyList.clear();
        for (int i = 0; i < vJavamailPropertyArray.length; i++) {
            this._javamailPropertyList.add(vJavamailPropertyArray[i]);
        }
    }

    /**
     * Sets the value of '_javamailPropertyList' by copying the given Vector. All elements will be checked for type safety.
     * 
     * @param vJavamailPropertyList the Vector to copy.
     */
    public void setJavamailProperty(final List<JavamailProperty> vJavamailPropertyList) {
        // copy vector
        this._javamailPropertyList.clear();
        this._javamailPropertyList.addAll(vJavamailPropertyList);
    }

    /**
     * Sets the value of '_javamailPropertyList' by setting it to the given Vector. No type checking is performed.
     *
     * @param javamailPropertyList the Vector to set.
     * @deprecated 
     */
    public void setJavamailPropertyCollection(final List<JavamailProperty> javamailPropertyList) {
        this._javamailPropertyList = javamailPropertyList;
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
     * Sets the value of field 'sendmailHost'. The field 'sendmailHost' has the following description: Configuration for a sendmail host
     *  
     * @param sendmailHost the value of field 'sendmailHost'.
     */
    public void setSendmailHost(final SendmailHost sendmailHost) {
        this._sendmailHost = sendmailHost;
    }

    /**
     * Sets the value of field 'sendmailMessage'. The field 'sendmailMessage' has the following description: Define the
     * To, From, Subject, and body of a message. If not defined, one will be defined for your benefit (or confusion ;-)
     *  
     * @param sendmailMessage the value of field 'sendmailMessage'.
     */
    public void setSendmailMessage(final SendmailMessage sendmailMessage) {
        this._sendmailMessage = sendmailMessage;
    }

    /**
     * Sets the value of field 'sendmailProtocol'. The field 'sendmailProtocol' has the following description: Basically
     * attributes that help setup the javamailer's confusion set of properties.
     * 
     * @param sendmailProtocol the value of field 'sendmailProtocol'
     */
    public void setSendmailProtocol(final SendmailProtocol sendmailProtocol) {
        this._sendmailProtocol = sendmailProtocol;
    }

    /**
     * Sets the value of field 'useAuthentication'.
     * 
     * @param useAuthentication the value of field 'useAuthentication'.
     */
    public void setUseAuthentication(final boolean useAuthentication) {
        this._useAuthentication = useAuthentication;
    }

    /**
     * Sets the value of field 'useJmta'.
     * 
     * @param useJmta the value of field 'useJmta'.
     */
    public void setUseJmta(final Boolean useJmta) {
        this._useJmta = useJmta;
    }

    /**
     * Sets the value of field 'userAuth'. The field 'userAuth' has the following description: Configure user based authentication.
     * 
     * @param userAuth the value of field 'userAuth'.
     */
    public void setUserAuth(final UserAuth userAuth) {
        this._userAuth = userAuth;
    }

}

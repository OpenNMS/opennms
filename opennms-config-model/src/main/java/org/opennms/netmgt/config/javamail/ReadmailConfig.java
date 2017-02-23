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
 * The Class ReadmailConfig.
 * 
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a>
 */
@XmlRootElement(name="readmail-config", namespace="http://xmlns.opennms.org/xsd/config/javamail-configuration")
@XmlAccessorType(XmlAccessType.FIELD)
public class ReadmailConfig implements Serializable {

    //--------------------------/
    //- Class/Member Variables -/
    //--------------------------/

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = -8838663418389605178L;

    /** The debug flag. */
    @XmlAttribute(name="debug")
    private Boolean _debug;

    /** The mail folder. */
    @XmlAttribute(name="mail-folder")
    private String _mailFolder;

    /** The attempt interval. */
    @XmlAttribute(name="attempt-interval")
    private Long _attemptInterval;

    /** The delete all mail flag. */
    @XmlAttribute(name="delete-all-mail")
    private Boolean _deleteAllMail;

    /** The name. */
    @XmlAttribute(name="name")
    private String _name;

    /**
     * Use these name value pairs to configure free-form properties from the JavaMail class.
     */
    @XmlElement(name="javamail-property")
    private List<JavamailProperty> _javamailPropertyList;

    /**
     * Define the host and port of a service for reading email.
     */
    @XmlElement(name="readmail-host")
    private ReadmailHost _readmailHost;

    /**
     * Configure user based authentication.
     */
    @XmlElement(name="user-auth")
    private UserAuth _userAuth;

    //----------------/
    //- Constructors -/
    //----------------/

    /**
     * Instantiates a new readmail config.
     */
    public ReadmailConfig() {
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
     * @return an Enumeration over all possible elements of this collection
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
        if (obj instanceof ReadmailConfig) {
            ReadmailConfig temp = (ReadmailConfig)obj;
            if (this._debug != temp._debug)
                return false;
            if (this._mailFolder != null) {
                if (temp._mailFolder == null) return false;
                else if (!(this._mailFolder.equals(temp._mailFolder))) 
                    return false;
            }
            else if (temp._mailFolder != null)
                return false;
            if (this._attemptInterval != temp._attemptInterval)
                return false;
            if (this._deleteAllMail != temp._deleteAllMail)
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
            if (this._readmailHost != null) {
                if (temp._readmailHost == null) return false;
                else if (!(this._readmailHost.equals(temp._readmailHost))) 
                    return false;
            }
            else if (temp._readmailHost != null)
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
        return this._attemptInterval == null ? 1000 : this._attemptInterval;
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
     * Returns the value of field 'deleteAllMail'.
     * 
     * @return the value of field 'DeleteAllMail'.
     */
    public Boolean isDeleteAllMail() {
        return this._deleteAllMail ? Boolean.FALSE : this._deleteAllMail;
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
     * Method getJavamailProperty.Returns the contents of the collection in an Array.
     * <p>Note:  Just in case the collection contents are changing in another thread, we pass a 0-length Array of the correct type into the API call. 
     * This way we <i>know</i> that the Array returned is of exactly the correct length.</p>
     * 
     * @return this collection as an Array
     */
    public JavamailProperty[] getJavamailProperty() {
        JavamailProperty[] array = new JavamailProperty[0];
        return this._javamailPropertyList.toArray(array);
    }

    /**
     * Method getJavamailPropertyCollection.Returns a reference to '_javamailPropertyList'. No type checking is performed on any modifications to the Vector.
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
     * Returns the value of field 'mailFolder'.
     * 
     * @return the value of field 'MailFolder'.
     */
    public String getMailFolder() {
        return this._mailFolder == null ? "INBOX" : this._mailFolder;
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
     * Returns the value of field 'readmailHost'. The field 'readmailHost' has the following description: Define the host and port of a service for reading email.
     * 
     * @return the value of field 'ReadmailHost'.
     */
    public ReadmailHost getReadmailHost() {
        return this._readmailHost;
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
        if (_mailFolder != null) {
            result = 37 * result + _mailFolder.hashCode();
        }
        result = 37 * result + (int)(_attemptInterval^(_attemptInterval>>>32));
        result = 37 * result + (_deleteAllMail?0:1);
        if (_name != null) {
            result = 37 * result + _name.hashCode();
        }
        if (_javamailPropertyList != null) {
            result = 37 * result + _javamailPropertyList.hashCode();
        }
        if (_readmailHost != null) {
            result = 37 * result + _readmailHost.hashCode();
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
     * @param vJavamailProperty the v javamail property
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
     * Sets the value of field 'deleteAllMail'.
     * 
     * @param deleteAllMail the value of field 'deleteAllMail'.
     */
    public void setDeleteAllMail(final Boolean deleteAllMail) {
        this._deleteAllMail = deleteAllMail;
    }

    /**
     * Sets the javamail property.
     *
     * @param index the index
     * @param vJavamailProperty the javamail property
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
     * Sets the value of field 'mailFolder'.
     * 
     * @param mailFolder the value of field 'mailFolder'.
     */
    public void setMailFolder(final String mailFolder) {
        this._mailFolder = mailFolder;
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
     * Sets the value of field 'readmailHost'. The field 'readmailHost' has the following description: Define the host and port of a service for reading email.
     *  
     * @param readmailHost the value of field 'readmailHost'.
     */
    public void setReadmailHost(final ReadmailHost readmailHost) {
        this._readmailHost = readmailHost;
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

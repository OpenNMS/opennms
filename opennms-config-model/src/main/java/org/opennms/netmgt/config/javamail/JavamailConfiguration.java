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
import java.util.Objects;
import java.util.Optional;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * The Class JavamailConfiguration.
 * 
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a>
 */
@XmlRootElement(name="javamail-configuration", namespace="http://xmlns.opennms.org/xsd/config/javamail-configuration")
@XmlAccessorType(XmlAccessType.FIELD)
public class JavamailConfiguration implements Serializable {

    //--------------------------/
    //- Class/Member Variables -/
    //--------------------------/

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /** The default send configuration name. */
    @XmlAttribute(name="default-send-config-name")
    private String _defaultSendConfigName;

    /** The default read configuration name. */
    @XmlAttribute(name="default-read-config-name")
    private String _defaultReadConfigName;

    /**
     * This entity defines the test for sending mail. Attributes are used to
     *  derive values of java mail properties, or, they can be specified directly
     *  as key value pairs. Attributes will are easier to read but there isn't 
     *  an attribute for every javamail property possible (some are fairly obscure).
     */
    @XmlElement(name="sendmail-config")
    private List<SendmailConfig> _sendmailConfigList = new ArrayList<>();

    /** Configuration container for configuration all settings for reading email. */
    @XmlElement(name="readmail-config")
    private List<ReadmailConfig> _readmailConfigList = new ArrayList<>();

    /** Read and Send configuration list. */
    @XmlElement(name="end2end-mail-config")
    private List<End2endMailConfig> _end2endMailConfigList = new ArrayList<>();

    //----------------/
    //- Constructors -/
    //----------------/

    /**
     * Instantiates a new javamail configuration.
     */
    public JavamailConfiguration() {
        super();
        this._sendmailConfigList = new ArrayList<SendmailConfig>();
        this._readmailConfigList = new ArrayList<ReadmailConfig>();
        this._end2endMailConfigList = new ArrayList<End2endMailConfig>();
    }

    //-----------/
    //- Methods -/
    //-----------/

    /**
     * Adds the end2end mail configuration.
     *
     * @param vEnd2endMailConfig the end2end mail configuration
     * @throws IndexOutOfBoundsException if the index given is outside the bounds of the collection
     */
    public void addEnd2endMailConfig(final End2endMailConfig vEnd2endMailConfig) throws IndexOutOfBoundsException {
        this._end2endMailConfigList.add(vEnd2endMailConfig);
    }

    /**
     * Adds the end2end mail configuration.
     *
     * @param index the index
     * @param vEnd2endMailConfig the end2end mail configuration
     * @throws IndexOutOfBoundsException if the index given is outside the bounds of the collection
     */
    public void addEnd2endMailConfig(final int index, final End2endMailConfig vEnd2endMailConfig) throws IndexOutOfBoundsException {
        this._end2endMailConfigList.add(index, vEnd2endMailConfig);
    }

    /**
     * Adds the readmail configuration.
     *
     * @param vReadmailConfig the readmail configuration
     * @throws IndexOutOfBoundsException if the index given is outside the bounds of the collection
     */
    public void addReadmailConfig(final ReadmailConfig vReadmailConfig) throws IndexOutOfBoundsException {
        this._readmailConfigList.add(vReadmailConfig);
    }

    /**
     * Adds the readmail configuration.
     *
     * @param index the index
     * @param vReadmailConfig the readmail configuration
     * @throws IndexOutOfBoundsException if the index given is outside the bounds of the collection
     */
    public void addReadmailConfig(final int index, final ReadmailConfig vReadmailConfig) throws IndexOutOfBoundsException {
        this._readmailConfigList.add(index, vReadmailConfig);
    }

    /**
     * Adds the sendmail configuration.
     *
     * @param vSendmailConfig the sendmail configuration
     * @throws IndexOutOfBoundsException if the index given is outside the bounds of the collection
     */
    public void addSendmailConfig(final SendmailConfig vSendmailConfig) throws IndexOutOfBoundsException {
        this._sendmailConfigList.add(vSendmailConfig);
    }

    /**
     * Adds the sendmail configuration.
     *
     * @param index the index
     * @param vSendmailConfig the sendmail configuration
     * @throws IndexOutOfBoundsException if the index given is outside the bounds of the collection
     */
    public void addSendmailConfig(final int index, final SendmailConfig vSendmailConfig) throws IndexOutOfBoundsException {
        this._sendmailConfigList.add(index, vSendmailConfig);
    }

    /**
     * Method enumerateEnd2endMailConfig.
     * 
     * @return an Enumeration over all possible elements of this collection
     */
    public Enumeration<End2endMailConfig> enumerateEnd2endMailConfig() {
        return Collections.enumeration(this._end2endMailConfigList);
    }

    /**
     * Method enumerateReadmailConfig.
     * 
     * @return an Enumeration over all possible elements of this collection
     */
    public Enumeration<ReadmailConfig> enumerateReadmailConfig() {
        return Collections.enumeration(this._readmailConfigList);
    }

    /**
     * Method enumerateSendmailConfig.
     * 
     * @return an Enumeration over all possible elements of this collection
     */
    public Enumeration<SendmailConfig> enumerateSendmailConfig() {
        return Collections.enumeration(this._sendmailConfigList);
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override()
    public boolean equals(final Object obj) {
        if ( this == obj ) {
            return true;
        }

        if (obj instanceof JavamailConfiguration) {
            final JavamailConfiguration temp = (JavamailConfiguration)obj;
            return Objects.equals(temp._defaultSendConfigName, _defaultSendConfigName)
                    && Objects.equals(temp._defaultReadConfigName, _defaultReadConfigName)
                    && Objects.equals(temp._sendmailConfigList, _sendmailConfigList)
                    && Objects.equals(temp._readmailConfigList, _readmailConfigList)
                    && Objects.equals(temp._end2endMailConfigList, _end2endMailConfigList);
        }
        return false;
    }

    /**
     * Returns the value of field 'defaultReadConfigName'.
     * 
     * @return the value of field 'DefaultReadConfigName'.
     */
    public Optional<String> getDefaultReadConfigName() {
        return Optional.ofNullable(this._defaultReadConfigName);
    }

    /**
     * Returns the value of field 'defaultSendConfigName'.
     * 
     * @return the value of field 'DefaultSendConfigName'.
     */
    public Optional<String> getDefaultSendConfigName() {
        return Optional.ofNullable(this._defaultSendConfigName);
    }

    /**
     * Method getEnd2endMailConfig.
     *
     * @param index the index
     * @return the value of the End2endMailConfig at the given index
     * @throws IndexOutOfBoundsException if the index given is outside the bounds of the collection
     */
    public End2endMailConfig getEnd2endMailConfig(final int index) throws IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this._end2endMailConfigList.size()) {
            throw new IndexOutOfBoundsException("getEnd2endMailConfig: Index value '" + index + "' not in range [0.." + (this._end2endMailConfigList.size() - 1) + "]");
        }
        return _end2endMailConfigList.get(index);
    }

    /**
     * Method getEnd2endMailConfig.Returns the contents of the collection in an Array.
     * <p>Note: Just in case the collection contents are changing in another thread, we pass a 0-length Array of the correct type into the API call.
     * This way we <i>know</i> that the Array returned is of exactly the correct length.</p>
     * 
     * @return this collection as an Array
     */
    public End2endMailConfig[] getEnd2endMailConfig() {
        End2endMailConfig[] array = new End2endMailConfig[0];
        return this._end2endMailConfigList.toArray(array);
    }

    /**
     * Method getEnd2endMailConfigCollection.Returns a reference to '_end2endMailConfigList'. No type checking is performed on any modifications to the Vector.
     * 
     * @return a reference to the Vector backing this class
     */
    public List<End2endMailConfig> getEnd2endMailConfigCollection() {
        return this._end2endMailConfigList;
    }

    /**
     * Method getEnd2endMailConfigCount.
     * 
     * @return the size of this collection
     */
    public int getEnd2endMailConfigCount() {
        return this._end2endMailConfigList.size();
    }

    /**
     * Method getReadmailConfig.
     *
     * @param index the index
     * @return the value of the ReadmailConfig at the given index
     * @throws IndexOutOfBoundsException if the index given is outside the bounds of the collection
     */
    public ReadmailConfig getReadmailConfig(final int index) throws IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this._readmailConfigList.size()) {
            throw new IndexOutOfBoundsException("getReadmailConfig: Index value '" + index + "' not in range [0.." + (this._readmailConfigList.size() - 1) + "]");
        }
        return _readmailConfigList.get(index);
    }

    /**
     * Method getReadmailConfig.Returns the contents of the collection in an Array.
     * <p>Note:  Just in case the collection contents are changing in another thread, we pass a 0-length Array of the correct type into the API call. 
     * This way we <i>know</i> that the Array returned is of exactly the correct length.</p>
     * 
     * @return this collection as an Array
     */
    public ReadmailConfig[] getReadmailConfig() {
        ReadmailConfig[] array = new ReadmailConfig[0];
        return this._readmailConfigList.toArray(array);
    }

    /**
     * Method getReadmailConfigCollection.Returns a reference to '_readmailConfigList'. No type checking is performed on any modifications to the Vector.
     * 
     * @return a reference to the Vector backing this class
     */
    public List<ReadmailConfig> getReadmailConfigCollection() {
        return this._readmailConfigList;
    }

    /**
     * Method getReadmailConfigCount.
     * 
     * @return the size of this collection
     */
    public int getReadmailConfigCount() {
        return this._readmailConfigList.size();
    }

    /**
     * Method getSendmailConfig.
     *
     * @param index the index
     * @return the value of the SendmailConfig at the given index
     * @throws IndexOutOfBoundsException if the index given is outside the bounds of the collection
     */
    public SendmailConfig getSendmailConfig(final int index) throws IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this._sendmailConfigList.size()) {
            throw new IndexOutOfBoundsException("getSendmailConfig: Index value '" + index + "' not in range [0.." + (this._sendmailConfigList.size() - 1) + "]");
        }
        return _sendmailConfigList.get(index);
    }

    /**
     * Method getSendmailConfig.Returns the contents of the collection in an Array.
     * <p>Note:  Just in case the collection contents are changing in another thread, we pass a 0-length Array of the correct type into the API 
     * call. This way we <i>know</i> that the Array returned is of exactly the correct length.</p>
     * 
     * @return this collection as an Array
     */
    public SendmailConfig[] getSendmailConfig() {
        SendmailConfig[] array = new SendmailConfig[0];
        return this._sendmailConfigList.toArray(array);
    }

    /**
     * Method getSendmailConfigCollection.Returns a reference to'_sendmailConfigList'. No type checking is performed on any modifications to the Vector.
     * 
     * @return a reference to the Vector backing this class
     */
    public List<SendmailConfig> getSendmailConfigCollection() {
        return this._sendmailConfigList;
    }

    /**
     * Method getSendmailConfigCount.
     * 
     * @return the size of this collection
     */
    public int getSendmailConfigCount() {
        return this._sendmailConfigList.size();
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override()
    public int hashCode() {
        return Objects.hash(_defaultSendConfigName, _defaultReadConfigName, _sendmailConfigList, _readmailConfigList, _end2endMailConfigList);
    }

    /**
     * Method iterateEnd2endMailConfig.
     * 
     * @return an Iterator over all possible elements in this collection
     */
    public Iterator<End2endMailConfig> iterateEnd2endMailConfig() {
        return this._end2endMailConfigList.iterator();
    }

    /**
     * Method iterateReadmailConfig.
     * 
     * @return an Iterator over all possible elements in this collection
     */
    public Iterator<ReadmailConfig> iterateReadmailConfig() {
        return this._readmailConfigList.iterator();
    }

    /**
     * Method iterateSendmailConfig.
     * 
     * @return an Iterator over all possible elements in this collection
     */
    public Iterator<SendmailConfig> iterateSendmailConfig() {
        return this._sendmailConfigList.iterator();
    }

    /**
     * Removes the all end2end mail configuration.
     */
    public void removeAllEnd2endMailConfig() {
        this._end2endMailConfigList.clear();
    }

    /**
     * Removes the all readmail configuration.
     */
    public void removeAllReadmailConfig() {
        this._readmailConfigList.clear();
    }

    /**
     * Removes the all sendmail configuration.
     */
    public void removeAllSendmailConfig() {
        this._sendmailConfigList.clear();
    }

    /**
     * Method removeEnd2endMailConfig.
     *
     * @param vEnd2endMailConfig the end2end mail configuration
     * @return true if the object was removed from the collection.
     */
    public boolean removeEnd2endMailConfig(final End2endMailConfig vEnd2endMailConfig) {
        return _end2endMailConfigList.remove(vEnd2endMailConfig);
    }

    /**
     * Method removeEnd2endMailConfigAt.
     *
     * @param index the index
     * @return the element removed from the collection
     */
    public End2endMailConfig removeEnd2endMailConfigAt(final int index) {
        return this._end2endMailConfigList.remove(index);
    }

    /**
     * Method removeReadmailConfig.
     *
     * @param vReadmailConfig the readmail configuration
     * @return true if the object was removed from the collection.
     */
    public boolean removeReadmailConfig(final ReadmailConfig vReadmailConfig) {
        return _readmailConfigList.remove(vReadmailConfig);
    }

    /**
     * Method removeReadmailConfigAt.
     *
     * @param index the index
     * @return the element removed from the collection
     */
    public ReadmailConfig removeReadmailConfigAt(final int index) {
        return this._readmailConfigList.remove(index);
    }

    /**
     * Method removeSendmailConfig.
     *
     * @param vSendmailConfig the sendmail configuration
     * @return true if the object was removed from the collection.
     */
    public boolean removeSendmailConfig(final SendmailConfig vSendmailConfig) {
        return _sendmailConfigList.remove(vSendmailConfig);
    }

    /**
     * Method removeSendmailConfigAt.
     *
     * @param index the index
     * @return the element removed from the collection
     */
    public SendmailConfig removeSendmailConfigAt(final int index) {
        return this._sendmailConfigList.remove(index);
    }

    /**
     * Sets the value of field 'defaultReadConfigName'.
     * 
     * @param defaultReadConfigName the value of field 'defaultReadConfigName'.
     */
    public void setDefaultReadConfigName(final String defaultReadConfigName) {
        this._defaultReadConfigName = defaultReadConfigName;
    }

    /**
     * Sets the value of field 'defaultSendConfigName'.
     * 
     * @param defaultSendConfigName the value of field 'defaultSendConfigName'.
     */
    public void setDefaultSendConfigName(final String defaultSendConfigName) {
        this._defaultSendConfigName = defaultSendConfigName;
    }

    /**
     * Sets the end2end mail configuration.
     *
     * @param index the index
     * @param vEnd2endMailConfig the v end2end mail configuration
     * @throws IndexOutOfBoundsException if the index given is outside the bounds of the collection
     */
    public void setEnd2endMailConfig(final int index, final End2endMailConfig vEnd2endMailConfig) throws IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this._end2endMailConfigList.size()) {
            throw new IndexOutOfBoundsException("setEnd2endMailConfig: Index value '" + index + "' not in range [0.." + (this._end2endMailConfigList.size() - 1) + "]");
        }
        this._end2endMailConfigList.set(index, vEnd2endMailConfig);
    }

    /**
     * Sets the end2end mail configuration.
     *
     * @param vEnd2endMailConfigArray the new end2end mail configuration
     */
    public void setEnd2endMailConfig(
            final End2endMailConfig[] vEnd2endMailConfigArray) {
        //-- copy array
        _end2endMailConfigList.clear();

        for (int i = 0; i < vEnd2endMailConfigArray.length; i++) {
            this._end2endMailConfigList.add(vEnd2endMailConfigArray[i]);
        }
    }

    /**
     * Sets the value of '_end2endMailConfigList' by copying the given Vector. All elements will be checked for type safety.
     * 
     * @param vEnd2endMailConfigList the Vector to copy.
     */
    public void setEnd2endMailConfig(
            final List<End2endMailConfig> vEnd2endMailConfigList) {
        // copy vector
        this._end2endMailConfigList.clear();

        this._end2endMailConfigList.addAll(vEnd2endMailConfigList);
    }

    /**
     * Sets the value of '_end2endMailConfigList' by setting it to the given Vector. No type checking is performed.
     *
     * @param end2endMailConfigList the Vector to set.
     * @deprecated 
     */
    public void setEnd2endMailConfigCollection(
            final List<End2endMailConfig> end2endMailConfigList) {
        this._end2endMailConfigList = end2endMailConfigList == null? new ArrayList<>() : end2endMailConfigList;
    }

    /**
     * Sets the readmail configuration.
     *
     * @param index the index
     * @param vReadmailConfig the readmail configuration
     * @throws IndexOutOfBoundsException if the index given is outside the bounds of the collection
     */
    public void setReadmailConfig( final int index, final ReadmailConfig vReadmailConfig) throws IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this._readmailConfigList.size()) {
            throw new IndexOutOfBoundsException("setReadmailConfig: Index value '" + index + "' not in range [0.." + (this._readmailConfigList.size() - 1) + "]");
        }
        this._readmailConfigList.set(index, vReadmailConfig);
    }

    /**
     * Sets the readmail configuration.
     *
     * @param vReadmailConfigArray the new readmail configuration
     */
    public void setReadmailConfig(final ReadmailConfig[] vReadmailConfigArray) {
        //-- copy array
        _readmailConfigList.clear();
        for (int i = 0; i < vReadmailConfigArray.length; i++) {
            this._readmailConfigList.add(vReadmailConfigArray[i]);
        }
    }

    /**
     * Sets the value of '_readmailConfigList' by copying the given Vector. All elements will be checked for type safety.
     * 
     * @param vReadmailConfigList the Vector to copy.
     */
    public void setReadmailConfig(final List<ReadmailConfig> vReadmailConfigList) {
        // copy vector
        this._readmailConfigList.clear();
        this._readmailConfigList.addAll(vReadmailConfigList);
    }

    /**
     * Sets the value of '_readmailConfigList' by setting it to the given Vector. No type checking is performed.
     *
     * @param readmailConfigList the Vector to set.
     * @deprecated 
     */
    public void setReadmailConfigCollection(final List<ReadmailConfig> readmailConfigList) {
        this._readmailConfigList = readmailConfigList == null? new ArrayList<>() : readmailConfigList;
    }

    /**
     * Sets the sendmail configuration.
     *
     * @param index the index
     * @param vSendmailConfig the sendmail configuration
     * @throws IndexOutOfBoundsException if the index given is outside the bounds of the collection
     */
    public void setSendmailConfig(final int index, final SendmailConfig vSendmailConfig) throws IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this._sendmailConfigList.size()) {
            throw new IndexOutOfBoundsException("setSendmailConfig: Index value '" + index + "' not in range [0.." + (this._sendmailConfigList.size() - 1) + "]");
        }
        this._sendmailConfigList.set(index, vSendmailConfig);
    }

    /**
     * Sets the sendmail configuration.
     *
     * @param vSendmailConfigArray the new sendmail configuration
     */
    public void setSendmailConfig(final SendmailConfig[] vSendmailConfigArray) {
        //-- copy array
        _sendmailConfigList.clear();
        for (int i = 0; i < vSendmailConfigArray.length; i++) {
            this._sendmailConfigList.add(vSendmailConfigArray[i]);
        }
    }

    /**
     * Sets the value of '_sendmailConfigList' by copying the given Vector. All elements will be checked for type safety.
     * 
     * @param vSendmailConfigList the Vector to copy.
     */
    public void setSendmailConfig(final List<SendmailConfig> vSendmailConfigList) {
        // copy vector
        this._sendmailConfigList.clear();
        this._sendmailConfigList.addAll(vSendmailConfigList);
    }

    /**
     * Sets the value of '_sendmailConfigList' by setting it to the given Vector. No type checking is performed.
     *
     * @param sendmailConfigList the Vector to set.
     * @deprecated 
     */
    public void setSendmailConfigCollection(final List<SendmailConfig> sendmailConfigList) {
        this._sendmailConfigList = sendmailConfigList == null? new ArrayList<>() : sendmailConfigList;
    }

}

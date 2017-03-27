/*******************************************************************************
 * This file is part of OpenNMS(R).
 * 
 * Copyright (C) 2017-2017 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2017 The OpenNMS Group, Inc.
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
 *     http://www.gnu.org/licenses/
 * 
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.config.syslogd;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Class SyslogdConfiguration.
 * 
 * @version $Revision$ $Date$
 */
@XmlRootElement(name = "syslogd-configuration")
@XmlAccessorType(XmlAccessType.FIELD)
public class SyslogdConfiguration implements java.io.Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * Top-level element for the syslogd-configuration.xml
     *  configuration file.
     *  
     */
    @XmlElement(name = "configuration", required = true)
    private Configuration configuration;

    @XmlElement(name = "ueiList")
    private UeiList ueiList;

    @XmlElement(name = "hideMessage")
    private HideMessage hideMessage;

    @XmlElement(name = "import-file")
    private List<String> importFileList = new ArrayList<>();

    /**
     * 
     * 
     * @param vImportFile
     * @throws IndexOutOfBoundsException if the index given is outside
     * the bounds of the collection
     */
    public void addImportFile(final String vImportFile) throws IndexOutOfBoundsException {
        this.importFileList.add(vImportFile);
    }

    /**
     * 
     * 
     * @param index
     * @param vImportFile
     * @throws IndexOutOfBoundsException if the index given is outside
     * the bounds of the collection
     */
    public void addImportFile(final int index, final String vImportFile) throws IndexOutOfBoundsException {
        this.importFileList.add(index, vImportFile);
    }

    /**
     * Method enumerateImportFile.
     * 
     * @return an Enumeration over all possible elements of this collection
     */
    public Enumeration<String> enumerateImportFile() {
        return Collections.enumeration(this.importFileList);
    }

    /**
     * Overrides the Object.equals method.
     * 
     * @param obj
     * @return true if the objects are equal.
     */
    @Override
    public boolean equals(final Object obj) {
        if ( this == obj ) {
            return true;
        }
        
        if (obj instanceof SyslogdConfiguration) {
            SyslogdConfiguration temp = (SyslogdConfiguration)obj;
            boolean equals = Objects.equals(temp.configuration, configuration)
                && Objects.equals(temp.ueiList, ueiList)
                && Objects.equals(temp.hideMessage, hideMessage)
                && Objects.equals(temp.importFileList, importFileList);
            return equals;
        }
        return false;
    }

    /**
     * Returns the value of field 'configuration'. The field 'configuration' has
     * the following description: Top-level element for the
     * syslogd-configuration.xml
     *  configuration file.
     *  
     * 
     * @return the value of field 'Configuration'.
     */
    public Configuration getConfiguration() {
        return this.configuration;
    }

    /**
     * Returns the value of field 'hideMessage'.
     * 
     * @return the value of field 'HideMessage'.
     */
    public HideMessage getHideMessage() {
        return this.hideMessage;
    }

    /**
     * Method getImportFile.
     * 
     * @param index
     * @throws IndexOutOfBoundsException if the index given is outside
     * the bounds of the collection
     * @return the value of the String at the given index
     */
    public String getImportFile(final int index) throws IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this.importFileList.size()) {
            throw new IndexOutOfBoundsException("getImportFile: Index value '" + index + "' not in range [0.." + (this.importFileList.size() - 1) + "]");
        }
        
        return (String) importFileList.get(index);
    }

    /**
     * Method getImportFile.Returns the contents of the collection in an Array. 
     * <p>Note:  Just in case the collection contents are changing in another
     * thread, we pass a 0-length Array of the correct type into the API call. 
     * This way we <i>know</i> that the Array returned is of exactly the correct
     * length.
     * 
     * @return this collection as an Array
     */
    public String[] getImportFile() {
        String[] array = new String[0];
        return (String[]) this.importFileList.toArray(array);
    }

    /**
     * Method getImportFileCollection.Returns a reference to 'importFileList'. No
     * type checking is performed on any modifications to the Vector.
     * 
     * @return a reference to the Vector backing this class
     */
    public List<String> getImportFileCollection() {
        return this.importFileList;
    }

    /**
     * Method getImportFileCount.
     * 
     * @return the size of this collection
     */
    public int getImportFileCount() {
        return this.importFileList.size();
    }

    /**
     * Returns the value of field 'ueiList'.
     * 
     * @return the value of field 'UeiList'.
     */
    public UeiList getUeiList() {
        return this.ueiList;
    }

    /**
     * Method hashCode.
     * 
     * @return a hash code value for the object.
     */
    @Override
    public int hashCode() {
        int hash = Objects.hash(
            configuration, 
            ueiList, 
            hideMessage, 
            importFileList);
        return hash;
    }

    /**
     * Method iterateImportFile.
     * 
     * @return an Iterator over all possible elements in this collection
     */
    public Iterator<String> iterateImportFile() {
        return this.importFileList.iterator();
    }

    /**
     */
    public void removeAllImportFile() {
        this.importFileList.clear();
    }

    /**
     * Method removeImportFile.
     * 
     * @param vImportFile
     * @return true if the object was removed from the collection.
     */
    public boolean removeImportFile(final String vImportFile) {
        boolean removed = importFileList.remove(vImportFile);
        return removed;
    }

    /**
     * Method removeImportFileAt.
     * 
     * @param index
     * @return the element removed from the collection
     */
    public String removeImportFileAt(final int index) {
        Object obj = this.importFileList.remove(index);
        return (String) obj;
    }

    /**
     * Sets the value of field 'configuration'. The field 'configuration' has the
     * following description: Top-level element for the syslogd-configuration.xml
     *  configuration file.
     *  
     * 
     * @param configuration the value of field 'configuration'.
     */
    public void setConfiguration(final Configuration configuration) {
        this.configuration = configuration;
    }

    /**
     * Sets the value of field 'hideMessage'.
     * 
     * @param hideMessage the value of field 'hideMessage'.
     */
    public void setHideMessage(final HideMessage hideMessage) {
        this.hideMessage = hideMessage;
    }

    /**
     * 
     * 
     * @param index
     * @param vImportFile
     * @throws IndexOutOfBoundsException if the index given is outside
     * the bounds of the collection
     */
    public void setImportFile(final int index, final String vImportFile) throws IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this.importFileList.size()) {
            throw new IndexOutOfBoundsException("setImportFile: Index value '" + index + "' not in range [0.." + (this.importFileList.size() - 1) + "]");
        }
        
        this.importFileList.set(index, vImportFile);
    }

    /**
     * 
     * 
     * @param vImportFileArray
     */
    public void setImportFile(final String[] vImportFileArray) {
        //-- copy array
        importFileList.clear();
        
        for (int i = 0; i < vImportFileArray.length; i++) {
                this.importFileList.add(vImportFileArray[i]);
        }
    }

    /**
     * Sets the value of 'importFileList' by copying the given Vector. All
     * elements will be checked for type safety.
     * 
     * @param vImportFileList the Vector to copy.
     */
    public void setImportFile(final List<String> vImportFileList) {
        // copy vector
        this.importFileList.clear();
        
        this.importFileList.addAll(vImportFileList);
    }

    /**
     * Sets the value of 'importFileList' by setting it to the given Vector. No
     * type checking is performed.
     * @deprecated
     * 
     * @param importFileList the Vector to set.
     */
    public void setImportFileCollection(final List<String> importFileList) {
        this.importFileList = importFileList;
    }

    /**
     * Sets the value of field 'ueiList'.
     * 
     * @param ueiList the value of field 'ueiList'.
     */
    public void setUeiList(final UeiList ueiList) {
        this.ueiList = ueiList;
    }

}

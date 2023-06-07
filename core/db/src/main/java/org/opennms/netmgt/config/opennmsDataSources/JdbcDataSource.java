/*******************************************************************************
 * This file is part of OpenNMS(R).
 * 
 * Copyright (C) 2017-2023 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2023 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.config.opennmsDataSources;


import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Objects;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.opennms.core.mate.api.Interpolator;
import org.opennms.core.mate.api.SecureCredentialsVaultScope;
import org.opennms.features.scv.api.SecureCredentialsVault;
import org.opennms.features.scv.jceks.JCEKSSecureCredentialsVault;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.swagger.v3.oas.annotations.Hidden;

/**
 * Top-level element for the opennms-database.xml configuration
 *  file.
 * 
 * @version $Revision$ $Date$
 */
@XmlRootElement(name = "jdbc-data-source")
@XmlAccessorType(XmlAccessType.NONE)
public class JdbcDataSource implements java.io.Serializable {
    private static final long serialVersionUID = -1120653287571635877L;
    private static final Logger LOG = LoggerFactory.getLogger(JdbcDataSource.class);

    private static final String KEYSTORE_KEY_PROPERTY = "org.opennms.features.scv.jceks.key";
    private static final String DEFAULT_KEYSTORE_KEY = "QqSezYvBtk2gzrdpggMHvt5fJGWCdkRw";

    @XmlAttribute(name = "name", required = true)
    private String name;

    @XmlAttribute(name = "database-name")
    private String databaseName;

    @XmlAttribute(name = "schema-name")
    private String schemaName;

    @XmlAttribute(name = "url", required = true)
    private String url;

    @XmlAttribute(name = "class-name", required = true)
    private String className;

    @XmlAttribute(name = "user-name")
    private String rawUserName;

    @XmlAttribute(name = "password")
    private String rawPassword;

    @XmlElement(name = "param")
    private java.util.List<org.opennms.netmgt.config.opennmsDataSources.Param> paramList;

    private transient String keystorePassword;
    private transient String keystoreFilename;

    public JdbcDataSource() {
        this.paramList = new java.util.ArrayList<>();
    }

    /**
     * 
     * 
     * @param vParam
     * @throws IndexOutOfBoundsException if the index given is outside
     * the bounds of the collection
     */
    public void addParam(final org.opennms.netmgt.config.opennmsDataSources.Param vParam) throws IndexOutOfBoundsException {
        this.paramList.add(vParam);
    }

    /**
     * 
     * 
     * @param index
     * @param vParam
     * @throws IndexOutOfBoundsException if the index given is outside
     * the bounds of the collection
     */
    public void addParam(final int index, final org.opennms.netmgt.config.opennmsDataSources.Param vParam) throws IndexOutOfBoundsException {
        this.paramList.add(index, vParam);
    }

    /**
     * Method enumerateParam.
     * 
     * @return an Enumeration over all possible elements of this collection
     */
    public java.util.Enumeration<org.opennms.netmgt.config.opennmsDataSources.Param> enumerateParam() {
        return java.util.Collections.enumeration(this.paramList);
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
        
        if (obj instanceof JdbcDataSource) {
            JdbcDataSource temp = (JdbcDataSource)obj;
            return Objects.equals(temp.name, name)
                && Objects.equals(temp.databaseName, databaseName)
                && Objects.equals(temp.schemaName, schemaName)
                && Objects.equals(temp.url, url)
                && Objects.equals(temp.className, className)
                && Objects.equals(temp.rawUserName, rawUserName)
                && Objects.equals(temp.rawPassword, rawPassword)
                && Objects.equals(temp.paramList, paramList);
        }
        return false;
    }

    /**
     * Returns the value of field 'className'.
     * 
     * @return the value of field 'ClassName'.
     */
    public String getClassName() {
        return this.className;
    }

    /**
     * Returns the value of field 'databaseName'.
     * 
     * @return the value of field 'DatabaseName'.
     */
    public String getDatabaseName() {
        return databaseName != null ? databaseName : "opennms";
    }

    /**
     * Returns the value of field 'name'.
     * 
     * @return the value of field 'Name'.
     */
    public String getName() {
        return this.name;
    }

    /**
     * Method getParam.
     * 
     * @param index
     * @throws IndexOutOfBoundsException if the index given is outside
     * the bounds of the collection
     * @return the value of the org.opennms.netmgt.config.opennmsDataSources.Param
     * at the given index
     */
    public org.opennms.netmgt.config.opennmsDataSources.Param getParam(final int index) throws IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this.paramList.size()) {
            throw new IndexOutOfBoundsException("getParam: Index value '" + index + "' not in range [0.." + (this.paramList.size() - 1) + "]");
        }
        
        return paramList.get(index);
    }

    /**
     * Method getParam.Returns the contents of the collection in an Array. 
     * <p>Note:  Just in case the collection contents are changing in another
     * thread, we pass a 0-length Array of the correct type into the API call. 
     * This way we <i>know</i> that the Array returned is of exactly the correct
     * length.
     * 
     * @return this collection as an Array
     */
    public org.opennms.netmgt.config.opennmsDataSources.Param[] getParam() {
        org.opennms.netmgt.config.opennmsDataSources.Param[] array = new org.opennms.netmgt.config.opennmsDataSources.Param[0];
        return this.paramList.toArray(array);
    }

    /**
     * Method getParamCollection.Returns a reference to 'paramList'. No type
     * checking is performed on any modifications to the Vector.
     * 
     * @return a reference to the Vector backing this class
     */
    public java.util.List<org.opennms.netmgt.config.opennmsDataSources.Param> getParamCollection() {
        return this.paramList;
    }

    /**
     * Method getParamCount.
     * 
     * @return the size of this collection
     */
    public int getParamCount() {
        return this.paramList.size();
    }

    /**
     * Returns the value of field 'password'.
     * 
     * @return the value of field 'Password'.
     */
    public String getRawPassword() {
        return this.rawPassword;
    }

    /**
     * Returns the value of field 'schemaName'.
     * 
     * @return the value of field 'SchemaName'.
     */
    public String getSchemaName() {
        return this.schemaName;
    }

    /**
     * Returns the value of field 'url'.
     * 
     * @return the value of field 'Url'.
     */
    public String getUrl() {
        return this.url;
    }

    /**
     * Returns the value of field 'userName'.
     * 
     * @return the value of field 'UserName'.
     */
    public String getRawUserName() {
        return this.rawUserName;
    }

    /**
     * Method hashCode.
     * 
     * @return a hash code value for the object.
     */
    public int hashCode() {
        return Objects.hash(
            name, 
            databaseName, 
            schemaName, 
            url, 
            className, 
            rawUserName,
            rawPassword,
            paramList);
    }

    /**
     * Method iterateParam.
     * 
     * @return an Iterator over all possible elements in this collection
     */
    public java.util.Iterator<org.opennms.netmgt.config.opennmsDataSources.Param> iterateParam() {
        return this.paramList.iterator();
    }

    /**
     */
    public void removeAllParam() {
        this.paramList.clear();
    }

    /**
     * Method removeParam.
     * 
     * @param vParam
     * @return true if the object was removed from the collection.
     */
    public boolean removeParam(final org.opennms.netmgt.config.opennmsDataSources.Param vParam) {
        return paramList.remove(vParam);
    }

    /**
     * Method removeParamAt.
     * 
     * @param index
     * @return the element removed from the collection
     */
    public org.opennms.netmgt.config.opennmsDataSources.Param removeParamAt(final int index) {
        Object obj = this.paramList.remove(index);
        return (org.opennms.netmgt.config.opennmsDataSources.Param) obj;
    }

    /**
     * Sets the value of field 'className'.
     * 
     * @param className the value of field 'className'.
     */
    public void setClassName(final String className) {
        this.className = className;
    }

    /**
     * Sets the value of field 'databaseName'.
     * 
     * @param databaseName the value of field 'databaseName'.
     */
    public void setDatabaseName(final String databaseName) {
        this.databaseName = databaseName;
    }

    /**
     * Sets the value of field 'name'.
     * 
     * @param name the value of field 'name'.
     */
    public void setName(final String name) {
        this.name = name;
    }

    /**
     * 
     * @deprecated
     * @param index
     * @param vParam
     * @throws IndexOutOfBoundsException if the index given is outside
     * the bounds of the collection
     */
    @Hidden
    public void setParam(final int index, final org.opennms.netmgt.config.opennmsDataSources.Param vParam) throws IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this.paramList.size()) {
            throw new IndexOutOfBoundsException("setParam: Index value '" + index + "' not in range [0.." + (this.paramList.size() - 1) + "]");
        }
        
        this.paramList.set(index, vParam);
    }

    /**
     * 
     * @deprecated
     * @param vParamArray
     */
    @Hidden
    public void setParam(final org.opennms.netmgt.config.opennmsDataSources.Param[] vParamArray) {
        //-- copy array
        paramList.clear();
        
        for (int i = 0; i < vParamArray.length; i++) {
                this.paramList.add(vParamArray[i]);
        }
    }

    /**
     * Sets the value of 'paramList' by copying the given Vector. All elements
     * will be checked for type safety.
     * 
     * @param vParamList the Vector to copy.
     */
    public void setParam(final java.util.List<org.opennms.netmgt.config.opennmsDataSources.Param> vParamList) {
        // copy vector
        this.paramList.clear();
        
        this.paramList.addAll(vParamList);
    }

    /**
     * Sets the value of 'paramList' by setting it to the given Vector. No type
     * checking is performed.
     * @deprecated
     * 
     * @param paramList the Vector to set.
     */
    @Hidden
    public void setParamCollection(final java.util.List<org.opennms.netmgt.config.opennmsDataSources.Param> paramList) {
        this.paramList = paramList;
    }

    /**
     * Sets the value of field 'rawPassword'.
     * 
     * @param rawPassword the value of field 'rawPassword'.
     */
    public void setPassword(final String rawPassword) {
        this.rawPassword = rawPassword;
    }

    /**
     * Sets the value of field 'schemaName'.
     * 
     * @param schemaName the value of field 'schemaName'.
     */
    public void setSchemaName(final String schemaName) {
        this.schemaName = schemaName;
    }

    /**
     * Sets the value of field 'url'.
     * 
     * @param url the value of field 'url'.
     */
    public void setUrl(final String url) {
        this.url = url;
    }

    /**
     * Sets the value of field 'rawUserName'.
     * 
     * @param rawUserName the value of field 'rawUserName'.
     */
    public void setUserName(final String rawUserName) {
        this.rawUserName = rawUserName;
    }

    protected String getKeystoreFilename() {
        if (this.keystoreFilename == null) {
            var opennmsHome = System.getProperty("opennms.home");
            if (opennmsHome == null) {
                try {
                    LOG.warn("opennms.home is not set; using a temporary directory for scv keystore. This is very likely not what you want.");
                    opennmsHome = Files.createTempDirectory("opennms-home-").toString();
                } catch (final IOException e) {
                    throw new IllegalStateException("Unable to create a temporary scv keystore home!", e);
                }
            }
            this.keystoreFilename = Paths.get(opennmsHome, "etc", "scv.jce").toString();
        }
        return this.keystoreFilename;
    }

    protected String getKeystorePassword() {
        if (this.keystorePassword == null) {
            this.keystorePassword = System.getProperty(KEYSTORE_KEY_PROPERTY, DEFAULT_KEYSTORE_KEY);
        }
        return this.keystorePassword;
    }

    public String interpolateAttribute(final String value) {
        return interpolateAttribute(value, this.getKeystoreFilename(), this.getKeystorePassword());
    }

    public String interpolateAttribute(final String value, final String keystoreFile, final String password) {
        final SecureCredentialsVault secureCredentialsVault = new JCEKSSecureCredentialsVault(keystoreFile, password);
        final Interpolator.Result result = Interpolator.interpolate(value, new SecureCredentialsVaultScope(secureCredentialsVault));
        return result.output;
    }

    public String getUserName() {
        return interpolateAttribute(getRawUserName());
    }

    public String getPassword() {
        return interpolateAttribute(getRawPassword());
    }
}

/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.opennms.netmgt.config.collectd.jmx;

import java.util.Collections;
import java.util.Objects;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

@XmlRootElement(name="mbean")
@XmlAccessorType(XmlAccessType.FIELD)
@SuppressWarnings("all")
public class Mbean implements java.io.Serializable {

    @XmlAttribute(name="name", required=true)
    private String _name;

    @XmlAttribute(name="objectname", required=true)
    private String _objectname;

    @XmlAttribute(name="keyfield")
    private String _keyfield;

    @XmlAttribute(name="exclude")
    private String _exclude;

    @XmlAttribute(name="key-alias")
    private String _keyAlias;

    @XmlAttribute(name="resource-type")
    private String resourceType;

    @XmlElement(name="attrib")
    private java.util.List<Attrib> _attribList = new java.util.ArrayList<>();

    @XmlTransient
    private java.util.List<String> _includeMbeanList = new java.util.ArrayList<>();

    @XmlElement(name="comp-attrib")
    private java.util.List<CompAttrib> _compAttribList = new java.util.ArrayList<>();

    public void addAttrib(final Attrib vAttrib) {
        this._attribList.add(vAttrib);
    }

    public void addCompAttrib(final CompAttrib vCompAttrib) {
        this._compAttribList.add(vCompAttrib);
    }

    @Override
    public boolean equals(final Object obj) {
        if ( this == obj )
            return true;

        if (obj instanceof Mbean) {
            Mbean temp = (Mbean)obj;

            boolean equals = Objects.equals(_name, temp._name)
                    && Objects.equals(_objectname, temp._objectname)
                    && Objects.equals(_keyfield, temp._keyfield)
                    && Objects.equals(_exclude, temp._exclude)
                    && Objects.equals(_keyAlias, temp._keyAlias)
                    && Objects.equals(_attribList, temp._attribList)
                    && Objects.equals(_includeMbeanList, temp._includeMbeanList)
                    && Objects.equals(_compAttribList, temp._compAttribList)
                    && Objects.equals(resourceType, temp.resourceType);
            return equals;
        }
        return false;
    }

    public java.util.List<Attrib> getAttribList() {
        return Collections.unmodifiableList(this._attribList);
    }

    /**
     * @return the size of this collection
     */
    public int getAttribCount() {
        return this._attribList.size();
    }

    public java.util.List<CompAttrib> getCompAttribList() {
        return Collections.unmodifiableList(this._compAttribList);
    }

    /**
     * @return the size of this collection
     */
    public int getCompAttribCount() {
        return this._compAttribList.size();
    }

    public String getExclude( ) {
        return this._exclude;
    }


    public java.util.List<String> getIncludeMbeanCollection() {
        return this._includeMbeanList;
    }

    public int getIncludeMbeanCount() {
        return this._includeMbeanList.size();
    }

    public String getKeyAlias() {
        return this._keyAlias;
    }

    public String getKeyfield() {
        return this._keyfield;
    }

    public String getName() {
        return this._name;
    }

    public String getObjectname() {
        return this._objectname;
    }

    @Override
    public int hashCode() {
        return Objects.hash(_name, _objectname, _keyfield, _exclude, _keyAlias, _attribList, _includeMbeanList, _compAttribList, resourceType);
    }

    public void setAttribCollection(final java.util.List<Attrib> attribList) {
        this._attribList = attribList;
    }


    public void setCompAttribCollection(final java.util.List<CompAttrib> compAttribList) {
        this._compAttribList = compAttribList;
    }

    public void setExclude( final String exclude) {
        this._exclude = exclude;
    }

    public void setIncludeMbeanCollection(final java.util.List<String> includeMbeanList) {
        this._includeMbeanList = includeMbeanList;
    }

    public void setKeyAlias(final String keyAlias) {
        this._keyAlias = keyAlias;
    }

    public void setKeyfield(final String keyfield) {
        this._keyfield = keyfield;
    }

    public void setName(final String name) {
        this._name = name;
    }

    public void setObjectname(final String objectname) {
        this._objectname = objectname;
    }

    public String getResourceType() {
        return resourceType;
    }

    public void setResourceType(String resourceType) {
        this.resourceType = resourceType;
    }

    public void clearAttribs() {
        this._attribList.clear();        
    }

    public void clearCompAttribs() {
        this._compAttribList.clear();
    }
}

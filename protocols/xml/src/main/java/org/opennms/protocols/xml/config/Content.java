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
package org.opennms.protocols.xml.config;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlValue;

/**
 * The Class Content.
 * 
 * <p>Post a Form:</p>
 * <pre>
 *   &lt;content type='application/x-www-form-urlencoded'&gt;&lt;![CDATA[
 *     &lt;form-fields&gt;
 *       &lt;form-field name='firstName'&gt;Alejandro&lt;/form-field&gt;
 *       &lt;form-field name='lastName'&gt;Galue&lt;/form-field&gt;
 *     &lt;/form-fields&gt;
 *   ]]&gt;&lt;/content&gt;
 * </pre>
 * 
 * <p>Post a JSON Object:</p>
 * <pre>
 *   &lt;content type='application/json'&gt;&lt;![CDATA[
 *     {
 *       person: {
 *         firstName: 'Alejandro',
 *         lastName: 'Galue'
 *       }
 *     }
 *   ]]&gt;&lt;/content&gt;
 * </pre>
 * 
 * <p>Post a XML:</p>
 * <pre>
 *   &lt;content type='application/xml'&gt;&lt;![CDATA[
 *     &lt;person&gt;
 *       &lt;firstName&gt;Alejandro&lt;/firstName&gt;
 *       &lt;lastName&gt;Galue&lt;/lastName&gt;
 *     &lt;/person&gt;
 *   ]]&gt;&lt;/content&gt;
 * </pre>
 * 
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a>
 */
@XmlRootElement(name="content")
@XmlAccessorType(XmlAccessType.FIELD)
public class Content implements Cloneable {

    /** The type. */
    @XmlAttribute(required=true)
    private String type;

    /** The data.
     *  <p>In order to put any arbitrary XML content, CDATA is required.</p>
     */
    @XmlValue
    private String data;

    /**
     * Instantiates a new content.
     */
    public Content() {}

    /**
     * Instantiates a new content.
     *
     * @param type the type
     * @param data the data
     */
    public Content(String type, String data) {
        this.type = type;
        this.data = data;
    }

    public Content(Content copy) {
        type = copy.type;
        data = copy.data;
    }

    /**
     * Gets the type.
     *
     * @return the type
     */
    public String getType() {
        return type;
    }

    /**
     * Sets the type.
     *
     * @param type the new type
     */
    public void setType(String type) {
        this.type = type;
    }

    /**
     * Gets the data.
     *
     * @return the data
     */
    public String getData() {
        return data;
    }

    /**
     * Sets the data.
     *
     * @param data the new data
     */
    public void setData(String data) {
        this.data = data;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "Content [type=" + type + ", data=" + data + "]";
    }

    @Override
    public Content clone() {
        return new Content(this);
    }
}

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
package org.opennms.protocols.http;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.message.BasicNameValuePair;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.annotate.JsonRootName;
import org.opennms.core.config.api.JaxbListWrapper;

/**
 * The Class FormFields.
 *
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a>
 */
@XmlRootElement(name="form-fields")
@JsonRootName("form-fields")
public class FormFields extends JaxbListWrapper<FormField> {

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /**
     * The Constructor.
     */
    public FormFields() { super(); }

    /**
     * The Constructor.
     *
     * @param fields the fields
     */
    public FormFields(final Collection<? extends FormField> fields) {
        super(fields);
    }

    /* (non-Javadoc)
     * @see org.opennms.core.config.api.JaxbListWrapper#getObjects()
     */
    @XmlElement(name="form-field")
    @JsonProperty("form-field")
    public List<FormField> getObjects() {
        return super.getObjects();
    }

    /**
     * Gets the entity.
     *
     * @return the entity
     * @throws UnsupportedEncodingException the unsupported encoding exception
     */
    @XmlTransient
    public UrlEncodedFormEntity getEntity() throws UnsupportedEncodingException {
        List<NameValuePair> nvps = new ArrayList<>();
        for (FormField field : this) {
            nvps.add(new BasicNameValuePair(field.getName(), field.getValue()));
        }
        return new UrlEncodedFormEntity(nvps, StandardCharsets.UTF_8);
    }

}

/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2011-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
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

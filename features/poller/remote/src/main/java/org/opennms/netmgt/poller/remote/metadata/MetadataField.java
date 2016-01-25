/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2016 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.poller.remote.metadata;

import java.io.Serializable;
import java.util.Objects;

public class MetadataField implements Serializable {
    private static final long serialVersionUID = 1L;
    private String m_key;
    private String m_description;
    private Validator m_validator;
    private boolean m_required;

    public MetadataField() {
    }

    public MetadataField(final String key, final String description, final Validator validator, final boolean required) {
        if (key == null || key.trim().isEmpty()) {
            throw new IllegalArgumentException("Key is required!");
        }
        if (description == null || description.trim().isEmpty()) {
            throw new IllegalArgumentException("Metadata field '" + key + "' is missing a description!");
        }
        m_key = key;
        m_description = description;
        m_validator = validator;
        m_required = required;
    }

    public String getKey() {
        return m_key;
    }

    public void setKey(final String key) {
        m_key = key;
    }

    public String getDescription() {
        return m_description;
    }

    public void setDescription(final String description) {
        m_description = description;
    }

    public Validator getValidator() {
        return m_validator;
    }

    public void setValidator(final Validator validator) {
        m_validator = validator;
    }

    public boolean isRequired() {
        return m_required;
    }

    public void setRequired(final boolean required) {
        m_required = required;
    }

    public interface Validator extends Serializable {
        public boolean isValid(final String value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(m_description, m_key, m_required, m_validator);
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof MetadataField)) {
            return false;
        }
        final MetadataField other = (MetadataField) obj;
        return Objects.equals(m_description, other.m_description) &&
                Objects.equals(m_key, other.m_key) &&
                Objects.equals(m_required, other.m_required) &&
                Objects.equals(m_validator, other.m_validator);
    }
}

/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.model;

import java.io.Serializable;
import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.annotate.JsonRootName;

@XmlRootElement(name="meta-data")
@JsonRootName("metaData")
@Embeddable
public class OnmsMetaData implements Serializable {

    private static final long serialVersionUID = 3529745790145204662L;

    private String context;
    private String key;
    private String value;

    public OnmsMetaData() {
    }

    public OnmsMetaData(String context, String key, String value) {
        this.context = Objects.requireNonNull(context);
        this.key = Objects.requireNonNull(key);
        this.value = Objects.requireNonNull(value);
    }

    @JsonProperty("context")
    @XmlElement(name="context")
    @Column(name="context", nullable = false)
    public String getContext(){
        return context;
    }

    public void setContext(String context) {
        this.context = context;
    }

    @JsonProperty("key")
    @XmlElement(name="key")
    @Column(name="key", nullable = false)
    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    @JsonProperty("value")
    @XmlElement(name="value")
    @Column(name="value", nullable = false)
    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return com.google.common.base.Objects.toStringHelper(this)
                .add("context", context)
                .add("key", key)
                .add("value", value)
                .toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OnmsMetaData that = (OnmsMetaData) o;
        return com.google.common.base.Objects.equal(context, that.context) &&
                com.google.common.base.Objects.equal(key, that.key) &&
                com.google.common.base.Objects.equal(value, that.value);
    }

    @Override
    public int hashCode() {
        return com.google.common.base.Objects.hashCode(context, key, value);
    }
}

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
        return com.google.common.base.MoreObjects.toStringHelper(this)
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

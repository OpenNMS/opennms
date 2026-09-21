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
package org.opennms.netmgt.config.tokenauth;

import java.io.Serializable;
import java.util.Objects;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlValue;

/**
 * Optional request body for the auth call. The {@code type} attribute
 * carries the Content-Type used for the body.
 */
@XmlRootElement(name = "content")
@XmlAccessorType(XmlAccessType.FIELD)
public class Content implements Serializable {
    private static final long serialVersionUID = 1L;

    @XmlAttribute(name = "type", required = true)
    private String type;

    @XmlValue
    private String data;

    public Content() {
    }

    public Content(final String type, final String data) {
        this.type = type;
        this.data = data;
    }

    public String getType() {
        return type;
    }

    public void setType(final String type) {
        this.type = type;
    }

    public String getData() {
        return data;
    }

    public void setData(final String data) {
        this.data = data;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (!(o instanceof Content)) return false;
        final Content other = (Content) o;
        return Objects.equals(type, other.type) && Objects.equals(data, other.data);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, data);
    }
}

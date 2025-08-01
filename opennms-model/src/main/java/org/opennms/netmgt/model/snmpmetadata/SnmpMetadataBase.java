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
package org.opennms.netmgt.model.snmpmetadata;

import javax.xml.bind.annotation.XmlTransient;
import java.util.Objects;

public abstract class SnmpMetadataBase {
    private SnmpMetadataBase parent;
    private final int id;
    private static int counter = 0;

    protected SnmpMetadataBase() {
        this.id = counter++;
    }

    @XmlTransient
    public SnmpMetadataBase getParent() {
        return parent;
    }

    public void setParent(final SnmpMetadataBase parent) {
        this.parent = parent;
    }

    protected String trimName(final String name) {
        final String arr[] = name.split("\\.");
        return arr[arr.length - 1];
    }

    public int getId() {
        return id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SnmpMetadataBase that = (SnmpMetadataBase) o;
        return id == that.id && Objects.equals(parent, that.parent);
    }

    @Override
    public int hashCode() {
        return Objects.hash(parent, id);
    }
}

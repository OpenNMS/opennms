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
package org.opennms.netmgt.telemetry.protocols.netflow.parser.ie.xml;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@XmlRootElement(name = "ipfix-dot-d")
public class IpfixDotD {
    private List<IpfixElements> ipfixElements = new ArrayList<>();

    public List<IpfixElements> getIpfixElements() {
        return ipfixElements;
    }

    public void setIpfixElements(List<IpfixElements> ipfixElements) {
        this.ipfixElements = ipfixElements;
    }

    @Override
    public String toString() {
        return "IpfixDotD{" +
                "ipfixElements=" + ipfixElements +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        IpfixDotD ipfixDotD = (IpfixDotD) o;
        return Objects.equals(ipfixElements, ipfixDotD.ipfixElements);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(ipfixElements);
    }
}

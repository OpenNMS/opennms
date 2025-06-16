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


import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.Objects;

@XmlRootElement(name = "rrd")
@XmlAccessorType(XmlAccessType.FIELD)
@SuppressWarnings("all")
public class Rrd implements java.io.Serializable {

    @XmlAttribute(name = "step", required = true)
    private int _step = 0;

    @XmlElement(name = "rra", required = true)
    private java.util.List<String> _rraList = new java.util.ArrayList<>();

    public void addRra(final String vRra)
            throws IndexOutOfBoundsException {
        this._rraList.add(vRra);
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj)
            return true;

        if (obj instanceof Rrd) {
            Rrd temp = (Rrd) obj;
            boolean equals = Objects.equals(this._step, temp._step)
                    && Objects.equals(this._rraList, temp._rraList);
            return equals;
        }
        return false;
    }

    public java.util.List<String> getRraCollection() {
        return this._rraList;
    }

    public int getRraCount() {
        return this._rraList.size();
    }

    public int getStep() {
        return this._step;
    }

    public boolean hasStep() {
        return this._step > 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(_step, _rraList);
    }

    public void setRraList(final java.util.List<String> rraList) {
        this._rraList = rraList;
    }

    public void setStep(final int step) {
        this._step = step;
    }

}

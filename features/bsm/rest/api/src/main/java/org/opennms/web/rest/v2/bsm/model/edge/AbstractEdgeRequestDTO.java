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
package org.opennms.web.rest.v2.bsm.model.edge;

import java.util.Objects;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

import org.opennms.web.rest.v2.bsm.model.MapFunctionDTO;

@XmlAccessorType(XmlAccessType.NONE)
public abstract class AbstractEdgeRequestDTO {

    @XmlElement(name="map-function")
    private MapFunctionDTO mapFunction;

    @XmlElement(name="weight", required = true)
    private int weight = 1;

    public void setMapFunction(MapFunctionDTO mapFunction) {
        this.mapFunction = mapFunction;
    }

    public void setWeight(int weight) {
        this.weight = weight;
    }

    public int getWeight() {
        return weight;
    }

    public MapFunctionDTO getMapFunction() {
        return mapFunction;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) { return false; }
        if (obj == this) { return true; }
        if (!(obj instanceof AbstractEdgeRequestDTO)) { return false; }
        AbstractEdgeRequestDTO other = (AbstractEdgeRequestDTO) obj;
        return Objects.equals(mapFunction, other.mapFunction)
                && Objects.equals(weight, other.weight);
    }

    @Override
    public int hashCode() {
        return Objects.hash(mapFunction, weight);
    }

    @Override
    public String toString() {
        return com.google.common.base.MoreObjects.toStringHelper(this)
                .add("weight", weight)
                .add("mapFunction", mapFunction)
                .toString();
    }

    public abstract void accept(EdgeRequestDTOVisitor visitor);
}

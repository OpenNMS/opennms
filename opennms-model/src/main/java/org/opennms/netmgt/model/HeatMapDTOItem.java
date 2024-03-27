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

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class HeatMapDTOItem {
    private String id;
    private Integer elementId;
    private List<Double> color = new ArrayList<>();
    private List<Double> size = new ArrayList<>();

    public HeatMapDTOItem() {
    }

    @XmlAttribute(name = "id")
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @XmlAttribute(name = "elementId")
    public Integer getElementId() {
        return elementId;
    }

    public void setElementId(Integer elementId) {
        this.elementId = elementId;
    }

    @XmlElement(name = "colorEntry")
    @XmlElementWrapper(name = "color")
    public List<Double> getColor() {
        return color;
    }

    public void setColor(List<Double> color) {
        this.color = color;
    }

    @XmlElement(name = "sizeEntry")
    @XmlElementWrapper(name = "size")
    public List<Double> getSize() {
        return size;
    }

    public void setSize(List<Double> size) {
        this.size = size;
    }
}
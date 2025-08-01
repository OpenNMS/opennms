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
package org.opennms.features.jmxconfiggenerator.graphs;

/**
 * @author Simon Walter <simon.walter@hp-factory.de>
 * @author Markus Neumann <markus@opennms.com>
 */
public class Graph {
    private String id;
    private String description;
    private String resourceName;
    private String coloreA;
    private String coloreB;
    private String coloreC;

    public Graph(String id, String description, String resourceName, String coloreA, String coloreB, String coloreC) {
        this.id = id;
        this.description = description;
        this.resourceName = resourceName;
        this.coloreA = coloreA;
        this.coloreB = coloreB;
        this.coloreC = coloreC;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getResourceName() {
        return resourceName;
    }

    public void setResourceName(String resourceName) {
        this.resourceName = resourceName;
    }

    public String getColoreA() {
        return coloreA;
    }

    public void setColoreA(String coloreA) {
        this.coloreA = coloreA;
    }

    public String getColoreB() {
        return coloreB;
    }

    public void setColoreB(String coloreB) {
        this.coloreB = coloreB;
    }

    public String getColoreC() {
        return coloreC;
    }

    public void setColoreC(String coloreC) {
        this.coloreC = coloreC;
    }

    @Override
    public String toString() {
        return "Graph{" + "id=" + id + ", description=" + description + ", resourceName=" + resourceName + ", coloreA=" + coloreA + ", coloreB=" + coloreB + ", coloreC=" + coloreC + '}';
    }
}

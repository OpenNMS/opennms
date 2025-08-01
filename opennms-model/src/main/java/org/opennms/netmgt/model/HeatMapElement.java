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

public class HeatMapElement {
    public enum Type {
        Outage,
        Alarm
    }

    private String name;
    private int id;
    private int nodesUp;
    private int nodesTotal;
    private int servicesDown;
    private int servicesTotal;
    private int maximumSeverity;
    private Type type;

    public HeatMapElement(String name, Number id, Number servicesDown, Number servicesTotal, Number nodesUp, Number nodesTotal) {
        this.name = name == null ? "Uncategorized" : name;
        this.id = id == null ? -1 : id.intValue();
        this.servicesDown = servicesDown == null ? 0 : servicesDown.intValue();
        this.servicesTotal = servicesTotal == null ? 0 : servicesTotal.intValue();
        this.nodesUp = nodesUp == null ? 0 : nodesUp.intValue();
        this.nodesTotal = nodesTotal == null ? 0 : nodesTotal.intValue();
        this.type = Type.Outage;
    }

    public HeatMapElement(String name, Number id, Number servicesTotal, Number nodesTotal, Number maximumSeverity) {
        this.name = name == null ? "Uncategorized" : name;
        this.id = id == null ? -1 : id.intValue();
        this.servicesTotal = servicesTotal == null ? 0 : servicesTotal.intValue();
        this.nodesTotal = nodesTotal == null ? 0 : nodesTotal.intValue();
        this.maximumSeverity = maximumSeverity == null ? 0 : maximumSeverity.intValue();
        this.type = Type.Alarm;
    }

    public String getName() {
        return name;
    }

    public int getId() {
        return id;
    }

    public int getNodesUp() {
        return nodesUp;
    }

    public int getNodesDown() {
        return getNodesTotal() - getNodesUp();
    }

    public int getNodesTotal() {
        return nodesTotal;
    }

    public int getServicesUp() {
        return getServicesTotal() - getServicesDown();
    }

    public int getServicesDown() {
        return servicesDown;
    }

    public int getServicesTotal() {
        return servicesTotal;
    }

    public int getMaximumSeverity() {
        return maximumSeverity;
    }

    public Type getType() {
        return type;
    }

    public double getColor() {
        if (type == Type.Alarm) {
            switch (OnmsSeverity.get(getMaximumSeverity())) {
                case WARNING: {
                    return 0.1;
                }
                case MINOR: {
                    return 0.2;
                }
                case MAJOR: {
                    return 0.4;
                }
                case CRITICAL: {
                    return 1.0;
                }
                default: {
                    return 0.0;
                }
            }
        } else {
            if (getServicesTotal() == 0) {
                return 0.0;
            } else {
                return (double) getServicesDown() / (double) getServicesTotal();
            }
        }
    }
}

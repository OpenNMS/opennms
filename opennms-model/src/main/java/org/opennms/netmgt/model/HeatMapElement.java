/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2014 The OpenNMS Group, Inc.
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

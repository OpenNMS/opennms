/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

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

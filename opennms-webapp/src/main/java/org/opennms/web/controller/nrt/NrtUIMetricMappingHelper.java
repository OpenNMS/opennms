/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2011 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2011 The OpenNMS Group, Inc.
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

package org.opennms.web.controller.nrt;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.Vector;

/*
 * @author Christian Pape
 */
public class NrtUIMetricMappingHelper {

    private String m_oid;

    private HashMap<String, HashSet<String>> m_consolidationFunction = new HashMap<String, HashSet<String>>();

    public NrtUIMetricMappingHelper(String oid) {
        this.m_oid = oid;
    }

    public void addTarget(String consolidationFunction, String target) {
        getConsolidationFunction(consolidationFunction).add(target);
    }

    private HashSet<String> getConsolidationFunction(String consolidationFunction) {
        if (!m_consolidationFunction.containsKey(consolidationFunction)) {
            m_consolidationFunction.put(consolidationFunction, new HashSet<String>());
        }
        return m_consolidationFunction.get(consolidationFunction);
    }

    public Set<String> getConsolidationFunctions() {
        return m_consolidationFunction.keySet();
    }

    public Set<String> getTargetsForConsolidationFunction(String consolidationFunction) {
        HashSet<String> targets = new HashSet<String>();
        for (String target : m_consolidationFunction.get(consolidationFunction)) {
            targets.add(target);
        }
        return targets;

    }
}

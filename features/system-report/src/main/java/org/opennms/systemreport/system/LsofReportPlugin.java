/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2010-2022 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2022 The OpenNMS Group, Inc.
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

package org.opennms.systemreport.system;

import org.opennms.systemreport.AbstractSystemReportPlugin;
import org.springframework.core.io.Resource;

import java.util.Map;
import java.util.TreeMap;

public class LsofReportPlugin extends AbstractSystemReportPlugin {
    @Override
    public String getName() {
        return "lsof";
    }

    @Override
    public String getDescription() {
        return "Output of the 'lsof' command (full output only)";
    }

    @Override
    public int getPriority() {
        return 12;
    }

    @Override
    public boolean getFullOutputOnly() {
        return true;
    }

    @Override
    public boolean getOutputsFiles() {
        return true;
    }

    @Override
    public Map<String, Resource> getEntries() {
        final Map<String,Resource> map = new TreeMap<String,Resource>();
        String lsofOutput = null;

        final String lsof = getResourceLocator().findBinary("lsof");

        if (lsof != null) {
            lsofOutput = getResourceLocator().slurpOutput(lsof, false);
        }

        if (lsofOutput != null) {
            map.put("Output", getResource(lsofOutput));
        }

        return map;
    }
}

/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2010-2014 The OpenNMS Group, Inc.
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

package org.opennms.systemreport.system;

import java.io.File;
import java.util.Map;
import java.util.TreeMap;

import org.opennms.systemreport.AbstractSystemReportPlugin;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TopReportPlugin extends AbstractSystemReportPlugin {
    private static final Logger LOG = LoggerFactory.getLogger(TopReportPlugin.class);

    @Override
    public String getName() {
        return "Top";
    }

    @Override
    public String getDescription() {
        return "Output of the 'top' command (full output only)";
    }

    @Override
    public int getPriority() {
        return 11;
    }

    public Map<String, Resource> getEntries() {
        final Map<String,Resource> map = new TreeMap<String,Resource>();
        final String top = getResourceLocator().findBinary("top");

        String topOutput = null;

        if (top != null) {
            topOutput = getResourceLocator().slurpOutput(top + " -h", true);
            LOG.debug("top -h output: {}", topOutput);

            if (topOutput.contains("-b") && topOutput.contains("-n")) {
                final String topcmd = top + " -n 1 -b";
                LOG.trace("calling: {}", topcmd);
                topOutput = getResourceLocator().slurpOutput(topcmd, false);
            } else if (topOutput.contains("-l")) {
                final String topcmd = top + " -l 1";
                LOG.trace("calling: {}", topcmd);
                topOutput = getResourceLocator().slurpOutput(topcmd, false);
            } else if (topOutput.contains("-d count") && topOutput.contains("-J jail")) {
                final String topcmd = top + " -b -d 1";
                LOG.trace("calling: {}", topcmd);
                topOutput = getResourceLocator().slurpOutput(topcmd, false);
            } else {
                topOutput = null;
            }
        }

        if (topOutput != null) {
            File tempFile = createTemporaryFileFromString(topOutput);
            if(tempFile != null) {
                map.put("Output", new FileSystemResource(tempFile));
            }
        }

        return map;
    }
}

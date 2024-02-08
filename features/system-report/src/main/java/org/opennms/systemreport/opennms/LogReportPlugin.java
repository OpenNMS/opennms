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
package org.opennms.systemreport.opennms;

import java.io.File;
import java.util.Map;
import java.util.TreeMap;

import org.opennms.systemreport.AbstractSystemReportPlugin;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

public class LogReportPlugin extends AbstractSystemReportPlugin {

    @Override
    public String getName() {
        return "Logs";
    }

    @Override
    public String getDescription() {
        return "OpenNMS log files (full output only)";
    }

    @Override
    public int getPriority() {
        return 21;
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
        final TreeMap<String,Resource> map = new TreeMap<String,Resource>();
        File f = new File(System.getProperty("opennms.home") + File.separator + "logs");
        processFile(f, map);
        return map;
    }

    public void processFile(final File file, final Map<String,Resource> map) {
        if (file.isDirectory()) {
            for (final File f : file.listFiles()) {
                processFile(f, map);
            }
        } else {
            String filename = file.getPath();
            filename = filename.replaceFirst("^" + System.getProperty("opennms.home") + File.separator + "logs" + File.separator + "?", "");
            if (filename.endsWith(".log") && file.length() > 0) {
                map.put(filename, new FileSystemResource(file));
            }
        }
    }
}

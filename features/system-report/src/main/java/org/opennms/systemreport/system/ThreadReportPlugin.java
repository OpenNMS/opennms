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
package org.opennms.systemreport.system;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.util.Map;
import java.util.TreeMap;

import org.opennms.systemreport.AbstractSystemReportPlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;

public class ThreadReportPlugin extends AbstractSystemReportPlugin {
    private static final Logger LOG = LoggerFactory.getLogger(ThreadReportPlugin.class);
    @Override
    public String getName() {
        return "Threads";
    }

    @Override
    public String getDescription() {
        return "Java thread dump (full output only)";
    }

    @Override
    public int getPriority() {
        return 10;
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

        final StringBuilder sb = new StringBuilder();
        try {
            final ThreadInfo[] threads = ManagementFactory.getThreadMXBean().dumpAllThreads(true, true);
            for (final ThreadInfo info : threads) {
                sb.append(info.toString());
            }

            map.put("ThreadDump.txt", getResource(sb.toString()));
        } catch (final Exception e) {
            LOG.debug("Unable to get thread dump.", e);
        }

        return map;
    }
}

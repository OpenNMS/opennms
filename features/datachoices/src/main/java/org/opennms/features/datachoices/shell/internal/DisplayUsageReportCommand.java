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
package org.opennms.features.datachoices.shell.internal;

import java.util.Objects;

import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.lifecycle.Reference;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.apache.karaf.shell.console.OsgiCommandSupport;
import org.opennms.features.datachoices.internal.usagestatistics.UsageStatisticsReporter;

/**
 * <p>This command implements the Apache Karaf 3 and Apache Karaf 4 shell APIs.
 * Once the Karaf 4 commands work, the deprecated Karaf 3 annotations should 
 * be removed:</p>
 * <ul>
 * <li>{@link org.apache.karaf.shell.commands.Command}</li>
 * <li>{@link org.apache.karaf.shell.console.OsgiCommandSupport}</li>
 * </ul>
 */
@Command(scope = "opennms", name = "datachoices-display-usage-report", description="Displays the usage statistics report.")
@org.apache.karaf.shell.commands.Command(scope = "opennms", name = "datachoices-display-usage-report", description="Displays the usage statistics report.")
@Service
public class DisplayUsageReportCommand extends OsgiCommandSupport implements Action {

    @Reference
    public UsageStatisticsReporter m_usageStatisticsReporter;

    @Override
    public Object execute() throws Exception {
        long then = System.currentTimeMillis();
        String reportAsJson = m_usageStatisticsReporter.generateReport().toJson(true);
        long delta = System.currentTimeMillis() - then;

        System.out.printf("Generated usage statistics reports in %.2f seconds:\n%s\n", delta / 1000f, reportAsJson);
        return null;
    }

    @Override
    @Deprecated
    protected Object doExecute() throws Exception {
        return execute();
    }

    @Deprecated
    public void setUsageStatisticsReporter(UsageStatisticsReporter usageStatisticsReporter) {
        m_usageStatisticsReporter = Objects.requireNonNull(usageStatisticsReporter);
    }
}

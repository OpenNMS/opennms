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
package org.opennms.netmgt.poller.shell;

import java.lang.management.ManagementFactory;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.management.AttributeNotFoundException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.ReflectionException;
import javax.management.openmbean.CompositeData;
import javax.management.openmbean.TabularData;

import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.Option;
import org.apache.karaf.shell.api.action.lifecycle.Reference;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.apache.karaf.shell.api.console.Terminal;
import org.apache.karaf.shell.support.table.HAlign;
import org.apache.karaf.shell.support.table.Row;
import org.apache.karaf.shell.support.table.ShellTable;

import com.google.common.base.Strings;

@Command(scope = "opennms", name = "pollerd-schedule", description = "Displays the scheduled poller tasks.")
@Service
public class PollerdScheduleCommand implements Action {

    private class ColumnDef {
        private final Comparator<CompositeData> comparator;
        private final HAlign alignment;

        public ColumnDef(final Comparator<CompositeData> comparator, final HAlign alignment) {
            this.comparator = comparator;
            this.alignment = alignment;
        }
    }

    @Option(name = "-s", aliases = "--services", description = "Show number of scheduled pollable services, 0 for all", required = false, multiValued = false)
    private Integer services = null;

    @Option(name = "-o", aliases = "--order", description = "Sort by given column", required = false, multiValued = false)
    private String order = "readyTimeAbsoluteMs";

    @Option(name = "-c", aliases = "--columns", description = "Comma-seperated list of columns to be displayed", required = false, multiValued = false)
    private String columns = null;

    @Option(name = "-d", aliases = "--desc", description = "Descending order", required = false, multiValued = false)
    private Boolean descendingOrder = false;

    @Option(name = "--no-ellipsis", description = "wrap table rows")
    boolean noEllipsis;

    @Reference(optional = true)
    Terminal terminal;

    final Map<String, ColumnDef> columnDefs = new LinkedHashMap<>();

    public PollerdScheduleCommand() {
        columnDefs.put("nodeId", new ColumnDef(Comparator.comparingInt(c -> (Integer) c.get("nodeId")), HAlign.right));
        columnDefs.put("nodeLabel", new ColumnDef(Comparator.comparing(c -> (String) c.get("nodeLabel")), HAlign.left));
        columnDefs.put("nodeLocation", new ColumnDef(Comparator.comparing(c -> (String) c.get("nodeLocation")), HAlign.left));
        columnDefs.put("ipAddress", new ColumnDef(Comparator.comparing(c -> (String) c.get("ipAddress")), HAlign.center));
        columnDefs.put("readyTimeMs", new ColumnDef(Comparator.comparingLong(c -> (Long) c.get("readyTimeMs")), HAlign.right));
        columnDefs.put("readyTimeAbsoluteMs", new ColumnDef(Comparator.comparingLong(c -> (Long) c.get("readyTimeAbsoluteMs")), HAlign.right));
        columnDefs.put("service", new ColumnDef(Comparator.comparing(c -> (String) c.get("service")), HAlign.center));
        columnDefs.put("status", new ColumnDef(Comparator.comparing(c -> (String) c.get("status")), HAlign.center));
        columnDefs.put("statusChangeTimeMs", new ColumnDef(Comparator.comparingLong(c -> (Long) c.get("statusChangeTimeMs")), HAlign.right));
    }

    private void printColumns() {
        System.out.printf("Column %s does not exist. Available columns are:\n", order);
        for (final String columnName : columnDefs.keySet()) {
            System.out.printf("  %s\n", columnName);
        }
    }

    @Override
    public Void execute() throws MalformedObjectNameException, ReflectionException, AttributeNotFoundException, InstanceNotFoundException, MBeanException {
        final MBeanServer mBeanServer = ManagementFactory.getPlatformMBeanServer();
        final ObjectName objectName = new ObjectName("OpenNMS:Name=Pollerd");
        String columnArr[] = columnDefs.keySet().toArray(new String[0]);

        if (!mBeanServer.isRegistered(objectName)) {
            System.out.println("Error accessing Pollerd mBean");
            return null;
        }

        if (!Strings.isNullOrEmpty(columns)) {
            if (services == null) {
                System.out.println("Option --columns/-c can only be used in combination with --schedule/-s.");
                return null;
            } else {
                if (!Strings.isNullOrEmpty(columns)) {
                    final String arr[] = columns.split(",");
                    for (final String column : arr) {
                        if (!columnDefs.keySet().contains(column)) {
                            printColumns();
                            return null;
                        }
                    }
                    columnArr = arr;
                }
            }
        }

        if (services != null) {
            if (!columnDefs.keySet().contains(order)) {
                printColumns();
                return null;
            }
        }

        Long currentTimeMillis = null;

        System.out.printf("Active Threads: %d\n", (long) mBeanServer.getAttribute(objectName, "ActiveThreads"));
        System.out.printf("Num Polls: %d\n", (long) mBeanServer.getAttribute(objectName, "NumPolls"));
        System.out.printf("Core Pool Threads: %d\n", (long) mBeanServer.getAttribute(objectName, "CorePoolThreads"));
        System.out.printf("Max Pool Threads: %d\n", (Long) mBeanServer.getAttribute(objectName, "MaxPoolThreads"));
        System.out.printf("Num Pool Threads:%d \n", (long) mBeanServer.getAttribute(objectName, "NumPoolThreads"));
        System.out.printf("Peak Pool Threads: %d\n", (long) mBeanServer.getAttribute(objectName, "PeakPoolThreads"));
        System.out.printf("Task Completion Ratio: %,.5f\n", (double) mBeanServer.getAttribute(objectName, "TaskCompletionRatio"));
        System.out.printf("Task Queue Pending Count: %d\n", (long) mBeanServer.getAttribute(objectName, "TaskQueuePendingCount"));
        System.out.printf("Task Queue Remaining Capacity: %d\n", (long) mBeanServer.getAttribute(objectName, "TaskQueueRemainingCapacity"));
        System.out.printf("Tasks Completed: %d\n", (long) mBeanServer.getAttribute(objectName, "TasksCompleted"));
        System.out.printf("Tasks Total: %d\n", (long) mBeanServer.getAttribute(objectName, "TasksTotal"));

        if (services != null) {
            final TabularData tabularData = (TabularData) mBeanServer.getAttribute(objectName, "Schedule");

            if (services < 0) {
                System.out.printf("\nOption --services/-s must be zero or positive.\n");
                return null;
            }

            if (tabularData.isEmpty()) {
                System.out.printf("\nNo Pollerd scheduled services found\n");
                return null;
            }

            final ShellTable table = new ShellTable();

            if (!noEllipsis && terminal != null && terminal.getWidth() > 0) {
                table.size(terminal.getWidth() - 1);
            }

            for (final String column : columnArr) {
                table.column(column).align(columnDefs.get(column).alignment);
            }

            final Comparator<CompositeData> compositeDataComparator = descendingOrder ?
                    columnDefs.get(order).comparator.reversed() :
                    columnDefs.get(order).comparator;

            Stream<CompositeData> compositeDataStream = ((Collection<CompositeData>) tabularData.values()).stream().sorted(compositeDataComparator);

            if (services > 0) {
                compositeDataStream = compositeDataStream.limit(services);
            }

            final List<CompositeData> compositeDataList = compositeDataStream.collect(Collectors.toList());

            for (final CompositeData compositeData : compositeDataList) {

                if (currentTimeMillis == null) {
                    currentTimeMillis = (Long) compositeData.get("readyTimeAbsoluteMs") - (Long) compositeData.get("readyTimeMs");
                }

                final Row row = table.addRow();
                for (final String column : columnArr) {
                    row.addContent(compositeData.get(column));
                }
            }
            System.out.printf("Current Time Millis: %d\n\n", currentTimeMillis);
            table.print(System.out);
        }

        return null;
    }
}

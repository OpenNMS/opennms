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

import org.opennms.systemreport.AbstractSystemReportPlugin;
import org.springframework.core.io.Resource;

import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;
import oshi.SystemInfo;
import oshi.hardware.HWDiskStore;
import oshi.hardware.HardwareAbstractionLayer;
import oshi.software.os.FileSystem;
import oshi.software.os.OperatingSystem;

public class HardDriveReportPlugin  extends AbstractSystemReportPlugin {


    @Override
    public String getName() {
        return "Hard Drive Stats";
    }

    @Override
    public String getDescription() { return "Hard Drive Capacity and Performance Information"; }

    @Override
    public int getPriority() {
        return 5;
    }

    @Override
    public boolean isVisible() { return true; }

    @Override
    public Map<String, Resource> getEntries() {
       final Map<String,Resource> map = new TreeMap<String,Resource>();
        map.put("Hard Drive Capacity", getResource("\n"+getDiskCapacityInfo()));
        map.put("Hard Drive Performance", getResource("\n"+getDiskPerformanceInfo()));
        return map;
    }


    private String getDiskPerformanceInfo(){

        SystemInfo systemInfo = new SystemInfo();
        HardwareAbstractionLayer hal = systemInfo.getHardware();
        HWDiskStore[] diskStores = hal.getDiskStores().toArray(new HWDiskStore[0]);

        String header = String.format("%-20s %-10s %-10s %-10s %-15s %-15s%n",
                "Disk", "Size", "Reads", "Writes", "Read Bytes", "Write Bytes");

        String diskInfo = java.util.Arrays.stream(diskStores)
                .map(disk -> String.format("%-20s %-10s %-10s %-10s %-15s %-15s%n",
                        disk.getName(),
                        formatDiskSize(disk.getSize()),
                        formatDiskSize(disk.getReads()),
                        formatDiskSize(disk.getWrites()),
                        formatDiskSize(disk.getReadBytes()),
                        formatDiskSize(disk.getWriteBytes())))
                .collect(Collectors.joining());

        return header + diskInfo;

    }


    private String getDiskCapacityInfo(){

        SystemInfo systemInfo = new SystemInfo();
        OperatingSystem os = systemInfo.getOperatingSystem();
        FileSystem fileSystem = os.getFileSystem();

        String headers = String.format("%-20s %-10s %-10s %-10s %-10s %-10s%n",
                "Filesystem", "Size", "Used", "Avail", "Use%", "Mounted on");

        String fileStoresInfo = fileSystem.getFileStores().stream()
                .map(fs -> {
                    long total = fs.getTotalSpace();
                    long free = fs.getUsableSpace();
                    long used = total - free;
                    double percentUsed = (total > 0) ? (100.0 * used / total) : 0;

                    return String.format("%-20s %-10s %-10s %-10s %-10s %-10s%n",
                            fs.getName(),
                            formatDiskSize(total),
                            formatDiskSize(used),
                            formatDiskSize(free),
                            String.format("%.1f%%", percentUsed),
                            fs.getMount());
                })
                .collect(Collectors.joining());


        return headers+fileStoresInfo;
    }

    private String formatDiskSize(long bytes) {
        return FileUtils.byteCountToDisplaySize(bytes);
    }

}

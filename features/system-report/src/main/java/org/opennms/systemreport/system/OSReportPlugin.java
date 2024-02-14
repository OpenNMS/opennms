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

import java.io.File;
import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.opennms.systemreport.AbstractSystemReportPlugin;
import org.springframework.core.io.Resource;


public class OSReportPlugin extends AbstractSystemReportPlugin {
    private static final Logger LOG = LoggerFactory.getLogger(OSReportPlugin.class);
    private static final Map<String, String> m_oses = new LinkedHashMap<String, String>();
    public OSReportPlugin() {
        if (m_oses.size() == 0) {
            m_oses.put("/etc/SUSE-release", "SuSE");
            m_oses.put("/etc/redhat-release", "Red Hat");
            m_oses.put("/etc/fedora-release", "Fedora");
            m_oses.put("/etc/slackware-release", "Slackware");
            m_oses.put("/etc/debian_version", "Debian");
            m_oses.put("/etc/debian_release", "Debian");
            m_oses.put("/etc/mandriva-release", "Mandriva");
            m_oses.put("/etc/mandrake-release", "Mandrake");
            m_oses.put("/etc/mandrakelinux-release", "Mandrake");
            m_oses.put("/etc/yellowdog-release", "Yellow Dog");
            m_oses.put("/etc/sun-release", "Java Desktop System");
            m_oses.put("/etc/gentoo-release", "Gentoo");
            m_oses.put("/etc/UnitedLinux-release", "UnitedLinux");
        }
    }

    @Override
    public String getName() {
        return "OS";
    }

    @Override
    public String getDescription() {
        return "Kernel, OS, and Distribution";
    }

    @Override
    public int getPriority() {
        return 2;
    }

    @Override
    public Map<String, Resource> getEntries() {
        final Map<String, Resource> map = new TreeMap<String, Resource>();
        map.put("Name", getResourceFromProperty("os.name"));
        map.put("Architecture", getResourceFromProperty("os.arch"));
        map.put("Version", getResourceFromProperty("os.version"));
        map.put("Distribution", map.get("Name"));

        OperatingSystemMXBean osBean = getBean(ManagementFactory.OPERATING_SYSTEM_MXBEAN_NAME, OperatingSystemMXBean.class);
        if (osBean == null) {
            LOG.info("falling back to local VM OperatingSystemMXBean");
            osBean = ManagementFactory.getOperatingSystemMXBean();
        }

        LOG.trace("bean = {}", osBean);
        addGetters(osBean, map);

        File lsb = new File("/bin/lsb_release");
        File solaris = new File("/var/sadm/softinfo/INST_RELEASE");
        if (lsb.exists()) {
            final String text = slurpCommand(new String[] { "/bin/lsb_release", "-a" });
            final Map<String,String> distMap = splitMultilineString(": +", text);
            for (final Map.Entry<String,String> entry : distMap.entrySet()) {
                map.put("Distribution " + entry.getKey(), getResource(entry.getValue()));
            }
        } else if (solaris.exists()) {
            map.put("Distribution OS", getResource("Solaris"));
            final String solarisText = slurp(solaris);
            final Map<String,String> distMap = splitMultilineString("=", solarisText);
            for (final Map.Entry<String,String> entry : distMap.entrySet()) {
                map.put("Distribution " + entry.getKey().toLowerCase().replaceFirst("^.", entry.getKey().substring(0, 1).toUpperCase()), getResource(entry.getValue()));
            }
            if (map.containsKey("Distribution OS")) {
                map.put("Distribution", map.remove("Distribution OS"));
            }
            File isainfo = new File("/usr/bin/isainfo");
            if (isainfo.exists()) {
                final String arch = slurpCommand(new String[] { "/usr/bin/isainfo", "-n" });
                if (arch != null) {
                    map.put("Instruction Set", getResource(arch));
                }
            }
        } else {
            for (final Map.Entry<String, String> entry : m_oses.entrySet()) {
                final String description = slurp(new File(entry.getKey()));
                if (description != null) {
                    map.put("Distribution", getResource(entry.getValue()));
                    map.put("Description", getResource(description.trim()));
                }
            }
        }

        if (map.containsKey("Distribution Distributor ID")) {
            map.put("Distribution", map.remove("Distribution Distributor ID"));
        }
        if (map.containsKey("Distribution Description")) {
            map.put("Description", map.remove("Distribution Description"));
        }

        return map;
    }
}

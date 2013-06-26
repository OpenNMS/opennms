/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2010-2012 The OpenNMS Group, Inc.
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
    public TreeMap<String, Resource> getEntries() {
        final TreeMap<String, Resource> map = new TreeMap<String, Resource>();
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

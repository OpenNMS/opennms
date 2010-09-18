package org.opennms.systemreport.system;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.opennms.core.utils.LogUtils;
import org.opennms.systemreport.AbstractSystemReportPlugin;
import org.springframework.core.io.Resource;


public class OSReportPlugin extends AbstractSystemReportPlugin {
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

    public String getName() {
        return "OS";
    }

    public String getDescription() {
        return "Kernel, OS, and Distribution";
    }

    public int getPriority() {
        return 2;
    }

    public TreeMap<String, Resource> getEntries() {
        final TreeMap<String, Resource> map = new TreeMap<String, Resource>();
        map.put("Name", getResourceFromProperty("os.name"));
        map.put("Architecture", getResourceFromProperty("os.arch"));
        map.put("Version", getResourceFromProperty("os.version"));
        map.put("Distribution", map.get("Name"));

        OperatingSystemMXBean osBean = getBean(ManagementFactory.OPERATING_SYSTEM_MXBEAN_NAME, OperatingSystemMXBean.class);
        if (osBean == null) {
            LogUtils.infof(this, "falling back to local VM OperatingSystemMXBean");
            osBean = ManagementFactory.getOperatingSystemMXBean();
        }

        LogUtils.tracef(this, "bean = %s", osBean.toString());
        addGetters(osBean, map);

        File lsb = new File("/bin/lsb_release");
        File solaris = new File("/var/sadm/softinfo/INST_RELEASE");
        if (lsb.exists() && lsb.canExecute()) {
            final String text = slurpCommand(new String[] { "/bin/lsb_release", "-a" });
            final Map<String,String> distMap = splitMultilineString(": +", text);
            for (final Map.Entry<String,String> entry : distMap.entrySet()) {
                map.put("Distribution " + entry.getKey(), getResource(entry.getValue()));
            }
        } else if (solaris.exists()) {
            map.put("Distribution Os", getResource("Solaris"));
            final String solarisText = slurp(solaris);
            final Map<String,String> distMap = splitMultilineString("=", solarisText);
            for (final Map.Entry<String,String> entry : distMap.entrySet()) {
                map.put("Distribution " + entry.getKey().toLowerCase().replaceFirst("^.", entry.getKey().substring(0, 1).toUpperCase()), getResource(entry.getValue()));
            }
            if (map.containsKey("Distribution Os")) {
                map.put("Distribution", map.remove("Distribution Os"));
            }
            File isainfo = new File("/usr/bin/isainfo");
            if (isainfo.exists() && isainfo.canExecute()) {
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
        if (map.containsKey("Distribution Descrption")) {
            map.put("Description", map.remove("Distribution Description"));
        }

        return map;
    }
}

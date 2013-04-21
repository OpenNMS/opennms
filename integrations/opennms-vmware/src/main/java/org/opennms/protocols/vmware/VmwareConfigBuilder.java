/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012 The OpenNMS Group, Inc.
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

package org.opennms.protocols.vmware;

import com.vmware.vim25.*;
import com.vmware.vim25.mo.*;
import com.vmware.vim25.mo.util.MorUtil;
import org.apache.commons.cli.*;

import javax.net.ssl.*;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

public class VmwareConfigBuilder {

    private static class VMwareConfigMetric implements Comparable<VMwareConfigMetric> {
        private String humanReadableName, aliasName, groupName;
        private PerfCounterInfo perfCounterInfo;
        private boolean multiInstance = false;

        public VMwareConfigMetric(PerfCounterInfo perfCounterInfo, String humanReadableName, String aliasName, boolean multiInstance, String groupName) {
            this.perfCounterInfo = perfCounterInfo;
            this.humanReadableName = humanReadableName;
            this.aliasName = aliasName;
            this.multiInstance = multiInstance;
            this.groupName = groupName;
        }

        public String getDatacollectionEntry() {
            return "        <attrib name=\"" + humanReadableName + "\" alias=\"" + aliasName + "\" type=\"Gauge\"/>\n";
        }

        public String getGraphDefinition(String apiVersion) {
            String resourceType = (multiInstance ? "vmware" + apiVersion + groupName : "nodeSnmp");

            String def = "report.vmware" + apiVersion + "." + aliasName + ".name=" + aliasName + "\n" + "report.vmware" + apiVersion + "." + aliasName + ".columns=" + aliasName + "\n";

            if (multiInstance) {
                def += "report.vmware" + apiVersion + "." + aliasName + ".propertiesValues=" + groupName + "Name\n";
            }

            def += "report.vmware" + apiVersion + "." + aliasName + ".type=" + resourceType + "\n" + "report.vmware" + apiVersion + "." + aliasName + ".command=--title=\"" + aliasName + (multiInstance ? " {" + resourceType + "Name}" : "") + "\" \\\n" + "--vertical-label=\"" + aliasName + "\" \\\n" + "DEF:xxx={rrd1}:"
                    + aliasName + ":AVERAGE \\\n" + "LINE2:xxx#0000ff:\"" + aliasName + "\" \\\n" + "GPRINT:xxx:AVERAGE:\"Avg  \\\\: %8.2lf %s\" \\\n" + "GPRINT:xxx:MIN:\"Min  \\\\: %8.2lf %s\" \\\n" + "GPRINT:xxx:MAX:\"Max  \\\\: %8.2lf %s\\\\n\" \\\n\n";

            return def;
        }

        public String getInclude(String apiVersion) {
            return "vmware" + apiVersion + "." + getAliasName() + ", \\\n";
        }

        public String getHumanReadableName() {
            return humanReadableName;
        }

        public String getAliasName() {
            return aliasName;
        }

        public PerfCounterInfo getPerfCounterInfo() {
            return perfCounterInfo;
        }

        public boolean isMultiInstance() {
            return multiInstance;
        }

        @Override
        public int compareTo(VMwareConfigMetric o) {
            return getAliasName().compareTo(o.getAliasName());
        }
    }

    private String hostname, username, password;
    private ServiceInstance serviceInstance;
    private PerformanceManager performanceManager;
    private Map<String, Map<String, TreeSet<VMwareConfigMetric>>> collections = new HashMap<String, Map<String, TreeSet<VMwareConfigMetric>>>();
    private Map<Integer, PerfCounterInfo> perfCounterInfoMap = new HashMap<Integer, PerfCounterInfo>();
    private String versionInformation = "", apiVersion = "";

    private static class TrustAllManager implements javax.net.ssl.TrustManager, javax.net.ssl.X509TrustManager {
        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
            return null;
        }

        public boolean isServerTrusted(java.security.cert.X509Certificate[] certs) {
            return true;
        }

        public boolean isClientTrusted(java.security.cert.X509Certificate[] certs) {
            return true;
        }

        public void checkServerTrusted(java.security.cert.X509Certificate[] certs, String authType) throws java.security.cert.CertificateException {
            return;
        }

        public void checkClientTrusted(java.security.cert.X509Certificate[] certs, String authType) throws java.security.cert.CertificateException {
            return;
        }
    }

    public VmwareConfigBuilder(String hostname, String username, String password) {
        this.hostname = hostname;
        this.username = username;
        this.password = password;
    }

    private String getHumanReadableName(PerfCounterInfo perfCounterInfo) {
        return perfCounterInfo.getGroupInfo().getKey() + "." + perfCounterInfo.getNameInfo().getKey() + "." + perfCounterInfo.getRollupType().toString();
    }

    private String normalizeName(String name) {
        return name.substring(0, 1).toUpperCase() + name.substring(1);
    }

    private String normalizeGroupName(String groupName) {
        String modifiedGroupName = groupName;
        String[] groupChunks = {"sys", "rescpu", "cpu", "net", "disk", "mem", "managementAgent", "virtualDisk", "datastore", "storageAdapter", "storagePath", "hbr", "power"};
        String[] groupReplacements = {"Sys", "ResCpu", "Cpu", "Net", "Disk", "Mem", "MgtAgt", "VrtDisk", "DaSt", "StAdptr", "StPth", "Hbr", "Power"};

        for (int i = 0; i < groupChunks.length; i++) {
            modifiedGroupName = modifiedGroupName.replace(groupChunks[i], groupReplacements[i]);
        }
        return modifiedGroupName;
    }

    private String condenseName(String text, String chunk) {
        String ignoreCaseChunk = "[" + chunk.substring(0, 1) + chunk.substring(0, 1).toUpperCase() + "]" + chunk.substring(1);
        String replacement = chunk.substring(0, 1).toUpperCase() + chunk.substring(chunk.length() - 1);
        return text.replaceAll(ignoreCaseChunk, replacement);
    }

    private String getAliasName(PerfCounterInfo perfCounterInfo) {

        String group = perfCounterInfo.getGroupInfo().getKey();
        String name = perfCounterInfo.getNameInfo().getKey();
        String rollup = perfCounterInfo.getRollupType().toString();

        group = normalizeGroupName(group);

        String[] rollupChunks = {"summation", "average", "latest", "none", "minimum", "maximum", "total"};
        String[] rollupReplacements = {"Sum", "Avg", "Lat", "Non", "Min", "Max", "Tot"};

        for (int i = 0; i < rollupChunks.length; i++) {
            rollup = rollup.replace(rollupChunks[i], rollupReplacements[i]);
        }

        String[] nameChunks = {"unkown", "protos", "threshold", "datastore", "alloc", "utilization", "normalized", "normal", "shares", "depth", "resource", "overhead", "swap", "rate", "metric", "number", "averaged", "load", "decompression", "compression", "device", "latency",
                "capacity", "commands", "target", "aborted", "kernel", "unreserved", "reserved", "total", "read", "write", "queue", "limited", "sample", "count", "touched"};

        for (String chunk : nameChunks) {
            name = condenseName(name, chunk);
        }

        name = normalizeName(name);

        String full = group + name + rollup;

        if (full.length() >= 19) {
            System.out.println("******************************************");
            System.out.println("Key '" + full + "' is " + full.length() + " characters long");
            System.out.println("******************************************");
        }

        return full;
    }

    private void lookupMetrics(String collectionName, String managedObjectId) throws Exception {
        ManagedObjectReference managedObjectReference = new ManagedObjectReference();

        managedObjectReference.setType("ManagedEntity");
        managedObjectReference.setVal(managedObjectId);

        ManagedEntity managedEntity = MorUtil.createExactManagedEntity(serviceInstance.getServerConnection(), managedObjectReference);

        int refreshRate = performanceManager.queryPerfProviderSummary(managedEntity).getRefreshRate();

        PerfQuerySpec perfQuerySpec = new PerfQuerySpec();
        perfQuerySpec.setEntity(managedEntity.getMOR());
        perfQuerySpec.setMaxSample(Integer.valueOf(1));
        perfQuerySpec.setIntervalId(refreshRate);

        PerfEntityMetricBase[] perfEntityMetricBases = performanceManager.queryPerf(new PerfQuerySpec[]{perfQuerySpec});

        HashMap<String, TreeSet<VMwareConfigMetric>> groupMap = new HashMap<String, TreeSet<VMwareConfigMetric>>();

        HashMap<String, Boolean> multiInstance = new HashMap<String, Boolean>();

        if (perfEntityMetricBases != null) {
            for (int i = 0; i < perfEntityMetricBases.length; i++) {
                PerfMetricSeries[] perfMetricSeries = ((PerfEntityMetric) perfEntityMetricBases[i]).getValue();

                for (int j = 0; perfMetricSeries != null && j < perfMetricSeries.length; j++) {

                    if (perfMetricSeries[j] instanceof PerfMetricIntSeries) {

                        long[] longs = ((PerfMetricIntSeries) perfMetricSeries[j]).getValue();

                        if (longs.length == 1) {

                            PerfCounterInfo perfCounterInfo = perfCounterInfoMap.get(perfMetricSeries[j].getId().getCounterId());

                            String instanceName = perfMetricSeries[j].getId().getInstance();

                            String humanReadableName = getHumanReadableName(perfCounterInfo);
                            String aliasName = getAliasName(perfCounterInfo);
                            String groupName = perfCounterInfo.getGroupInfo().getKey();
                            String normalizedGroupName = normalizeGroupName(groupName);

                            Boolean b = multiInstance.get(getHumanReadableName(perfCounterInfo));

                            if (b == null) {
                                b = Boolean.valueOf(instanceName != null && !"".equals(instanceName));
                            } else {
                                b = Boolean.valueOf(b.booleanValue() || (instanceName != null && !"".equals(instanceName)));
                            }

                            if (!b) {
                                groupName = "Node";
                                normalizedGroupName = "Node";
                            }

                            if (!groupMap.containsKey(normalizedGroupName)) {
                                groupMap.put(normalizedGroupName, new TreeSet<VMwareConfigMetric>());
                            }

                            TreeSet<VMwareConfigMetric> counterSet = groupMap.get(normalizedGroupName);

                            multiInstance.put(getHumanReadableName(perfCounterInfo), b);

                            counterSet.add(new VMwareConfigMetric(perfCounterInfo, humanReadableName, aliasName, b, normalizedGroupName));
                        }
                    }
                }
            }
        }
        collections.put(collectionName, groupMap);
    }

    private void generateData(String rrdRepository) throws Exception {
        serviceInstance = new ServiceInstance(new URL("https://" + hostname + "/sdk"), username, password);

        performanceManager = serviceInstance.getPerformanceManager();

        PerfCounterInfo[] perfCounterInfos = performanceManager.getPerfCounter();

        for (PerfCounterInfo perfCounterInfo : perfCounterInfos) {
            perfCounterInfoMap.put(perfCounterInfo.getKey(), perfCounterInfo);
        }

        System.out.println("Generating configuration files for '" + serviceInstance.getAboutInfo().getFullName() + "' using rrdRepository '" + rrdRepository + "'...");

        StringBuffer buffer = new StringBuffer();
        buffer.append("Configuration file generated for:\n\n");
        buffer.append("Full name.......: " + serviceInstance.getAboutInfo().getFullName() + "\n");
        buffer.append("API type........: " + serviceInstance.getAboutInfo().getApiType() + "\n");
        buffer.append("API version.....: " + serviceInstance.getAboutInfo().getApiVersion() + "\n");
        buffer.append("Product name....: " + serviceInstance.getAboutInfo().getLicenseProductName() + "\n");
        buffer.append("Product version.: " + serviceInstance.getAboutInfo().getLicenseProductVersion() + "\n");
        buffer.append("OS type.........: " + serviceInstance.getAboutInfo().getOsType() + "\n");

        versionInformation = buffer.toString();

        String arr[] = serviceInstance.getAboutInfo().getApiVersion().split("\\.");

        if (arr.length > 1) {
            apiVersion = arr[0];
            if (Integer.valueOf(apiVersion) < 4) {
                apiVersion = "3";
            }
        }

        ManagedEntity[] hostSystems, virtualMachines;

        virtualMachines = new InventoryNavigator(serviceInstance.getRootFolder()).searchManagedEntities("VirtualMachine");

        if (virtualMachines != null) {
            if (virtualMachines.length > 0) {
                for (ManagedEntity managedEntity : virtualMachines) {
                    if ("poweredOn".equals(((VirtualMachine) managedEntity).getRuntime().getPowerState().toString())) {
                        lookupMetrics("default-VirtualMachine" + apiVersion, managedEntity.getMOR().getVal());
                        break;
                    }
                }
            } else {
                System.err.println("No virtual machines found");
            }
        }

        hostSystems = new InventoryNavigator(serviceInstance.getRootFolder()).searchManagedEntities("HostSystem");

        if (hostSystems != null) {
            if (hostSystems.length > 0) {
                for (ManagedEntity managedEntity : hostSystems) {
                    if ("poweredOn".equals(((HostSystem) managedEntity).getRuntime().getPowerState().toString())) {
                        lookupMetrics("default-HostSystem" + apiVersion, managedEntity.getMOR().getVal());
                        break;
                    }
                }
            } else {
                System.err.println("No host systems found!");
            }
        }

        saveVMwareDatacollectionConfig(rrdRepository);
        saveVMwareDatacollectionInclude();
        saveVMwareGraphProperties();
    }

    private void saveVMwareGraphProperties() {
        StringBuffer buffer = new StringBuffer();
        StringBuffer include = new StringBuffer();
        HashMap<String, Boolean> generatedGraphs = new HashMap<String, Boolean>();

        for (String collectionName : collections.keySet()) {
            Map<String, TreeSet<VMwareConfigMetric>> collection = collections.get(collectionName);
            for (String groupName : collection.keySet()) {
                TreeSet<VMwareConfigMetric> metrics = collection.get(groupName);
                for (VMwareConfigMetric vmwarePerformanceMetric : metrics) {
                    Boolean generated = (generatedGraphs.get(vmwarePerformanceMetric.getAliasName()) == null ? false : generatedGraphs.get(vmwarePerformanceMetric.getAliasName()));

                    if (!generated) {
                        buffer.append(vmwarePerformanceMetric.getGraphDefinition(apiVersion));
                        include.append(vmwarePerformanceMetric.getInclude(apiVersion));
                    }
                }
            }
        }

        saveFile("vmware" + apiVersion + "-graph-simple.properties", "reports=" + include.toString() + "\n\n" + buffer.toString());
    }

    private void saveFile(String filename, String contents) {
        System.out.println("Saving file '" + filename + "'...");
        try {
            FileWriter f = new FileWriter(filename);
            f.write(contents);
            f.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void saveVMwareDatacollectionInclude() {
        StringBuffer buffer = new StringBuffer();

        buffer.append("<?xml version=\"1.0\"?>\n");

        buffer.append("\n<!--\n");
        buffer.append(versionInformation);
        buffer.append("-->\n\n");

        buffer.append("<datacollection-group name=\"VMware" + apiVersion + "\">\n\n");

        for (String collectionName : collections.keySet()) {
            Map<String, TreeSet<VMwareConfigMetric>> collection = collections.get(collectionName);
            for (String groupName : collection.keySet()) {
                if (!"node".equalsIgnoreCase(groupName)) {
                    buffer.append("  <resourceType name=\"vmware" + apiVersion + groupName + "\" label=\"VMware v" + apiVersion + " " + groupName + "\" resourceLabel=\"${" + groupName + "Name}\">\n");
                    buffer.append("    <persistenceSelectorStrategy class=\"org.opennms.netmgt.collectd.PersistAllSelectorStrategy\"/>\n");
                    buffer.append("    <storageStrategy class=\"org.opennms.netmgt.dao.support.IndexStorageStrategy\"/>\n");
                    buffer.append("  </resourceType>\n\n");
                }
            }
        }

        buffer.append("</datacollection-group>");

        saveFile("vmware" + apiVersion + ".xml", buffer.toString());
    }

    private void saveVMwareDatacollectionConfig(String rrdRepository) {
        StringBuffer buffer = new StringBuffer();
        buffer.append("<?xml version=\"1.0\"?>\n");

        buffer.append("\n<!--\n");
        buffer.append(versionInformation);
        buffer.append("-->\n\n");

        buffer.append("<vmware-datacollection-config rrdRepository=\"" + rrdRepository + "\">\n");
        for (String collectionName : collections.keySet()) {
            Map<String, TreeSet<VMwareConfigMetric>> collection = collections.get(collectionName);

            buffer.append("  <vmware-collection name=\"" + collectionName + "\">\n");
            buffer.append("    <rrd step=\"300\">\n");
            buffer.append("      <rra>RRA:AVERAGE:0.5:1:2016</rra>\n");
            buffer.append("      <rra>RRA:AVERAGE:0.5:12:1488</rra>\n");
            buffer.append("      <rra>RRA:AVERAGE:0.5:288:366</rra>\n");
            buffer.append("      <rra>RRA:MAX:0.5:288:366</rra>\n");
            buffer.append("      <rra>RRA:MIN:0.5:288:366</rra>\n");
            buffer.append("    </rrd>\n");

            buffer.append("    <vmware-groups>\n");
            for (String groupName : collection.keySet()) {
                if ("node".equalsIgnoreCase(groupName)) {
                    buffer.append("      <vmware-group name=\"vmware" + apiVersion + groupName + "\" resourceType=\"" + groupName + "\">\n");
                } else {
                    buffer.append("      <vmware-group name=\"vmware" + apiVersion + groupName + "\" resourceType=\"vmware" + apiVersion + groupName + "\">\n");
                }
                TreeSet<VMwareConfigMetric> metrics = collection.get(groupName);
                for (VMwareConfigMetric vmwarePerformanceMetric : metrics) {
                    buffer.append(vmwarePerformanceMetric.getDatacollectionEntry());
                }
                buffer.append("      </vmware-group>\n");
            }
            buffer.append("    </vmware-groups>\n");
            buffer.append("  </vmware-collection>\n");
        }
        buffer.append("</vmware-datacollection-config>\n");

        saveFile("vmware" + apiVersion + "-datacollection-config.xml", buffer.toString());
    }

    private static void usage(final Options options, final CommandLine cmd, final String error, final Exception e) {
        final HelpFormatter formatter = new HelpFormatter();
        final PrintWriter pw = new PrintWriter(System.out);
        if (error != null) {
            pw.println("An error occurred: " + error + "\n");
        }

        formatter.printHelp("Usage: VmwareConfigBuilder <hostname> <username> <password>", options);

        if (e != null) {
            pw.println(e.getMessage());
            e.printStackTrace(pw);
        }

        pw.close();
    }

    private static void usage(final Options options, final CommandLine cmd) {
        usage(options, cmd, null, null);
    }


    public static void main(String args[]) throws ParseException {
        String hostname = null;
        String username = null;
        String password = null;
        String rrdRepository = null;

        final Options options = new Options();

        options.addOption("rrdRepository", true, "set rrdRepository path for generated config files, default: '/opt/opennms/share/rrd/snmp/'");

        final CommandLineParser parser = new PosixParser();
        final CommandLine cmd = parser.parse(options, args);

        @SuppressWarnings("unchecked")
        List<String> arguments = (List<String>) cmd.getArgList();

        if (arguments.size() < 3) {
            usage(options, cmd);
            System.exit(1);
        }

        hostname = arguments.remove(0);
        username = arguments.remove(0);
        password = arguments.remove(0);

        if (cmd.hasOption("rrdRepository")) {
            rrdRepository = cmd.getOptionValue("rrdRepository");
        } else {
            rrdRepository = "/opt/opennms/share/rrd/snmp/";
        }

        TrustManager[] trustAllCerts = new TrustManager[1];

        trustAllCerts[0] = new TrustAllManager();

        SSLContext sc = null;
        try {
            sc = SSLContext.getInstance("SSL");
            sc.init(null, trustAllCerts, null);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (KeyManagementException e) {
            e.printStackTrace();
        }

        HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());

        HostnameVerifier hv = new HostnameVerifier() {
            public boolean verify(String urlHostName, SSLSession session) {
                return true;
            }
        };

        HttpsURLConnection.setDefaultHostnameVerifier(hv);

        VmwareConfigBuilder vmwareConfigBuilder;

        vmwareConfigBuilder = new VmwareConfigBuilder(hostname, username, password);

        try {
            vmwareConfigBuilder.generateData(rrdRepository);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

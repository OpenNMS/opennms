package org.opennms.netmgt.jasper.helper;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

import net.sf.jasperreports.engine.util.JRProperties;

public class JRobinDirectoryUtil {
    
    public boolean isStoreByGroup() {
        return JRProperties.getBooleanProperty("org.opennms.rrd.storeByGroup") || Boolean.getBoolean("org.opennms.rrd.storeByGroup");
    }
    
    public String getIfInOctetsJrb(String rrdDirectory, String nodeId,String iFace) throws FileNotFoundException, IOException {
        return getOctetsFile(rrdDirectory, nodeId, iFace, "ifHCInOctets", "ifInOctets");
    }
    
    public String getIfOutOctetsJrb(String rrdDirectory, String nodeId,String iFace) throws FileNotFoundException, IOException {
        return getOctetsFile(rrdDirectory, nodeId, iFace, "ifHCOutOctets", "ifOutOctets");
    }

    private String getOctetsFile(String rrdDirectory, String nodeId, String iFace, String ifHCFilename, String ifFilename) throws FileNotFoundException, IOException {
        StringBuffer directory = new StringBuffer();
        directory.append(rrdDirectory)
            .append(File.separator)
            .append(nodeId)
            .append(File.separator)
            .append(iFace);
        
        if(isStoreByGroup()) {
            appendStoreByGroup(directory);
        }else {
            
            File ifOctets = new File(directory.toString() + "" + File.separator + ifHCFilename + getExtension());
            if(ifOctets.exists()) {
                directory.append(File.separator).append(ifHCFilename).append(getExtension());
            }else {
                directory.append(File.separator).append(ifFilename).append(getExtension());
            }
        }
        
        return  directory.toString();
    }
    
    private String getExtension() {
        if(JRProperties.getProperty("org.opennms.rrd.fileExtension") != null) {
            return JRProperties.getProperty("org.opennms.rrd.fileExtension");
        }else if(System.getProperty("org.opennms.rrd.fileExtension") != null) {
            return System.getProperty("org.opennms.rrd.fileExtension");
        }else {
            if(System.getProperty("org.opennms.rrd.strategyClass") != null) {
                return System.getProperty("org.opennms.rrd.strategyClass", "UnknownStrategy").endsWith("JRobinRrdStrategy") ? ".jrb" : ".rrd";
            }
            return ".jrb";
        }
        
    }

    private void appendStoreByGroup(StringBuffer directory) throws FileNotFoundException, IOException {
        File f = new File(directory.toString() + "" + File.separator + "ds.properties");
        System.out.println("path to ds.properties: " + f.getAbsolutePath());
        if(f.exists()) {
            Properties prop = new Properties();
            FileInputStream fis = new FileInputStream(f);
            prop.load(fis);
            fis.close();
            
            directory.append(File.separator).append((String) prop.get("ifInOctets")).append(getExtension());
        }
    }

    public String getInterfaceDirectory(String snmpifname, String snmpifdescr, String snmpphysaddr) {
        
        String ifLabel = "";
        if(snmpifname == null || "".equals(snmpifname)) {
            ifLabel = snmpifdescr;
        }else {
            ifLabel = snmpifname;
        }
        
        String dir = "";
        if(snmpphysaddr == null || "".equals(snmpphysaddr)) {
            dir = ifLabel.replaceAll("[\\/ :| \\.]", "_");
        }else {
            dir = ifLabel.replaceAll("[\\/ :| \\.]", "_") + "-" + snmpphysaddr;
        }
        
        return dir;
    }

}

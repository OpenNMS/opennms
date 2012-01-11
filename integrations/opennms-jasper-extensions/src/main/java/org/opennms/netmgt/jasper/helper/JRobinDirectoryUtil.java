package org.opennms.netmgt.jasper.helper;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

import net.sf.jasperreports.engine.util.JRProperties;

import org.opennms.core.utils.RrdLabelUtils;

public class JRobinDirectoryUtil {
    
    public boolean isStoreByGroup() {
        return JRProperties.getBooleanProperty("org.opennms.rrd.storeByGroup") || Boolean.getBoolean("org.opennms.rrd.storeByGroup");
    }
    
    public String getIfInOctetsDataSource(String rrdDirectory, String nodeId, String iFace) throws IOException {
        StringBuffer directory = new StringBuffer();
        directory.append(rrdDirectory)
            .append(File.separator)
            .append(nodeId)
            .append(File.separator)
            .append(iFace);
        
        if(!isStoreByGroup()) {
            if(checkIfHCExists("ifHCInOctets", directory.toString())) {
                return "ifHCInOctets";
            }else {
                return "ifInOctets";
            }
        }else {
            if(checkDsPropertyFileFor("ifHCInOctets", directory.toString())) {
                return "ifHCInOctets";
            }else {
                return "ifInOctets";
            }
        }
    }
    
    public String getIfOutOctetsDataSource(String rrdDirectory, String nodeId,String iFace) throws IOException {
        StringBuffer directory = new StringBuffer();
        directory.append(rrdDirectory)
            .append(File.separator)
            .append(nodeId)
            .append(File.separator)
            .append(iFace);
        
        if(!isStoreByGroup()) {
            
            if(checkIfHCExists("ifHCOutOctets", directory.toString())) {
                return "ifHCOutOctets";
            }else {
                return "ifOutOctets";
            }
            
        }else {
           
            if(checkDsPropertyFileFor("ifHCOutOctets", directory.toString())) {
                return "ifHCOutOctets";
            }else {
                return "ifOutOctets";
            }
        }
    }
    
    private boolean checkDsPropertyFileFor(String ifOctetsDS, String directory) throws IOException {
        File f = new File(directory.toString() + "" + File.separator + "ds.properties");
        if(f.exists()) {
            Properties prop = new Properties();
            FileInputStream fis = new FileInputStream(f);
            prop.load(fis);
            fis.close();
            
            return prop.get(ifOctetsDS) != null ? true : false;
        }else {
            return false;
        }
        
    }

    public String getIfInOctetsJrb(String rrdDirectory, String nodeId,String iFace) throws FileNotFoundException, IOException {
        return getOctetsJrbFile(rrdDirectory, nodeId, iFace, "ifHCInOctets.jrb", "ifInOctets.jrb");
    }
    
    public String getIfOutOctetsJrb(String rrdDirectory, String nodeId,String iFace) throws FileNotFoundException, IOException {
        return getOctetsJrbFile(rrdDirectory, nodeId, iFace, "ifHCOutOctets.jrb", "ifOutOctets.jrb");
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

    private String getOctetsJrbFile(String rrdDirectory, String nodeId, String iFace, String ifHCFilename, String ifFilename) throws FileNotFoundException, IOException {
        StringBuffer directory = new StringBuffer();
        directory.append(rrdDirectory)
            .append(File.separator)
            .append(nodeId)
            .append(File.separator)
            .append(iFace);
        
        if(isStoreByGroup()) {
            appendStoreByGroup(directory);
        }else {
            if(checkIfHCExists(ifHCFilename, directory.toString())) {
                directory.append(File.separator).append(ifHCFilename).append(getExtension());
            }else {
                directory.append(File.separator).append(ifFilename);
            }
        }
        
        return  directory.toString();
    }

    private boolean checkIfHCExists(String ifHCFilename, String dir) {
        File ifOctets = new File(dir + "" + File.separator + ifHCFilename + getExtension());
        return ifOctets.exists();
    }
    
    private void appendStoreByGroup(StringBuffer directory) throws FileNotFoundException, IOException {
        File f = new File(directory.toString() + "" + File.separator + "ds.properties");
        if(f.exists()) {
            Properties prop = new Properties();
            FileInputStream fis = new FileInputStream(f);
            prop.load(fis);
            fis.close();
            
            if(prop.get("ifHCInOctets") != null) {
                directory.append(File.separator).append((String) prop.get("ifHCInOctets")).append(getExtension());
            }else {
                directory.append(File.separator).append((String) prop.get("ifInOctets")).append(getExtension());
            }
            
        }
    }

    public String getInterfaceDirectory(String snmpifname, String snmpifdescr, String snmpphysaddr) {
        return RrdLabelUtils.computeLabelForRRD(snmpifname, snmpifdescr, snmpphysaddr);
    }
}

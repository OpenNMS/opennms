package org.opennms.netmgt.jasper.helper;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

import net.sf.jasperreports.engine.util.JRProperties;

import org.opennms.core.utils.AlphaNumeric;

public class JRobinDirectoryUtil {
    
    public boolean isStoreByGroup() {
        return JRProperties.getBooleanProperty("org.opennms.rrd.storeByGroup") || Boolean.getBoolean("org.opennms.rrd.storeByGroup");
    }
    
    public String getIfInOctetsJrb(String rrdDirectory, String nodeId,String iFace) throws FileNotFoundException, IOException {
        return getOctetsJrbFile(rrdDirectory, nodeId, iFace, "ifHCInOctets.jrb", "ifInOctets.jrb");
    }
    
    public String getIfOutOctetsJrb(String rrdDirectory, String nodeId,String iFace) throws FileNotFoundException, IOException {
        return getOctetsJrbFile(rrdDirectory, nodeId, iFace, "ifHCOutOctets.jrb", "ifOutOctets.jrb");
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
            
            File ifOctets = new File(directory.toString() + "" + File.separator + ifHCFilename);
            if(ifOctets.exists()) {
                directory.append(File.separator).append(ifHCFilename);
            }else {
                directory.append(File.separator).append(ifFilename);
            }
        }
        
        return  directory.toString();
    }
    
    private void appendStoreByGroup(StringBuffer directory) throws FileNotFoundException, IOException {
        File f = new File(directory.toString() + "" + File.separator + "ds.properties");
        System.out.println("path to ds.properties: " + f.getAbsolutePath());
        if(f.exists()) {
            Properties prop = new Properties();
            FileInputStream fis = new FileInputStream(f);
            prop.load(fis);
            fis.close();
            
            directory.append(File.separator).append((String) prop.get("ifInOctets")).append(".jrb");
        }
    }

    public String getInterfaceDirectory(String snmpifname, String snmpifdescr, String snmpphysaddr) {
        
        String name = computeNameForRRD(snmpifname, snmpifdescr);
        String physAddrForRRD = computePhysAddrForRRD(snmpphysaddr);
        
        return (physAddrForRRD == null ? name : name + '-' + physAddrForRRD);
    }

    private String computePhysAddrForRRD(String snmpphysaddr) {
        String physAddrForRRD = null;

        if (snmpphysaddr != null) {
            String parsedPhysAddr = AlphaNumeric.parseAndTrim(snmpphysaddr);
            if (parsedPhysAddr.length() == 12) {
                physAddrForRRD = parsedPhysAddr;
            } 
        }
       
        return physAddrForRRD;
    }

    private String computeNameForRRD(String snmpifname, String snmpifdescr) {
        String label = null;
        if (snmpifname != null) {
            label = AlphaNumeric.parseAndReplace(snmpifname, '_');
        } else if (snmpifdescr != null) {
            label = AlphaNumeric.parseAndReplace(snmpifdescr, '_');
        } else {
            label = "no_ifLabel";
        }
        return label;
    }

}

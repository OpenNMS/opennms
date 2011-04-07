package org.opennms.core.utils;

public class RrdLabelUtils {
    
    public static String computeNameForRRD(String ifname, String ifdescr) {
        String label = null;
        if (ifname != null) {
            label = AlphaNumeric.parseAndReplace(ifname, '_');
        } else if (ifdescr != null) {
            label = AlphaNumeric.parseAndReplace(ifdescr, '_');
        }
        return label;
        
    }
    
    public static String computePhysAddrForRRD(String physaddr) {
        String physAddrForRRD = null;

        if (physaddr != null) {
            String parsedPhysAddr = AlphaNumeric.parseAndTrim(physaddr);
            if (parsedPhysAddr.length() == 12) {
                physAddrForRRD = parsedPhysAddr;
            } 
        }
       
        return physAddrForRRD;
        
    }
    
    public static String computeLabelForRRD(String ifname, String ifdescr, String physaddr) {
        String name = computeNameForRRD(ifname, ifdescr);
        String physAddrForRRD = computePhysAddrForRRD(physaddr);
        return (physAddrForRRD == null ? name : name + '-' + physAddrForRRD);
    }
}

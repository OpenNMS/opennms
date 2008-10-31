package org.opennms.netmgt.provision.detector;

import java.lang.reflect.UndeclaredThrowableException;
import java.net.InetAddress;
import java.util.regex.Pattern;

import org.opennms.netmgt.config.SnmpPeerFactory;
import org.opennms.netmgt.provision.DetectorMonitor;
import org.opennms.netmgt.snmp.SnmpAgentConfig;
import org.opennms.netmgt.snmp.SnmpObjId;
import org.opennms.netmgt.snmp.SnmpUtils;
import org.opennms.netmgt.snmp.SnmpValue;

public class SnmpDetector extends AbstractDetector {
    
    /**
     * The system object identifier to retreive from the remote agent.
     */
    private static final String DEFAULT_OID = ".1.3.6.1.2.1.1.2.0";
    
    private static final int DEFAULT_PORT = 21;
    private static final int DEFAULT_TIMEOUT = 3000;
    private static final int DEFAULT_RETRIES = 3;
    
    private String m_oid;
    private String m_forceVersion;
    private String m_vbvalue;
    
    protected SnmpDetector() {
        setServiceName("SNMP");
        setPort(DEFAULT_PORT);
        setTimeout(DEFAULT_TIMEOUT);
        setRetries(DEFAULT_RETRIES);
        setOid(DEFAULT_OID);
    }

    @Override
    public void init() {}

    @Override
    public boolean isServiceDetected(InetAddress address, DetectorMonitor detectMonitor) {
        try {

            String oid = getOid(); //ParameterMap.getKeyedString(qualifiers, "vbname", DEFAULT_OID);
            SnmpAgentConfig agentConfig = SnmpPeerFactory.getInstance().getAgentConfig(address);
            String expectedValue = null;
            
            agentConfig.setPort(getPort());
            agentConfig.setTimeout(getTimeout());
            agentConfig.setRetries(getRetries());
            
            if (getForceVersion() != null) {
                String version = getForceVersion();
                if (version.equalsIgnoreCase("snmpv1"))
                    agentConfig.setVersion(SnmpAgentConfig.VERSION1);
                else if (version.equalsIgnoreCase("snmpv2") || version.equalsIgnoreCase("snmpv2c"))
                    agentConfig.setVersion(SnmpAgentConfig.VERSION2C);
                
                //TODO: make sure JoeSnmpStrategy correctly handles this.
                else if (version.equalsIgnoreCase("snmpv3"))
                    agentConfig.setVersion(SnmpAgentConfig.VERSION3);
            }
            
            if (getVbvalue() != null) {
                expectedValue = getVbvalue();
            }
            
            String retrievedValue = getValue(agentConfig, oid);
            
            if (retrievedValue != null && expectedValue != null) {
                return (Pattern.compile(expectedValue).matcher(retrievedValue).find());
            } else {
                return (retrievedValue != null);
            }
            
        } catch (Throwable t) {
            throw new UndeclaredThrowableException(t);
        }
    }
    
    private String getValue(SnmpAgentConfig agentConfig, String oid) {
        SnmpValue val = SnmpUtils.get(agentConfig, SnmpObjId.get(oid));
        if (val == null || val.isNull() || val.isEndOfMib() || val.isError()) {
            return null;
        }
        else {
            return val.toString();
        }
        
    }

    public void setOid(String oid) {
        m_oid = oid;
    }

    public String getOid() {
        return m_oid;
    }

    public void setForceVersion(String forceVersion) {
        m_forceVersion = forceVersion;
    }

    public String getForceVersion() {
        return m_forceVersion;
    }

    public void setVbvalue(String vbvalue) {
        m_vbvalue = vbvalue;
    }

    public String getVbvalue() {
        return m_vbvalue;
    }
    

}

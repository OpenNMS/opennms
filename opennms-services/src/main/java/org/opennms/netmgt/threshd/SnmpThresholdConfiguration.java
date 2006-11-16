package org.opennms.netmgt.threshd;

import java.io.File;
import java.util.Map;

import org.opennms.netmgt.config.ThresholdingConfigFactory;
import org.opennms.netmgt.poller.NetworkInterface;

public class SnmpThresholdConfiguration {
    
    private static final String THRESHD_SERVICE_CONFIG_KEY = SnmpThresholdConfiguration.class.getName();


    public static SnmpThresholdConfiguration get(NetworkInterface iface, Map parms) {
        SnmpThresholdConfiguration config = (SnmpThresholdConfiguration)iface.getAttribute(THRESHD_SERVICE_CONFIG_KEY);
        if (config == null) {
            config = new SnmpThresholdConfiguration(parms);
            iface.setAttribute(THRESHD_SERVICE_CONFIG_KEY, config);
        }
        return config;
    }

    private SnmpThresholdParameters m_snmpParameters;
    private SnmpThresholdConfig m_snmpThreshConfig;
    private File m_rrdRepository;
    private Map<String, ThresholdEntity> m_nodeMap;
    private Map<String, ThresholdEntity> m_baseIfMap;
    
    
    private SnmpThresholdConfiguration(Map parms) {
        this(new SnmpThresholdParameters(parms), new SnmpThresholdConfig(ThresholdingConfigFactory.getInstance()));
    }

    public SnmpThresholdConfiguration(SnmpThresholdParameters snmpParameters, SnmpThresholdConfig snmpThreshConfig) {
        m_snmpParameters = snmpParameters;
        m_snmpThreshConfig = snmpThreshConfig;
        
        m_rrdRepository = new File(getThreshConfig().getRrdRepository(getGroupName()));
        
        initNodeMap();
        initBaseInterfaceMap();
    }
    
    public SnmpThresholdParameters getSnmpParameters() {
        return m_snmpParameters;
    }
    
    public SnmpThresholdConfig getThreshConfig() {
        return m_snmpThreshConfig;
    }

    File getRrdRepository() {
        return m_rrdRepository;
    }

    String getGroupName() {
        return getSnmpParameters().getGroupName();
    }

    public void setNodeMap(Map<String, ThresholdEntity> nodeMap) {
        m_nodeMap = nodeMap;
    }

    public Map<String, ThresholdEntity> getNodeMap() {
        return m_nodeMap;
    }

    void initNodeMap() {
        setNodeMap(getThreshConfig().createThresholdMap(getGroupName(), "node"));
    }

    public void setBaseInterfaceMap(Map<String, ThresholdEntity> baseIfMap) {
        m_baseIfMap = baseIfMap;
    }

    public Map<String, ThresholdEntity> getBaseInterfaceMap() {
        return m_baseIfMap;
    }

    void initBaseInterfaceMap() {
        setBaseInterfaceMap(getThreshConfig().createThresholdMap(getGroupName(), "if"));
    }

    int getRange() {
        return getSnmpParameters().getRange();
    }

    int getInterval() {
        return getSnmpParameters().getInterval();
    }

}

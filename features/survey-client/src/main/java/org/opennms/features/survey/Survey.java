package org.opennms.features.survey;

public class Survey {
    
    
    private int m_nodeCount;

    private int m_interfaceCount;
    
    // opennms version
    private String m_opennmsVersion;

    private int m_processorCount = Runtime.getRuntime().availableProcessors();
    private long m_memorySize = Runtime.getRuntime().totalMemory();
    
    // java.version
    private String m_javaVersion = System.getProperty("java.version");
    // java.vendor
    private String m_javaVendor = System.getProperty("java.vendor");
    // java.vm.name
    private String m_javaVmName = System.getProperty("java.vm.name");
    // os.name
    private String m_osName = System.getProperty("os.name");
    // os.arch
    private String m_osArch = System.getProperty("os.arch");
    // os.version
    private String m_osVersion = System.getProperty("os.version");
    
    public int getNodeCount() {
        return m_nodeCount;
    }
    public int getInterfaceCount() {
        return m_interfaceCount;
    }
    public String getOpennmsVersion() {
        return m_opennmsVersion;
    }
    public int getProcessorCount() {
        return m_processorCount;
    }
    public long getMemorySize() {
        return m_memorySize;
    }
    public String getJavaVersion() {
        return m_javaVersion;
    }
    public String getJavaVendor() {
        return m_javaVendor;
    }
    public String getJavaVmName() {
        return m_javaVmName;
    }
    public String getOsName() {
        return m_osName;
    }
    public String getOsArch() {
        return m_osArch;
    }
    public String getOsVersion() {
        return m_osVersion;
    }
    
    public void setNodeCount(int nodeCount) {
        m_nodeCount = nodeCount;
    }
    public void setInterfaceCount(int interfaceCount) {
        m_interfaceCount = interfaceCount;
    }
    public void setOpennmsVersion(String opennmsVersion) {
        m_opennmsVersion = opennmsVersion;
    }
    

}

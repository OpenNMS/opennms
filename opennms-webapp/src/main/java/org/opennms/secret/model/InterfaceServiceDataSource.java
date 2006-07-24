package org.opennms.secret.model;


public class InterfaceServiceDataSource {
    private InterfaceService m_interfaceService;
    private DataSource m_dataSource;
    
    public DataSource getDataSource() {
        return m_dataSource;
    }
    public void setDataSource(DataSource dataSource) {
        m_dataSource = dataSource;
    }
    public InterfaceService getInterfaceService() {
        return m_interfaceService;
    }
    public void setInterfaceService(InterfaceService interfaceService) {
        m_interfaceService = interfaceService;
    }
}

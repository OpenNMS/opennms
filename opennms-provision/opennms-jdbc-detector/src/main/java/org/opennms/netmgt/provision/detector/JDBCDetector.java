package org.opennms.netmgt.provision.detector;

import org.opennms.netmgt.provision.support.BasicDetector;
import org.opennms.netmgt.provision.support.Client;
import org.opennms.netmgt.provision.support.ClientConversation.ResponseValidator;
import org.opennms.netmgt.provision.support.jdbc.DBTools;

public class JDBCDetector extends BasicDetector<JDBCRequest, JDBCResponse>{
    
    private static int DEFAULT_PORT = 3306;
    private static int DEFAULT_TIMEOUT = 1000;
    private static int DEFAULT_RETRIES = 0;
    
    
    private String m_dbDriver = DBTools.DEFAULT_JDBC_DRIVER;
    private String m_user = DBTools.DEFAULT_DATABASE_USER;
    private String m_password = DBTools.DEFAULT_DATABASE_PASSWORD;
    private String m_url = DBTools.DEFAULT_URL;
    
    protected JDBCDetector() {
        super(DEFAULT_PORT, DEFAULT_TIMEOUT, DEFAULT_RETRIES);
        setServiceName("JDBC");
    }

    @Override
    protected void onInit() {
        expectBanner(resultSetNotNull());
    }
    
    @Override
    protected Client<JDBCRequest, JDBCResponse> getClient() {
        JDBCClient client = new JDBCClient();
        client.setDbDriver(getDbDriver());
        client.setUser(getUser());
        client.setPassword(getPassword());
        client.setUrl(getUrl());
        return client;
    }
    
    private ResponseValidator<JDBCResponse> resultSetNotNull(){
        return new ResponseValidator<JDBCResponse>() {

            public boolean validate(JDBCResponse response) throws Exception {
                return response.resultSetNotNull();
            }
        };
    }

    public void setDbDriver(String dbDriver) {
        m_dbDriver = dbDriver;
    }

    public String getDbDriver() {
        return m_dbDriver;
    }

    public void setUser(String username) {
        m_user = username;
    }

    public String getUser() {
        return m_user;
    }

    public void setPassword(String password) {
        m_password = password;
    }

    public String getPassword() {
        return m_password;
    }

    public void setUrl(String url) {
        m_url = url;
    }

    public String getUrl() {
        return m_url;
    }
	
    
    
}
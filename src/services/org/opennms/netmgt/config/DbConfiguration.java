package org.opennms.netmgt.config;

import org.exolab.castor.jdo.conf.Database;
import org.exolab.castor.jdo.conf.Param;

public class DbConfiguration {

    private Database m_database;
	private String m_driverUser;
	private String m_driverPass;
	private String m_driverUrl;
	private String m_driverClassName;

	public DbConfiguration(Database database) {
        m_database = database;
        Param[] parms = getDatabase().getDriver().getParam();
        for (int i = 0; i < parms.length; i++) {
            if (parms[i].getName().equals("user"))
                setDriverUser(parms[i].getValue());
            else if (parms[i].getName().equals("password"))
                setDriverPass(parms[i].getValue());
        }
        setDriverUrl(getDatabase().getDriver().getUrl());
        setDriverClassName(getDatabase().getDriver().getClassName());
	}

    public Database getDatabase() {
        return m_database;
    }

    public void setDriverUser(String driverUser) {
        m_driverUser = driverUser;
    }

    public String getDriverUser() {
        return m_driverUser;
    }

    public void setDriverPass(String driverPass) {
        m_driverPass = driverPass;
    }

    public String getDriverPass() {
        return m_driverPass;
    }

    public void setDriverUrl(String driverUrl) {
        m_driverUrl = driverUrl;
    }

    public String getDriverUrl() {
        return m_driverUrl;
    }

    public void setDriverClassName(String driverClassName) {
        m_driverClassName = driverClassName;
    }

    public String getDriverClassName() {
        return m_driverClassName;
    }

}

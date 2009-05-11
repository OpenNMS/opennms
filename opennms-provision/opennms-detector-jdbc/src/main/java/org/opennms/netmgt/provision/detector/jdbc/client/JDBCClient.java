/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2008 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 *
 * For more information contact:
 * OpenNMS Licensing       <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 */
package org.opennms.netmgt.provision.detector.jdbc.client;

import java.io.IOException;
import java.net.InetAddress;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.SQLException;
import java.util.Properties;

import org.opennms.netmgt.provision.detector.jdbc.request.JDBCRequest;
import org.opennms.netmgt.provision.detector.jdbc.response.JDBCResponse;
import org.opennms.netmgt.provision.support.Client;
import org.opennms.netmgt.provision.support.jdbc.DBTools;

/**
 * @author thedesloge
 *
 */
public class JDBCClient implements Client<JDBCRequest, JDBCResponse> {

    private String m_dbDriver;
    private String m_user;
    private String m_password;
    private String m_url;
    
//    private ResultSet m_result;
    private Connection m_connection;
    
    public void close() {
        if(m_connection != null) {
            try {
                m_connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public void connect(InetAddress address, int port, int timeout) throws IOException, Exception {
        
        System.out.println("Loading JDBC driver: '" + getDbDriver() + "'");
        Driver driver = (Driver)Class.forName(getDbDriver()).newInstance();
        System.out.println("JDBC driver loaded: '" + getDbDriver() + "'");

        String url = DBTools.constructUrl(getUrl(), address.getCanonicalHostName());
        System.out.println("Constructed JDBC url: '" + url + "'");

        Properties props = new Properties();
        props.setProperty("user", getUser());
        props.setProperty("password", getPassword());
        props.setProperty("timeout", String.valueOf(timeout/1000));
        m_connection = driver.connect(url, props);

        System.out.println("Got database connection: '" + m_connection + "' (" + url + ", " + getUser() + ", " + getPassword() + ")");
        
    }

    public JDBCResponse receiveBanner() throws IOException, Exception {
        JDBCResponse response = new JDBCResponse();
        response.receive(m_connection);
        return response;
    }

    public JDBCResponse sendRequest(JDBCRequest request) throws IOException, Exception {
        return request.send(m_connection);
    }

    public void setDbDriver(String dbDriver) {
        m_dbDriver = dbDriver;
    }

    public String getDbDriver() {
        return m_dbDriver;
    }

    public void setUser(String user) {
        m_user = user;
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

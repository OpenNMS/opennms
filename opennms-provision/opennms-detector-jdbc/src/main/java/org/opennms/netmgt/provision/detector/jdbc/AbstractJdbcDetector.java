/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2009 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
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
package org.opennms.netmgt.provision.detector.jdbc;

import org.opennms.netmgt.provision.detector.jdbc.client.JDBCClient;
import org.opennms.netmgt.provision.detector.jdbc.request.JDBCRequest;
import org.opennms.netmgt.provision.detector.jdbc.response.JDBCResponse;
import org.opennms.netmgt.provision.support.BasicDetector;
import org.opennms.netmgt.provision.support.Client;
import org.opennms.netmgt.provision.support.ClientConversation.ResponseValidator;
import org.opennms.netmgt.provision.support.jdbc.DBTools;

/**
 * <p>Abstract AbstractJdbcDetector class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public abstract class AbstractJdbcDetector extends BasicDetector<JDBCRequest, JDBCResponse> {
    
    /** Constant <code>DEFAULT_PORT=3306</code> */
    protected static int DEFAULT_PORT = 3306;
    
    private String m_dbDriver = DBTools.DEFAULT_JDBC_DRIVER;
    private String m_user = DBTools.DEFAULT_DATABASE_USER;
    private String m_password = DBTools.DEFAULT_DATABASE_PASSWORD;
    private String m_url = DBTools.DEFAULT_URL;
    
    /**
     * <p>Constructor for AbstractJdbcDetector.</p>
     *
     * @param serviceName a {@link java.lang.String} object.
     * @param port a int.
     */
    protected AbstractJdbcDetector(String serviceName, int port) {
        super(serviceName, port);
        
    }
    
    /**
     * <p>resultSetNotNull</p>
     *
     * @return a {@link org.opennms.netmgt.provision.support.ClientConversation.ResponseValidator} object.
     */
    protected ResponseValidator<JDBCResponse> resultSetNotNull(){
        return new ResponseValidator<JDBCResponse>() {

            public boolean validate(JDBCResponse response) throws Exception {
                return response.resultSetNotNull();
            }
        };
    }
    
    /** {@inheritDoc} */
    @Override
    protected Client<JDBCRequest, JDBCResponse> getClient() {
        JDBCClient client = new JDBCClient();
        client.setDbDriver(getDbDriver());
        client.setUser(getUser());
        client.setPassword(getPassword());
        client.setUrl(getUrl());
        return client;
    }
    
    /**
     * <p>setDbDriver</p>
     *
     * @param dbDriver a {@link java.lang.String} object.
     */
    public void setDbDriver(String dbDriver) {
        m_dbDriver = dbDriver;
    }

    /**
     * <p>getDbDriver</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getDbDriver() {
        return m_dbDriver;
    }

    /**
     * <p>setUser</p>
     *
     * @param username a {@link java.lang.String} object.
     */
    public void setUser(String username) {
        m_user = username;
    }

    /**
     * <p>getUser</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getUser() {
        return m_user;
    }

    /**
     * <p>setPassword</p>
     *
     * @param password a {@link java.lang.String} object.
     */
    public void setPassword(String password) {
        m_password = password;
    }

    /**
     * <p>getPassword</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getPassword() {
        return m_password;
    }

    /**
     * <p>setUrl</p>
     *
     * @param url a {@link java.lang.String} object.
     */
    public void setUrl(String url) {
        m_url = url;
    }

    /**
     * <p>getUrl</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getUrl() {
        return m_url;
    }

}

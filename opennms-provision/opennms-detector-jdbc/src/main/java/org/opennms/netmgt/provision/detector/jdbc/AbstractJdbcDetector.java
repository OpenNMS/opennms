/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.opennms.netmgt.provision.detector.jdbc;

import org.opennms.core.utils.DBTools;
import org.opennms.netmgt.provision.detector.jdbc.client.JDBCClient;
import org.opennms.netmgt.provision.detector.jdbc.request.JDBCRequest;
import org.opennms.netmgt.provision.detector.jdbc.response.JDBCResponse;
import org.opennms.netmgt.provision.support.BasicDetector;
import org.opennms.netmgt.provision.support.Client;
import org.opennms.netmgt.provision.support.ResponseValidator;

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
     * @return a {@link org.opennms.netmgt.provision.support.ResponseValidator} object.
     */
    protected static ResponseValidator<JDBCResponse> resultSetNotNull(){
        return new ResponseValidator<JDBCResponse>() {

            @Override
            public boolean validate(JDBCResponse response) {
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

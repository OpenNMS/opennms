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

import org.opennms.netmgt.provision.detector.jdbc.request.JDBCRequest;
import org.opennms.netmgt.provision.detector.jdbc.response.JDBCResponse;
import org.opennms.netmgt.provision.support.RequestBuilder;
import org.opennms.netmgt.provision.support.ResponseValidator;

import java.util.regex.Pattern;

/**
 * <p>JdbcStoredProcedureDetector class.</p>
 *
 * @author ranger
 * @version $Id: $
 */

public class JdbcStoredProcedureDetector extends AbstractJdbcDetector {
    
    private static final String DEFAULT_STORED_PROCEDURE = "isRunning";
    private static final String DEFAULT_SCHEMA = "test";
    
    private String m_storedProcedure;
    private String m_schema = "test";
    
    /**
     * <p>Constructor for JdbcStoredProcedureDetector.</p>
     */
    public JdbcStoredProcedureDetector(){
        super("JdbcStoredProcedureDetector", 3306);
        setSchema(DEFAULT_SCHEMA);
        setStoredProcedure(DEFAULT_STORED_PROCEDURE);
    }
    
    /** {@inheritDoc} */
    @Override
    protected void onInit(){
        expectBanner(resultSetNotNull());
        send(storedProcedure(createProcedureCall(getSchema(), getStoredProcedure())), isValidProcedureCall());
    }
    
    private static ResponseValidator<JDBCResponse> isValidProcedureCall() {
        return new ResponseValidator<JDBCResponse>(){

            @Override
            public boolean validate(JDBCResponse response) {
                return response.isValidProcedureCall();
            }
            
        };
    }

    private static RequestBuilder<JDBCRequest> storedProcedure(final String storedProcedure) {
        return new RequestBuilder<JDBCRequest>(){

            @Override
            public JDBCRequest getRequest() {
                JDBCRequest request = new JDBCRequest();
                request.setStoredProcedure(storedProcedure);
                return request;
            }
            
        };
    }
    
    private String createProcedureCall(String schema, String procedure) {
        if(schema != null){
            return String.format("%s.%s", schema, procedure);
        }else{
            return procedure;
        }
    }

    /**
     * <p>setStoredProcedure</p>
     *
     * @param storedProcedure a {@link java.lang.String} object.
     */
    public void setStoredProcedure(String storedProcedure) {
        if (!isValidProcedureName(storedProcedure)) {
            throw new IllegalArgumentException(String.format("%s is not a valid procedure name", storedProcedure));
        }
        else {
            m_storedProcedure = storedProcedure;
        }
    }

    /**
     * Simple input validation for procedure names.
     * Procedure names are considered valid if they contain only alphanumerics and underscores.
     * SQL allows for other characters if they are properly escaped. We will ignore these for
     * now.
     *
     * @param procedure Procedure name to validate
     * @return true if valid, false if not
     */
    private boolean isValidProcedureName(String procedure) {
        return Pattern.matches("^[0-9a-zA-Z_]*$", procedure);
    }

    /**
     * <p>getStoredProcedure</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getStoredProcedure() {
        return m_storedProcedure;
    }

    /**
     * <p>setSchema</p>
     *
     * @param schema a {@link java.lang.String} object.
     */
    public void setSchema(String schema) {
        m_schema = schema;
    }

    /**
     * <p>getSchema</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getSchema() {
        return m_schema;
    }

}

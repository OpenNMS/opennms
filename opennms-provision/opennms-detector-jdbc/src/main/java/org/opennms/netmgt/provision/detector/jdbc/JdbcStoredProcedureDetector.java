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

import org.opennms.netmgt.provision.detector.jdbc.request.JDBCRequest;
import org.opennms.netmgt.provision.detector.jdbc.response.JDBCResponse;
import org.opennms.netmgt.provision.support.ClientConversation.RequestBuilder;
import org.opennms.netmgt.provision.support.ClientConversation.ResponseValidator;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
/**
 * <p>JdbcStoredProcedureDetector class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
@Scope("prototype")
public class JdbcStoredProcedureDetector extends AbstractJdbcDetector {
    
    private final static String DEFAULT_STORED_PROCEDURE = "isRunning";
    private final static String DEFAULT_SCHEMA = "test";
    
    private String m_storedProcedure;
    private String m_schema = "test";
    
    /**
     * <p>Constructor for JdbcStoredProcedureDetector.</p>
     */
    protected JdbcStoredProcedureDetector(){
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
    
    private ResponseValidator<JDBCResponse> isValidProcedureCall() {
        return new ResponseValidator<JDBCResponse>(){

            public boolean validate(JDBCResponse response) throws Exception {
                return response.isValidProcedureCall();
            }
            
        };
    }

    private RequestBuilder<JDBCRequest> storedProcedure(final String storedProcedure) {
        return new RequestBuilder<JDBCRequest>(){

            public JDBCRequest getRequest() throws Exception {
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
        m_storedProcedure = storedProcedure;
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

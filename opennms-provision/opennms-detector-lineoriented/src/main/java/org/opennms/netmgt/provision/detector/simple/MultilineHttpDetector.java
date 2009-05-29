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
package org.opennms.netmgt.provision.detector.simple;

import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.opennms.netmgt.provision.detector.simple.request.LineOrientedRequest;
import org.opennms.netmgt.provision.detector.simple.response.MultilineHttpResponse;
import org.opennms.netmgt.provision.support.AsyncBasicDetector;
import org.opennms.netmgt.provision.support.AsyncClientConversation.ResponseValidator;
import org.opennms.netmgt.provision.support.codec.MultilineHttpProtocolFactory;


public abstract class MultilineHttpDetector extends AsyncBasicDetector<LineOrientedRequest, MultilineHttpResponse> {
    
    private static final String DEFAULT_SERVICE_NAME = "HTTP";
    private static final int DEFAULT_PORT = 80;
    private static String DEFAULT_URL="/";
    private static int DEFAULT_MAX_RET_CODE = 399;
    
    private String m_url;
    private int m_maxRetCode;
    private boolean m_checkRetCode = false;
    
    public MultilineHttpDetector(){
        super(DEFAULT_SERVICE_NAME, DEFAULT_PORT);
        contructDefaults();
    }
    
    public MultilineHttpDetector(String serviceName, int port) {
        super(serviceName, port);
        contructDefaults();
    }
    
    private void contructDefaults() {
        setProtocolCodecFilter(new ProtocolCodecFilter(new MultilineHttpProtocolFactory()));
        setUrl(DEFAULT_URL);
        setMaxRetCode(DEFAULT_MAX_RET_CODE);
    }
    
    

    @Override
    protected void onInit() {
        send(request(httpCommand("GET")), contains(DEFAULT_SERVICE_NAME, getUrl(), isCheckRetCode(), getMaxRetCode()));
    }
    
    /**
     * @param string
     * @return
     */
    protected String httpCommand(String command) {
        
        return String.format("%s %s  HTTP/1.0\r\n\r\n", command, getUrl());
    }
    
    protected LineOrientedRequest request(String command) {
        return new LineOrientedRequest(command);
    }
    
    protected ResponseValidator<MultilineHttpResponse> contains(final String pattern, final String url, final boolean isCheckCode, final int maxRetCode){
        return new ResponseValidator<MultilineHttpResponse>(){

            public boolean validate(MultilineHttpResponse message) {
                
                try {
                    return message.validateResponse(pattern, url, isCheckCode, maxRetCode);
                } catch (Exception e) {
                    e.printStackTrace();
                    return false;
                }
            }
            
        };
    }

    public void setUrl(String url) {
        m_url = url;
    }

    public String getUrl() {
        return m_url;
    }

    public void setMaxRetCode(int maxRetCode) {
        m_maxRetCode = maxRetCode;
    }

    public int getMaxRetCode() {
        return m_maxRetCode;
    }

    public void setCheckRetCode(boolean checkRetCode) {
        m_checkRetCode = checkRetCode;
    }

    public boolean isCheckRetCode() {
        return m_checkRetCode;
    }

}

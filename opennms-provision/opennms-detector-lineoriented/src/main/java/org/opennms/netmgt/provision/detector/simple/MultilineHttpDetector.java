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
import org.opennms.core.utils.LogUtils;
import org.opennms.netmgt.provision.detector.simple.request.LineOrientedRequest;
import org.opennms.netmgt.provision.detector.simple.response.MultilineHttpResponse;
import org.opennms.netmgt.provision.support.AsyncBasicDetector;
import org.opennms.netmgt.provision.support.AsyncClientConversation.ResponseValidator;
import org.opennms.netmgt.provision.support.codec.MultilineHttpProtocolFactory;


/**
 * <p>Abstract MultilineHttpDetector class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public abstract class MultilineHttpDetector extends AsyncBasicDetector<LineOrientedRequest, MultilineHttpResponse> {
    
    private static final String DEFAULT_SERVICE_NAME = "HTTP";
    private static final int DEFAULT_PORT = 80;
    private static String DEFAULT_URL="/";
    private static int DEFAULT_MAX_RET_CODE = 399;
    
    private String m_url;
    private int m_maxRetCode;
    private boolean m_checkRetCode = false;
    
    /**
     * <p>Constructor for MultilineHttpDetector.</p>
     */
    public MultilineHttpDetector(){
        super(DEFAULT_SERVICE_NAME, DEFAULT_PORT);
        contructDefaults();
    }
    
    /**
     * <p>Constructor for MultilineHttpDetector.</p>
     *
     * @param serviceName a {@link java.lang.String} object.
     * @param port a int.
     */
    public MultilineHttpDetector(final String serviceName, final int port) {
        super(serviceName, port);
        contructDefaults();
    }
    
    private void contructDefaults() {
        setProtocolCodecFilter(new ProtocolCodecFilter(new MultilineHttpProtocolFactory()));
        setUrl(DEFAULT_URL);
        setMaxRetCode(DEFAULT_MAX_RET_CODE);
    }
    
    

    /** {@inheritDoc} */
    @Override
    protected void onInit() {
        send(request(httpCommand("GET")), contains(DEFAULT_SERVICE_NAME, getUrl(), isCheckRetCode(), getMaxRetCode()));
    }
    
    /**
     * <p>httpCommand</p>
     *
     * @param command a {@link java.lang.String} object.
     * @return a {@link java.lang.String} object.
     */
    protected String httpCommand(final String command) {
        
        return String.format("%s %s  HTTP/1.0\r\n\r\n", command, getUrl());
    }
    
    /**
     * <p>request</p>
     *
     * @param command a {@link java.lang.String} object.
     * @return a {@link org.opennms.netmgt.provision.detector.simple.request.LineOrientedRequest} object.
     */
    protected LineOrientedRequest request(final String command) {
        return new LineOrientedRequest(command);
    }
    
    /**
     * <p>contains</p>
     *
     * @param pattern a {@link java.lang.String} object.
     * @param url a {@link java.lang.String} object.
     * @param isCheckCode a boolean.
     * @param maxRetCode a int.
     * @return a {@link org.opennms.netmgt.provision.support.AsyncClientConversation.ResponseValidator} object.
     */
    protected ResponseValidator<MultilineHttpResponse> contains(final String pattern, final String url, final boolean isCheckCode, final int maxRetCode){
        return new ResponseValidator<MultilineHttpResponse>(){

            public boolean validate(final MultilineHttpResponse message) {
                
                try {
                    return message.validateResponse(pattern, url, isCheckCode, maxRetCode);
                } catch (final Exception e) {
                    LogUtils.debugf(this, e, "Unable to validate response");
                    return false;
                }
            }
            
        };
    }

    /**
     * <p>setUrl</p>
     *
     * @param url a {@link java.lang.String} object.
     */
    public void setUrl(final String url) {
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

    /**
     * <p>setMaxRetCode</p>
     *
     * @param maxRetCode a int.
     */
    public void setMaxRetCode(final int maxRetCode) {
        m_maxRetCode = maxRetCode;
    }

    /**
     * <p>getMaxRetCode</p>
     *
     * @return a int.
     */
    public int getMaxRetCode() {
        return m_maxRetCode;
    }

    /**
     * <p>setCheckRetCode</p>
     *
     * @param checkRetCode a boolean.
     */
    public void setCheckRetCode(final boolean checkRetCode) {
        m_checkRetCode = checkRetCode;
    }

    /**
     * <p>isCheckRetCode</p>
     *
     * @return a boolean.
     */
    public boolean isCheckRetCode() {
        return m_checkRetCode;
    }

}

/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2008-2013 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2013 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.provision.detector.simple;

import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.opennms.netmgt.provision.detector.simple.request.LineOrientedRequest;
import org.opennms.netmgt.provision.detector.simple.response.HttpStatusResponse;
import org.opennms.netmgt.provision.support.AsyncBasicDetectorMinaImpl;
import org.opennms.netmgt.provision.support.ResponseValidator;
import org.opennms.netmgt.provision.support.codec.HttpProtocolCodecFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * <p>HttpDetector class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
@Component
@Scope("prototype")
public class HttpDetector extends AsyncBasicDetectorMinaImpl<LineOrientedRequest, HttpStatusResponse> {
    
    private static final Logger LOG = LoggerFactory.getLogger(HttpDetector.class);
    private static final String DEFAULT_SERVICE_NAME = "HTTP";
    private static final int DEFAULT_PORT = 80;
    private static String DEFAULT_URL="/";
    private static int DEFAULT_MAX_RET_CODE = 399;
    
    private String m_url;
    private int m_maxRetCode;
    private boolean m_checkRetCode = false;

    /**
     * Default constructor
     */
    public HttpDetector() {
        super(DEFAULT_SERVICE_NAME, DEFAULT_PORT);
        contructDefaults();
    }

    /**
     * Constructor for creating a non-default service based on this protocol
     *
     * @param serviceName a {@link java.lang.String} object.
     * @param port a int.
     */
    public HttpDetector(final String serviceName, final int port) {
        super(serviceName, port);
        contructDefaults();
    }
    
    private void contructDefaults() {
        setProtocolCodecFilter(new ProtocolCodecFilter(new HttpProtocolCodecFactory()));
        setUrl(DEFAULT_URL);
        setMaxRetCode(DEFAULT_MAX_RET_CODE);
    }
    
    /**
     * <p>onInit</p>
     */
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
    protected static LineOrientedRequest request(final String command) {
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
    protected static ResponseValidator<HttpStatusResponse> contains(final String pattern, final String url, final boolean isCheckCode, final int maxRetCode){
        return new ResponseValidator<HttpStatusResponse>(){

            @Override
            public boolean validate(final HttpStatusResponse message) {
                
                try {
                    return message.validateResponse(pattern, url, isCheckCode, maxRetCode);
                } catch (final Exception e) {
                    LOG.debug("Failed to validate response.", e);
                    return false;
                }
            }
            
        };
    }
    
    
    //Public setters and getters
    
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

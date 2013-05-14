/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2008-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
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
import org.opennms.netmgt.provision.detector.simple.response.LineOrientedResponse;
import org.opennms.netmgt.provision.detector.simple.support.TcpDetectorHandler;
import org.opennms.netmgt.provision.support.ConversationExchange;
import org.opennms.netmgt.provision.support.ResponseValidator;
import org.opennms.netmgt.provision.support.codec.TcpCodecFactory;
import org.opennms.netmgt.provision.support.codec.TcpLineDecoder;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * <p>TcpDetector class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
@Component
@Scope("prototype")
public class TcpDetector extends AsyncLineOrientedDetectorMinaImpl {
    
    private static final String DEFAULT_SERVICE_NAME = "TCP";
    private static final int DEFAULT_PORT = 23;
    
    private String m_banner = null;
    
    /**
     * Default constructor
     */
    public TcpDetector() {
        this(DEFAULT_SERVICE_NAME, DEFAULT_PORT);
    }
    
    /**
     * Constructor for creating a non-default service based on this protocol
     *
     * @param serviceName a {@link java.lang.String} object.
     * @param port a int.
     */
    public TcpDetector(final String serviceName, final int port) {
        super(serviceName, port);
        setDetectorHandler(new TcpDetectorHandler());
        setProtocolCodecFilter(new ProtocolCodecFilter(new TcpCodecFactory(CHARSET_UTF8)));
    }

    /**
     * <p>onInit</p>
     */
    @Override
    protected void onInit() {
        if(getBanner() != null) {
            expectBanner(matches(getBanner()));
        }else {
            getConversation().addExchange(testBannerlessConnection());
        }
    }
    
    private static ConversationExchange<LineOrientedRequest, LineOrientedResponse> testBannerlessConnection() {

        return new ConversationExchange<LineOrientedRequest, LineOrientedResponse>() {

            @Override
            public boolean validate(final LineOrientedResponse response) {
                return response.equals(TcpLineDecoder.NO_MESSAGES_RECEIVED);
            }

            @Override
            public LineOrientedRequest getRequest() {
                return null;
            }
        };
    }

    /**
     * <p>matches</p>
     *
     * @param regex a {@link java.lang.String} object.
     * @return a {@link org.opennms.netmgt.provision.support.AsyncClientConversation.ResponseValidator} object.
     */
    public static ResponseValidator<LineOrientedResponse> matches(final String regex){
        return new ResponseValidator<LineOrientedResponse>() {

            @Override
            public boolean validate(final LineOrientedResponse response) {
                // Make sure that the response matches the regex and that it is not an instance of the
                // special token that represents that no banner was received.
                return response.matches(regex) && !response.equals(TcpLineDecoder.NO_MESSAGES_RECEIVED);
            }

        };
    }

    /**
     * <p>setBanner</p>
     *
     * @param banner a {@link java.lang.String} object.
     */
    public void setBanner(final String banner) {
        m_banner = banner;
    }

    /**
     * <p>getBanner</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getBanner() {
        return m_banner;
    }
    
}

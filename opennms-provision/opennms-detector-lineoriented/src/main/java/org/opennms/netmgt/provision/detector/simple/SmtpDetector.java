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
package org.opennms.netmgt.provision.detector.simple;

import java.nio.charset.Charset;

import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.opennms.netmgt.provision.support.codec.MultilineOrientedCodecFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;



@Component
/**
 * <p>SmtpDetector class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
@Scope("prototype")
public class SmtpDetector extends AsyncMultilineDetector {
    
    private static final ProtocolCodecFilter PROTOCOL_CODEC_FILTER = new ProtocolCodecFilter(new MultilineOrientedCodecFactory( Charset.forName("UTF-8"), "-"));
    private static final String DEFAULT_SERVICE_NAME = "SMTP";
    private static final int DEFAULT_PORT = 25;

    /**
     * Default constructor
     */
    public SmtpDetector() {
        super(DEFAULT_SERVICE_NAME, DEFAULT_PORT);
    }
    
    /**
     * Constructor for creating a non-default service based on this protocol
     *
     * @param serviceName a {@link java.lang.String} object.
     * @param port a int.
     */
    public SmtpDetector(final String serviceName, final int port) {
        super(serviceName, port);
    }
    
    /**
     * <p>onInit</p>
     */
    public void onInit() {
        setProtocolCodecFilter(PROTOCOL_CODEC_FILTER);
        
        expectBanner(startsWith("220"));
        send(request("HELO LOCALHOST"), startsWith("250"));
        send(request("QUIT"), startsWith("221"));
    }
    
    
    
}

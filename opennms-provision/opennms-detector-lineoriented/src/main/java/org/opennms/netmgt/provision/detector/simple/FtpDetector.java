/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2008-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
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
import org.opennms.netmgt.provision.support.codec.MultilineOrientedCodecFactory;

/**
 * <p>FtpDetector class.</p>
 *
 * @author ranger
 * @version $Id: $
 */

public class FtpDetector extends AsyncMultilineDetectorMinaImpl {
    
    private static final String DEFAULT_SERVICE_NAME = "FTP";
    private static final int DEFAULT_PORT = 21;
    private String m_multilineIndicator = "-";
    
    /**
     * Default constructor
     */
    public FtpDetector() {
        super(DEFAULT_SERVICE_NAME, DEFAULT_PORT);
    }
    
    /**
     * Constructor for creating a non-default service based on this protocol
     *
     * @param serviceName a {@link java.lang.String} object.
     * @param port a int.
     */
    public FtpDetector(final String serviceName, final int port) {
        super(serviceName, port);
    }

    /**
     * <p>onInit</p>
     */
    @Override
    protected void onInit() {
        //setup the correct codec for this Detector
        setProtocolCodecFilter(new ProtocolCodecFilter(new MultilineOrientedCodecFactory(CHARSET_UTF8, getMultilineIndicator())));
        
        expectBanner(expectCodeRange(100, 600));
        send(request("quit"), expectCodeRange(100,600));
    }

    /**
     * <p>setMultilineIndicator</p>
     *
     * @param multilineIndicator a {@link java.lang.String} object.
     */
    public void setMultilineIndicator(final String multilineIndicator) {
        m_multilineIndicator = multilineIndicator;
    }

    /**
     * <p>getMultilineIndicator</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getMultilineIndicator() {
        return m_multilineIndicator;
    }
}

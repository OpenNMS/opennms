/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012 The OpenNMS Group, Inc.
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

import java.nio.charset.Charset;

import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.opennms.netmgt.provision.detector.simple.request.LineOrientedRequest;
import org.opennms.netmgt.provision.detector.simple.response.LineOrientedResponse;
import org.opennms.netmgt.provision.support.AsyncBasicDetectorMinaImpl;
import org.opennms.netmgt.provision.support.ResponseValidator;
import org.opennms.netmgt.provision.support.codec.LineOrientedCodecFactory;

/**
 * <p>Abstract AsyncLineOrientedDetector class.</p>
 *
 * @author Donald Desloge
 * @version $Id: $
 */
public abstract class AsyncLineOrientedDetectorMinaImpl extends AsyncBasicDetectorMinaImpl<LineOrientedRequest, LineOrientedResponse> {

    protected static final Charset CHARSET_UTF8 = Charset.forName("UTF-8");

    /**
     * <p>Constructor for AsyncLineOrientedDetector.</p>
     *
     * @param serviceName a {@link java.lang.String} object.
     * @param port a int.
     */
    public AsyncLineOrientedDetectorMinaImpl(final String serviceName, final int port) {
        super(serviceName, port);
        setProtocolCodecFilter(new ProtocolCodecFilter(new LineOrientedCodecFactory(CHARSET_UTF8)));
    }

    /**
     * <p>Constructor for AsyncLineOrientedDetector.</p>
     *
     * @param port a int.
     * @param timeout a int.
     * @param retries a int.
     * @param serviceName a {@link java.lang.String} object.
     */
    public AsyncLineOrientedDetectorMinaImpl(final String serviceName, final int port, final int timeout, final int retries) {
        super(serviceName, port, timeout, retries);
        setProtocolCodecFilter(new ProtocolCodecFilter(new LineOrientedCodecFactory(CHARSET_UTF8)));
    }

    /** {@inheritDoc} */
    @Override
    protected ResponseValidator<LineOrientedResponse> startsWith(final String prefix) {
        return new ResponseValidator<LineOrientedResponse>() {

            @Override
            public boolean validate(final LineOrientedResponse response) {
                return response.startsWith(prefix);
            }
            
        };
    }
    
    /** {@inheritDoc} */
    @Override
    public ResponseValidator<LineOrientedResponse> find(final String regex){
        return new ResponseValidator<LineOrientedResponse>() {

            @Override
            public boolean validate(final LineOrientedResponse response) {
                return response.find(regex);
            }
          
            
        };
    }
    
    /**
     * <p>request</p>
     *
     * @param command a {@link java.lang.String} object.
     * @return a {@link org.opennms.netmgt.provision.detector.simple.request.LineOrientedRequest} object.
     */
    public LineOrientedRequest request(final String command) {
        return new LineOrientedRequest(command);
    }

}

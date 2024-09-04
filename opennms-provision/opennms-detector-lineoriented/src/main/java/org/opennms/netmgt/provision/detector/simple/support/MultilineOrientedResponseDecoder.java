/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.provision.detector.simple.support;

import java.util.List;

import io.netty.channel.ChannelHandlerContext;

import io.netty.handler.codec.MessageToMessageDecoder;
import org.opennms.netmgt.provision.detector.simple.response.MultilineOrientedResponse;

/**
 * <p>MultilineOrientedResponseDecoder class.</p>
 *
 * @author Seth
 */
public class MultilineOrientedResponseDecoder extends MessageToMessageDecoder<Object> {

    public static final String DEFAULT_MULTILINE_INDICATOR = "-";

    private final String m_multilineIndicator;
    private MultilineOrientedResponse m_response;

    public MultilineOrientedResponseDecoder() {
        this(DEFAULT_MULTILINE_INDICATOR);
    }

    /**
     * <p>Constructor for MultilineOrientedResponseDecoder.</p>
     *
     * @param multilineIndicator a {@link java.lang.String} object.
     */
    public MultilineOrientedResponseDecoder(final String multilineIndicator) {
        m_multilineIndicator = multilineIndicator;
    }

    /**
     * This method decodes {@link String} objects into {@link MultilineOrientedResponse} instances
     * that contain each line of the string response.
     */
    @Override
    protected void decode(final ChannelHandlerContext ctx, final Object msg, final List<Object> messages) throws Exception {
        // We can only decode strings
        if (!(msg instanceof String)) {
            messages.add(msg);
        }
        // Construct a new response if there isn't one yet
        if (m_response == null) {
            m_response = new MultilineOrientedResponse();
        }
        String response = (String)msg;
        m_response.addLine(response);
        if(checkIndicator(response)) {
            // Do nothing; if the multi-line indicator is present then 
            // continue to accumulate lines into the m_response instance
        } else {
            MultilineOrientedResponse retval = m_response;
            m_response = null;
            messages.add(retval);
        }
    }

    /**
     * <p>checkIndicator</p>
     *
     * @param line a {@link String} object.
     * @return a boolean.
     */
    protected boolean checkIndicator(final String line) {
        return line.substring(3, 4).equals(getMultilineIndicator());
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

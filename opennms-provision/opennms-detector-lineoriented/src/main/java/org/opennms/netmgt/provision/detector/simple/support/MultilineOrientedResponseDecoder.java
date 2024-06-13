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

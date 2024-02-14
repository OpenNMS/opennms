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

import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.oneone.OneToOneDecoder;
import org.opennms.netmgt.provision.detector.simple.response.MultilineOrientedResponse;

/**
 * <p>MultilineOrientedResponseDecoder class.</p>
 *
 * @author Seth
 */
public class MultilineOrientedResponseDecoder extends OneToOneDecoder {

    public static final String DEFAULT_MULTILINE_INDICATOR = "-";

    private final String m_multilineIndicator;
    private MultilineOrientedResponse m_response;

    /**
     * <p>Constructor for MultilineOrientedResponseDecoder.</p>
     *
     * @param multilineIndicator a {@link java.lang.String} object.
     */
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
    protected Object decode(final ChannelHandlerContext ctx, final Channel channel, final Object msg) throws Exception {
        // We can only decode strings
        if (!(msg instanceof String)) {
            return msg;
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
            return null;
        } else {
            MultilineOrientedResponse retval = m_response;
            m_response = null;
            return retval;
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

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
package org.opennms.netmgt.provision.detector.simple;

import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.Delimiters;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.util.CharsetUtil;
import org.opennms.netmgt.provision.detector.simple.request.LineOrientedRequest;
import org.opennms.netmgt.provision.detector.simple.response.LineOrientedResponse;
import org.opennms.netmgt.provision.detector.simple.support.LineOrientedRequestEncoder;
import org.opennms.netmgt.provision.detector.simple.support.LineOrientedResponseDecoder;
import org.opennms.netmgt.provision.support.AsyncBasicDetectorNettyImpl;
import org.opennms.netmgt.provision.support.ResponseValidator;

/**
 * <p>Abstract AsyncLineOrientedDetectorNettyImpl class.</p>
 *
 * CAUTION: This class is unused. This implementation has never been in production.
 *
 * @author Donald Desloge
 * @version $Id: $
 */
public abstract class AsyncLineOrientedDetectorNettyImpl extends AsyncBasicDetectorNettyImpl<LineOrientedRequest, LineOrientedResponse> {

    /**
     * <p>Constructor for AsyncLineOrientedDetectorNettyImpl.</p>
     *
     * @param serviceName a {@link java.lang.String} object.
     * @param port a int.
     */
    public AsyncLineOrientedDetectorNettyImpl(final String serviceName, final int port) {
        super(serviceName, port);
        //setProtocolCodecFilter(new ProtocolCodecFilter(new LineOrientedCodecFactory(CHARSET_UTF8)));
    }

    /**
     * <p>Constructor for AsyncLineOrientedDetectorNettyImpl.</p>
     *
     * @param port a int.
     * @param timeout a int.
     * @param retries a int.
     * @param serviceName a {@link java.lang.String} object.
     */
    public AsyncLineOrientedDetectorNettyImpl(final String serviceName, final int port, final int timeout, final int retries) {
        super(serviceName, port, timeout, retries);
        //setProtocolCodecFilter(new ProtocolCodecFilter(new LineOrientedCodecFactory(CHARSET_UTF8)));
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

    @Override
    protected void appendToPipeline(ChannelPipeline retval) {
        // Upstream handlers
        retval.addLast("frameDecoder", new DelimiterBasedFrameDecoder(1024, Delimiters.lineDelimiter()));
        retval.addLast("stringDecoder", new StringDecoder(CharsetUtil.UTF_8));
        retval.addLast("lineDecoder", new LineOrientedResponseDecoder());

        // Downstream handlers
        retval.addLast("stringEncoder", new StringEncoder(CharsetUtil.UTF_8));
        retval.addLast("lineEncoder", new LineOrientedRequestEncoder());
    }
}

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
package org.opennms.netmgt.provision.support.codec;

import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;

import org.apache.mina.core.buffer.IoBuffer;
import org.opennms.netmgt.provision.detector.simple.response.HttpStatusResponse;

/**
 * <p>HttpStatusResponseDecoder class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public class HttpStatusResponseDecoder extends LineOrientedDecoder {

    /**
     * <p>Constructor for HttpStatusResponseDecoder.</p>
     *
     * @param charset a {@link java.nio.charset.Charset} object.
     */
    public HttpStatusResponseDecoder(final Charset charset) {
        super(charset);
        
    }
    
    /** {@inheritDoc} */
    @Override
    protected Object parseCommand(final IoBuffer in) throws CharacterCodingException {
        return new HttpStatusResponse(in.getString(getCharset().newDecoder()));
    }
}

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

import java.nio.charset.Charset;

import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFactory;
import org.apache.mina.filter.codec.ProtocolDecoder;
import org.apache.mina.filter.codec.ProtocolEncoder;

/**
 * <p>LineOrientedCodecFactory class.</p>
 *
 * @author Donald Desloge
 * @version $Id: $
 */
public class LineOrientedCodecFactory implements ProtocolCodecFactory {
    
    private final LineOrientedEncoder m_encoder;
    private final LineOrientedDecoder m_decoder;
    
    /**
     * <p>Constructor for LineOrientedCodecFactory.</p>
     *
     * @param charset a {@link java.nio.charset.Charset} object.
     */
    public LineOrientedCodecFactory(final Charset charset) {
        this(charset, null);
    }
    
    /**
     * <p>Constructor for LineOrientedCodecFactory.</p>
     *
     * @param charset a {@link java.nio.charset.Charset} object.
     * @param delimit a {@link java.lang.String} object.
     */
    public LineOrientedCodecFactory(final Charset charset, final String delimit) {
        m_encoder = new LineOrientedEncoder(charset);
        m_decoder = new LineOrientedDecoder(charset);
    }
    
    /** {@inheritDoc} */
    @Override
    public ProtocolDecoder getDecoder(final IoSession session) throws Exception {
        return m_decoder;
    }

    /** {@inheritDoc} */
    @Override
    public ProtocolEncoder getEncoder(final IoSession session) throws Exception {
        return m_encoder;
    }

}

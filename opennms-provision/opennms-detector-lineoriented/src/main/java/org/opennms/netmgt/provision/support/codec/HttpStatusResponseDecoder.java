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

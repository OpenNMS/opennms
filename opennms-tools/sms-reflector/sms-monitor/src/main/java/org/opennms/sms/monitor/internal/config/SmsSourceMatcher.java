/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2009-2012 The OpenNMS Group, Inc.
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

package org.opennms.sms.monitor.internal.config;

import java.util.Arrays;

import javax.xml.bind.annotation.XmlRootElement;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.opennms.sms.monitor.MobileSequenceSession;
import org.opennms.sms.reflector.smsservice.MobileMsgRequest;
import org.opennms.sms.reflector.smsservice.MobileMsgResponse;
import org.opennms.sms.reflector.smsservice.SmsResponse;

/**
 * <p>SmsSourceMatcher class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
@XmlRootElement(name="validate-source")
public class SmsSourceMatcher extends SequenceResponseMatcher {
    private static final Logger LOG = LoggerFactory.getLogger(SmsSourceMatcher.class);

	/**
	 * <p>Constructor for SmsSourceMatcher.</p>
	 */
	public SmsSourceMatcher() {
		super();
	}
	
	/**
	 * <p>Constructor for SmsSourceMatcher.</p>
	 *
	 * @param originator a {@link java.lang.String} object.
	 */
	public SmsSourceMatcher(String originator) {
		super(originator);
	}

	/** {@inheritDoc} */
	@Override
    public boolean matches(MobileSequenceSession session, MobileMsgRequest request, MobileMsgResponse response) {
        LOG.trace("smsFrom.matches({}, {}, {})", Arrays.asList(session.substitute(getText()), request, response));
        return response instanceof SmsResponse && session.eqOrMatches(getText(), ((SmsResponse)response).getOriginator());
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return "smsSourceMatches(" + getText() +")";
    }

}

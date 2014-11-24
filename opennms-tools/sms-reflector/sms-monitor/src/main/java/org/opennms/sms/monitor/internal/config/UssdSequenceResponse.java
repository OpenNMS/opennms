/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2009-2014 The OpenNMS Group, Inc.
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

package org.opennms.sms.monitor.internal.config;

import javax.xml.bind.annotation.XmlRootElement;

import org.opennms.sms.monitor.MobileSequenceSession;
import org.opennms.sms.reflector.smsservice.MobileMsgRequest;
import org.opennms.sms.reflector.smsservice.MobileMsgResponse;
import org.opennms.sms.reflector.smsservice.UssdResponse;

/**
 * <p>UssdSequenceResponse class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
@XmlRootElement(name="ussd-response")
public class UssdSequenceResponse extends MobileSequenceResponse {

    /**
     * <p>Constructor for UssdSequenceResponse.</p>
     */
    public UssdSequenceResponse() {
		super();
	}
	
	/**
	 * <p>Constructor for UssdSequenceResponse.</p>
	 *
	 * @param label a {@link java.lang.String} object.
	 */
	public UssdSequenceResponse(String label) {
		super(label);
	}
	
	/**
	 * <p>Constructor for UssdSequenceResponse.</p>
	 *
	 * @param gatewayId a {@link java.lang.String} object.
	 * @param label a {@link java.lang.String} object.
	 */
	public UssdSequenceResponse(String gatewayId, String label) {
		super(gatewayId, label);
	}

	/** {@inheritDoc} */
	@Override
    protected boolean matchesResponseType(MobileMsgRequest request, MobileMsgResponse response) {
        return response instanceof UssdResponse;
    }

    /** {@inheritDoc} */
    @Override
    public void processResponse(MobileSequenceSession session, MobileMsgRequest request, MobileMsgResponse response) {
        if (response instanceof UssdResponse) {
            
        }
    }



}

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

/**
 * <p>USSDNotification class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
package org.opennms.sms.reflector.commands.internal;

import org.opennms.core.utils.ThreadCategory;
import org.smslib.IUSSDNotification;
import org.smslib.USSDResponse;
public class USSDNotification implements IUSSDNotification {
    /** {@inheritDoc} */
    @Override
    public void process(String gatewayId, USSDResponse ussdResponse) {
        debugf(">>> Inbound USSD detected from gateway %s : %s",  gatewayId, ussdResponse.getContent());
        debugf(">>> USSD session status: %s", ussdResponse.getSessionStatus());
    }
    
    private void debugf(String format, Object ...args){
        ThreadCategory log = ThreadCategory.getInstance(USSDNotification.class);
        
        if(log.isDebugEnabled()){
            log.debug(String.format(format, args));
        }
    }
    
}

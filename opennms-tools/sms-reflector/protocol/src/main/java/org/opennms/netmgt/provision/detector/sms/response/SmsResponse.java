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

package org.opennms.netmgt.provision.detector.sms.response;

import org.opennms.sms.phonebook.Phonebook;
import org.opennms.sms.phonebook.PhonebookException;
import org.opennms.sms.phonebook.PropertyPhonebook;


/**
 * Response handler for the <code>SmsDetector</code>  Uses the
 *
 * @author <a href="mailto:david@opennms.org">David Hustace</a>
 * @version $Id: $
 */
public class SmsResponse {
    
    //do this for now.
    private Phonebook m_smsDirectory = new PropertyPhonebook();

    /**
     * <p>isSms</p>
     *
     * @param ipAddr a {@link java.lang.String} object.
     * @return a boolean.
     */
    public boolean isSms(String ipAddr) {
        
        try {
            m_smsDirectory.getTargetForAddress(ipAddr);
        } catch (PhonebookException e) {
            return false;
        }
        
        return true;
    }

}

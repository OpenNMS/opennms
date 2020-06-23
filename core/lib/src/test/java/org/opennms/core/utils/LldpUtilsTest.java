/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2011-2014 The OpenNMS Group, Inc.
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

package org.opennms.core.utils;

import static org.junit.Assert.assertEquals;
import org.junit.Test;

public class LldpUtilsTest {
    
    @Test
    /* 
     * From NMS-7148: 01:ac:14:14:8b
     * 01 is IP version 4 end the address is 172.20.20.139
     */
    public void testNMS7184() throws Exception {
        String ianafamilyaddress = "01:ac:14:14:8b";
        assertEquals(1, LldpUtils.IanaFamilyAddressStringToType(ianafamilyaddress).intValue());
        assertEquals("172.20.20.139",LldpUtils.decodeNetworkAddress(ianafamilyaddress));
    }
    
}

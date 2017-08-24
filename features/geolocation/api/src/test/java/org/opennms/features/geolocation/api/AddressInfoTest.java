/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2017-2017 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2017 The OpenNMS Group, Inc.
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

package org.opennms.features.geolocation.api;


import org.junit.Assert;
import org.junit.Test;

public class AddressInfoTest {

    @Test
    public void test() {
        AddressInfo addressInfo = new AddressInfo();
        addressInfo.setAddress1("220 Chatham Business Dr");
        addressInfo.setCity("Pittsboro");
        addressInfo.setState("NC");
        addressInfo.setZip("27312");

        Assert.assertEquals("220 Chatham Business Dr, Pittsboro, NC 27312", addressInfo.toAddressString());

        addressInfo.setState(null);
        Assert.assertEquals("220 Chatham Business Dr, Pittsboro, 27312", addressInfo.toAddressString());

        addressInfo.setAddress2("ive");
        Assert.assertEquals("220 Chatham Business Dr ive, Pittsboro, 27312", addressInfo.toAddressString());
    }

}


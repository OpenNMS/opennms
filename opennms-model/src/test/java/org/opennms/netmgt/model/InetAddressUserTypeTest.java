/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2013-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.model;

import static org.junit.Assert.assertNull;

import java.sql.ResultSet;

import org.easymock.EasyMock;
import org.hibernate.engine.spi.SessionImplementor;
import org.junit.Ignore;
import org.junit.Test;

public class InetAddressUserTypeTest {

    @Ignore // It is difficult to run this test in Hibernate 4 without an easy mock version of SessionImplementor
    @Test
    public void testInetAddressType() throws Exception {
        ResultSet rs = EasyMock.createMock(ResultSet.class);
        EasyMock.expect(rs.getString("ipAddr")).andReturn(null);
        SessionImplementor session = EasyMock.createMock(SessionImplementor.class);
        //EasyMock.expect(session.getFactory()).andReturn(SOME OTHER MOCK OBJECT);
        EasyMock.replay(rs, session);
        final InetAddressUserType userType = new InetAddressUserType();
        final Object result = userType.nullSafeGet(rs, new String[]{"ipAddr"}, session, null);
        EasyMock.verify(rs);
        assertNull(result);
    }
}

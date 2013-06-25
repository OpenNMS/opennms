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

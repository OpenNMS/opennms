package org.opennms.netmgt.model;

import static org.junit.Assert.assertNull;

import java.sql.ResultSet;

import org.easymock.EasyMock;
import org.junit.Test;

public class InetAddressUserTypeTest {

    @Test
    public void testInetAddressType() throws Exception {
        ResultSet rs = EasyMock.createMock(ResultSet.class);
        EasyMock.expect(rs.getString("ipAddr")).andReturn(null);
        EasyMock.replay(rs);
        final InetAddressUserType userType = new InetAddressUserType();
        final Object result = userType.nullSafeGet(rs, new String[]{"ipAddr"}, null);
        EasyMock.verify(rs);
        assertNull(result);
    }
}

/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2018 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2018 The OpenNMS Group, Inc.
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

package org.opennms.core.test.db;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class IPLikeCoverageIT extends InstallerDbITCase {

    /*
     * This set of coverage data matches that in https://github.com/OpenNMS/iplike/blob/master/tests.dat
     */
    @Test
    public void testIplikeCoverage() throws Exception {
        getInstallerDb().updatePlPgsql();
        getInstallerDb().setPostgresIpLikeLocation(null); // Ensure that we don't try to load the C version
        getInstallerDb().updateIplike();

        // IPv4 basic matches
        checkIplikeRule("1.2.3.4","1.2.3.4",true);
        checkIplikeRule("1.2.3.4","1.2.3.5",false);
        checkIplikeRule("1.2.3.4","1.2.3.*",true);
        checkIplikeRule("1.2.3.4","1.*.3.4",true);
        checkIplikeRule("1.2.3.4","1.*.3.5",false);

        // IPv4 range matches
        checkIplikeRule("192.168.10.11","192.168.10.10-11",true);
        checkIplikeRule("192.168.10.12","192.168.10.10-11",false);
        checkIplikeRule("192.168.223.9","192.168.216-223.*",true);
        checkIplikeRule("192.168.224.9","192.168.216-223.*",false);

        // IPv4 list matches
        checkIplikeRule("192.168.1.9","192.168.0,1,2.*",true);
        checkIplikeRule("192.168.1.9","192.168.1,2,0.*",true);
        checkIplikeRule("192.168.1.9","192.168.2,0,1.*",true);
        checkIplikeRule("192.168.3.9","192.168.0,1,2.*",false);
        checkIplikeRule("192.168.3.9","192.168.1,2,0.*",false);
        checkIplikeRule("192.168.3.9","192.168.2,0,1.*",false);
        checkIplikeRule("192.168.3.9","192.168.*,1,2.*",true);
        checkIplikeRule("192.168.3.9","192.168.0,*,2.*",true);
        checkIplikeRule("192.168.3.9","192.168.0,1,*.*",true);

        // IPv4 list and range in separate octet
        checkIplikeRule("192.168.1.9","192.168.0,1,2.0-20",true);
        checkIplikeRule("192.168.1.21","192.168.0,1,2.0-20",false);

        // IPv4 list and range in same octet
        checkIplikeRule("192.168.1.9","192.168.0,1,2-4.0-20",true);
        checkIplikeRule("192.168.3.9","192.168.0,1,2-4.0-20",true);
        checkIplikeRule("192.168.5.9","192.168.0,1,2-4.0-20",false);
        checkIplikeRule("192.168.1.21","192.168.0,1,2,3-4.0-20",false);
        checkIplikeRule("192.168.0.1","192.168.1-2,5.*",false);

        // IPv6 tests
        checkIplikeRule("fe80:0000:0000:0000:aaaa:bbbb:cccc:dddd","*:*:*:*:*:*:*:*",true);
        checkIplikeRule("fe80:0000:0000:0000:aaaa:bbbb:cccc:dddd%4","*:*:*:*:*:*:*:*",true);
        checkIplikeRule("fe80:0000:0000:0000:aaaa:bbbb:cccc:dddd%4","*:*:*:*:*:*:*:*%4",true);
        checkIplikeRule("fe80:0000:0000:0000:aaaa:bbbb:cccc:dddd","*:*:*:*:*:*:*:*%4",false);

        checkIplikeRule("fe80:0000:0000:0000:aaaa:bbbb:cccc:dddd","fe80:*:*:*:*:*:*:*",true);
        checkIplikeRule("fe80:0000:0000:0000:aaaa:bbbb:cccc:dddd%45","fe80:*:*:*:*:*:*:*",true);
        checkIplikeRule("fe80:0000:0000:0000:aaaa:bbbb:cccc:dddd%45","fe80:*:*:*:*:*:*:*%45",true);
        checkIplikeRule("fe80:0000:0000:0000:aaaa:bbbb:cccc:dddd","fe80:*:*:*:*:*:*:*%45",false);

        checkIplikeRule("fe80:0000:0000:0000:aaaa:bbbb:cccc:dddd","*:*:*:0:*:*:*:*",true);
        checkIplikeRule("fe80:0000:0000:0000:aaaa:bbbb:cccc:dddd%4","*:*:*:0:*:*:*:*",true);
        checkIplikeRule("fe80:0000:0000:0000:aaaa:bbbb:cccc:dddd%4","*:*:*:0:*:*:*:*%4",true);
        checkIplikeRule("fe80:0000:0000:0000:aaaa:bbbb:cccc:dddd","*:*:*:0:*:*:*:*%4",false);

        checkIplikeRule("fe80:0000:0000:0000:aaaa:bbbb:cccc:dddd","*:*:*:*:*:bbbb:*:*",true);
        checkIplikeRule("fe80:0000:0000:0000:aaaa:bbbb:cccc:dddd%4","*:*:*:*:*:bbbb:*:*",true);
        checkIplikeRule("fe80:0000:0000:0000:aaaa:bbbb:cccc:dddd%4","*:*:*:*:*:bbbb:*:*%4",true);
        checkIplikeRule("fe80:0000:0000:0000:aaaa:bbbb:cccc:dddd%4","*:*:*:*:*:bbbb:*:*%5",false);
        checkIplikeRule("fe80:0000:0000:0000:aaaa:bbbb:cccc:dddd","*:*:*:*:*:bbbb:*:*%4",false);

        checkIplikeRule("fe80:0000:0000:0000:aaaa:bbbb:cccc:dddd","*:*:*:*:*:bbb0-bbbf:*:*",true);
        checkIplikeRule("fe80:0000:0000:0000:aaaa:bbbb:cccc:dddd%4","*:*:*:*:*:bbb0-bbbf:*:*",true);
        checkIplikeRule("fe80:0000:0000:0000:aaaa:bbbb:cccc:dddd%4","*:*:*:*:*:bbb0-bbbf:*:*%4",true);
        checkIplikeRule("fe80:0000:0000:0000:aaaa:bbbb:cccc:dddd","*:*:*:*:*:bbb0-bbbf:*:*%4",false);

        checkIplikeRule("fe80:0000:0000:0000:aaaa:bbbb:cccc:dddd","fe80:0000:0000:0000:aaaa:bbb0-bbbf:cccc:dddd",true);
        checkIplikeRule("fe80:0000:0000:0000:aaaa:bbbb:cccc:dddd%4","fe80:0000:0000:0000:aaaa:bbb0-bbbf:cccc:dddd",true);
        checkIplikeRule("fe80:0000:0000:0000:aaaa:bbbb:cccc:dddd%4","fe80:0000:0000:0000:aaaa:bbb0-bbbf:cccc:dddd%4",true);
        checkIplikeRule("fe80:0000:0000:0000:aaaa:bbbb:cccc:dddd","fe80:0000:0000:0000:aaaa:bbb0-bbbf:cccc:dddd%4",false);

        checkIplikeRule("fe80:0000:0000:0000:aaaa:bbbb:cccc:dddd","fe20,fe70-fe90:0000:0000:0000:*:bbb0,bbb1,bbb2,bbb3,bbb4,bbbb,bbbc:cccc:dddd",true);
        checkIplikeRule("fe80:0000:0000:0000:aaaa:bbbb:cccc:dddd%4","fe20,fe70-fe90:0000:0000:0000:*:bbb0,bbb1,bbb2,bbb3,bbb4,bbbb,bbbc:cccc:dddd",true);
        checkIplikeRule("fe80:0000:0000:0000:aaaa:bbbb:cccc:dddd%4","fe20,fe70-fe90:0000:0000:0000:*:bbb0,bbb1,bbb2,bbb3,bbb4,bbbb,bbbc:cccc:dddd%4",true);
        checkIplikeRule("fe80:0000:0000:0000:aaaa:bbbb:cccc:dddd","fe20,fe70-fe90:0000:0000:0000:*:bbb0,bbb1,bbb2,bbb3,bbb4,bbbb,bbbc:cccc:dddd%4",false);

        checkIplikeRule("fe80:0000:0000:0000:aaaa:bbbb:cccc:dddd","fe80:0000:0000:0000:aaaa:bbbb:cccc:dddd",true);
        checkIplikeRule("fe80:0000:0000:0000:aaaa:bbbb:cccc:dddd%4","fe80:0000:0000:0000:aaaa:bbbb:cccc:dddd",true);
        checkIplikeRule("fe80:0000:0000:0000:aaaa:bbbb:cccc:dddd%4","fe80:0000:0000:0000:aaaa:bbbb:cccc:dddd%4",true);
        checkIplikeRule("fe80:0000:0000:0000:aaaa:bbbb:cccc:dddd","fe80:0000:0000:0000:aaaa:bbbb:cccc:dddd%4",false);

        getInstallerDb().closeConnection();
    }

    private void checkIplikeRule(final String value, final String rule, final boolean expected) throws Exception {
        final Boolean result = getJdbcTemplate().queryForObject("SELECT iplike(CAST(? AS TEXT),CAST(? AS TEXT))", new String[] { value, rule }, Boolean.class);
        if (expected) {
            assertTrue("SELECT iplike(" + value + "," + rule + ") === " + expected, result);
        }
    }

}

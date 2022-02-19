/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2022 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2022 The OpenNMS Group, Inc.
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


package org.opennms.features.config.service.util;

import static org.junit.Assert.*;

import org.junit.Test;

public class SubstringTest {

    @Test
    public void shouldDoBeforeLastIndex() {
        assertEquals("abc-def", new Substring("abc-def-ghi").getBeforeLast("-").toString());
        assertEquals("-abc-def", new Substring("-abc-def-ghi").getBeforeLast("-").toString());
        assertEquals("abc", new Substring("abc").getBeforeLast("-").toString());
        assertEquals("", new Substring("").getBeforeLast("-").toString());
    }

    @Test
    public void shouldDoAfterLastIndex() {
        assertEquals("def", new Substring("abc-def").getAfterLast("-").toString());
        assertEquals("", new Substring("-abc-def-ghi-").getAfterLast("-").toString());
        assertEquals("", new Substring("abc").getAfterLast("-").toString());
        assertEquals("", new Substring("").getAfterLast("-").toString());
    }

    @Test
    public void shouldDoAfter() {
        assertEquals("def-ghi", new Substring("abc-def-ghi").getAfter("-").toString());
        assertEquals("abc-def-ghi-", new Substring("-abc-def-ghi-").getAfter("-").toString());
        assertEquals("", new Substring("abc").getAfter("-").toString());
        assertEquals("", new Substring("").getAfterLast("-").toString());
    }

}
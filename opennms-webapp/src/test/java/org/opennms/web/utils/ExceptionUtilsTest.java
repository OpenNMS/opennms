/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2016-2016 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2016 The OpenNMS Group, Inc.
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

package org.opennms.web.utils;

import static org.junit.Assert.assertEquals;

import javax.servlet.ServletException;

import org.junit.Test;
import org.opennms.web.alarm.AlarmIdNotFoundException;

public class ExceptionUtilsTest {

    @Test(expected=ServletException.class)
    public void getRootCauseThrowsOnNull() throws ServletException {
        ExceptionUtils.getRootCause(null, AlarmIdNotFoundException.class);
    }

    @Test(expected=ServletException.class)
    public void getRootCauseThrowsOnServletExcetptionWithNullRootCuase() throws ServletException {
        ExceptionUtils.getRootCause(new ServletException("test", null), AlarmIdNotFoundException.class);
    }

    @Test
    public void canGetRootCause() throws ServletException {
        AlarmIdNotFoundException ainfe = new AlarmIdNotFoundException("", "");
        assertEquals(ainfe, ExceptionUtils.getRootCause(ainfe, AlarmIdNotFoundException.class));

        ServletException nested = new ServletException(ainfe);
        assertEquals(ainfe, ExceptionUtils.getRootCause(nested, AlarmIdNotFoundException.class));

        ServletException doubleNested = new ServletException(nested);
        assertEquals(ainfe, ExceptionUtils.getRootCause(doubleNested, AlarmIdNotFoundException.class));
    }

}

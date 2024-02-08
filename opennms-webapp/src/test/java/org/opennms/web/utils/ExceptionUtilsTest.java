/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.opennms.web.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

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

    @Test
    public void willPrintFullStackTrace(){
        Exception rootException = throwAndCatchException(new NullPointerException("root message"));
        Exception wrapperException = throwAndCatchException(new IllegalArgumentException("wrapper message", rootException));
        String stackTrace = ExceptionUtils.getFullStackTrace(wrapperException);
        assertTrue(stackTrace.contains("root message"));
        assertTrue(stackTrace.contains("wrapper message"));
        assertTrue(stackTrace.contains("NullPointerException"));
        assertTrue(stackTrace.contains("IllegalArgumentException"));
    }

    @Test
    public void isTolerantAgainstNullException(){
        String stackTrace = ExceptionUtils.getFullStackTrace(null);
        assertNotNull(stackTrace);
    }

    private <E extends Exception> E throwAndCatchException(E exception){
        try{
            throw exception;
        } catch(Exception e){
            return (E)e;
        }
    }

}

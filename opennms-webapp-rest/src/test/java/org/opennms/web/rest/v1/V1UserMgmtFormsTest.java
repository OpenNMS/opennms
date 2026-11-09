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
package org.opennms.web.rest.v1;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import org.junit.Test;
import org.opennms.netmgt.config.UserDataChangedException;
import org.opennms.web.rest.v2.UserMgmtValidationException;
import org.springframework.dao.DataRetrievalFailureException;

/** The v1 translation of a failed write: found anywhere in the cause chain, as the group DAO wraps what the manager throws. */
public class V1UserMgmtFormsTest {

    private static int status(final Throwable t) {
        return V1UserMgmtForms.translate(t).getResponse().getStatus();
    }

    private static String entity(final Throwable t) {
        return (String) V1UserMgmtForms.translate(t).getResponse().getEntity();
    }

    @Test
    public void testWrappedValidationFailuresAre400() {
        final Exception validation = new DataRetrievalFailureException("saving group", new UserMgmtValidationException("The group name is too long."));
        assertEquals(400, status(validation));
        assertEquals("The group name is too long.", entity(validation));
        assertEquals(400, status(new IllegalArgumentException("bad value")));
    }

    @Test
    public void testAnIllegalArgumentExceptionDeeperInTheChainIs500() {
        // it may come from a bug anywhere below the service, so it is not reported as the client's mistake
        assertEquals(500, status(new RuntimeException(new IllegalStateException(new IllegalArgumentException("bad value")))));
    }

    @Test
    public void testAWrappedStaleCacheIs409() {
        assertEquals(409, status(new DataRetrievalFailureException("saving group", new UserDataChangedException())));
    }

    @Test
    public void testAnythingElseIs500AndAWebApplicationExceptionPassesThrough() {
        assertEquals(500, status(new IllegalStateException("The user could not be saved.")));
        final WebApplicationException forbidden = new WebApplicationException(Response.status(403).build());
        assertSame(forbidden, V1UserMgmtForms.translate(forbidden));
        assertSame(forbidden, V1UserMgmtForms.translate(new RuntimeException(forbidden)));
    }
}

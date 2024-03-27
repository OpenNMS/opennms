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
package org.opennms.netmgt.flows.classification.internal.validation;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import org.opennms.netmgt.flows.classification.error.ErrorTemplate;
import org.opennms.netmgt.flows.classification.exception.ClassificationException;

public class ValidatorTestUtils {
    protected interface Block {
        void execute();
    }

    protected static void verify(final Block block) {
        verify(block, null);
    }

    protected static void verify(final Block block, final ErrorTemplate expectedErrorTemplate) {
        try {
            block.execute();
            if (expectedErrorTemplate != null) {
                fail("Expected validation to fail, but succeeded");
            }
        } catch (ClassificationException ex) {
            // Arguments may vary, so just verify the template
            assertThat(ex.getError().getTemplate(), is(expectedErrorTemplate));
        }
    }
}

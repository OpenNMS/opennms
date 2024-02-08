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
package org.opennms.features.jest.client.bulk;

import org.junit.Assert;
import org.junit.Test;

public class BulkUtilsTest {

    @Test
    public void verifyExceptionParsing() {
        // Parse Error
        final String error = "{\"type\":\"mapper_parsing_exception\",\"reason\":\"failed to parse [timestamp]\",\"caused_by\":{\"type\":\"number_format_exception\",\"reason\":\"For input string: \\\"XXX\\\"\"}}";
        final Exception exception = BulkUtils.convertToException(error);

        // Manually verify exception
        Assert.assertEquals("mapper_parsing_exception: failed to parse [timestamp]", exception.getMessage());
        Assert.assertNotNull(exception.getCause());
        Assert.assertEquals("number_format_exception: For input string: \"XXX\"", exception.getCause().getMessage());
        Assert.assertNull(exception.getCause().getCause());
    }
}

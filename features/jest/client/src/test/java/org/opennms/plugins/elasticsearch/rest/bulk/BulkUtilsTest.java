/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2017-2017 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2017 The OpenNMS Group, Inc.
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

package org.opennms.plugins.elasticsearch.rest.bulk;

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

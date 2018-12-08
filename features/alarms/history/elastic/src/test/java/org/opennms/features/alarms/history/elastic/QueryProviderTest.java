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

package org.opennms.features.alarms.history.elastic;

import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.isEmptyOrNullString;

import java.util.Map;

import org.junit.Test;

import com.google.gson.Gson;

public class QueryProviderTest {

    private static final Gson gson = new Gson();

    /**
     * Verifies that the queries generate valid JSON.
     */
    @Test
    public void canGenerateQueries() {
        QueryProvider queryProvider = new QueryProvider();
        validate(queryProvider.getAlarmAt(1,1));
        validate(queryProvider.getActiveAlarmsAt(1));
        validate(queryProvider.getAllAlarms());
    }

    private void validate(String query) {
        assertThat(query, not(isEmptyOrNullString()));
        gson.fromJson(query, Map.class);
    }

}

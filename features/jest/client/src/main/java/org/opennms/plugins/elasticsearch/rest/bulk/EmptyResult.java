/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2018-2018 The OpenNMS Group, Inc.
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

package org.opennms.plugins.elasticsearch.rest.bulk;

import java.util.ArrayList;
import java.util.List;

import io.searchbox.core.BulkResult;

public class EmptyResult<T> implements BulkResultWrapper<T> {
    @Override
    public boolean isSucceeded() {
        return true;
    }

    @Override
    public String getErrorMessage() {
        return null;
    }

    @Override
    public List<FailedItem<T>> getFailedItems() {
        return new ArrayList<>();
    }

    @Override
    public BulkResult getRawResult() {
        return null;
    }

    @Override
    public List<T> getFailedDocuments() {
        return new ArrayList<>();
    }
}

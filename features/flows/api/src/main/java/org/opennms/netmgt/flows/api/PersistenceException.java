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

package org.opennms.netmgt.flows.api;

import java.util.ArrayList;
import java.util.List;

public class PersistenceException extends FlowException {

    private List<FailedItem> failedItems = new ArrayList<>();

    public PersistenceException(String message, List<FailedItem> failedItems) {
        super(message);
        this.failedItems = failedItems;
    }

    public static class FailedItem {
        private final NetflowDocument item;
        private final Exception cause;

        public FailedItem(NetflowDocument failedItem, Exception cause) {
            this.item = failedItem;
            this.cause = cause;
        }

        public NetflowDocument getItem() {
            return item;
        }

        public Exception getCause() {
            return cause;
        }
    }

    public List<FailedItem> getFailedItems() {
        return failedItems;
    }
}

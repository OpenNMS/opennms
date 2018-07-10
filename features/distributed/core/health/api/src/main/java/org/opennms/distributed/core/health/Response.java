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

package org.opennms.distributed.core.health;

public class Response {

    private final Status status;
    private final String message;
    private final Exception ex;

    public Response(Status status) {
        this(status, null, null);
    }

    public Response(Status status, String message) {
        this(status, message, null);
    }

    public Response(Exception ex) {
        this(Status.Failure, ex.getMessage(), ex);
    }

    public Response(Status status, String message, Exception ex) {
        this.status = status;
        this.message = message;
        this.ex = ex;
    }

    public boolean isSuccess() {
        return status == Status.Success;
    }

    public boolean isFailure() {
        return !isSuccess();
    }

    public String getMessage() {
        return message;
    }

    public Status getStatus() {
        return status;
    }
}

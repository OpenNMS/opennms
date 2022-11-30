/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2022 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2022 The OpenNMS Group, Inc.
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

package org.opennms.smoketest.utils;

import static java.util.Objects.nonNull;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;

import javax.ws.rs.HttpMethod;

public class RestClientUtil {

    private static final int CONNECT_TIMEOUT = 1000;
    private static final String HTTP_METHOD = HttpMethod.GET;

    /* Method to avoid connectionReset*/
    public static boolean checkUriAvailability(URI uri) {
        boolean isReachable;
        try {
            HttpURLConnection connection = (HttpURLConnection) new URL(uri.toString()).openConnection();
            connection.setRequestMethod(HTTP_METHOD);
            connection.setConnectTimeout(CONNECT_TIMEOUT);
            connection.connect();
            isReachable = nonNull(connection.getResponseMessage());
        } catch (IOException e) {
            isReachable = false;
        }
        return isReachable;
    }
}

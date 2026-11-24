/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2026 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2026 The OpenNMS Group, Inc.
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

package org.opennms.web.rest.support;

import java.util.Locale;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.opennms.netmgt.model.OnmsAssetRecord;

/**
 * Rejects query filters and sort orders on write-only asset properties.
 * A filter or sort on a credential lets a caller find its value one character at a time.
 */
public final class WriteOnlyPropertyGuard {

    private static final String ASSET_RECORD = "assetrecord";

    private WriteOnlyPropertyGuard() {
    }

    /**
     * @param property a criteria property path, for example "assetRecord.password" or "node.assetRecord.enable"
     * @throws WebApplicationException with status 400 when the path ends with a write-only asset property
     */
    public static void check(final String property) {
        if (isWriteOnly(property)) {
            throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST)
                    .type(MediaType.TEXT_PLAIN)
                    .entity("Property '" + property + "' cannot be used to filter or sort.")
                    .build());
        }
    }

    static boolean isWriteOnly(final String property) {
        if (property == null) {
            return false;
        }
        final String[] parts = property.trim().toLowerCase(Locale.ROOT).split("\\.");
        return parts.length >= 2
                && ASSET_RECORD.equals(parts[parts.length - 2])
                && OnmsAssetRecord.WRITE_ONLY_PROPERTIES.contains(parts[parts.length - 1]);
    }
}

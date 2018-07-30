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

package org.opennms.web.tags;

import java.io.IOException;
import javax.servlet.jsp.tagext.SimpleTagSupport;

import org.opennms.core.time.CentralizedDateTimeFormat;

/**
 * Renders the specified dateformat in different flavors such as for AngularJS
 *
 * See also:
 * - https://docs.angularjs.org/api/ng/filter/date
 * - java.time.format.DateTimeFormatter
 */
public class DateTimeFormatTag extends SimpleTagSupport {

    private FormatType type;

    @Override
    public void doTag() throws IOException {
        if(FormatType.AngularJS == type) {
            getJspContext().getOut().write(new CentralizedDateTimeFormat().getFormatAsAngularJS());
        } else {
            throw new UnsupportedOperationException("Unknown type = "+type);
        }
    }

    public void setType(FormatType type) {
        this.type = type;
    }
}

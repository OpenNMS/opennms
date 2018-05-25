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

package org.opennms.netmgt.flows.rest.internal.classification;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

class FilenameHelper {

    public final static String REGEX_ALLOWED_CHAR = "^[a-zA-Z1-9_ .-]{1,}$";

    private Pattern p = Pattern.compile(REGEX_ALLOWED_CHAR);

    boolean isValidFileName(String filename){
        if(filename == null
                || filename.startsWith(" " )
                || filename.endsWith(" ")
                || filename.trim().isEmpty()){
            return false;
        }
        Matcher m = p.matcher(filename);
        return m.matches();
    }

    String createFilenameForGroupExport(int groupId, String requestedFilename){
        return isValidFileName(requestedFilename) ? requestedFilename: groupId + "_rules.csv";
    }
}

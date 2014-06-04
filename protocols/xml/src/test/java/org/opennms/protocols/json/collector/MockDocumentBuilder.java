/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2011-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.protocols.json.collector;

import java.io.FileInputStream;

import net.sf.json.JSONObject;

import org.apache.commons.io.IOUtils;

/**
 * The Mock Document Builder.
 *
 * @author <a href="mailto:ronald.roskens@gmail.com">Ronald Roskens</a>
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a>
 */
public class MockDocumentBuilder {

    /** The JSON file name. */
    public static String m_jsonFileName;

    /**
     * Instantiates a new mock document builder.
     */
    private MockDocumentBuilder() {}

    /**
     * Gets the JSON document.
     *
     * @return the JSON document
     */
    public static JSONObject getJSONDocument() {
        if (m_jsonFileName == null)
            return null;
        JSONObject json = null;
        
        FileInputStream inputStream = null;
        try {
            inputStream = new FileInputStream(m_jsonFileName);
            String everything = IOUtils.toString(inputStream);
            json = JSONObject.fromObject(everything);
        } catch (Exception e) {
        } finally {
            if (inputStream != null)
            IOUtils.closeQuietly(inputStream);
        }
        
        return json;
    }

    /**
     * Sets the XML file name.
     *
     * @param xmlFileName the new XML file name
     */
    public static void setJSONFileName(String jsonFileName) {
        m_jsonFileName = jsonFileName;
    }
}


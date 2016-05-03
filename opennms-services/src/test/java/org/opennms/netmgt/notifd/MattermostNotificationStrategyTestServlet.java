/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2015-2016 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2016 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.notifd;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class MattermostNotificationStrategyTestServlet extends HttpServlet {

	private static final long serialVersionUID = 2125954127457631594L;
	private static JSONObject m_inputJson;

    @Override protected void doPost(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
        if (! "application/json".equals(req.getContentType())) {
        	squawk(resp, "Invalid content type " + req.getContentType());
        }
        
        m_inputJson = null;
        JSONParser jp = new JSONParser();
        try {
			Object inStuff = jp.parse(req.getReader());
			if (inStuff instanceof JSONObject) {
				m_inputJson = (JSONObject)inStuff;
			}
		} catch (ParseException e1) {
			squawk(resp, "Input is not well-formed JSON");
			return;
		}
        
        if ((! m_inputJson.containsKey("text")) || "".equals(m_inputJson.get("text"))) {
        	squawk(resp, "No text specified");
        	return;
        }
        
        if ((! m_inputJson.containsKey("username")) || "".equals(m_inputJson.get("username"))) {
        	squawk(resp, "No username specified");
        	return;
        }
        
        final String responseText = "ok";
        final ServletOutputStream os = resp.getOutputStream();
        os.print(responseText);
        os.close();
        resp.setContentType("text/plain");
        resp.setContentLength(responseText.length());
    }
    
    @SuppressWarnings("unchecked")
	private void squawk(final HttpServletResponse resp, String reason) throws IOException {
    	JSONObject errorJson = new JSONObject();
    	errorJson.put("message", reason);
    	errorJson.put("detailed_error", "");
    	errorJson.put("request_id", "deadbeefcafebabe");
    	errorJson.put("status_code", 500);
    	errorJson.put("isoauth", false);
    	
    	final String responseText = errorJson.toJSONString();
    	final ServletOutputStream os = resp.getOutputStream();
    	os.print(responseText);
    	os.close();
    	resp.setContentType("application/json");
    	resp.setContentLength(responseText.length());
    	resp.setStatus(500);
    }
    
    public static JSONObject getInputJson() {
    	return m_inputJson;
    }
}
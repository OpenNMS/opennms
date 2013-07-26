/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012 The OpenNMS Group, Inc.
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

package org.opennms.features.survey;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Authenticator;
import java.net.HttpURLConnection;
import java.net.PasswordAuthentication;
import java.net.URL;

import org.codehaus.jackson.map.ObjectMapper;

public class SurveyClient {

	private static final String			USERNAME		= "karaf";
	private static final String			PASSWORD		= "karaf";

	static {
		// Setup the user/pass for Basic/Digest authentication
		Authenticator.setDefault(new Authenticator() {
			@Override
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication(USERNAME, PASSWORD.toCharArray());
			}
		});
	}

    private URL m_baseURL = new URL("http://localhost:8181/cxf/surveys");
    
    private static final ObjectMapper m_jsonMapper = new ObjectMapper();


    public SurveyClient() throws Exception {

        Survey survey = new Survey();

        HttpURLConnection connection = (HttpURLConnection)POST(survey, m_baseURL);

        System.err.println("Returned status: " + connection.getResponseCode());
        System.err.println(connection.getHeaderField("Location"));
    }

    private static HttpURLConnection POST(Object json, URL url) throws IOException {

		HttpURLConnection connection = (HttpURLConnection)url.openConnection();
		connection.setDoOutput(true);
		connection.setInstanceFollowRedirects(false);
		connection.setRequestMethod("POST");
		connection.setRequestProperty("Content-Type", "application/json");

		OutputStream output = connection.getOutputStream();
		m_jsonMapper.writeValue(output, json);

		return connection;
	}

    public static void main(String[] args) throws Exception {
    	new SurveyClient();
    }

}

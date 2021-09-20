/* 
 * Licensed to the OpenNMS Group Inc. under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The OpenNMS Group Inc. licences this file to You under the Apache License, 
 * Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *  http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.opennms.xmlclient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLConnection;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory; 


/**
 * OpenNmsXmlclient
 * This class implements in java similar functionality to the provision.pl script
 */
public class BasicHttpMethods  {
	private Log log = LogFactory.getLog(BasicHttpMethods.class.getName());


	/* *****************************************
	 * GETTERS AND SETTERS FOR CLASS PROPERTIES
	 * *****************************************/

	public Log getLog() {
		return log;
	}

	/**
	 * @param log set commons Log for this class. Defaults to OpenNmsXmlClient.class.getName() if not set.
	 */
	public void setLog(Log log) {
		this.log = log;
	}

	/* *********************************************************************
	 * BASIC HTTP POST, DELETE PUT AND GET METHODS
	 * See http://stackoverflow.com/questions/1051004/how-to-send-put-delete
	 * *********************************************************************/

	/**
	 * Sends an HTTP GET request to a url
	 *
	 * @param endpoint - The URL of the server. (Example: " http://www.yahoo.com/search")
	 * @param requestParameters - all the request parameters (Example: "param1=val1&param2=val2"). Note: This method will add the question mark (?) to the request - DO NOT add it yourself
	 * @return - The response from the end point
	 */
	public String sendGetRequest(String endpoint, String requestParameters, String username, String password) throws Exception {
		String result = null;
		if (endpoint.startsWith("http://")) {
			// Send a GET request to the servlet
			try {
				// Send data
				String urlStr = endpoint;
				if (requestParameters != null && requestParameters.length () > 0) {
					urlStr += "?" + requestParameters;
				}
				URL url = new URL(urlStr);
				URLConnection conn = url.openConnection ();
				
				// set username and password 
				// see http://www.javaworld.com/javaworld/javatips/jw-javatip47.html
				String userPassword = username + ":" + password;
				// Encode String
	            // String encoding = new sun.misc.BASE64Encoder().encode (userPassword.getBytes()); // depreciated sun.misc.BASE64Encoder()
				byte[] encoding = org.apache.commons.codec.binary.Base64.encodeBase64(userPassword.getBytes());
				String authStringEnc = new String(encoding);
				log.debug("Base64 encoded auth string: '" + authStringEnc+"'");
				conn.setRequestProperty  ("Authorization", "Basic " + authStringEnc);

				// Get the response
				BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
				StringBuffer sb = new StringBuffer();
				String line;
				while ((line = rd.readLine()) != null) {
					sb.append(line);
				}
				rd.close();
				result = sb.toString();

			} catch (Throwable e) {
				throw new Exception("problem issuing get to URL", e);
			}
		}
		return result;
	}

	/**
	 * Reads data from the data reader and posts it to a server via POST request.
	 * data - The data you want to send
	 * endpoint - The server's address
	 * output - writes the server's response to output
	 * @throws Exception
	 */
	public void postData(Reader data, URL endpoint, Writer output, String username, String password) throws Exception {
		HttpURLConnection conn = null;
		try {
			conn = (HttpURLConnection) endpoint.openConnection();

			try {
				conn.setRequestMethod("POST");
			} catch (ProtocolException e) {
				throw new Exception("Shouldn't happen: HttpURLConnection doesn't support POST??", e);
			}

			// set username and password 
			// see http://www.javaworld.com/javaworld/javatips/jw-javatip47.html
			String userPassword = username + ":" + password;
			// Encode String
            // String encoding = new sun.misc.BASE64Encoder().encode (userPassword.getBytes()); // depreciated sun.misc.BASE64Encoder()
			byte[] encoding = org.apache.commons.codec.binary.Base64.encodeBase64(userPassword.getBytes());
			String authStringEnc = new String(encoding);
			log.debug("Base64 encoded auth string: '" + authStringEnc+"'");
			conn.setRequestProperty  ("Authorization", "Basic " + authStringEnc);

			conn.setDoOutput(true);
			conn.setDoInput(true);
			conn.setUseCaches(false);
			conn.setAllowUserInteraction(false);
			conn.setRequestProperty("Content-type", "application/xml; charset=" + "UTF-8");

			OutputStream out = conn.getOutputStream();

			try {
				Writer writer = new OutputStreamWriter(out, "UTF-8");
				pipe(data, writer);
				writer.close();
			} catch (IOException e) {
				throw new Exception("IOException while posting data", e);
			} finally {
				if (out != null)
					out.close();
			}

			InputStream in = conn.getInputStream();
			try {
				Reader reader = new InputStreamReader(in);
				pipe(reader, output);
				reader.close();
			} catch (IOException e) {
				throw new Exception("IOException while reading response", e);
			} finally 	{
				if (in != null)
					in.close();
			}

		} catch (IOException e) {
			throw new Exception("Connection error (is server running at " + endpoint + " ?): " + e);
		} finally {
			if (conn != null)
				conn.disconnect();
		}
	}

	/**
	 * Pipes everything from the reader to the writer via a buffer
	 */
	private void pipe(Reader reader, Writer writer) throws IOException {
		char[] buf = new char[1024];
		int read = 0;
		while ((read = reader.read(buf)) >= 0) {
			writer.write(buf, 0, read);
		}
		writer.flush();
	}


	/**
	 * Sends an HTTP DELETE request to a url
	 * endpoint - The server's address
	 * output - writes the server's response to output
	 * @throws Exception
	 */
	public void deleteData( URL endpoint, Writer output, String username, String password) throws Exception {

		HttpURLConnection conn = null;
		try {
			conn = (HttpURLConnection) endpoint.openConnection();

			try {
				conn.setRequestMethod("DELETE");
			} catch (ProtocolException e) {
				throw new Exception("Shouldn't happen: HttpURLConnection doesn't support DELETE??", e);
			}

			// set username and password 
			// see http://www.javaworld.com/javaworld/javatips/jw-javatip47.html
			String userPassword = username + ":" + password;
			// Encode String
            // String encoding = new sun.misc.BASE64Encoder().encode (userPassword.getBytes()); // depreciated sun.misc.BASE64Encoder()
			byte[] encoding = org.apache.commons.codec.binary.Base64.encodeBase64(userPassword.getBytes());
			String authStringEnc = new String(encoding);
			log.debug("Base64 encoded auth string: '" + authStringEnc+"'");
			conn.setRequestProperty  ("Authorization", "Basic " + authStringEnc);

			conn.setDoOutput(true);
			conn.setDoInput(true);
			conn.setUseCaches(false);
			conn.setAllowUserInteraction(false);
			conn.setRequestProperty("Content-type", "application/xml; charset=" + "UTF-8");

			InputStream in = conn.getInputStream();
			try {
				Reader reader = new InputStreamReader(in);
				pipe(reader, output);
				reader.close();
			} catch (IOException e) {
				throw new Exception("IOException while reading response", e);
			} finally {
				if (in != null)
					in.close();
			}

		} catch (IOException e) {
			throw new Exception("Connection error (is server running at " + endpoint + " ?): " + e);
		} finally {
			if (conn != null)
				conn.disconnect();
		}
	}

	/**
	 * sends data to a server via PUT request.
	 * data - The data you want to send
	 * endpoint - The server's address
	 * output - writes the server's response to output
	 * @throws Exception
	 */
	public void putData(Reader data, URL endpoint, Writer output, String username, String password) throws Exception {

		HttpURLConnection conn = null;
		try {
			conn = (HttpURLConnection) endpoint.openConnection();

			try {
				conn.setRequestMethod("PUT");
			} catch (ProtocolException e) {
				throw new Exception("Shouldn't happen: HttpURLConnection doesn't support PUT??", e);
			}

			// set username and password 
			// see http://www.javaworld.com/javaworld/javatips/jw-javatip47.html
			String userPassword = username + ":" + password;
			// Encode String
            // String encoding = new sun.misc.BASE64Encoder().encode (userPassword.getBytes()); // depreciated sun.misc.BASE64Encoder()
			byte[] encoding = org.apache.commons.codec.binary.Base64.encodeBase64(userPassword.getBytes());
			String authStringEnc = new String(encoding);
			log.debug("Base64 encoded auth string: '" + authStringEnc+"'");
			conn.setRequestProperty  ("Authorization", "Basic " + authStringEnc);

			conn.setDoOutput(true);
			conn.setDoInput(true);
			conn.setUseCaches(false);
			conn.setAllowUserInteraction(false);
			conn.setRequestProperty("Content-type", "application/x-www-form-urlencoded; charset=" + "UTF-8");

			OutputStream out = conn.getOutputStream();

			try {
				Writer writer = new OutputStreamWriter(out, "UTF-8");
				pipe(data, writer);
				writer.close();
			} catch (IOException e) {
				throw new Exception("IOException while putting data", e);
			} finally {
				if (out != null)
					out.close();
			}

			InputStream in = conn.getInputStream();
			try {
				Reader reader = new InputStreamReader(in);
				pipe(reader, output);
				reader.close();
			} catch (IOException e) {
				throw new Exception("IOException while reading response", e);
			} finally 	{
				if (in != null)
					in.close();
			}

		} catch (IOException e) {
			throw new Exception("Connection error (is server running at " + endpoint + " ?): " + e);
		} finally {
			if (conn != null)
				conn.disconnect();
		}
	}

}



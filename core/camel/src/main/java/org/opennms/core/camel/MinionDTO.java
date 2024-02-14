/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.opennms.core.camel;

import java.net.InetAddress;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

import org.opennms.core.utils.InetAddressUtils;

@XmlRootElement(name = "minion-dto")
@XmlAccessorType(XmlAccessType.NONE)
public class MinionDTO {

	public static final String SYSTEM_ID = "systemId";
	public static final String LOCATION = "location";
	public static final String SOURCE_ADDRESS = "sourceAddress";
	public static final String SOURCE_PORT = "sourcePort";

	@XmlElementWrapper(name = "headers")
	private final Map<String, String> m_headers = Collections.synchronizedMap(new TreeMap<String, String>());

	@XmlElement(name = "body")
	private byte[] m_body;

	public MinionDTO() {
		// No-arg constructor for JAXB
	}

	@Override
	public int hashCode() {
		return Objects.hash(m_headers, m_body);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		final MinionDTO other = (MinionDTO)obj;
		return Objects.equals(this.m_headers, other.m_headers)
				&& Arrays.equals(this.m_body, other.m_body);
	}

	public Map<String, String> getHeaders() {
		return Collections.unmodifiableMap(m_headers);
	}

	public void setHeaders(final Map<String, String> newHeaders) {
		synchronized (m_headers) {
			m_headers.clear();
			m_headers.putAll(newHeaders);
		}
	}

	public void putHeader(String key, String value) {
		synchronized (m_headers) {
			m_headers.put(key, value);
		}
	}

	public String getHeader(String key) {
		synchronized (m_headers) {
			return m_headers.get(key);
		}
	}

	public byte[] getBody() {
		return m_body;
	}

	public void setBody(byte[] m_body) {
		this.m_body = m_body;
	}

	public final void setSystemId(String m_systemId) {
		this.putHeader(SYSTEM_ID, m_systemId);
	}

	public final void setLocation(String m_location) {
		this.putHeader(LOCATION, m_location);
	}

	public final void setSourceAddress(InetAddress m_sourceAddress) {
		this.putHeader(SOURCE_ADDRESS, InetAddressUtils.str(m_sourceAddress));
	}

	public final void setSourcePort(int m_sourceport) {
		this.putHeader(SOURCE_PORT, Integer.toString(m_sourceport));
	}

}

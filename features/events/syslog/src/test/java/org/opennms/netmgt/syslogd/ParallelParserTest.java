/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2005-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.syslogd;

import java.nio.ByteBuffer;

import org.junit.Test;
import org.opennms.netmgt.syslogd.ParallelActorParser.MatchMonth;
import org.opennms.netmgt.syslogd.ParallelActorParser.Stage;
import org.opennms.netmgt.syslogd.ParallelActorParser.SyslogParserState;

public class ParallelParserTest {

	@Test
	public void testMatchMonth() {
		SyslogParserState state = new SyslogParserState();
		state.buffer = ByteBuffer.wrap("Oct".getBytes());

		Stage stage = new MatchMonth((s,v) -> { System.out.println(v); });
		stage.apply(state);
	}

	@Test
	public void testGrokParser() {
		ParallelActorParser.parseGrok("<%{INTEGER:facilityPriority}> %{MONTH:month} %{INTEGER:day} %{INTEGER:hour}:%{INTEGER:minute}:%{INTEGER:second} %{STRING:hostname} %{STRING:processName}[%{INTEGER:processId}]: %{STRING:month} %{INTEGER:day} %{STRING:timestamp} %{STRING:timezone} \\%%{STRING:facility}-%{INTEGER:priority}-%{STRING:mnemonic}: %{STRING:message}");
	}
}

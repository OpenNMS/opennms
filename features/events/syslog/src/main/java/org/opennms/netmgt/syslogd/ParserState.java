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
package org.opennms.netmgt.syslogd;

import java.nio.ByteBuffer;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The state of the entire parse operation. This state
 * should include all of the finished tokens generated
 * by {@link ParserStage} operations.
 */
public class ParserState implements Cloneable {

	private final static Logger LOG = LoggerFactory.getLogger(ParserState.class);

	private final ByteBuffer buffer;

	// TODO: Replace with a strategy
	public final SyslogMessage message;

	public ParserState(ByteBuffer input) {
		this(input, new SyslogMessage());
	}

	public ParserState(ByteBuffer input, SyslogMessage message) {
		this.buffer = input;
		this.message = message;
	}

	public ByteBuffer getBuffer() {
		// TODO: See if this slows anything down
		return buffer.asReadOnlyBuffer();
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this)
			.append("message", message)
			.toString();
	}
	
	@Override
	public ParserState clone() {
		ParserState retval = new ParserState(buffer.duplicate(), message.clone());
		LOG.trace("ORIGINAL: {}", this);
		LOG.trace("CLONE   : {}", retval);
		return retval;
	}
}

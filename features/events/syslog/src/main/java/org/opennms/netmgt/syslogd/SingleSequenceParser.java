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
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * This class uses a single {@link ParserStage} sequence to parse an incoming
 * {@link ByteBuffer} message.
 */
public class SingleSequenceParser implements ByteBufferParser<SyslogMessage> {

	private final List<ParserStage> m_stages;

	public SingleSequenceParser(List<ParserStage> stages) {
		m_stages = Collections.unmodifiableList(stages);
	}

	@Override
	public CompletableFuture<SyslogMessage> parse(ByteBuffer incoming) {

		// Put all mutable parts of the parse operation into a state object
		final ParserState state = new ParserState(incoming);

		CompletableFuture<ParserState> future = CompletableFuture.completedFuture(state);

		// Apply each parse stage to the message
		for (ParserStage stage : m_stages) {
			future = future.thenApply(stage::apply);
		}

		//future.exceptionally(e -> { /* DO SOMETHING */ return null; });

		return future.thenApply(s -> {
			if (s == null) {
				return null;
			} else {
				return s.message;
			}
		});
	}
}

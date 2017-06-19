/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2017-2017 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2017 The OpenNMS Group, Inc.
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

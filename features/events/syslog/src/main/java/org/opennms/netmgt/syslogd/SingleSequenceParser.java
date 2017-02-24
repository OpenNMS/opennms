package org.opennms.netmgt.syslogd;

import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.opennms.netmgt.model.events.EventBuilder;
import org.opennms.netmgt.syslogd.BufferParser.ParserStage;
import org.opennms.netmgt.syslogd.BufferParser.ParserState;
import org.opennms.netmgt.xml.event.Event;

/**
 * This class uses a single {@link ParserStage} sequence to parse an incoming
 * {@link ByteBuffer} message.
 */
public class SingleSequenceParser implements ByteBufferParser<Event> {

	private final List<ParserStage> m_stages;

	public SingleSequenceParser(List<ParserStage> stages) {
		m_stages = Collections.unmodifiableList(stages);
	}

	@Override
	public CompletableFuture<Event> parse(ByteBuffer incoming) {

		// Put all mutable parts of the parse operation into a state object
		final ParserState state = new ParserState(incoming, new EventBuilder("uei.opennms.org/test", this.getClass().getSimpleName()));

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
				return s.builder.getEvent();
			}
		});
	}
}

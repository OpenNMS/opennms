/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2016-2016 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.syslogd;

import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.EmptyStackException;
import java.util.List;
import java.util.Stack;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.opennms.netmgt.model.events.EventBuilder;
import org.opennms.netmgt.xml.event.Event;

/**
 * This class can be used to parse {@link ByteBuffer} objects into tokens.
 * As each token is completed, the value can be used to invoke a {@link BiConsumer}
 * method to dynamically construct objects such as OpenNMS events from the tokens.
 * 
 * @author Seth
 */
public class BufferParser {

	/**
	 * TODO: Make the fields in this a little
	 * more elegant.
	 */
	public static class ParserState {
		public ByteBuffer buffer;
		public EventBuilder builder;
		public StringBuffer accumulatedValue;
		public AtomicInteger accumulatedSize;
		public int index = -1;
		public int charIndex = -1;
	}

	public static class BufferParserFactory {
		final List<ParserStage> m_stages = new ArrayList<>();

		final Stack<Boolean> m_optional = new Stack<>();
		final Stack<Boolean> m_terminal = new Stack<>();

		public BufferParserFactory optional() {
			m_optional.push(true);
			return this;
		}

		public BufferParserFactory terminal() {
			m_terminal.push(true);
			return this;
		}

		private boolean getOptional() {
			try {
				return m_optional.pop();
			} catch (EmptyStackException e) {
				return false;
			}
		}

		private boolean getTerminal() {
			try {
				return m_terminal.pop();
			} catch (EmptyStackException e) {
				return false;
			}
		}

		private void addStage(ParserStage stage) {
			stage.setOptional(getOptional());
			stage.setTerminal(getTerminal());
			System.out.println(stage.toString());
			m_stages.add(stage);
		}

		public BufferParserFactory whitespace() {
			addStage(new MatchWhitespace());
			return this;
		}

		public BufferParserFactory character(char character) {
			addStage(new MatchChar(character));
			return this;
		}

		public BufferParserFactory string(BiConsumer<ParserState, String> consumer) {
			addStage(new MatchAny(consumer, Integer.MAX_VALUE));
			return this;
		}

		public BufferParserFactory integer(BiConsumer<ParserState, Integer> consumer) {
			addStage(new MatchInteger(consumer));
			return this;
		}

		public BufferParserFactory month(BiConsumer<ParserState, Integer> consumer) {
			addStage(new MatchMonth(consumer));
			return this;
		}

		public BufferParserFactory stringUntil(String ends, BiConsumer<ParserState,String> consumer) {
			addStage(new MatchStringUntil(consumer, ends));
			return this;
		}

		public BufferParserFactory stringUntilWhitespace(BiConsumer<ParserState,String> consumer) {
			addStage(new MatchStringUntil(consumer, MatchUntil.WHITESPACE));
			return this;
		}

		public BufferParserFactory stringUntilChar(char end, BiConsumer<ParserState,String> consumer) {
			addStage(new MatchStringUntil(consumer, end));
			return this;
		}

		public BufferParserFactory intUntilWhitespace(BiConsumer<ParserState,Integer> consumer) {
			addStage(new MatchIntegerUntil(consumer, MatchUntil.WHITESPACE));
			return this;
		}

		public BufferParserFactory stringBetweenDelimiters(char start, char end, BiConsumer<ParserState,String> consumer) {
			addStage(new MatchChar(start));
			addStage(new MatchStringUntil(consumer, end));
			addStage(new MatchChar(end));
			return this;
		}

		public BufferParserFactory intBetweenDelimiters(char start, char end, BiConsumer<ParserState,Integer> consumer) {
			addStage(new MatchChar(start));
			addStage(new MatchIntegerUntil(consumer, end));
			addStage(new MatchChar(end));
			return this;
		}

		public CompletableFuture<Event> parse(ByteBuffer incoming, ExecutorService executor) {

			// Put all mutable parts of the parse operation into a state object
			final ParserState state = new ParserState();
			state.buffer = incoming;
			state.builder = new EventBuilder();

			CompletableFuture<ParserState> future = CompletableFuture.completedFuture(state);

			// Apply each parse stage to the message
			for (ParserStage stage : m_stages) {
				future = future.thenApply(stage::apply);
			}

			//future.exceptionally(e -> { /* DO SOMETHING */ return null; });

			return future.thenApply(s -> {
				return s.builder.getEvent();
			});
		}
	}

	/**
	 * An individual stage of the token parser. A parser is composed of
	 * a sequence of {@link ParserStage} objects.
	 */
	public static interface ParserStage {

		public static enum AcceptResult {
			/**
			 * Continue the parsing process.
			 */
			CONTINUE,
			/**
			 * Complete the parsing stage and consider the current
			 * character as already consumed.
			 */
			COMPLETE_AFTER_CONSUMING,
			/**
			 * Complete the parsing stage and reset the position of the
			 * buffer so that the next stage can consume the current
			 * character.
			 */
			COMPLETE_WITHOUT_CONSUMING,
			/**
			 * Cancel the parsing process due to a failure to parse
			 * based on the current rules for this stage.
			 */
			CANCEL
		}

		/**
		 * Mark the stage as optional.
		 * 
		 * @param optional
		 */
		void setOptional(boolean optional);

		/**
		 * Mark the stage as terminal, ie. it handles a buffer
		 * underflow as successful completion instead of failure.
		 * 
		 * @param terminal
		 */
		void setTerminal(boolean terminal);

		/**
		 * Process the state for this stage and return it so
		 * that the next stage can continue processing.
		 */
		ParserState apply(ParserState state);
	}

	/**
	 * This base class handles most of the statefulness of each {@link ParserStage}.
	 * 
	 * @param <R> Type of the value that can be emitted by this stage to the
	 * {@link BiConsumer} consumer.
	 */
	public static abstract class AbstractParserStage<R> implements ParserStage {

		private boolean m_optional = false;
		private boolean m_terminal = false;
		private final BiConsumer<ParserState, R> m_resultConsumer;

		/**
		 * Create an {@link AbstractParserStage} with no consumer.
		 */
		protected AbstractParserStage() {
			this(null);
		}

		/**
		 * Create an {@link AbstractParserStage} with the specified consumer.
		 */
		protected AbstractParserStage(BiConsumer<ParserState,R> resultConsumer) {
			m_resultConsumer = resultConsumer;
		}

		/**
		 * Mark this stage as optional. This means that if parsing fails during
		 * this stage, the parser will continue on to the next stage without
		 * failing exceptionally.
		 */
		@Override
		public void setOptional(boolean optional) {
			m_optional = optional;
		}

		/**
		 * Mark this stage as terminal. This means that if the end of the stream
		 * is reached, it is considered a normal termination of the stage instead
		 * of a parsing failure.
		 */
		@Override
		public void setTerminal(boolean terminal) {
			m_terminal = terminal;
		}

		public abstract AcceptResult acceptChar(ParserState state, char c);

		public final ParserState apply(ParserState state) {
			// Reset the accumulator state if necessary
			if (m_resultConsumer != null) {
				state.accumulatedValue = new StringBuffer();
				state.accumulatedSize = new AtomicInteger(0);
			}

			while(true) {
				state.buffer.mark();

				char c;
				try {
					c = (char)state.buffer.get();
				} catch (BufferUnderflowException e) {
					if (m_terminal) {
						if (m_resultConsumer != null) {
							m_resultConsumer.accept(state, getValue(state));
						}
						// Reset any local state if necessary
						reset(state);
						return state;
					} else if (m_optional) {
						// TODO: Should we reset the buffer here?
						state.buffer.reset();
						// Reset any local state if necessary
						reset(state);
						return state;
					} else {
						throw new CancellationException(getClass().getSimpleName() + " reached end of buffer, match failed");
					}
				}

				switch (acceptChar(state, c)) {
					case CONTINUE:
						continue;
					case COMPLETE_AFTER_CONSUMING:
						if (m_resultConsumer != null) {
							m_resultConsumer.accept(state, getValue(state));
						}
						// Reset any local state if necessary
						reset(state);
						return state;
					case COMPLETE_WITHOUT_CONSUMING:
						// Put the char back on the deque
						//m_incoming.putFirst(c);
						state.buffer.reset();
						if (m_resultConsumer != null) {
							m_resultConsumer.accept(state, getValue(state));
						}
						// Reset any local state if necessary
						reset(state);
						return state;
					case CANCEL:
						if (m_optional) {
							state.buffer.reset();
							// Reset any local state if necessary
							reset(state);
							return state;
						} else {
							throw new CancellationException(getClass().getSimpleName() + " match failed");
						}
				}
			}
		}

		public void reset(ParserState state) {
			// Do nothing by default
		}

		protected static void accumulate(ParserState state, char c) {
			state.accumulatedValue.append(c);
			state.accumulatedSize.incrementAndGet();
		}

		protected static int accumulatedSize(ParserState state) {
			return state.accumulatedSize.get();
		}

		protected static String accumulatedValue(ParserState state) {
			return state.accumulatedValue.toString();
		}

		protected R getValue(ParserState state) {
			return null;
		}
	}

	/**
	 * Match any whitespace character.
	 */
	public static class MatchWhitespace extends AbstractParserStage<Void> {
		@Override
		public AcceptResult acceptChar(ParserState state, char c) {
			// TODO: Make this more efficient
			if ("".equals(String.valueOf(c).trim())) {
				return AcceptResult.CONTINUE;
			} else {
				return AcceptResult.COMPLETE_WITHOUT_CONSUMING;
			}
		}
	}

	/**
	 * Match a single character.
	 */
	public static class MatchChar extends AbstractParserStage<Void> {
		private final char m_char;
		MatchChar(char c) {
			super();
			m_char = c;
		}

		@Override
		public AcceptResult acceptChar(ParserState state, char c) {
			if (c == m_char) {
				return AcceptResult.COMPLETE_AFTER_CONSUMING;
			} else {
				return AcceptResult.CANCEL;
			}
		}

		@Override
		public String toString() {
			return new ToStringBuilder(this)
				.append("char", m_char)
				.toString();
		}
	}

	/**
	 * Match a month.
	 * 
	 * TODO: This only works if the initial character is unique, fix it!
	 */
	public static class MatchMonth extends AbstractParserStage<Integer> {
		final static char[][] MONTHS = new char[][] {
			"Jan".toCharArray(),
			"jan".toCharArray(),
			"Feb".toCharArray(),
			"feb".toCharArray(),
			"Mar".toCharArray(),
			"mar".toCharArray(),
			"Apr".toCharArray(),
			"apr".toCharArray(),
			"May".toCharArray(),
			"may".toCharArray(),
			"Jun".toCharArray(),
			"jun".toCharArray(),
			"Jul".toCharArray(),
			"jul".toCharArray(),
			"Aug".toCharArray(),
			"aug".toCharArray(),
			"Sep".toCharArray(),
			"sep".toCharArray(),
			"Oct".toCharArray(),
			"oct".toCharArray(),
			"Nov".toCharArray(),
			"nov".toCharArray(),
			"Dec".toCharArray(),
			"dec".toCharArray()
		};

		public MatchMonth(BiConsumer<ParserState,Integer> consumer) {
			super(consumer);
		}

		@Override
		public AcceptResult acceptChar(ParserState state, char c) {
			if (state.index >= 0) {
				if (c == MONTHS[state.index][state.charIndex]) {
					if (state.charIndex == 2) {
						return AcceptResult.COMPLETE_AFTER_CONSUMING;
					} else {
						state.charIndex++;
						return AcceptResult.CONTINUE;
					}
				} else {
					return AcceptResult.CANCEL;
				}
			}
			for (int i = 0; i < MONTHS.length; i++) {
				if (c == MONTHS[i][0]) {
					state.index = i;
					state.charIndex = 1;
					return AcceptResult.CONTINUE;
				} else {
					continue;
				}
			}
			return AcceptResult.CANCEL;
		}
		
		@Override
		public Integer getValue(ParserState state) {
			return state.index;
		}

		@Override
		public void reset(ParserState state) {
			super.reset(state);
			state.charIndex = -1;
			state.index = -1;
		}
	}

	/**
	 * Match any sequence of characters.
	 */
	public static class MatchAny extends AbstractParserStage<String> {
		private final int m_length;

		public MatchAny() {
			this(null, 1);
		}

		public MatchAny(BiConsumer<ParserState,String> consumer) {
			this(consumer, 1);
		}

		public MatchAny(int length) {
			this(null, length);
		}

		public MatchAny(BiConsumer<ParserState,String> consumer, int length) {
			super(consumer);
			m_length = length;
		}

		@Override
		public AcceptResult acceptChar(ParserState state, char c) {
			accumulate(state, c);
			if (accumulatedSize(state) >= m_length) {
				return AcceptResult.COMPLETE_AFTER_CONSUMING;
			} else {
				return AcceptResult.CONTINUE;
			}
		}
		
		@Override
		public String getValue(ParserState state) {
			return accumulatedValue(state);
		}

		@Override
		public String toString() {
			return new ToStringBuilder(this)
				.append("length", m_length)
				.toString();
		}
	}

	/**
	 * Match a string terminated by a character in a list of end tokens.
	 */
	public static abstract class MatchUntil<R> extends AbstractParserStage<R> {
		public static final String WHITESPACE = "\\s";

		private final char[] m_end;
		private boolean m_endOnwhitespace = false;

		MatchUntil(BiConsumer<ParserState,R> consumer, char end) {
			super(consumer);
			m_end = new char[] { end };
		}

		MatchUntil(BiConsumer<ParserState,R> consumer, String end) {
			super(consumer);
			m_endOnwhitespace = end.contains(WHITESPACE);
			// Erase the WHITESPACE token from the end char list
			end = end.replaceAll("\\\\s", "");
			m_end = end.toCharArray();
		}

		@Override
		public AcceptResult acceptChar(ParserState state, char c) {
			for (char end : m_end) {
				if (end == c) {
					return AcceptResult.COMPLETE_WITHOUT_CONSUMING;
				}
			}
			// TODO: Make this more efficient?
			if (m_endOnwhitespace && "".equals(String.valueOf(c).trim())) {
				return AcceptResult.COMPLETE_WITHOUT_CONSUMING;
			}
			accumulate(state, c);
			return AcceptResult.CONTINUE;
		}

		@Override
		public String toString() {
			return new ToStringBuilder(this)
				.append("ends", m_end)
				.append("endOnWhitespace", m_endOnwhitespace)
				.toString();
		}
	}

	public static class MatchStringUntil extends MatchUntil<String> {
		public MatchStringUntil(BiConsumer<ParserState,String> consumer, char end) {
			super(consumer, end);
		}

		public MatchStringUntil(BiConsumer<ParserState,String> consumer, String ends) {
			super(consumer, ends);
		}

		@Override
		public String getValue(ParserState state) {
			return accumulatedValue(state);
		}
	}

	public static class MatchIntegerUntil extends MatchUntil<Integer> {
		public MatchIntegerUntil(BiConsumer<ParserState,Integer> consumer, char end) {
			super(consumer, end);
		}

		public MatchIntegerUntil(BiConsumer<ParserState,Integer> consumer, String ends) {
			super(consumer, ends);
		}

		@Override
		public Integer getValue(ParserState state) {
			// Trim the leading zeros from this value
			String value = accumulatedValue(state);
			while (value.startsWith("0")) {
				value = value.substring(1);
			}

			if ("".equals(value)) {
				return 0;
			} else {
				return Integer.parseInt(value);
			}
		}
	}

	/**
	 * Match an integer.
	 */
	public static class MatchInteger extends AbstractParserStage<Integer> {
		MatchInteger(BiConsumer<ParserState,Integer> consumer) {
			super(consumer);
		}

		@Override
		public AcceptResult acceptChar(ParserState state, char c) {
			if (c >= '0' && c <= '9') {
				accumulate(state, c);
				return AcceptResult.CONTINUE;
			} else {
				// If any characters were accumulated, complete
				if (accumulatedSize(state) > 0) {
					return AcceptResult.COMPLETE_WITHOUT_CONSUMING;
				} else {
					return AcceptResult.CANCEL;
				}
			}
		}

		@Override
		public Integer getValue(ParserState state) {
			// Trim the leading zeros from this value
			String value = accumulatedValue(state);
			while (value.startsWith("0")) {
				value = value.substring(1);
			}

			if ("".equals(value)) {
				return 0;
			} else {
				return Integer.parseInt(value);
			}
		}
	}
}

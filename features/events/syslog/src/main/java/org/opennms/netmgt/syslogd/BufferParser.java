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
import java.util.Collections;
import java.util.EmptyStackException;
import java.util.List;
import java.util.Objects;
import java.util.Stack;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.opennms.netmgt.model.events.EventBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class can be used to parse {@link ByteBuffer} objects into tokens.
 * As each token is completed, the value can be used to invoke a {@link BiConsumer}
 * method to dynamically construct objects such as OpenNMS events from the tokens.
 * 
 * @author Seth
 */
public class BufferParser {

	private static final Logger LOG = LoggerFactory.getLogger(BufferParser.class);

	/**
	 * The state of the entire parse operation. This state
	 * should include all of the finished tokens generated
	 * by {@link ParserStage} operations.
	 */
	public static class ParserState {
		private final ByteBuffer buffer;

		// TODO: Replace with a strategy
		public final EventBuilder builder;

		public ParserState(ByteBuffer input) {
			this(input, new EventBuilder("uei.opennms.org/test", ParserState.class.getSimpleName()));
		}

		public ParserState(ByteBuffer input, EventBuilder builder) {
			this.buffer = input;
			this.builder = builder;
		}

		@Override
		public String toString() {
			return new ToStringBuilder(this)
				.append("builder", builder)
				.toString();
		}
	}

	/**
	 * The state of an individual {@link ParserStage} operation.
	 */
	public static class ParserStageState {
		public final ByteBuffer buffer;

		private final StringBuffer accumulatedValue;
		private final AtomicInteger accumulatedSize;

		// Only used by MatchMonth
		public int index = -1;
		public int charIndex = -1;

		public ParserStageState(ByteBuffer input) {
			buffer = input;
			accumulatedValue = new StringBuffer();
			accumulatedSize = new AtomicInteger();
		}

		public void accumulate(char c) {
			accumulatedValue.append(c);
			accumulatedSize.incrementAndGet();
		}

		public int getAccumulatedSize() {
			return accumulatedSize.get();
		}

		@Override
		public String toString() {
			return new ToStringBuilder(this)
				.append("accumulatedValue", accumulatedValue.toString())
				.append("accumulatedSize", accumulatedSize.get())
				.toString();
		}
	}

	public static class ParserStageSequenceBuilder {
		final List<ParserStage> m_stages = new ArrayList<>();

		final Stack<Boolean> m_optional = new Stack<>();
		final Stack<Boolean> m_terminal = new Stack<>();

		public List<ParserStage> getStages() {
			return Collections.unmodifiableList(m_stages);
		}

		public ParserStageSequenceBuilder optional() {
			m_optional.push(true);
			return this;
		}

		public ParserStageSequenceBuilder terminal() {
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

		public ParserStageSequenceBuilder whitespace() {
			addStage(new MatchWhitespace());
			return this;
		}

		public ParserStageSequenceBuilder character(char character) {
			addStage(new MatchChar(character));
			return this;
		}

		public ParserStageSequenceBuilder string(BiConsumer<ParserState, String> consumer) {
			addStage(new MatchAny(consumer, Integer.MAX_VALUE));
			return this;
		}

		public ParserStageSequenceBuilder integer(BiConsumer<ParserState, Integer> consumer) {
			addStage(new MatchInteger(consumer));
			return this;
		}

		public ParserStageSequenceBuilder month(BiConsumer<ParserState, Integer> consumer) {
			addStage(new MatchMonth(consumer));
			return this;
		}

		public ParserStageSequenceBuilder stringUntil(String ends, BiConsumer<ParserState,String> consumer) {
			addStage(new MatchStringUntil(consumer, ends));
			return this;
		}

		public ParserStageSequenceBuilder stringUntilWhitespace(BiConsumer<ParserState,String> consumer) {
			addStage(new MatchStringUntil(consumer, MatchUntil.WHITESPACE));
			return this;
		}

		public ParserStageSequenceBuilder stringUntilChar(char end, BiConsumer<ParserState,String> consumer) {
			addStage(new MatchStringUntil(consumer, end));
			return this;
		}

		public ParserStageSequenceBuilder intUntilWhitespace(BiConsumer<ParserState,Integer> consumer) {
			addStage(new MatchIntegerUntil(consumer, MatchUntil.WHITESPACE));
			return this;
		}

		public ParserStageSequenceBuilder stringBetweenDelimiters(char start, char end, BiConsumer<ParserState,String> consumer) {
			addStage(new MatchChar(start));
			addStage(new MatchStringUntil(consumer, end));
			addStage(new MatchChar(end));
			return this;
		}

		public ParserStageSequenceBuilder intBetweenDelimiters(char start, char end, BiConsumer<ParserState,Integer> consumer) {
			addStage(new MatchChar(start));
			addStage(new MatchIntegerUntil(consumer, end));
			addStage(new MatchChar(end));
			return this;
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

		public abstract AcceptResult acceptChar(ParserStageState state, char c);

		public final ParserState apply(final ParserState state) {
			// Create a new state for the current ParserStage.
			// Use ByteBuffer.duplicate() to create a buffer with marks
			// and positions that only this stage will use.
			ParserStageState stageState = new ParserStageState(state.buffer.duplicate()); 

			while(true) {
				stageState.buffer.mark();

				char c;
				try {
					c = (char)stageState.buffer.get();
				} catch (BufferUnderflowException e) {
					if (m_terminal) {
						if (m_resultConsumer != null) {
							m_resultConsumer.accept(state, getValue(stageState));
						}

//						// Reset any local state if necessary
//						reset(stageState);

						return new ParserState(stageState.buffer, state.builder);
					} else if (m_optional) {
//						// TODO: Should we reset the buffer here? It probably
//						// doesn't matter since we're at the end of the buffer.
//						stageState.buffer.reset();

//						// Reset any local state if necessary
//						reset(stageState);

						return new ParserState(stageState.buffer, state.builder);
					} else {
						// Reached end of buffer, match failed
						return null;
					}
				}

				switch (acceptChar(stageState, c)) {
					case CONTINUE:
						continue;
					case COMPLETE_AFTER_CONSUMING:
						if (m_resultConsumer != null) {
							m_resultConsumer.accept(state, getValue(stageState));
						}

//						// Reset any local state if necessary
//						reset(stageState);

						return new ParserState(stageState.buffer, state.builder);
					case COMPLETE_WITHOUT_CONSUMING:
						if (m_resultConsumer != null) {
							m_resultConsumer.accept(state, getValue(stageState));
						}

						// Reset any local state if necessary
						reset(stageState);

						// Move the mark back before the current character
						stageState.buffer.reset();

						return new ParserState(stageState.buffer, state.builder);
					case CANCEL:
						if (m_optional) {
							stageState.buffer.reset();

							// Reset any local state if necessary
							reset(stageState);

							return new ParserState(stageState.buffer, state.builder);
						} else {
							// Match failed
							return null;
						}
				}
			}
		}

		public void reset(ParserStageState state) {
			// Do nothing by default
		}

		protected static void accumulate(ParserStageState state, char c) {
			state.accumulate(c);
		}

		protected static int getAccumulatedSize(ParserStageState state) {
			return state.getAccumulatedSize();
		}

		protected static String getAccumulatedValue(ParserStageState state) {
			return state.accumulatedValue.toString();
		}

		protected R getValue(ParserStageState state) {
			return null;
		}
	}

	/**
	 * Match any whitespace character.
	 */
	public static class MatchWhitespace extends AbstractParserStage<Void> {
		@Override
		public AcceptResult acceptChar(ParserStageState state, char c) {
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

		public char getChar() {
			return m_char;
		}

		@Override
		public AcceptResult acceptChar(ParserStageState state, char c) {
			if (c == m_char) {
//				System.out.println("ACCEPT " + m_char);
				return AcceptResult.COMPLETE_AFTER_CONSUMING;
			} else {
//				System.out.println("CANCEL " + m_char);
				return AcceptResult.CANCEL;
			}
		}

		@Override
		public boolean equals(Object o) {
			if (o == this) return true;
			if (!(o instanceof MatchChar)) return false;
			MatchChar other = (MatchChar)o;
			return Objects.equals(m_char, other.getChar());
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
		public AcceptResult acceptChar(ParserStageState state, char c) {
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
		public Integer getValue(ParserStageState state) {
			return state.index;
		}

		@Override
		public void reset(ParserStageState state) {
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
		public AcceptResult acceptChar(ParserStageState state, char c) {
			accumulate(state, c);
			if (getAccumulatedSize(state) >= m_length) {
				return AcceptResult.COMPLETE_AFTER_CONSUMING;
			} else {
				return AcceptResult.CONTINUE;
			}
		}
		
		@Override
		public String getValue(ParserStageState state) {
			return getAccumulatedValue(state);
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
		public AcceptResult acceptChar(ParserStageState state, char c) {
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
		public String getValue(ParserStageState state) {
			return getAccumulatedValue(state);
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
		public Integer getValue(ParserStageState state) {
			// Trim the leading zeros from this value
			String value = getAccumulatedValue(state);
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
		public AcceptResult acceptChar(ParserStageState state, char c) {
			if (c >= '0' && c <= '9') {
				accumulate(state, c);
				return AcceptResult.CONTINUE;
			} else {
				// If any characters were accumulated, complete
				if (getAccumulatedSize(state) > 0) {
					return AcceptResult.COMPLETE_WITHOUT_CONSUMING;
				} else {
					return AcceptResult.CANCEL;
				}
			}
		}

		@Override
		public Integer getValue(ParserStageState state) {
			// Trim the leading zeros from this value
			String value = getAccumulatedValue(state);
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

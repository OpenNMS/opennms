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
import java.util.Arrays;
import java.util.Collections;
import java.util.EmptyStackException;
import java.util.List;
import java.util.Objects;
import java.util.Stack;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.opennms.core.collections.RadixTree;
import org.opennms.core.collections.RadixTreeImpl;
import org.opennms.core.collections.RadixTreeNode;
import org.opennms.core.utils.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>This class is used to construct a sequence of {@link ParserStage} objects
 * that can parse {@link ByteBuffer} objects into tokens.</p>
 * 
 * <p>As each token is completed, the value can be used to invoke a {@link BiConsumer}
 * method to dynamically construct objects such as OpenNMS events from the tokens.</p>
 * 
 * @author Seth
 */
public class ParserStageSequenceBuilder {

	private static final Logger LOG = LoggerFactory.getLogger(ParserStageSequenceBuilder.class);

	/**
	 * The state of an individual {@link ParserStage} operation.
	 */
	private static class ParserStageState {
		public final ByteBuffer buffer;

		private StringBuilder accumulatedValue = null;
		private AtomicInteger accumulatedSize = null;

		// Only used by MatchMonth
		public RadixTreeNode<CharacterWithValue> currentNode = null; 

		public ParserStageState(ByteBuffer input) {
			buffer = input;
		}

		public void accumulate(char c) {
			accessAccumulatedValue().append(c);
			accessAccumulatedSize().incrementAndGet();
		}

		public int getAccumulatedSize() {
			return accessAccumulatedSize().get();
		}

		private final StringBuilder accessAccumulatedValue() {
			if (accumulatedValue == null) {
				accumulatedValue = new StringBuilder();
			}
			return accumulatedValue;
		}

		private final AtomicInteger accessAccumulatedSize() {
			if (accumulatedSize == null) {
				accumulatedSize = new AtomicInteger();
			}
			return accumulatedSize;
		}

		@Override
		public String toString() {
			return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
				.append("accumulatedValue", accumulatedValue == null ? "null" : accumulatedValue.toString())
				.append("accumulatedSize", accumulatedSize == null ? 0 : accumulatedSize.get())
				.toString();
		}
	}

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

	public ParserStageSequenceBuilder monthString(BiConsumer<ParserState, Integer> consumer) {
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

	/**
	 * This base class handles most of the statefulness of each {@link ParserStage}.
	 * 
	 * @param <R> Type of the value that can be emitted by this stage to the
	 * {@link BiConsumer} consumer.
	 */
	private static abstract class AbstractParserStage<R> implements ParserStage {

		private boolean m_optional = false;
		private boolean m_terminal = false;
		protected final BiConsumer<ParserState, R> m_resultConsumer;

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

		public final ParserState apply(final ParserState incomingState) {
			if (incomingState == null) {
				return null;
			} else {
				LOG.trace("Starting stage: " + this);
			}

			ParserState state = incomingState.clone();

			// Create a new state for the current ParserStage.
			// Use ByteBuffer.duplicate() to create a buffer with marks
			// and positions that only this stage will use.
//			ParserStageState stageState = new ParserStageState(state.getBuffer().duplicate()); 
			ParserStageState stageState = new ParserStageState(state.getBuffer()); 

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

						LOG.trace("End of buffer with terminal match");
						return new ParserState(stageState.buffer, state.message);
					} else if (m_optional) {
//						// TODO: Should we reset the buffer here? It probably
//						// doesn't matter since we're at the end of the buffer.
//						stageState.buffer.reset();

//						// Reset any local state if necessary
//						reset(stageState);

						LOG.trace("End of buffer with optional match");
						return new ParserState(stageState.buffer, state.message);
					} else {
						// Reached end of buffer, match failed
						LOG.trace("Parse failed due to buffer underflow: " + this);
						return null;
					}
				}

				switch (acceptChar(stageState, c)) {
					case CONTINUE:
						continue;
					case COMPLETE_AFTER_CONSUMING:
						if (m_resultConsumer != null) {
							try {
								m_resultConsumer.accept(state, getValue(stageState));
							} catch (Exception e) {
								// Conversion to value failed
								LOG.trace("Parse failed on result consumer: {}", stageState, e);
								return null;
							}
						}

//						// Reset any local state if necessary
//						reset(stageState);

						return new ParserState(stageState.buffer, state.message);
					case COMPLETE_WITHOUT_CONSUMING:
						if (m_resultConsumer != null) {
							try {
								m_resultConsumer.accept(state, getValue(stageState));
							} catch (Exception e) {
								// Conversion to value failed
								LOG.trace("Parse failed on result consumer: {}", stageState, e);
								return null;
							}
						}

						// Reset any local state if necessary
						reset(stageState);

						// Move the mark back before the current character
						stageState.buffer.reset();

						return new ParserState(stageState.buffer, state.message);
					case CANCEL:
						if (m_optional) {
							stageState.buffer.reset();

							// Reset any local state if necessary
							reset(stageState);

							return new ParserState(stageState.buffer, state.message);
						} else {
							// Match failed
							LOG.trace("Parse failed: {}", this);
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
			final StringBuilder accumulatedValue = state.accumulatedValue;
			if (accumulatedValue == null) {
				return null;
			} else {
				return accumulatedValue.toString();
			}
		}

		protected R getValue(ParserStageState state) {
			return null;
		}
	}

	/**
	 * Match 0...n whitespace characters.
	 */
	static class MatchWhitespace extends AbstractParserStage<Void> {
		@Override
		public AcceptResult acceptChar(ParserStageState state, char c) {
			// TODO: Make this more efficient
			if ("".equals(String.valueOf(c).trim())) {
				return AcceptResult.CONTINUE;
			} else {
				return AcceptResult.COMPLETE_WITHOUT_CONSUMING;
			}
		}

		@Override
		public boolean equals(Object o) {
			if (o == null) return false;
			if (o == this) return true;
			if (!(o instanceof MatchWhitespace)) return false;
			MatchWhitespace other = (MatchWhitespace)o;
			return Objects.equals(m_resultConsumer, other.m_resultConsumer);
		}

		@Override
		public String toString() {
			return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
				.toString();
		}
	}

	/**
	 * Match a single character.
	 */
	static class MatchChar extends AbstractParserStage<Void> {
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
				return AcceptResult.COMPLETE_AFTER_CONSUMING;
			} else {
				return AcceptResult.CANCEL;
			}
		}

		@Override
		public boolean equals(Object o) {
			if (o == null) return false;
			if (o == this) return true;
			if (!(o instanceof MatchChar)) return false;
			MatchChar other = (MatchChar)o;
			return Objects.equals(m_char, other.getChar()) &&
					Objects.equals(m_resultConsumer, other.m_resultConsumer);
		}

		@Override
		public String toString() {
			return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
				.append("char", m_char)
				.toString();
		}
	}

	/**
	 * Immutable char/int pair.
	 */
	private static class CharacterWithValue {
		private final char character;
		private final int value;

		public static CharacterWithValue[] toArray(String string, Integer value) {
			return string.chars().mapToObj(c -> new CharacterWithValue((char)c, value)).collect(Collectors.toList()).toArray(new CharacterWithValue[0]);
		}

		private CharacterWithValue(Character character, Integer value) {
			this.character = character;
			this.value = value;
		}

		public char getCharacter() {
			return character;
		}

		public int getValue() {
			return value;
		}

		@Override
		public boolean equals(Object o) {
			if (o == null) return false;
			if (o == this) return true;
			if (!(o instanceof CharacterWithValue)) return false;
			CharacterWithValue other = (CharacterWithValue)o;
			return Objects.equals(character, other.getCharacter());
		}

		@Override
		public String toString() {
			return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
				.append("character", character)
				.append("value", value)
				.toString();
		}
	}

	/**
	 * Match a 3-character en_us month string as specified in syslog RFC 3164.
	 * This {@link ParserStage} uses a {@link RadixTree} internally to match
	 * possible strings.
	 */
	static class MatchMonth extends AbstractParserStage<Integer> {
		private static final RadixTree<CharacterWithValue> MONTH_STRINGS = new RadixTreeImpl<>();

		static {
			MONTH_STRINGS.addChildren(CharacterWithValue.toArray("Jan", 1));
			MONTH_STRINGS.addChildren(CharacterWithValue.toArray("jan", 1));
			MONTH_STRINGS.addChildren(CharacterWithValue.toArray("Feb", 2));
			MONTH_STRINGS.addChildren(CharacterWithValue.toArray("feb", 2));
			MONTH_STRINGS.addChildren(CharacterWithValue.toArray("Mar", 3));
			MONTH_STRINGS.addChildren(CharacterWithValue.toArray("mar", 3));
			MONTH_STRINGS.addChildren(CharacterWithValue.toArray("Apr", 4));
			MONTH_STRINGS.addChildren(CharacterWithValue.toArray("apr", 4));
			MONTH_STRINGS.addChildren(CharacterWithValue.toArray("May", 5));
			MONTH_STRINGS.addChildren(CharacterWithValue.toArray("may", 5));
			MONTH_STRINGS.addChildren(CharacterWithValue.toArray("Jun", 6));
			MONTH_STRINGS.addChildren(CharacterWithValue.toArray("jun", 6));
			MONTH_STRINGS.addChildren(CharacterWithValue.toArray("Jul", 7));
			MONTH_STRINGS.addChildren(CharacterWithValue.toArray("jul", 7));
			MONTH_STRINGS.addChildren(CharacterWithValue.toArray("Aug", 8));
			MONTH_STRINGS.addChildren(CharacterWithValue.toArray("aug", 8));
			MONTH_STRINGS.addChildren(CharacterWithValue.toArray("Sep", 9));
			MONTH_STRINGS.addChildren(CharacterWithValue.toArray("sep", 9));
			MONTH_STRINGS.addChildren(CharacterWithValue.toArray("Oct", 10));
			MONTH_STRINGS.addChildren(CharacterWithValue.toArray("oct", 10));
			MONTH_STRINGS.addChildren(CharacterWithValue.toArray("Nov", 11));
			MONTH_STRINGS.addChildren(CharacterWithValue.toArray("nov", 11));
			MONTH_STRINGS.addChildren(CharacterWithValue.toArray("Dec", 12));
			MONTH_STRINGS.addChildren(CharacterWithValue.toArray("dec", 12));
		}

		public MatchMonth(BiConsumer<ParserState,Integer> consumer) {
			super(consumer);
		}

		@Override
		public AcceptResult acceptChar(ParserStageState state, char c) {
			if (state.currentNode == null) {
				state.currentNode = MONTH_STRINGS;
			}

			for (RadixTreeNode<CharacterWithValue> child : state.currentNode.getChildren()) {
				if (child.getContent().getCharacter() == c) {
					state.currentNode = child;

					// If the child is a leaf node, then complete this stage
					if (child.getChildren().isEmpty()) {
						return AcceptResult.COMPLETE_AFTER_CONSUMING;
					} else {
						return AcceptResult.CONTINUE;
					}
				}
			}
			// No children matched, cancel the stage
			return AcceptResult.CANCEL;
		}

		@Override
		public Integer getValue(ParserStageState state) {
			return state.currentNode.getContent().getValue();
		}

		@Override
		public void reset(ParserStageState state) {
			super.reset(state);
			// Reset the radix tree node
			state.currentNode = null;
		}

		@Override
		public boolean equals(Object o) {
			if (o == null) return false;
			if (o == this) return true;
			if (!(o instanceof MatchMonth)) return false;
			MatchMonth other = (MatchMonth)o;
			return Objects.equals(m_resultConsumer, other.m_resultConsumer);
		}

		@Override
		public String toString() {
			return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
				.toString();
		}
	}

	/**
	 * Match any sequence of characters.
	 */
	static class MatchAny extends AbstractParserStage<String> {
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

		public int getLength() {
			return m_length;
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
			return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
				.append("length", m_length)
				.toString();
		}

		@Override
		public boolean equals(Object o) {
			if (o == null) return false;
			if (o == this) return true;
			if (!(o instanceof MatchAny)) return false;
			MatchAny other = (MatchAny)o;
			return Objects.equals(m_length, other.getLength()) &&
					Objects.equals(m_resultConsumer, other.m_resultConsumer);
		}
	}

	/**
	 * Match a string terminated by a character in a list of end tokens.
	 */
	static abstract class MatchUntil<R> extends AbstractParserStage<R> {
		public static final String WHITESPACE = "\\s";

		private final char[] m_end;
		private boolean m_endOnwhitespace = false;

		MatchUntil(BiConsumer<ParserState,R> consumer, char end) {
			super(consumer);
			m_end = new char[] { end };
		}

		public char[] getEnd() {
			return m_end;
		}

		public boolean isEndOnWhitespace() {
			return m_endOnwhitespace;
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
			return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
				.append("ends", m_end)
				.append("endOnWhitespace", m_endOnwhitespace)
				.toString();
		}

		@Override
		public boolean equals(Object o) {
			if (o == null) return false;
			if (o == this) return true;
			if (!(o instanceof MatchUntil)) return false;
			MatchUntil<?> other = (MatchUntil<?>)o;
			return Arrays.equals(m_end, other.getEnd()) &&
					Objects.equals(m_endOnwhitespace, other.isEndOnWhitespace() &&
					Objects.equals(m_resultConsumer, other.m_resultConsumer)
			);
		}
	}

	static class MatchStringUntil extends MatchUntil<String> {
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

		@Override
		public boolean equals(Object o) {
			if (o == null) return false;
			if (o == this) return true;
			if (!(o instanceof MatchStringUntil)) return false;
			return super.equals(o);
		}
	}

	static class MatchIntegerUntil extends MatchUntil<Integer> {
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
			boolean trimmed = false;
			while (value.startsWith("0")) {
				value = value.substring(1);
				trimmed = true;
			}

			if ("".equals(value)) {
				return trimmed ? 0 : null;
			} else {
				return StringUtils.parseDecimalInt(value, false);
			}
		}

		@Override
		public boolean equals(Object o) {
			if (o == null) return false;
			if (o == this) return true;
			if (!(o instanceof MatchIntegerUntil)) return false;
			return super.equals(o);
		}
	}

	/**
	 * Match an integer.
	 */
	static class MatchInteger extends AbstractParserStage<Integer> {
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
			return trimAndConvert(getAccumulatedValue(state));
		}

		/**
		 * Trim the leading zeros from the {@link String} and
		 * convert what is left to an integer.
		 * 
		 * @param value
		 * @return
		 */
		public static int trimAndConvert(String value) {
			boolean trimmed = false;
			while (value.startsWith("0")) {
				value = value.substring(1);
				trimmed = true;
			}

			if ("".equals(value)) {
				return trimmed ? 0 : null;
			} else {
				return StringUtils.parseDecimalInt(value, false);
			}
		}

		@Override
		public boolean equals(Object o) {
			if (o == null) return false;
			if (o == this) return true;
			if (!(o instanceof MatchInteger)) return false;
			MatchInteger other = (MatchInteger)o;
			return Objects.equals(m_resultConsumer, other.m_resultConsumer);
		}

		@Override
		public String toString() {
			return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
				.toString();
		}
	}
}

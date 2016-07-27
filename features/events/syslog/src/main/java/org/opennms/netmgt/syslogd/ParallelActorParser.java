package org.opennms.netmgt.syslogd;

import java.io.InputStream;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.EmptyStackException;
import java.util.List;
import java.util.Stack;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.junit.Test;
import org.opennms.core.test.ConfigurationTestUtils;
import org.opennms.core.test.MockLogAppender;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.config.SyslogdConfig;
import org.opennms.netmgt.config.SyslogdConfigFactory;
import org.opennms.netmgt.dao.api.DistPollerDao;
import org.opennms.netmgt.model.events.EventBuilder;
import org.opennms.netmgt.xml.event.Event;

public class ParallelActorParser {

	private final ExecutorService m_executor = Executors.newSingleThreadExecutor();

	//private final ExecutorService m_executor = new ExecutorFactoryCassandraSEPImpl().newExecutor("StagedParser", "StageExecutor");

	//private final ExecutorService m_executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() * 2);

	public static class SyslogParserState {
		public ByteBuffer buffer;
		public EventBuilder builder;
		public StringBuffer accumulatedValue;
		public AtomicInteger accumulatedSize;
		public int index = -1;
		public int charIndex = -1;
	}

	public static enum GrokState {
		TEXT,
		ESCAPE_PATTERN,
		START_PATTERN,
		PATTERN,
		SEMANTIC,
		END_PATTERN
	}
	
	public static enum GrokPattern {
		STRING,
		INTEGER,
		MONTH
	}

	public static final ParserFactory parseGrok(String grok) {
		GrokState state = GrokState.TEXT;
		ParserFactory factory = new ParserFactory();

		StringBuffer pattern = new StringBuffer();
		StringBuffer semantic = new StringBuffer();

		for (char c : grok.toCharArray()) {
			switch(state) {
			case TEXT:
				switch(c) {
				case '%':
					state = GrokState.START_PATTERN;
					continue;
				case '\\':
					state = GrokState.ESCAPE_PATTERN;
					continue;
				case ' ':
					factory = factory.whitespace();
					continue;
				default:
					factory = factory.character(c);
					continue;
				}
			case ESCAPE_PATTERN:
				switch(c) {
				default:
					factory = factory.character(c);
					state = GrokState.TEXT;
					continue;
				}
			case START_PATTERN:
				switch(c) {
				case '{':
					state = GrokState.PATTERN;
					continue;
				default:
					throw new IllegalStateException("Illegal character to start pattern");
				}
			case PATTERN:
				switch(c) {
				case ':':
					state = GrokState.SEMANTIC;
					continue;
				default:
					pattern.append(c);
					continue;
				}
			case SEMANTIC:
				switch(c) {
				case '}':
					state = GrokState.END_PATTERN;
					continue;
				default:
					semantic.append(c);
					continue;
				}
			case END_PATTERN:
				final String patternString = pattern.toString();
				final String semanticString = semantic.toString();
				System.out.println(semanticString);
				GrokPattern patternType = GrokPattern.valueOf(patternString);
				switch(c) {
				case ' ':
					switch(patternType) {
					case STRING:
						factory.stringUntilWhitespace((s,v) -> {
							s.builder.addParam(semanticString, v);
						});
						factory.whitespace();
						break;
					case INTEGER:
						factory.intUntilWhitespace((s,v) -> {
							s.builder.addParam(semanticString, v);
						});
						factory.whitespace();
						break;
					case MONTH:
						factory.month((s,v) -> {
							s.builder.addParam(semanticString, v);
						});
						factory.whitespace();
						break;
					}
					break;
				default:
					switch(patternType) {
					case STRING:
						factory.stringUntil(String.valueOf(c), (s,v) -> {
							s.builder.addParam(semanticString, v);
						});
						factory.character(c);
						break;
					case INTEGER:
						factory.integer((s,v) -> {
							s.builder.addParam(semanticString, v);
						});
						factory.character(c);
						break;
					case MONTH:
						factory.month((s,v) -> {
							s.builder.addParam(semanticString, v);
						});
						factory.character(c);
						break;
					}
				}
				pattern = new StringBuffer();
				semantic = new StringBuffer();
				state = GrokState.TEXT;
				continue;
			}
		}

		// If we are in the process of ending a pattern, then wrap it up with bow
		if (state == GrokState.END_PATTERN) {
			final String patternString = pattern.toString();
			final String semanticString = semantic.toString();
			System.out.println(semanticString);
			GrokPattern patternType = GrokPattern.valueOf(patternString);

			switch(patternType) {
			case STRING:
				factory.terminal().string((s,v) -> {
					s.builder.addParam(semanticString, v);
				});
				break;
			case INTEGER:
				factory.terminal().integer((s,v) -> {
					s.builder.addParam(semanticString, v);
				});
				break;
			case MONTH:
				factory.terminal().month((s,v) -> {
					s.builder.addParam(semanticString, v);
				});
				break;
			}
		}

		return factory;
	}

	@Test
	public void testMe() throws Exception {

		MockLogAppender.setupLogging(true, "INFO");

		String abc = "<190>Mar 11 08:35:17 127.0.0.1 30128311[4]: Mar 11 08:35:16.844 CST: %SEC-6-IPACCESSLOGP: list in110 denied tcp 192.168.10.100(63923) -> 192.168.11.128(1521), 1 packet";
		//String abc = "<190>Mar 11 08:35:17 127.0.0.1 30128311: Mar 11 08:35:16.844 CST: %SEC-6-IPACCESSLOGP: list in110 denied tcp 192.168.10.100(63923) -> 192.168.11.128(1521), 1 packet";
		ByteBuffer incoming = ByteBuffer.wrap(abc.getBytes());

		AtomicReference<SyslogFacility> facility = new AtomicReference<>();
		AtomicReference<Integer> year = new AtomicReference<>();
		AtomicReference<Integer> month = new AtomicReference<>();
		AtomicReference<Integer> day = new AtomicReference<>();
		AtomicReference<Integer> hour = new AtomicReference<>();
		AtomicReference<Integer> minute = new AtomicReference<>();
		AtomicReference<Integer> second = new AtomicReference<>();

		ParserFactory grokFactory = parseGrok("<%{INTEGER:facilityPriority}> %{MONTH:month} %{INTEGER:day} %{INTEGER:hour}:%{INTEGER:minute}:%{INTEGER:second} %{STRING:hostname} %{STRING:processName}[%{INTEGER:processId}]: %{STRING:month} %{INTEGER:day} %{STRING:timestamp} %{STRING:timezone} \\%%{STRING:facility}-%{INTEGER:priority}-%{STRING:mnemonic}: %{STRING:message}");
		//ParserFactory grokFactory = parseGrok("<%{INTEGER:facilityPriority}> %{MONTH:month} %{INTEGER:day} %{INTEGER:hour}:%{INTEGER:minute}:%{INTEGER:second} %{STRING:hostname} %{STRING:processName}: %{STRING:month} %{INTEGER:day} %{STRING:timestamp} %{STRING:timezone} \\%%{STRING:facility}-%{INTEGER:priority}-%{STRING:mnemonic}: %{STRING:message}");

		// SyslogNG format
		ParserFactory factory = new ParserFactory()
			.intBetweenDelimiters('<', '>', (s,v) -> { facility.set(SyslogFacility.getFacilityForCode(v)); })
			.whitespace()
			.month((s,v) -> month.set(v))
			.whitespace()
			.integer((s,v) -> day.set(v))
			.whitespace()
			.integer((s,v) -> hour.set(v))
			.character(':')
			.integer((s,v) -> minute.set(v))
			.character(':')
			.integer((s,v) -> second.set(v))
			.whitespace()
			.stringUntilWhitespace((s,v) -> { s.builder.setHost(v); })
			.whitespace()
			.stringUntil("\\s[:", (s,v) -> { s.builder.addParam("processName", v); })
			.optional().character('[')
			.optional().integer((s,v) -> { s.builder.addParam("processId", v); })
			.optional().character(']')
			.optional().character(':')
			.whitespace()
			.stringUntilWhitespace(null) // Original month
			.whitespace()
			.integer(null) // Original day
			.whitespace()
			.stringUntilWhitespace(null) // Original timestamp
			.whitespace()
			.stringUntilWhitespace(null) // Original time zone
			.whitespace()
			.character('%')
			.stringUntilChar('-', (s,v) -> { /* TODO: Set facility */ })
			.character('-')
			.stringUntilChar('-', (s,v) -> { /* TODO: Set severity */ })
			.character('-')
			.stringUntilChar(':', (s,v) -> { /* TODO: Set mnemonic */ })
			.character(':')
			.whitespace()
			.terminal().string((s,v) -> { s.builder.setLogMessage(v); })
			;

		int iterations = 100000;

		CompletableFuture<Event> event = null;
		long start = System.currentTimeMillis();
		for (int i = 0; i < iterations; i++) {
			event = factory.parse(incoming.asReadOnlyBuffer(), m_executor);
			event.whenComplete((e, ex) -> {
				if (ex == null) {
					//System.out.println(e.toString());
				} else {
					ex.printStackTrace();
				}
			});
		}
		// Wait for the last future to complete
		try {
			event.get();
		} catch (InterruptedException | ExecutionException e) {
			e.printStackTrace();
		}
		long end = System.currentTimeMillis();
		System.out.println("NEW: " + (end - start) + "ms");

		start = System.currentTimeMillis();
		for (int i = 0; i < iterations; i++) {
			event = grokFactory.parse(incoming.asReadOnlyBuffer(), m_executor);
			event.whenComplete((e, ex) -> {
				if (ex == null) {
					//System.out.println(e.toString());
				} else {
					ex.printStackTrace();
				}
			});
		}
		// Wait for the last future to complete
		try {
			event.get();
		} catch (InterruptedException | ExecutionException e) {
			e.printStackTrace();
		}
		end = System.currentTimeMillis();
		System.out.println("GROK: " + (end - start) + "ms");

		InputStream stream = ConfigurationTestUtils.getInputStreamForResource(this, "/etc/syslogd-syslogng-configuration.xml");
		SyslogdConfig config = new SyslogdConfigFactory(stream);

		start = System.currentTimeMillis();
		for (int i = 0; i < iterations; i++) {
			ConvertToEvent convertToEvent = new ConvertToEvent(
				DistPollerDao.DEFAULT_DIST_POLLER_ID,
				InetAddressUtils.ONE_TWENTY_SEVEN,
				9999,
				abc, 
				config
			);
			Event convertedEvent = convertToEvent.getEvent();
		}
		end = System.currentTimeMillis();
		System.out.println("OLD: " + (end - start) + "ms");

	}

	public static class ParserFactory {
		final List<Stage> m_stages = new ArrayList<>();
		final Stack<Boolean> m_optional = new Stack<>();
		final Stack<Boolean> m_terminal = new Stack<>();

		ThreadLocal<EventBuilder> m_builder = new ThreadLocal<EventBuilder>();

		public ParserFactory() {
		}

		public ParserFactory optional() {
			m_optional.push(true);
			return this;
		}

		public ParserFactory terminal() {
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

		private void addStage(Stage stage) {
			stage.setOptional(getOptional());
			stage.setTerminal(getTerminal());
			System.out.println(stage.toString());
			m_stages.add(stage);
		}

		public ParserFactory whitespace() {
			addStage(new MatchWhitespace());
			return this;
		}

		public ParserFactory character(char character) {
			addStage(new MatchChar(character));
			return this;
		}

		public ParserFactory string(BiConsumer<SyslogParserState, String> consumer) {
			addStage(new MatchAny(consumer, Integer.MAX_VALUE));
			return this;
		}

		public ParserFactory integer(BiConsumer<SyslogParserState, Integer> consumer) {
			addStage(new MatchInteger(consumer));
			return this;
		}

		public ParserFactory month(BiConsumer<SyslogParserState, Integer> consumer) {
			addStage(new MatchMonth(consumer));
			return this;
		}

		public ParserFactory stringUntil(String ends, BiConsumer<SyslogParserState,String> consumer) {
			addStage(new MatchStringUntil(consumer, ends));
			return this;
		}

		public ParserFactory stringUntilWhitespace(BiConsumer<SyslogParserState,String> consumer) {
			addStage(new MatchStringUntil(consumer, MatchUntil.WHITESPACE));
			return this;
		}

		public ParserFactory stringUntilChar(char end, BiConsumer<SyslogParserState,String> consumer) {
			addStage(new MatchStringUntil(consumer, end));
			return this;
		}

		public ParserFactory intUntilWhitespace(BiConsumer<SyslogParserState,Integer> consumer) {
			addStage(new MatchIntegerUntil(consumer, MatchUntil.WHITESPACE));
			return this;
		}

		public ParserFactory stringBetweenDelimiters(char start, char end, BiConsumer<SyslogParserState,String> consumer) {
			addStage(new MatchChar(start));
			addStage(new MatchStringUntil(consumer, end));
			addStage(new MatchChar(end));
			return this;
		}

		public ParserFactory intBetweenDelimiters(char start, char end, BiConsumer<SyslogParserState,Integer> consumer) {
			addStage(new MatchChar(start));
			addStage(new MatchIntegerUntil(consumer, end));
			addStage(new MatchChar(end));
			return this;
		}

		public CompletableFuture<Event> parse(ByteBuffer incoming, ExecutorService executor) {

			// Put all mutable parts of the parse operation into a state object
			final SyslogParserState state = new SyslogParserState();
			state.buffer = incoming;
			state.builder = new EventBuilder();

			CompletableFuture<SyslogParserState> future = CompletableFuture.completedFuture(state);

			// Apply each parse stage to the message
			for (Stage stage : m_stages) {
				future = future.thenApply(stage::apply);
			}

			//future.exceptionally(e -> { /* DO SOMETHING */ return null; });

			return future.thenApply(s -> {
				return s.builder.getEvent();
			});
		}
	}

	public interface Stage {
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
		SyslogParserState apply(SyslogParserState state);
	}

	public static abstract class AbstractStage<R> implements Stage {

		private boolean m_optional = false;
		private boolean m_terminal = false;
		private final BiConsumer<SyslogParserState, R> m_resultConsumer;

		protected AbstractStage() {
			this(null);
		}

		protected AbstractStage(BiConsumer<SyslogParserState,R> resultConsumer) {
			m_resultConsumer = resultConsumer;
		}

		@Override
		public void setOptional(boolean optional) {
			m_optional = optional;
		}

		@Override
		public void setTerminal(boolean terminal) {
			m_terminal = terminal;
		}

		public enum AcceptResult {
			CONTINUE,
			COMPLETE_AFTER_CONSUMING,
			COMPLETE_WITHOUT_CONSUMING,
			CANCEL
		}

		public abstract AcceptResult acceptChar(SyslogParserState state, char c);

		public final SyslogParserState apply(SyslogParserState state) {
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

		public void reset(SyslogParserState state) {
			// Do nothing by default
		}

		protected static void accumulate(SyslogParserState state, char c) {
			state.accumulatedValue.append(c);
			state.accumulatedSize.incrementAndGet();
		}

		protected static int accumulatedSize(SyslogParserState state) {
			return state.accumulatedSize.get();
		}

		protected static String accumulatedValue(SyslogParserState state) {
			return state.accumulatedValue.toString();
		}

		protected R getValue(SyslogParserState state) {
			return null;
		}
	}

	/**
	 * Match any whitespace character.
	 */
	public static class MatchWhitespace extends AbstractStage<Void> {
		@Override
		public AcceptResult acceptChar(SyslogParserState state, char c) {
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
	public static class MatchChar extends AbstractStage<Void> {
		private final char m_char;
		MatchChar(char c) {
			super();
			m_char = c;
		}

		@Override
		public AcceptResult acceptChar(SyslogParserState state, char c) {
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
	public static class MatchMonth extends AbstractStage<Integer> {
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

		public MatchMonth(BiConsumer<SyslogParserState,Integer> consumer) {
			super(consumer);
		}

		@Override
		public AcceptResult acceptChar(SyslogParserState state, char c) {
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
		public Integer getValue(SyslogParserState state) {
			return state.index;
		}

		@Override
		public void reset(SyslogParserState state) {
			super.reset(state);
			state.charIndex = -1;
			state.index = -1;
		}
	}

	/**
	 * Match any sequence of characters.
	 */
	public static class MatchAny extends AbstractStage<String> {
		private final int m_length;

		public MatchAny() {
			this(null, 1);
		}

		public MatchAny(BiConsumer<SyslogParserState,String> consumer) {
			this(consumer, 1);
		}

		public MatchAny(int length) {
			this(null, length);
		}

		public MatchAny(BiConsumer<SyslogParserState,String> consumer, int length) {
			super(consumer);
			m_length = length;
		}

		@Override
		public AcceptResult acceptChar(SyslogParserState state, char c) {
			accumulate(state, c);
			if (accumulatedSize(state) >= m_length) {
				return AcceptResult.COMPLETE_AFTER_CONSUMING;
			} else {
				return AcceptResult.CONTINUE;
			}
		}
		
		@Override
		public String getValue(SyslogParserState state) {
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
	public static abstract class MatchUntil<R> extends AbstractStage<R> {
		public static final String WHITESPACE = "\\s";

		private final char[] m_end;
		private boolean m_endOnwhitespace = false;

		MatchUntil(BiConsumer<SyslogParserState,R> consumer, char end) {
			super(consumer);
			m_end = new char[] { end };
		}

		MatchUntil(BiConsumer<SyslogParserState,R> consumer, String end) {
			super(consumer);
			m_endOnwhitespace = end.contains(WHITESPACE);
			// Erase the WHITESPACE token from the end char list
			end = end.replaceAll("\\\\s", "");
			m_end = end.toCharArray();
		}

		@Override
		public AcceptResult acceptChar(SyslogParserState state, char c) {
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
		public MatchStringUntil(BiConsumer<SyslogParserState,String> consumer, char end) {
			super(consumer, end);
		}

		public MatchStringUntil(BiConsumer<SyslogParserState,String> consumer, String ends) {
			super(consumer, ends);
		}

		@Override
		public String getValue(SyslogParserState state) {
			return accumulatedValue(state);
		}
	}

	public static class MatchIntegerUntil extends MatchUntil<Integer> {
		public MatchIntegerUntil(BiConsumer<SyslogParserState,Integer> consumer, char end) {
			super(consumer, end);
		}

		public MatchIntegerUntil(BiConsumer<SyslogParserState,Integer> consumer, String ends) {
			super(consumer, ends);
		}

		@Override
		public Integer getValue(SyslogParserState state) {
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
	public static class MatchInteger extends AbstractStage<Integer> {
		MatchInteger(BiConsumer<SyslogParserState,Integer> consumer) {
			super(consumer);
		}

		@Override
		public AcceptResult acceptChar(SyslogParserState state, char c) {
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
		public Integer getValue(SyslogParserState state) {
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

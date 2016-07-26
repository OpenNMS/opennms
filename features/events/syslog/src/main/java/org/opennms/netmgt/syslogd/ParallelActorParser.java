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

	@Test
	public void testMe() throws Exception {

		MockLogAppender.setupLogging(true, "INFO");

		String abc = "<190>Mar 11 08:35:17 127.0.0.1 30128311[4]: Mar 11 08:35:16.844 CST: %SEC-6-IPACCESSLOGP: list in110 denied tcp 192.168.10.100(63923) -> 192.168.11.128(1521), 1 packet";
		ByteBuffer incoming = ByteBuffer.wrap(abc.getBytes());

		AtomicReference<SyslogFacility> facility = new AtomicReference<>();
		AtomicReference<Integer> year = new AtomicReference<>();
		AtomicReference<Integer> month = new AtomicReference<>();
		AtomicReference<Integer> day = new AtomicReference<>();
		AtomicReference<Integer> hour = new AtomicReference<>();
		AtomicReference<Integer> minute = new AtomicReference<>();
		AtomicReference<Integer> second = new AtomicReference<>();

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
			Event event2 = convertToEvent.getEvent();
		}
		end = System.currentTimeMillis();
		System.out.println("OLD: " + (end - start) + "ms");

	}

	public class ParserFactory {
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
			addStage(new MatchStringUntilChar(consumer, ends));
			return this;
		}

		public ParserFactory stringUntilWhitespace(BiConsumer<SyslogParserState,String> consumer) {
			addStage(new MatchStringUntilWhitespace(consumer));
			return this;
		}

		public ParserFactory stringUntilChar(char end, BiConsumer<SyslogParserState,String> consumer) {
			addStage(new MatchStringUntilChar(consumer, end));
			return this;
		}

		public ParserFactory intUntilWhitespace(BiConsumer<SyslogParserState,Integer> consumer) {
			addStage(new MatchIntegerUntilWhitespace(consumer));
			return this;
		}

		public ParserFactory stringBetweenDelimiters(char start, char end, BiConsumer<SyslogParserState,String> consumer) {
			addStage(new MatchChar(start));
			addStage(new MatchStringUntilChar(consumer, end));
			addStage(new MatchChar(end));
			return this;
		}

		public ParserFactory intBetweenDelimiters(char start, char end, BiConsumer<SyslogParserState,Integer> consumer) {
			addStage(new MatchChar(start));
			addStage(new MatchIntegerUntilChar(consumer, end));
			addStage(new MatchChar(end));
			return this;
		}

		public CompletableFuture<Event> parse(ByteBuffer incoming, ExecutorService executor) {

			// Put all mutable parts of the parse operation into a state object
			SyslogParserState state = new SyslogParserState();
			state.buffer = incoming;
			state.builder = new EventBuilder();

			CompletableFuture<SyslogParserState> future = CompletableFuture.completedFuture(state);

			// Apply each parse stage to the message
			for (Stage stage : m_stages) {
				future = future.thenApply(stage::apply);
			}

			future.exceptionally(e -> { /* DO SOMETHING */ return null; });

			return future.thenApply(v -> {
				return state.builder.getEvent();
			});
		}
	}

	public interface Stage {
		void setOptional(boolean optional);

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
	}

	/**
	 * Match a whitespace-terminated value.
	 */
	public static abstract class MatchUntilChar<R> extends AbstractStage<R> {
		private final char[] m_end;
		private boolean m_endOnwhitespace = false;

		MatchUntilChar(BiConsumer<SyslogParserState,R> consumer, char end) {
			super(consumer);
			m_end = new char[] { end };
		}

		MatchUntilChar(BiConsumer<SyslogParserState,R> consumer, String end) {
			super(consumer);
			m_endOnwhitespace = end.contains("\\s");
			end = end.replaceAll("\\\\s", end);
			m_end = end.toCharArray();
		}

		@Override
		public AcceptResult acceptChar(SyslogParserState state, char c) {
			for (char end : m_end) {
				if (end == c) {
					return AcceptResult.COMPLETE_WITHOUT_CONSUMING;
				}
			}
			if (m_endOnwhitespace) {
				// TODO: Make this more efficient?
				if ("".equals(String.valueOf(c).trim())) {
					return AcceptResult.COMPLETE_WITHOUT_CONSUMING;
				}
			}
			accumulate(state, c);
			return AcceptResult.CONTINUE;
		}
	}

	public static class MatchStringUntilChar extends MatchUntilChar<String> {
		public MatchStringUntilChar(BiConsumer<SyslogParserState,String> consumer, char end) {
			super(consumer, end);
		}

		public MatchStringUntilChar(BiConsumer<SyslogParserState,String> consumer, String ends) {
			super(consumer, ends);
		}

		@Override
		public String getValue(SyslogParserState state) {
			return accumulatedValue(state);
		}
	}

	public static class MatchIntegerUntilChar extends MatchUntilChar<Integer> {
		public MatchIntegerUntilChar(BiConsumer<SyslogParserState,Integer> consumer, char end) {
			super(consumer, end);
		}

		public MatchIntegerUntilChar(BiConsumer<SyslogParserState,Integer> consumer, String ends) {
			super(consumer, ends);
		}

		@Override
		public Integer getValue(SyslogParserState state) {
			return Integer.valueOf(accumulatedValue(state));
		}
	}

	/**
	 * Match a whitespace-terminated value.
	 */
	public static abstract class MatchUntilWhitespace<R> extends AbstractStage<R> {

		MatchUntilWhitespace(BiConsumer<SyslogParserState,R> consumer) {
			super(consumer);
		}

		@Override
		public AcceptResult acceptChar(SyslogParserState state, char c) {
			// TODO: Make this more efficient?
			if ("".equals(String.valueOf(c).trim())) {
				return AcceptResult.COMPLETE_WITHOUT_CONSUMING;
			} else {
				accumulate(state, c);
				return AcceptResult.CONTINUE;
			}
		}
	}

	public static class MatchStringUntilWhitespace extends MatchUntilWhitespace<String> {
		public MatchStringUntilWhitespace(BiConsumer<SyslogParserState,String> consumer) {
			super(consumer);
		}

		@Override
		public String getValue(SyslogParserState state) {
			return accumulatedValue(state);
		}
	}

	public static class MatchIntegerUntilWhitespace extends MatchUntilWhitespace<Integer> {
		public MatchIntegerUntilWhitespace(BiConsumer<SyslogParserState,Integer> consumer) {
			super(consumer);
		}

		@Override
		public Integer getValue(SyslogParserState state) {
			return Integer.valueOf(accumulatedValue(state));
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
			// TODO: Trim leading zeros from string value
			return Integer.parseInt(accumulatedValue(state));
		}
	}
}

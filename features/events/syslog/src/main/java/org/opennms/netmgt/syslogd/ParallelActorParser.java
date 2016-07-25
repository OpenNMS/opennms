package org.opennms.netmgt.syslogd;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import org.junit.Test;
import org.opennms.core.concurrent.cassandra.ExecutorFactoryCassandraSEPImpl;
import org.opennms.netmgt.model.events.EventBuilder;
import org.opennms.netmgt.xml.event.Event;

public class ParallelActorParser {

	/*
	public final LinkedBlockingQueue<Callable<Callable<?>>> workQueue = new LinkedBlockingQueue<>();

	//ExecutorService executor = new ThreadPoolExecutor(1, 1, 0, TimeUnit.SECONDS, workQueue);

	public final Thread worker = new Thread() {
		public void run() {
			while(true) {
				try {
					Callable<?> work = workQueue.take();
					Callable<Callable<?>> next = (Callable<Callable<?>>)work.call();
					workQueue.offer(next);
				} catch (InterruptedException e) {
					// TODO: Do something
				} catch (Exception e) {
					// TODO: Do something
				}
			}
		}
	};
	*/

	//private final ExecutorService m_executor = Executors.newSingleThreadExecutor();

	//private final ExecutorService m_executor = new ExecutorFactoryCassandraSEPImpl().newExecutor("StagedParser", "StageExecutor");

	private final ExecutorService m_executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() * 2);

	@Test
	//public void main(final String[] args) {
	public void main() {

		/*
		System.out.println("1: " + CamelUtils.nanoTime());

		Parser parser = new Parser(
			new Stage[] {
				new MatchChar('<'),
				new MatchInteger(event::setPriority),
				new MatchChar('>'),
				new MatchWhitespace(),
				new MatchAny('a'),
				new MatchAny('b')
			}
		);
		*/

		// Try to use ByteBuffer directly instead of LBQ
		//ByteBuffer incoming = new LinkedBlockingDeque<>();

		String abc = "<5>aaabbb    ccc";
		ByteBuffer incoming = ByteBuffer.wrap(abc.getBytes());

		/***
		System.out.println("2: " + CamelUtils.nanoTime());

		EventBuilder builder = new EventBuilder();

		long total = 0; 

		for (int i = 0; i < 100000; i++) {

			AtomicLong end = new AtomicLong();

			SyslogFacility facility = null;

			CompletableFuture future = CompletableFuture
				    .runAsync(new MatchChar(incoming, '<'), m_executor)
				.thenRunAsync(new MatchInteger(incoming, v -> { facility = SyslogFacility.getFacilityForCode(v); }), m_executor)
				.thenRunAsync(new MatchChar(incoming, '>'), m_executor)
				/ *
				.thenRunAsync(new MatchAny(incoming, System.out::println), m_executor)
				.thenRunAsync(new MatchAny(incoming, System.out::println), m_executor)
				.thenRunAsync(new MatchAny(incoming, System.out::println), m_executor)
				.thenRunAsync(new MatchAny(incoming, System.out::println), m_executor)
				.thenRunAsync(new MatchAny(incoming, System.out::println), m_executor)
				.thenRunAsync(new MatchAny(incoming, System.out::println), m_executor)
				* /
				.thenRunAsync(new MatchAny(incoming), m_executor)
				.thenRunAsync(new MatchAny(incoming), m_executor)
				.thenRunAsync(new MatchAny(incoming), m_executor)
				.thenRunAsync(new MatchAny(incoming), m_executor)
				.thenRunAsync(new MatchAny(incoming), m_executor)
				.thenRunAsync(new MatchAny(incoming), m_executor)
				.thenRunAsync(new MatchWhitespace(incoming), m_executor)
				//.thenRunAsync(new MatchAny(incoming, System.out::println), m_executor)
				.exceptionally(e -> { return null; })
				.thenAccept(v -> {
//					System.out.println("5: " + CamelUtils.nanoTime());
//					System.out.println(event.toString());
					end.set(System.nanoTime());
				});
			;

			long start = System.nanoTime();
			//System.out.println("3: " + );
			/ *
			try {
				while(buffer.hasRemaining()) {
					incoming.put((char)buffer.get());
				}
			} catch (InterruptedException e) {
				// TODO: Do something
			}
			* /
			//System.out.println("4: " + CamelUtils.nanoTime());
			try {
				future.get();
			} catch (InterruptedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			total += (end.get() - start);
		}

		System.out.println("TOTAL TIME: " + total);
		//try { Thread.sleep(2000); } catch (Exception e) {}
		***/

		//EventBuilder builder = new EventBuilder();
		AtomicReference<SyslogFacility> facility = new AtomicReference<>();

		ParserFactory factory = new ParserFactory();
		factory
			.intBetweenDelimiters('<', '>', v -> { facility.set(SyslogFacility.getFacilityForCode(v)); })
			.stringBetweenDelimiters('a', 'b', v -> { factory.getBuilder().setHost(v); })
			.character('b')
			.character('b')
			.whitespace();

		CompletableFuture<Event> event = null;
		for (int i = 0; i < 100; i++) {
			//incoming.rewind();
			event = factory.parse(incoming.asReadOnlyBuffer(), m_executor);
			event.whenComplete((e, ex) -> {
				if (ex == null) {
					//System.out.println(e.toString());
				} else {
					ex.printStackTrace();
				}
			});
		}
		try {
			event.get();
		} catch (InterruptedException | ExecutionException e) {
			e.printStackTrace();
		}
	}

	/*
	public interface Parser {
		CompletableFuture<Event> parse(ByteBuffer incoming);
	}
	*/

	public class ParserFactory {
		final List<Stage> m_stages = new ArrayList<>();
		//final ExecutorService m_executor;
		ThreadLocal<EventBuilder> m_builder = new ThreadLocal<EventBuilder>();

		public ParserFactory() {
			//m_executor = executor;
		}

		public ParserFactory whitespace() {
			m_stages.add(new MatchWhitespace());
			return this;
		}

		public ParserFactory character(char character) {
			m_stages.add(new MatchChar(character));
			return this;
		}

		public ParserFactory stringBetweenDelimiters(char start, char end, Consumer<String> consumer) {
			m_stages.add(new MatchStringBetweenDelimiters(consumer, start, end));
			return this;
		}

		public ParserFactory intBetweenDelimiters(char start, char end, Consumer<Integer> consumer) {
			m_stages.add(new MatchIntegerBetweenDelimiters(consumer, start, end));
			return this;
		}

		public EventBuilder getBuilder() {
			if (m_builder.get() == null) {
				m_builder.set(new EventBuilder());
			}
			return m_builder.get();
		}

		public CompletableFuture<Event> parse(ByteBuffer incoming, ExecutorService executor) {

			// Build a CompletableFuture chain out of the Stages
			boolean first = true;
			CompletableFuture<Void> future = null;

			for (Stage stage : m_stages) {
				// Reset the stage's state
				stage.reset();
				// Give the stage a reference to the buffer
				stage.setBuffer(incoming);
				if (first) {
					future = CompletableFuture.runAsync(stage, executor);
					first = false;
				} else {
					future = future.thenRun(stage);
				}
			}

			future.exceptionally(e -> { /* DO SOMETHING */ return null; });

			//futureRef.set(future);

			return future.thenCompose(v -> {
				return CompletableFuture.completedFuture(m_builder.get().getEvent());
			});
		}
	}

	//public final Parser syslogNgParser = new ParserFactory().matchIntegerBetweenDelimiters('<', '>', Event::setPriority);

	/*
	public final Parser syslogNgParser = new ParserFactory(byteBuffer)
		.intBetweenDelimiters('<', '>', event::setPriority)
	;
	*/

	public interface Stage extends Runnable {
		/**
		 * Accept the next character in the stream
		 */
		//void run();
		/**
		 * Cancel this parser
		 */
		//void cancel();
		/**
		 * Complete this parser stage and pass the
		 * character to the next stage
		 * @param lastChar
		 */
		//void complete(char lastChar);

		void reset();
		void setBuffer(ByteBuffer buffer);
	}

	public static abstract class AbstractStage<R> implements Runnable, Stage {

		private ByteBuffer m_incoming;

		private Consumer<R> m_resultConsumer;

		private StringBuffer m_accumulatedValue = new StringBuffer();

		AtomicInteger m_accumulatedSize = new AtomicInteger(0);

		protected AbstractStage() {
			this(null);
		}

		protected AbstractStage(Consumer<R> resultConsumer) {
			m_resultConsumer = resultConsumer;
		}

		public enum AcceptResult {
			CONTINUE,
			COMPLETE_AFTER_CONSUMING,
			COMPLETE_WITHOUT_CONSUMING,
			CANCEL
		}

		public void reset() {
			m_accumulatedSize.set(0);
			m_accumulatedValue = new StringBuffer();
		}

		public void setBuffer(ByteBuffer incoming) {
			m_incoming = incoming;
		}

		public abstract AcceptResult acceptChar(char c);

		public final void run() {
			while(true) {
				try {
					m_incoming.mark();
					char c = (char)m_incoming.get();
					//System.err.println(c);
					switch (acceptChar(c)) {
						case CONTINUE:
							continue;
						case COMPLETE_AFTER_CONSUMING:
							if (m_resultConsumer != null) {
								m_resultConsumer.accept(getValue());
							}
							return;
						case COMPLETE_WITHOUT_CONSUMING:
							// Put the char back on the deque
							//m_incoming.putFirst(c);
							m_incoming.reset();
							if (m_resultConsumer != null) {
								m_resultConsumer.accept(getValue());
							}
							return;
						case CANCEL:
							throw new CancellationException(getClass().getSimpleName() + " match failed");
					}
					/*
				} catch (InterruptedException e) {
					CancellationException ex = new CancellationException("Unexpected InterruptedException while parsing: " + e.getMessage());
					ex.initCause(e);
					throw ex;
					*/
				} finally {}
			}
		}

		protected void accumulate(char c) {
			m_accumulatedValue.append(c);
			m_accumulatedSize.incrementAndGet();
		}

		protected int accumulatedSize() {
			return m_accumulatedSize.get();
		}

		protected String accumulatedValue() {
			return m_accumulatedValue.toString();
		}

		protected R getValue() {
			return null;
		}
	}

	/**
	 * Match any whitespace character.
	 */
	public static class MatchWhitespace extends AbstractStage<Void> {
		public AcceptResult acceptChar(char c) {
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

		public AcceptResult acceptChar(char c) {
			if (c == m_char) {
				return AcceptResult.COMPLETE_AFTER_CONSUMING;
			} else {
				return AcceptResult.CANCEL;
			}
		}
	}

	/**
	 * Match any single character.
	 */
	public static class MatchAny extends AbstractStage<String> {
		public AcceptResult acceptChar(char c) {
			accumulate(c);
			return AcceptResult.COMPLETE_AFTER_CONSUMING;
		}
		
		public String getValue() {
			return accumulatedValue();
		}
	}

	/**
	 * Match a string value between delimiters.
	 */
	public static abstract class MatchBetweenDelimiters<R> extends AbstractStage<R> {
		private final char m_startDelimiter;
		private final char m_endDelimiter;
		boolean foundStart = false;

		MatchBetweenDelimiters(Consumer<R> consumer, char startDelimiter, char endDelimiter) {
			super(consumer);
			m_startDelimiter = startDelimiter;
			m_endDelimiter = endDelimiter;
		}

		@Override
		public AcceptResult acceptChar(char c) {
			if (foundStart) {
				if (c == m_endDelimiter) {
					return AcceptResult.COMPLETE_AFTER_CONSUMING;
				} else {
					accumulate(c);
					return AcceptResult.CONTINUE;
				}
			} else {
				if (c == m_startDelimiter) {
					foundStart = true;
					return AcceptResult.CONTINUE;
				} else {
					return AcceptResult.CANCEL;
				}
			}
		}

		@Override
		public void reset() {
			super.reset();
			foundStart = false;
		}
	}

	public static class MatchStringBetweenDelimiters extends MatchBetweenDelimiters<String> {
		public MatchStringBetweenDelimiters(Consumer<String> consumer, char startDelimiter, char endDelimiter) {
			super(consumer, startDelimiter, endDelimiter);
		}

		@Override
		public String getValue() {
			return accumulatedValue();
		}
	}

	public static class MatchIntegerBetweenDelimiters extends MatchBetweenDelimiters<Integer> {
		public MatchIntegerBetweenDelimiters(Consumer<Integer> consumer, char startDelimiter, char endDelimiter) {
			super(consumer, startDelimiter, endDelimiter);
		}

		@Override
		public Integer getValue() {
			return Integer.valueOf(accumulatedValue());
		}
	}

	/**
	 * Match an integer.
	 */
	public static class MatchInteger extends AbstractStage<Integer> {
		MatchInteger(Consumer<Integer> consumer) {
			super(consumer);
		}

		@Override
		public AcceptResult acceptChar(char c) {
			if (c >= '0' && c <= '9') {
				accumulate(c);
				return AcceptResult.CONTINUE;
			} else {
				// If any characters were accumulated, complete
				if (accumulatedSize() > 0) {
					return AcceptResult.COMPLETE_WITHOUT_CONSUMING;
				} else {
					return AcceptResult.CANCEL;
				}
			}
		}

		@Override
		public Integer getValue() {
			return Integer.parseInt(accumulatedValue());
		}
	}
}

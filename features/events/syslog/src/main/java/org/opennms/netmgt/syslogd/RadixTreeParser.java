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
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.stream.Collectors;

import org.opennms.core.collections.RadixTree;
import org.opennms.core.collections.RadixTreeImpl;
import org.opennms.core.collections.RadixTreeNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>This class uses a {@link RadixTree} to store a tree of parser stages.
 * Each leaf node of the tree represents a complete parse of an incoming
 * message. To parse incoming {@link ByteBuffer} messages, the {@link RadixTree}
 * is traversed and each stage is added as a future processing step. When a leaf node 
 * is reached and returns a non-null result, ie. a complete parse, that result is 
 * returned as the parse result.</p>
 * 
 * <p>You can teach the parser new {@link ParserStage} sequences by using the 
 * {@link #teach(ParserStage[])} method. This will add the stages to the {@link RadixTree}.</p>
 * 
 * <p>TODO: Make the parser tree construction phased or asynchronous so that tree 
 * nodes are added as-needed to complete the parse. In many cases, the first
 * several branches of the tree will result in a complete parse, making it unnecessary
 * to add all possible branches to the execution tree.</p>
 * 
 * <p>TODO: Allow cancellation of in-progress branches when the first non-null result
 * is obtained.</p>
 * 
 * <p>TODO: Score and sort parse branches based on successful parses. If a particular parse
 * branch is successful, we should have a way to increment a score on the stages in
 * that branch so that we can periodically sort the tree based on these scores. This 
 * will put heavily utilized parse branches near the front of the tree, making it faster
 * to traverse the branches to a successful parse.</p>
 * 
 * @author Seth
 */
public class RadixTreeParser implements ByteBufferParser<SyslogMessage> {

	private final static Logger LOG = LoggerFactory.getLogger(RadixTreeParser.class);

	final RadixTree<ParserStage> tree = new RadixTreeImpl<>();

//	private static final ThreadPoolExecutor m_executor = new ThreadPoolExecutor(
//		1,
//		1,
//		0L, TimeUnit.MILLISECONDS,
//		new SynchronousQueue<>(true),
//		new ThreadFactory() {
//			final AtomicInteger index = new AtomicInteger();
//			@Override
//			public Thread newThread(Runnable r) {
//				return new Thread(r, RadixTreeParser.class.getSimpleName() + "-Thread-" + String.valueOf(index.incrementAndGet()));
//			}
//		},
//		// Throttle incoming tasks by running them on the caller thread
//		new ThreadPoolExecutor.CallerRunsPolicy()
//	);

//	private static final ExecutorService m_executor = Executors.newSingleThreadExecutor();

//	private final ExecutorService m_executor = new ExecutorFactoryCassandraSEPImpl().newExecutor("StagedParser", "StageExecutor");

//	private final ExecutorService m_executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() * 2);

	/**
	 * @return The number of nodes in the underlying radix tree.
	 */
	public int size() {
		return tree.size();
	}

	/**
	 * Teach a new {@link ParserStage} sequence to this parser.
	 * 
	 * @param stages
	 */
	public void teach(ParserStage[] stages) {
		tree.addChildren(stages);
	}

	public void performEdgeCompression() {
		for (RadixTreeNode<ParserStage> child : tree.getChildren()) {
			compressNode(child);
		}
	}

	public static void compressNode(RadixTreeNode<ParserStage> node) {
		if (node.getChildren() != null) {
			switch(node.getChildren().size()) {
			case(0):
				return;
			case(1):
				RadixTreeNode<ParserStage> child = node.getChildren().iterator().next();
				if (node.getContent() instanceof CompositeParserStage) {
					((CompositeParserStage)node.getContent()).members.add(child.getContent());
				} else {
					CompositeParserStage stage = new CompositeParserStage();
					stage.members.add(node.getContent());
					stage.members.add(node.getChildren().iterator().next().getContent());
					node.setContent(stage);
				}
				// Link the child's children to this node
				node.setChildren(child.getChildren());
				// Recompress the node
				compressNode(node);
				break;
			default:
				for (RadixTreeNode<ParserStage> current : node.getChildren()) {
					compressNode(current);
				}
				break;
			}
		}
	}

	private static class CompositeParserStage implements ParserStage {

		public final List<ParserStage> members = new ArrayList<>();

		@Override
		public ParserState apply(ParserState state) {
			ParserState currentState = state;
			for (ParserStage member : members) {
				currentState = member.apply(currentState);
			}
			return currentState;
		}

		@Override
		public void setOptional(boolean optional) {
			throw new UnsupportedOperationException();
		}

		@Override
		public void setTerminal(boolean terminal) {
			throw new UnsupportedOperationException();
		}

		@Override
		public String toString() {
			final StringBuilder buffer = new StringBuilder();
			buffer.append("[");
			buffer.append(members.stream().map(ParserStage::toString).collect(Collectors.joining(", ")));
			buffer.append("]");
			return buffer.toString();
		}
	}

	@Override
	public CompletableFuture<SyslogMessage> parse(ByteBuffer incoming) {
		ParserState state = new ParserState(incoming);

		// TODO: Use better collection than ArrayList?
		// We have to make sure that this list maintains the insertion order
		// so that the first future that completes is definitely returned by
		// {@link RadixTreeParser#firstNonNullResult()}
		final List<CompletableFuture<ParserState>> finishedFutures = new ArrayList<>();

		// Top of future tree is parser state
		final CompletableFuture<ParserState> parent = CompletableFuture.completedFuture(state);

		// Recursively construct the parser tree
		addStageFutures(finishedFutures, parent, tree);

		return firstNonNullResult(finishedFutures).thenApply(s -> {
			if (s == null) {
				return null;
			} else {
				return s.message;
			}
		});
	}

	/**
	 * TODO: Use a visitor pattern instead?
	 */
	private static void addStageFutures(List<CompletableFuture<ParserState>> finishedFutures, CompletableFuture<ParserState> parent, RadixTreeNode<ParserStage> node) {
		final CompletableFuture<ParserState> current;

		// If we're at the root of the radix tree (where the content is null), 
		// use the parent future as the current future
		if (node.getContent() == null) {
			current = parent;
		} else {
			// Otherwise, apply the current node's stage to the parent
			current = parent.thenApply(s -> { return node.getContent().apply(s); });
		}

		// If the node has children, then recursively process the children. Do not
		// add the current node to the finished futures list: we're only concerned
		// with seeing if the leaf nodes complete, not the intermediate stages
		if (node.getChildren() != null && node.getChildren().size() > 0) {
			for (RadixTreeNode<ParserStage> child : node.getChildren()) {
				addStageFutures(finishedFutures, current, child);
			}
		} else {
			// If the node is a leaf node, then add it to the list of futures
			// that need to be evaluated as part of the parse tree
			finishedFutures.add(current);
		}
	}

	private static <T> CompletableFuture<T> firstNonNullResult(List<? extends CompletionStage<T>> futures) {

		CompletableFuture<T> parent = new CompletableFuture<>();

		CompletableFuture.allOf(
			futures.stream().map(
				s -> s.thenAccept(t -> {
//					System.out.println("VALUE " + t);
					// After each stage completes, if its result is non-null
					// then complete the parent future
					if (t != null) {
						if (!parent.complete(t)) {
							LOG.trace("More than one future completed with a non-null result");
						} else {
							LOG.trace("Non-null result returned");
						}
					}
				})
			).toArray(
				CompletableFuture<?>[]::new
			)
		).exceptionally(ex -> {
			// If all futures complete exceptionally, mark this future as exceptional as well
//			parent.completeExceptionally(ex);
			parent.complete(null);
			return null;
		}).thenAccept(v -> {
			// If the parent isn't complete yet, then all children returned null so just
			// complete with null
			if (parent.complete(null)) {
				LOG.debug("All futures completed with a null result");
			}
		});

		return parent;
	}
}

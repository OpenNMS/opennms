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

import org.opennms.core.collections.RadixTree;
import org.opennms.core.collections.RadixTreeImpl;
import org.opennms.core.collections.RadixTreeNode;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.model.events.EventBuilder;
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
public class RadixTreeParser implements ByteBufferParser<EventBuilder> {

	final static Logger LOG = LoggerFactory.getLogger(RadixTreeParser.class);

	RadixTree<ParserStage> tree = new RadixTreeImpl<>();

	/**
	 * Teach a new {@link ParserStage} sequence to this parser.
	 * 
	 * @param stages
	 */
	public void teach(ParserStage[] stages) {
		tree.addChildren(stages);
	}

	@Override
	public CompletableFuture<EventBuilder> parse(ByteBuffer incoming) {
		ParserState state = new ParserState(incoming, new EventBuilder());

// TODO
//		state.builder.setDistPoller(systemId);
		state.builder.setLogDest("logndisplay");
		state.builder.setSource(Syslogd.LOG4J_CATEGORY);
		// Set event host
		state.builder.setHost(InetAddressUtils.getLocalHostName());


		// TODO: Use better collection than ArrayList?
		final List<CompletableFuture<ParserState>> finishedFutures = new ArrayList<>();

		// Top of future tree is parser state
		final CompletableFuture<ParserState> parent = CompletableFuture.completedFuture(state);

		// Recursively construct the parser tree
		addStageFutures(finishedFutures, parent, tree);

		return firstNonNullResult(finishedFutures).thenApply(s -> {
			if (s == null) {
				return null;
			} else {
				return s.builder;
			}
		});
	}

	/**
	 * TODO: Use a visitor pattern instead?
	 * 
	 * TODO: Coalesce multiple stages into a single thenApply() call when
	 * node.getChildren().size() == 1. Then figure out how to do that recursively.
	 */
	private static void addStageFutures(List<CompletableFuture<ParserState>> finishedFutures, CompletableFuture<ParserState> parent, RadixTreeNode<ParserStage> node) {
		CompletableFuture<ParserState> current = null;

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

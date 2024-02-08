/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.opennms.core.collections;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * This class implements the {@link RadixTreeNode} interface by storing an
 * instance of the content and a {@link Collection} of child nodes.
 *
 * @author Seth
 * 
 * @param <T> The content type of each node
 */
public class RadixTreeNodeImpl<T> implements RadixTreeNode<T> {

	private static final String TO_STRING_INDENT = "  ";

	private T content;
	private final Set<RadixTreeNode<T>> children = new LinkedHashSet<>();

	/**
	 * TODO: Is this going to be used?
	 */
	public RadixTreeNodeImpl(T content, Collection<RadixTreeNode<T>> children) {
		this.content = content;
		this.children.addAll(children);
	}

	public RadixTreeNodeImpl(T[] chain) {
		this.content = chain[0];
		// Recursively add the rest of the chain as children to this node
		if (chain.length > 1) {
			this.children.add(new RadixTreeNodeImpl<T>(Arrays.copyOfRange(chain, 1, chain.length)));
		}
	}

	@Override
	public T getContent() {
		return content;
	}

	@Override
	public void setContent(T newContent) {
		content = newContent;
	}

	@Override
	public void addChildren(T[] chain) {
		final T head = chain[0];
		for (final RadixTreeNode<T> node : children) {
			// If the head of the chain matches the content of any child nodes...
			if (head.equals(node.getContent())) {
				if (chain.length == 1) {
					// TODO: If there are no more children, we need to mark that this
					// stage is a valid termination somehow, maybe an isTerminal()
					// boolean would work
				} else {
					// Then append the subsequent members of the chain to the node
					node.addChildren(Arrays.copyOfRange(chain, 1, chain.length));
					return;
				}
			}
		}
		// If the object doesn't exist in the root element yet, add a new tree for it
		RadixTreeNode<T> newNode = new RadixTreeNodeImpl<>(chain);
		children.add(newNode);
	}

	@Override
	public Set<RadixTreeNode<T>> getChildren() {
		return children;
	}

	@Override
	public void setChildren(Set<RadixTreeNode<T>> newChildren) {
		children.clear();
		children.addAll(newChildren);
	}

	@Override
	public int size() {
		// Sum up the size of the children
		int retval = getChildren().stream().collect(Collectors.summingInt(RadixTreeNode::size));
		// And add 1 if our node has content
		return content == null ? retval : ++retval;
	}

	@Override
	public String toStringWithPrefix(String prefix) {
		final StringBuilder value = new StringBuilder();
		value.append(content == null ? "" : content.toString()).append("\n");
		for (RadixTreeNode<T> child : children) {
			value.append(prefix).append(child.toStringWithPrefix(prefix + TO_STRING_INDENT));
		}
		return value.toString();
	}

	@Override
	public String toString() {
		return toStringWithPrefix("");
	}
}

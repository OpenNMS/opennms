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
package org.opennms.features.vaadin.jmxconfiggenerator.data;

import com.google.common.base.Objects;

import java.util.Map.Entry;

/**
 * Straight forward implementation of {@link java.util.Map.Entry}.<br/>
 * <b>Note:</b> Does not support null keys.
 * 
 * @author Markus von Rüden
 */
public class SimpleEntry<T> implements Entry<T, T> {

	/**
	 * read only key
	 */
	final private T key;
	private T value;

	public SimpleEntry(T key) {
		this(key, null);
	}

	public SimpleEntry(T key, T value) {
		if (key == null) throw new IllegalArgumentException(getClass().getSimpleName() + " cannot handle null keys");
		this.key = key;
		this.value = value;
	}

	@Override
	public String toString() {
		return key + "=" + value;
	}

	@Override
	public T getKey() {
		return key;
	}

	@Override
	public T getValue() {
		return value;
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(key, value);
	}

	/**
	 * Returns the result of
	 * <code>this.key.equals(that.key) && this.value.equals(that.value)</code>.
	 * 
	 * @param obj
	 * @return true if both keys and values are equal, false otherwise. False is
	 *         also returned if <codE>obj</code> does not inherit/implement
	 *         <code>Entry</code> does not match or <code>obj</code> is null.
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj == null) return false;
		if (!(obj instanceof Entry)) return false;
		return Objects.equal(this.key, ((Entry) obj).getKey()) && Objects.equal(this.value, ((Entry) obj).getValue());
	}

	@Override
	public T setValue(T value) {
		T old = this.value;
		this.value = value;
		return old;
	}
}

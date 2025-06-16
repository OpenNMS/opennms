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
package org.opennms.netmgt.model;

import junit.framework.TestCase;


public class OnmsIpInterfaceTest extends TestCase {

	public void testCollectionTypeGetNull () {
		PrimaryType collectionType = PrimaryType.get(null);
		
		assertSame("The expected value is N for a null", PrimaryType.NOT_ELIGIBLE, collectionType);
		
	}

	public void testCollectionTypeGetSpaces () {
		PrimaryType collectionType = PrimaryType.get("   ");
		
		assertSame("The expected valus is N for all spaces", PrimaryType.NOT_ELIGIBLE, collectionType);
	}
	
	public void testCollectionTypeGetTwoChars () {
		
		try {
			@SuppressWarnings("unused")
			PrimaryType collectionType = PrimaryType.get(" MN  ");
			fail("Expected to catch an exception here");
		} catch (IllegalArgumentException e) {
		}
	}

	public void testCollectionTypeGetZ () {
		
		try {
			@SuppressWarnings("unused")
			PrimaryType collectionType = PrimaryType.get("Z");
			fail("Expected to catch an exception here");
		} catch (IllegalArgumentException e) {
		}
	}
	
	public void testCollectionTypeComparison () {
		PrimaryType left = PrimaryType.NOT_ELIGIBLE;
		PrimaryType right = null;
		try {
			left.isLessThan(right);
			fail("Expected to catch an exception here");
		} catch (NullPointerException e) {
		}
	}
}

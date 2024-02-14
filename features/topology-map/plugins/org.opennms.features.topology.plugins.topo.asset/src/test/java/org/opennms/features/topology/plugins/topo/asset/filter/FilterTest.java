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
package org.opennms.features.topology.plugins.topo.asset.filter;

import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

public class FilterTest {

	@Test
	public void verifyNot() {
		Filter<String> filter = new NotFilter<>(new EqFilter<>("Stuttgart"));
		Assert.assertEquals(false, filter.apply("Stuttgart"));
		Assert.assertEquals(true, filter.apply("Fulda"));
	}

	@Test
	public void verifyRegExp() {
		Filter<String> filter = new RegExFilter<>(".*gar(t|d)");
		Assert.assertEquals(false, filter.apply("Fulda"));
		Assert.assertEquals(true, filter.apply("Stuttgart"));
		Assert.assertEquals(true, filter.apply("Isengard"));
	}
	
	@Test
	public void verifyRegExpCsv() {
		Filter<String> filter = new RegExCsvFilter<>(".*gar(t|d)");
		Assert.assertEquals(false, filter.apply("Fulda"));
		Assert.assertEquals(true, filter.apply("Stuttgart"));
		Assert.assertEquals(true, filter.apply("Isengard"));
		
		Assert.assertEquals(false, filter.apply(""));
		Assert.assertEquals(false, filter.apply(","));
		Assert.assertEquals(false, filter.apply("France,Germany,Fulda"));
		Assert.assertEquals(true, filter.apply("France,Germany,Stuttgart,Fulda"));
		Assert.assertEquals(true, filter.apply("France,Isengard,Germany,Fulda"));
	}

	@Test
	public void verifyEq() {
		Filter<String> filter = new EqFilter<>("Stuttgart");
		Assert.assertEquals(true, filter.apply("Stuttgart"));
		Assert.assertEquals(false, filter.apply("Fulda"));
	}
	
	@Test
	public void verifyCsvEq() {
		Filter<String> filter = new EqCsvFilter<>("Stuttgart");
		Assert.assertEquals(true, filter.apply("Stuttgart"));
		Assert.assertEquals(false, filter.apply("Fulda"));
		
		Assert.assertEquals(false, filter.apply(""));
		Assert.assertEquals(false, filter.apply(","));
		Assert.assertEquals(false, filter.apply("France,Germany,Fulda"));
		Assert.assertEquals(true, filter.apply("France,Germany,Stuttgart,Fulda"));
	}

	@Test
	public void verifyAnd() {
		Filter<String> filter = new AndFilter<>(new EqFilter<>("Stuttgart"), new RegExFilter<>(".*gar(t|d)"));
		Assert.assertEquals(true, filter.apply("Stuttgart"));
		Assert.assertEquals(false, filter.apply("Isengard"));
		Assert.assertEquals(false, filter.apply("Fulda"));
	}

	@Test
	public void verifyMultiAnd() {
		List<Filter<String>> filters=Arrays.asList(
				new EqFilter<>("Stuttgart"),
				new RegExFilter<>(".*gar(t|d)")
				);

		Filter<String> filter = new AndFilter<String>(filters);
		Assert.assertEquals(true, filter.apply("Stuttgart"));
		Assert.assertEquals(false, filter.apply("Isengard"));
		Assert.assertEquals(false, filter.apply("Fulda"));
		Assert.assertEquals(false, filter.apply("Southampton"));
	}

	@Test
	public void verifyOr() {
		Filter<String> filter = new OrFilter<>(new EqFilter<>("Stuttgart"), new EqFilter<>("Fulda"));
		Assert.assertEquals(true, filter.apply("Stuttgart"));
		Assert.assertEquals(true, filter.apply("Fulda"));
		Assert.assertEquals(false, filter.apply("Isengard"));

		filter = new OrFilter<>(new EqFilter<>("Stuttgart"), new RegExFilter<>(".*gar(t|d)"));
		Assert.assertEquals(true, filter.apply("Stuttgart"));
		Assert.assertEquals(true, filter.apply("Isengard"));
	}
	
	@Test
	public void verifyMultiOr() {
		List<Filter<String>> filters=Arrays.asList(
				new EqFilter<>("Stuttgart"),
				new RegExFilter<>(".*gar(t|d)"),
				new EqFilter<>("Southampton")
				);
		Filter<String> filter = new OrFilter<String>(filters);
		Assert.assertEquals(true, filter.apply("Stuttgart"));
		Assert.assertEquals(true, filter.apply("Isengard"));
		Assert.assertEquals(true, filter.apply("Southampton"));
		Assert.assertEquals(false, filter.apply("Fulda"));
	}
}

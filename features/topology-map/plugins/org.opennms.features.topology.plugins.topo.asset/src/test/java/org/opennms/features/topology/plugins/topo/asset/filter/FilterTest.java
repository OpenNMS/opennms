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

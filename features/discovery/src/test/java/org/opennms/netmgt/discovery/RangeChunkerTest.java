/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2015-2016 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2016 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.discovery;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

import java.net.InetAddress;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

import org.junit.Before;
import org.junit.Test;
import org.opennms.core.network.IPAddress;
import org.opennms.netmgt.config.discovery.DiscoveryConfiguration;
import org.opennms.netmgt.config.discovery.ExcludeRange;
import org.opennms.netmgt.config.discovery.IncludeRange;
import org.opennms.netmgt.config.discovery.IncludeUrl;
import org.opennms.netmgt.config.discovery.Specific;
import org.opennms.netmgt.model.discovery.IPPollRange;

public class RangeChunkerTest {

    private IpAddressFilter ipAddressFilter;

    @Before
    public void setUp() {
        ipAddressFilter = mock(IpAddressFilter.class);
        when(ipAddressFilter.matches(any(), any(InetAddress.class))).thenReturn(true);
    }

	@Test
	public void testRangeChunker() throws Exception {
		DiscoveryConfiguration config = new DiscoveryConfiguration();

		IncludeRange range = new IncludeRange();
		range.setBegin("10.1.0.1");
		range.setEnd("10.1.4.254");
		range.setForeignSource("Routers");
		range.setLocation("Raleigh");
		range.setRetries(2);
		range.setTimeout(500l);

		IncludeUrl url = new IncludeUrl();
		url.setUrl("file:src/test/resources/included_ip_addresses");
		url.setForeignSource("Switches");
		url.setLocation("Durham");
		url.setRetries(1);
		url.setTimeout(5000l);

		ExcludeRange excludes = new ExcludeRange();
		excludes.setBegin("10.1.3.0");
		excludes.setEnd("10.1.5.0");

		ExcludeRange excludes2 = new ExcludeRange();
		excludes2.setBegin("10.1.1.100");
		excludes2.setEnd("10.1.1.150");

		Specific specific = new Specific();
		specific.setAddress("10.1.3.5");
		specific.setForeignSource("Gateway");
		specific.setLocation("Co-lo");
		specific.setRetries(5);
		specific.setTimeout(1000l);

		Specific specific2 = new Specific();
		specific2.setAddress("10.1.2.5");
		specific2.setForeignSource("Routers");
		specific2.setLocation("Raleigh");
		specific2.setRetries(5);
		specific2.setTimeout(1000l);

		config.addExcludeRange(excludes);
		config.addExcludeRange(excludes2);
		config.addIncludeRange(range);
		config.addIncludeUrl(url);
		config.addSpecific(specific);
		config.addSpecific(specific2);
		
		config.setChunkSize(100);
		config.setForeignSource("Datacenter");
		config.setLocation("RDU");

		printJobs(new RangeChunker(ipAddressFilter).chunk(config));
	}

	@Test
	public void testCoalesceConsecutiveSpecifics() {
		DiscoveryConfiguration config = new DiscoveryConfiguration();

		for (int i = 0; i < 5; i++) {
			Specific specific = new Specific();
			specific.setAddress("10.0.0." + i);
			specific.setForeignSource("ABC");
			specific.setLocation("123");
			specific.setRetries(1);
			specific.setTimeout(1000l);
			config.addSpecific(specific);
		}

		Map<String, List<DiscoveryJob>> jobs = new RangeChunker(ipAddressFilter).chunk(config);
		printJobs(jobs);

		// The specifics have been combined into one range
		assertEquals(1, jobs.size());
		assertEquals(1, jobs.get("123").get(0).getRanges().size());

		IPPollRange range = jobs.get("123").get(0).getRanges().iterator().next();
		assertEquals("10.0.0.0", new IPAddress(range.getAddressRange().getBegin()).toString());
		assertEquals("10.0.0.4", new IPAddress(range.getAddressRange().getEnd()).toString());
	}

	@Test
	public void testConsecutiveSpecificsWithDifferentForeignSources() {
		DiscoveryConfiguration config = new DiscoveryConfiguration();

		for (int i = 0; i < 5; i++) {
			Specific specific = new Specific();
			specific.setAddress("10.0.0." + i);
			specific.setForeignSource(i % 2 == 0 ? "ABC" : "DEF");
			specific.setLocation("123");
			specific.setRetries(1);
			specific.setTimeout(1000l);
			config.addSpecific(specific);
		}

		Map<String, List<DiscoveryJob>> jobs = new RangeChunker(ipAddressFilter).chunk(config);
		printJobs(jobs);

		assertEquals(2, jobs.get("123").size());
		assertEquals(3, jobs.get("123").get(0).getRanges().size());
		assertEquals(2, jobs.get("123").get(1).getRanges().size());
	}

	@Test
	public void testConsecutiveSpecificsWithDifferentLocations() {
		DiscoveryConfiguration config = new DiscoveryConfiguration();

		for (int i = 0; i < 5; i++) {
			Specific specific = new Specific();
			specific.setAddress("10.0.0." + i);
			specific.setForeignSource("ABC");
			specific.setLocation(i % 2 == 0 ? "123" : "456");
			specific.setRetries(1);
			specific.setTimeout(1000l);
			config.addSpecific(specific);
		}

		Map<String, List<DiscoveryJob>> jobs = new RangeChunker(ipAddressFilter).chunk(config);
		printJobs(jobs);

		assertEquals(2, jobs.size());
		assertEquals(3, jobs.get("123").get(0).getRanges().size());
		assertEquals(2, jobs.get("456").get(0).getRanges().size());
	}

	@Test
	public void testConsecutiveSpecificsWithDifferentTimeouts() {
		DiscoveryConfiguration config = new DiscoveryConfiguration();

		for (long i = 0; i < 5; i++) {
			Specific specific = new Specific();
			specific.setAddress("10.0.0." + i);
			specific.setForeignSource("ABC");
			specific.setLocation("123");
			specific.setRetries(5);
			specific.setTimeout(i+1);
			config.addSpecific(specific);
		}

		Map<String, List<DiscoveryJob>> jobs = new RangeChunker(ipAddressFilter).chunk(config);
		printJobs(jobs);

		assertEquals(1, jobs.get("123").size());
		assertEquals(5, jobs.get("123").get(0).getRanges().size());
	}

	@Test
	public void testConsecutiveSpecificsWithDifferentRetries() {
		DiscoveryConfiguration config = new DiscoveryConfiguration();

		for (int i = 0; i < 5; i++) {
			Specific specific = new Specific();
			specific.setAddress("10.0.0." + i);
			specific.setForeignSource("ABC");
			specific.setLocation("123");
			specific.setRetries(i);
			specific.setTimeout(1000l);
			config.addSpecific(specific);
		}

		Map<String, List<DiscoveryJob>> jobs = new RangeChunker(ipAddressFilter).chunk(config);
		printJobs(jobs);

		assertEquals(1, jobs.get("123").size());
		assertEquals(5, jobs.get("123").get(0).getRanges().size());
	}

	private static void printJobs(Map<String, List<DiscoveryJob>> jobs) {
	    jobs.entrySet().stream()
	        .forEach(j -> {
	            System.out.println("Location: " + j.getKey());
	            printJobs(j.getValue());
	        });
	}

	/**
	 * Pretty-print DiscoveryJobs.
	 * 
	 * @param jobs
	 */
	private static void printJobs(List<DiscoveryJob> jobs) {
		jobs.stream().forEach(job -> {
			final StringBuilder buffer = new StringBuilder();
			final AtomicInteger depth = new AtomicInteger(0);
			job.toString().chars().forEach(c -> {
				switch(c) {
					case('{'):
						depth.incrementAndGet();
						buffer.append("{\n");
						IntStream.range(0,  depth.get()).forEach(i -> {buffer.append("\t");}); 
						break;
					case('}'):
						depth.decrementAndGet();
						buffer.append("\n");
						IntStream.range(0,  depth.get()).forEach(i -> {buffer.append("\t");}); 
						buffer.append("}");
						break;
					case('['):
						depth.incrementAndGet();
						buffer.append("[\n");
						IntStream.range(0,  depth.get()).forEach(i -> {buffer.append("\t");}); 
						break;
					case(']'):
						depth.decrementAndGet();
						buffer.append("\n");
						IntStream.range(0,  depth.get()).forEach(i -> {buffer.append("\t");}); 
						buffer.append("]");
						break;
					/*
					case(','):
						buffer.append(",\n");
						IntStream.range(0,  depth.get()).forEach(i -> {buffer.append("\t");}); 
						break;
					*/
					default:
						buffer.append((char)c);
				}
			});

			System.out.println(buffer.toString());
		});
	}
}

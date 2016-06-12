/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2016 The OpenNMS Group, Inc.
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

package org.opennms.web.rest.v1;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

import org.springframework.stereotype.Component;
import org.xbill.DNS.AAAARecord;
import org.xbill.DNS.ARecord;
import org.xbill.DNS.ExtendedResolver;
import org.xbill.DNS.Lookup;
import org.xbill.DNS.MXRecord;
import org.xbill.DNS.Record;
import org.xbill.DNS.Resolver;
import org.xbill.DNS.SRVRecord;
import org.xbill.DNS.TXTRecord;
import org.xbill.DNS.TextParseException;
import org.xbill.DNS.Type;

/**
 * ReST service for (JAXB) ConfigurationResource files.
 */

@Component("dnsLookupRestService")
@Path("dnsLookup")
public class DnsLookupRestService extends OnmsRestService {
	
	@GET
	@Path("{resolver}/{recordType}/{recordName}")
	public List<Map<Object, Object>> dnsLookup(@PathParam("resolver") final String resolver, @PathParam("recordType") final String recordType, @PathParam("recordName") final String recordName) throws UnknownHostException, TextParseException {
		return doDnsLookup(resolver, recordType, recordName);
	}
	
	@GET
	@Path("{recordType}/{recordName}")
	public List<Map<Object, Object>> dnsLookup(@PathParam("recordType") final String recordType, @PathParam("recordName") final String recordName) throws TextParseException, UnknownHostException {
		return doDnsLookup(recordType, recordName);
	}
	
	private List<Map<Object, Object>> doDnsLookup(final String type, final String name) throws TextParseException, UnknownHostException {
		return doDnsLookup(null, type, name);
	}
	
	private List<Map<Object,Object>> doDnsLookup(final String name) throws TextParseException, UnknownHostException {
		return doDnsLookup(null, null, name);
	}
	
	private List<Map<Object,Object>> doDnsLookup(final String resolver, final String type, final String name) throws TextParseException, UnknownHostException {
		Lookup lookup = new Lookup(name, makeType(type));
		lookup.setResolver(makeResolver(resolver));
		return recordsToMapList(lookup.run());
	}
	
	private int makeType(final String recordType) {
		if ("A".equalsIgnoreCase(recordType)) return Type.A;
		if ("AAAA".equalsIgnoreCase(recordType)) return Type.AAAA;
		if ("MX".equalsIgnoreCase(recordType)) return Type.MX;
		if ("SRV".equalsIgnoreCase(recordType)) return Type.SRV;
		if ("TXT".equalsIgnoreCase(recordType)) return Type.TXT;
		throw new IllegalArgumentException("Record type " + recordType + " is unrecognized or unsupported");
	}
	
	private Resolver makeResolver(String resolverName) throws UnknownHostException {
		if (resolverName == null) {
			return new ExtendedResolver();
		}
		
		String[] resolvers = { resolverName };
		int resolverPort = 53;
		if (resolverName.contains(":")) {
			String[] resolverParts = resolverName.split(":", 2);
			resolvers[0] = resolverParts[0];
			try {
				resolverPort = Integer.parseInt(resolverParts[1]);
			} catch (NumberFormatException nfe) {
				// Eat it, stick with 53
			}
		}
		try {
			Resolver resolverObj = new ExtendedResolver(resolvers);
			resolverObj.setPort(resolverPort);
			return resolverObj;
		} catch (UnknownHostException uhe) {
			return new ExtendedResolver();
		}
	}
	
	private List<Map<Object,Object>> recordsToMapList(Record[] records) {
		List<Map<Object,Object>> recordMapList = new ArrayList<Map<Object,Object>>();
		if (records == null || records.length < 1) {
			return recordMapList;
		}
		for (Record rec : records) {
			Map<Object,Object> recordMap = new HashMap<Object,Object>();
			recordMap.put("name", rec.getName().toString());
			if (rec.getAdditionalName() != null) {
				recordMap.put("additionalName", rec.getAdditionalName().toString());
			}

			if (rec instanceof ARecord) {
				recordMap.put("address", ((ARecord) rec).getAddress());
			} else if (rec instanceof AAAARecord) {
				recordMap.put("address",  ((AAAARecord) rec).getAddress());
			} else if (rec instanceof MXRecord) { 
				recordMap.put("priority", ((MXRecord) rec).getPriority());
				recordMap.put("target", ((MXRecord) rec).getTarget());
			} else if (rec instanceof SRVRecord) {
				recordMap.put("priority", ((SRVRecord) rec).getPriority());
				recordMap.put("weight", ((SRVRecord) rec).getWeight());
				recordMap.put("port", ((SRVRecord) rec).getPort());
			} else if (rec instanceof TXTRecord) {
				recordMap.put("strings", ((TXTRecord) rec).getStrings());
			}
			recordMapList.add(recordMap);
		}
		return recordMapList;
	}

}

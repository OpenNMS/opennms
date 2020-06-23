/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.collection.sampler;

import java.io.File;

import org.opennms.netmgt.api.sample.Resource;
import org.opennms.netmgt.collection.api.CollectionResource;
import org.opennms.netmgt.collection.support.AbstractCollectionResource;
import org.opennms.netmgt.rrd.RrdRepository;

public class SamplerCollectionResource extends AbstractCollectionResource {

	private final Resource m_resource;

	public SamplerCollectionResource(Resource resource) {
		super(new SamplerCollectionAgent(resource.getAgent()));
		m_resource = resource;
	}

	@Override
	public File getResourceDir(RrdRepository repository) {
		File rrdBaseDir = repository.getRrdBaseDir();
		File nodeDir = new File(rrdBaseDir, getParent());
		if (CollectionResource.RESOURCE_TYPE_NODE.equalsIgnoreCase(m_resource.getType())) {
			return nodeDir;
		} else if (CollectionResource.RESOURCE_TYPE_IF.equalsIgnoreCase(m_resource.getType())) {
			// The label field contains the interface label
			return new File(nodeDir, getInterfaceLabel());
		} else {
			// TODO What do we do here?
			throw new IllegalStateException("Cannot figure out resource directory for type: " + m_resource.getType());
		}
		/*
		File typeDir = new File(nodeDir, m_resource.getOwnerName());
		File instDir = new File(typeDir, m_inst.replaceAll("\\s+", "_").replaceAll(":", "_").replaceAll("\\\\", "_").replaceAll("[\\[\\]]", "_"));
		return instDir;
		*/
	}

	@Override
	public String getResourceTypeName() {
		return m_resource.getType();
	}

	@Override
	public String getInstance() {
		// TODO Not sure how indexed metrics are represented in sampler API
		return null;
	}

}

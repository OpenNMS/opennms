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

package org.opennms.features.topology.plugins.topo.asset;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class GeneratorConfig {

    private String label;
    private String breadcrumbStrategy;
    private String providerId;
    private String preferredLayout;
    private String assetLayers;
    private String filter;
    private boolean generateUnallocated=false;

    public boolean getGenerateUnallocated() {
		return generateUnallocated;
	}

	public void setGenerateUnallocated(boolean generateUnallocated) {
		this.generateUnallocated = generateUnallocated;
	}

	public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getBreadcrumbStrategy() {
        return breadcrumbStrategy;
    }

    public void setBreadcrumbStrategy(String breadcrumbStrategy) {
        this.breadcrumbStrategy = breadcrumbStrategy;
    }

    public String getProviderId() {
        return providerId;
    }

    public void setProviderId(String providerId) {
        this.providerId = providerId;
    }

    public String getPreferredLayout() {
        return preferredLayout;
    }

    public void setPreferredLayout(String preferredLayout) {
        this.preferredLayout = preferredLayout;
    }

    public List<String> getLayerHierarchies() {
        return  Arrays.asList(assetLayers.split(",")).stream()
                .filter(h -> h != null && !h.trim().isEmpty())
                .map(h -> h.trim())
                .collect(Collectors.toList());
    }

    public String getAssetLayers() {
        return assetLayers;
    }

    public void setAssetLayers(String assetLayers) {
        this.assetLayers = assetLayers;
    }

	public String getFilter() {
		return filter;
	}

	public void setFilter(String filter) {
		this.filter = filter;
	}
	
    public List<String> getFilterList() {
    	if(filter==null || filter.isEmpty()) return null;
        return  Arrays.asList(filter.split("&")).stream()
                .filter(h -> h != null && !h.trim().isEmpty())
                .map(h -> h.trim())
                .collect(Collectors.toList());
    }

	@Override
	public String toString() {
		return "GeneratorConfig ["
				+ "providerId=" + providerId
				+ ", label=" + label 
				+ ", assetLayers="	+ assetLayers 
				+ ", filter=" + filter 
				+ ", breadcrumbStrategy="+ breadcrumbStrategy
				+ ", preferredLayout=" + preferredLayout 
				+ ", geneateUnallocated=" + generateUnallocated 
				+ "]";
	}
    
    
	
}

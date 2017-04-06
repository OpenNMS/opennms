package org.opennms.features.topology.plugins.topo.asset;

import java.util.Arrays;
import java.util.List;

public class UriParameters {

	public static final String  PROVIDER_ID="providerId";
	public static final String  ASSET_LAYERS="assetLayers";
	public static final String  FILTER="filter";
	public static final String  LABEL="label";
	public static final String  BREADCRUMB_STRATEGY="breadcrumbStrategy";
	public static final String  PREFFERED_LAYOUT="preferredLayout";
	
	public static final List<String> ALL_PARAMETERS= Arrays.asList(PROVIDER_ID,ASSET_LAYERS,FILTER,LABEL,BREADCRUMB_STRATEGY,PREFFERED_LAYOUT);
}

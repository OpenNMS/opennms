# System Properties

Enable the time-series storage API to delegate persistence and retrieval to a plugin:
```
org.opennms.timeseries.strategy=integration
org.opennms.timeseries.tin.metatags.tag.node=${node:label}
org.opennms.timeseries.tin.metatags.tag.location=${node:location}
org.opennms.timeseries.tin.metatags.tag.geohash=${node:geohash}
org.opennms.timeseries.tin.metatags.tag.ifDescr=${interface:if-description}
org.opennms.timeseries.tin.metatags.tag.label=${resource:label}
```

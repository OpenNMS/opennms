<#if nodeCriteria.foreignSource?? && nodeCriteria.foreignId??>
{
  "terms": {
    "exporter.foreign_source": ["${nodeCriteria.foreignSource?json_string}"]
  }
},
{
  "terms": {
    "exporter.foreign_id": ["${nodeCriteria.foreignId?json_string}"]
  }
}
<#else>
{
  "terms": {
    "exporter.node_id": ["${nodeCriteria.nodeId?int?c}"]
  }
}
</#if>
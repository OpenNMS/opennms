<#if nodeCriteria.foreignSource?? && nodeCriteria.foreignId??>
{
  "terms": {
    "node_exporter.foreign_source": ["${nodeCriteria.foreignSource?json_string}"]
  }
},
{
  "terms": {
    "node_exporter.foreign_id": ["${nodeCriteria.foreignId?json_string}"]
  }
}
<#else>
{
  "terms": {
    "node_exporter.node_id": ["${nodeCriteria.nodeId?int?c}"]
  }
}
</#if>
### merge-source
The merge source allows to merge two requisitions. You can also use provided resources by pris recursively.

    source = merge

| parameter        | required | description             |
|------------------|:--------:|------------------------:|
|  A.url           | *        |URL to the requisition A |
|  A.username      |          |username for access      |
|  A.password      |          |password for access      |
|                  |          |                         |
|  B.url           | *        |URL to the requisition B |
|  B.username      |          |username for access      |
|  B.password      |          |password for access      |
|                  |          |                         |
|  A.keepAll       |          | if this parameters is present in the config all nodes from requisition A will be present in the resulting requisition. |
|  B.keepAll       |          | if this parameters is present in the config all nodes from requisition B will be present in the resulting requisition. |

This source is reading two already defined requisitions via _HTTP_ and merges them into one new requisition. 
By default the resulting requisition will contain all nodes that are present in both requisitions, identified by the `foreignId`.
The A-Node (from requisition A) is enriched with the data from B-Node.
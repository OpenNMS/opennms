## Mapper
A mapper can be used to map the result of a source to an OpenNMS Requisition model. The mapper receives the result of the source.
The source is not limited in the data model it provides to the mapper. The mapper has to provide an OpenNMS Requisition as its result. 
If the source provides a custom data model, the mapper has to map it into a Requisition. Some sources provide OpenNMS Requisition directly, in thouse cases the `echo.mapper` can be used.
Complex sources like the `OCS-Sources` provide OCS specific models and require there own specific mappers.

### Echo.Mapper
The `echo.mapper` is a mapper that forwards the result of the source directly. 
That requires the source to provide a requisition it self. This mapper dose not change the result of the source.

### Null.Mapper
The `null.mapper` is a special mapper that just provides an empty requisition.
This can be usefull to handle the entire mapping between the result of the source and the OpenNMS Requision in a script step.
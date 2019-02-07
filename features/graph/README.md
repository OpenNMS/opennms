## Namespace

 - namespace should be unique overall graphs. Best practise: prefix with <container-id>
 - container id hsould be unique overall containers
 - namespace and container id must also be unique. So container id A and namespace A are interfering with each other   
    The reason for this is, that edges may point to a vertex in another graph (namespace). Without this limitation, 
    the reference should also container the container id, which makes stuff harder
    
## Graph Container Info

In order to get meta data about a container without actually loading it, which may take some time, the GraphContainerProvider
provides the possibility to only load the metadata. 
However the returned Container should also return the same values.
In order to not populate the Container Info into the actual Container, the service layer (Graph Service) will do that for you.
You may set them, but they will be overriden afterwards. TODO MVR wirklich überschreiben?
 
## Graph Info

 See Graph Container Info   
 
## Graph Provider

Convenience. Allows to simply provide one graph without implementing a Wrapping Container. This is automatically done

## Rest

 - Get data as XML -> graphml
 - Get data as JSON
 
 
 TODO MVR provide better documentation and put it in dev doc
 
## Persistence

All values of a graph may be persisted -> conversion to Generic*.
However only primitive types as well as a defined set is supported.
Supported types are:
 
 - String, boolean, int, char, ... as well as Integer, etc.
 - Enums
 - NodeRef, etc.
 - LocationRef

TODO MVR implement the following, as it is not supported at the moment
Collections are only supported if the elements of the list are from the supported type.
If you want to persist a type, which is not supported, you may provide an additional Converter.

## Enrichment

A common use case is to enrich your graph data (e.g. vertices) with node data.
Originally this had to be manually implemented.
Now it is possible to annotate a field/method (TODO MVR last is not yet implemented) to enrich it automatically.
An EnrichmentProcessor will then populate that field on access (or at certain points).
The EnrichmentProcessor may make some assumption about the type of the vertex.
For example, in order to enrich a vertex with some node data, the vertex must implement the interface NodeRef.
The EnrichmentProcessor then can use the NodeRef to obtain the node data from the database and assign the NodeInfo holding all node relevant information.
However if the graph is persisted, that data is not persisted as well.

## Changelog

It is possible to detect the changes between graphs or containers.
Use ChangeLog or ContainerChangeLog for this.

TODO MVR implement property updates

## Updates

Listen for updates... TODO MVR
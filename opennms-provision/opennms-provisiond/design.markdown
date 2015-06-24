Lifecycle/Phases in Provisioning
================================

The ideas here were inspired my maven's architecture.

Allow Simple and Customizable Provisioning Services to Users of OpenNMS
-----------------------------------------------------------------------

This will include 'discovered provisioning' as currently provided by Capsd
and 'directed provisioning' at currently provided by the importer as well as 
a hybrid of direction and discover that is as yet unprovided.

Allow Enhancement of Provisioning Without Rewriting the Provisioning Service
----------------------------------------------------------------------------

We need to be able to cover things like:

* only provision interfaces that are not phone connections
* enable collection on switch ports if they are active
* add an entry to the /etc/hosts file for any newly discovered nodes/interfaces

Abstractions
============

To accomplish this we have defined several key abstractions:

Lifecycle
---------

This represents an entire process.  For example the import lifecycle represents the entire
process of importing a file, scanning the nodes, and updated the database.  There is more than
one kind of lifecycle in provisioning.  Scanning a new node is represented as a lifecycle,
importing an entire network, and scanning an interface for services.

A lifecycle can contain nested lifecycles, for example, the import a network lifecyle contains
nested node scanning lifecycles

A lifecycle consists of nested phases.  A phase represents one sequential step of the lifecyle.
Phases happen in series within a lifecyle.  Activities necessary to do the provisioning are 
each associated with a lifecycle phase.

Lifecycles are 'triggered' by various events which can include the following:
1. a newSuspect event or other event is received
2. the scheduled time for a scan has occurred
3. a lifecycle triggers a nested lifecycle

Data associated with the lifecycle are stored as attributes on the lifecycle so activities
can access the data.

Phase
-----

This represents one single sequential step in a lifecycle.  They each have a well-known name as
described in the configuration.  Activities are associated with the phases of a lifecyle

Activity
--------

These are the actions that are performed in the processing of a lifecycle phase.  An activity is
represented by method on an ActivityProvider.  Methods are annotated to indicate which lifecycle
and phase they should be associated with.

Trigger
-------

This is the impetus for starting and running a new lifecyle.  The properties related to the
lifecycle are here.  For example, an import event that triggers the import of a network would
trigger the import lifecycle and the related properties would be the importResource and the
foreignSource (if it is overridden)

Other Issues
============

- How can I trigger a new lifecycle
- How do I manage the synchronization of lifecycle work?
- Is there a way to 'define' a lifecycle on the fly that has various phases in order to increase
  the parallelism?
- Can you have asynchronous phases?
- Phase dependencies and parallelization of lifecycles?
- How about phases that trigger an entire collection of lifecycles? 

Category Lifecycle
==================

Node categories present an interesting problem in that there are multiple phases of the import
and scan process that influence the categories a node should have, and it is problematic if
categories are removed and then re-added through different phases of a scan while they are being
used at runtime by the OpenNMS system.

To resolve this, we have the following design:

Import Phase
------------

During the import phase, the behavior differs depending on whether the node already exists in
the database (update), or doesn't (insert).

In the case of a new node (insert), Provisiond will add each requisitioned category to the
node before persisting.

In the case of an existing node (update), the node's list of categories will remain unchanged
until the scan phase.  Note that between this time and the time that the Scan Phase finishes,
updated nodes will contain any interface/etc. changes made by the requisition, but they will
*not* contain category changes.

Scan Phase
----------

At the start of the scan phase, the node object's "requisitioned categories" will be populated
using the list of categories from the requisition.  Then, during the scan phase, NodePolicies
will update these same "requisitioned categories", rather than addCategory() or removeCategory()
like the previous behavior.

When the scan phase has completed, the list of previously requisitioned categories will be
pulled from the database, and compared to the list provided by getRequisitionedCategories().
These 2 lists will be reconciled, adding and removing categories with addCategory() and
removeCategory() to/from the node as necessary.  The new list will be persisted back to the
database for the next scan.

Thus:

* if a requisition adds a category, it will be added to the node
* if a requisition removes a category, it will be removed from the node
* any categories added through other means (which weren't also in the requisition) will be left alone


Search Control
==============

Search input box, with autocomplete drawer when matching.

* inputs:
  - markersModelUpdated: redraw autocomplete list
  - searchStringUpdated: update search input box (but don't re-fire events)
* outputs:
  - searchStringUpdated

Alarm Control
=============

Alarm severity selection control.

* inputs:
  - alarmSeverityUpdated: update selector (but don't re-fire events)
* outputs:
  - alarmSeverityUpdated

Connector (Server-to-Client)
============================

Updated when the server side pushes new nodes, or a search string.

* inputs:
* outputs:
  - searchStringUpdated

Node/Alarm Table
================

The list of matching nodes or alarms in the lower part of the screen.

* inputs:
  - alarmSeverityUpdated: re-filter results
* outputs:
  - searchStringUpdated: set nodeId=foo based on link click

Marker Filter
=============

Handles the implementation of search matching.  Given a search string and
a marker, returns true if the marker matches.

* inputs:
  - searchStringUpdated: if the search string is updated, store it
    and fire an event that filters need to be re-evaluated
  - alarmSeverityUpdated: if the alarm severity is changed, store it
    and fire an event that filters need to be re-evaluated
* outputs:
  - filterUpdated

Marker Container
================

Contains the full list of map-ready marker objects sent from the server
side, as well as a sub-list of markers that match based on the Marker
Filter.

* inputs:
  - filterUpdated: If the filter has changed, re-apply the filter to
    the marker list and store in the filtered sub-list.  Then fire
    filteredMarkersUpdated.
  - markersModelUpdated: If the model has been updated, re-apply
    the filter to the marker list and store in the filtered sub-list.
    Then fire filteredMarkersUpdated.
* outputs:
  - markersModelUpdated: If an external class has updated the set
    of markers (setMarkers()), we fire this so a filter refresh occurs.
  - filteredMarkersUpdated: If the filters were re-applied to the
    marker list, fire an event.

Node Map
========

The main map widget.  Handles redrawing all layers.

* inputs:
  - filteredMarkersUpdated: If the filtered markers have changed,
    update the map layers.

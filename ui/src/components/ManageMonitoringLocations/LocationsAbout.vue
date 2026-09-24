<template>
  <div class="help-section">
    <div class="section-title">What monitoring locations are for</div>
    <p>
      A monitoring location groups the OpenNMS main system and its Minions
      into a monitoring area, so polling and data collection can run from a
      specific place. Nodes and Minions reference a location by its
      <strong>name</strong>; the optional geolocation, latitude and
      longitude place it on the geographic map, and the
      <strong>priority</strong> orders locations (lower first).
    </p>
  </div>
  <div class="help-section">
    <div class="section-title">How to use this page</div>
    <p>
      <strong>Add New Location</strong> creates a location; <strong>Edit</strong>
      changes its area, geolocation, coordinates and priority. The name is the
      identifier and cannot be changed — recreate the location to rename it.
      <strong>Delete</strong> only succeeds once no nodes are assigned to the
      location, so reassign them first; Minions still pointing at it keep the
      old location name until they are re-registered. The same operations are
      available to tooling through the <code>/api/v2/monitoringLocations</code>
      REST API.
    </p>
  </div>
</template>

<script setup lang="ts">
</script>

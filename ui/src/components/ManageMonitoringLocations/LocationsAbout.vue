<template>
  <div class="help-section">
    <div class="section-title">What monitoring locations are for</div>
    <p>
      A monitoring location groups the OpenNMS main system and its Minions
      so polling and data collection can run from a specific place. Nodes
      and Minions reference a location by its <strong>name</strong>; the
      <strong>description</strong> is free text for administrators. The
      built-in <strong>Default</strong> location is the OpenNMS core itself,
      so it cannot be edited or deleted here.
    </p>
  </div>
  <div class="help-section">
    <div class="section-title">How to use this page</div>
    <p>
      <strong>Add New Location</strong> creates a location; <strong>Edit</strong>
      changes its description. The name is the identifier and cannot be
      changed — recreate the location to rename it. The <strong>Minions</strong>
      column counts the Minions registered in the location and flags those
      that are down or unknown; the link opens them on the Minions tab.
      <strong>Nodes</strong> counts the nodes assigned to the location.
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

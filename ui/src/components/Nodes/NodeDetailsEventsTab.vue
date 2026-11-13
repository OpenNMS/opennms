<template>
  <!-- Stacked full width: with the tab to themselves, the tables get room for their columns. -->
  <div class="onms-row">
    <div class="onms-col-12">
      <EventsTable />
      <OutagesTable />
    </div>
  </div>
</template>

<script setup lang="ts">
import EventsTable from '@/components/Nodes/EventsTable.vue'
import OutagesTable from '@/components/Nodes/OutagesTable.vue'

// Both tables fetch by the route's node id themselves, so this tab needs nothing from the page.
</script>

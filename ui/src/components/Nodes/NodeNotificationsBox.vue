<template>
  <NodeDetailsBanner
    severity="normal"
    :strip="false"
    data-test="node-notifications-box"
  >
    <strong>Notifications</strong>
  </NodeDetailsBanner>
</template>

<script setup lang="ts">
import NodeDetailsBanner from './NodeDetailsBanner.vue'

// A condensed view of the node's notifications, beside the status banner. It borrows the banner's
// look with the normal tint but no severity strip: notifications have no severity of their own to
// signal. Its content is still to be designed (NMS-20449).
</script>

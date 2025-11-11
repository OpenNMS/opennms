<template>
  <div class="event-config">
    <div class="heading">
      <h1>Event Configuration</h1>
    </div>
    <div class="tabs">
      <EventConfigTabContainer />
    </div>
  </div>
</template>

<script lang="ts" setup>
import EventConfigTabContainer from '@/components/EventConfiguration/EventConfigTabContainer.vue'
</script>

<style lang="scss" scoped>
.event-config {
  padding: 20px;

  .heading {
    margin-bottom: 10px;
  }
}
</style>


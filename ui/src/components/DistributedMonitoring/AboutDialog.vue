<template>
  <AboutDialogButton title="Minions and Locations" class="about-dialog">
    <div class="help-section" data-test="about-minions">
      <div class="section-heading">Minions</div>
      <div class="section-title">Status</div>
      <p>Up when heartbeats arrive and RPC works. Down when heartbeats stop or RPC fails. Unknown when OpenNMS has no node for the Minion in the Minions requisition, so it does not monitor the Minion.</p>
      <div class="section-title">Monitoring location and version</div>
      <p>Reported by the Minion itself. Change the location in its <code>org.opennms.minion.controller</code> config.</p>
      <div class="section-title">Delete</div>
      <p>Meant for decommissioned Minions, so a Minion that is up cannot be deleted; stop it first. Its alarms are removed with it, and a Minion that is still running registers again.</p>
    </div>
    <div class="help-section" data-test="about-locations">
      <div class="section-heading">Monitoring locations</div>
      <div class="section-title">Monitoring location</div>
      <p>Where Minions run and where nodes are monitored from. Default means the core server itself.</p>
      <div class="section-title">Created automatically</div>
      <p>A requisition or Minion that uses a new name creates it. Adding one here is rarely needed.</p>
      <div class="section-title">Delete</div>
      <p>Only possible with no nodes or Minions assigned. Its perspective outage history is removed with it.</p>
    </div>
  </AboutDialogButton>
</template>

<script setup lang="ts">
import AboutDialogButton from '@/components/Common/AboutDialogButton.vue'
</script>

<style lang="scss" scoped>
.section-heading {
  font-size: 1.05rem;
  font-weight: 600;
  margin-bottom: 0.75rem;
}
</style>

<template>
  <div class="help-section">
    <div class="section-title">What Minions are</div>
    <p>
      A Minion is a lightweight remote process that performs polling, data
      collection and flow/trap reception on behalf of the main OpenNMS
      system from a monitoring location. Each Minion registers itself and
      reports its <strong>status</strong> (UP, DOWN or UNKNOWN), its
      <strong>version</strong> and its last check-in time, all maintained by
      the system and shown here read-only.
    </p>
  </div>
  <div class="help-section">
    <div class="section-title">How to use this page</div>
    <p>
      The <strong>Version</strong> tag is green when it matches the core's
      version and red otherwise. <strong>Last heartbeat</strong> is green
      within five minutes, amber within two hours and red beyond that. The
      quick filters narrow the list to Minions that need attention; the
      <strong>Monitoring location</strong> link opens that location on the
      Monitoring locations tab.
      <strong>Delete</strong> removes a Minion and its auto-created
      requisition node; a Minion whose process is still running will
      re-register on its next check-in. Editing is not available on this
      page; the <code>/api/v2/minions</code> REST API still accepts updates.
    </p>
  </div>
</template>

<script setup lang="ts">
</script>

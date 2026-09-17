<template>
  <div class="help-section">
    <div class="section-title">What Minions are</div>
    <p>
      A Minion is a lightweight remote process that performs polling, data
      collection and flow/trap reception on behalf of the main OpenNMS
      system from a monitoring location. Each Minion registers itself and
      reports its <strong>status</strong>, <strong>version</strong> and last
      check-in time; those, along with its type, are maintained by the
      system and shown here read-only.
    </p>
  </div>
  <div class="help-section">
    <div class="section-title">How to use this page</div>
    <p>
      <strong>Edit</strong> changes a Minion's label, its
      <strong>location</strong>, and its key/value <strong>properties</strong>.
      <strong>Delete</strong> removes a Minion and its auto-created
      requisition node; a Minion whose process is still running will
      re-register on its next check-in. The same operations are available to
      tooling through the <code>/api/v2/minions</code> REST API.
    </p>
  </div>
</template>

<script setup lang="ts">
</script>

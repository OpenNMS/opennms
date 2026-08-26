<template>
  <div data-test="scheduled-outages-about">
    <p>
      A scheduled outage is a named window during which OpenNMS suppresses selected activity for
      the chosen nodes and interfaces. Each outage has a type (specific dates, or a recurring
      daily, weekly, or monthly schedule) and one or more time spans.
    </p>
    <p>
      The <strong>Applies To</strong> settings control which subsystems honor the outage: notifications,
      and the status-polling, threshold-checking, and data-collection packages you select.
    </p>
    <p>
      Choosing <strong>Select all nodes and interfaces</strong> makes the outage apply everywhere,
      replacing any specific node or interface selections.
    </p>
  </div>
</template>

<script setup lang="ts">
</script>

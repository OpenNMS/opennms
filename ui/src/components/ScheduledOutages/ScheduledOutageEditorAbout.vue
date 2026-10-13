<template>
  <div class="help-section" data-test="about-selection">
    <div class="section-title">Nodes and interfaces</div>
    <p>The lists show what the outage covers now. Search below a list and press <strong>Add</strong> to extend it, or remove a row with its delete button.</p>
    <p><strong>Select all nodes and interfaces</strong> makes the outage apply everywhere and replaces any specific selection. Remove <strong>All Interfaces</strong> to go back to choosing nodes and interfaces.</p>
    <div class="section-title">Applies To</div>
    <p>Tick which subsystems honor the outage: notifications, and the status-polling, threshold-checking and data-collection packages.</p>
  </div>
  <div class="help-section" data-test="about-schedule">
    <div class="section-title">Outage type and time spans</div>
    <p><strong>Specific</strong> is a one-off window between two dates. <strong>Daily</strong>, <strong>Weekly</strong> and <strong>Monthly</strong> repeat a time-of-day window every day, on a weekday, or on a day of the month.</p>
    <p>The saved time spans are listed first. Pick a start and an end under <strong>Add a time span</strong> and press <strong>Add Time Span</strong> for each window. The end must come after the start, and times are in the server's time zone.</p>
    <div class="section-title">Saving</div>
    <p>Nothing is stored until you press <strong>Create</strong> or <strong>Save</strong>. Saving tells the daemons to reload the outage configuration, so the change takes effect without a restart.</p>
  </div>
</template>

<script setup lang="ts">
</script>

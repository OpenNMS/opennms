<template>
  <div class="help-section">
    <div class="section-title">What on-call roles are for</div>
    <p>
      An on-call role is a notification rota. It names a <strong>membership group</strong>, whose
      members can be scheduled, and a <strong>supervisor</strong>. A destination path that
      targets the role notifies whoever is on call at that moment.
    </p>
    <p>
      Each member has one or more <strong>schedules</strong> that set the days and hours they
      are on duty. When nobody is scheduled, the notice goes to the supervisor; the
      <em>On Call Now</em> column marks that case with a <em>fallback</em> tag.
    </p>
  </div>
  <div class="help-section">
    <div class="section-title">How to use this page</div>
    <p>
      <strong>Add New On-Call Role</strong> creates a role; <strong>Edit</strong> changes it.
      <strong>Rename</strong> is in the row menu; destination paths that target the role by name
      do not follow a rename, so update them afterwards. Use the filter above the table to find a
      role by name, supervisor, group or description. Roles are stored in the OpenNMS database
      and are also available through the versioned <code>/api/v2/oncall-roles</code> REST API.
    </p>
  </div>
</template>

<script setup lang="ts">
</script>

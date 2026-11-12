<template>
  <div class="help-section">
    <div class="section-title">What groups are for</div>
    <p>
      A group is an ordered collection of users, used mainly for notifications: when a
      destination path targets a group, its members are notified one at a time, in the order
      they appear here, with the configured interval between them. Groups are also the
      membership pool for on-call roles, which schedule who is on duty.
    </p>
    <p>
      Groups are stored in the OpenNMS database and managed here or through the versioned
      <code>/api/v2/groups</code> REST API.
    </p>
  </div>
  <div class="help-section">
    <div class="section-title">What you can enter</div>
    <p>
      The <strong>Group</strong> tab holds the name, the comments and the members. Use the up
      and down arrows to set the member order: for notifications this is the escalation order,
      so put the first responder at the top. The <strong>Duty Schedules</strong> tab sets the
      days and hours at which the group can be notified. The <strong>Categories</strong> tab
      lists the node categories the group's members may see when ACLs are enabled. Use the
      filter above the table to find a group by name, comments or member.
    </p>
  </div>
  <div class="help-section">
    <div class="section-title">How to use this page</div>
    <p>
      <strong>Add New Group</strong> creates a group; <strong>Edit</strong> changes it.
      <strong>Rename</strong> is in the row menu: the on-call roles that use the group and its
      category authorizations follow the rename. Destination paths that target the group by
      name do not follow a rename; update them afterwards. <strong>Delete</strong> is refused while an
      on-call role or a destination path uses the group; change or delete those first. The
      <em>Admin</em> group is a system group and cannot be deleted or renamed.
    </p>
  </div>
</template>

<script setup lang="ts">
</script>

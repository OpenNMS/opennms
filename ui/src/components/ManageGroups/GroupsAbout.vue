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
      Groups are stored in <code>groups.xml</code>. Editing that file by hand keeps working;
      changes made here and changes made in the file stay in sync. Group duty schedules and
      the default map, which have no editor on this page yet, are preserved untouched.
    </p>
  </div>
  <div class="help-section">
    <div class="section-title">How to use this page</div>
    <p>
      <strong>Add New Group</strong> creates a group; <strong>Edit</strong> manages the
      members. Use the up and down arrows to set the member order — for notifications this is
      the escalation order, so put the first responder at the top.
    </p>
    <p>
      <strong>Rename</strong> keeps everything consistent: on-call roles that reference the
      group follow the rename automatically. <strong>Delete</strong> is refused while an
      on-call role still references the group — delete or reassign the role first. The
      <em>Admin</em> group is a system group and cannot be deleted or renamed. Destination
      paths that target a group by name do not follow renames or deletes; review them after
      changing group names.
    </p>
  </div>
</template>

<script setup lang="ts">
</script>

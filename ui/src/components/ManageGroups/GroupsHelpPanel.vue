<template>
  <TogglePanel
    :collapsed="collapsed"
    class="groups-help-panel"
    data-test="groups-help-panel"
    @update:collapsed="(value: boolean) => (collapsed = value)"
  >
    <template #header>
      <span class="panel-header">
        <i class="pi pi-question-circle" aria-hidden="true" />
        About Groups
      </span>
    </template>
    <div class="help-columns">
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
    </div>
  </TogglePanel>
</template>

<script setup lang="ts">
import { ref } from 'vue'

import TogglePanel from '@/components/Common/TogglePanel.vue'

// Starts collapsed — the "?" invites first-time users to expand.
const collapsed = ref(true)
</script>

<style lang="scss" scoped>
.panel-header {
  display: inline-flex;
  align-items: center;
  gap: 0.5rem;
  font-weight: 600;

  .pi-question-circle {
    color: var(--p-primary-color);
  }
}

.help-columns {
  display: flex;
  gap: 2.5rem;
  flex-wrap: wrap;

  .help-section {
    flex: 1;
    min-width: 320px;
  }
}

.section-title {
  font-size: 1rem;
  font-weight: 600;
  margin-bottom: 0.5rem;
}

p {
  margin: 0 0 0.75rem 0;
  font-size: 0.9rem;
  line-height: 1.5;
}
</style>

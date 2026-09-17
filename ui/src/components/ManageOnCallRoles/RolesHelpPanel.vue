<template>
  <TogglePanel
    :collapsed="collapsed"
    class="roles-help-panel"
    data-test="roles-help-panel"
    @update:collapsed="(value: boolean) => (collapsed = value)"
  >
    <template #header>
      <span class="panel-header">
        <i class="pi pi-question-circle" aria-hidden="true" />
        About On-Call Roles
      </span>
    </template>
    <div class="help-columns">
      <div class="help-section">
        <div class="section-title">What on-call roles are for</div>
        <p>
          An on-call role is a rotating duty rota: a destination path can target the role, and
          whoever is scheduled at the moment a notification fires receives it. Scheduled users are
          drawn from the role's <strong>membership group</strong>; the <strong>supervisor</strong>
          receives the notifications whenever nobody is scheduled — those intervals show as
          <em>unscheduled</em> on the calendar.
        </p>
        <p>
          Roles and their schedules are stored in <code>groups.xml</code>. Editing that file by
          hand keeps working; recurring weekly, daily or monthly entries defined there are shown
          on the calendar and preserved untouched by this page.
        </p>
      </div>
      <div class="help-section">
        <div class="section-title">How to use this page</div>
        <p>
          <strong>Add New Role</strong> creates the role with its group and supervisor;
          <strong>Edit</strong> changes them. <strong>Schedule</strong> opens the month calendar:
          it shows exactly what the notification engine will do, and the coverage editor below it
          adds one-off entries — pick a member, a start and an end. Multiple people may be
          scheduled at once; all of them are notified.
        </p>
        <p>
          <strong>Rename</strong> and <strong>Delete</strong> do not update destination paths that
          target the role by name — review those afterwards. The whole page is also available to
          your own tooling through the versioned <code>/api/v2/on-call-roles</code> REST API,
          including the computed calendar.
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

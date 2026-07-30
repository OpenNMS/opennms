<template>
  <TogglePanel
    :collapsed="collapsed"
    class="users-help-panel"
    data-test="users-help-panel"
    @update:collapsed="(value: boolean) => (collapsed = value)"
  >
    <template #header>
      <span class="panel-header">
        <i class="pi pi-question-circle" aria-hidden="true" />
        About Users
      </span>
    </template>
    <div class="help-columns">
      <div class="help-section">
        <div class="section-title">What users are for</div>
        <p>
          A user is both a web console login and a notification recipient. The email and pager
          email addresses entered here are where notifd delivers notices when the user is targeted
          directly or through a group or on-call role. Security roles control what the account may
          do: <em>ROLE_USER</em> grants normal console access, <em>ROLE_ADMIN</em> full
          administration, <em>ROLE_READONLY</em> makes the account read-only.
        </p>
        <p>
          Users are stored in <code>users.xml</code>. Editing that file by hand keeps working, and
          contact types this page does not show (XMPP, phone numbers, paging services) as well as
          duty schedules are preserved untouched. Passwords are always stored salted; they are
          never shown or returned by the API.
        </p>
      </div>
      <div class="help-section">
        <div class="section-title">How to use this page</div>
        <p>
          <strong>Add New User</strong> creates the account with its initial password;
          <strong>Edit</strong> changes contact details and security roles;
          <strong>Password</strong> resets the password. New accounts can log in within a few
          seconds of creation.
        </p>
        <p>
          <strong>Rename</strong> keeps group memberships intact — the user stays in all their
          groups under the new id. <strong>Delete</strong> also removes the user from every group.
          The <em>admin</em> and <em>rtc</em> accounts are system accounts and cannot be deleted
          or renamed. All of this is also available to your own tooling through the versioned
          <code>/api/v2/users</code> REST API.
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

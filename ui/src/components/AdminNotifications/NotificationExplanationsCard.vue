<template>
  <TogglePanel
    :collapsed="collapsed"
    class="notification-explanations-panel"
    data-test="explanations-panel"
    @update:collapsed="onCollapsedChange"
  >
    <template #header>
      <span class="panel-header">
        <i class="pi pi-question-circle" aria-hidden="true" />
        About Notices and Escalation
      </span>
    </template>
    <div class="explanation-columns">
      <div class="explanation-section">
        <div class="section-title">Outstanding and Acknowledged Notices</div>
        <p>
          When important events are detected by OpenNMS, users may receive a <em>notice</em>, a
          descriptive message sent automatically to a pager, an email address, or both. In order to
          receive notices, the user must have their notification information configured in their
          user profile (see your Administrator for assistance), notices must be <em>on</em>, and an
          important event must be received.
        </p>
        <p>
          From this panel, you may: <strong>Check your outstanding notices</strong>, which displays
          all unacknowledged notices sent to your user ID; <strong>View all outstanding
          notices</strong>, which displays all unacknowledged notices for all users; or
          <strong>View all acknowledged notices</strong>, which provides a summary of all notices
          sent and acknowledged for all users.
        </p>
        <p>
          You may also search for notices associated with a specific user ID by entering that user
          ID in the <strong>Check notices for user</strong> text box. And finally, you can jump
          immediately to a page with details specific to a given notice identifier by entering that
          numeric identifier in the <strong>Get details for notice</strong> text box. Note that
          this is particularly useful if you are using a numeric paging service and receive the
          numeric notice identifier as part of the page.
        </p>
      </div>
      <div class="explanation-section">
        <div class="section-title">Notification Escalation</div>
        <p>
          Once a notice is sent, it is considered <em>outstanding</em> until someone
          <em>acknowledge</em>s receipt of the notice via the OpenNMS Notification interface. If
          the event that triggered the notice was related to managed network devices or systems,
          the <strong>Network/Systems</strong> group will be notified, one by one, with a notice
          sent to the next member on the list only after 15 minutes has elapsed since the last
          message was sent. This progression through the list, or <em>escalation</em>, can be
          stopped at any time by acknowledging the notice. Note that this is <strong>not</strong>
          the same as acknowledging the event which triggered the notice. If all members of the
          group have been notified and the notice has not been acknowledged, the notice will be
          escalated to the <strong>Management</strong> group, where all members of that group will
          be notified at once with no 15 minute escalation interval.
        </p>
      </div>
    </div>
  </TogglePanel>
</template>

<script setup lang="ts">
import { ref } from 'vue'

import TogglePanel from '@/components/Common/TogglePanel.vue'

// Starts collapsed — the "?" in the header invites first-time users to expand;
// the notices list keeps the screen real estate by default.
const collapsed = ref(true)

const onCollapsedChange = (value: boolean) => {
  collapsed.value = value
}
</script>

<style lang="scss" scoped>
// Line the +/- toggle up with the page-header gear button above it: both sit
// on the page's right edge, but the Panel header's default right padding puts
// the toggle ~10px left of the gear's icon center.
.notification-explanations-panel :deep(.p-panel-header) {
  padding-right: 0.5rem;
}

.panel-header {
  display: inline-flex;
  align-items: center;
  gap: 0.5rem;
  font-weight: 600;

  .pi-question-circle {
    color: var(--p-primary-color);
  }
}

.explanation-columns {
  display: flex;
  gap: 2.5rem;
  flex-wrap: wrap;

  .explanation-section {
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

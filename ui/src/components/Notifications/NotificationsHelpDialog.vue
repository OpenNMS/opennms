<template>
  <OnmsMessageDialog
    :visible="visible"
    :relative="true"
    maxWidth="60em"
    maxHeight="80vh"
    title="About Notifications and Escalation"
    data-test="explanations-dialog"
    @close="emit('update:visible', false)"
  >
    <template #content>
      <div class="explanation-columns">
        <div class="explanation-section">
          <div class="section-title">Outstanding and Acknowledged Notifications</div>
          <p>
            When important events are detected by OpenNMS, users may receive a <em>notification</em>, a
            descriptive message sent automatically to a pager, an email address, or both. In order to
            receive notifications, the user must have their notification information configured in their
            user profile (see your Administrator for assistance), notifications must be <em>on</em>, and an
            important event must be received.
          </p>
          <p>
            From this panel, you may select: <strong>Your outstanding notifications</strong>, which
            displays all unacknowledged notifications sent to your user ID; <strong>Outstanding for
              anyone but you</strong>, which displays all unacknowledged notifications sent to other
            users; <strong>All outstanding notifications</strong>, which displays all unacknowledged
            notifications for all users; or <strong>All acknowledged notifications</strong>, which
            provides a summary of all notifications sent and acknowledged for all users.
          </p>
          <p>
            You may also search for notifications associated with a specific user ID by selecting
            <strong>Notifications for user:</strong> and entering that user ID in the box beside it.
            And finally, you can jump immediately to a page with details specific to a given
            notification identifier by selecting <strong>View details for ID:</strong> and
            entering that numeric identifier. Note that this is particularly useful if you are using a
            numeric paging service and receive the numeric notification identifier as part of the page.
          </p>
        </div>
        <div class="explanation-section">
          <div class="section-title">Notification Escalation</div>
          <p>
            Once a notification is sent, it is considered <em>outstanding</em> until someone
            <em>acknowledge</em>s receipt of the notification via the OpenNMS Notification interface. If
            the event that triggered the notification was related to managed network devices or systems,
            the <strong>Network/Systems</strong> group will be notified, one by one, with a notification
            sent to the next member on the list only after 15 minutes has elapsed since the last
            message was sent. This progression through the list, or <em>escalation</em>, can be
            stopped at any time by acknowledging the notification. Note that this is <strong>not</strong>
            the same as acknowledging the event which triggered the notification. If all members of the
            group have been notified and the notification has not been acknowledged, the notification will be
            escalated to the <strong>Management</strong> group, where all members of that group will
            be notified at once with no 15 minute escalation interval.
          </p>
        </div>
      </div>
    </template>
  </OnmsMessageDialog>
</template>

<script setup lang="ts">
import { OnmsMessageDialog } from '@opennms/onms-ui'

defineProps<{ visible: boolean }>()
const emit = defineEmits(['update:visible'])
</script>

<style lang="scss" scoped>
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

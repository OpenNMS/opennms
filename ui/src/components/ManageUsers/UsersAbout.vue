<template>
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
      Users are stored in the OpenNMS database and managed here or through the versioned
      <code>/api/v2/users</code> REST API. Passwords are always stored salted; they are never
      shown or returned by the API. A user that cannot be changed through this page carries a
      <em>read-only</em> tag next to its id.
    </p>
  </div>
  <div class="help-section">
    <div class="section-title">What you can enter</div>
    <p>
      The <strong>User</strong> tab holds the user id and, when you create a user, the initial
      password. Beside them: full name, comments, email, pager email, work, mobile and home
      phone, the TUI PIN, the numeric pager service and PIN, the text pager service and PIN, the
      time zone, and the security roles. The <strong>Duty Schedules</strong> tab sets the days
      and hours at which the user can be notified. Use the filter above the table to find a
      user by id, full name, email or comments.
    </p>
  </div>
  <div class="help-section">
    <div class="section-title">How to use this page</div>
    <p>
      <strong>Add New User</strong> creates the account; <strong>Edit</strong> changes any of
      the details above. The row menu (the three dots) has <strong>Change Password</strong>,
      which resets the password, and <strong>Rename</strong>. New accounts can log in within a
      few seconds of creation.
    </p>
    <p>
      <strong>Rename</strong> keeps group memberships intact: the user stays in all their
      groups under the new id. <strong>Delete</strong> also removes the user from every group.
      The <em>admin</em> and <em>rtc</em> accounts are system accounts and cannot be deleted
      or renamed.
    </p>
  </div>
</template>

<script setup lang="ts">
</script>

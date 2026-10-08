<template>
  <AboutDialogButton title="Applications" class="about-dialog">
    <div class="help-section" data-test="about-applications">
      <div class="section-title">Applications</div>
      <p>An application groups monitored services that together deliver one thing your users rely on, such as a web store made of web, database and DNS services.</p>
      <div class="section-title">Services</div>
      <p>Any monitored service on any node can belong to one or more applications. Adding a service here does not change how the core server polls it.</p>
    </div>
    <div class="help-section" data-test="about-perspectives">
      <div class="section-title">Perspective locations</div>
      <p>The perspective poller checks the application's services from each of these monitoring locations, so you see outages the way users in those places would. The Default location is the core server itself. Without perspective locations the application only groups its services.</p>
      <div class="section-title">Delete</div>
      <p>Removes the application only. Its services, their nodes and their outage history stay. A service that no other application checks from the same location stops being checked from there, and its open perspective outage is closed.</p>
    </div>
  </AboutDialogButton>
</template>

<script setup lang="ts">
import AboutDialogButton from '@/components/Common/AboutDialogButton.vue'
</script>

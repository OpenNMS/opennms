<template>
  <div v-if="state !== 'ok'" class="banner" :class="state" role="status" :data-test="`readiness-${state}`">
    <div class="text">
      <strong v-if="state === 'not-ready'">WS-Man is not ready.</strong>
      <strong v-else-if="state === 'unpolled'">Some WS-Man servers are not being polled.</strong>
      <strong v-else>No WS-Man servers yet.</strong>
      <span v-if="state === 'not-ready'">
        <template v-if="!readiness.pollerService || !readiness.pollerMonitor">
          No package in <code>poller-configuration.xml</code> includes the WS-Man service<template v-if="!readiness.pollerMonitor"> and its monitor is not registered</template>,
          so provisioned servers are never checked. Enabling adds the service to
          <code>{{ readiness.pollerPackage || 'the catch-all package' }}</code> and reloads Pollerd.
        </template>
        <template v-if="!readiness.collectdService || !readiness.collectdCollector">
          <code>collectd-configuration.xml</code> does not include the WS-Man service and collector; add them there and reload Collectd.
        </template>
      </span>
      <span v-else-if="state === 'unpolled'">
        {{ readiness.unpolledServers }} server(s) were provisioned before polling was enabled and are marked not polled.
        Rescanning their requisition(s) ({{ readiness.requisitionsWithUnpolled.join(', ') || 'unknown' }}) lets provisioning re-evaluate them.
      </span>
      <span v-else>
        Link a server definition to a requisition and press Sync, or add nodes with the WS-Man service in
        Provisioning Requisitions.
      </span>
      <span v-if="message" class="message" data-test="readiness-message">{{ message }}</span>
    </div>
    <div class="actions">
      <OnmsButton v-if="state === 'not-ready' && (!readiness.pollerService || !readiness.pollerMonitor)" label="Enable WS-Man polling" :disabled="busy" data-test="enable-polling" @click="run('enable-polling')" />
      <OnmsButton v-if="state === 'unpolled'" label="Rescan requisitions" :disabled="busy" data-test="rescan" @click="run('rescan')" />
      <a v-if="state === 'no-servers'" :href="requisitionsUrl" class="link" data-test="requisitions-link">Open Provisioning Requisitions</a>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import { OnmsButton } from '@opennms/onms-ui'
import { WsmanReadiness } from '@/types/wsmanAdmin'

// One banner, one state at a time, in order of what blocks the most: the
// poller and collectd not covering WS-Man at all, then servers provisioned
// before that was fixed, then simply having no servers yet.
const props = defineProps<{
  readiness: WsmanReadiness
  requisitionsUrl: string
  // runs the action and resolves to null on success or the reason to show
  runAction: (action: 'enable-polling' | 'rescan') => Promise<string | null>
}>()

const busy = ref(false)
const message = ref('')

const state = computed<'not-ready' | 'unpolled' | 'no-servers' | 'ok'>(() => {
  if (!props.readiness.ready) {
    return 'not-ready'
  }
  if (props.readiness.unpolledServers > 0) {
    return 'unpolled'
  }
  if (props.readiness.servers === 0) {
    return 'no-servers'
  }
  return 'ok'
})

const run = async (action: 'enable-polling' | 'rescan') => {
  busy.value = true
  message.value = ''
  try {
    const result = await props.runAction(action)
    if (typeof result === 'string') {
      message.value = result
    } else if (action === 'rescan') {
      message.value = 'Rescan requested; the poller picks the servers up once provisioning finishes.'
    }
  } finally {
    busy.value = false
  }
}
</script>

<style lang="scss" scoped>
.banner {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 1rem;
  padding: 0.75rem 1rem;
  border-radius: 6px;
  border-left: 4px solid var(--p-orange-500, #ef6c00);
  background: color-mix(in srgb, var(--p-orange-500, #ef6c00) 12%, transparent);

  &.unpolled,
  &.no-servers {
    border-left-color: var(--p-blue-500, #1976d2);
    background: color-mix(in srgb, var(--p-blue-500, #1976d2) 10%, transparent);
  }
}

.text {
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
  font-size: 0.9rem;
}

.message {
  color: var(--p-text-muted-color);
}

.actions {
  flex: 0 0 auto;
}

.link {
  white-space: nowrap;
}
</style>

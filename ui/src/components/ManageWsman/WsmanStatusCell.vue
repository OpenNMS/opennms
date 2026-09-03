<template>
  <span v-if="!bucket" class="unknown" data-test="status-unknown">—</span>
  <span v-else class="status" :title="title" data-test="status-cell">
    <OnmsTag :value="`${bucket.responding} / ${bucket.servers}`" :severity="severity" />
  </span>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { OnmsTag, type OnmsTagSeverity } from '@opennms/onms-ui'
import { WsmanStatusBucket } from '@/types/wsmanAdmin'

// "responding / servers" from the poller's view; null when the status could
// not be read, which must not look like "no servers"
const props = defineProps<{
  bucket: WsmanStatusBucket | null | undefined
}>()

const severity = computed<OnmsTagSeverity>(() => {
  if (!props.bucket || props.bucket.servers === 0) {
    return 'secondary'
  }
  if (props.bucket.down === 0) {
    return 'success'
  }
  return props.bucket.responding === 0 ? 'danger' : 'warn'
})

const title = computed(() => {
  if (!props.bucket) {
    return ''
  }
  const last = props.bucket.lastResponse ? new Date(props.bucket.lastResponse).toLocaleString() : 'never'
  return `${props.bucket.servers} server(s) with the WS-Man service; ${props.bucket.responding} responding, ${props.bucket.down} down; last response ${last}`
})
</script>

<style lang="scss" scoped>
.unknown {
  color: var(--p-text-muted-color);
}
</style>

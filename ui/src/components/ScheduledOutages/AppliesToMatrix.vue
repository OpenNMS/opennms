<template>
  <div class="applies-to" data-test="applies-to">
    <h3 class="section-title">Applies To</h3>

    <div class="subsystem">
      <div class="subsystem-title">Notifications</div>
      <label class="check-row">
        <OnmsCheckbox
          :modelValue="notifications"
          data-test="applies-notifications"
          @update:modelValue="emit('update:notifications', $event)"
        />
        <span>All Notifications</span>
      </label>
    </div>

    <div v-for="group in groups" :key="group.subsystem" class="subsystem">
      <div class="subsystem-header">
        <span class="subsystem-title">{{ group.label }}</span>
        <span class="bulk-actions">
          <a href="#" data-test="select-all" @click.prevent="emit('setAll', group.subsystem, true)">Select All</a>
          <a href="#" data-test="unselect-all" @click.prevent="emit('setAll', group.subsystem, false)">Unselect All</a>
        </span>
      </div>
      <p v-if="!group.packages.length" class="none">No packages configured.</p>
      <label
        v-for="pkg in group.packages"
        :key="pkg.name"
        class="check-row"
      >
        <OnmsCheckbox
          :modelValue="pkg.applied"
          :data-test="`applies-${group.subsystem}`"
          @update:modelValue="emit('togglePackage', group.subsystem, pkg.name, $event)"
        />
        <span>{{ pkg.name }}</span>
      </label>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { OnmsCheckbox } from '@opennms/onms-ui'
import { PackageRef } from '@/types/scheduledOutage'

export type Subsystem = 'pollerd' | 'threshd' | 'collectd'

const props = defineProps<{
  notifications: boolean
  pollers: PackageRef[]
  thresholders: PackageRef[]
  collectors: PackageRef[]
}>()

const emit = defineEmits<{
  'update:notifications': [value: boolean]
  togglePackage: [subsystem: Subsystem, name: string, value: boolean]
  setAll: [subsystem: Subsystem, value: boolean]
}>()

const groups = computed(() => [
  { subsystem: 'pollerd' as Subsystem, label: 'Status Polling', packages: props.pollers },
  { subsystem: 'threshd' as Subsystem, label: 'Threshold Checking', packages: props.thresholders },
  { subsystem: 'collectd' as Subsystem, label: 'Data Collection', packages: props.collectors }
])
</script>

<style scoped lang="scss">
.applies-to {
  .section-title {
    margin: 0 0 0.75rem 0;
  }

  .subsystem {
    margin-bottom: 1rem;
  }

  .subsystem-header {
    display: flex;
    align-items: baseline;
    gap: 1rem;
    margin-bottom: 0.25rem;
  }

  .subsystem-title {
    font-weight: 600;
  }

  .bulk-actions {
    display: flex;
    gap: 0.75rem;
    font-size: 0.85rem;
  }

  .check-row {
    display: flex;
    align-items: center;
    gap: 0.5rem;
    padding: 0.15rem 0 0.15rem 1rem;
    cursor: pointer;
  }

  .none {
    margin: 0 0 0 1rem;
    color: var(--p-text-muted-color);
  }
}
</style>

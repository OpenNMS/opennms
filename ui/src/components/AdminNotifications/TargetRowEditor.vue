<template>
  <div class="target-row">
    <FormField
      label="Target Type"
      :for="`target-type-${row.key}`"
    >
      <OnmsSelect
        v-model="row.type"
        :inputId="`target-type-${row.key}`"
        :options="typeOptions"
        optionLabel="label"
        optionValue="value"
        optionDisabled="disabled"
        data-test="target-type-select"
        @update:modelValue="onTypeChange"
      />
    </FormField>

    <FormField
      class="target-name"
      :label="nameLabel"
      :for="`target-name-${row.key}`"
    >
      <OnmsSelect
        v-if="row.type === 'user'"
        v-model="row.name"
        :inputId="`target-name-${row.key}`"
        :options="users"
        filter
        data-test="target-user-select"
      />
      <OnmsSelect
        v-else-if="row.type === 'group'"
        v-model="row.name"
        :inputId="`target-name-${row.key}`"
        :options="groups"
        filter
        data-test="target-group-select"
      />
      <OnmsSelect
        v-else-if="row.type === 'role'"
        v-model="row.name"
        :inputId="`target-name-${row.key}`"
        :options="roles"
        filter
        data-test="target-role-select"
      />
      <OnmsInputText
        v-else
        :id="`target-name-${row.key}`"
        v-model="row.name"
        :placeholder="row.type === 'email' ? 'someone@example.com' : ''"
        data-test="target-name-input"
      />
    </FormField>

    <FormField
      class="target-methods"
      label="Notification Methods"
      :for="`target-methods-${row.key}`"
    >
      <OnmsMultiSelect
        v-model="row.commands"
        :inputId="`target-methods-${row.key}`"
        :options="rowMethodOptions"
        optionLabel="label"
        optionValue="value"
        optionDisabled="disabled"
        display="chip"
        :showToggleAll="false"
        data-test="target-methods-select"
      />
    </FormField>

    <FormField
      class="target-interval"
      label="Interval"
      :for="`target-interval-${row.key}`"
    >
      <template #label-suffix>
        <HelpBadge :content="intervalHelp" ariaLabel="Interval help" />
      </template>
      <OnmsInputText
        :id="`target-interval-${row.key}`"
        v-model="row.interval"
        placeholder="0s"
        data-test="target-interval-input"
      />
    </FormField>

    <OnmsIconButton
      v-if="removable"
      severity="danger"
      :icon="Cancel"
      aria-label="Remove target"
      data-test="remove-target-button"
      @click="emit('remove')"
    />
  </div>
</template>

<script lang="ts">
// Shared row model between the editor dialog and this row component.
export interface TargetRow {
  key: number
  type: 'user' | 'group' | 'email' | 'role'
  name: string
  commands: string[]
  interval?: string
  autoNotify?: string
}

export interface MethodOption {
  value: string
  label: string
  disabled: boolean
}
</script>

<script setup lang="ts">
import { computed } from 'vue'

import { OnmsIconButton, OnmsInputText, OnmsMultiSelect, OnmsSelect } from '@opennms/onms-ui'

import Cancel from '@/components/icons/navigation/Cancel.vue'
import FormField from '@/components/Common/FormField.vue'
import HelpBadge from '@/components/Common/HelpBadge.vue'

// row is a model (the parent shares the object and reads the edited fields back);
// using defineModel keeps that two-way flow without mutating a plain prop.
const row = defineModel<TargetRow>('row', { required: true })

const props = defineProps<{
  users: string[]
  groups: string[]
  roles: string[]
  methodOptions: MethodOption[]
  removable: boolean
}>()

const emit = defineEmits(['remove'])

const typeOptions = [
  { label: 'User', value: 'user' },
  { label: 'Group', value: 'group' },
  { label: 'Email Address', value: 'email' },
  { label: 'On-Call Role', value: 'role' }
]

const intervalHelp = 'For a group or role target, how long to wait between notifying each successive member (e.g. 30s), so they are paged in sequence rather than all at once. Leave blank to notify everyone immediately.'

// Browser notifications are delivered to logged-in web sessions by user id.
// Group and role targets resolve to users at send time, so only raw email
// address targets can never receive them.
const rowMethodOptions = computed<MethodOption[]>(() => {
  if (row.value.type !== 'email') {
    return props.methodOptions
  }
  return props.methodOptions.map(option =>
    option.value === 'browser' ? { ...option, label: `${option.label} — not for email targets`, disabled: true } : option
  )
})

const nameLabel = computed(() => {
  switch (row.value.type) {
    case 'user':
      return 'User'
    case 'group':
      return 'Group'
    case 'email':
      return 'Email Address'
    default:
      return 'Role'
  }
})

const onTypeChange = () => {
  row.value.name = ''
}
</script>

<style lang="scss" scoped>
.target-row {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  flex-wrap: wrap;
  margin-bottom: 0.5rem;

  :deep(.p-select) {
    min-width: 150px;
  }

  .target-name {
    :deep(.p-select),
    :deep(input) {
      min-width: 200px;
    }
  }

  .target-methods {
    flex: 1;
    min-width: 240px;

    :deep(.p-multiselect) {
      width: 100%;
    }
  }

  .target-interval {
    :deep(input) {
      width: 90px;
    }
  }
}
</style>

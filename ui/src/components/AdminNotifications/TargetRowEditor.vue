<template>
  <div class="target-row">
    <IftaLabel>
      <Select
        v-model="row.type"
        :labelId="`target-type-${row.key}`"
        :options="typeOptions"
        optionLabel="label"
        optionValue="value"
        optionDisabled="disabled"
        data-test="target-type-select"
        @update:modelValue="onTypeChange"
      />
      <label :for="`target-type-${row.key}`">Target Type</label>
    </IftaLabel>

    <IftaLabel class="target-name">
      <Select
        v-if="row.type === 'user'"
        v-model="row.name"
        :labelId="`target-name-${row.key}`"
        :options="users"
        filter
        data-test="target-user-select"
      />
      <Select
        v-else-if="row.type === 'group'"
        v-model="row.name"
        :labelId="`target-name-${row.key}`"
        :options="groups"
        filter
        data-test="target-group-select"
      />
      <Select
        v-else-if="row.type === 'role'"
        v-model="row.name"
        :labelId="`target-name-${row.key}`"
        :options="roles"
        filter
        data-test="target-role-select"
      />
      <InputText
        v-else
        :id="`target-name-${row.key}`"
        v-model="row.name"
        :placeholder="row.type === 'email' ? 'someone@example.com' : ''"
        data-test="target-name-input"
      />
      <label :for="`target-name-${row.key}`">{{ nameLabel }}</label>
    </IftaLabel>

    <IftaLabel class="target-methods">
      <MultiSelect
        v-model="row.commands"
        :labelId="`target-methods-${row.key}`"
        :options="rowMethodOptions"
        optionLabel="label"
        optionValue="value"
        optionDisabled="disabled"
        display="chip"
        :showToggleAll="false"
        data-test="target-methods-select"
      />
      <label :for="`target-methods-${row.key}`">Notification Methods</label>
    </IftaLabel>

    <IftaLabel class="target-interval">
      <InputText
        :id="`target-interval-${row.key}`"
        v-model="row.interval"
        placeholder="0s"
        data-test="target-interval-input"
      />
      <label :for="`target-interval-${row.key}`">Interval</label>
    </IftaLabel>

    <Button
      v-if="removable"
      text
      rounded
      icon="pi pi-times"
      severity="danger"
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

import Button from 'primevue/button'
import IftaLabel from 'primevue/iftalabel'
import InputText from 'primevue/inputtext'
import MultiSelect from 'primevue/multiselect'
import Select from 'primevue/select'

const props = defineProps<{
  row: TargetRow
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

// Browser notifications are delivered to logged-in web sessions by user id.
// Group and role targets resolve to users at send time, so only raw email
// address targets can never receive them.
const rowMethodOptions = computed<MethodOption[]>(() => {
  if (props.row.type !== 'email') {
    return props.methodOptions
  }
  return props.methodOptions.map((option) =>
    option.value === 'browser' ? { ...option, label: `${option.label} — not for email targets`, disabled: true } : option
  )
})

const nameLabel = computed(() => {
  switch (props.row.type) {
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
  props.row.name = ''
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

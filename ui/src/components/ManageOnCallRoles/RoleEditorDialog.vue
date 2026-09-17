<template>
  <OnmsDialog
    :visible="visible"
    modal
    :header="isEditing ? `Edit On-Call Role: ${originalName}` : 'Add New On-Call Role'"
    class="role-editor-dialog"
    width="min(520px, 95vw)"
    data-test="role-editor-dialog"
    @update:visible="(value: boolean) => emit('update:visible', value)"
  >
    <div class="form-column">
      <div
        v-if="errorText"
        class="dialog-error"
        role="alert"
        data-test="dialog-error"
      >{{ errorText }}</div>
      <FormField
        v-if="!isEditing"
        label="Role Name"
        for="role-editor-name"
        required
      >
        <OnmsInputText
          id="role-editor-name"
          v-model="name"
          :invalid="!!nameProblem"
          fluid
          data-test="role-name-input"
        />
        <small
          v-if="nameProblem"
          class="field-error"
          data-test="name-error"
        >{{ nameProblem }}</small>
      </FormField>
      <FormField
        label="Membership Group"
        for="role-editor-group"
        required
      >
        <OnmsSelect
          v-model="membershipGroup"
          inputId="role-editor-group"
          :options="groupOptions"
          filter
          fluid
          data-test="membership-group-select"
        />
        <small class="hint">Scheduled users are chosen from this group's members.</small>
      </FormField>
      <FormField
        label="Supervisor"
        for="role-editor-supervisor"
        required
      >
        <OnmsSelect
          v-model="supervisor"
          inputId="role-editor-supervisor"
          :options="store.supervisorCandidates"
          filter
          fluid
          data-test="supervisor-select"
        />
        <small class="hint">On call whenever nobody is scheduled.</small>
      </FormField>
      <FormField label="Description" for="role-editor-description">
        <OnmsInputText
          id="role-editor-description"
          v-model="description"
          fluid
          data-test="role-description-input"
        />
      </FormField>
    </div>

    <template #footer>
      <OnmsButton
        variant="text"
        label="Cancel"
        data-test="cancel-button"
        @click="emit('update:visible', false)"
      />
      <OnmsButton
        :label="isEditing ? 'Save Role' : 'Add Role'"
        :disabled="!isValid || saving"
        data-test="save-button"
        @click="save"
      />
    </template>
  </OnmsDialog>
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue'

import { OnmsButton, OnmsDialog, OnmsInputText, OnmsSelect } from '@opennms/onms-ui'

import FormField from '@/components/Common/FormField.vue'
import { validateAdminName } from '@/lib/adminValidation'
import { useOnCallRoleAdminStore } from '@/stores/onCallRoleAdminStore'
import { OnCallRole } from '@/types/onCallRoleAdmin'

const props = defineProps<{
  visible: boolean
  role: OnCallRole | null
}>()

const emit = defineEmits(['update:visible'])

const store = useOnCallRoleAdminStore()

const name = ref('')
const membershipGroup = ref<string | null>(null)
const supervisor = ref<string | null>(null)
const description = ref('')
const saving = ref(false)
const errorText = ref('')

const isEditing = computed(() => props.role !== null)
const originalName = computed(() => props.role?.name ?? '')

const groupOptions = computed(() => Object.keys(store.groupMembers))

const nameProblem = computed(() => (isEditing.value ? null : validateAdminName(name.value, 'role name')))

const isValid = computed(() =>
  (isEditing.value || !!name.value.trim()) && !nameProblem.value && !!membershipGroup.value && !!supervisor.value)

watch(
  () => props.visible,
  (isVisible) => {
    if (!isVisible) {
      return
    }
    errorText.value = ''
    if (props.role) {
      name.value = props.role.name
      membershipGroup.value = props.role['membership-group'] ?? null
      supervisor.value = props.role.supervisor ?? null
      description.value = props.role.description ?? ''
    } else {
      name.value = ''
      membershipGroup.value = null
      supervisor.value = null
      description.value = ''
    }
  }
)

const save = async () => {
  saving.value = true
  try {
    // schedules are deliberately omitted: the server preserves them
    const payload: OnCallRole = {
      name: isEditing.value ? originalName.value : name.value.trim(),
      'membership-group': membershipGroup.value ?? undefined,
      supervisor: supervisor.value ?? undefined,
      description: description.value.trim()
    }
    const error = isEditing.value ? await store.updateRole(payload) : await store.createRole(payload)
    if (error === null) {
      emit('update:visible', false)
    } else {
      errorText.value = error
    }
  } finally {
    saving.value = false
  }
}
</script>

<style lang="scss" scoped>
.form-column {
  display: flex;
  flex-direction: column;
  gap: 1rem;
  padding-top: 0.5rem;

  :deep(input),
  :deep(.p-select) {
    width: 100%;
  }
}

.dialog-error {
  padding: 0.5rem 0.75rem;
  border-radius: 6px;
  border: 1px solid var(--p-red-200, #fecaca);
  background: var(--p-red-50, #fef2f2);
  color: var(--p-red-700, #b91c1c);
  font-size: 0.9rem;
}

.field-error {
  display: block;
  margin-top: 0.25rem;
  color: var(--p-red-500, #e24c4c);
}

.hint {
  display: block;
  margin-top: 0.25rem;
  color: var(--p-text-muted-color);
}
</style>

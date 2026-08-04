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
      <Message
        v-if="errorText"
        severity="error"
        :closable="false"
        data-test="dialog-error"
      >{{ errorText }}</Message>
      <FormField v-if="!isEditing">
        <IftaLabel>
          <OnmsInputText
            id="role-editor-name"
            v-model="name"
            :invalid="!!nameProblem"
            data-test="role-name-input"
          />
          <label for="role-editor-name">Role Name *</label>
        </IftaLabel>
        <small
          v-if="nameProblem"
          class="field-error"
          data-test="name-error"
        >{{ nameProblem }}</small>
      </FormField>
      <FormField>
        <IftaLabel>
          <OnmsSelect
            v-model="membershipGroup"
            labelId="role-editor-group"
            :options="groupOptions"
            filter
            data-test="membership-group-select"
          />
          <label for="role-editor-group">Membership Group *</label>
        </IftaLabel>
        <small class="hint">Scheduled users are chosen from this group's members.</small>
      </FormField>
      <FormField>
        <IftaLabel>
          <OnmsSelect
            v-model="supervisor"
            labelId="role-editor-supervisor"
            :options="store.supervisorCandidates"
            filter
            data-test="supervisor-select"
          />
          <label for="role-editor-supervisor">Supervisor *</label>
        </IftaLabel>
        <small class="hint">On call whenever nobody is scheduled.</small>
      </FormField>
      <FormField>
        <IftaLabel>
          <OnmsInputText
            id="role-editor-description"
            v-model="description"
            data-test="role-description-input"
          />
          <label for="role-editor-description">Description</label>
        </IftaLabel>
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

import IftaLabel from 'primevue/iftalabel'
import Message from 'primevue/message'
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

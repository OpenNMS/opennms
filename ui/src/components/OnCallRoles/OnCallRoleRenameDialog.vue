<template>
  <OnmsDialog
    :visible="visible"
    modal
    :header="`Rename On-Call Role: ${roleName}`"
    class="oncall-role-rename-dialog"
    width="min(420px, 95vw)"
    data-test="oncall-role-rename-dialog"
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
        label="New On-Call Role Name"
        for="oncall-role-rename-new-name"
        required
        :error="nameProblem || undefined"
        hint="Destination paths that target the role by name do not follow the rename."
      >
        <OnmsInputText
          id="oncall-role-rename-new-name"
          v-model="newName"
          :invalid="!!nameProblem"
          data-test="new-name-input"
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
        label="Rename"
        :disabled="!newName || !!nameProblem || newName === roleName || saving"
        data-test="save-button"
        @click="save"
      />
    </template>
  </OnmsDialog>
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue'

import { OnmsButton, OnmsDialog, OnmsInputText } from '@opennms/onms-ui'

import FormField from '@/components/Common/FormField.vue'
import useActionFeedback from '@/composables/useActionFeedback'
import useAdminWriteFeedback from '@/composables/useAdminWriteFeedback'
import { validateOnCallRoleName } from '@/lib/adminValidation'
import { useOnCallRoleAdminStore } from '@/stores/onCallRoleAdminStore'

const props = defineProps<{
  visible: boolean
  roleName: string
  version: number
}>()

const emit = defineEmits(['update:visible'])

const store = useOnCallRoleAdminStore()
const { withSpinner } = useActionFeedback()
const { reportWrite } = useAdminWriteFeedback()

const newName = ref('')
const saving = ref(false)
const errorText = ref('')

// nothing typed yet is not a problem to show; Rename stays disabled until a name is typed
const nameProblem = computed(() => newName.value ? validateOnCallRoleName(newName.value) : null)

watch(
  () => props.visible,
  (isVisible) => {
    if (isVisible) {
      newName.value = ''
      errorText.value = ''
    }
  }
)

const save = async () => {
  saving.value = true
  try {
    // not trimmed: the server does not trim, and a padded name is flagged above
    const target = newName.value
    const result = await withSpinner(() => store.renameRole(props.roleName, target, props.version))
    reportWrite(result, `On-call role '${props.roleName}' renamed to '${target}'.`, {
      close: () => emit('update:visible', false),
      banner: (msg) => {
        errorText.value = msg
      }
    })
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

  :deep(input) {
    width: 100%;
  }
}

.dialog-error {
  padding: 0.5rem 0.75rem;
  border-radius: 4px;
  border-left: 3px solid var(--p-red-500, #ef4444);
  background: color-mix(in srgb, var(--p-red-500, #ef4444) 10%, transparent);
  color: var(--p-red-600, #dc2626);
  font-size: 0.9rem;
}
</style>

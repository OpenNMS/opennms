<template>
  <OnmsDialog
    :visible="visible"
    modal
    :header="isEditing ? `Edit User: ${originalUserId}` : 'Add New User'"
    class="user-editor-dialog"
    :style="{ width: '640px', maxWidth: '95vw' }"
    data-test="user-editor-dialog"
    @update:visible="(value: boolean) => emit('update:visible', value)"
  >
    <div class="form-grid">
      <Message
        v-if="errorText"
        severity="error"
        :closable="false"
        class="full-width"
        data-test="dialog-error"
      >{{ errorText }}</Message>
      <FormField v-if="!isEditing">
        <IftaLabel>
          <OnmsInputText
            id="user-editor-id"
            v-model="form.userId"
            :invalid="!!userIdProblem"
            data-test="user-id-input"
          />
          <label for="user-editor-id">User ID *</label>
        </IftaLabel>
        <small
          v-if="userIdProblem"
          class="field-error"
          data-test="user-id-error"
        >{{ userIdProblem }}</small>
      </FormField>
      <FormField v-if="!isEditing">
        <IftaLabel>
          <OnmsPassword
            v-model="form.password"
            inputId="user-editor-password"
            :feedback="false"
            toggleMask
            fluid
            data-test="password-input"
          />
          <label for="user-editor-password">Password *</label>
        </IftaLabel>
      </FormField>
      <FormField>
        <IftaLabel>
          <OnmsInputText
            id="user-editor-fullname"
            v-model="form.fullName"
            data-test="full-name-input"
          />
          <label for="user-editor-fullname">Full Name</label>
        </IftaLabel>
      </FormField>
      <FormField>
        <IftaLabel>
          <OnmsInputText
            id="user-editor-comments"
            v-model="form.comments"
            :invalid="!!commentsProblem"
            data-test="comments-input"
          />
          <label for="user-editor-comments">Comments</label>
        </IftaLabel>
        <small
          v-if="commentsProblem"
          class="field-error"
          data-test="comments-error"
        >{{ commentsProblem }}</small>
      </FormField>
      <FormField>
        <IftaLabel>
          <OnmsInputText
            id="user-editor-email"
            v-model="form.email"
            :invalid="!!emailProblem"
            data-test="email-input"
          />
          <label for="user-editor-email">Email</label>
        </IftaLabel>
        <small
          v-if="emailProblem"
          class="field-error"
          data-test="email-error"
        >{{ emailProblem }}</small>
      </FormField>
      <FormField>
        <IftaLabel>
          <OnmsInputText
            id="user-editor-pager-email"
            v-model="form.pagerEmail"
            :invalid="!!pagerEmailProblem"
            data-test="pager-email-input"
          />
          <label for="user-editor-pager-email">Pager Email</label>
        </IftaLabel>
        <small
          v-if="pagerEmailProblem"
          class="field-error"
          data-test="pager-email-error"
        >{{ pagerEmailProblem }}</small>
      </FormField>
      <FormField class="full-width">
        <IftaLabel>
          <OnmsMultiSelect
            v-model="form.roles"
            labelId="user-editor-roles"
            :options="store.availableRoles"
            display="chip"
            :showToggleAll="false"
            filter
            fluid
            data-test="roles-select"
          />
          <label for="user-editor-roles">Security Roles</label>
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
        :label="isEditing ? 'Save User' : 'Add User'"
        :disabled="!isValid || saving"
        data-test="save-button"
        @click="save"
      />
    </template>
  </OnmsDialog>
</template>

<script setup lang="ts">
import { computed, reactive, ref, watch } from 'vue'

import IftaLabel from 'primevue/iftalabel'
import Message from 'primevue/message'
import { OnmsButton, OnmsDialog, OnmsInputText, OnmsMultiSelect, OnmsPassword } from '@opennms/onms-ui'

import FormField from '@/components/Common/FormField.vue'
import { validateAdminComments, validateAdminName, validateEmailShape } from '@/lib/adminValidation'
import { useUserAdminStore } from '@/stores/userAdminStore'
import { ManagedUser } from '@/types/userAdmin'

const props = defineProps<{
  visible: boolean
  user: ManagedUser | null
}>()

const emit = defineEmits(['update:visible'])

const store = useUserAdminStore()

const form = reactive({
  userId: '',
  password: '',
  fullName: '',
  comments: '',
  email: '',
  pagerEmail: '',
  roles: [] as string[]
})

const saving = ref(false)
const errorText = ref('')

const isEditing = computed(() => props.user !== null)
const originalUserId = computed(() => props.user?.['user-id'] ?? '')

const userIdProblem = computed(() => (isEditing.value ? null : validateAdminName(form.userId, 'user-id')))

// pre-existing values are never flagged (hand-edited users.xml may hold
// forms these checks don't model); only changed input is validated
const changedOnly = (value: string, original: string | undefined, problem: string | null) =>
  value.trim() === (original ?? '').trim() ? null : problem

const emailProblem = computed(() =>
  changedOnly(form.email, props.user?.email ?? '', validateEmailShape(form.email, 'email')))
const pagerEmailProblem = computed(() =>
  changedOnly(form.pagerEmail, props.user?.['pager-email'] ?? '', validateEmailShape(form.pagerEmail, 'pager email')))
const commentsProblem = computed(() =>
  changedOnly(form.comments, props.user?.['user-comments'] ?? '', validateAdminComments(form.comments)))

const isValid = computed(() => {
  if (userIdProblem.value || emailProblem.value || pagerEmailProblem.value || commentsProblem.value) {
    return false
  }
  if (isEditing.value) {
    return true
  }
  return !!form.userId.trim() && !!form.password
})

watch(
  () => props.visible,
  (isVisible) => {
    if (!isVisible) {
      return
    }
    errorText.value = ''
    if (props.user) {
      Object.assign(form, {
        userId: props.user['user-id'],
        password: '',
        fullName: props.user['full-name'] ?? '',
        comments: props.user['user-comments'] ?? '',
        email: props.user.email ?? '',
        pagerEmail: props.user['pager-email'] ?? '',
        roles: [...(props.user.role ?? [])]
      })
    } else {
      Object.assign(form, { userId: '', password: '', fullName: '', comments: '', email: '', pagerEmail: '', roles: [] })
    }
  }
)

const save = async () => {
  saving.value = true
  try {
    // spread the original so fields this form doesn't expose (duty schedules,
    // tui-pin, time-zone-id) round-trip untouched; other contact types (XMPP
    // among them) are preserved server-side.
    const base = props.user ?? {}
    const payload: ManagedUser = {
      ...base,
      'user-id': isEditing.value ? originalUserId.value : form.userId.trim(),
      'full-name': form.fullName.trim(),
      'user-comments': form.comments.trim(),
      email: form.email.trim(),
      'pager-email': form.pagerEmail.trim(),
      role: [...form.roles]
    }
    const error = isEditing.value
      ? await store.updateUser(payload)
      : await store.createUser({ ...payload, password: form.password })
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
.form-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 1rem;
  padding-top: 0.5rem;

  .full-width {
    grid-column: 1 / -1;
  }

  :deep(input),
  :deep(.p-password),
  :deep(.p-multiselect) {
    width: 100%;
  }
}

.field-error {
  display: block;
  margin-top: 0.25rem;
  color: var(--p-red-500, #e24c4c);
}
</style>

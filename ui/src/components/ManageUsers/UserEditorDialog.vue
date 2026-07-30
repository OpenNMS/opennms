<template>
  <Dialog
    :visible="visible"
    modal
    :header="isEditing ? `Edit User: ${originalUserId}` : 'Add New User'"
    class="user-editor-dialog"
    :style="{ width: '640px', maxWidth: '95vw' }"
    data-test="user-editor-dialog"
    @update:visible="(value: boolean) => emit('update:visible', value)"
  >
    <div class="form-grid">
      <FormField v-if="!isEditing">
        <IftaLabel>
          <InputText
            id="user-editor-id"
            v-model="form.userId"
            data-test="user-id-input"
          />
          <label for="user-editor-id">User ID *</label>
        </IftaLabel>
      </FormField>
      <FormField v-if="!isEditing">
        <IftaLabel>
          <Password
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
          <InputText
            id="user-editor-fullname"
            v-model="form.fullName"
            data-test="full-name-input"
          />
          <label for="user-editor-fullname">Full Name</label>
        </IftaLabel>
      </FormField>
      <FormField>
        <IftaLabel>
          <InputText
            id="user-editor-comments"
            v-model="form.comments"
            data-test="comments-input"
          />
          <label for="user-editor-comments">Comments</label>
        </IftaLabel>
      </FormField>
      <FormField>
        <IftaLabel>
          <InputText
            id="user-editor-email"
            v-model="form.email"
            data-test="email-input"
          />
          <label for="user-editor-email">Email</label>
        </IftaLabel>
      </FormField>
      <FormField>
        <IftaLabel>
          <InputText
            id="user-editor-pager-email"
            v-model="form.pagerEmail"
            data-test="pager-email-input"
          />
          <label for="user-editor-pager-email">Pager Email</label>
        </IftaLabel>
      </FormField>
      <FormField class="full-width">
        <IftaLabel>
          <MultiSelect
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
      <Button
        text
        label="Cancel"
        data-test="cancel-button"
        @click="emit('update:visible', false)"
      />
      <Button
        :label="isEditing ? 'Save User' : 'Add User'"
        :disabled="!isValid || saving"
        data-test="save-button"
        @click="save"
      />
    </template>
  </Dialog>
</template>

<script setup lang="ts">
import { computed, reactive, ref, watch } from 'vue'

import Button from 'primevue/button'
import Dialog from 'primevue/dialog'
import IftaLabel from 'primevue/iftalabel'
import InputText from 'primevue/inputtext'
import MultiSelect from 'primevue/multiselect'
import Password from 'primevue/password'

import FormField from '@/components/Common/FormField.vue'
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

const isEditing = computed(() => props.user !== null)
const originalUserId = computed(() => props.user?.['user-id'] ?? '')

const isValid = computed(() => {
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
      'full-name': form.fullName.trim() || undefined,
      'user-comments': form.comments.trim() || undefined,
      email: form.email.trim() || undefined,
      'pager-email': form.pagerEmail.trim() || undefined,
      role: [...form.roles]
    }
    const ok = isEditing.value
      ? await store.updateUser(payload)
      : await store.createUser({ ...payload, password: form.password })
    if (ok) {
      emit('update:visible', false)
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
</style>

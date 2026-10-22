<template>
  <OnmsDialog
    :visible="visible"
    modal
    :header="isEditing ? `Edit User: ${originalUserId}` : 'Add New User'"
    class="user-editor-dialog"
    width="min(640px, 95vw)"
    data-test="user-editor-dialog"
    @update:visible="(value: boolean) => emit('update:visible', value)"
  >
    <div class="form-grid">
      <div
        v-if="errorText"
        class="dialog-error full-width"
        role="alert"
        data-test="dialog-error"
      >{{ errorText }}</div>
      <FormField
        v-if="!isEditing"
        label="User ID"
        for="user-editor-id"
        required
        :error="userIdProblem || undefined"
      >
        <OnmsInputText
          id="user-editor-id"
          v-model="form.userId"
          :invalid="!!userIdProblem"
          data-test="user-id-input"
        />
      </FormField>
      <FormField
        v-if="!isEditing"
        label="Password"
        for="user-editor-password"
        required
      >
        <OnmsPassword
          v-model="form.password"
          inputId="user-editor-password"
          :feedback="false"
          toggleMask
          fluid
          data-test="password-input"
        />
      </FormField>
      <FormField
        label="Full Name"
        for="user-editor-fullname"
      >
        <OnmsInputText
          id="user-editor-fullname"
          v-model="form.fullName"
          data-test="full-name-input"
        />
      </FormField>
      <FormField
        label="Comments"
        for="user-editor-comments"
        :error="commentsProblem || undefined"
      >
        <OnmsInputText
          id="user-editor-comments"
          v-model="form.comments"
          :invalid="!!commentsProblem"
          data-test="comments-input"
        />
      </FormField>
      <FormField
        label="Email"
        for="user-editor-email"
        :error="emailProblem || undefined"
      >
        <OnmsInputText
          id="user-editor-email"
          v-model="form.email"
          :invalid="!!emailProblem"
          data-test="email-input"
        />
      </FormField>
      <FormField
        label="Pager Email"
        for="user-editor-pager-email"
        :error="pagerEmailProblem || undefined"
      >
        <OnmsInputText
          id="user-editor-pager-email"
          v-model="form.pagerEmail"
          :invalid="!!pagerEmailProblem"
          data-test="pager-email-input"
        />
      </FormField>
      <FormField label="Work Phone" for="user-editor-work-phone">
        <OnmsInputText id="user-editor-work-phone" v-model="form.workPhone" data-test="work-phone-input" />
      </FormField>
      <FormField label="Mobile Phone" for="user-editor-mobile-phone">
        <OnmsInputText id="user-editor-mobile-phone" v-model="form.mobilePhone" data-test="mobile-phone-input" />
      </FormField>
      <FormField label="Home Phone" for="user-editor-home-phone">
        <OnmsInputText id="user-editor-home-phone" v-model="form.homePhone" data-test="home-phone-input" />
      </FormField>
      <FormField label="Telephone PIN" for="user-editor-tui-pin">
        <OnmsInputText id="user-editor-tui-pin" v-model="form.tuiPin" data-test="tui-pin-input" />
      </FormField>
      <FormField label="Numeric Pager Service" for="user-editor-numeric-service">
        <OnmsInputText id="user-editor-numeric-service" v-model="form.numericPagerService" data-test="numeric-service-input" />
      </FormField>
      <FormField label="Numeric Pager PIN" for="user-editor-numeric-pin">
        <OnmsInputText id="user-editor-numeric-pin" v-model="form.numericPagerPin" data-test="numeric-pin-input" />
      </FormField>
      <FormField label="Text Pager Service" for="user-editor-text-service">
        <OnmsInputText id="user-editor-text-service" v-model="form.textPagerService" data-test="text-service-input" />
      </FormField>
      <FormField label="Text Pager PIN" for="user-editor-text-pin">
        <OnmsInputText id="user-editor-text-pin" v-model="form.textPagerPin" data-test="text-pin-input" />
      </FormField>
      <FormField
        class="full-width"
        label="Time Zone"
        for="user-editor-timezone"
      >
        <OnmsSelect
          v-model="form.timeZoneId"
          inputId="user-editor-timezone"
          :options="timeZoneOptions"
          filter
          showClear
          fluid
          data-test="timezone-select"
        />
      </FormField>
      <FormField
        class="full-width"
        label="Duty Schedules"
        hint="Pick the days and the begin/end times for each coverage window."
      >
        <DutyScheduleEditor v-model="form.dutySchedules" />
      </FormField>
      <FormField
        class="full-width"
        label="Security Roles"
        for="user-editor-roles"
      >
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

import { OnmsButton, OnmsDialog, OnmsInputText, OnmsMultiSelect, OnmsPassword, OnmsSelect } from '@opennms/onms-ui'

import DutyScheduleEditor from '@/components/ManageUsers/DutyScheduleEditor.vue'
import FormField from '@/components/Common/FormField.vue'
import { validateAdminComments, validateAdminName, validateEmailShape } from '@/lib/adminValidation'
import { useUserAdminStore } from '@/stores/userAdminStore'
import { ManagedUser } from '@/types/userAdmin'

// IANA zone ids for the time-zone picker; supportedValuesOf is present in the
// app's target browsers, guarded so a missing implementation just yields no list
const timeZoneOptions: string[] = (() => {
  try {
    return (Intl as unknown as { supportedValuesOf(key: string): string[] }).supportedValuesOf('timeZone')
  } catch {
    return []
  }
})()

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
  workPhone: '',
  mobilePhone: '',
  homePhone: '',
  numericPagerService: '',
  numericPagerPin: '',
  textPagerService: '',
  textPagerPin: '',
  tuiPin: '',
  timeZoneId: null as string | null,
  dutySchedules: [] as string[],
  roles: [] as string[]
})

const saving = ref(false)
const errorText = ref('')

const isEditing = computed(() => props.user !== null)
const originalUserId = computed(() => props.user?.userId ?? '')

const userIdProblem = computed(() => (isEditing.value ? null : validateAdminName(form.userId, 'user-id')))

// pre-existing values are never flagged (hand-edited users.xml may hold
// forms these checks don't model); only changed input is validated
const changedOnly = (value: string, original: string | undefined, problem: string | null) =>
  value.trim() === (original ?? '').trim() ? null : problem

const emailProblem = computed(() =>
  changedOnly(form.email, props.user?.email ?? '', validateEmailShape(form.email, 'email')))
const pagerEmailProblem = computed(() =>
  changedOnly(form.pagerEmail, props.user?.pagerEmail ?? '', validateEmailShape(form.pagerEmail, 'pager email')))
const commentsProblem = computed(() =>
  changedOnly(form.comments, props.user?.userComments ?? '', validateAdminComments(form.comments)))

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
        userId: props.user.userId,
        password: '',
        fullName: props.user.fullName ?? '',
        comments: props.user.userComments ?? '',
        email: props.user.email ?? '',
        pagerEmail: props.user.pagerEmail ?? '',
        workPhone: props.user.workPhone ?? '',
        mobilePhone: props.user.mobilePhone ?? '',
        homePhone: props.user.homePhone ?? '',
        numericPagerService: props.user.numericPagerService ?? '',
        numericPagerPin: props.user.numericPagerPin ?? '',
        textPagerService: props.user.textPagerService ?? '',
        textPagerPin: props.user.textPagerPin ?? '',
        tuiPin: props.user.tuiPin ?? '',
        timeZoneId: props.user.timeZoneId ?? null,
        dutySchedules: [...(props.user.dutySchedules ?? [])],
        roles: [...(props.user.roles ?? [])]
      })
    } else {
      Object.assign(form, {
        userId: '', password: '', fullName: '', comments: '', email: '', pagerEmail: '',
        workPhone: '', mobilePhone: '', homePhone: '', numericPagerService: '', numericPagerPin: '',
        textPagerService: '', textPagerPin: '', tuiPin: '', timeZoneId: null, dutySchedules: [], roles: []
      })
    }
  }
)

const save = async () => {
  saving.value = true
  try {
    // spread the original so contact types the form still doesn't expose (XMPP,
    // microblog) round-trip untouched; the server skips re-writing any value
    // that matches what is stored.
    const base = props.user ?? {}
    const payload: ManagedUser = {
      ...base,
      userId: isEditing.value ? originalUserId.value : form.userId.trim(),
      fullName: form.fullName.trim(),
      userComments: form.comments.trim(),
      email: form.email.trim(),
      pagerEmail: form.pagerEmail.trim(),
      workPhone: form.workPhone.trim(),
      mobilePhone: form.mobilePhone.trim(),
      homePhone: form.homePhone.trim(),
      numericPagerService: form.numericPagerService.trim(),
      numericPagerPin: form.numericPagerPin.trim(),
      textPagerService: form.textPagerService.trim(),
      textPagerPin: form.textPagerPin.trim(),
      tuiPin: form.tuiPin.trim(),
      timeZoneId: form.timeZoneId,
      dutySchedules: form.dutySchedules.map(s => s.trim()).filter(Boolean),
      roles: [...form.roles]
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

.dialog-error {
  padding: 0.5rem 0.75rem;
  border-radius: 4px;
  border-left: 3px solid var(--p-red-500, #ef4444);
  background: color-mix(in srgb, var(--p-red-500, #ef4444) 10%, transparent);
  color: var(--p-red-600, #dc2626);
  font-size: 0.9rem;
}
</style>

<template>
  <TableCard
    class="user-editor-panel"
    data-test="user-editor-panel"
  >
    <div class="panel-header">
      <OnmsButton
        variant="text"
        class="back-button"
        data-test="back-button"
        @click="emit('close')"
      >
        <OnmsIcon :icon="ArrowBack" />
        Back
      </OnmsButton>
      <h2 class="card-title">{{ isEditing ? `Edit User: ${originalUserId}` : 'Add New User' }}</h2>
    </div>
    <div class="editor-body">
      <div
        v-if="errorText"
        class="error-banner"
        role="alert"
        data-test="save-error"
      >{{ errorText }}</div>
      <OnmsTabs v-model:value="activeTab" data-test="user-editor-tabs">
        <OnmsTabList>
          <OnmsTab value="user" data-test="tab-user">User</OnmsTab>
          <OnmsTab value="duty" data-test="tab-duty-schedules">Duty Schedules</OnmsTab>
        </OnmsTabList>
        <OnmsTabPanels>
          <OnmsTabPanel value="user">
            <div class="form-grid">
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
          </OnmsTabPanel>
          <OnmsTabPanel value="duty">
            <FormField
              label="Duty Schedules"
              hint="Pick the days and the begin/end times for each coverage window."
            >
              <DutyScheduleEditor v-model="form.dutySchedules" />
            </FormField>
          </OnmsTabPanel>
        </OnmsTabPanels>
      </OnmsTabs>
    </div>
    <div class="panel-footer">
      <OnmsButton
        variant="text"
        label="Cancel"
        data-test="cancel-button"
        @click="emit('close')"
      />
      <OnmsButton
        :label="isEditing ? 'Save User' : 'Add User'"
        :disabled="!isValid || saving"
        data-test="save-button"
        @click="save"
      />
    </div>
  </TableCard>
</template>

<script setup lang="ts">
import { computed, reactive, ref, watch } from 'vue'

import {
  OnmsButton,
  OnmsIcon,
  OnmsInputText,
  OnmsMultiSelect,
  OnmsPassword,
  OnmsSelect,
  OnmsTab,
  OnmsTabList,
  OnmsTabPanel,
  OnmsTabPanels,
  OnmsTabs
} from '@opennms/onms-ui'

import DutyScheduleEditor from '@/components/ManageUsers/DutyScheduleEditor.vue'
import FormField from '@/components/Common/FormField.vue'
import TableCard from '@/components/Common/TableCard.vue'
import ArrowBack from '@opennms/onms-ui/icons/navigation/ArrowBack.vue'
import useActionFeedback from '@/composables/useActionFeedback'
import useAdminWriteFeedback from '@/composables/useAdminWriteFeedback'
import { RESERVED_USER_IDS, validateAdminComments, validateAdminName, validateEmailShape, validateReservedName } from '@/lib/adminValidation'
import { useUserAdminStore } from '@/stores/userAdminStore'
import { ManagedUser, ManagedUserCreate } from '@/types/userAdmin'

// IANA zone ids for the time-zone picker; supportedValuesOf is present in the
// app's target browsers, guarded so a missing implementation just yields no list
const timeZoneOptions: string[] = (() => {
  try {
    return (Intl as unknown as { supportedValuesOf(key: string): string[] }).supportedValuesOf('timeZone')
  } catch {
    return []
  }
})()

// Rendered in place of the table (see ManageUsers); `user` is null when creating.
// Emits `close` on Back, Cancel, a successful save, or a save rejected as stale.
const props = defineProps<{
  user: ManagedUser | null
}>()

const emit = defineEmits<{
  close: []
}>()

const store = useUserAdminStore()
const { withSpinner } = useActionFeedback()
const { reportWrite } = useAdminWriteFeedback()

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

type EditorTab = 'user' | 'duty'
const activeTab = ref<EditorTab>('user')

const isEditing = computed(() => props.user !== null)
const originalUserId = computed(() => props.user?.userId ?? '')

const userIdProblem = computed(() => (isEditing.value ? null : validateAdminName(form.userId, 'user-id') || validateReservedName(form.userId, RESERVED_USER_IDS, 'user-id')))

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
  () => props.user,
  () => {
    errorText.value = ''
    activeTab.value = 'user'
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
  },
  { immediate: true }
)

const save = async () => {
  saving.value = true
  try {
    // spread the original so contact types the form still doesn't expose (XMPP,
    // microblog) round-trip untouched; the server skips re-writing any value
    // that matches what is stored.
    const fields: Omit<ManagedUserCreate, 'password'> = {
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
    const error = await withSpinner(() => props.user
      ? store.updateUser({ ...props.user, ...fields })
      : store.createUser({ ...fields, password: form.password }))
    reportWrite(error, props.user ? `User '${fields.userId}' updated.` : `User '${fields.userId}' created.`, store.reloadError, {
      close: () => emit('close'),
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
.user-editor-panel {
  padding: 25px;
}

.panel-header {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  margin-bottom: 1rem;

  .back-button {
    padding-left: 0;
  }

  .card-title {
    font-size: 1.1rem;
    font-weight: 600;
    margin: 0;
  }
}

.editor-body,
.panel-footer {
  max-width: 1040px;
}

.panel-footer {
  display: flex;
  justify-content: flex-end;
  gap: 0.5rem;
  margin-top: 1.5rem;
}

.error-banner {
  margin-bottom: 1rem;
  padding: 0.6rem 0.85rem;
  border: 1px solid var(--p-red-400, #e57373);
  border-radius: 4px;
  color: var(--p-red-400, #e57373);
  background: color-mix(in srgb, var(--p-red-400, #e57373) 12%, transparent);
}

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

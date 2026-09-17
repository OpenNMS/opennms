<template>
  <div class="onms-row">
    <div class="onms-col-12">
      <BreadCrumbs :items="breadcrumbs" />
    </div>
  </div>

  <div class="user-editor-container">
    <div class="page-header">
      <OnmsButton variant="text" class="back-button" data-test="back-button" @click="goBack">
        <OnmsIcon :icon="ArrowBack" />
        Go Back
      </OnmsButton>
      <h1 class="page-title" data-test="editor-title">
        {{ isEditing ? `Edit User: ${routeUserId}` : 'Create New User' }}
      </h1>
    </div>

    <TableCard class="editor-card">
      <div
        v-if="errorText"
        class="page-error"
        role="alert"
        data-test="editor-error"
      >{{ errorText }}</div>

      <div v-if="notFound" class="not-found" data-test="editor-not-found">
        <OnmsButton variant="outlined" label="Back to Manage Users" @click="goBack" />
      </div>

      <template v-else>
        <OnmsTabs v-model:value="activeTab">
          <OnmsTabList>
            <OnmsTab value="general" data-test="tab-general">General Information</OnmsTab>
            <OnmsTab value="duty" data-test="tab-duty">Duty Schedule</OnmsTab>
            <OnmsTab value="roles" data-test="tab-roles">Roles</OnmsTab>
          </OnmsTabList>
          <OnmsTabPanels>
            <OnmsTabPanel value="general">
              <div class="section-label">Account</div>
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
                <FormField label="Full Name" for="user-editor-fullname">
                  <OnmsInputText id="user-editor-fullname" v-model="form.fullName" data-test="full-name-input" />
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
              </div>

              <div class="section-label">Contact Information</div>
              <div class="form-grid">
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
                <FormField label="Mobile Phone" for="user-editor-mobile-phone">
                  <OnmsInputText id="user-editor-mobile-phone" v-model="form.mobilePhone" data-test="mobile-phone-input" />
                </FormField>
              </div>
              <TogglePanel
                header="Additional Contact Methods"
                :collapsed="moreContactsCollapsed"
                class="more-contacts"
                data-test="more-contacts-panel"
                @update:collapsed="moreContactsCollapsed = $event"
              >
                <div class="form-grid">
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
                </div>
              </TogglePanel>

              <div class="section-label">Time Zone</div>
              <TimeZonePicker v-model="form.timeZoneId" idBase="user-editor-timezone" />
            </OnmsTabPanel>
            <OnmsTabPanel value="duty">
              <DutySchedulesTab v-model="form.dutySchedules" />
            </OnmsTabPanel>
            <OnmsTabPanel value="roles">
              <FormField
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
            </OnmsTabPanel>
          </OnmsTabPanels>
        </OnmsTabs>

        <div class="actions">
          <OnmsButton
            :label="isEditing ? 'Save' : 'Create'"
            :disabled="!isValid || saving"
            data-test="save-button"
            @click="save"
          />
          <OnmsButton
            variant="text"
            label="Cancel"
            data-test="cancel-button"
            @click="goBack"
          />
        </div>
      </template>
    </TableCard>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'

import {
  OnmsButton, OnmsIcon, OnmsInputText, OnmsMultiSelect, OnmsPassword,
  OnmsTab, OnmsTabList, OnmsTabPanel, OnmsTabPanels, OnmsTabs
} from '@opennms/onms-ui'

import ArrowBack from '@opennms/onms-ui/icons/navigation/ArrowBack.vue'
import BreadCrumbs from '@/components/Layout/BreadCrumbs.vue'
import FormField from '@/components/Common/FormField.vue'
import TableCard from '@/components/Common/TableCard.vue'
import TogglePanel from '@/components/Common/TogglePanel.vue'
import DutySchedulesTab from '@/components/ManageUsers/DutySchedulesTab.vue'
import TimeZonePicker from '@/components/ManageUsers/TimeZonePicker.vue'
import { validateAdminComments, validateAdminName, validateEmailShape } from '@/lib/adminValidation'
import { useUserAdminStore } from '@/stores/userAdminStore'
import { useMenuStore } from '@/stores/menuStore'
import { ManagedUser } from '@/types/userAdmin'
import { BreadCrumb } from '@/types'

// ':userId' is 'create' for a new user, per the SNMP source-detail route
const CREATE_MARKER = 'create'

const route = useRoute()
const router = useRouter()
const store = useUserAdminStore()
const menuStore = useMenuStore()

const routeUserId = computed(() => decodeURIComponent(String(route.params.userId ?? '')))
const isEditing = computed(() => routeUserId.value !== CREATE_MARKER)

const homeUrl = computed<string>(() => menuStore.mainMenu.homeUrl)

const breadcrumbs = computed<BreadCrumb[]>(() => [
  { label: 'Home', to: homeUrl.value, isAbsoluteLink: true },
  { label: 'Manage Users', to: '/admin/users' },
  { label: isEditing.value ? routeUserId.value : 'Create New User', to: '#', position: 'last' }
])

const activeTab = ref<string | number>('general')

// rarely-used contact methods start folded away; an existing user who has any
// of them filled must see them, so editing such a user starts expanded
const moreContactsCollapsed = ref(true)

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
const notFound = ref(false)
// the loaded user, so contact types the form doesn't expose (XMPP, microblog)
// round-trip untouched on save
let original: ManagedUser | null = null

const userIdProblem = computed(() => (isEditing.value ? null : validateAdminName(form.userId, 'user-id')))

// pre-existing values are never flagged (hand-edited users.xml may hold
// forms these checks don't model); only changed input is validated
const changedOnly = (value: string, originalValue: string | undefined, problem: string | null) =>
  value.trim() === (originalValue ?? '').trim() ? null : problem

const emailProblem = computed(() =>
  changedOnly(form.email, original?.email ?? '', validateEmailShape(form.email, 'email')))
const pagerEmailProblem = computed(() =>
  changedOnly(form.pagerEmail, original?.pagerEmail ?? '', validateEmailShape(form.pagerEmail, 'pager email')))
const commentsProblem = computed(() =>
  changedOnly(form.comments, original?.userComments ?? '', validateAdminComments(form.comments)))

const isValid = computed(() => {
  if (userIdProblem.value || emailProblem.value || pagerEmailProblem.value || commentsProblem.value) {
    return false
  }
  if (isEditing.value) {
    return true
  }
  return !!form.userId.trim() && !!form.password
})

onMounted(async () => {
  await store.populate()
  if (!isEditing.value) {
    return
  }
  const user = store.users.find((u: ManagedUser) => u.userId === routeUserId.value)
  if (!user) {
    notFound.value = true
    errorText.value = `User "${routeUserId.value}" was not found. It may have been deleted or renamed.`
    return
  }
  original = user
  moreContactsCollapsed.value = ![
    user.pagerEmail, user.workPhone, user.homePhone, user.tuiPin,
    user.numericPagerService, user.numericPagerPin, user.textPagerService, user.textPagerPin
  ].some(value => (value ?? '').trim())
  Object.assign(form, {
    fullName: user.fullName ?? '',
    comments: user.userComments ?? '',
    email: user.email ?? '',
    pagerEmail: user.pagerEmail ?? '',
    workPhone: user.workPhone ?? '',
    mobilePhone: user.mobilePhone ?? '',
    homePhone: user.homePhone ?? '',
    numericPagerService: user.numericPagerService ?? '',
    numericPagerPin: user.numericPagerPin ?? '',
    textPagerService: user.textPagerService ?? '',
    textPagerPin: user.textPagerPin ?? '',
    tuiPin: user.tuiPin ?? '',
    timeZoneId: user.timeZoneId ?? null,
    dutySchedules: [...(user.dutySchedules ?? [])],
    roles: [...(user.roles ?? [])]
  })
})

const save = async () => {
  saving.value = true
  errorText.value = ''
  try {
    const base = original ?? {}
    const payload: ManagedUser = {
      ...base,
      userId: isEditing.value ? routeUserId.value : form.userId.trim(),
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
      goBack()
    } else {
      errorText.value = error
    }
  } finally {
    saving.value = false
  }
}

const goBack = () => {
  router.push({ path: '/admin/users' })
}
</script>

<style lang="scss" scoped>
.user-editor-container {
  display: flex;
  flex-direction: column;
  gap: 1rem;
  padding: 0 2px 2rem 2px;
}

.page-header {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  gap: 0.25rem;
}

.back-button {
  padding-left: 0;
}

.page-title {
  font-size: 1.4rem;
  font-weight: 600;
  margin: 0;
}

.editor-card {
  padding: 25px;
}

.page-error {
  margin-bottom: 1rem;
  padding: 0.5rem 0.75rem;
  border-radius: 4px;
  border-left: 3px solid var(--p-red-500, #ef4444);
  background: color-mix(in srgb, var(--p-red-500, #ef4444) 10%, transparent);
  color: var(--p-red-600, #dc2626);
  font-size: 0.9rem;
}

.section-label {
  font-weight: 600;
  margin: 1rem 0 0.5rem 0;

  &:first-child {
    margin-top: 0;
  }
}

.more-contacts {
  margin-top: 1rem;
}

.form-grid {
  display: grid;
  // as many columns as fit: ~4-5 on a desktop so the whole tab fits one
  // screen, collapsing gracefully so a minimized browser wraps and scrolls
  grid-template-columns: repeat(auto-fit, minmax(16rem, 1fr));
  gap: 0.75rem 1.25rem;
  padding-top: 0.5rem;

  :deep(input),
  :deep(.p-password) {
    width: 100%;
  }
}

.actions {
  display: flex;
  gap: 0.75rem;
  margin-top: 1rem;
}
</style>

///
/// Licensed to The OpenNMS Group, Inc (TOG) under one or more
/// contributor license agreements.  See the LICENSE.md file
/// distributed with this work for additional information
/// regarding copyright ownership.
///
/// TOG licenses this file to You under the GNU Affero General
/// Public License Version 3 (the "License") or (at your option)
/// any later version.  You may not use this file except in
/// compliance with the License.  You may obtain a copy of the
/// License at:
///
///      https://www.gnu.org/licenses/agpl-3.0.txt
///
/// Unless required by applicable law or agreed to in writing,
/// software distributed under the License is distributed on an
/// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
/// either express or implied.  See the License for the specific
/// language governing permissions and limitations under the
/// License.
///
<template>
  <FeatherDialog v-model="isVisible" :labels="dialogLabels" hide-close>
    <!--
      Single wrapper div forces column layout regardless of how FeatherDialog
      lays out its default-slot children internally.
    -->
    <div class="wizard-wrapper">

      <!-- Step bar -->
      <nav
        v-if="steps.length > 1"
        class="step-bar"
        :aria-label="`Step ${currentStepIndex + 1} of ${steps.length}`"
      >
        <template v-for="(step, index) in steps" :key="step.id">
          <div
            class="step-item"
            :class="{
              'step-item--active': index === currentStepIndex,
              'step-item--done': index < currentStepIndex
            }"
            :aria-current="index === currentStepIndex ? 'step' : undefined"
          >
            <div class="step-item__circle">
              <span v-if="index < currentStepIndex" class="step-item__check" aria-hidden="true">✓</span>
              <span v-else aria-hidden="true">{{ index + 1 }}</span>
            </div>
            <span class="step-item__title">{{ step.title }}</span>
          </div>
          <div
            v-if="index < steps.length - 1"
            class="step-bar__connector"
            :class="{ 'step-bar__connector--done': index < currentStepIndex }"
            aria-hidden="true"
          />
        </template>
      </nav>

      <!-- Step: Change Password -->
      <div v-if="currentStep.id === 'change-password'" class="step-content">
        <h3 class="step-title">Change Default Password</h3>
        <p>
          You are using the default <strong>admin</strong> password. Please change it now to
          protect your installation against
          <a href="https://www.cisa.gov/news-events/cybersecurity-advisories/aa22-137a" target="_blank" rel="noopener noreferrer">
            default credential attacks</a>.
        </p>

        <FeatherInput
          v-model="pwForm.currentPassword"
          label="Current Password"
          type="password"
          :error="pwErrors.currentPassword"
        />
        <FeatherInput
          v-model="pwForm.newPassword"
          label="New Password"
          type="password"
          :error="pwErrors.newPassword"
        />
        <FeatherInput
          v-model="pwForm.confirmPassword"
          label="Confirm New Password"
          type="password"
          :error="pwErrors.confirmPassword"
        />

        <p class="legal-text">
          Password must be 12–128 characters and contain at least one digit, one lowercase letter,
          one uppercase letter, and one special character (<code>!@#$%&amp;.*+-</code>).
          Sequences of 6 or more identical characters are not allowed.
        </p>
      </div>

      <!-- Step: Usage Statistics -->
      <div v-else-if="currentStep.id === 'usage-statistics'" class="step-content">
        <h3 class="step-title">Usage Statistics Sharing</h3>
        <p>
          Your OpenNMS instance shares anonymous
          <a href="https://stats.opennms.org" target="_blank" rel="noopener noreferrer">usage statistics</a>
          by default. Our
          <a href="https://www.opennms.com/privacy/" target="_blank" rel="noopener noreferrer">privacy policy</a>
          governs our use of this data to help improve the software.
        </p>
        <p>
          Click <em>Learn More</em> to visit the usage statistics page where you can opt out,
          or click <em>Got It</em> to acknowledge and continue.
        </p>
      </div>

      <!-- Step: Community Sign Up -->
      <div v-else-if="currentStep.id === 'community-signup'" class="step-content">
        <template v-if="!signupSubmitted">
          <h3 class="step-title">Stay Connected</h3>
          <p class="step-description">
            Now that you're part of the OpenNMS community, let's stay in touch!
            Share your info and we'll keep you up to date on the latest from OpenNMS.
            This notice will only display once.
          </p>

          <div class="form-row">
            <FeatherInput
              v-model="form.firstName"
              label="First Name"
              :error="errors.firstName"
              class="form-field"
            />
            <FeatherInput
              v-model="form.lastName"
              label="Last Name"
              :error="errors.lastName"
              class="form-field"
            />
          </div>
          <FeatherInput
            v-model="form.email"
            label="Email Address"
            :error="errors.email"
          />
          <FeatherInput
            v-model="form.company"
            label="Company Name"
            :error="errors.company"
          />

          <p class="legal-text">
            If you consent to us contacting you, please opt in below. We will maintain your data
            until you request us to delete it from our systems. You may opt out of receiving
            communications from us at any time.
          </p>

          <FeatherCheckbox
            @update:modelValue="(val: boolean | undefined) => form.consent = !!val"
            :modelValue="form.consent"
          >
            I agree to receive email communications from OpenNMS
            <span class="required-mark" aria-hidden="true">*</span>
          </FeatherCheckbox>
          <p v-if="errors.consent" class="consent-error" role="alert">{{ errors.consent }}</p>

          <p class="legal-text">
            For more information, check out our
            <a href="https://www.opennms.com/privacy/" target="_blank" rel="noopener noreferrer">Privacy Policy</a>.
            Required fields are marked with <span class="required-mark" aria-hidden="true">*</span>.
          </p>
        </template>

        <div v-else class="success-content">
          <p class="success-title">Thank you!</p>
          <p>
            Welcome to the OpenNMS Community! Feel free to browse our
            <a href="https://www.opennms.com/blog/" target="_blank" rel="noopener noreferrer">blog</a>
            or follow us on our socials:
          </p>
          <p class="social-links">
            <a href="https://www.linkedin.com/company/the-opennms-group/" target="_blank" rel="noopener noreferrer">LinkedIn</a>
            · <a href="https://www.twitter.com/opennms" target="_blank" rel="noopener noreferrer">Twitter</a>
            · <a href="https://www.facebook.com/OpenNMS/" target="_blank" rel="noopener noreferrer">Facebook</a>
            · <a href="https://www.youtube.com/user/opennms" target="_blank" rel="noopener noreferrer">YouTube</a>
            · <a href="https://chat.opennms.com/" target="_blank" rel="noopener noreferrer">Mattermost</a>
            · <a href="https://opennms.discourse.group/" target="_blank" rel="noopener noreferrer">Discourse</a>
          </p>
        </div>
      </div>

    </div><!-- /wizard-wrapper -->

    <!-- Footer actions -->
    <template v-slot:footer>
      <!-- Change Password step -->
      <template v-if="currentStep.id === 'change-password'">
        <FeatherButton text @click="onSkipPasswordChange" :disabled="isBusy">Skip</FeatherButton>
        <FeatherButton primary @click="onChangePassword" :disabled="isBusy">Change Password</FeatherButton>
      </template>

      <!-- Usage Statistics step -->
      <template v-else-if="currentStep.id === 'usage-statistics'">
        <FeatherButton text @click="onLearnMore" :disabled="isBusy">Learn More</FeatherButton>
        <FeatherButton primary @click="onGotIt" :disabled="isBusy">
          {{ isLastStep ? 'Got It' : 'Got It · Next' }}
        </FeatherButton>
      </template>

      <!-- Community Sign Up step -->
      <template v-else-if="currentStep.id === 'community-signup'">
        <template v-if="!signupSubmitted">
          <FeatherButton text @click="onOptOut" :disabled="isBusy">Opt Out</FeatherButton>
          <FeatherButton primary @click="onSignUp" :disabled="isBusy">Sign Up</FeatherButton>
        </template>
        <template v-else>
          <FeatherButton primary @click="closeModal">Close</FeatherButton>
        </template>
      </template>
    </template>
  </FeatherDialog>
</template>

<script setup lang="ts">
import { FeatherDialog } from '@featherds/dialog'
import { FeatherInput } from '@featherds/input'
import { FeatherCheckbox } from '@featherds/checkbox'
import { FeatherButton } from '@featherds/button'
import API from '@/services'
import useSnackbar from '@/composables/useSnackbar'

// Password complexity — mirrors AbstractBasePasswordChangeActionServlet constants
const PASSWORD_REGEX = /^((?=.*\d)(?=.*[a-z])(?=.*[A-Z])(?=.*[!@#$%&.*+\-]).{12,128})$/
const SAME_CHAR_REGEX = /(.)\1{5}/

interface WizardStep {
  id: 'change-password' | 'usage-statistics' | 'community-signup'
  title: string
}

const props = defineProps<{
  showChangePassword: boolean
  showUsageStatistics: boolean
  showCommunitySignup: boolean
}>()

const emit = defineEmits<{
  (e: 'close'): void
}>()

const router = useRouter()
const { showSnackBar } = useSnackbar()

const isVisible = ref(true)
const isBusy = ref(false)
const signupSubmitted = ref(false)

const steps = computed<WizardStep[]>(() => {
  const result: WizardStep[] = []
  if (props.showChangePassword) result.push({ id: 'change-password', title: 'Change Password' })
  if (props.showUsageStatistics) result.push({ id: 'usage-statistics', title: 'Usage Statistics' })
  if (props.showCommunitySignup) result.push({ id: 'community-signup', title: 'Stay Connected' })
  return result
})

const currentStepIndex = ref(0)
const currentStep = computed<WizardStep>(() => steps.value[currentStepIndex.value])
const isLastStep = computed(() => currentStepIndex.value === steps.value.length - 1)

const dialogLabels = computed(() => ({
  title: 'Welcome to OpenNMS',
  close: 'Close'
}))

// ── Community sign-up form ─────────────────────────────────────────────────
const form = reactive({
  firstName: '',
  lastName: '',
  email: '',
  company: '',
  consent: false
})

const errors = reactive({
  firstName: '',
  lastName: '',
  email: '',
  company: '',
  consent: ''
})

const validateSignupForm = (): boolean => {
  errors.firstName = form.firstName.trim() ? '' : 'This field is required.'
  errors.lastName = form.lastName.trim() ? '' : 'This field is required.'
  errors.email = form.email.trim() ? '' : 'This field is required.'
  errors.company = form.company.trim() ? '' : 'This field is required.'
  errors.consent = form.consent ? '' : 'You must agree to receive communications to sign up.'
  return !Object.values(errors).some(e => e !== '')
}

// ── Password change form ───────────────────────────────────────────────────
const pwForm = reactive({
  currentPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const pwErrors = reactive({
  currentPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const validatePasswordForm = (): boolean => {
  pwErrors.currentPassword = pwForm.currentPassword.trim() ? '' : 'This field is required.'

  if (!pwForm.newPassword.trim()) {
    pwErrors.newPassword = 'This field is required.'
  } else if (!PASSWORD_REGEX.test(pwForm.newPassword)) {
    pwErrors.newPassword =
      'Must be 12–128 characters with at least one digit, uppercase, lowercase, and special character (!@#$%&.*+-).'
  } else if (SAME_CHAR_REGEX.test(pwForm.newPassword)) {
    pwErrors.newPassword = 'Cannot contain 6 or more identical characters in a row.'
  } else {
    pwErrors.newPassword = ''
  }

  pwErrors.confirmPassword =
    pwForm.confirmPassword === pwForm.newPassword ? '' : 'Passwords do not match.'

  return !Object.values(pwErrors).some(e => e !== '')
}

// ── Navigation ─────────────────────────────────────────────────────────────
const advanceOrClose = () => {
  if (isLastStep.value) {
    closeModal()
  } else {
    currentStepIndex.value++
  }
}

// ── Change Password handlers ───────────────────────────────────────────────
const onChangePassword = async () => {
  if (!validatePasswordForm()) return

  isBusy.value = true
  try {
    const result = await API.changePassword(pwForm.currentPassword, pwForm.newPassword)
    if (result === true) {
      advanceOrClose()
    } else {
      pwErrors.currentPassword = result.toLowerCase().includes('current') ? result : ''
      pwErrors.newPassword = result.toLowerCase().includes('current') ? '' : result
    }
  } catch {
    showSnackBar({ msg: 'There was an error changing your password. Please try again.', error: true })
  } finally {
    isBusy.value = false
  }
}

const onSkipPasswordChange = async () => {
  isBusy.value = true
  try {
    await API.dismissPasswordChangePrompt()
  } finally {
    isBusy.value = false
    advanceOrClose()
  }
}

// ── Usage Statistics handlers ──────────────────────────────────────────────
const onGotIt = async () => {
  isBusy.value = true
  try {
    await API.acknowledgeUsageStatisticsNotice()
  } finally {
    isBusy.value = false
    advanceOrClose()
  }
}

const onLearnMore = async () => {
  isBusy.value = true
  try {
    await API.acknowledgeUsageStatisticsNotice()
  } finally {
    isBusy.value = false
  }
  router.push('/usage-statistics')
  closeModal()
}

// ── Community sign-up handlers ─────────────────────────────────────────────
const onSignUp = async () => {
  if (!validateSignupForm()) return

  isBusy.value = true
  try {
    const submitResult = await API.submitProductUpdateEnrollmentForm({
      firstName: form.firstName,
      lastName: form.lastName,
      email: form.email,
      company: form.company,
      consent: form.consent
    })

    if (submitResult === false) {
      throw new Error('Submit failed')
    }

    await API.setProductUpdateEnrollmentStatus(true, true)
    signupSubmitted.value = true
  } catch {
    showSnackBar({ msg: 'There was an error submitting your information. Please try again.', error: true })
  } finally {
    isBusy.value = false
  }
}

const onOptOut = async () => {
  isBusy.value = true
  try {
    await API.setProductUpdateEnrollmentStatus(false, true)
  } finally {
    isBusy.value = false
    closeModal()
  }
}

const closeModal = () => {
  isVisible.value = false
  emit('close')
}
</script>

<style scoped lang="scss">
@import "@featherds/styles/mixins/typography";
@import "@featherds/styles/themes/variables";

// ---------------------------------------------------------------------------
// Outer wrapper — single flex child of FeatherDialog, column layout so the
// step bar stacks above the content instead of sitting beside it.
// ---------------------------------------------------------------------------
.wizard-wrapper {
  display: flex;
  flex-direction: column;
  min-width: 520px;
  max-width: 600px;
}

// ---------------------------------------------------------------------------
// Step bar — full width of the wrapper, separated from content by a divider.
// No negative margins needed; the wrapper controls the width.
// ---------------------------------------------------------------------------
.step-bar {
  display: flex;
  align-items: flex-start;
  padding-bottom: 1.25rem;
  margin-bottom: 1.5rem;
  border-bottom: 1px solid var($border-on-surface);
}

.step-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 0.4rem;
  flex-shrink: 0;

  &__circle {
    width: 2rem;
    height: 2rem;
    border-radius: 50%;
    border: 2px solid var($border-on-surface);
    background: var($surface);
    color: var($secondary-text-on-surface);
    display: flex;
    align-items: center;
    justify-content: center;
    @include body-small;
    font-weight: 700;
    transition: background 0.2s, border-color 0.2s, color 0.2s;
  }

  &__title {
    @include body-small;
    color: var($secondary-text-on-surface);
    text-align: center;
    white-space: nowrap;
    transition: color 0.2s, font-weight 0.2s;
  }

  &__check {
    font-size: 0.85rem;
    line-height: 1;
  }

  &--active {
    .step-item__circle {
      background: var($primary);
      border-color: var($primary);
      color: var($primary-text-on-color);
    }
    .step-item__title {
      color: var($primary);
      font-weight: 600;
    }
  }

  &--done {
    .step-item__circle {
      background: var($primary);
      border-color: var($primary);
      color: var($primary-text-on-color);
      opacity: 0.7;
    }
    .step-item__title {
      opacity: 0.7;
    }
  }
}

// Horizontal connector between step circles; margin-top centres it on the circles
.step-bar__connector {
  flex: 1;
  height: 2px;
  margin-top: 1rem;
  background: var($border-on-surface);
  transition: background 0.2s;

  &--done {
    background: var($primary);
    opacity: 0.7;
  }
}

// ---------------------------------------------------------------------------
// Step content
// ---------------------------------------------------------------------------
.step-content {
  flex: 1;
}

.step-title {
  @include subtitle1;
  margin: 0 0 0.75rem;
}

.step-description {
  margin-bottom: 1rem;
}

.form-row {
  display: flex;
  gap: 1rem;

  .form-field {
    flex: 1;
  }
}

.legal-text {
  @include body-small;
  color: var($secondary-text-on-surface);
  margin-top: 0.75rem;
  margin-bottom: 0.25rem;
}

.required-mark {
  color: var($error);
  margin-left: 2px;
}

.consent-error {
  @include body-small;
  color: var($error);
  margin-top: 0.25rem;
}

// ---------------------------------------------------------------------------
// Success state
// ---------------------------------------------------------------------------
.success-content {
  flex: 1;
}

.success-title {
  @include subtitle1;
  margin-bottom: 0.5rem;
}

.social-links {
  @include body-small;
  margin-top: 0.5rem;

  a {
    text-decoration: none;
    color: var($clickable-normal);

    &:hover {
      text-decoration: underline;
    }
  }
}
</style>

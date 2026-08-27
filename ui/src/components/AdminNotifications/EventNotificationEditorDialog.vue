<template>
  <OnmsDialog
    :visible="visible"
    modal
    :header="isEditing ? `Edit Event Notification: ${originalName}` : 'Add Event Notification'"
    class="event-notification-editor-dialog"
    width="min(1040px, 95vw)"
    data-test="event-notification-editor-dialog"
    @update:visible="(value: boolean) => emit('update:visible', value)"
  >
    <div v-if="errors.general" class="error-banner" data-test="save-error">{{ errors.general }}</div>

    <!-- Event UEI: full-width, friendly label first (how the legacy chooser
         listed them) with the UEI beneath; free-typed UEIs/regexes still work. -->
    <FormField
      class="uei-field"
      label="Event UEI"
      required
      for="new-notification-uei"
      :error="errors.uei || undefined"
    >
      <OnmsAutoComplete
        v-model="ueiValue"
        inputId="new-notification-uei"
        :suggestions="ueiSuggestions"
        optionLabel="uei"
        dropdown
        dropdownMode="current"
        completeOnFocus
        data-test="uei-input"
        fluid
        @complete="onUeiComplete"
      >
        <template #option="{ option }">
          <div class="uei-option">
            <div class="label">{{ option.eventLabel || '(no friendly label)' }}</div>
            <div class="uei">{{ option.uei }}</div>
          </div>
        </template>
      </OnmsAutoComplete>
      <small v-if="selectedLabel" class="hint" data-test="uei-friendly">Event: <strong>{{ selectedLabel }}</strong></small>
      <small v-else class="hint">Search by UEI. Enter a literal UEI, or a <code>~</code>-prefixed regular expression to match several.</small>
    </FormField>

    <div class="form-grid">
      <FormField
        label="Name"
        required
        for="new-notification-name"
        :error="errors.name || undefined"
      >
        <OnmsInputText id="new-notification-name" v-model="form.name" :maxlength="NAME_MAX" data-test="name-input" />
      </FormField>
      <FormField
        label="Destination Path"
        required
        for="new-notification-path"
        :error="errors.destinationPath || undefined"
      >
        <template #label-suffix>
          <HelpBadge :content="pathHelp" ariaLabel="Destination Path help" />
        </template>
        <OnmsSelect
          v-model="form.destinationPath"
          inputId="new-notification-path"
          :options="pathOptions"
          data-test="destination-path-select"
          fluid
        />
      </FormField>
      <FormField
        label="Description"
        for="new-notification-description"
      >
        <OnmsInputText id="new-notification-description" v-model="form.description" :maxlength="512" data-test="description-input" />
      </FormField>
      <FormField
        label="Subject"
        for="new-notification-subject"
      >
        <OnmsInputText id="new-notification-subject" v-model="form.subject" :maxlength="512" data-test="subject-input" />
      </FormField>
      <FormField
        label="Numeric (pager) Message"
        for="new-notification-numeric"
      >
        <OnmsInputText id="new-notification-numeric" v-model="form.numericMessage" :maxlength="512" data-test="numeric-message-input" />
        <small class="hint">
          Numeric-only message for pagers
          <HelpBadge :content="numericHelp" ariaLabel="Numeric message help" />
        </small>
      </FormField>
      <FormField
        class="full-width"
        label="Text Message"
        required
        for="new-notification-text"
        :error="errors.textMessage || undefined"
      >
        <OnmsTextarea id="new-notification-text" v-model="form.textMessage" rows="4" :maxlength="8000" data-test="text-message-input" fluid />
        <small class="hint">
          Supports event replacement tokens such as %nodelabel%, %interface%, %parm[...]%
          <HelpBadge :content="replacementHelp" ariaLabel="Event replacement tokens help" />
        </small>
      </FormField>
    </div>

    <div class="rule-block" data-test="rule-block">
      <div class="rule-header">
        <span class="rule-header__label">Rule</span>
        <div class="mode-toggle">
          <OnmsButton
            :variant="ruleMode === 'builder' ? 'filled' : 'text'"
            label="Builder"
            data-test="rule-mode-builder"
            @click="setRuleMode('builder')"
          />
          <OnmsButton
            :variant="ruleMode === 'raw' ? 'filled' : 'text'"
            label="Raw"
            data-test="rule-mode-raw"
            @click="setRuleMode('raw')"
          />
          <HelpBadge :content="ruleHelp" />
        </div>
      </div>

        <template v-if="ruleMode === 'builder'">
          <FormField
            label="IP address filter"
            for="rule-ipfilter"
            hint="Octets accept * (any), ranges (0-3) and lists (0,1,2), e.g. IPADDR IPLIKE 192.168.0-3.*"
          >
            <OnmsInputText id="rule-ipfilter" v-model="ipFilter" data-test="rule-ipfilter" />
          </FormField>
          <div class="svc-grid">
            <FormField
              label="Services (match ANY)"
              for="rule-services"
            >
              <OnmsMultiSelect
                v-model="ruleServices"
                inputId="rule-services"
                :options="availableServices"
                filter
                display="chip"
                data-test="rule-services"
                fluid
              />
            </FormField>
            <FormField
              label="Exclude services (NOT)"
              for="rule-not-services"
            >
              <OnmsMultiSelect
                v-model="ruleNotServices"
                inputId="rule-not-services"
                :options="availableServices"
                filter
                display="chip"
                data-test="rule-not-services"
                fluid
              />
            </FormField>
          </div>
          <div class="rule-generated">
            <span class="rule-generated__label">Generated rule:</span>
            <code data-test="rule-generated">{{ form.rule || '(empty)' }}</code>
          </div>
        </template>

        <FormField
          v-else
          label="Rule expression"
          for="new-notification-rule"
        >
          <OnmsInputText id="new-notification-rule" v-model="form.rule" data-test="rule-input" />
          <small class="hint">The filter that limits which nodes/interfaces trigger this notification.</small>
          <small v-if="ruleParseNote" class="rule-note" data-test="rule-note">{{ ruleParseNote }}</small>
        </FormField>

        <small v-if="errors.rule" class="field-error" data-test="rule-error">{{ errors.rule }}</small>

        <div class="rule-validate">
          <OnmsButton
            variant="outlined"
            label="Validate"
            :loading="validating"
            data-test="rule-validate"
            @click="doValidateRule"
          />
          <template v-if="ruleValidation">
            <span v-if="!ruleValidation.valid" class="rule-invalid" data-test="rule-invalid">
              Invalid: {{ ruleValidation.error }}
            </span>
            <span v-else class="rule-valid" data-test="rule-valid">
              Valid — {{ ruleValidation.matchCount }} interface{{ ruleValidation.matchCount === 1 ? '' : 's' }} match
              <template v-if="ruleValidation.error"> ({{ ruleValidation.error }})</template>
            </span>
          </template>
        </div>

        <div v-if="ruleValidation?.valid && ruleValidation.matches.length" class="rule-matches" data-test="rule-matches">
          <table>
            <thead><tr><th>Interface</th><th>Services</th></tr></thead>
            <tbody>
              <tr v-for="m in ruleValidation.matches" :key="m.ipAddress">
                <td>{{ m.ipAddress }}</td>
                <td>{{ m.services.join(', ') || 'All services' }}</td>
              </tr>
            </tbody>
          </table>
          <small v-if="ruleValidation.matchCount > ruleValidation.matches.length" class="hint">
            Showing first {{ ruleValidation.matches.length }} of {{ ruleValidation.matchCount }}.
          </small>
        </div>
    </div>

    <TogglePanel
      :collapsed="advancedCollapsed"
      class="advanced-panel"
      data-test="advanced-options"
      @update:collapsed="(value: boolean) => (advancedCollapsed = value)"
    >
      <template #header>
        <span class="panel-header">Advanced options</span>
      </template>

      <div class="advanced-body">
        <p class="advanced-hint">
          Rarely-needed fields carried in <code>notifications.xml</code>. Leave blank unless you need them.
        </p>

        <div class="params-block">
          <div class="params-title">Notification parameters <HelpBadge :content="paramsHelp" /></div>
          <div v-for="(p, i) in form.parameters" :key="i" class="param-row" :data-test="`param-row-${i}`">
            <OnmsInputText v-model="p.name" placeholder="Name" :data-test="`param-name-${i}`" />
            <OnmsInputText v-model="p.value" placeholder="Value" :data-test="`param-value-${i}`" />
            <OnmsIconButton
              :icon="Delete"
              title="Remove parameter"
              severity="danger"
              :data-test="`param-remove-${i}`"
              @click="form.parameters.splice(i, 1)"
            />
          </div>
          <OnmsButton
            variant="text"
            label="Add parameter"
            data-test="add-param"
            @click="form.parameters.push({ name: '', value: '' })"
          />
        </div>

        <div class="form-grid">
          <FormField
            label="Varbind name"
            for="new-notification-vbname"
          >
            <template #label-suffix>
              <HelpBadge :content="varbindHelp" ariaLabel="Varbind name help" />
            </template>
            <OnmsInputText id="new-notification-vbname" v-model="form.varbindName" data-test="varbind-name-input" />
          </FormField>
          <FormField
            label="Varbind value"
            for="new-notification-vbvalue"
          >
            <OnmsInputText id="new-notification-vbvalue" v-model="form.varbindValue" data-test="varbind-value-input" />
          </FormField>
          <FormField
            label="Event severity"
            for="new-notification-severity"
          >
            <OnmsSelect
              v-model="form.eventSeverity"
              inputId="new-notification-severity"
              :options="severityOptions"
              showClear
              data-test="event-severity-select"
              fluid
            />
            <small class="hint">Only notify when the event carries this severity.</small>
          </FormField>
          <FormField
            label="Notice queue"
            for="new-notification-queue"
          >
            <template #label-suffix>
              <HelpBadge :content="queueHelp" ariaLabel="Notice queue help" />
            </template>
            <OnmsInputText id="new-notification-queue" v-model="form.noticeQueue" data-test="notice-queue-input" />
          </FormField>
        </div>
      </div>
    </TogglePanel>

    <div class="status-row">
      <OnmsToggleSwitch v-model="form.enabled" aria-label="Enable this notification" data-test="enabled-toggle" />
      <span>Notification {{ form.enabled ? 'on' : 'off' }}</span>
    </div>

    <template #footer>
      <OnmsButton variant="text" label="Cancel" data-test="cancel-button" @click="emit('update:visible', false)" />
      <OnmsButton
        :label="isEditing ? 'Save Notification' : 'Add Notification'"
        :disabled="saving"
        data-test="save-button"
        @click="save"
      />
    </template>
  </OnmsDialog>
</template>

<script setup lang="ts">
import { computed, nextTick, reactive, ref, watch } from 'vue'

import { OnmsAutoComplete, OnmsButton, OnmsDialog, OnmsIconButton, OnmsInputText, OnmsMultiSelect, OnmsSelect, OnmsTextarea, OnmsToggleSwitch } from '@opennms/onms-ui'

import Delete from '@opennms/onms-ui/icons/action/Delete.vue'
import FormField from '@/components/Common/FormField.vue'
import HelpBadge from '@/components/Common/HelpBadge.vue'
import TogglePanel from '@/components/Common/TogglePanel.vue'
import API from '@/services'
import { useNotificationConfigStore } from '@/stores/notificationConfigStore'
import { EventNotification, RuleValidation, UeiSuggestion } from '@/types/notificationConfig'

const props = defineProps<{
  visible: boolean
  notification: EventNotification | null
}>()

const emit = defineEmits(['update:visible'])

const store = useNotificationConfigStore()

const ruleHelp = 'A filter rule, e.g. IPADDR IPLIKE *.*.*.*. Octets accept * (any), ranges (0-3) and lists (0,1,2). '
  + 'Combine with service checks, e.g. (IPADDR IPLIKE *.*.*.*) & (isHTTP). Leave the default to match every node.'
const numericHelp = 'For numeric-only pager destinations that can display digits (and a few symbols) but not letters. '
  + 'Typically a callback or PIN number plus the notice id, e.g. 111-%noticeid%. It is a free string, not enforced to be '
  + 'digits, but alphanumeric pagers and SMS should use the Text Message above instead.'
const replacementHelp = 'Event replacement tokens are substituted when the notice is sent. Common ones: '
  + '%noticeid% %eventid% %uei% %nodeid% %nodelabel% %foreignsource% %foreignid% %interface% %interfaceresolve% '
  + '%service% %severity% %time% %shorttime% %descr% %logmsg% %operinstruct% %ifalias% %parm[NAME]% %parm[#N]% '
  + '%parm[all]% %parm[names]%. Any %parm[...]% pulls a named parameter off the triggering event.'
const pathHelp = 'The escalation chain of who is notified and how (email, browser, …). Create and edit these on the Destination Paths tab.'
const paramsHelp = 'Extra name/value pairs passed through to the notification command — most methods need none. '
  + 'They are available to the command as additional %parm[name]% tokens.'
const varbindHelp = 'Restricts this notification to events that carry an SNMP varbind (event parameter) with this exact name AND value. '
  + 'Set both or neither; leave blank to match regardless of varbinds.'
const queueHelp = 'Which notifd queue processes this notice. Leave blank for the default in-memory queue unless you have configured extra queues in notifd-configuration.xml.'

const severityOptions = ['Indeterminate', 'Cleared', 'Normal', 'Warning', 'Minor', 'Major', 'Critical']

const defaultForm = () => ({
  name: '',
  description: '',
  rule: 'IPADDR IPLIKE *.*.*.*',
  destinationPath: '',
  subject: 'Notice #%noticeid%',
  textMessage: '',
  numericMessage: '111-%noticeid%',
  enabled: true,
  eventSeverity: '',
  noticeQueue: '',
  varbindName: '',
  varbindValue: '',
  parameters: [] as { name: string, value: string }[]
})

const form = reactive(defaultForm())
// AutoComplete's model holds either the free-typed string or a selected suggestion object.
const ueiValue = ref<string | UeiSuggestion>('')
const ueiSuggestions = ref<UeiSuggestion[]>([])
const saving = ref(false)

const NAME_MAX = 255
const errors = reactive({ name: '', uei: '', destinationPath: '', textMessage: '', rule: '', general: '' })
const clearErrors = () => {
  errors.name = ''
  errors.uei = ''
  errors.destinationPath = ''
  errors.textMessage = ''
  errors.rule = ''
  errors.general = ''
}
const advancedCollapsed = ref(true)

const ruleParseNote = ref('')
const ruleMode = ref<'builder' | 'raw'>('builder')

// Switch modes without letting the two representations diverge: entering Builder
// only succeeds if the current rule parses into the pickers, otherwise stay Raw.
const setRuleMode = (mode: 'builder' | 'raw') => {
  if (mode === 'builder') {
    if (parseRule(form.rule)) {
      ruleParseNote.value = ''
      ruleMode.value = 'builder'
    } else {
      ruleParseNote.value = 'This rule is too advanced for the builder — edit it as raw text.'
      ruleMode.value = 'raw'
    }
  } else {
    ruleMode.value = 'raw'
  }
}
const ipFilter = ref('IPADDR IPLIKE *.*.*.*')
const ruleServices = ref<string[]>([])
const ruleNotServices = ref<string[]>([])
const availableServices = ref<string[]>([])
const ruleValidation = ref<RuleValidation | null>(null)
const validating = ref(false)

// A filter is already grouped only when a single outer paren pair encloses the
// whole thing; testing just for a leading '(' mis-reads "(A) | (B)" as grouped
// and, once a service clause is appended, silently drops the OR grouping.
const isFullyGrouped = (s: string): boolean => {
  if (!s.startsWith('(') || !s.endsWith(')')) {
    return false
  }
  let depth = 0
  for (let i = 0; i < s.length; i++) {
    if (s[i] === '(') {
      depth++
    } else if (s[i] === ')') {
      depth--
      if (depth === 0 && i < s.length - 1) {
        return false
      }
    }
  }
  return depth === 0
}

// A service clause becomes the filter token is<Name>; only names made of the
// identifier characters the filter grammar accepts (matching parseRule's charset)
// can be emitted — a name with whitespace or other illegal characters would
// produce an unparseable token, so it is skipped.
const isLegalServiceName = (s: string): boolean => /^[\w.-]+$/.test(s)

// Assemble the rule the way the legacy wizard did: (ip filter) & (isA | isB) & (!isC & !isD).
const buildRule = (): string => {
  let base = ipFilter.value.trim() || 'IPADDR IPLIKE *.*.*.*'
  if (!isFullyGrouped(base)) {
    base = `(${base})`
  }
  let out = base
  const svc = ruleServices.value.filter(isLegalServiceName)
  if (svc.length) {
    out += ` & (${svc.map(s => `is${s}`).join(' | ')})`
  }
  const notSvc = ruleNotServices.value.filter(isLegalServiceName)
  if (notSvc.length) {
    out += ` & (${notSvc.map(s => `!is${s}`).join(' & ')})`
  }
  return out
}

// Best-effort round-trip: strip the trailing service clauses and treat the
// remainder as the IP filter. Anything we can't represent falls back to Raw.
const parseRule = (rule: string): boolean => {
  let s = rule.trim()
  // service names can contain hyphens/dots (e.g. OpenNMS-DB-Stats), so match more than \w
  const notRe = /\s*&\s*\(\s*(!is[\w.-]+(?:\s*&\s*!is[\w.-]+)*)\s*\)\s*$/
  const orRe = /\s*&\s*\(\s*(is[\w.-]+(?:\s*\|\s*is[\w.-]+)*)\s*\)\s*$/
  const not: string[] = []
  const svc: string[] = []
  let m = s.match(notRe)
  if (m) {
    not.push(...m[1].split(/\s*&\s*/).map(x => x.replace(/^!is/, '')))
    s = s.slice(0, m.index).trim()
  }
  m = s.match(orRe)
  if (m) {
    svc.push(...m[1].split(/\s*\|\s*/).map(x => x.replace(/^is/, '')))
    s = s.slice(0, m.index).trim()
  }
  let ip = s
  if (ip.startsWith('(') && ip.endsWith(')')) {
    ip = ip.slice(1, -1).trim()
  }
  // Detect a leftover service clause (isSSH, isHTTP) as a whole word; a plain
  // substring test also trips on IP filters containing dist/list/history.
  if (/\bis[A-Za-z]|[|&]/.test(ip)) {
    return false
  }
  ipFilter.value = ip || 'IPADDR IPLIKE *.*.*.*'
  ruleServices.value = svc
  ruleNotServices.value = not
  return true
}

const loadServices = async () => {
  if (availableServices.value.length) {
    return
  }
  const svcs = await API.getNotificationServices()
  if (svcs) {
    availableServices.value = svcs
  }
}

const doValidateRule = async () => {
  validating.value = true
  // Explicit user action: opt into the match preview (matchCount + matches).
  ruleValidation.value = await API.validateNotificationRule(form.rule.trim() || 'IPADDR IPLIKE *.*.*.*', true)
  validating.value = false
}

// Populating the pickers from an existing rule mutates ipFilter/ruleServices/
// ruleNotServices, which would fire the sync below and rewrite form.rule (e.g.
// wrapping the IP filter in parens) — an unrequested mutation. Suppress the sync
// during initial population so opening and saving with no edit leaves the rule
// byte-identical; genuine user input to a builder field clears the guard.
const suppressRuleBuild = ref(false)

// While in Builder mode the pickers own the rule string.
watch([ipFilter, ruleServices, ruleNotServices], () => {
  if (ruleMode.value === 'builder' && !suppressRuleBuild.value) {
    form.rule = buildRule()
  }
})

const pathOptions = computed(() => store.destinationPaths.map(p => p.name))

const isEditing = computed(() => props.notification !== null)
const originalName = computed(() => props.notification?.name ?? '')

const uei = computed<string>(() =>
  typeof ueiValue.value === 'string' ? ueiValue.value.trim() : ueiValue.value?.uei ?? '')

const selectedLabel = computed<string>(() =>
  typeof ueiValue.value === 'object' && ueiValue.value ? ueiValue.value.eventLabel ?? '' : '')

const validateForm = async (): Promise<boolean> => {
  clearErrors()
  const name = form.name.trim()
  if (!name) {
    errors.name = 'Name is required.'
  } else if (name.length > NAME_MAX) {
    errors.name = `Name must be ${NAME_MAX} characters or fewer.`
  } else if (!isEditing.value && store.eventNotifications.some(n => n.name === name)) {
    errors.name = 'A notification with this name already exists.'
  }
  if (!uei.value) {
    errors.uei = 'Event UEI is required.'
  } else if (uei.value.startsWith('~')) {
    try {
      RegExp(uei.value.slice(1))
    } catch {
      errors.uei = 'The ~ regular expression is not valid.'
    }
  }
  if (!form.destinationPath) {
    errors.destinationPath = 'Destination Path is required.'
  }
  if (!form.textMessage.trim()) {
    errors.textMessage = 'Text Message is required.'
  }
  // Only pay for the rule round-trip once the cheap checks pass.
  if (!errors.name && !errors.uei && !errors.destinationPath && !errors.textMessage) {
    const result = await API.validateNotificationRule(form.rule.trim() || 'IPADDR IPLIKE *.*.*.*')
    if (result && !result.valid) {
      errors.rule = `Rule is not a valid filter: ${result.error ?? 'unknown error'}`
    }
  }
  return !errors.name && !errors.uei && !errors.destinationPath && !errors.textMessage && !errors.rule
}

// Clear surfaced errors as soon as the user starts fixing anything.
watch([() => form.name, () => form.destinationPath, () => form.textMessage, () => form.rule, ueiValue], () => {
  if (errors.name || errors.uei || errors.destinationPath || errors.textMessage || errors.rule || errors.general) {
    clearErrors()
  }
})

const ruleValue = (rule: EventNotification['rule']): string => {
  if (typeof rule === 'string') {
    return rule
  }
  return rule?.value ?? ''
}

watch(
  () => props.visible,
  (isVisible) => {
    if (!isVisible) {
      return
    }
    ueiSuggestions.value = []
    advancedCollapsed.value = true
    clearErrors()
    suppressRuleBuild.value = true
    if (props.notification) {
      const n = props.notification
      Object.assign(form, {
        name: n.name,
        description: n.description ?? '',
        rule: ruleValue(n.rule) || 'IPADDR IPLIKE *.*.*.*',
        destinationPath: n.destinationPath ?? '',
        subject: n.subject ?? '',
        textMessage: n['text-message'] ?? '',
        numericMessage: n['numeric-message'] ?? '',
        enabled: n.status === 'on',
        eventSeverity: n['event-severity'] ?? '',
        noticeQueue: n['notice-queue'] ?? '',
        varbindName: n.varbind?.vbname ?? '',
        varbindValue: n.varbind?.vbvalue ?? '',
        parameters: (n.parameter ?? []).map(p => ({ name: p.name, value: p.value }))
      })
      ueiValue.value = n.uei ?? ''
    } else {
      Object.assign(form, defaultForm())
      ueiValue.value = ''
    }
    ruleValidation.value = null
    ruleParseNote.value = ''
    loadServices()
    ruleMode.value = parseRule(form.rule) ? 'builder' : 'raw'
    // Release the guard only after the pending picker mutations have flushed,
    // so the suppressed sync never rewrites the freshly loaded rule.
    nextTick(() => {
      suppressRuleBuild.value = false
    })
  }
)

const onUeiComplete = async (query: string) => {
  ueiSuggestions.value = await API.searchEventConfUeis(query)
}

const save = async () => {
  saving.value = true
  try {
    if (!(await validateForm())) {
      return
    }
    // Spread the original first so fields the form still doesn't expose
    // (writeable, rule.strict) survive the round-trip into notifications.xml.
    const original = props.notification ?? {}
    const originalRule = typeof props.notification?.rule === 'object' && props.notification?.rule !== null
      ? props.notification.rule
      : {}
    // The server (Parameter.setValue) rejects an empty value with a 400, so drop
    // any row missing either cell — mirroring the varbind's both-or-neither rule.
    const params = form.parameters
      .filter(p => p.name.trim() && p.value.trim())
      .map(p => ({ name: p.name.trim(), value: p.value }))
    const varbind = form.varbindName.trim() && form.varbindValue.trim()
      ? { vbname: form.varbindName.trim(), vbvalue: form.varbindValue.trim() }
      : undefined
    const notification: EventNotification = {
      ...original,
      name: form.name.trim(),
      status: form.enabled ? 'on' : 'off',
      uei: uei.value,
      description: form.description.trim() || undefined,
      rule: { ...originalRule, value: form.rule.trim() || 'IPADDR IPLIKE *.*.*.*' },
      destinationPath: form.destinationPath,
      subject: form.subject.trim() || undefined,
      'text-message': form.textMessage,
      'numeric-message': form.numericMessage.trim() || undefined,
      'event-severity': form.eventSeverity || undefined,
      'notice-queue': form.noticeQueue.trim() || undefined,
      parameter: params,
      varbind
    }
    const ok = isEditing.value
      ? await store.updateEventNotification(originalName.value, notification)
      : await store.addEventNotification(notification)
    if (ok) {
      emit('update:visible', false)
    } else {
      errors.general = 'Could not save the notification. Please review the fields and try again.'
    }
  } finally {
    saving.value = false
  }
}
</script>

<style lang="scss" scoped>
.uei-field {
  margin-bottom: 1rem;
}

.form-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 1rem;
  padding-top: 0.25rem;

  .full-width {
    grid-column: 1 / -1;
  }
}

:deep(input),
:deep(textarea),
:deep(.p-select),
:deep(.p-autocomplete) {
  width: 100%;
}

.hint {
  display: block;
  margin-top: 0.25rem;
  color: var(--p-text-muted-color);
}

.field-error {
  display: block;
  margin-top: 0.25rem;
  color: var(--p-red-400, #e57373);
  font-size: 0.85rem;
}

.error-banner {
  margin-bottom: 1rem;
  padding: 0.6rem 0.85rem;
  border: 1px solid var(--p-red-400, #e57373);
  border-radius: 4px;
  color: var(--p-red-400, #e57373);
  background: color-mix(in srgb, var(--p-red-400, #e57373) 12%, transparent);
}

.uei-option {
  .label {
    font-weight: 600;
  }

  .uei {
    font-size: 0.8rem;
    color: var(--p-text-muted-color);
  }
}

.advanced-panel {
  margin-top: 1rem;

  .panel-header {
    font-weight: 600;
  }

  .advanced-body {
    padding-top: 0.5rem;
  }

  .advanced-hint {
    margin: 0 0 1rem 0;
    color: var(--p-text-muted-color);
    font-size: 0.85rem;
  }

  .params-block {
    margin-bottom: 1rem;

    .params-title {
      font-weight: 600;
      margin-bottom: 0.5rem;
    }

    .param-row {
      display: grid;
      grid-template-columns: 1fr 1fr auto;
      gap: 0.5rem;
      align-items: center;
      margin-bottom: 0.5rem;
    }
  }
}

.status-row {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  margin-top: 1rem;
}

.rule-block {
  margin-top: 1rem;
  padding: 0.75rem;
  border: 1px solid var(--p-content-border-color, #e0e0e0);
  border-radius: 6px;

  .rule-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-bottom: 0.75rem;

    &__label {
      font-weight: 600;
    }
  }

  .rule-note {
    display: block;
    margin-top: 0.25rem;
    color: var(--p-orange-600, #c77700);
  }

  .mode-toggle {
    display: flex;
    align-items: center;
    gap: 0.5rem;
  }

  .svc-grid {
    display: grid;
    grid-template-columns: 1fr 1fr;
    gap: 1rem;
    margin-top: 0.5rem;
  }

  .rule-generated {
    margin-top: 0.75rem;
    padding: 0.5rem 0.75rem;
    border: 1px solid var(--p-content-border-color, #ccc);
    border-radius: 4px;
    font-size: 0.85rem;
    color: var(--p-text-color, inherit);

    &__label {
      font-weight: 600;
      margin-right: 0.5rem;
      color: var(--p-text-muted-color);
    }

    code {
      color: var(--p-text-color, inherit);
      word-break: break-all;
    }
  }

  .rule-validate {
    display: flex;
    align-items: center;
    gap: 0.75rem;
    margin-top: 0.75rem;
    flex-wrap: wrap;
  }

  .rule-valid {
    color: var(--p-green-600, #2e7d32);
  }

  .rule-invalid {
    color: var(--p-red-500, #b00020);
  }

  .rule-matches {
    margin-top: 0.75rem;
    max-height: 180px;
    overflow: auto;

    table {
      width: 100%;
      border-collapse: collapse;
      font-size: 0.85rem;
    }

    th,
    td {
      text-align: left;
      padding: 0.25rem 0.5rem;
      border-bottom: 1px solid var(--p-content-border-color, #eee);
    }
  }
}
</style>

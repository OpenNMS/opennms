<template>
  <Dialog
    :visible="visible"
    modal
    :header="isEditing ? `Edit Event Notification: ${originalName}` : 'Add Event Notification'"
    class="event-notification-editor-dialog"
    :style="{ width: '700px', maxWidth: '95vw' }"
    data-test="event-notification-editor-dialog"
    @update:visible="(value: boolean) => emit('update:visible', value)"
  >
    <div class="form-grid">
      <FormField>
        <IftaLabel>
          <InputText
            id="new-notification-name"
            v-model="form.name"
            data-test="name-input"
          />
          <label for="new-notification-name">Name *</label>
        </IftaLabel>
      </FormField>
      <FormField>
        <IftaLabel>
          <AutoComplete
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
                <div class="uei">{{ option.uei }}</div>
                <div class="label">{{ option.eventLabel }}</div>
              </div>
            </template>
          </AutoComplete>
          <label for="new-notification-uei">Event UEI *</label>
        </IftaLabel>
      </FormField>
      <FormField>
        <IftaLabel>
          <InputText
            id="new-notification-description"
            v-model="form.description"
            data-test="description-input"
          />
          <label for="new-notification-description">Description</label>
        </IftaLabel>
      </FormField>
      <FormField>
        <IftaLabel>
          <InputText
            id="new-notification-rule"
            v-model="form.rule"
            data-test="rule-input"
          />
          <label for="new-notification-rule">Rule</label>
        </IftaLabel>
        <small class="hint">Filter rule limiting which nodes/interfaces trigger this notification, e.g. IPADDR IPLIKE *.*.*.*</small>
      </FormField>
      <FormField>
        <IftaLabel>
          <Select
            v-model="form.destinationPath"
            labelId="new-notification-path"
            :options="pathOptions"
            data-test="destination-path-select"
            fluid
          />
          <label for="new-notification-path">Destination Path *</label>
        </IftaLabel>
      </FormField>
      <FormField>
        <IftaLabel>
          <InputText
            id="new-notification-subject"
            v-model="form.subject"
            data-test="subject-input"
          />
          <label for="new-notification-subject">Subject</label>
        </IftaLabel>
      </FormField>
      <FormField class="full-width">
        <IftaLabel>
          <Textarea
            id="new-notification-text"
            v-model="form.textMessage"
            rows="4"
            data-test="text-message-input"
            fluid
          />
          <label for="new-notification-text">Text Message *</label>
        </IftaLabel>
        <small class="hint">Supports event replacement tokens such as %nodelabel%, %interface%, %parm[...]%</small>
      </FormField>
      <FormField>
        <IftaLabel>
          <InputText
            id="new-notification-numeric"
            v-model="form.numericMessage"
            data-test="numeric-message-input"
          />
          <label for="new-notification-numeric">Numeric Message</label>
        </IftaLabel>
      </FormField>
      <FormField class="status-field">
        <div class="status-toggle">
          <PToggleSwitch
            v-model="form.enabled"
            aria-label="Enable this notification"
            data-test="enabled-toggle"
          />
          <span>Notification {{ form.enabled ? 'on' : 'off' }}</span>
        </div>
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
        :label="isEditing ? 'Save Notification' : 'Add Notification'"
        :disabled="!isValid || saving"
        data-test="save-button"
        @click="save"
      />
    </template>
  </Dialog>
</template>

<script setup lang="ts">
import { computed, reactive, ref, watch } from 'vue'

import AutoComplete, { AutoCompleteCompleteEvent } from 'primevue/autocomplete'
import Button from 'primevue/button'
import Dialog from 'primevue/dialog'
import IftaLabel from 'primevue/iftalabel'
import InputText from 'primevue/inputtext'
import Select from 'primevue/select'
import Textarea from 'primevue/textarea'
import ToggleSwitch from 'primevue/toggleswitch'

import FormField from '@/components/Common/FormField.vue'
import API from '@/services'
import { useNotificationConfigStore } from '@/stores/notificationConfigStore'
import { EventNotification, UeiSuggestion } from '@/types/notificationConfig'

const PToggleSwitch = ToggleSwitch

const props = defineProps<{
  visible: boolean
  notification: EventNotification | null
}>()

const emit = defineEmits(['update:visible'])

const store = useNotificationConfigStore()

const defaultForm = () => ({
  name: '',
  description: '',
  rule: 'IPADDR IPLIKE *.*.*.*',
  destinationPath: '',
  subject: '',
  textMessage: '',
  numericMessage: '',
  enabled: true
})

const form = reactive(defaultForm())
// AutoComplete's model holds either the free-typed string or a selected suggestion object.
const ueiValue = ref<string | UeiSuggestion>('')
const ueiSuggestions = ref<UeiSuggestion[]>([])
const saving = ref(false)

const pathOptions = computed(() => store.destinationPaths.map((p) => p.name))

const isEditing = computed(() => props.notification !== null)
const originalName = computed(() => props.notification?.name ?? '')

const uei = computed<string>(() => {
  return typeof ueiValue.value === 'string' ? ueiValue.value.trim() : ueiValue.value?.uei ?? ''
})

const isValid = computed(() => {
  return !!form.name.trim() && !!uei.value && !!form.destinationPath && !!form.textMessage.trim()
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
        enabled: n.status === 'on'
      })
      ueiValue.value = n.uei ?? ''
    } else {
      Object.assign(form, defaultForm())
      ueiValue.value = ''
    }
  }
)

const onUeiComplete = async (event: AutoCompleteCompleteEvent) => {
  ueiSuggestions.value = await API.searchEventConfUeis(event.query)
}

const save = async () => {
  saving.value = true
  try {
    // When editing, spread the original first so fields the form does not
    // expose (varbind, parameter, notice-queue, event-severity, rule.strict,
    // writeable) survive the round-trip into notifications.xml.
    const original = props.notification ?? {}
    const originalRule = typeof props.notification?.rule === 'object' && props.notification?.rule !== null
      ? props.notification.rule
      : {}
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
      'numeric-message': form.numericMessage.trim() || undefined
    }
    const ok = isEditing.value
      ? await store.updateEventNotification(originalName.value, notification)
      : await store.addEventNotification(notification)
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

  .status-field {
    display: flex;
    align-items: center;

    .status-toggle {
      display: flex;
      align-items: center;
      gap: 0.75rem;
    }
  }
}

.uei-option {
  .uei {
    font-weight: 600;
  }

  .label {
    font-size: 0.85rem;
    color: var(--p-text-muted-color);
  }
}
</style>

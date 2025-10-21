<template>
  <div
    class="main-content"
    v-if="store.selectedSource && store.eventModificationState.eventConfigEvent"
  >
    <div>
      <h3>
        {{ store.eventModificationState.isEditMode === CreateEditMode.Create ? 'Create New Event' : 'Edit Event' }}
      </h3>
    </div>
    <div class="spacer"></div>
    <div class="spacer"></div>
    <div class="basic-info">
      <div class="section-content">
        <div>
          <h3>Basic Information</h3>
        </div>
        <div class="spacer"></div>
        <FeatherInput
          label="Event UEI"
          data-test="event-uei"
          :error="errors.uei"
          v-model.trim="eventUei"
        >
        </FeatherInput>
        <div class="spacer"></div>
        <FeatherInput
          label="Event Label"
          data-test="event-label"
          :error="errors.eventLabel"
          v-model.trim="eventLabel"
        >
        </FeatherInput>
        <div class="spacer"></div>
        <FeatherTextarea
          v-model.trim="eventDescription"
          :error="errors.description"
          data-test="event-description"
          label="Event Description"
          placeholder="Type your event description here..."
          rows="10"
          cols="40"
          auto
          clear
        >
        </FeatherTextarea>
        <div class="spacer"></div>
        <FeatherInput
          label="Log Message"
          :error="errors.logmsg"
          type="search"
          data-test="log-message"
          v-model.trim="logMessage"
        >
        </FeatherInput>
        <div class="spacer"></div>
        <div class="dropdown">
          <FeatherSelect
            label="Destination"
            data-test="event-destination"
            :error="errors.dest"
            :options="DestinationOptions"
            v-model="selectedEventDestination"
          >
            <FeatherIcon :icon="MoreVert" />
          </FeatherSelect>
        </div>
        <div class="spacer"></div>
        <div class="dropdown">
          <FeatherSelect
            label="Severity"
            data-test="event-severity"
            :error="errors.severity"
            :options="SeverityOptions"
            v-model="selectedEventSeverity"
          >
            <FeatherIcon :icon="MoreVert" />
          </FeatherSelect>
        </div>
        <div class="spacer"></div>
        <div class="action-container">
          <FeatherButton
            secondary
            @click="handleCancel"
            data-test="cancel-event-button"
          >
            Cancel
          </FeatherButton>
          <FeatherButton
            primary
            @click="handleSaveEvent"
            data-test="save-event-button"
            :disabled="!isValid"
          >
            {{ store.eventModificationState.isEditMode === CreateEditMode.Create ? 'Create Event' : 'Save Changes' }}
          </FeatherButton>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import useSnackbar from '@/composables/useSnackbar'
import { createEventConfigEventXml } from '@/services/eventConfigService'
import { useEventModificationStore } from '@/stores/eventModificationStore'
import { CreateEditMode } from '@/types'
import { EventConfigEvent, EventFormErrors } from '@/types/eventConfig'
import { FeatherButton } from '@featherds/button'
import { FeatherIcon } from '@featherds/icon'
import MoreVert from '@featherds/icon/navigation/MoreVert'
import { FeatherInput } from '@featherds/input'
import { FeatherSelect, ISelectItemType } from '@featherds/select'
import { FeatherTextarea } from '@featherds/textarea'
import vkbeautify from 'vkbeautify'
import { DestinationOptions, Severity, SeverityOptions } from './constants'
import { validateEvent } from './eventValidator'

const router = useRouter()
const store = useEventModificationStore()
const eventUei = ref('')
const eventLabel = ref('')
const eventDescription = ref('')
const logMessage = ref('')
const errors = ref<EventFormErrors>({})
const isValid = ref(false)
const snackbar = useSnackbar()
const selectedEventDestination = ref<ISelectItemType>({ _text: '', _value: '' })
const selectedEventSeverity = ref<ISelectItemType>({ _text: '', _value: '' })

const xmlContent = computed(() => {
  return vkbeautify.xml(
    `<event xmlns="http://xmlns.opennms.org/xsd/eventconf">
        <uei>${eventUei.value}</uei>
        <event-label>${eventLabel.value}</event-label>
        <descr><![CDATA[${eventDescription.value}]]></descr>
        <logmsg dest="${selectedEventDestination.value._value}"><![CDATA[${logMessage.value}]]></logmsg>
        <severity>${selectedEventSeverity.value._value}</severity>
    </event>`.trim()
  )
})

const loadInitialValues = (val: EventConfigEvent | null) => {
  if (val) {
    const parser = new DOMParser()
    const xmlDoc = parser.parseFromString(val.xmlContent || '', 'application/xml')
    const logmsgElement = xmlDoc.getElementsByTagName('logmsg')[0]
    logMessage.value = logmsgElement ? logmsgElement.textContent || '' : ''
    const destAttr = logmsgElement?.getAttribute('dest') || ''
    selectedEventDestination.value = {
      _text: destAttr,
      _value: destAttr
    }
    eventDescription.value = val.description || ''
    eventUei.value = val.uei || ''
    eventLabel.value = val.eventLabel || ''
    selectedEventSeverity.value = {
      _text: val.severity ? Severity[val.severity as keyof typeof Severity] : '',
      _value: val.enabled ? Severity[val.severity as keyof typeof Severity] : ''
    }
  } else {
    eventUei.value = ''
    eventLabel.value = ''
    eventDescription.value = ''
    selectedEventSeverity.value = { _text: '', _value: '' }
  }
}

const handleSaveEvent = async () => {
  if (!store.eventModificationState.eventConfigEvent || !store.selectedSource) {
    return
  }

  try {
    if (!isValid.value) {
      return
    }

    let response
    // if (store.eventModificationState.isEditMode === CreateEditMode.Edit) {
    //   response = await updateEventConfigEventByIdXml(
    //     beautifiedXml,
    //     store.selectedSource.id,
    //     store.eventModificationState.eventConfigEvent.id,
    //     store.eventModificationState.eventConfigEvent.enabled,
    //     selected.value
    //   )
    // }
    if (store.eventModificationState.isEditMode === CreateEditMode.Create) {
      response = await createEventConfigEventXml(xmlContent.value, store.selectedSource.id)
    }

    if (response) {
      snackbar.showSnackBar({ msg: store.eventModificationState.isEditMode === CreateEditMode.Create ? 'Event created successfully' : 'Event updated successfully', error: false })
      router.push({
        name: 'Event Configuration Detail'
      })
    } else {
      snackbar.showSnackBar({ msg: 'Something went wrong', error: true })
    }
  } catch (error) {
    console.error(error)
    snackbar.showSnackBar({ msg: 'Something went wrong', error: true })
  }
}

const handleCancel = () => {
  store.resetEventModificationState()
  router.push({
    name: 'Event Configuration Detail'
  })
}

watchEffect(() => {
  const currentErrors = validateEvent(
    eventUei.value,
    eventLabel.value,
    eventDescription.value,
    selectedEventSeverity.value._value as string,
    selectedEventDestination.value._value as string,
    logMessage.value
  )
  isValid.value = Object.keys(currentErrors).length === 0
  errors.value = currentErrors as EventFormErrors
})

onMounted(() => {
  loadInitialValues(store.eventModificationState.eventConfigEvent)
})
</script>

<style scoped lang="scss">
@use '@featherds/styles/themes/variables';
@use '@featherds/styles/mixins/typography';

.main-content {
  padding: 30px;
  margin: 30px;

  border-radius: 8px;
  background-color: #ffffff;

  .basic-info {
    border-width: 1px;
    border-style: solid;
    border-color: var(variables.$border-on-surface);
    padding: 20px;
    border-radius: 8px;

    .section-content {
      width: 50%;
    }

    .dropdown {
      width: 50%;
    }
  }

  .spacer {
    min-height: 0.5em;
  }

  .action-container {
    display: flex;
    justify-content: flex-end;
    gap: 10px;
  }
}
</style>


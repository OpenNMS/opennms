<template>
  <div
    class="main-content"
    v-if="store.selectedSource && store.eventModificationState.eventConfigEvent"
  >
    <div>
      <h3>
        {{ store.eventModificationState.isEditMode === CreateEditMode.Create ? 'Create New Event' : 'Edit Event Details' }}
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
            v-model="destination"
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
            v-model="severity"
          >
            <FeatherIcon :icon="MoreVert" />
          </FeatherSelect>
        </div>
        <div class="spacer"></div>
        <div>
          <AlarmDataInfo
            data-test="alarm-data-info"
            :errors="errors"
            :addAlarmData="addAlarmData"
            :reductionKey="reductionKey"
            :alarmType="alarmType"
            :autoClean="autoClean"
            :clearKey="clearKey"
            @setAlarmData="setAlarmData"
          />
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
import AlarmDataInfo from './AlarmDataInfo.vue'
import { DestinationOptions, SeverityOptions } from './constants'
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
const destination = ref<ISelectItemType>({ _text: '', _value: '' })
const severity = ref<ISelectItemType>({ _text: '', _value: '' })
const alarmType = ref<ISelectItemType>({ _text: '', _value: '' })
const addAlarmData = ref(false)
const reductionKey = ref('')
const autoClean = ref(false)
const clearKey = ref('')

const xmlContent = computed(() => {
  return vkbeautify.xml(
    `<event xmlns="http://xmlns.opennms.org/xsd/eventconf">
        <uei>${eventUei.value}</uei>
        <event-label>${eventLabel.value}</event-label>
        <descr><![CDATA[${eventDescription.value}]]></descr>
        <logmsg dest="${destination.value._value}"><![CDATA[${logMessage.value}]]></logmsg>
        <severity>${severity.value._value}</severity>
        ${addAlarmData.value ? `<alarm-data
          reduction-key="${reductionKey.value}"
          alarm-type="${alarmType.value._value}"
          auto-clean="${autoClean.value}"
          ${clearKey.value ? `clear-key="${clearKey.value}"` : ''}
        />` : ''}
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
    destination.value = {
      _text: destAttr,
      _value: destAttr
    }
    eventDescription.value = val.description || ''
    eventUei.value = val.uei || ''
    eventLabel.value = val.eventLabel || ''
    severity.value = {
      _text: val.severity || '',
      _value: val.severity || ''
    }
    addAlarmData.value = xmlDoc.getElementsByTagName('alarm-data')[0] ? true : false
    if (addAlarmData.value) {
      const alarmDataElement = xmlDoc.getElementsByTagName('alarm-data')[0]
      reductionKey.value = alarmDataElement?.getAttribute('reduction-key') || ''
      alarmType.value = {
        _text: alarmDataElement?.getAttribute('alarm-type') || '',
        _value: alarmDataElement?.getAttribute('alarm-type') || ''
      }
      autoClean.value = alarmDataElement?.getAttribute('auto-clean') === 'true' ? true : false
      clearKey.value = alarmDataElement?.getAttribute('clear-key') || ''
    }
  } else {
    eventUei.value = ''
    eventLabel.value = ''
    eventDescription.value = ''
    severity.value = { _text: '', _value: '' }
    destination.value = { _text: '', _value: '' }
    logMessage.value = ''
    addAlarmData.value = false
    reductionKey.value = ''
    alarmType.value = { _text: '', _value: '' }
    autoClean.value = false
    clearKey.value = ''
  }
}

const setAlarmData = (key: string, value: any) => {
  if (key === 'addAlarmData') {
    addAlarmData.value = value
    if ((value as boolean) === false) {
      reductionKey.value = ''
      alarmType.value = { _text: '', _value: '' }
      autoClean.value = false
    }
  }

  if (key === 'reductionKey') {
    reductionKey.value = value
  }

  if (key === 'alarmType') {
    alarmType.value = {
      _text: value._text,
      _value: value._value
    }
  }

  if (key === 'autoClean') {
    autoClean.value = value
  }

  if (key === 'clearKey') {
    clearKey.value = value
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

    let response = null
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
    severity.value._value as string,
    destination.value._value as string,
    logMessage.value,
    addAlarmData.value,
    reductionKey.value,
    alarmType.value._value as string,
    autoClean.value,
    clearKey.value
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


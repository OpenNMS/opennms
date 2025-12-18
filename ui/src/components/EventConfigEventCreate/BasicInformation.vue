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
        <label class="label">Event UEI:</label>
        <div class="spacer"></div>
        <FeatherInput
          label=""
          data-test="event-uei"
          :error="errors.uei"
          v-model.trim="eventUei"
          hint="e.g., 'uei.opennms.org/vendor/application/eventname'"
        >
        </FeatherInput>
        <div class="spacer"></div>
        <label class="label">Event Label:</label>
        <div class="spacer"></div>
        <FeatherInput
          label=""
          data-test="event-label"
          :error="errors.eventLabel"
          v-model.trim="eventLabel"
          hint="e.g., 'Vendor Application Event Name'"
        >
        </FeatherInput>
        <div class="spacer"></div>
        <label class="label">Event Description:</label>
        <div class="spacer"></div>
        <FeatherTextarea
          v-model.trim="eventDescription"
          :error="errors.description"
          data-test="event-description"
          label=""
          hint="Provide a detailed description of the event."
          rows="10"
          auto
          clear
        >
        </FeatherTextarea>
        <div class="spacer"></div>
        <label class="label">Operator Instructions:</label>
        <div class="spacer"></div>
        <FeatherTextarea
          v-model.trim="operatorInstructions"
          data-test="operator-instructions"
          label=""
          hint="Instructions for operators when this event occurs."
          rows="5"
          auto
          clear
        >
        </FeatherTextarea>
        <div class="spacer"></div>
        <label class="label">Log Message Destination:</label>
        <div class="spacer"></div>
        <div class="dropdown">
          <FeatherSelect
            label="Destination"
            data-test="event-destination"
            :error="errors.dest"
            hint="Select the destination for the log message."
            :options="DestinationOptions"
            v-model="destination"
          >
            <FeatherIcon :icon="MoreVert" />
          </FeatherSelect>
        </div>
        <div class="spacer"></div>
        <label class="label">Log Message:</label>
        <div class="spacer"></div>
        <FeatherTextarea
          v-model.trim="logMessage"
          :error="errors.logmsg"
          data-test="log-message"
          label=""
          hint="Provide the log message for this event."
          rows="5"
          auto
          clear
        >
        </FeatherTextarea>
        <div class="spacer"></div>
        <label class="label">Severity:</label>
        <div class="spacer"></div>
        <div class="dropdown">
          <FeatherSelect
            label="Severity"
            data-test="event-severity"
            hint="Select the severity of the event."
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
        <div>
          <MaskElements
            data-test="mask-elements"
            @setMaskElements="setMaskElements"
            :maskElements="maskElements"
            :errors="errors"
          />
        </div>
        <div class="spacer"></div>
        <div>
          <MaskVarbinds
            data-test="mask-varbinds"
            :varbinds="varbinds"
            :maskElements="maskElements"
            :errors="errors"
            @setVarbinds="setVarbinds"
          />
        </div>
        <div class="spacer"></div>
        <div>
          <VarbindsDecode
            data-test="varbind-decodes"
            :varbindsDecode="varbindsDecode"
            @setVarbindsDecode="setVarbindsDecode"
            :errors="errors"
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
import { createEventConfigEvent, updateEventConfigEventById } from '@/services/eventConfigService'
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
import { AlarmTypeOptions, DestinationOptions, MAX_MASK_ELEMENTS, SeverityOptions } from './constants'
import { validateEvent } from './eventValidator'
import MaskElements from './MaskElements.vue'
import MaskVarbinds from './MaskVarbinds.vue'
import VarbindsDecode from './VarbindsDecode.vue'

const router = useRouter()
const store = useEventModificationStore()
const eventUei = ref('')
const eventLabel = ref('')
const eventDescription = ref('')
const operatorInstructions = ref('')
const logMessage = ref('')
const errors = ref<EventFormErrors>({})
const isValid = ref(false)
const snackbar = useSnackbar()
const destination = ref<ISelectItemType>({ _text: '', _value: '' })
const severity = ref<ISelectItemType>({ _text: '', _value: '' })
const alarmType = ref<ISelectItemType>({ _text: '', _value: '' })
const maskElements = ref<Array<{ name: ISelectItemType; value: string }>>([
  { name: { _text: '', _value: '' }, value: '' }
])
const addAlarmData = ref(false)
const reductionKey = ref('')
const autoClean = ref(false)
const clearKey = ref('')
const varbinds = ref<Array<{ index: string; value: string }>>([
  { index: '0', value: '' }
])
const varbindsDecode = ref<Array<{ parmId: string; decode: Array<{ key: string; value: string }> }>>([])

const xmlContent = computed(() => {
  return vkbeautify.xml(
    `<event xmlns="http://xmlns.opennms.org/xsd/eventconf">
        ${maskElements.value.length > 0 ? `
        <mask>
          ${maskElements.value.map(me => `
            <maskelement>
              <mename>${me.name._value}</mename>
              <mevalue>${me.value}</mevalue>
            </maskelement>`).join('')}
          ${varbinds.value.map(vb => `
            <varbind>
              <vbnumber>${vb.index}</vbnumber>
              <vbvalue>${vb.value}</vbvalue>
            </varbind>`).join('')}
        </mask>` : ''}
        ${varbindsDecode.value.map(vb => `
            <varbindsdecode>
              <parmid>${vb.parmId}</parmid>
              ${vb.decode.map(d => `
                <decode varbinddecodedstring="${d.key}" varbindvalue="${d.value}" />`).join('')}
            </varbindsdecode>`).join('')}
        <uei>${eventUei.value}</uei>
        <event-label>${eventLabel.value}</event-label>
        <descr><![CDATA[${eventDescription.value}]]></descr>
        <operinstruct><![CDATA[${operatorInstructions.value}]]></operinstruct>
        <logmsg dest="${destination.value._value}"><![CDATA[${logMessage.value}]]></logmsg>
        <severity>${severity.value._value}</severity>
        ${addAlarmData.value ? `
        <alarm-data
          reduction-key="${reductionKey.value}"
          alarm-type="${alarmType.value._value}"
          auto-clean="${autoClean.value}"
          ${clearKey.value ? `clear-key="${clearKey.value}"` : ''}
        />` : ''}
    </event>`.trim()
  )
})

const resetValues = () => {
  eventUei.value = ''
  eventLabel.value = ''
  eventDescription.value = ''
  operatorInstructions.value = ''
  logMessage.value = ''
  destination.value = { _text: '', _value: '' }
  severity.value = { _text: '', _value: '' }
  addAlarmData.value = false
  reductionKey.value = ''
  alarmType.value = { _text: '', _value: '' }
  autoClean.value = false
  clearKey.value = ''
  maskElements.value = []
  varbinds.value = []
  varbindsDecode.value = []
}

const loadInitialValues = (val: EventConfigEvent | null) => {
  if (val?.jsonContent) {
    eventUei.value = val?.jsonContent?.uei || ''
    eventLabel.value = val?.jsonContent?.eventLabel || ''
    eventDescription.value = val?.jsonContent?.descr || ''
    operatorInstructions.value = val?.jsonContent?.operinstruct || ''
    logMessage.value = val?.jsonContent?.logmsg?.content || ''
    const dest = val?.jsonContent?.logmsg?.dest || ''
    destination.value = {
      _text: dest,
      _value: dest
    }
    severity.value = {
      _text: val?.jsonContent?.severity || '',
      _value: val?.jsonContent?.severity || ''
    }
    addAlarmData.value = Object.keys(val?.jsonContent || {}).includes('alarmData') && val?.jsonContent?.alarmData ? true : false
    if (addAlarmData.value && val?.jsonContent?.alarmData) {
      reductionKey.value = val.jsonContent.alarmData.reductionKey || ''
      alarmType.value = {
        _text: AlarmTypeOptions.find(option => option._value === String(val.jsonContent?.alarmData?.alarmType))?._text,
        _value: String(val.jsonContent.alarmData.alarmType)
      }
      autoClean.value = val.jsonContent.alarmData.autoClean
      clearKey.value = val.jsonContent.alarmData.clearKey || ''
    }

    maskElements.value = (val?.jsonContent?.mask?.maskelements || []).map((me) => ({
      name: {
        _text: me.mename,
        _value: me.mename
      },
      value: me.mevalue
    }))

    varbinds.value = (val?.jsonContent?.mask?.varbinds || []).map((vb) => ({
      index: String(vb.vbnumber || 0),
      value: vb.vbvalue
    }))

    varbindsDecode.value = (val?.jsonContent?.varbindsdecodes || []).map((vbd) => ({
      parmId: vbd.parmid,
      decode: vbd.decodes.map((dec) => ({
        key: dec.varbinddecodedstring,
        value: dec.varbindvalue
      }))
    }))    
  } else {
    resetValues()
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

const setMaskElements = (key: string, value: any, index: number) => {
  if (index === undefined) {
    return
  }

  if (key === 'setName') {
    maskElements.value[index].name = value
  }

  if (key === 'setValue') {
    maskElements.value[index].value = value
  }

  if (key === 'addMaskRow') {
    if (maskElements.value.length < MAX_MASK_ELEMENTS) {
      maskElements.value.push({
        name: { _text: '', _value: '' },
        value: ''
      })
    } else {
      snackbar.showSnackBar({ msg: `Maximum of ${MAX_MASK_ELEMENTS} mask elements allowed.`, error: true })
    }
  }

  if (key === 'removeMaskRow') {
    maskElements.value.splice(index, 1)
  }
}

const setVarbinds = (key: string, value: any, index: number) => {
  if (index === undefined) {
    return
  }

  if (key === 'setIndex' && Number(value)) {
    varbinds.value[index].index = value
  }

  if (key === 'setValue') {
    varbinds.value[index].value = value
  }

  if (key === 'addVarbindRow') {
    varbinds.value.push({ index: '0', value: '' })
  }

  if (key === 'removeVarbindRow') {
    varbinds.value.splice(index, 1)
  }

  if (key === 'clearAllVarbinds') {
    varbinds.value = []
  }
}

const setVarbindsDecode = (key: string, value: any, index: number, decodeIndex: number) => {
  if (index === undefined) {
    return
  }

  if (key === 'setParmId') {
    varbindsDecode.value[index].parmId = value
  }

  if (key === 'addVarbindDecodeRow') {
    varbindsDecode.value.push({ parmId: '', decode: [] })
  }

  if (key === 'removeVarbindDecodeRow') {
    varbindsDecode.value.splice(index, 1)
  }

  if (key === 'addDecodeRow') {
    varbindsDecode.value[index].decode.push({ key: '', value: '' })
  }

  if (key === 'removeDecodeRow') {
    varbindsDecode.value[index].decode.splice(decodeIndex, 1)
  }

  if (key === 'setDecodeKey') {
    varbindsDecode.value[index].decode[decodeIndex].key = value
  }

  if (key === 'setDecodeValue') {
    varbindsDecode.value[index].decode[decodeIndex].value = value
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
    if (store.eventModificationState.isEditMode === CreateEditMode.Edit) {
      response = await updateEventConfigEventById(
        xmlContent.value,
        store.selectedSource.id,
        store.eventModificationState.eventConfigEvent.id,
        store.eventModificationState.eventConfigEvent.enabled
      )
    }
    if (store.eventModificationState.isEditMode === CreateEditMode.Create) {
      response = await createEventConfigEvent(xmlContent.value, store.selectedSource.id)
    }

    if (response) {
      snackbar.showSnackBar({ msg: store.eventModificationState.isEditMode === CreateEditMode.Create ? 'Event created successfully' : 'Event updated successfully', error: false })
      resetValues()
      const id = store.selectedSource.id
      store.resetEventModificationState()
      if (id) {
        router.push({
          name: 'Event Configuration Detail',
          params: { id }
        })
      } else {
        router.push({ name: 'Event Configuration' })
      }
    } else {
      snackbar.showSnackBar({ msg: 'Something went wrong', error: true })
    }
  } catch (error) {
    console.error(error)
    snackbar.showSnackBar({ msg: 'Something went wrong', error: true })
  }
}

const handleCancel = () => {
  const id = store.selectedSource?.id
  store.resetEventModificationState()
  if (id) {
    router.push({
      name: 'Event Configuration Detail',
      params: { id }
    })
  } else {
    router.push({ name: 'Event Configuration' })
  }
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
    clearKey.value,
    maskElements.value,
    varbinds.value,
    varbindsDecode.value
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
    padding: 10px;
    border-radius: 8px;

    .label {
      font-weight: 600;
    }

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


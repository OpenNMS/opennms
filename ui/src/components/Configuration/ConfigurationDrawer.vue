<template>
  <div :class="configurationDrawerActive ? 'active' : 'hidden'">
    <div class="click-close" @click="props.closePanel"></div>
    <ConfigurationHelpPanel
      :active="helpState.open"
      :onClose="() => {
        disableHelp();
        helpState.open = false
      }"
    ></ConfigurationHelpPanel>
    <div class="sideshared" :class="wrapperClass()">
      <div class="side-inner">
        <div class="side-inner-title">
          <div class="title">{{ editing ? 'Edit' : 'Add New' }} Requisition Definition</div>
          <div class="icon">
            <FeatherButton icon="Cancel" text @click="props.closePanel">
              <FeatherIcon class="close-icon" :icon="cancelIcon" />
            </FeatherButton>
          </div>
        </div>
      </div>
      <div class="slide-outer-body">
        <p class="slide-short">
          To synchronize inventory automatically from an outside source, build a requisition definition and set a
          schedule for it.
        </p>
        <div class="slide-inner-body">
          <FeatherInput
            ref="firstInput"
            class="side-input"
            label="Name"
            hint="Hint Text"
            :error="props.item.errors.name"
            v-model="stateIn.name"
          />
          <FeatherSelect
            class="side-input"
            textProp="name"
            hint="Hint Text"
            label="Type"
            :options="requisitionTypes"
            v-model="stateIn.type"
          />
          <div v-if="stateIn?.type?.name === 'Requisition'">
            <FeatherSelect
              class="side-input"
              textProp="name"
              hint="Hint Text"
              label="Requisition Type"
              :options="requisitionSubTypes"
              v-model="stateIn.subType"
            />
          </div>
          <div v-if="stateIn?.type?.name !== 'Requisition'">
            <FeatherInput
              label="Host"
              class="side-input"
              :error="props.item.errors.host"
              v-model="stateIn.host"
              @update:modelValue="(newVal: string) => { if (stateIn?.host) { stateIn.host = newVal; } }"
              hint="Hint Text"
            />
          </div>
          <div class="flex-center side-input">
            <FeatherSelect
              textProp="name"
              label="Monthly"
              :options="scheduleTypes"
              v-model="stateIn.occurance"
              class="occurance"
            />
            <FeatherInput type="time" class="time" label="Schedule Time" v-model="stateIn.time" />
          </div>
          <div class>
            <FeatherRadioGroup
              class="side-label"
              label="Rescan Behavior"
              v-model="stateIn.rescanBehavior"
            >
              <FeatherRadio
                v-for="item in rescanItems"
                :value="item.value"
                :key="item.name"
              >{{ item.name }}</FeatherRadio>
            </FeatherRadioGroup>
          </div>
        </div>
        <ConfigurationAdvancedPanel
          :active="props.advancedActive"
          :activeUpdate="props.activeUpdate"
          :helpState="props.helpState"
          :items="stateIn.advancedOptions"
          :addAdvancedOption="props.addAdvancedOption"
          :deleteAdvancedOption="props.deleteAdvancedOption"
        />
        <ConfigurationGeneratedUrl :config="urlState" />
        <FeatherButton @click="props.saveCurrentState" primary>Save &amp; Close</FeatherButton>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { reactive, computed, watch, ref, PropType } from 'vue'

import { FeatherSelect } from '@featherds/select'
import { FeatherButton } from '@featherds/button'
import { FeatherIcon } from '@featherds/icon'
import { FeatherInput } from '@featherds/input'
import { FeatherRadioGroup, FeatherRadio } from '@featherds/radio'

import Cancel from '@featherds/icon/navigation/Cancel'

import ConfigurationAdvancedPanel from './ConfigurationAdvancedPanel.vue'
import ConfigurationGeneratedUrl from './ConfigurationGeneratedUrl.vue'
import ConfigurationHelpPanel from './ConfigurationHelpPanel.vue'

import { requisitionTypes, requisitionSubTypes } from './copy/requisitionTypes'
import { scheduleTypes } from './copy/scheduleTypes'
import { rescanItems } from './copy/rescanItems'
import { LocalConfigurationWrapper } from './hooks'


/**
 * Props
 */
const props = defineProps({
  configurationDrawerActive: Boolean,
  activeUpdate: Function,
  closePanel: { type: Function as PropType<(payload: MouseEvent) => void>, required: true },
  item: { type: Object as PropType<LocalConfigurationWrapper>, required: true },
  advancedActive: Boolean,
  addAdvancedOption: { type: Function, required: true },
  deleteAdvancedOption: { type: Function, required: true },
  saveCurrentState: Function,
  edit: Boolean,
  helpState: { type: Object, required: true }
})

/**
 * Local State
 */

const firstInput = ref<HTMLInputElement | null>(null)
const bounceOutTimeout = ref(-1);
const bounceInTimeout = ref(-1);

const configurationDrawerActive = computed(() => props?.configurationDrawerActive)
const cancelIcon = computed(() => Cancel)
const helpState = computed(() => props.helpState)
const editing = computed(() => props.edit)
const errors = computed(() => props?.item?.errors)
const urlState = reactive({ item: {} })

const stateIn = computed(() => {
  const currentItem = props?.item?.config
  return reactive(currentItem || {})
})

/**
 * Scrolls the drawer to the first error on creation 
 * (should draw the user's eye to the problem)
 */
watch(errors, () => {
  if (props.item?.errors?.hasErrors) {
    const elem = document.querySelector('.feather-input-error')
    const wrapper = document.querySelector('.slide-outer-body')

    //Scrolls the drawer to the first error.
    wrapper?.scrollTo({ top: elem?.getBoundingClientRect().top, behavior: 'smooth' })

    //This 'bounce' animation is an attempt to further draw
    //the user's eye to the field with the error. It quickly scales and then descales
    //the element. The delays are to allow the above scroll to complete before moving on.
    clearTimeout(bounceOutTimeout.value)
    clearTimeout(bounceInTimeout.value)

    bounceOutTimeout.value = window.setTimeout(() => {
      const inputWrapper = elem?.parentElement?.parentElement
      inputWrapper?.classList.add('bounce')
      inputWrapper?.querySelector<HTMLInputElement>('.feather-input')?.focus()
      bounceInTimeout.value = window.setTimeout(() => {
        inputWrapper?.classList.remove('bounce')
      }, 200)
    }, 300)
  }
})

watch(configurationDrawerActive, () => {
  if (configurationDrawerActive.value) {
    if (firstInput.value) {
      firstInput.value.focus()
    }
  }
})

watch(stateIn, () => {
  urlState.item = stateIn.value
})


/**
 * Determines which classes to apply to the drawer.
 */
const wrapperClass = () => {
  let classes = 'sidepanelclosed '
  if (props.configurationDrawerActive) {
    classes = 'sidepanel '
  }
  if (helpState?.value?.open) {
    classes += 'help-open'
  }
  return classes
}

/**
 * Disables the Help on Page Reload
 */
const disableHelp = () => {
  localStorage.setItem('disable-help', 'true')
}
</script>


<style lang="scss">
.side-label {
  .group-label {
    color: #2a358a;
  }
}
.slide-inner-body {
  .feather-input-container {
    transform: scale(1);
    transition: all ease-in-out 0.2s;
  }
  .feather-input-container.bounce {
    transform: scale(1.02);
  }
}
.expansion-panel {
  .feather-expansion-header-button-text {
    color: #273180;
  }
}
</style>
<style lang="scss" scoped>
@import "@featherds/styles/mixins/typography";
@import "@featherds/styles/mixins/elevation";

.side-input {
  padding-bottom: 0;
}
.slide-short {
  margin-bottom: 32px;
}
.slide-outer-body {
  padding: 28px 40px;
  height: calc(100vh - 110px);
  overflow-y: auto;
}
.occurance {
  width: 100%;
}
.time {
  margin-left: 16px;
  width: 100%;
}
.flex-center {
  display: flex;
  align-items: center;
}
.title-padding {
  padding: 28px;
  padding-bottom: 0;
}
.active {
  opacity: 1;
  pointer-events: all;
  transition: opacity ease-in-out 0.4s;
}
.hidden {
  opacity: 0;
  pointer-events: none;
  transition: opacity ease-in-out 0.4s;
}
.click-close {
  transition: opacity ease-in-out 0.3s;
  width: 100%;
  height: 100vh;
  position: fixed;
  z-index: 2;
  background-color: rgba(0, 0, 0, 0.5);
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
}

.slide-inner-body {
  padding: 4px 20px;
  margin-top: 20px;
  background-color: #fff;
  @include elevation(1);
}

.side-inner-title {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding-top: 68px;
  padding-left: 40px;
  padding-right: 40px;
  padding-bottom: 8px;
  background-color: #fff;
  border-top: 1px solid #d7d7dc;
  border-bottom: 1px solid #d7d7dc;
}

.title {
  @include headline2();
  color: #2a358a;
  min-height: 40px;
  display: flex;
  align-items: center;
}
.icon {
  .btn {
    margin: 0;
  }
}
.close-icon {
  font-size: 32px;
}
.sideshared {
  z-index: 2;
  background-color: #f4f7fc;
  width: 40vw;
  min-width: 320px;
  height: 100vh;
  position: fixed;
  right: 0;
  top: 0;
  bottom: 0;
  transition: all ease-in-out 0.3s;
  border-left: 1px solid #b2b2b2;
}
.sidepanel {
  transform: translateX(0vw);
  transition: all ease-in-out 0.3s;
}
.sidepanelclosed {
  transform: translateX(45vw);
  transition: all ease-in-out 0.3s;
}
.help-open {
  transform: translateX(-20vw);
  transition: all ease-in-out 0.3s;
}
</style>
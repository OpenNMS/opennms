<template>
  <div :class="configurationDrawerActive ? 'active' : 'hidden'">
    <div
      class="click-close"
      @click="props.closePanel"
    ></div>
    <ConfigurationHelpPanel
      :item="props.item.config"
      :active="helpState.open"
      :onClose="() => {
        helpState.open = false
      }"
    />
    <div
      class="sideshared"
      :class="wrapperClass()"
    >
      <div class="side-inner">
        <div class="side-inner-title">
          <div class="title">
            {{ editing ? 'Edit' : 'Add' }}
            Requisition
          </div>
          <div class="icon">
            <FeatherButton
              icon="Cancel"
              text
              @click="props.closePanel"
            >
              <FeatherIcon
                class="close-icon"
                :icon="cancelIcon"
              />
            </FeatherButton>
          </div>
        </div>
      </div>
      <div class="slide-outer-body">
        <p class="slide-short">
          To help synchronize inventory automatically from an external source, build an external requisition and
          schedule it. The external requisition provides a URL that specifies where OpenNMS can get this input
          information.
        </p>
        <div class="slide-inner-body">
          <ProvisionDForm
            :updateFormValue="updateFormValue"
            :item="props.item"
            :helpState="helpState.open"
            :toggleHelp="toggleHelp"
            :formActive="props.configurationDrawerActive"
          />
        </div>
        <ConfigurationAdvancedPanel
          v-if="props.item.config.type.name !== 'File'"
          :active="props.advancedActive"
          :activeUpdate="props.activeUpdate"
          :helpState="props.helpState"
          :items="props.item.config.advancedOptions"
          :type="props.item.config.type.name"
          :subType="props.item.config.subType.name"
          :addAdvancedOption="props.addAdvancedOption"
          :deleteAdvancedOption="props.deleteAdvancedOption"
          :advancedKeyUpdate="props.advancedKeyUpdate"
        />
        <ConfigurationGeneratedUrl :item="props.item.config" />
        <div class="spinner-button flex button-align-right mt-20">
          <FeatherButton
            @click="props.saveCurrentState"
            primary
            :disabled="loading"
          >
            <FeatherSpinner v-if="loading" />
            <span v-if="!loading">Save &amp; Close</span>
          </FeatherButton>
        </div>
      </div>
    </div>
  </div>
</template>

<script
  setup
  lang="ts"
>
import { PropType } from 'vue'

import { FeatherButton } from '@featherds/button'
import { FeatherIcon } from '@featherds/icon'
import { FeatherSpinner } from '@featherds/progress'

import Cancel from '@featherds/icon/navigation/Cancel'

import ConfigurationAdvancedPanel from './ConfigurationAdvancedPanel.vue'
import ConfigurationGeneratedUrl from './ConfigurationGeneratedUrl.vue'
import ConfigurationHelpPanel from './ConfigurationHelpPanel.vue'
import ProvisionDForm from './ProvisionDForm.vue'
import { LocalConfigurationWrapper } from './configuration.types'

/**
 * Props
 */
const props = defineProps({
  configurationDrawerActive: Boolean,
  activeUpdate:  { type: Function as PropType<(_v: boolean) => void>, required: true },
  closePanel: { type: Function as PropType<(payload: MouseEvent) => void>, required: true },
  item: { type: Object as PropType<LocalConfigurationWrapper>, required: true },
  advancedActive: Boolean,
  addAdvancedOption: { type: Function as PropType<(payload: MouseEvent) => void>, required: true },
  advancedKeyUpdate: { type: Function, required: true },
  deleteAdvancedOption: { type: Function, required: true },
  saveCurrentState: { type: Function as PropType<(payload: MouseEvent) => void>, required: true },
  edit: Boolean,
  helpState: { type: Object, required: true },
  updateFormValue: { type: Function, required: true },
  loading: { type: Boolean, required: true }
})

/**
 * Local State
 */

const bounceOutTimeout = ref(-1)
const bounceInTimeout = ref(-1)
const initialWatchTimeout = ref(-1)

const configurationDrawerActive = computed(() => props?.configurationDrawerActive)
const cancelIcon = computed(() => Cancel)
const helpState = computed(() => props.helpState)
const editing = computed(() => props.edit)
const errors = computed(() => props?.item?.errors)

/**
 * Scrolls the drawer to the first error on creation
 * (should draw the user's eye to the problem)
 *  This is unneccessary, but a nice to have.
 */
watch(errors, () => {
  if (props.item?.errors?.hasErrors) {
    clearTimeout(initialWatchTimeout.value)
    initialWatchTimeout.value = window.setTimeout(() => {
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
    }, 50)
  }
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

const toggleHelp = () => {
  helpState.value.open = !helpState.value.open
}
</script>

<style lang="scss">
@import "@featherds/styles/themes/variables";

.side-label {
  .group-label {
    color: var($primary);
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

.spinner-button {
  .spinner {
    width: 20px;
    height: 20px;
  }
  .spinner-container {
    display: flex;
    align-items: center;
    height: 100%;
  }
}
</style>
<style
  lang="scss"
  scoped
>
@import "@featherds/styles/mixins/typography";
@import "@featherds/styles/mixins/elevation";
@import "@featherds/styles/themes/variables";

.flex {
  display: flex;
}
.button-align-right {
  justify-content: flex-end;
}
.mt-20 {
  margin-top: 20px;
}
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
.active {
  opacity: 1;
  pointer-events: all;
  transition: opacity ease-in-out 0.4s;
  z-index: 2;
  position: relative;
}
.hidden {
  opacity: 0;
  pointer-events: none;
  transition: opacity ease-in-out 0.4s;
  z-index: 2;
  position: relative;
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
  padding: 20px 20px 4px;
  background-color: var($background);
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
  background-color: var($background);
  border-top: 1px solid #d7d7dc;
  border-bottom: 1px solid #d7d7dc;
}
.title {
  @include headline2();
  color: var($primary);
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
  background-color: var($background);
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
  right: 0;
  transition: all ease-in-out 0.3s;
}
.sidepanelclosed {
  transform: translateX(45vw);
  transition: all ease-in-out 0.3s;
}
.help-open {
  right: 20vw;
  transition: all ease-in-out 0.3s;
}
</style>


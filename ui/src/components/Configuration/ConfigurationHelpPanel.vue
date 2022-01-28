<template>
  <div class="config-help-panel" :class="props?.active ? 'config-help-panel-open' : ''">
    <div class="config-help-close">
      <FeatherButton class="button" text icon @click="onClose">
        <FeatherIcon class="buttonIcon" :icon="chevronRight" />
      </FeatherButton>
    </div>
    <div class="config-help-header">
      <div class="config-help-title">Advanced Options</div>
      <div class="config-help-body">
        <p>
          The OpenDaylight integration synchronizes inventory and topology from an OpenDaylight
          software-defined network controller.
        </p>
        <p>Detailed information on the options it supports is available in the online documentation:</p>
      </div>
      <a href="/docs" target="_blank" class="config-help-link">READ FULL ARTICLE</a>
    </div>
    <div class="config-help-hr"></div>
    <div class="config-help-footer">
      <div class="footer-title">HELP US IMPROVE</div>
      <div class="footer-subtitle">Was this information helpful?</div>
      <div class="footer-button" :class="getFooterClickClass()">
        <div class="footer-yes" @click="footerYes">YES</div>
        <div class="footer-no" @click="footerNo">NO</div>
      </div>
    </div>
  </div>
</template>

<script lang="ts" setup>

import { computed, reactive } from 'vue'

import { FeatherIcon } from '@featherds/icon'
import { FeatherButton } from '@featherds/button'

import ChevronRight from '@featherds/icon/navigation/ChevronRight'

/**
 * Props
 */
const props = defineProps({
  active: Boolean,
  onClose: Function
})

/**
 * Local State
 */
const chevronRight = computed(() => ChevronRight)
const footerVals = reactive({ yes: true })

/**
 * Gets the current class structure for the
 * two zone click box.
 */
const getFooterClickClass = () => {
  let vals = 'footer-wrap-no'
  if (footerVals.yes) {
    vals = 'footer-wrap-yes'
  }
  return vals
}

/**
 * Stub for when the user clicks YES on two zone click box.
 * Functionality for this to be determined.
 */
const footerYes = () => {
  footerVals.yes = true
  console.log('The User Has Selected Yes!')
}

/**
 * Stub for when the user clicks NO on the two zone click box.
 * Functionality for this to be determined.
 */
const footerNo = () => {
  console.log('The User Has Selected No!')
  footerVals.yes = false
}

</script>

<style lang="scss">
.config-help-header {
  a.config-help-link:visited {
    color: var(--feather-secondary-variant);
  }
}
</style>
<style lang="scss" scoped>
@import "@featherds/styles/mixins/typography";
.config-help-close {
  display: flex;
  justify-content: flex-end;
  padding-top: 12px;
  height: 50px;
  .button {
    margin-top: 24px;
    font-size: 42px;
    color: var(--feather-secondary-variant);
    display: flex;
    align-items: center;
  }
}
.config-help-panel {
  position: fixed;
  background-color: white;
  z-index: 5;
  top: 60px;
  right: 0;
  width: 20vw;
  height: calc(100vh - 60px);
  transform: translateX(20vw);
  transition: transform ease-in-out 0.3s;
}
.config-help-panel-open {
  transform: translateX(0vw);
}
.config-help-header {
  padding: 20px 40px;
  a.config-help-link {
    font-weight: 700;
    color: var(--feather-secondary-variant);
    margin-top: 40px;
    margin-bottom: 0px;
  }
}
.config-help-title {
  @include headline2();
  color: var(--feather-primary);
  margin-top: 32px;
}
.config-help-body {
  @include body-small();
  margin-top: 12px;
  p {
    margin-bottom: 24px;
  }
}

.config-help-hr {
  border-bottom: 1px solid var(--feather-primary);
  margin: 0 40px;
  margin-bottom: 80px;
}
.config-help-footer {
  margin: 0 40px;
  .footer-title {
    font-weight: 700;
    color: var(--feather-primary);
  }
  .footer-subtitle {
    @include headline4();
    font-weight: 700;
  }
  .footer-button {
    display: flex;
    border: 1px solid var(--feather-secondary-variant);
    max-width: 200px;
    text-align: center;
    height: 50px;
    align-items: center;
    margin-top: 34px;
    div {
      width: 100px;
      height: 50px;
      display: flex;
      align-items: center;
      justify-content: center;
      cursor: pointer;
    }
    .footer-yes {
      color: var(--feather-primary);
      transition: all ease-in-out 0.3s;
    }
    .footer-no {
      color: var(--feather-primary);
      width: 100px;
      transition: all ease-in-out 0.3s;
    }
    &.footer-wrap-yes {
      .footer-yes {
        background-color: var(--feather-secondary-variant);
        color: var(--feather-surface);
      }
    }
    &.footer-wrap-no {
      .footer-no {
        background-color: var(--feather-secondary-variant);
        color: var(--feather-surface);
      }
    }
  }
}
</style>
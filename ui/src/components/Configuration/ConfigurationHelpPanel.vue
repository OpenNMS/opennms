<template>
  <div class="config-help-panel" :class="props?.active ? 'config-help-panel-open' : ''">
    <div class="config-help-close">
      <FeatherButton class="button" text icon @click="onClose">
        <FeatherIcon class="buttonIcon" :icon="chevronRight" />
      </FeatherButton>
    </div>
    <div class="config-help-header">
      <div class="config-help-title">{{ helpText.title }}</div>
      <div class="config-help-body">
        <p>{{ helpText.subTitle }}</p>
        <p>{{ helpText.help }}</p>
      </div>
      <a :href="helpText.link" target="_blank" class="config-help-link">{{ helpText.linkCopy }}</a>
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

import { computed, PropType, reactive } from 'vue'

import { FeatherIcon } from '@featherds/icon'
import { FeatherButton } from '@featherds/button'

import ChevronRight from '@featherds/icon/navigation/ChevronRight'
import { RequisitionPluginSubTypes, RequisitionTypes } from './copy/requisitionTypes'
import { LocalConfiguration } from './configuration.types'

/**
 * Props
 */
const props = defineProps({
  active: Boolean,
  onClose: Function,
  item: { type: Object as PropType<LocalConfiguration>, required: true }
})

/**
 * Local State
 */
const chevronRight = computed(() => ChevronRight)
const footerVals = reactive({ yes: false,no:false })

/**
 * Get the copy for the help window based on the selected
 * type and subtype (if Requisition is selected)
 * This should probably be moved to an API call in the future
 */
const helpText = computed(() => {
  const typeName = props.item.type.name
  const subType = props.item.subType.name

  let helpVals = {
    title: 'Requisition Definition',
    subTitle: 'A little default text...',
    help: 'Detailed information on the options it supports is available in the online documentation:',
    linkCopy: 'READ FULL ARTICLE',
    link: 'https://docs.opennms.com/horizon/29/reference/provisioning'
  }

  if (typeName === RequisitionTypes.File) {
    helpVals = {
      title: 'File',
      subTitle: 'The file handler imports a properly-formatted requisition definition from an XML file stored locally on the server.',
      help: 'Detailed information on the options it supports is available in the online documentation:',
      linkCopy: 'READ FULL ARTICLE',
      link: 'https://docs.opennms.com/horizon/29/reference/provisioning/handlers/file.html'
    }
  } else if (typeName === RequisitionTypes.RequisitionPlugin) {
    if (subType === '') {
      helpVals = {
        title: RequisitionTypes.RequisitionPlugin,
        subTitle: 'Some information about Requisition...',
        help: 'Detailed information on the options it supports is available in the online documentation:',
        linkCopy: 'READ FULL ARTICLE',
        link: ''
      }
    } else if (subType === RequisitionPluginSubTypes.OpenDaylight) {
      helpVals = {
        title: 'Open Daylight',
        subTitle: 'Open Daylight...',
        help: 'Detailed information on the options it supports is available in the online documentation:',
        linkCopy: 'READ FULL ARTICLE',
        link: ''
      }
    } else if (subType === RequisitionPluginSubTypes.ACI) {
      helpVals = {
        title: 'ACI',
        subTitle: 'ACI...',
        help: 'Detailed information on the options it supports is available in the online documentation:',
        linkCopy: 'READ FULL ARTICLE',
        link: ''
      }
    } else if (subType === RequisitionPluginSubTypes.Zabbix) {
      helpVals = {
        title: 'Zabbix',
        subTitle: 'Zabbix...',
        help: 'Detailed information on the options it supports is available in the online documentation:',
        linkCopy: 'READ FULL ARTICLE',
        link: ''
      }
    } else if (subType === RequisitionPluginSubTypes.AzureIot) {
      helpVals = {
        title: 'Azure IoT',
        subTitle: 'Azure IoT...',
        help: 'Detailed information on the options it supports is available in the online documentation:',
        linkCopy: 'READ FULL ARTICLE',
        link: ''
      }
    } else if (subType === RequisitionPluginSubTypes.PRIS) {
      helpVals = {
        title: 'PRIS',
        subTitle: 'PRIS...',
        help: 'Detailed information on the options it supports is available in the online documentation:',
        linkCopy: 'READ FULL ARTICLE',
        link: ''
      }
    }
  } else if (typeName === RequisitionTypes.VMWare) {
    helpVals = {
      title: RequisitionTypes.VMWare,
      subTitle: 'The VMware adapter pulls hosts and/or virtual machines from a vCenter server into Horizon. With this adapter, nodes can automatically be added, updated, or removed from your Horizon based on the status of the VMware entity.',
      help: 'Detailed information on the options it supports is available in the online documentation:',
      linkCopy: 'READ FULL ARTICLE',
      link: 'https://docs.opennms.com/horizon/29/reference/provisioning/handlers/vmware.html'
    }
  } else if (typeName === RequisitionTypes.HTTP || typeName === RequisitionTypes.HTTPS) {
    helpVals = {
      title: 'HTTP(S)',
      subTitle: 'The HTTP ...',
      help: 'Detailed information on the options it supports is available in the online documentation:',
      linkCopy: 'READ FULL ARTICLE',
      link: ''
    }
  } else if (typeName === RequisitionTypes.DNS) {
    helpVals = {
      title: 'DNS',
      subTitle: 'The DNS handler requests a zone transfer (AXFR) from a DNS server. The A and AAAA records are retrieved and used to build an import requisition.',
      help: 'Detailed information on the options it supports is available in the online documentation:',
      linkCopy: 'READ FULL ARTICLE',
      link: 'https://docs.opennms.com/horizon/29/reference/provisioning/handlers/dns.html'
    }
  }
  return helpVals
})


/**
 * Gets the current class structure for the
 * two zone click box.
 */
const getFooterClickClass = () => {
  let vals = ''
  if (footerVals.yes) {
    vals = 'footer-wrap-yes'
  }
  if (footerVals.no) {
    vals = 'footer-wrap-no'
  }
  return vals
}

/**
 * Stub for when the user clicks YES on two zone click box.
 * Functionality for this to be determined. Included in design from UX.
 * 
 */
const footerYes = () => {
  footerVals.yes = true
  footerVals.no = false
  console.log('The User Has Selected Yes!',props.item)
}

/**
 * Stub for when the user clicks NO on the two zone click box.
 * Functionality for this to be determined. Included in design from UX.
 */
const footerNo = () => {
  console.log('The User Has Selected No!',props.item)
  footerVals.no = true
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
    font-size: 42px;
    color: var(--feather-secondary-variant);
    display: flex;
    align-items: center;
    margin:0;
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
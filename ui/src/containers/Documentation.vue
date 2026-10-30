<template>
  <HelpPage
    title="Documentation"
    subtitle="Guides and references for installing, running, and extending OpenNMS."
    route="/documentation"
    :sections="sections"
  >
    <p v-if="loadFailed" class="docs-note" data-test="docs-unavailable">
      The guides for this release could not be determined. The links below lead to documentation for every OpenNMS
      product and release.
    </p>
  </HelpPage>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import ApiEndpoints from '@opennms/onms-ui/icons/network/ApiEndpoints.vue'
import BlogArticle from '@opennms/onms-ui/icons/content/BlogArticle.vue'
import Code from '@opennms/onms-ui/icons/network/Code.vue'
import DocumentationIcon from '@opennms/onms-ui/icons/communication/Documentation.vue'
import Monitoring from '@opennms/onms-ui/icons/network/Monitoring.vue'
import Server from '@opennms/onms-ui/icons/network/Server.vue'

import HelpPage from '@/components/Help/HelpPage.vue'
import API from '@/services'
import { HelpSection } from '@/types/help'

// The docs site is versioned per product, and only the server knows which product this is
// (the UI bundle is shared), so the release guides wait for /api/v2/about.
const product = ref<string | null>(null)
const docsVersion = ref<string | null>(null)
const loadFailed = ref(false)

const docsBase = computed(() => `https://docs.opennms.com/${product.value}/${docsVersion.value}`)

const releaseGuides = computed<HelpSection>(() => (
  {
    title: 'Guides for this release',
    links: [
      {
        label: 'Deployment Guide',
        href: `${docsBase.value}/deployment/core/introduction.html`,
        description: 'Deploy and configure the OpenNMS core, Minion, and Sentinel, including message brokers and time series storage.',
        icon: Server
      },
      {
        label: 'Operation Guide',
        href: `${docsBase.value}/operation/overview/overview.html`,
        description: 'Configure OpenNMS to monitor your infrastructure and services.',
        icon: Monitoring
      },
      {
        label: 'Development Guide',
        href: `${docsBase.value}/development/development.html`,
        description: 'Extend OpenNMS, integrate with its REST APIs, and set up a development environment.',
        icon: Code
      },
      {
        label: 'Release Notes',
        href: `${docsBase.value}/releasenotes/whatsnew.html`,
        description: 'What is new and what has changed in this release.',
        icon: BlogArticle
      }
    ]
  }
))

const moreDocumentation: HelpSection =
  {
    title: 'More documentation',
    links: [
      {
        label: 'OpenNMS Documentation',
        href: 'https://docs.opennms.com/',
        description: 'Documentation for every OpenNMS product and release.',
        icon: DocumentationIcon
      },
      {
        label: 'OpenAPI Documentation',
        href: '#/open-api',
        description: 'Browse and try out the OpenNMS REST APIs.',
        icon: ApiEndpoints
      }
    ]
  }

const sections = computed<HelpSection[]>(() =>
  product.value && docsVersion.value ? [releaseGuides.value, moreDocumentation] : [moreDocumentation])

onMounted(async () => {
  const result = await API.getAbout()
  if (result.success && result.payload?.product && result.payload.docsVersion) {
    product.value = result.payload.product
    docsVersion.value = result.payload.docsVersion
  } else {
    loadFailed.value = true
  }
})
</script>

<style scoped lang="scss">
.docs-note {
  margin: 0;
  color: var(--p-text-muted-color);
}
</style>

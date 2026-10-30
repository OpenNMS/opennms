<template>
  <div class="onms-row">
    <div class="onms-col-12">
      <BreadCrumbs :items="breadcrumbs" />
    </div>
  </div>

  <div class="about">
    <header class="hero">
      <div class="hero-text">
        <h1 class="product">{{ productName }}</h1>
        <div v-if="about" class="version" data-test="version">{{ about.displayVersion }}</div>
      </div>
      <a
        v-if="about"
        class="release-notes"
        :href="releaseNotesUrl"
        target="_blank"
        rel="noopener noreferrer"
        data-test="release-notes"
      >
        Release notes
        <OnmsIcon :icon="ExitToApp" />
      </a>
    </header>

    <div v-if="loadError" class="about-error" data-test="load-error">{{ loadError }}</div>

    <section>
      <h2 class="section-title">System details</h2>
      <div class="details-card" data-test="version-details">
        <div v-for="group in detailGroups" :key="group.title" class="detail-group">
          <div class="group-title">
            <OnmsIcon :icon="group.icon" class="group-icon" />
            {{ group.title }}
          </div>
          <dl>
            <template v-for="row in group.rows" :key="row.label">
              <dt>{{ row.label }}</dt>
              <dd>{{ row.value }}</dd>
            </template>
          </dl>
        </div>
      </div>
    </section>

    <section>
      <h2 class="section-title">License and copyright</h2>
      <div class="legal-card" data-test="license">
        <p>
          <a href="https://www.opennms.com/" target="_blank" rel="noopener noreferrer">OpenNMS&reg;</a> is a registered
          trademark, and Horizon&trade;, Meridian&trade;, and Compass&trade; are trademarks, of
          <a href="https://www.opennms.com/" target="_blank" rel="noopener noreferrer">The OpenNMS Group, Inc.</a>
          Horizon&trade; software by OpenNMS&reg; and Meridian&trade; software by OpenNMS&reg;, as distributed here,
          are copyright &copy; {{ copyrightDates }} The OpenNMS Group, Inc.
        </p>
        <p>
          OpenNMS contains original code as well as code from other open source projects. The license terms for this
          release, including those for third-party components, are in the <code>licenses</code> directory of your
          OpenNMS installation. The OpenNMS source code is available on
          <a href="https://github.com/OpenNMS/opennms" target="_blank" rel="noopener noreferrer">GitHub</a>.
        </p>
        <div class="osi">
          <a href="https://opensource.org/" target="_blank" rel="noopener noreferrer">
            <img :src="OSI_IMAGE" alt="OSI Certified" />
          </a>
          <span>
            This software is OSI Certified Open Source Software. OSI Certified is a certification mark of the
            <a href="https://opensource.org/" target="_blank" rel="noopener noreferrer">Open Source Initiative</a>.
          </span>
        </div>
      </div>
    </section>

    <section>
      <h2 class="section-title">Built with</h2>
      <div class="frameworks">
        <a v-for="fw in frameworks" :key="fw.href" :href="fw.href" target="_blank" rel="noopener noreferrer" class="framework">
          <img :src="fw.image" :alt="fw.alt" />
          <span>{{ fw.label }}</span>
        </a>
      </div>
    </section>
  </div>
</template>

<script setup lang="ts">
import { Component, computed, onMounted, ref } from 'vue'

import { OnmsIcon } from '@opennms/onms-ui'
import Database from '@opennms/onms-ui/icons/network/Database.vue'
import DesktopWindows from '@opennms/onms-ui/icons/hardware/DesktopWindows.vue'
import ExitToApp from '@opennms/onms-ui/icons/action/ExitToApp.vue'
import Microservices from '@opennms/onms-ui/icons/network/Microservices.vue'
import Server from '@opennms/onms-ui/icons/network/Server.vue'

import BreadCrumbs from '@/components/Layout/BreadCrumbs.vue'
import API from '@/services'
import { useMenuStore } from '@/stores/menuStore'
import { BreadCrumb } from '@/types'
import { AboutInfo } from '@/types/about'

// legacy webapp images, relative to /opennms/ui/; bound so Vite does not try to bundle them
const OSI_IMAGE = '../images/osi-certified.png'

const menuStore = useMenuStore()

const breadcrumbs = computed<BreadCrumb[]>(() => [
  { label: 'Home', to: menuStore.mainMenu?.homeUrl, isAbsoluteLink: true },
  { label: 'About', to: '/about', position: 'last' }
])

const about = ref<AboutInfo | null>(null)
const loadError = ref<string | null>(null)
const clientTime = new Date().toString()

const copyrightDates = computed(() => menuStore.mainMenu?.copyrightDates || `2002-${new Date().getFullYear()}`)

const productName = computed(() => {
  const product = about.value?.product
  return product ? `OpenNMS ${product.charAt(0).toUpperCase()}${product.slice(1)}` : 'OpenNMS'
})

const releaseNotesUrl = computed(() =>
  `https://docs.opennms.com/${about.value?.product}/${about.value?.docsVersion}/releasenotes/whatsnew.html`)

interface DetailGroup {
  title: string
  icon: Component
  rows: { label: string, value: string }[]
}

const detailGroups = computed<DetailGroup[]>(() => {
  const groups: DetailGroup[] = []
  const a = about.value
  if (a) {
    groups.push(
      {
        title: 'Server',
        icon: Server,
        rows: [
          { label: 'Server Time', value: a.serverTime },
          { label: 'Operating System', value: a.operatingSystem },
          { label: 'OSGi Container', value: a.osgiContainer },
          { label: 'Servlet Container', value: a.servletContainer ?? 'Unknown' }
        ]
      },
      {
        title: 'Java',
        icon: Microservices,
        rows: [
          { label: 'Version', value: a.javaVersion },
          { label: 'Runtime', value: a.javaRuntime },
          { label: 'Specification', value: a.javaSpecification },
          { label: 'Virtual Machine', value: a.javaVirtualMachine },
          { label: 'VM Specification', value: a.javaVirtualMachineSpecification }
        ]
      },
      {
        title: 'Data and messaging',
        icon: Database,
        rows: [
          { label: 'Database Type', value: a.databaseType },
          { label: 'Database Version', value: a.databaseVersion },
          { label: 'Time-Series Strategy', value: a.timeSeriesStrategy },
          { label: 'IPC Strategy', value: a.ipcStrategy }
        ]
      }
    )
  }
  groups.push({
    title: 'Your browser',
    icon: DesktopWindows,
    rows: [
      { label: 'Client Time', value: clientTime },
      { label: 'User Agent', value: navigator.userAgent }
    ]
  })
  return groups
})

const frameworks = [
  { label: 'Jetty', href: 'https://jetty.org/', image: '../images/powered_by_jetty.png', alt: 'Powered by Jetty' },
  { label: 'RRDtool', href: 'https://www.rrdtool.org/', image: '../images/rrdtool-logo-dark.png', alt: 'RRDtool' },
  { label: 'PostgreSQL', href: 'https://www.postgresql.org/', image: '../images/pg-power_95x51_4.gif', alt: 'PostgreSQL Powered' },
  { label: 'Hibernate ORM', href: 'https://hibernate.org/', image: '../images/hibernate_logo.gif', alt: 'Powered by Hibernate' }
]

onMounted(async () => {
  const result = await API.getAbout()
  if (result.success && result.payload) {
    about.value = result.payload
  } else {
    loadError.value = result.message
  }
})
</script>

<style scoped lang="scss">
.about {
  display: flex;
  flex-direction: column;
  gap: 2rem;
  padding: 0.5rem 2px 2rem 2px;
}
.hero {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  flex-wrap: wrap;
  gap: 1rem;
  padding: 1.5rem;
  border: 1px solid var(--p-content-border-color);
  border-left: 4px solid var(--p-primary-color);
  border-radius: 8px;
  background: var(--p-content-background);
}
.product {
  margin: 0;
  font-size: 1.75rem;
  font-weight: 600;
}
.version {
  margin-top: 0.25rem;
  font-size: 1.1rem;
  color: var(--p-text-muted-color);
  font-variant-numeric: tabular-nums;
}
.release-notes {
  display: inline-flex;
  align-items: center;
  gap: 0.4rem;
  font-weight: 600;
}
.section-title {
  margin: 0 0 0.75rem 0;
  font-size: 0.8rem;
  font-weight: 600;
  letter-spacing: 0.06em;
  text-transform: uppercase;
  color: var(--p-text-muted-color);
}
.details-card,
.legal-card {
  padding: 1.25rem 1.5rem;
  border: 1px solid var(--p-content-border-color);
  border-radius: 8px;
  background: var(--p-content-background);
}
.detail-group + .detail-group {
  margin-top: 1.5rem;
}
.group-title {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  margin-bottom: 0.25rem;
  font-weight: 600;
}
.group-icon {
  font-size: 1.15rem;
  color: var(--p-primary-color);
}
dl {
  display: grid;
  grid-template-columns: 12rem 1fr;
  margin: 0;
  font-size: 0.9rem;

  dt,
  dd {
    margin: 0;
    padding: 0.5rem 0;
    border-bottom: 1px solid var(--p-content-border-color);
  }
  dt {
    color: var(--p-text-muted-color);
  }
  dd {
    overflow-wrap: anywhere;
  }
  dt:last-of-type,
  dd:last-of-type {
    border-bottom: none;
  }
}
@media (max-width: 40rem) {
  dl {
    grid-template-columns: 1fr;

    dt {
      padding-bottom: 0;
      border-bottom: none;
    }
  }
}
.legal-card {
  p {
    margin: 0 0 0.75rem 0;
    line-height: 1.5;
  }
}
.osi {
  display: flex;
  align-items: center;
  gap: 1rem;
  padding-top: 0.75rem;
  border-top: 1px solid var(--p-content-border-color);
  color: var(--p-text-muted-color);
  font-size: 0.9rem;

  img {
    height: 3.5rem;
  }
}
.frameworks {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(10rem, 1fr));
  gap: 1rem;
}
.framework {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 0.6rem;
  padding: 1rem;
  border: 1px solid var(--p-content-border-color);
  border-radius: 8px;
  background: var(--p-content-background);
  color: var(--p-text-muted-color);
  text-decoration: none;
  font-size: 0.85rem;
  transition: border-color 0.15s ease;

  &:hover {
    border-color: var(--p-primary-color);
  }
  img {
    max-height: 3rem;
    max-width: 100%;
  }
}
.about-error {
  padding: 0.5rem 0.75rem;
  border-radius: 4px;
  border: 1px solid var(--onms-error);
  color: var(--onms-error);
}
</style>

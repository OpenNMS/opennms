<template>
  <HelpPage
    title="Commercial Support"
    subtitle="The OpenNMS Group offers support subscriptions, training, and professional services."
    route="/commercial-support"
    :sections="sections"
  />
</template>

<script setup lang="ts">
import { computed } from 'vue'
import Business from '@opennms/onms-ui/icons/action/Business.vue'
import Email from '@opennms/onms-ui/icons/action/Email.vue'
import Reporting from '@opennms/onms-ui/icons/action/Reporting.vue'
import SupportCenter from '@opennms/onms-ui/icons/action/SupportCenter.vue'

import HelpPage from '@/components/Help/HelpPage.vue'
import useRole from '@/composables/useRole'
import { HelpLink, HelpSection } from '@/types/help'

const { adminRole } = useRole()

const sections = computed<HelpSection[]>(() => {
  const customerLinks: HelpLink[] = [
    {
      label: 'Support Portal',
      href: 'https://support.opennms.com',
      description: 'Log in to create and track support tickets. Attaching a system report helps the support engineer diagnose the problem more quickly.',
      icon: SupportCenter
    }
  ]
  // the report page is admin-only, so only offer it to admins
  if (adminRole.value) {
    customerLinks.push({
      label: 'Generate a System Report',
      href: '#/system-report',
      description: 'Collect information about this OpenNMS instance and its environment to attach to a support ticket.',
      icon: Reporting
    })
  }

  return [
    {
      title: 'Get commercial support',
      links: [
        {
          label: 'Why OpenNMS',
          href: 'https://www.opennms.com/resources/why-opennms/',
          description: 'Learn about the support and services available from The OpenNMS Group.',
          icon: Business
        },
        {
          label: 'Contact Us',
          href: 'https://www.opennms.com/contact/',
          description: 'Talk to The OpenNMS Group about a support subscription.',
          icon: Email
        }
      ]
    },
    {
      title: 'Already a customer',
      links: customerLinks
    }
  ]
})
</script>

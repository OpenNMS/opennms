<template>
  <HelpPage
    title="Community Resources"
    subtitle="Ask questions, share ideas, and take part in OpenNMS development."
    route="/community-resources"
    :sections="sections"
  />
</template>

<script setup lang="ts">
import Chat from '@opennms/onms-ui/icons/communication/Chat.vue'
import Comment from '@opennms/onms-ui/icons/action/Comment.vue'
import Cycle from '@opennms/onms-ui/icons/action/Cycle.vue'
import Feedback from '@opennms/onms-ui/icons/action/Feedback.vue'
import ViewCode from '@opennms/onms-ui/icons/network/ViewCode.vue'

import HelpPage from '@/components/Help/HelpPage.vue'
import { HelpSection } from '@/types/help'

const sections: HelpSection[] = [
  {
    title: 'Get help from the community',
    links: [
      {
        label: 'Discourse',
        href: 'https://opennms.discourse.group',
        description: 'Community forum to get help, share ideas, and take part in OpenNMS development.',
        icon: Comment
      },
      {
        label: 'Web Chat',
        href: 'https://chat.opennms.com/opennms',
        description: 'Chat with users and developers on Mattermost. The Discussion channel is a good place to start.',
        icon: Chat
      }
    ]
  },
  {
    title: 'Get involved',
    links: [
      {
        label: 'OpenNMS on GitHub',
        href: 'https://github.com/OpenNMS/opennms',
        description: 'Source code for released versions of OpenNMS and the latest development code.',
        icon: ViewCode
      },
      {
        label: 'Issue Tracker',
        href: 'https://issues.opennms.com',
        description: 'Report bugs and request enhancements in Jira. Accounts are free.',
        icon: Feedback
      },
      {
        label: 'Continuous Integration',
        href: 'https://app.circleci.com/pipelines/github/OpenNMS/opennms',
        description: 'Build and test pipelines on CircleCI. Requires a GitHub login.',
        icon: Cycle
      }
    ]
  }
]
</script>

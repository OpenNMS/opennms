<template>
  <TogglePanel
    :collapsed="collapsed"
    class="about-panel"
    data-test="about-panel"
    @update:collapsed="(value: boolean) => (collapsed = value)"
  >
    <template #header>
      <span class="panel-header">
        <i class="pi pi-question-circle" aria-hidden="true" />
        About Minions and Locations
      </span>
    </template>
    <div class="help-columns">
      <div class="help-column">
        <div class="help-section">
          <div class="section-title">What monitoring locations and Minions are</div>
          <p>
            A monitoring location is a named place from which monitoring runs. The OpenNMS core itself is the
            built-in <strong>Default</strong> location. A Minion is a lightweight remote process that runs
            polling, data collection, flow and trap reception from its location on behalf of the core. Nodes and
            Minions reference a location by its <strong>name</strong>; the <strong>description</strong> is free
            text for administrators. A Minion registers itself with the core on first contact and sends a
            heartbeat every 30 seconds; the core tracks its <strong>status</strong> (UP, DOWN or UNKNOWN), its
            <strong>version</strong> and its last heartbeat, all shown here read-only.
          </p>
        </div>
        <div class="help-section">
          <div class="section-title">How to set up a Minion</div>
          <p>
            Create the location on the Monitoring locations tab first, then install the Minion package on the
            remote host. In the Minion's <code>etc/org.opennms.minion.controller.cfg</code> set
            <code>location</code> to that location name, <code>http-url</code> to the core's base URL (for
            example <code>http://core:8980/opennms</code>) and <code>broker-url</code> to the core's message
            broker, for example <code>failover:tcp://core:61616</code> when using the embedded ActiveMQ broker
            (its OpenWire connector must be enabled in the core's <code>etc/opennms-activemq.xml</code>); Kafka
            is the alternative transport. <code>id</code> is optional and is generated when absent. Store the
            REST and broker credentials with <code>bin/scvcli set opennms.http &lt;user&gt; &lt;password&gt;</code>
            and <code>bin/scvcli set opennms.broker &lt;user&gt; &lt;password&gt;</code>, then start the Minion.
            It appears on the Minions tab within a minute, and its node is provisioned into the
            <strong>Minions</strong> requisition. A Minion's location is set in its own configuration, so a
            Minion cannot be moved to another location from this page.
          </p>
        </div>
      </div>
      <div class="help-column">
        <div class="help-section">
          <div class="section-title">What this page shows</div>
          <p>
            The <strong>Minions</strong> tab lists every registered Minion and the <strong>Monitoring
            locations</strong> tab lists every location. Both tabs refresh automatically every 30 seconds; the
            <strong>Updated</strong> label says how old the data is and <strong>Refresh</strong> reloads it
            immediately. The Minions tab shows the Minion id (linked to its node once it has been provisioned),
            its <strong>Monitoring location</strong> (the link opens that location on the other tab), its
            <strong>Status</strong>, its <strong>Version</strong> and its <strong>Last heartbeat</strong>. The
            Version tag is green when it matches the core's version (a snapshot suffix is ignored) and reads
            "differs from core" otherwise. Last heartbeat is shown as the time since it arrived: green within
            5 minutes, amber within 2 hours and red beyond that. The quick filters (<strong>All</strong>,
            <strong>Down or unknown</strong>, <strong>Not seen in 24 h</strong>, <strong>Version differs from
            core</strong>) narrow the list to Minions that need attention; the location dropdown scopes it to
            one location, shown as a chip, and the search box matches on any column. Editing Minions is not
            available here; the <code>/api/v2/minions</code> REST API still accepts updates.
            <strong>Delete</strong> is for decommissioned Minions and is only offered once the last heartbeat
            is older than two minutes, since a Minion that is still running registers again within 30 seconds.
            The alarms raised through the Minion are deleted with it; its node stays until the
            <strong>Minions</strong> requisition is synchronized.
          </p>
          <p>
            The Monitoring locations tab shows each location's <strong>Name</strong>,
            <strong>Description</strong>, <strong>Minions</strong> (with tags for those down or unknown, and a
            link that opens them on the Minions tab) and the number of <strong>Nodes</strong> assigned to it.
            <strong>Add New Location</strong> creates a location with a name and a description; the name is the
            identifier and cannot be changed afterwards, so recreate the location to rename it.
            <strong>Edit</strong> changes the description. <strong>Default</strong> is the core itself and
            cannot be edited or deleted. <strong>Delete</strong> asks you to type the location's name and is
            only possible once no nodes or Minions are in it (each running Minion's own node counts), so move or
            delete them first. Deleting a location removes it as a perspective from any application that polled
            from it and deletes the outage history recorded from that perspective. The same operations are
            available to tooling through the <code>/api/v2/minions</code> and
            <code>/api/v2/monitoringLocations</code> REST APIs.
          </p>
        </div>
      </div>
    </div>
  </TogglePanel>
</template>

<script setup lang="ts">
import { ref } from 'vue'

import TogglePanel from '@/components/Common/TogglePanel.vue'

const collapsed = ref(true)
</script>

<style lang="scss" scoped>
.panel-header {
  display: inline-flex;
  align-items: center;
  gap: 0.5rem;
  font-weight: 600;

  .pi-question-circle {
    color: var(--p-primary-color);
  }
}

.help-columns {
  display: flex;
  gap: 2.5rem;
  flex-wrap: wrap;

  .help-column {
    flex: 1;
    min-width: 320px;
  }
}

.section-title {
  font-size: 1rem;
  font-weight: 600;
  margin-bottom: 0.5rem;
}

.help-section + .help-section {
  margin-top: 0.75rem;
}

p {
  margin: 0 0 0.75rem 0;
  font-size: 0.9rem;
  line-height: 1.5;
}
</style>

import { mount, RouterLinkStub } from '@vue/test-utils'
import dateFormatDirective from '@/directives/v-date'
import DCB from '@/containers/DeviceConfigBackup.vue'
import store from '@/store'
import { test, expect } from 'vitest'
import { DeviceConfigBackup } from '@/types/deviceConfig'

const mockDeviceConfigBackups: DeviceConfigBackup[] = [
  {
    id: 123,
    deviceName: 'Cisco-7201',
    location: 'location',
    ipAddress: '10.21.10.81',
    lastSucceededDate: 1643831118973,
    lastUpdatedDate: 1643831118973,
    backupStatus: 'success',
    nextScheduledBackupDate: 1643831118973,
    scheduledInterval: { deviceConfig: 'daily' },
    config: 'mock cisco config',
    configType: 'running',
    ipInterfaceId: 1,
    lastBackupDate: 1643831118973,
    lastFailedDate: 1643831118973,
    fileName: 'filename',
    failureReason: 'reason',
    encoding: '',
    nodeId: 1,
    nodeLabel: 'node1',
    operatingSystem: '',
    isSuccessfulBackup: true,
    monitoredServiceId: 1,
    serviceName: 'DeviceConfig-running'
  },
  {
    id: 12,
    deviceName: 'Aruba-7003-1',
    location: 'location',
    ipAddress: '10.21.10.81',
    lastSucceededDate: 1643831118973,
    lastUpdatedDate: 1643831118973,
    backupStatus: 'failed',
    nextScheduledBackupDate: 1643831118973,
    scheduledInterval: { deviceConfig: 'daily' },
    config: 'mock aruba config',
    configType: 'default',
    ipInterfaceId: 1,
    lastBackupDate: 1643831118973,
    lastFailedDate: 1643831118973,
    fileName: 'filename',
    failureReason: 'reason',
    encoding: '',
    nodeId: 1,
    nodeLabel: 'node1',
    operatingSystem: '',
    isSuccessfulBackup: true,
    monitoredServiceId: 1,
    serviceName: 'DeviceConfig-default'
  }
]

store.commit('deviceModule/SAVE_DEVICE_CONFIG_BACKUPS', mockDeviceConfigBackups)

const wrapper = mount(DCB, {
  global: {
    plugins: [store],
    directives: {
      date: dateFormatDirective
    },
    stubs: {
      RouterLink: RouterLinkStub
    }
  }
})

test('action btns enable and disable correctly', async () => {
  const viewHistoryBtn = wrapper.get('[data-test="view-history-btn"]')
  const downloadBtn = wrapper.get('[data-test="download-btn"]')
  const backupNowBtn = wrapper.get('[data-test="backup-now-btn"]')
  const checkboxes = wrapper.findAll('.device-config-checkbox')
  const firstDeviceConfig = wrapper.find('.device-config-checkbox > .feather-checkbox')
  const allCheckbox = wrapper.find('[data-test="all-checkbox"] > .feather-checkbox')

  // two DCB mock records
  expect(checkboxes.length).toBe(2)

  // all actions init disabled
  expect(viewHistoryBtn.attributes('aria-disabled')).toBe('true')
  expect(downloadBtn.attributes('aria-disabled')).toBe('true')
  expect(backupNowBtn.attributes('aria-disabled')).toBe('true')
  expect(allCheckbox.attributes('aria-checked')).toBe('false')

  // select first config
  await firstDeviceConfig.trigger('click')
  // all btns should be enabled
  expect(viewHistoryBtn.attributes('aria-disabled')).toBeUndefined()
  expect(downloadBtn.attributes('aria-disabled')).toBeUndefined()
  expect(backupNowBtn.attributes('aria-disabled')).toBeUndefined()

  // select 'all devices' checkbox
  await allCheckbox.trigger('click')
  // the view history and backup btns should be disabled. Dwnld btn enabled
  expect(allCheckbox.attributes('aria-checked')).toBe('true')
  expect(viewHistoryBtn.attributes('aria-disabled')).toBe('true')
  expect(downloadBtn.attributes('aria-disabled')).toBeUndefined()
  expect(backupNowBtn.attributes('aria-disabled')).toBe('true')

  // change 'all devices' to false
  await allCheckbox.trigger('click')
  // all actions back to disabled
  expect(viewHistoryBtn.attributes('aria-disabled')).toBe('true')
  expect(downloadBtn.attributes('aria-disabled')).toBe('true')
  expect(backupNowBtn.attributes('aria-disabled')).toBe('true')
  expect(allCheckbox.attributes('aria-checked')).toBe('false')
})

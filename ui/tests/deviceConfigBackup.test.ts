import { mount, RouterLinkStub } from '@vue/test-utils'
import dateFormatDirective from '@/directives/v-date'
import DCB from '@/containers/DeviceConfigBackup.vue'
import store from '@/store'
import { test, expect } from 'vitest'

const mockDeviceConfigBackups = [
  {
    id: 123,
    deviceName: 'Cisco-7201',
    location: 'location',
    ipAddress: '10.21.10.81',
    lastSucceededDate: '1643831118973',
    lastUpdatedDate: '1643831118973',
    backupStatus: 'Success',
    scheduleDate: '1643831118973',
    scheduleInterval: 'daily',
    config: 'mock config',
    configType: 'running'
  },
  {
    id: 12,
    deviceName: 'Aruba-7003-1',
    location: 'location',
    ipAddress: '10.21.10.81',
    lastSucceededDate: '1643831118973',
    lastUpdatedDate: '1643831118973',
    backupStatus: 'Failed',
    scheduleDate: '1643831118973',
    scheduleInterval: 'daily',
    config: 'mock config',
    configType: 'default'
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
  expect(viewHistoryBtn.attributes('aria-disabled')).toBeUndefined()
  expect(downloadBtn.attributes('aria-disabled')).toBeUndefined()
  expect(backupNowBtn.attributes('aria-disabled')).toBeUndefined()

  // select 'all devices' checkbox
  await allCheckbox.trigger('click')

  expect(allCheckbox.attributes('aria-checked')).toBe('true')
  expect(viewHistoryBtn.attributes('aria-disabled')).toBe('true')
  expect(downloadBtn.attributes('aria-disabled')).toBeUndefined()
  expect(backupNowBtn.attributes('aria-disabled')).toBeUndefined()

  // change 'all devices' to false
  await allCheckbox.trigger('click')

  // all actions back to disabled
  expect(viewHistoryBtn.attributes('aria-disabled')).toBe('true')
  expect(downloadBtn.attributes('aria-disabled')).toBe('true')
  expect(backupNowBtn.attributes('aria-disabled')).toBe('true')
  expect(allCheckbox.attributes('aria-checked')).toBe('false')
})

test('the modal opens and displays the config property', async () => {
  const viewHistoryBtn = wrapper.get('[data-test="view-history-btn"]')
  const lastBackupDateBtn = wrapper.get('.last-backup-date')
  const firstDeviceConfig = wrapper.find('.device-config-checkbox > .feather-checkbox')
  const dialog = wrapper.get('.feather-dialog')
  const closeModalBtn = wrapper.get('[data-ref-id="dialog-close"]')
  const dialogContent = wrapper.get('.dialog-content > .content')

  expect(dialog.attributes('style')).toBe('display: none;')

  await lastBackupDateBtn.trigger('click')
  expect(dialog.attributes('style')).not.toBe('display: none;')
  expect(dialogContent.text()).toBe('mock config')

  await closeModalBtn.trigger('click')
  expect(dialog.attributes('style')).toBe('display: none;')

  await firstDeviceConfig.trigger('click')
  await viewHistoryBtn.trigger('click')
  expect(dialog.attributes('style')).not.toBe('display: none;')
})

// response from Zenith
export interface ZenithConnectRegistrationResponse {
  success: boolean
  nmsSystemId: string
  nmsDisplayName: string
  accessToken: string
  refreshToken: string
}

// A Zenith Connect registration item in the OpenNMS database
export interface ZenithConnectRegistration {
  id?: string
  createTimeMs?: number       // created time in UTC ms
  systemId: string
  displayName: string
  zenithHost: string
  zenithRelativeUrl: string
  accessToken: string
  refreshToken: string
  registered?: boolean
  active?: boolean
}

// List of Zenith Connect registrations in the OpenNMS database
export interface ZenithConnectRegistrations {
  registrations: ZenithConnectRegistration[]
}

export interface MenuItem {
  id: string | null
  name: string | null
  url: string | null
  locationMatch: string | null
  roles: string[] | null
  items?: MenuItem[] | null
  requiredSystemProperties?: [{ name: string, value: string }] | null

  // not in Rest API, used for menu creation
  onClick?: () => void
}

export interface TileProviderItem {
  name: string
  url: string
  attribution: string
  userDefinedAsDefault?: boolean
  visible?: boolean
}

export interface MainMenu {
  baseHref: string
  homeUrl: string
  formattedDateTime: string
  formattedDate: string
  formattedTime: string
  noticeStatus: string
  username: string
  baseNodeUrl: string
  copyrightDates: string
  version: string
  zenithConnectEnabled: boolean
  zenithConnectBaseUrl: string
  zenithConnectRelativeUrl: string
  userTileProviders?: TileProviderItem[]
  
  menus: MenuItem[]
  helpMenu: MenuItem | null
  selfServiceMenu: MenuItem | null
  userNotificationMenu: MenuItem | null
  provisionMenu: MenuItem | null
  flowsMenu: MenuItem | null
  configurationMenu: MenuItem | null
}

export interface NoticeStatusDisplay {
  icon: string
  iconComponent: object | null
  colorClass: string
  title: string
}

export interface OnmsServiceType {
  id: number
  name: string
}

export interface OnmsNotification {
  id: number
  ipAddress: string
  nodeLabel: string
  notificationName: string
  pageTime: Date
  serviceType: OnmsServiceType | null
  severity: string
}

export interface NotificationItem {
  offset: number
  count: number
  totalCount: number
  notification: OnmsNotification[]
}

export interface NotificationSummary {
  totalCount: number
  totalUnacknowledgedCount: number
  user: string
  userUnacknowledgedCount: number
  teamUnacknowledgedCount: number
  userUnacknowledgedNotifications: NotificationItem
}

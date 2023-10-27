export interface MenuItem {
  id: string | null
  className: string | null
  name: string | null
  url: string | null
  locationMatch: string | null
  icon: string | null
  iconType: string | null
  isIconOnly: boolean | null
  isVueLink: boolean | null
  roles: string[] | null
}

export interface TopMenuItem extends MenuItem {
  items: MenuItem[] | null | undefined
}

export interface Notices {
  countUser: number | null
  countNonUser: number | null
  linkUser: string | null
  linkNonUser: string | null
  status: string | null
}

export interface TileProviderItem {
  name: string
  url: string
  attribution: string
}

export interface MainMenu {
  baseHref: string
  homeUrl: string
  formattedTime: string
  noticeStatus: string
  username: string
  baseNodeUrl: string
  copyrightDates: string
  version: string
  userTileProviders?: TileProviderItem[]
  
  menus: TopMenuItem[]
  helpMenu: TopMenuItem | null
  selfServiceMenu: TopMenuItem | null
  userNotificationMenu: TopMenuItem | null
  provisionMenu: TopMenuItem | null
  flowsMenu: TopMenuItem | null
  configurationMenu: TopMenuItem | null
  notices: Notices | null
}

export interface NoticeStatusDisplay {
  icon: string
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
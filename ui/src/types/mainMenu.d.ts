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
  requiredRoles: string[] | null
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

export interface MainMenu {
  baseHref: string
  formattedTime: string
  noticeStatus: string
  username: string
  
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
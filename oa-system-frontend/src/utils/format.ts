/**
 * 格式化日期
 * @param date 日期
 * @param format 格式
 */
export function formatDate(date: Date | string | number, format = 'YYYY-MM-DD'): string {
  const d = new Date(date)

  const year = d.getFullYear()
  const month = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  const hours = String(d.getHours()).padStart(2, '0')
  const minutes = String(d.getMinutes()).padStart(2, '0')
  const seconds = String(d.getSeconds()).padStart(2, '0')

  return format
    .replace('YYYY', String(year))
    .replace('MM', month)
    .replace('DD', day)
    .replace('HH', hours)
    .replace('mm', minutes)
    .replace('ss', seconds)
}

/**
 * 格式化日期时间
 */
export function formatDateTime(date: Date | string | number): string {
  return formatDate(date, 'YYYY-MM-DD HH:mm:ss')
}

/**
 * 格式化金额
 * @param amount 金额
 * @param currency 货币符号
 */
export function formatCurrency(amount: number, currency = '¥'): string {
  return `${currency}${amount.toFixed(2).replace(/\B(?=(\d{3})+(?!\d))/g, ',')}`
}

/**
 * 格式化数字 (带千分位)
 */
export function formatNumber(num: number): string {
  return num.toString().replace(/\B(?=(\d{3})+(?!\d))/g, ',')
}

/**
 * 计算两个日期之间的天数差
 */
export function daysBetween(date1: Date | string, date2: Date | string): number {
  const d1 = new Date(date1)
  const d2 = new Date(date2)
  const diffTime = d2.getTime() - d1.getTime()
  return Math.ceil(diffTime / (1000 * 60 * 60 * 24))
}

/**
 * 计算工龄
 */
export function calculateWorkYears(entryDate: Date | string): number {
  const entry = new Date(entryDate)
  const now = new Date()
  const diffTime = now.getTime() - entry.getTime()
  const diffYears = diffTime / (1000 * 60 * 60 * 24 * 365.25)
  return Math.floor(diffYears)
}

/**
 * 获取相对时间描述 (如: 3天前)
 */
export function getRelativeTime(date: Date | string): string {
  const d = new Date(date)
  const now = new Date()
  const diffTime = now.getTime() - d.getTime()
  const diffDays = Math.floor(diffTime / (1000 * 60 * 60 * 24))

  if (diffDays === 0) {
    const diffHours = Math.floor(diffTime / (1000 * 60 * 60))
    if (diffHours === 0) {
      const diffMinutes = Math.floor(diffTime / (1000 * 60))
      return diffMinutes <= 1 ? '刚刚' : `${diffMinutes}分钟前`
    }
    return `${diffHours}小时前`
  }

  if (diffDays === 1) return '昨天'
  if (diffDays < 7) return `${diffDays}天前`
  if (diffDays < 30) return `${Math.floor(diffDays / 7)}周前`
  if (diffDays < 365) return `${Math.floor(diffDays / 30)}个月前`
  return `${Math.floor(diffDays / 365)}年前`
}

/**
 * 手机号脱敏
 */
export function maskPhoneNumber(phone: string): string {
  if (!phone || phone.length !== 11) return phone
  return phone.replace(/(\d{3})\d{4}(\d{4})/, '$1****$2')
}

/**
 * 邮箱脱敏
 */
export function maskEmail(email: string): string {
  if (!email || !email.includes('@')) return email
  const [username, domain] = email.split('@')
  if (username.length <= 2) return email
  const maskedUsername = username.substring(0, 2) + '***'
  return `${maskedUsername}@${domain}`
}

/**
 * 生成随机颜色
 */
export function getRandomColor(): string {
  const colors = ['#1890FF', '#52C41A', '#FAAD14', '#F5222D', '#13C2C2', '#722ED1', '#EB2F96']
  return colors[Math.floor(Math.random() * colors.length)]
}

/**
 * 防抖函数
 */
export function debounce<T extends (...args: any[]) => any>(
  func: T,
  wait: number
): (...args: Parameters<T>) => void {
  let timeout: ReturnType<typeof setTimeout> | null = null

  return function (this: any, ...args: Parameters<T>) {
    if (timeout) clearTimeout(timeout)
    timeout = setTimeout(() => {
      func.apply(this, args)
    }, wait)
  }
}

/**
 * 节流函数
 */
export function throttle<T extends (...args: any[]) => any>(
  func: T,
  wait: number
): (...args: Parameters<T>) => void {
  let timeout: ReturnType<typeof setTimeout> | null = null
  let previous = 0

  return function (this: any, ...args: Parameters<T>) {
    const now = Date.now()
    const remaining = wait - (now - previous)

    if (remaining <= 0) {
      if (timeout) {
        clearTimeout(timeout)
        timeout = null
      }
      previous = now
      func.apply(this, args)
    } else if (!timeout) {
      timeout = setTimeout(() => {
        previous = Date.now()
        timeout = null
        func.apply(this, args)
      }, remaining)
    }
  }
}

/**
 * 深拷贝
 */
export function deepClone<T>(obj: T): T {
  if (obj === null || typeof obj !== 'object') return obj
  if (obj instanceof Date) return new Date(obj.getTime()) as any
  if (obj instanceof Array) return obj.map(item => deepClone(item)) as any
  if (obj instanceof Object) {
    const clonedObj: any = {}
    for (const key in obj) {
      if (obj.hasOwnProperty(key)) {
        clonedObj[key] = deepClone(obj[key])
      }
    }
    return clonedObj
  }
  return obj
}

/**
 * 树形数据扁平化
 */
export function flattenTree<T extends { children?: T[] }>(tree: T[]): T[] {
  const result: T[] = []

  const traverse = (nodes: T[]) => {
    nodes.forEach(node => {
      result.push(node)
      if (node.children && node.children.length > 0) {
        traverse(node.children)
      }
    })
  }

  traverse(tree)
  return result
}

/**
 * 扁平数据转树形
 */
export function arrayToTree<T extends { id: string | number; parentId?: string | number | null }>(
  items: T[],
  options?: { idKey?: string; parentIdKey?: string; childrenKey?: string }
): T[] {
  const { idKey = 'id', parentIdKey = 'parentId', childrenKey = 'children' } = options || {}

  const rootItems: T[] = []
  const lookup: Record<string, T> = {}

  // 初始化 lookup 表
  items.forEach(item => {
    lookup[String(item[idKey as keyof T])] = { ...item, [childrenKey]: [] } as T
  })

  // 构建树
  items.forEach(item => {
    const itemId = String(item[idKey as keyof T])
    const parentId = item[parentIdKey as keyof T]

    if (parentId === null || parentId === undefined || lookup[String(parentId)] === undefined) {
      rootItems.push(lookup[itemId])
    } else {
      const parent = lookup[String(parentId)]
      if (parent) {
        if (!parent[childrenKey as keyof T]) {
          (parent as any)[childrenKey] = []
        }
        ;(parent as any)[childrenKey].push(lookup[itemId])
      }
    }
  })

  return rootItems
}

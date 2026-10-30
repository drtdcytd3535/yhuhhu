import { reactive, computed, ref } from 'vue'

/* =============================================================
 * 我的洞府 · 修为 / 灵石 / 藏书阁 / 足迹 / 签到 / 修炼
 *
 * 数据分层：
 *   - 数据库 t_cave 为权威数据源（后端 /api/cave），换浏览器、清缓存都不丢
 *   - localStorage 仅为离线缓存与首屏秒开，断网也能继续玩，联网后自动补传
 *   - 境界配置取自 t_realm / t_realm_stage（/api/realms），取不到时用内置兜底
 * ============================================================= */

/* ==================== 接口地址 ==================== */
const API_CAVE = '/api/cave'
const API_REALMS = '/api/realms'

/* ==================== 内置境界配置（兜底） ==================== */

/**
 * 仙逆境界表（依原著四大步序列）
 * - segs：该境内的层次数（凝气十五层、踏天九桥，其余十重）
 * - span：该境所需修为
 * - unit：重数单位；suffix：境界后缀（前期称"期"，第二步起称"境"）
 * - step：所属大阶段，用于境界表分组
 */
const DEFAULT_REALMS = [
  // 第一步 · 凡蜕问道
  { name: '凝气', suffix: '期', unit: '层', segs: 15, span: 150, step: 'first' },
  { name: '筑基', suffix: '期', unit: '重', segs: 10, span: 160, step: 'first' },
  { name: '结丹', suffix: '期', unit: '重', segs: 10, span: 260, step: 'first' },
  { name: '元婴', suffix: '期', unit: '重', segs: 10, span: 420, step: 'first' },
  { name: '化神', suffix: '期', unit: '重', segs: 10, span: 680, step: 'first' },
  { name: '婴变', suffix: '期', unit: '重', segs: 10, span: 1100, step: 'first' },
  { name: '问鼎', suffix: '期', unit: '重', segs: 10, span: 1800, step: 'first' },
  // 过渡 · 阴虚阳实
  { name: '阴虚', suffix: '期', unit: '重', segs: 10, span: 900, step: 'bridge1' },
  { name: '阳实', suffix: '期', unit: '重', segs: 10, span: 900, step: 'bridge1' },
  // 第二步 · 规则掌控
  { name: '窥涅', suffix: '境', unit: '重', segs: 10, span: 2600, step: 'second' },
  { name: '净涅', suffix: '境', unit: '重', segs: 10, span: 3800, step: 'second' },
  { name: '碎涅', suffix: '境', unit: '重', segs: 10, span: 5200, step: 'second' },
  // 第三步 · 本源超脱
  { name: '空涅', suffix: '境', unit: '重', segs: 10, span: 7000, step: 'third' },
  { name: '空灵', suffix: '境', unit: '重', segs: 10, span: 9500, step: 'third' },
  { name: '空玄', suffix: '境', unit: '重', segs: 10, span: 13000, step: 'third' },
  { name: '空劫', suffix: '境', unit: '重', segs: 10, span: 18000, step: 'third' },
  // 第四步 · 踏天超脱
  { name: '踏天', suffix: '境', unit: '桥', segs: 9, span: 27000, step: 'fourth' }
]

/** 大阶段命名与说明，供境界表分组展示 */
const DEFAULT_STEPS = [
  { key: 'first', badge: '第一步', label: '凡蜕问道', note: '引气入体至问鼎，凡躯蜕为仙体' },
  { key: 'bridge1', badge: '过渡', label: '阴虚阳实', note: '元神吐纳元力、化元完成，方入第二步' },
  { key: 'second', badge: '第二步', label: '规则掌控', note: '窥涅天仙 · 净涅仙王 · 碎涅仙君，碎涅圆满需渡天人五衰' },
  { key: 'third', badge: '第三步', label: '本源超脱', note: '空涅仙帝至空劫大天尊，言出法随，需渡九次玄劫' },
  { key: 'fourth', badge: '第四步', label: '踏天超脱', note: '踏天九桥，一步一桥，明悟轮回真我' }
]

/** 当前生效的境界配置（数据库加载后覆盖内置值） */
export const realms = ref(DEFAULT_REALMS)
/** 当前生效的大阶段配置 */
export const STEPS = ref(DEFAULT_STEPS)
/** 境界配置来源：'db' 数据库 / 'builtin' 内置兜底 */
export const realmSource = ref('builtin')

/* ==================== 数值规则 ==================== */
/** 收录藏书阁一次的修为（五千） */
const FAV_EXP = 5000
/** 首次研读一次的修为 */
const HISTORY_EXP = 15
/** 签到灵石 */
const SIGN_STONES = 50
/** 签到修为 */
const SIGN_EXP = 10
/** 藏书阁容量上限 */
const FAVS_MAX = 50
/** 足迹保留条数 */
const HISTORY_MAX = 100
/** 每日修炼（小游戏）可得修为上限 */
export const TRAIN_LIMIT = 300
/** 写入数据库的防抖间隔（毫秒） */
const PUSH_DELAY = 400

/** 洞府全局状态 */
export const cave = reactive({
  username: '道友',
  exp: 0,
  stones: 0,
  favs: [],      // 藏书阁：{ id, title, categoryPath, createdAt }
  history: [],   // 足迹：{ id, title, categoryPath, readAt }
  signDate: '',  // 最近签到日期 yyyy-mm-dd
  trainDate: '', // 最近修炼日期 yyyy-mm-dd
  trainExp: 0,   // 当日修炼（小游戏）已得修为
  savedAt: 0,    // 最后修改时间戳（毫秒），用于与数据库比对新旧
  synced: false, // 是否已与数据库同步过
  cloud: false   // true 本轮数据来自云端
})

/** 当前境界：{ name, suffix, unit, level, segs } */
export const realm = computed(() => {
  const list = realms.value
  let base = 0
  for (const r of list) {
    if (cave.exp < base + r.span) {
      const ratio = (cave.exp - base) / r.span
      const level = Math.min(r.segs, Math.floor(ratio * r.segs) + 1)
      return { name: r.name, suffix: r.suffix, unit: r.unit, level, segs: r.segs }
    }
    base += r.span
  }
  const last = list[list.length - 1] || DEFAULT_REALMS[DEFAULT_REALMS.length - 1]
  return { name: last.name, suffix: last.suffix, unit: last.unit, level: last.segs, segs: last.segs }
})

/** 当前境界内进度 0~1（用于修为条宽度） */
export const realmProgress = computed(() => {
  let base = 0
  for (const r of realms.value) {
    if (cave.exp < base + r.span) return (cave.exp - base) / r.span
    base += r.span
  }
  return 1
})

/** 距下一境圆满所需修为：{ name, need }；踏天九桥圆满后为 null */
export const nextRealm = computed(() => {
  let base = 0
  for (const r of realms.value) {
    if (cave.exp < base + r.span) {
      return {
        name: r.name,
        suffix: r.suffix,
        unit: r.unit,
        need: base + r.span - cave.exp
      }
    }
    base += r.span
  }
  return null
})

/**
 * 境界阶序总览：每境的修为区间、每重所需、当前所处位置
 * status: 'done' 已渡 / 'current' 当前所在 / 'future' 未至
 */
export const realmTable = computed(() => {
  const at = realm.value
  const rows = []
  let base = 0
  for (const r of realms.value) {
    const from = base
    const to = base + r.span
    const isCurrent = r.name === at.name
    const done = !isCurrent && cave.exp >= to
    rows.push({
      name: r.name,
      suffix: r.suffix,
      unit: r.unit,
      segs: r.segs,
      step: r.step,
      from,
      to,
      span: r.span,
      perLevel: Math.round(r.span / r.segs),
      status: isCurrent ? 'current' : done ? 'done' : 'future',
      level: isCurrent ? at.level : done ? r.segs : 0,
      progress: isCurrent
        ? Math.min(1, Math.max(0, (cave.exp - from) / r.span))
        : done
          ? 1
          : 0
    })
    base = to
  }
  return rows
})

/** 十七境圆满所需累计修为 */
export const REALM_TOTAL = computed(() => realms.value.reduce((s, r) => s + r.span, 0))

/** 境界总数 */
export const REALM_COUNT = computed(() => realms.value.length)

/* ==================== 工具 ==================== */

/** 今日日期 yyyy-mm-dd */
function today() {
  const d = new Date()
  const m = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  return `${d.getFullYear()}-${m}-${day}`
}

/** 某道号的本地缓存键 */
function storageKey(name) {
  return `xianni_cave_${name || 'guest'}`
}

/** 当前存档快照（用于本地缓存与提交数据库） */
function snapshot() {
  return {
    username: cave.username,
    exp: cave.exp,
    stones: cave.stones,
    favs: cave.favs,
    history: cave.history,
    signDate: cave.signDate,
    trainDate: cave.trainDate,
    trainExp: cave.trainExp,
    savedAt: cave.savedAt
  }
}

/** 把一份存档写进内存状态 */
function applyState(s) {
  if (!s) return
  cave.exp = Number(s.exp) || 0
  cave.stones = Number(s.stones) || 0
  cave.favs = Array.isArray(s.favs) ? s.favs : []
  cave.history = Array.isArray(s.history) ? s.history : []
  cave.signDate = typeof s.signDate === 'string' ? s.signDate : ''
  cave.trainDate = typeof s.trainDate === 'string' ? s.trainDate : ''
  cave.trainExp = Number(s.trainExp) || 0
  cave.savedAt = Number(s.savedAt) || 0
}

/** 本地是否有值得保留的数据（用于判断是否要迁到数据库） */
function hasLocalData() {
  return (
    cave.exp > 0 ||
    cave.stones > 0 ||
    cave.favs.length > 0 ||
    cave.history.length > 0 ||
    !!cave.signDate
  )
}

/** 写本地缓存（离线可用） */
function writeLocal() {
  try {
    localStorage.setItem(storageKey(cave.username), JSON.stringify(snapshot()))
  } catch {
    /* 隐私模式忽略 */
  }
}

/** 读本地缓存；旧版本数据无 savedAt，视为待迁移 */
function readLocal() {
  try {
    const raw = localStorage.getItem(storageKey(cave.username))
    const saved = raw ? JSON.parse(raw) : null
    if (!saved) return null
    applyState(saved)
    return saved
  } catch {
    return null
  }
}

/* ==================== 数据库同步 ==================== */

let pushTimer = null
let pushing = false
let pushAgain = false

/** 防抖推送：本地改完稍等片刻再落库，避免连点刷库 */
function schedulePush() {
  if (typeof fetch !== 'function') return
  clearTimeout(pushTimer)
  pushTimer = setTimeout(() => pushToServer(), PUSH_DELAY)
}

/** 立即把当前存档写入数据库；失败则静默重试于下次改动 */
async function pushToServer(force = false) {
  if (typeof fetch !== 'function') return
  if (pushing) {
    pushAgain = true
    return
  }
  if (force) cave.savedAt = Date.now()
  const at = cave.savedAt
  const name = cave.username
  pushing = true
  try {
    const res = await fetch(`${API_CAVE}/save`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(snapshot())
    })
    const json = await res.json().catch(() => null)
    if (json?.success) {
      cave.synced = true
      const r = json.data
      // 云端记录更新（例如另开过页面）：以云端为准，避免互相覆盖
      if (r?.newer && r.state && Number(r.state.savedAt || 0) > at && name === cave.username) {
        applyState(r.state)
        writeLocal()
      }
    } else {
      cave.synced = false
    }
  } catch {
    cave.synced = false
  } finally {
    pushing = false
    if (pushAgain) {
      pushAgain = false
      schedulePush()
    }
  }
}

/** 从数据库拉取存档并按新旧合并 */
async function pullFromServer() {
  if (typeof fetch !== 'function') return
  const name = cave.username
  try {
    const res = await fetch(`${API_CAVE}?username=${encodeURIComponent(name)}`)
    const json = await res.json().catch(() => null)
    if (!json?.success || name !== cave.username) return
    const remote = json.data?.state || null
    const remoteTime = Number(remote?.savedAt || 0)
    const localTime = Number(cave.savedAt || 0)

    if (!remote) {
      // 库里没有：把本地已有数据（旧版本存档）迁进去，下次打开直接读库
      if (hasLocalData()) await pushToServer(true)
      return
    }
    if (remoteTime > localTime) {
      // 云端更新：采用云端
      applyState(remote)
      cave.cloud = true
      writeLocal()
    } else if (localTime > remoteTime) {
      // 本地更新：上传覆盖
      await pushToServer()
    } else if (hasLocalData() && Number(cave.exp) > Number(remote.exp)) {
      // 时间戳相同（旧数据无时间戳）：修为更高者为准
      await pushToServer(true)
    } else {
      applyState(remote)
      cave.cloud = true
      writeLocal()
    }
    cave.synced = true
  } catch {
    cave.synced = false
  }
}

/** 从数据库加载境界配置；失败则沿用内置配置 */
export async function loadRealms() {
  if (typeof fetch !== 'function') return
  try {
    const res = await fetch(API_REALMS)
    const json = await res.json().catch(() => null)
    const d = json?.data
    if (json?.success && Array.isArray(d?.realms) && d.realms.length) {
      realms.value = d.realms.map((r) => ({
        name: r.name,
        suffix: r.suffix || '境',
        unit: r.unit || '重',
        segs: Number(r.segs) || 10,
        span: Number(r.span) || 0,
        step: r.step || 'first'
      }))
      if (Array.isArray(d.stages) && d.stages.length) STEPS.value = d.stages
      realmSource.value = 'db'
    }
  } catch {
    /* 后端未启动：沿用内置配置 */
  }
}

/* ==================== 持久化入口 ==================== */

/** 持久化：先落本地缓存（立刻生效），再防抖推数据库 */
function persist() {
  cave.savedAt = Date.now()
  writeLocal()
  schedulePush()
}

/**
 * 开辟（或载入）某道号的洞府
 * 同步先读本地缓存避免白屏，随后异步与数据库合并
 */
export async function initCave(username) {
  cave.username = username || '道友'
  cave.cloud = false
  readLocal()
  await Promise.all([loadRealms(), pullFromServer()])
}

/**
 * 离开洞府（登出 / 换号）：仅清空内存与本地缓存
 * 数据库存档保留，下次同名道号登录仍能读回
 */
export function clearCave() {
  clearTimeout(pushTimer)
  // 还有未落库的改动：立刻提交（pushToServer 在首个 await 前已取好快照）
  if (cave.savedAt > 0 && hasLocalData()) pushToServer()
  pushAgain = false
  cave.exp = 0
  cave.stones = 0
  cave.favs = []
  cave.history = []
  cave.signDate = ''
  cave.trainDate = ''
  cave.trainExp = 0
  cave.savedAt = 0
  cave.synced = false
  cave.cloud = false
}

/** 重置洞府：清空内存、本地缓存，并删除数据库存档（不可恢复） */
export async function resetCave() {
  clearTimeout(pushTimer)
  pushAgain = false
  const name = cave.username
  cave.exp = 0
  cave.stones = 0
  cave.favs = []
  cave.history = []
  cave.signDate = ''
  cave.trainDate = ''
  cave.trainExp = 0
  cave.savedAt = 0
  try {
    localStorage.removeItem(storageKey(name))
  } catch {
    /* 忽略 */
  }
  if (typeof fetch === 'function') {
    try {
      await fetch(`${API_CAVE}?username=${encodeURIComponent(name)}`, { method: 'DELETE' })
    } catch {
      /* 断网时本地已清空，下次改动会重新写库 */
    }
  }
}

/* ==================== 藏书阁 / 足迹 ==================== */

/** 是否已收录某内容 */
export function isFav(id) {
  return id == null ? false : cave.favs.some((f) => String(f.id) === String(id))
}

/** 收录进藏书阁：+5000 修为 */
export function addFav(item) {
  if (!item || item.id == null) return { ok: false, exp: 0, msg: '内容无效' }
  if (isFav(item.id)) return { ok: false, exp: 0, msg: '已在藏书阁中' }
  if (cave.favs.length >= FAVS_MAX) return { ok: false, exp: 0, msg: '藏书阁已满' }
  cave.favs = [
    { id: item.id, title: item.title || '无题', categoryPath: item.categoryPath || '', createdAt: Date.now() },
    ...cave.favs
  ]
  cave.exp += FAV_EXP
  persist()
  return { ok: true, exp: FAV_EXP, msg: '已收入藏书阁' }
}

/** 移出藏书阁 */
export function removeFav(id) {
  if (id != null) {
    cave.favs = cave.favs.filter((f) => String(f.id) !== String(id))
    persist()
  }
}

/** 记录研读足迹：首次 +15 修为，重读不加分 */
export function pushHistory(item) {
  if (!item || item.id == null) return { first: false, exp: 0, msg: '' }
  if (cave.history.some((h) => String(h.id) === String(item.id))) {
    return { first: false, exp: 0, msg: '重读旧卷' }
  }
  cave.history = [
    { id: item.id, title: item.title || '无题', categoryPath: item.categoryPath || '', readAt: Date.now() },
    ...cave.history
  ]
  if (cave.history.length > HISTORY_MAX) cave.history.length = HISTORY_MAX
  cave.exp += HISTORY_EXP
  persist()
  return { first: true, exp: HISTORY_EXP, msg: '首次研读' }
}

/* ==================== 签到 ==================== */

/** 今日是否已签到 */
export function isSignedToday() {
  return cave.signDate === today()
}

/** 签到：+50 灵石 / +10 修为，每日一次 */
export function sign() {
  if (isSignedToday()) {
    return { ok: false, stones: 0, exp: 0, msg: '今日已签到，明日再来' }
  }
  cave.signDate = today()
  cave.stones += SIGN_STONES
  cave.exp += SIGN_EXP
  persist()
  return { ok: true, stones: SIGN_STONES, exp: SIGN_EXP, msg: `签到成功 · 灵石 +${SIGN_STONES}` }
}

/* =============================================================
 * 修炼 · 小游戏所得修为
 * 每日上限 TRAIN_LIMIT，跨日自动重置当日计数
 * ============================================================= */

/** 今日修炼尚可得的修为 */
export function trainRemaining() {
  if (cave.trainDate !== today()) return TRAIN_LIMIT
  return Math.max(0, TRAIN_LIMIT - cave.trainExp)
}

/**
 * 修炼结算：按今日剩余额度入账
 * @param {number} amount 本次应得修为
 * @returns {{ok:boolean, exp:number, capped:boolean, left:number, levelUp:boolean, realm:object, msg:string}}
 */
export function addTrainExp(amount) {
  const want = Math.max(0, Math.floor(Number(amount) || 0))
  if (cave.trainDate !== today()) {
    cave.trainDate = today()
    cave.trainExp = 0
  }
  const left = Math.max(0, TRAIN_LIMIT - cave.trainExp)
  const real = Math.min(want, left)

  if (real <= 0) {
    const capped = left <= 0
    return {
      ok: false,
      exp: 0,
      capped,
      left,
      levelUp: false,
      realm: realm.value,
      msg: capped ? '今日修炼已满，明日再续' : '此番未得修为，且再入定一试'
    }
  }

  const before = realm.value
  cave.exp += real
  cave.trainExp += real
  persist()
  const after = realm.value
  const brokeThrough = after.name !== before.name
  const levelUp = brokeThrough || after.level !== before.level

  return {
    ok: true,
    exp: real,
    capped: real < want,
    left: Math.max(0, TRAIN_LIMIT - cave.trainExp),
    levelUp,
    realm: after,
    msg: brokeThrough
      ? `修为 +${real} · 破境至${after.name}${after.suffix}第 ${after.level} ${after.unit}`
      : levelUp
        ? `修为 +${real} · ${after.name}${after.suffix}第 ${after.level} ${after.unit}`
        : `修为 +${real}`
  }
}

/** 灵石入账（采石等玩法所得，不受每日修为上限约束） */
export function addStones(amount) {
  const n = Math.max(0, Math.floor(Number(amount) || 0))
  if (n <= 0) return 0
  cave.stones += n
  persist()
  return n
}

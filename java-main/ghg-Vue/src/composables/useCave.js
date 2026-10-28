import { reactive, computed } from 'vue'

/* =============================================================
 * 我的洞府 · 修为 / 灵石 / 藏书阁 / 足迹 / 签到
 * 状态保存在内存（reactive），数据按道号持久化到 localStorage
 * ============================================================= */

/**
 * 仙逆境界表（依原著四大步序列）
 * - segs：该境内的层次数（凝气十五层、踏天九桥，其余十重）
 * - span：该境所需修为
 * - unit：重数单位；suffix：境界后缀（前期称"期"，第二步起称"境"）
 * - step：所属大阶段，用于境界表分组
 */
const REALMS = [
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
export const STEPS = [
  { key: 'first', badge: '第一步', label: '凡蜕问道', note: '引气入体至问鼎，凡躯蜕为仙体' },
  { key: 'bridge1', badge: '过渡', label: '阴虚阳实', note: '元神吐纳元力、化元完成，方入第二步' },
  { key: 'second', badge: '第二步', label: '规则掌控', note: '窥涅天仙 · 净涅仙王 · 碎涅仙君，碎涅圆满需渡天人五衰' },
  { key: 'third', badge: '第三步', label: '本源超脱', note: '空涅仙帝至空劫大天尊，言出法随，需渡九次玄劫' },
  { key: 'fourth', badge: '第四步', label: '踏天超脱', note: '踏天九桥，一步一桥，明悟轮回真我' }
]

/** 收录藏书阁一次的修为 */
const FAV_EXP = 20
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

/** 洞府全局状态 */
export const cave = reactive({
  username: '道友',
  exp: 0,
  stones: 0,
  favs: [],      // 藏书阁：{ id, title, categoryPath, createdAt }
  history: [],   // 足迹：{ id, title, categoryPath, readAt }
  signDate: '',  // 最近签到日期 yyyy-mm-dd
  trainDate: '', // 最近修炼日期 yyyy-mm-dd
  trainExp: 0    // 当日修炼（小游戏）已得修为
})

/** 当前境界：{ name, suffix, unit, level }，level 1~segs（凝气十五层、踏天九桥、其余十重） */
export const realm = computed(() => {
  let base = 0
  for (const r of REALMS) {
    if (cave.exp < base + r.span) {
      const ratio = (cave.exp - base) / r.span
      const level = Math.min(r.segs, Math.floor(ratio * r.segs) + 1)
      return { name: r.name, suffix: r.suffix, unit: r.unit, level, segs: r.segs }
    }
    base += r.span
  }
  const last = REALMS[REALMS.length - 1]
  return { name: last.name, suffix: last.suffix, unit: last.unit, level: last.segs, segs: last.segs }
})

/** 当前境界内进度 0~1（用于修为条宽度） */
export const realmProgress = computed(() => {
  let base = 0
  for (const r of REALMS) {
    if (cave.exp < base + r.span) return (cave.exp - base) / r.span
    base += r.span
  }
  return 1
})

/** 距下一境圆满所需修为：{ name, need }；踏天九桥圆满后为 null */
export const nextRealm = computed(() => {
  let base = 0
  for (const r of REALMS) {
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
  for (const r of REALMS) {
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

/** 九境圆满所需累计修为 */
export const REALM_TOTAL = REALMS.reduce((s, r) => s + r.span, 0)

/** 境界总数 */
export const REALM_COUNT = REALMS.length

/** 今日日期 yyyy-mm-dd */
function today() {
  const d = new Date()
  const m = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  return `${d.getFullYear()}-${m}-${day}`
}

/** 某道号的洞府存储键 */
function storageKey(name) {
  return `xianni_cave_${name || 'guest'}`
}

/** 持久化当前洞府 */
function persist() {
  try {
    localStorage.setItem(
      storageKey(cave.username),
      JSON.stringify({
        exp: cave.exp,
        stones: cave.stones,
        favs: cave.favs,
        history: cave.history,
        signDate: cave.signDate,
        trainDate: cave.trainDate,
        trainExp: cave.trainExp
      })
    )
  } catch {
    /* 隐私模式忽略 */
  }
}

/** 载入（或开辟）某道号的洞府 */
export function initCave(username) {
  cave.username = username || '道友'
  try {
    const raw = localStorage.getItem(storageKey(cave.username))
    const saved = raw ? JSON.parse(raw) : null
    cave.exp = Number(saved?.exp) || 0
    cave.stones = Number(saved?.stones) || 0
    cave.favs = Array.isArray(saved?.favs) ? saved.favs : []
    cave.history = Array.isArray(saved?.history) ? saved.history : []
    cave.signDate = typeof saved?.signDate === 'string' ? saved.signDate : ''
    cave.trainDate = typeof saved?.trainDate === 'string' ? saved.trainDate : ''
    cave.trainExp = Number(saved?.trainExp) || 0
  } catch {
    cave.exp = 0
    cave.stones = 0
    cave.favs = []
    cave.history = []
    cave.signDate = ''
    cave.trainDate = ''
    cave.trainExp = 0
  }
}

/** 重置洞府：清空内存与本地存储 */
export function resetCave() {
  cave.exp = 0
  cave.stones = 0
  cave.favs = []
  cave.history = []
  cave.signDate = ''
  cave.trainDate = ''
  cave.trainExp = 0
  try {
    localStorage.removeItem(storageKey(cave.username))
  } catch {
    /* 忽略 */
  }
}

/** 是否已收录某内容 */
export function isFav(id) {
  return id == null ? false : cave.favs.some((f) => String(f.id) === String(id))
}

/** 收录进藏书阁：+20 修为 */
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

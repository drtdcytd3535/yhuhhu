import { reactive, computed } from 'vue'

/* =============================================================
 * 我的洞府 · 修为 / 灵石 / 藏书阁 / 足迹 / 签到
 * 状态保存在内存（reactive），数据按道号持久化到 localStorage
 * ============================================================= */

/** 境界表：累计修为（exp）逐级推进 */
const REALMS = [
  { name: '炼气', span: 100 },
  { name: '筑基', span: 300 },
  { name: '结丹', span: 700 },
  { name: '元婴', span: 1500 },
  { name: '化神', span: 3000 },
  { name: '炼虚', span: 6000 },
  { name: '合体', span: 12000 },
  { name: '大乘', span: 25000 },
  { name: '渡劫', span: 50000 }
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

/** 当前境界：{ name, level }，level 1~10 重 */
export const realm = computed(() => {
  let base = 0
  for (const r of REALMS) {
    if (cave.exp < base + r.span) {
      const ratio = (cave.exp - base) / r.span
      const level = Math.min(10, Math.floor(ratio * 10) + 1)
      return { name: r.name, level }
    }
    base += r.span
  }
  return { name: '渡劫', level: 10 }
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

/** 距下一境圆满所需修为：{ name, need }；渡劫圆满后为 null */
export const nextRealm = computed(() => {
  let base = 0
  for (const r of REALMS) {
    if (cave.exp < base + r.span) {
      return { name: r.name, need: base + r.span - cave.exp }
    }
    base += r.span
  }
  return null
})

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
      ? `修为 +${real} · 破境至${after.name}第 ${after.level} 重`
      : levelUp
        ? `修为 +${real} · ${after.name}境第 ${after.level} 重`
        : `修为 +${real}`
  }
}

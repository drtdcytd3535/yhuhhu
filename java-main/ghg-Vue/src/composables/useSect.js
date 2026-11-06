import { reactive } from 'vue'
import { cave, realms, wornStats, battlePower, addStones } from './useCave.js'

/* =============================================================
 * 云岚宗 · 十三人试
 *
 * 玩法规则（需求对齐）：
 *   1. 名册十三人，由弱到强：自外门弟子起修为逐位递增，宗主压轴且修为最高；
 *   2. 须「打完一个再打一个」——前一位未败，后一位不可挑战；
 *   3. 胜负先比修为：修为高者胜；
 *   4. 修为相同则比属性（装备加成折算的战力），战力高者胜；
 *   5. 修为与战力皆同，判为平手，需精进后再来。
 *
 * 进度只记「已击败成员 id」，按道号存数据库（t_sect，/api/sect），
 * localStorage 仅作离线缓存：换浏览器、清缓存、换设备登录，战绩都还在。
 * ============================================================= */

/** 宗门名 */
export const SECT_NAME = '云岚宗'

/**
 * 云岚宗名册原始表（宗主在前，修为递减，便于对照）
 * - exp：该人修为（对应仙逆境界阶梯）
 * - stats：战斗属性，与装备加成同一套字段（atk/def/hp/spd/crit）
 * - reward：首次击败所得灵石
 */
const ROSTER_DESC = [
  {
    id: 'yunshan',
    name: '云山',
    title: '宗主',
    exp: 92470,
    stats: { atk: 78, def: 44, hp: 850, spd: 30, crit: 14 },
    reward: 900,
    line: '云岚宗百年基业，岂容外人踏足山门。'
  },
  {
    id: 'yunyun',
    name: '云韵',
    title: '副宗主',
    exp: 12600,
    stats: { atk: 61, def: 43, hp: 350, spd: 26, crit: 11 },
    reward: 700,
    line: '宗主之下，尚有我一关。'
  },
  {
    id: 'nalan-yanran',
    name: '纳兰嫣然',
    title: '少宗主',
    exp: 8900,
    stats: { atk: 55, def: 38, hp: 310, spd: 24, crit: 10 },
    reward: 560,
    line: '三年之约未至，先过我这一剑。'
  },
  {
    id: 'yunleng',
    name: '云棱',
    title: '大长老',
    exp: 6400,
    stats: { atk: 49, def: 35, hp: 280, spd: 21, crit: 9 },
    reward: 440,
    line: '小辈无礼，长老教你规矩。'
  },
  {
    id: 'yunfan',
    name: '云帆',
    title: '二长老',
    exp: 4600,
    stats: { atk: 43, def: 30, hp: 245, spd: 19, crit: 8 },
    reward: 340,
    line: '长风送帆，也送你下山。'
  },
  {
    id: 'yunsheng',
    name: '云盛',
    title: '三长老',
    exp: 3300,
    stats: { atk: 38, def: 27, hp: 215, spd: 17, crit: 7 },
    reward: 260,
    line: '宗门气象正盛，不容轻慢。'
  },
  {
    id: 'yunlei',
    name: '云雷',
    title: '执剑长老',
    exp: 2400,
    stats: { atk: 36, def: 20, hp: 175, spd: 19, crit: 6 },
    reward: 220,
    line: '剑出如雷，躲得开再说话。'
  },
  {
    id: 'yunhe',
    name: '云鹤',
    title: '丹堂长老',
    exp: 1750,
    stats: { atk: 30, def: 21, hp: 160, spd: 13, crit: 6 },
    reward: 170,
    line: '药可救人，亦可送你一程。'
  },
  {
    id: 'yunxiao',
    name: '云啸',
    title: '刑罚长老',
    exp: 1200,
    stats: { atk: 26, def: 17, hp: 130, spd: 13, crit: 5 },
    reward: 130,
    line: '犯我宗规者，先吃我一啸。'
  },
  {
    id: 'hanfeng',
    name: '韩枫',
    title: '客卿药师',
    exp: 820,
    stats: { atk: 22, def: 13, hp: 105, spd: 10, crit: 4 },
    reward: 100,
    line: '外客而已，何必认真。'
  },
  {
    id: 'yunzhonghe',
    name: '云中鹤',
    title: '内门首席',
    exp: 520,
    stats: { atk: 18, def: 12, hp: 92, spd: 12, crit: 3 },
    reward: 70,
    line: '内门第一，请赐教。'
  },
  {
    id: 'yunxiaolan',
    name: '云小岚',
    title: '内门弟子',
    exp: 310,
    stats: { atk: 15, def: 10, hp: 75, spd: 9, crit: 3 },
    reward: 50,
    line: '我也在云岚宗学剑三年了。'
  },
  {
    id: 'yunshan-youth',
    name: '云杉',
    title: '外门弟子',
    exp: 160,
    stats: { atk: 12, def: 8, hp: 60, spd: 8, crit: 2 },
    reward: 30,
    line: '先过我这关……吧？'
  }
]

/**
 * 挑战名册：由弱到强
 * 原表按修为递减排列（宗主在前），此处反转为「外门弟子 → 宗主」，
 * 挑战顺序与列表顺序一致：先打修为最低者，一路打到修为最高的宗主。
 */
export const SECT_MEMBERS = [...ROSTER_DESC].reverse()

/** 十三人数 */
export const SECT_SIZE = SECT_MEMBERS.length

/* ==================== 境界折算 ==================== */

const CN_NUM = ['零', '一', '二', '三', '四', '五', '六', '七', '八', '九', '十',
  '十一', '十二', '十三', '十四', '十五', '十六', '十七', '十八', '十九', '二十']

function cn(n) {
  return CN_NUM[n] || String(n)
}

/**
 * 以仙逆境界阶梯把「修为数值」折算成可读境界
 * 例：18000 → 空涅境·一重；12600 → 净涅境·十重
 */
export function realmLabel(exp) {
  const list = realms.value || []
  if (!list.length) return '凡人'
  const v = Math.max(0, Number(exp) || 0)
  let base = 0
  for (const r of list) {
    if (v < base + r.span) {
      const ratio = (v - base) / r.span
      const level = Math.min(r.segs, Math.floor(ratio * r.segs) + 1)
      return `${r.name}${r.suffix}·${cn(level)}${r.unit}`
    }
    base += r.span
  }
  const last = list[list.length - 1]
  return `${last.name}${last.suffix}·圆满`
}

/** 某人的战力（含底子，与我方同口径） */
export function memberPower(m) {
  return battlePower(withBase(m.stats))
}

/* ==================== 进度（本地缓存 + 云端落库） ==================== */

const API_SECT = '/api/sect'
const PUSH_DELAY = 320
const SECT_KEY = (name) => `xianni_sect_${name || 'guest'}`

export const sect = reactive({
  username: '',
  cleared: [],   // 已击败成员 id
  savedAt: 0,    // 最后改动时间戳
  cloud: false,  // 是否已从云端读到战绩
  synced: false  // 最近一次落库是否成功
})

/** 只保留名册中存在的 id，并按名册顺序排列 */
function sanitize(list) {
  const ids = Array.isArray(list) ? list : []
  return SECT_MEMBERS.map((m) => m.id).filter((id) => ids.includes(id))
}

function applyCleared(list) {
  sect.cleared = sanitize(list)
}

/** 写本地缓存（离线也能记住） */
function writeLocal() {
  try {
    localStorage.setItem(
      SECT_KEY(sect.username),
      JSON.stringify({ cleared: sect.cleared, savedAt: sect.savedAt })
    )
  } catch {
    /* 隐私模式忽略 */
  }
}

let pushTimer = null

/** 防抖落库：本地先写，稍后推送数据库 */
function schedulePush() {
  if (typeof fetch !== 'function') return
  clearTimeout(pushTimer)
  pushTimer = setTimeout(() => pushSect(), PUSH_DELAY)
}

/** 推送战绩到数据库；云端返回并集后回写，多端互不覆盖 */
async function pushSect() {
  if (typeof fetch !== 'function') return
  const name = sect.username
  if (!name) return
  const at = sect.savedAt
  try {
    const res = await fetch(`${API_SECT}/save`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ username: name, cleared: sect.cleared, savedAt: at }),
      // 关页/刷新瞬间也能把最后一次战果发完
      keepalive: true
    })
    const json = await res.json().catch(() => null)
    if (json?.success && name === sect.username) {
      sect.synced = true
      sect.cloud = true
      const merged = json.data?.cleared
      if (Array.isArray(merged)) applyCleared(merged)
      writeLocal()
    } else {
      sect.synced = false
    }
  } catch {
    sect.synced = false
  }
}

/** 从数据库拉取战绩，与本地取并集（战绩只增不减） */
async function pullFromServer() {
  if (typeof fetch !== 'function') return
  const name = sect.username
  try {
    const res = await fetch(`${API_SECT}?username=${encodeURIComponent(name)}`)
    const json = await res.json().catch(() => null)
    if (!json?.success || name !== sect.username) return
    const remote = Array.isArray(json.data?.cleared) ? json.data.cleared : []
    const merged = sanitize([...sect.cleared, ...remote])
    const grew = merged.length > sect.cleared.length
    applyCleared(merged)
    sect.cloud = true
    sect.synced = true
    if (grew) {
      // 云端战绩更多（换了设备或清了缓存）：回写一次，把本地补齐
      sect.savedAt = Math.max(sect.savedAt, Number(json.data?.savedAt) || 0)
      writeLocal()
      schedulePush()
    } else {
      sect.savedAt = Math.max(sect.savedAt, Number(json.data?.savedAt) || 0)
      writeLocal()
    }
  } catch {
    sect.synced = false
  }
}

/**
 * 载入某道号的宗门进度：先读本地保证瞬开，再异步拉云端取并集
 */
export function initSect(username) {
  sect.username = username || '道友'
  sect.cleared = []
  sect.savedAt = 0
  sect.cloud = false
  sect.synced = false
  try {
    const raw = localStorage.getItem(SECT_KEY(sect.username))
    const saved = raw ? JSON.parse(raw) : null
    if (saved && Array.isArray(saved.cleared)) {
      applyCleared(saved.cleared)
      sect.savedAt = Number(saved.savedAt) || 0
    }
  } catch {
    /* 隐私模式 / 脏数据：从零开始 */
  }
  pullFromServer()
  return sect
}

/** 重头再战：清空本地与云端进度（已得灵石不回退） */
export function resetSect() {
  sect.cleared = []
  sect.savedAt = Date.now()
  writeLocal()
  if (typeof fetch === 'function' && sect.username) {
    clearTimeout(pushTimer)
    fetch(`${API_SECT}?username=${encodeURIComponent(sect.username)}`, {
      method: 'DELETE',
      keepalive: true
    }).catch(() => {})
  }
}

/** 登出：只清内存，不碰本地缓存与数据库（换号登录不串数据） */
export function clearSect() {
  clearTimeout(pushTimer)
  sect.username = ''
  sect.cleared = []
  sect.savedAt = 0
  sect.cloud = false
  sect.synced = false
}

/** 记录一场胜绩：本地立即留存，随后推送数据库 */
export function markCleared(id) {
  if (!id || sect.cleared.includes(id)) return false
  sect.cleared = sanitize([...sect.cleared, id])
  sect.savedAt = Date.now()
  writeLocal()
  // 胜绩不容有失：即时推送一次，关页也不丢
  clearTimeout(pushTimer)
  pushSect()
  return true
}

/** 是否已击败某人 */
export function isDefeated(member) {
  return sect.cleared.includes(member.id)
}

/** 第 index 位是否已解锁：首位常开，其余须前一位已败 */
export function isUnlocked(index) {
  if (index <= 0) return SECT_SIZE > 0
  return isDefeated(SECT_MEMBERS[index - 1])
}

/** 已击败人数 */
export function clearedCount() {
  return sect.cleared.length
}

/* ==================== 斗法：数值基础 ==================== */

/** 双方共用的底子（裸装/无宝之人也有筋骨，不是纸人） */
export const PLAYER_BASE = { atk: 14, def: 8, hp: 90, spd: 10, crit: 4 }

/** 底子 + 加成（法宝或成员属性），逐项相加 */
export function withBase(stats) {
  const s = stats || {}
  return {
    atk: PLAYER_BASE.atk + (Number(s.atk) || 0),
    def: PLAYER_BASE.def + (Number(s.def) || 0),
    hp: PLAYER_BASE.hp + (Number(s.hp) || 0),
    spd: PLAYER_BASE.spd + (Number(s.spd) || 0),
    crit: PLAYER_BASE.crit + (Number(s.crit) || 0)
  }
}

/**
 * 我方实战属性 = 底子 + 佩戴法宝加成 + 修为养出的气血
 * 门人属性表已含其修为底蕴，我方则以修为折算气血补足，两下口径才可比
 */
export function playerStats() {
  const s = withBase(wornStats.value)
  const e = Math.max(0, Number(cave.exp) || 0)
  s.hp += Math.round(Math.pow(e, 0.6) * 0.8)
  return s
}

/** 我方实战战力（含底子，与名册「战力」同口径） */
export function playerPower() {
  return battlePower(playerStats())
}

/** 单场最多回合数，逾此按残余气血比例定胜负 */
export const MAX_ROUND = 20

/**
 * 修为压制：修为高者伤害更重、受创更轻
 * 修为等同 → 1.00；高出一半 → 约 1.5；不及其半 → 约 0.4，差距愈大愈悬殊
 */
export function suppress(myExp, theirExp) {
  const a = Math.max(0, Number(myExp) || 0)
  const b = Math.max(0, Number(theirExp) || 0)
  const m = Math.max(a, b, 1)
  const diff = (a - b) / m           // -1 ~ 1
  return Math.min(2.4, Math.max(0.25, 1 + 1.25 * diff))
}

/**
 * 招式：每回合三选一，攻守各有取舍
 * - atk：本回合攻击倍率
 * - def：本回合防御倍率（同时抵挡对方来袭）
 * - taken：本回合受创倍率
 * - heal：本回合回气比例（占最大气血）
 */
export const TACTICS = [
  {
    key: 'attack',
    name: '强攻',
    ico: '⚔',
    desc: '攻势凌厉 · 攻 +30% · 守 -20%',
    atk: 1.3,
    def: 0.8,
    taken: 1,
    heal: 0
  },
  {
    key: 'guard',
    name: '守御',
    ico: '⛨',
    desc: '以守待攻 · 守 +70% · 攻 -30% · 回气 6%',
    atk: 0.7,
    def: 1.7,
    taken: 0.85,
    heal: 0.06
  },
  {
    key: 'burst',
    name: '拼死',
    ico: '✺',
    desc: '搏命一击 · 攻 +75% · 自伤 +35%',
    atk: 1.75,
    def: 0.9,
    taken: 1.35,
    heal: 0
  }
]

/** 一击伤害：攻击 × 招式 × 修为压制 × 防御减免 × 暴击 × 受创系数 */
function strike(attacker, defender, sup, atkMul, defMul, takenMul) {
  const crit = Math.random() * 100 < attacker.crit
  const effDef = defender.def * defMul
  const raw = attacker.atk * atkMul * sup * (100 / (100 + effDef))
  const dmg = Math.max(1, Math.round(raw * (crit ? 1.8 : 1) * takenMul))
  return { dmg, crit }
}

/** 对方的出招：血少拼命，血厚试探，偶尔守御 */
function foeTactic(foe) {
  const ratio = foe.hp / foe.maxHp
  if (ratio < 0.3) return TACTICS[2]
  if (ratio < 0.65) return TACTICS[0]
  return Math.random() < 0.3 ? TACTICS[1] : TACTICS[0]
}

/* ==================== 斗法：开战与回合 ==================== */

/**
 * 开一场斗法（不改存档，胜负结算见 settleDuel）
 * @returns {{ok:boolean, msg:string, state:object|null}}
 */
export function startDuel(index) {
  const member = SECT_MEMBERS[index]
  if (!member) return { ok: false, msg: '此位不在名册之中', state: null }
  if (!isUnlocked(index)) {
    const prev = SECT_MEMBERS[index - 1]
    return { ok: false, msg: `须先胜过「${prev ? prev.name : '前一位'}」`, state: null }
  }

  const ps = playerStats()
  const fs = withBase(member.stats)

  return {
    ok: true,
    msg: `请战「${member.name}」`,
    state: {
      index,
      member,
      round: 0,
      maxRound: MAX_ROUND,
      over: false,
      win: false,
      draw: false,
      byTimeout: false,
      log: [],
      me: {
        name: '我',
        exp: Math.max(0, Number(cave.exp) || 0),
        stats: ps,
        hp: ps.hp,
        maxHp: ps.hp
      },
      foe: {
        name: member.name,
        exp: member.exp,
        stats: fs,
        hp: fs.hp,
        maxHp: fs.hp
      }
    }
  }
}

/**
 * 走一回合（原地修改 state）
 * @param {object} st startDuel 给出的 state
 * @param {string} tacticKey attack | guard | burst
 * @returns {{events:Array<{side:string, crit:boolean, text:string}>, over:boolean, win:boolean, draw:boolean}}
 */
export function duelRound(st, tacticKey) {
  if (!st || st.over) {
    return { events: [], over: true, win: st ? st.win : false, draw: st ? st.draw : false }
  }

  const me = st.me
  const foe = st.foe
  const t = TACTICS.find((x) => x.key === tacticKey) || TACTICS[0]
  const ft = foeTactic(foe)
  const events = []

  st.round += 1

  // 守御调息：先回一口气
  if (t.heal > 0 && me.hp > 0) {
    const n = Math.min(me.maxHp - me.hp, Math.round(me.maxHp * t.heal))
    if (n > 0) {
      me.hp += n
      events.push({ side: 'me', crit: false, text: `调息回气 +${n}` })
    }
  }

  const mySup = suppress(me.exp, foe.exp)
  const foeSup = suppress(foe.exp, me.exp)

  const myHit = () => {
    if (foe.hp <= 0) return
    const r = strike(me.stats, foe.stats, mySup, t.atk, ft.def, ft.taken)
    foe.hp = Math.max(0, foe.hp - r.dmg)
    events.push({
      side: 'me',
      crit: r.crit,
      text: `${t.name}一击${r.crit ? ' · 暴击' : ''} · 伤其 ${r.dmg}`
    })
  }
  const foeHit = () => {
    if (me.hp <= 0) return
    const r = strike(foe.stats, me.stats, foeSup, ft.atk, t.def, t.taken)
    me.hp = Math.max(0, me.hp - r.dmg)
    events.push({
      side: 'foe',
      crit: r.crit,
      text: `${foe.name}${ft.name}${r.crit ? ' · 暴击' : ''} · 伤我 ${r.dmg}`
    })
  }

  // 身法高者先出手；先手若将对方打空，则无还手之机
  if (me.stats.spd >= foe.stats.spd) {
    myHit()
    if (foe.hp > 0) foeHit()
  } else {
    foeHit()
    if (me.hp > 0) myHit()
  }

  if (foe.hp <= 0) {
    st.over = true
    st.win = true
  } else if (me.hp <= 0) {
    st.over = true
    st.win = false
  } else if (st.round >= st.maxRound) {
    // 久战不下：以残余气血比例定高下
    const a = me.hp / me.maxHp
    const b = foe.hp / foe.maxHp
    st.over = true
    st.byTimeout = true
    if (Math.abs(a - b) < 0.02) {
      st.draw = true
      st.win = false
    } else {
      st.win = a > b
    }
  }

  st.log.push({
    round: st.round,
    line: events.map((e) => e.text).join('　'),
    win: st.over ? st.win : null
  })

  return { events, over: st.over, win: st.win, draw: st.draw }
}

/**
 * 结算一场斗法：胜利且首通者记入名册并赏灵石
 * @returns {{win:boolean, draw:boolean, first:boolean, reward:number, member:object, msg:string}}
 */
export function settleDuel(st) {
  const member = st.member
  const first = !isDefeated(member)
  let reward = 0

  if (st.win && first) {
    markCleared(member.id)
    reward = addStones(member.reward)
  }

  const msg = st.win
    ? first
      ? `胜「${member.name}」· 灵石 +${reward}`
      : `再胜「${member.name}」· 首通之赏已领`
    : st.draw
      ? `与「${member.name}」战至力竭，未分胜负`
      : `败于「${member.name}」之手`

  return { win: st.win, draw: st.draw, first, reward, member, msg }
}


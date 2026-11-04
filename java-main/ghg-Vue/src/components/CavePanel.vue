<template>
  <section class="cave">
    <!-- ================= 头部：返回 + 标题 ================= -->
    <div class="cave-head">
      <button class="cave-back" @click="emit('back')">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8">
          <path d="M15 5 8 12l7 7" />
        </svg>
        返回仙门
      </button>
      <div class="cave-title">
        <h2>我的洞府</h2>
        <p>
          {{ username }} · 洞天福地
          <span class="sync-tag" :class="{ ok: cave.synced }">
            <i></i>{{ cave.synced ? '已存云端' : '离线暂存' }}
          </span>
        </p>
      </div>
    </div>

    <!-- ================= 修为面板 ================= -->
    <div class="cave-panel culti">
      <div class="culti-left">
        <p class="culti-label">当前境界</p>
        <p class="culti-realm">{{ realm.name }}{{ realm.suffix }}</p>
        <p class="culti-level">第 {{ realm.level }} {{ realm.unit }}</p>
      </div>

      <div class="culti-mid">
        <div class="culti-bar">
          <i :style="{ width: progressPct + '%' }"></i>
        </div>
        <p class="culti-tip">
          {{ next ? `距 ${next.name}${next.suffix}圆满还需 ${next.need} 修为` : '已踏完第九桥 · 逆天超脱' }}
        </p>
      </div>

      <div class="culti-right">
        <p class="culti-stones">◈ {{ cave.stones }}</p>
        <p class="culti-stones-label">灵石</p>
      </div>

      <button class="cave-sign" :class="{ done: signed }" @click="onSign">
        {{ signed ? '今日已签到' : '签到 · 灵石 +50' }}
      </button>
    </div>

    <!-- ================= 境界总览 ================= -->
    <RealmTable class="cave-realm" />

    <!-- ================= 仙逆动漫 ================= -->
    <div class="cave-panel">
      <div class="panel-head">
        <h3>仙逆动漫</h3>
        <span class="panel-count">腾讯视频 · 全季</span>
      </div>
      <div
        class="anime-card"
        role="button"
        tabindex="0"
        @click="openAnime"
        @keyup.enter="openAnime"
        @keyup.space.prevent="openAnime"
      >
        <span class="anime-seal">仙逆</span>
        <div class="anime-info">
          <p class="anime-name">仙逆 · 逆天改命</p>
          <p class="anime-desc">王林自恒岳派起步，问道长生、踏天逆凡。洞府打坐之余，可观此界影像。</p>
        </div>
        <span class="anime-play">
          <svg viewBox="0 0 24 24" fill="currentColor" aria-hidden="true">
            <path d="M8 5.5v13l11-6.5-11-6.5Z" />
          </svg>
          前往观看
        </span>
      </div>
    </div>

    <!-- ================= 藏书阁 / 修道足迹（可展开 · 可合并） ================= -->
    <div class="cave-panel">
      <div class="panel-head">
        <h3>{{ merged ? '藏迹合璧' : '藏书阁' }}</h3>
        <span class="panel-count">{{ merged ? mergedList.length + ' 卷' : cave.favs.length + ' 卷' }}</span>
        <div class="panel-tools">
          <button class="panel-tool" :class="{ on: merged }" @click="toggleMerge">
            {{ merged ? '分开查看' : '合并两阁' }}
          </button>
          <button class="panel-tool" @click="toggleFavFold">
            {{ favFolded ? '展开' : '收起' }}
          </button>
        </div>
      </div>

      <!-- ---------- 合并视图：藏书 + 足迹，同卷合一，按最近时间排序 ---------- -->
      <template v-if="merged">
        <template v-if="mergedList.length">
          <ul v-if="!favFolded" class="cave-list">
            <li v-for="m in mergedShown" :key="m.id" @click="emit('open', m.id)">
              <span class="list-tags">
                <i v-if="m.fav" class="list-tag tag-fav">藏</i>
                <i v-if="m.trail" class="list-tag tag-trail">迹</i>
              </span>
              <span class="list-path">{{ m.categoryPath || '未分类' }}</span>
              <span class="list-title">{{ m.title }}</span>
              <span class="list-time">{{ fmtTime(m.lastAt) }}</span>
            </li>
          </ul>
          <p v-else class="cave-empty">已收起 · 点右上「展开」查看 {{ mergedList.length }} 卷典藏</p>
          <button
            v-if="mergedList.length > FOLD_N"
            class="list-more"
            @click="mergedExpanded = !mergedExpanded"
          >
            {{ mergedExpanded ? '收起列表' : `展开全部 ${mergedList.length} 卷` }}
          </button>
        </template>
        <p v-else class="cave-empty">两阁皆空，去首页研读功法、点「收入藏书阁」即可收录。</p>
      </template>

      <!-- ---------- 分开视图：藏书阁 ---------- -->
      <template v-else>
        <template v-if="cave.favs.length">
          <ul v-if="!favFolded" class="cave-list">
            <li v-for="f in favShown" :key="f.id" @click="emit('open', f.id)">
              <span class="list-path">{{ f.categoryPath || '未分类' }}</span>
              <span class="list-title">{{ f.title }}</span>
              <span class="list-time">{{ fmtTime(f.createdAt) }}</span>
            </li>
          </ul>
          <p v-else class="cave-empty">已收起 · 点右上「展开」查看 {{ cave.favs.length }} 卷藏书</p>
          <button v-if="cave.favs.length > FOLD_N" class="list-more" @click="favExpanded = !favExpanded">
            {{ favExpanded ? '收起列表' : `展开全部 ${cave.favs.length} 卷` }}
          </button>
        </template>
        <p v-else class="cave-empty">阁中空空，去首页研读功法后点「收入藏书阁」即可收录。</p>
      </template>
    </div>

    <!-- ================= 修道足迹（合并时并入上一阁） ================= -->
    <div v-if="!merged" class="cave-panel">
      <div class="panel-head">
        <h3>修道足迹</h3>
        <span class="panel-count">{{ cave.history.length }} 卷</span>
        <div class="panel-tools">
          <button class="panel-tool" @click="toggleTrailFold">
            {{ trailFolded ? '展开' : '收起' }}
          </button>
        </div>
      </div>
      <template v-if="cave.history.length">
        <ul v-if="!trailFolded" class="cave-list">
          <li v-for="h in trailShown" :key="h.id" @click="emit('open', h.id)">
            <span class="list-path">{{ h.categoryPath || '未分类' }}</span>
            <span class="list-title">{{ h.title }}</span>
            <span class="list-time">{{ fmtTime(h.readAt) }}</span>
          </li>
        </ul>
        <p v-else class="cave-empty">已收起 · 点右上「展开」查看 {{ cave.history.length }} 卷足迹</p>
        <button v-if="cave.history.length > FOLD_N" class="list-more" @click="trailExpanded = !trailExpanded">
          {{ trailExpanded ? '收起列表' : `展开全部 ${cave.history.length} 卷` }}
        </button>
      </template>
      <p v-else class="cave-empty">尚未留下足迹，翻阅任意典籍即可记录于此。</p>
    </div>

    <!-- ================= 底部：重置 ================= -->
    <div class="cave-foot">
      <button class="cave-reset" :class="{ armed: confirmReset }" @click="onReset">
        {{ confirmReset ? '再点一次 · 确认散去（含云端存档）' : '重置洞府 · 散去修为' }}
      </button>
      <p class="cave-save-tip">
        修为已随道号存于仙门玉册（数据库），换浏览器、清缓存后依旧还在。
      </p>
    </div>
  </section>
</template>

<script setup>
import { computed, ref, onBeforeUnmount } from 'vue'
import RealmTable from './RealmTable.vue'
import {
  cave,
  realm,
  realmProgress,
  nextRealm,
  isSignedToday,
  sign,
  resetCave
} from '../composables/useCave.js'

const props = defineProps({
  username: { type: String, default: '道友' },
  // 动漫外链（腾讯视频《仙逆》专辑页），由首页传入
  animeUrl: { type: String, default: '' }
})

const emit = defineEmits(['back', 'open', 'toast'])

/* =============================================================
 * 藏书阁 / 修道足迹：展开 · 收起 · 合并
 * ============================================================= */
/** 收起状态下列表显示的条数 */
const FOLD_N = 5

const merged = ref(false)          // 是否合并两阁
const favFolded = ref(false)       // 藏书阁（合并时为合璧阁）整体收起
const trailFolded = ref(false)     // 修道足迹整体收起
const favExpanded = ref(false)     // 藏书阁列表展开全部
const trailExpanded = ref(false)   // 足迹列表展开全部
const mergedExpanded = ref(false)  // 合并列表展开全部

/* 界面偏好持久化：合并 / 收起状态下次登录、刷新后仍保留 */
const PANEL_PREF_KEY = 'cave.panelPrefs.v1'

function loadPanelPrefs() {
  try {
    const raw = localStorage.getItem(PANEL_PREF_KEY)
    if (!raw) return
    const p = JSON.parse(raw) || {}
    if (typeof p.merged === 'boolean') merged.value = p.merged
    if (typeof p.favFolded === 'boolean') favFolded.value = p.favFolded
    if (typeof p.trailFolded === 'boolean') trailFolded.value = p.trailFolded
  } catch {
    /* 偏好损坏则用默认 */
  }
}

function savePanelPrefs() {
  try {
    localStorage.setItem(
      PANEL_PREF_KEY,
      JSON.stringify({
        merged: merged.value,
        favFolded: favFolded.value,
        trailFolded: trailFolded.value
      })
    )
  } catch {
    /* 隐私模式等无法写入时忽略 */
  }
}

function toggleFavFold() {
  favFolded.value = !favFolded.value
  savePanelPrefs()
}

function toggleTrailFold() {
  trailFolded.value = !trailFolded.value
  savePanelPrefs()
}

loadPanelPrefs()

const favShown = computed(() => (favExpanded.value ? cave.favs : cave.favs.slice(0, FOLD_N)))
const trailShown = computed(() => (trailExpanded.value ? cave.history : cave.history.slice(0, FOLD_N)))

/**
 * 合并两阁：同一卷（id 相同）只留一条，标记「藏 / 迹」，
 * 按最近一次动作（收藏时间 / 阅读时间）倒序排列。
 */
const mergedList = computed(() => {
  const map = new Map()
  for (const f of cave.favs || []) {
    map.set(f.id, {
      id: f.id,
      title: f.title,
      categoryPath: f.categoryPath,
      fav: true,
      trail: false,
      lastAt: Number(f.createdAt) || 0
    })
  }
  for (const h of cave.history || []) {
    const cur = map.get(h.id)
    const t = Number(h.readAt) || 0
    if (cur) {
      cur.trail = true
      cur.lastAt = Math.max(cur.lastAt, t)
    } else {
      map.set(h.id, {
        id: h.id,
        title: h.title,
        categoryPath: h.categoryPath,
        fav: false,
        trail: true,
        lastAt: t
      })
    }
  }
  return [...map.values()].sort((a, b) => b.lastAt - a.lastAt)
})

const mergedShown = computed(() =>
  mergedExpanded.value ? mergedList.value : mergedList.value.slice(0, FOLD_N)
)

function toggleMerge() {
  merged.value = !merged.value
  mergedExpanded.value = false
  savePanelPrefs()
  emit('toast', merged.value ? '两阁已合璧 · 藏书与足迹同卷归一' : '已分开查看藏书与足迹')
}

/* ---------------- 仙逆动漫 ---------------- */
function openAnime() {
  if (!props.animeUrl) {
    emit('toast', '动漫入口暂未开启')
    return
  }
  window.open(props.animeUrl, '_blank', 'noopener,noreferrer')
  emit('toast', '正在开启腾讯视频 · 《仙逆》动漫')
}

/* ---------------- 签到 / 进度 ---------------- */
const signed = computed(() => isSignedToday())
const progressPct = computed(() => Math.round(realmProgress.value * 100))
const next = computed(() => nextRealm.value)

function onSign() {
  const r = sign()
  emit('toast', r.msg)
}

/* ---------------- 重置（二次确认，因会删除云端存档） ---------------- */
const confirmReset = ref(false)
let confirmTimer = null

function onReset() {
  if (!confirmReset.value) {
    confirmReset.value = true
    emit('toast', '再点一次，散去修为（云端存档一并删除）')
    clearTimeout(confirmTimer)
    confirmTimer = setTimeout(() => (confirmReset.value = false), 4000)
    return
  }
  clearTimeout(confirmTimer)
  confirmReset.value = false
  resetCave()
  emit('toast', '洞府已重置 · 重入仙途')
}

onBeforeUnmount(() => clearTimeout(confirmTimer))

/* ---------------- 时间展示：MM-DD HH:mm ---------------- */
function fmtTime(ts) {
  if (!ts) return ''
  const d = new Date(ts)
  const mo = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  const hh = String(d.getHours()).padStart(2, '0')
  const mm = String(d.getMinutes()).padStart(2, '0')
  return `${mo}-${day} ${hh}:${mm}`
}
</script>

<style scoped>
.cave {
  position: relative;
  z-index: 3;
  max-width: 1080px;
  margin: 0 auto;
  padding: 26px 24px 40px;
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.cave-head {
  display: flex;
  align-items: center;
  gap: 18px;
}

.cave-back {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 8px 14px;
  border-radius: 10px;
  border: 1px solid rgba(111, 233, 255, 0.28);
  background: rgba(111, 233, 255, 0.07);
  color: rgba(111, 233, 255, 0.92);
  font-family: inherit;
  font-size: 14px;
  letter-spacing: 2px;
  cursor: pointer;
  transition: 0.22s;
}

.cave-back svg {
  width: 15px;
  height: 15px;
}

.cave-back:hover {
  color: #1a1023;
  background: linear-gradient(100deg, var(--cyan), #b8f4ff 70%, var(--gold));
  border-color: transparent;
}

.cave-title h2 {
  font-family: var(--font-display);
  font-size: 34px;
  letter-spacing: 6px;
  color: var(--gold);
  text-shadow: 0 0 20px rgba(232, 198, 107, 0.45);
}

.cave-title p {
  margin-top: 4px;
  font-size: 13px;
  letter-spacing: 3px;
  color: rgba(233, 238, 251, 0.6);
}

/* 存档状态：已存云端 / 离线暂存 */
.sync-tag {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  margin-left: 10px;
  padding: 2px 9px 2px 7px;
  border-radius: 20px;
  font-size: 11px;
  letter-spacing: 1px;
  vertical-align: 1px;
  color: rgba(233, 238, 251, 0.5);
  background: rgba(160, 190, 230, 0.08);
  border: 1px solid rgba(160, 190, 230, 0.22);
}

.sync-tag i {
  width: 5px;
  height: 5px;
  border-radius: 50%;
  background: rgba(233, 238, 251, 0.4);
}

.sync-tag.ok {
  color: rgba(111, 233, 255, 0.9);
  background: rgba(111, 233, 255, 0.08);
  border-color: rgba(111, 233, 255, 0.3);
}

.sync-tag.ok i {
  background: var(--cyan);
  box-shadow: 0 0 8px rgba(111, 233, 255, 0.9);
}

.cave-panel {
  position: relative;
  padding: 20px 20px 18px;
  border-radius: 16px;
  background: linear-gradient(160deg, rgba(20, 22, 46, 0.62), rgba(8, 9, 22, 0.7));
  border: 1px solid rgba(111, 233, 255, 0.18);
  box-shadow: inset 0 0 26px rgba(58, 42, 107, 0.24);
  backdrop-filter: blur(10px);
}

.panel-head {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 14px;
}

.panel-head h3 {
  margin-right: auto;
  font-family: var(--font-display);
  font-size: 21px;
  letter-spacing: 4px;
  color: var(--gold);
}

.panel-count {
  font-size: 12px;
  letter-spacing: 2px;
  color: rgba(233, 238, 251, 0.5);
}

/* 面板工具：合并 / 展开收起 */
.panel-tools {
  display: flex;
  align-items: center;
  gap: 6px;
  flex-shrink: 0;
}

.panel-tool {
  padding: 3px 12px;
  border: 1px solid rgba(111, 233, 255, 0.22);
  border-radius: 999px;
  background: rgba(8, 9, 22, 0.5);
  color: rgba(207, 216, 242, 0.8);
  font-size: 12px;
  letter-spacing: 1px;
  cursor: pointer;
  transition: 0.2s;
}

.panel-tool:hover {
  border-color: rgba(232, 198, 107, 0.55);
  color: var(--gold);
}

.panel-tool.on {
  border-color: rgba(232, 198, 107, 0.7);
  background: rgba(232, 198, 107, 0.12);
  color: var(--gold);
}

/* 修为面板 */
.culti {
  display: flex;
  align-items: center;
  gap: 22px;
  flex-wrap: wrap;
}

.culti-label {
  font-size: 12px;
  letter-spacing: 3px;
  color: rgba(233, 238, 251, 0.5);
}

.culti-realm {
  font-family: var(--font-display);
  font-size: 30px;
  letter-spacing: 4px;
  color: #fff;
  text-shadow: 0 0 18px rgba(111, 233, 255, 0.5);
}

.culti-level {
  font-size: 13px;
  letter-spacing: 2px;
  color: rgba(111, 233, 255, 0.85);
}

.culti-mid {
  flex: 1;
  min-width: 220px;
}

.culti-bar {
  height: 8px;
  border-radius: 6px;
  background: rgba(8, 9, 22, 0.8);
  border: 1px solid rgba(111, 233, 255, 0.2);
  overflow: hidden;
}

.culti-bar i {
  display: block;
  height: 100%;
  background: linear-gradient(90deg, var(--cyan), var(--gold));
  transition: width 0.5s ease;
}

.culti-tip {
  margin-top: 8px;
  font-size: 12px;
  letter-spacing: 1px;
  color: rgba(233, 238, 251, 0.6);
}

.culti-stones {
  font-family: var(--font-display);
  font-size: 26px;
  letter-spacing: 2px;
  color: var(--gold);
}

.culti-stones-label {
  font-size: 12px;
  letter-spacing: 3px;
  color: rgba(233, 238, 251, 0.5);
  text-align: right;
}

.cave-sign {
  padding: 9px 18px;
  border-radius: 10px;
  border: 1px solid rgba(232, 198, 107, 0.45);
  background: rgba(232, 198, 107, 0.12);
  color: var(--gold);
  font-family: inherit;
  font-size: 14px;
  letter-spacing: 2px;
  cursor: pointer;
  transition: 0.22s;
}

.cave-sign:hover:not(.done) {
  color: #1a1023;
  background: linear-gradient(100deg, var(--gold), #ffe9a8 60%, var(--cyan));
  border-color: transparent;
}

.cave-sign.done {
  cursor: default;
  opacity: 0.55;
}

/* 藏书阁 / 足迹列表 */
.cave-list {
  list-style: none;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.cave-list li {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 10px 12px;
  border-radius: 10px;
  background: rgba(8, 9, 22, 0.45);
  border: 1px solid rgba(111, 233, 255, 0.1);
  cursor: pointer;
  transition: 0.2s;
}

.cave-list li:hover {
  border-color: rgba(232, 198, 107, 0.4);
  background: rgba(58, 42, 107, 0.28);
}

.list-path {
  flex-shrink: 0;
  font-size: 12px;
  letter-spacing: 1px;
  color: rgba(111, 233, 255, 0.75);
}

.list-title {
  flex: 1;
  font-size: 14px;
  color: #e9eefb;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.list-time {
  flex-shrink: 0;
  font-size: 12px;
  color: rgba(233, 238, 251, 0.4);
}

.cave-empty {
  padding: 18px 4px;
  font-size: 13px;
  letter-spacing: 1px;
  color: rgba(233, 238, 251, 0.45);
}

/* 合并视图：藏 / 迹 标记 */
.list-tags {
  display: flex;
  gap: 4px;
  flex-shrink: 0;
}

.list-tag {
  padding: 3px 7px;
  border-radius: 5px;
  font-style: normal;
  font-size: 11px;
  line-height: 1;
}

.tag-fav {
  color: var(--gold);
  background: rgba(232, 198, 107, 0.14);
  border: 1px solid rgba(232, 198, 107, 0.4);
}

.tag-trail {
  color: var(--cyan);
  background: rgba(111, 233, 255, 0.12);
  border: 1px solid rgba(111, 233, 255, 0.35);
}

/* 列表展开更多 */
.list-more {
  margin-top: 10px;
  width: 100%;
  padding: 8px;
  border: 1px dashed rgba(111, 233, 255, 0.22);
  border-radius: 10px;
  background: rgba(8, 9, 22, 0.35);
  color: rgba(207, 216, 242, 0.75);
  font-size: 12px;
  letter-spacing: 2px;
  cursor: pointer;
  transition: 0.2s;
}

.list-more:hover {
  border-color: rgba(232, 198, 107, 0.5);
  color: var(--gold);
}

/* 仙逆动漫入口 */
.anime-card {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 14px 16px;
  border-radius: 12px;
  background: linear-gradient(100deg, rgba(58, 42, 107, 0.34), rgba(8, 9, 22, 0.5));
  border: 1px solid rgba(232, 198, 107, 0.22);
  cursor: pointer;
  transition: 0.24s;
}

.anime-card:hover {
  border-color: rgba(232, 198, 107, 0.55);
  box-shadow: 0 0 22px rgba(232, 198, 107, 0.16);
  transform: translateY(-1px);
}

.anime-card:focus-visible {
  outline: 1px solid var(--cyan);
  outline-offset: 2px;
}

.anime-seal {
  flex-shrink: 0;
  display: grid;
  place-items: center;
  width: 54px;
  height: 54px;
  border-radius: 12px;
  font-family: var(--font-display);
  font-size: 19px;
  letter-spacing: 1px;
  color: #1a1023;
  background: linear-gradient(140deg, var(--gold), #ffe9a8 55%, var(--cyan));
  box-shadow: 0 0 18px rgba(232, 198, 107, 0.32);
}

.anime-info {
  flex: 1;
  min-width: 0;
}

.anime-name {
  font-size: 15px;
  letter-spacing: 2px;
  color: #e9eefb;
}

.anime-desc {
  margin-top: 5px;
  font-size: 12.5px;
  letter-spacing: 1px;
  line-height: 1.6;
  color: rgba(233, 238, 251, 0.55);
}

.anime-play {
  flex-shrink: 0;
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 8px 14px;
  border-radius: 10px;
  font-size: 13px;
  letter-spacing: 2px;
  color: var(--gold);
  background: rgba(232, 198, 107, 0.1);
  border: 1px solid rgba(232, 198, 107, 0.36);
  transition: 0.22s;
}

.anime-play svg {
  width: 12px;
  height: 12px;
}

.anime-card:hover .anime-play {
  color: #1a1023;
  background: linear-gradient(100deg, var(--gold), #ffe9a8 60%, var(--cyan));
  border-color: transparent;
}

@media (max-width: 620px) {
  .anime-card {
    flex-wrap: wrap;
  }

  .anime-info {
    flex-basis: calc(100% - 70px);
  }

  .anime-play {
    margin-left: auto;
  }
}

/* 底部重置 */
.cave-foot {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 8px;
}

.cave-save-tip {
  font-size: 11.5px;
  letter-spacing: 1px;
  color: rgba(233, 238, 251, 0.3);
}

.cave-reset {
  padding: 8px 16px;
  border-radius: 10px;
  border: 1px solid rgba(226, 75, 74, 0.4);
  background: rgba(226, 75, 74, 0.08);
  color: rgba(247, 193, 193, 0.9);
  font-family: inherit;
  font-size: 13px;
  letter-spacing: 2px;
  cursor: pointer;
  transition: 0.22s;
}

.cave-reset:hover {
  background: rgba(226, 75, 74, 0.22);
  color: #fff;
}

/* 二次确认态：更醒目，避免误删云端存档 */
.cave-reset.armed {
  background: rgba(226, 75, 74, 0.28);
  border-color: rgba(226, 75, 74, 0.75);
  color: #fff;
  box-shadow: 0 0 18px rgba(226, 75, 74, 0.35);
  animation: resetPulse 1.1s ease-in-out infinite;
}

@keyframes resetPulse {
  0%,
  100% {
    box-shadow: 0 0 12px rgba(226, 75, 74, 0.25);
  }
  50% {
    box-shadow: 0 0 22px rgba(226, 75, 74, 0.55);
  }
}
</style>

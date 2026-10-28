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

    <!-- ================= 藏书阁 ================= -->
    <div class="cave-panel">
      <div class="panel-head">
        <h3>藏书阁</h3>
        <span class="panel-count">{{ cave.favs.length }} 卷</span>
      </div>
      <ul v-if="cave.favs.length" class="cave-list">
        <li v-for="f in cave.favs" :key="f.id" @click="emit('open', f.id)">
          <span class="list-path">{{ f.categoryPath || '未分类' }}</span>
          <span class="list-title">{{ f.title }}</span>
          <span class="list-time">{{ fmtTime(f.createdAt) }}</span>
        </li>
      </ul>
      <p v-else class="cave-empty">阁中空空，去首页研读功法后点「收入藏书阁」即可收录。</p>
    </div>

    <!-- ================= 修道足迹 ================= -->
    <div class="cave-panel">
      <div class="panel-head">
        <h3>修道足迹</h3>
        <span class="panel-count">{{ cave.history.length }} 卷</span>
      </div>
      <ul v-if="cave.history.length" class="cave-list">
        <li v-for="h in cave.history" :key="h.id" @click="emit('open', h.id)">
          <span class="list-path">{{ h.categoryPath || '未分类' }}</span>
          <span class="list-title">{{ h.title }}</span>
          <span class="list-time">{{ fmtTime(h.readAt) }}</span>
        </li>
      </ul>
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
  username: { type: String, default: '道友' }
})

const emit = defineEmits(['back', 'open', 'toast'])

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
  font-family: 'Ma Shan Zheng', serif;
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
  align-items: baseline;
  justify-content: space-between;
  margin-bottom: 14px;
}

.panel-head h3 {
  font-family: 'Ma Shan Zheng', serif;
  font-size: 21px;
  letter-spacing: 4px;
  color: var(--gold);
}

.panel-count {
  font-size: 12px;
  letter-spacing: 2px;
  color: rgba(233, 238, 251, 0.5);
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
  font-family: 'Ma Shan Zheng', serif;
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
  font-family: 'Ma Shan Zheng', serif;
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

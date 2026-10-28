<template>
  <div class="realm-card">
    <!-- 折叠头：随时可见当前境界 -->
    <button class="rc-head" :class="{ open }" @click="open = !open">
      <span class="rc-ico">境</span>
      <span class="rc-title">
        <b>境界总览</b>
        <i>仙逆 · 四大步 {{ count }} 境</i>
      </span>
      <span class="rc-now">
        <em>{{ now.name }}{{ now.suffix }}</em>
        <u>第 {{ now.level }} {{ now.unit }}</u>
      </span>
      <span class="rc-arrow" :class="{ up: open }">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.9">
          <path d="M6 9.5 12 15.5 18 9.5" />
        </svg>
      </span>
    </button>

    <!-- 境界表 -->
    <div v-if="open" class="rc-body">
      <div class="rc-head-row">
        <span>境界</span>
        <span>修为区间</span>
        <span>每重/层所需</span>
        <span>当前状态</span>
      </div>

      <section v-for="g in groups" :key="g.key" class="rc-group">
        <p class="rc-group-head">
          <span class="rc-badge" :class="{ bridge: g.badge === '过渡' }">{{ g.badge }}</span>
          <b>{{ g.label }}</b>
          <span class="rc-group-range">{{ g.from }} – {{ g.to }}</span>
        </p>

        <ul class="rc-list">
          <li v-for="r in g.rows" :key="r.name" class="rc-row" :class="r.status">
            <span class="rc-name">
              {{ r.name }}
              <em>{{ r.suffix }}</em>
            </span>
            <span class="rc-range">{{ r.from }} – {{ r.to }}</span>
            <span class="rc-step">
              {{ r.perLevel }}
              <i>{{ r.unit }}</i>
            </span>
            <span class="rc-state">
              <template v-if="r.status === 'current'">
                <b>第 {{ r.level }} {{ r.unit }}</b>
                <span class="rc-mini">
                  <span class="rc-mini-fill" :style="{ width: Math.round(r.progress * 100) + '%' }"></span>
                </span>
              </template>
              <template v-else-if="r.status === 'done'">已渡</template>
              <template v-else>未至</template>
            </span>
          </li>
        </ul>

        <p class="rc-group-note">{{ g.note }}</p>
      </section>

      <p class="rc-note">
        修为累至区间上限即破境：凝气十五层、踏天九桥，余者各十重。
        <template v-if="next">
          当前距 {{ next.name }}{{ next.suffix }}圆满尚需 <b>{{ next.need }}</b> 修为。
        </template>
        <template v-else>已踏完第九桥 · 四大步共需 {{ TOTAL }} 修为，逆天之路至此超脱。</template>
      </p>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import {
  realm,
  realmTable,
  nextRealm,
  REALM_TOTAL,
  REALM_COUNT,
  STEPS
} from '../composables/useCave.js'

const props = defineProps({
  defaultOpen: { type: Boolean, default: false }
})

const open = ref(props.defaultOpen)
const rows = computed(() => realmTable.value)
const now = computed(() => realm.value)
const next = computed(() => nextRealm.value)
const TOTAL = REALM_TOTAL
const count = REALM_COUNT

/** 按四大步分组（阴虚阳实单独作过渡组） */
const groups = computed(() =>
  STEPS.map((s) => {
    const list = rows.value.filter((r) => r.step === s.key)
    if (!list.length) return null
    return {
      ...s,
      rows: list,
      from: list[0].from,
      to: list[list.length - 1].to
    }
  }).filter(Boolean)
)
</script>

<style scoped>
.realm-card {
  border-radius: 16px;
  background: linear-gradient(160deg, rgba(20, 22, 46, 0.7), rgba(8, 9, 22, 0.78));
  border: 1px solid rgba(111, 233, 255, 0.18);
  box-shadow: inset 0 0 28px rgba(58, 42, 107, 0.24);
  backdrop-filter: blur(10px);
  overflow: hidden;
}

/* ================= 折叠头 ================= */
.rc-head {
  display: flex;
  align-items: center;
  gap: 13px;
  width: 100%;
  padding: 15px 20px;
  border: none;
  background: transparent;
  font-family: inherit;
  color: inherit;
  text-align: left;
  cursor: pointer;
  transition: 0.24s;
}

.rc-head:hover {
  background: rgba(111, 233, 255, 0.05);
}

.rc-head.open {
  border-bottom: 1px solid rgba(160, 190, 230, 0.12);
}

.rc-ico {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  width: 32px;
  height: 32px;
  border-radius: 9px;
  font-family: 'Ma Shan Zheng', serif;
  font-size: 18px;
  color: var(--gold);
  background: rgba(232, 198, 107, 0.1);
  border: 1px solid rgba(232, 198, 107, 0.38);
  text-shadow: 0 0 12px rgba(232, 198, 107, 0.5);
}

.rc-title {
  display: flex;
  flex-direction: column;
  gap: 2px;
  flex: 1;
  min-width: 0;
}

.rc-title b {
  font-size: 14px;
  font-weight: 400;
  letter-spacing: 3px;
  color: #fff;
}

.rc-title i {
  font-size: 11px;
  font-style: normal;
  letter-spacing: 2px;
  color: rgba(233, 238, 251, 0.38);
}

.rc-now {
  display: flex;
  align-items: baseline;
  gap: 8px;
  flex-shrink: 0;
}

.rc-now em {
  font-family: 'Ma Shan Zheng', serif;
  font-style: normal;
  font-size: 19px;
  letter-spacing: 2px;
  color: var(--gold);
  text-shadow: 0 0 14px rgba(232, 198, 107, 0.45);
}

.rc-now u {
  font-size: 12px;
  text-decoration: none;
  letter-spacing: 1px;
  color: rgba(111, 233, 255, 0.85);
}

.rc-arrow {
  flex-shrink: 0;
  display: inline-flex;
  color: rgba(233, 238, 251, 0.45);
  transition: 0.28s;
}

.rc-arrow svg {
  width: 17px;
  height: 17px;
}

.rc-arrow.up {
  transform: rotate(180deg);
  color: var(--cyan);
}

/* ================= 表格 ================= */
.rc-body {
  padding: 6px 14px 16px;
  animation: rcIn 0.26s ease both;
}

@keyframes rcIn {
  from {
    opacity: 0;
    transform: translateY(-6px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.rc-head-row,
.rc-row {
  display: grid;
  grid-template-columns: minmax(88px, 0.92fr) minmax(112px, 1.1fr) minmax(84px, 0.82fr) minmax(104px, 1fr);
  align-items: center;
  gap: 8px;
}

.rc-head-row {
  padding: 8px 12px 9px;
  font-size: 11px;
  letter-spacing: 3px;
  color: rgba(233, 238, 251, 0.34);
}

/* ================= 分组 ================= */
.rc-group {
  margin-top: 10px;
}

.rc-group-head {
  display: flex;
  align-items: center;
  gap: 9px;
  padding: 7px 12px 6px;
}

.rc-badge {
  flex-shrink: 0;
  padding: 2px 8px;
  border-radius: 5px;
  font-size: 10.5px;
  letter-spacing: 1px;
  color: var(--gold);
  background: rgba(232, 198, 107, 0.1);
  border: 1px solid rgba(232, 198, 107, 0.32);
}

.rc-badge.bridge {
  color: rgba(233, 238, 251, 0.55);
  background: rgba(160, 190, 230, 0.07);
  border-color: rgba(160, 190, 230, 0.28);
}

.rc-group-head b {
  font-family: 'Ma Shan Zheng', serif;
  font-size: 15px;
  font-weight: 400;
  letter-spacing: 2px;
  color: rgba(233, 238, 251, 0.82);
}

.rc-group-range {
  margin-left: auto;
  font-size: 11px;
  letter-spacing: 0.5px;
  font-variant-numeric: tabular-nums;
  color: rgba(233, 238, 251, 0.28);
}

.rc-group-note {
  padding: 2px 12px 4px 12px;
  font-size: 11px;
  line-height: 1.7;
  letter-spacing: 0.5px;
  color: rgba(233, 238, 251, 0.28);
}

/* ================= 行 ================= */
.rc-list {
  list-style: none;
  display: flex;
  flex-direction: column;
  gap: 3px;
}

.rc-row {
  position: relative;
  padding: 9px 12px;
  border-radius: 10px;
  font-size: 13px;
  letter-spacing: 1px;
  color: rgba(233, 238, 251, 0.62);
  transition: 0.22s;
}

.rc-row:hover {
  background: rgba(255, 255, 255, 0.035);
}

/* 已渡 */
.rc-row.done {
  color: rgba(233, 238, 251, 0.4);
}

.rc-row.done .rc-name {
  color: rgba(233, 238, 251, 0.58);
}

/* 未至 */
.rc-row.future {
  color: rgba(233, 238, 251, 0.3);
}

.rc-row.future .rc-name {
  color: rgba(233, 238, 251, 0.42);
}

/* 当前所在 */
.rc-row.current {
  padding: 11px 12px;
  color: rgba(233, 238, 251, 0.92);
  background: linear-gradient(100deg, rgba(232, 198, 107, 0.16), rgba(111, 233, 255, 0.06) 70%, transparent);
  box-shadow:
    inset 0 0 0 1px rgba(232, 198, 107, 0.42),
    0 0 22px rgba(232, 198, 107, 0.14);
}

.rc-row.current::before {
  content: '';
  position: absolute;
  left: 0;
  top: 18%;
  bottom: 18%;
  width: 3px;
  border-radius: 0 3px 3px 0;
  background: linear-gradient(180deg, var(--gold), var(--cyan));
  box-shadow: 0 0 12px rgba(232, 198, 107, 0.7);
}

.rc-name {
  display: flex;
  align-items: baseline;
  gap: 3px;
  font-family: 'Ma Shan Zheng', serif;
  font-size: 18px;
  letter-spacing: 2px;
  color: rgba(233, 238, 251, 0.75);
}

.rc-row.current .rc-name {
  font-size: 20px;
  color: var(--gold);
  text-shadow: 0 0 16px rgba(232, 198, 107, 0.55);
}

.rc-name em {
  font-size: 11px;
  font-style: normal;
  letter-spacing: 0;
  opacity: 0.55;
}

.rc-range {
  font-size: 12.5px;
  letter-spacing: 0.5px;
  font-variant-numeric: tabular-nums;
}

.rc-step {
  font-size: 12.5px;
  font-variant-numeric: tabular-nums;
  color: rgba(233, 238, 251, 0.45);
}

.rc-step i {
  font-style: normal;
  font-size: 10.5px;
  opacity: 0.6;
}

.rc-row.current .rc-step {
  color: rgba(232, 198, 107, 0.85);
}

.rc-state {
  display: flex;
  flex-direction: column;
  gap: 5px;
  font-size: 12px;
  letter-spacing: 1px;
}

.rc-row.done .rc-state {
  color: rgba(111, 233, 255, 0.6);
}

.rc-state b {
  font-weight: 400;
  font-size: 12.5px;
  color: var(--cyan);
}

.rc-mini {
  display: block;
  height: 4px;
  border-radius: 3px;
  background: rgba(255, 255, 255, 0.09);
  overflow: hidden;
}

.rc-mini-fill {
  display: block;
  height: 100%;
  border-radius: 3px;
  background: linear-gradient(90deg, var(--gold), var(--cyan));
  box-shadow: 0 0 10px rgba(232, 198, 107, 0.6);
  transition: width 0.45s ease;
}

.rc-note {
  margin-top: 14px;
  padding: 11px 14px;
  border-radius: 10px;
  font-size: 11.5px;
  line-height: 1.9;
  letter-spacing: 1px;
  color: rgba(233, 238, 251, 0.4);
  background: rgba(255, 255, 255, 0.028);
}

.rc-note b {
  font-weight: 400;
  color: var(--gold);
}

/* ================= 响应式 ================= */
@media (max-width: 620px) {
  .rc-head {
    flex-wrap: wrap;
    gap: 10px;
    padding: 13px 16px;
  }
  .rc-title i {
    display: none;
  }
  .rc-now {
    order: 3;
    width: 100%;
    padding-left: 45px;
  }
  .rc-head-row,
  .rc-row {
    grid-template-columns: minmax(74px, 0.9fr) minmax(94px, 1.05fr) minmax(84px, 0.95fr);
  }
  .rc-head-row span:nth-child(3),
  .rc-row .rc-step {
    display: none;
  }
  .rc-name {
    font-size: 16px;
  }
  .rc-row.current .rc-name {
    font-size: 18px;
  }
  .rc-group-range {
    display: none;
  }
}
</style>

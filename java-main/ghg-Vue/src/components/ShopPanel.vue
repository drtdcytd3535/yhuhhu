<template>
  <section class="shop">
    <!-- ================= 头部：返回 + 标题 + 灵石 ================= -->
    <div class="shop-head">
      <button class="shop-back" @click="emit('back')">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8">
          <path d="M15 5 8 12l7 7" />
        </svg>
        返回仙门
      </button>
      <div class="shop-title">
        <h2>天宝坊市</h2>
        <p>{{ cave.username }} · 仙家百货，灵石为聘 · 修炼加成在此换取</p>
      </div>
      <div class="shop-stones">
        <p class="stones-num">◈ {{ cave.stones }}</p>
        <p class="stones-label">灵石</p>
      </div>
    </div>

    <!-- ================= 当前佩戴 ================= -->
    <div class="shop-wearing">
      <span class="wearing-label">当前佩戴</span>
      <template v-if="worn">
        <span class="wearing-item">{{ worn.ico }} {{ worn.name }}</span>
        <span class="wearing-bonus">修炼加成 +{{ Math.round(worn.bonus * 100) }}%</span>
        <span class="wearing-stats">
          <span v-for="st in statChips(worn)" :key="st.key" class="stat-chip">
            {{ st.ico }} {{ st.name }} +{{ st.val }}{{ st.suffix || '' }}
          </span>
        </span>
        <span class="wearing-power">战力 {{ wornPower }}</span>
        <button class="wearing-unequip" @click="onUnequip">卸下</button>
      </template>
      <template v-else>
        <span class="wearing-none">未佩戴法宝 · 购入后可佩 · 战力 0</span>
      </template>
    </div>

    <!-- ================= 商品列表 ================= -->
    <div class="shop-grid">
      <article
        v-for="item in EQUIPS"
        :key="item.id"
        class="goods-card"
        :class="{ owned: hasEquip(item.id), worn: isEquipped(item.id) }"
      >
        <span class="goods-ico">{{ item.ico }}</span>
        <span class="goods-kind">{{ item.kind }} · {{ item.from }}</span>
        <h3 class="goods-name">{{ item.name }}</h3>
        <p class="goods-desc">{{ item.desc }}</p>

        <div class="goods-bonus">
          <i></i>修炼加成 +{{ Math.round(item.bonus * 100) }}%
          <em class="goods-power">战力 {{ battlePower(item.stats) }}</em>
        </div>

        <div class="goods-stats">
          <span v-for="st in statChips(item)" :key="st.key" class="stat-chip">
            {{ st.ico }} {{ st.name }} +{{ st.val }}{{ st.suffix || '' }}
          </span>
        </div>

        <div class="goods-foot">
          <span class="goods-price">◈ {{ item.price }}</span>

          <!-- 未拥有：购买 -->
          <button
            v-if="!hasEquip(item.id)"
            class="goods-buy"
            :disabled="cave.stones < item.price"
            @click="onBuy(item)"
          >
            {{ cave.stones < item.price ? '灵石不足' : '购入' }}
          </button>

          <!-- 已拥有：佩戴 / 已佩戴 -->
          <button
            v-else-if="!isEquipped(item.id)"
            class="goods-buy equip"
            @click="onEquip(item)"
          >
            佩戴
          </button>
          <span v-else class="goods-worn">佩戴中</span>
        </div>
      </article>
    </div>

    <p class="shop-tip">灵石可于洞府签到、修炼积攒；购得的法宝同步云端，换设备也不丢。</p>
  </section>
</template>

<script setup>
import { computed } from 'vue'
import {
  cave,
  EQUIPS,
  STAT_DEFS,
  hasEquip,
  isEquipped,
  buyEquip,
  equipItem,
  unequipItem,
  equippedItem,
  equipStats,
  battlePower,
  wornPower
} from '../composables/useCave.js'

const props = defineProps({
  username: { type: String, default: '道友' }
})
const emit = defineEmits(['back', 'toast'])

const worn = computed(() => equippedItem.value)

/** 把一件装备的属性转成展示用碎片（只留非零项） */
function statChips(item) {
  const st = equipStats(item)
  return STAT_DEFS.map((d) => ({ ...d, val: st[d.key] })).filter((c) => c.val > 0)
}

function onBuy(item) {
  const r = buyEquip(item)
  emit('toast', r.msg)
}
function onEquip(item) {
  const r = equipItem(item.id)
  emit('toast', r.msg)
}
function onUnequip() {
  const r = unequipItem()
  emit('toast', r.msg)
}
</script>

<style scoped>
.shop {
  width: min(1080px, 94vw);
  margin: 0 auto;
  padding: 28px 0 60px;
}

/* ---------- 头部 ---------- */
.shop-head {
  display: flex;
  align-items: center;
  gap: 18px;
  margin-bottom: 18px;
}

.shop-back {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 8px 16px;
  border: 1px solid var(--mist);
  border-radius: 999px;
  background: rgba(10, 12, 30, 0.55);
  color: #cfd8f2;
  font-size: 14px;
  cursor: pointer;
  transition: border-color 0.2s, color 0.2s;
}
.shop-back svg { width: 16px; height: 16px; }
.shop-back:hover { border-color: var(--gold); color: var(--gold); }

.shop-title { flex: 1; }
.shop-title h2 {
  font-family: var(--font-display);
  font-size: 30px;
  letter-spacing: 4px;
  color: var(--gold);
}
.shop-title p { margin-top: 4px; font-size: 13px; color: #93a0c4; }

.shop-stones { text-align: right; }
.stones-num {
  font-size: 22px;
  color: var(--cyan);
  text-shadow: 0 0 12px rgba(111, 233, 255, 0.45);
}
.stones-label { font-size: 12px; color: #93a0c4; }

/* ---------- 当前佩戴条 ---------- */
.shop-wearing {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 12px 18px;
  margin-bottom: 20px;
  border: 1px solid var(--mist);
  border-radius: 12px;
  background: rgba(14, 16, 38, 0.6);
  font-size: 14px;
}
.wearing-label { color: #93a0c4; letter-spacing: 2px; }
.wearing-item { color: var(--gold); font-family: var(--font-display); font-size: 17px; letter-spacing: 1px; }
.wearing-bonus { color: var(--cyan); font-size: 13px; }
.wearing-none { color: #6f7a9c; }
.wearing-stats { display: inline-flex; align-items: center; gap: 8px; flex-wrap: wrap; }
.wearing-power { color: var(--gold); font-size: 13px; }
.wearing-unequip {
  margin-left: auto;
  padding: 5px 16px;
  border: 1px solid var(--mist);
  border-radius: 999px;
  background: transparent;
  color: #cfd8f2;
  font-size: 13px;
  cursor: pointer;
  transition: border-color 0.2s, color 0.2s;
}
.wearing-unequip:hover { border-color: #e08c8c; color: #e08c8c; }

/* ---------- 商品网格 ---------- */
.shop-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(310px, 1fr));
  gap: 18px;
}

.goods-card {
  position: relative;
  padding: 20px 20px 16px;
  border: 1px solid var(--mist);
  border-radius: 14px;
  background: linear-gradient(160deg, rgba(24, 20, 56, 0.72), rgba(9, 10, 26, 0.85));
  overflow: hidden;
  transition: border-color 0.25s, transform 0.25s;
}
.goods-card:hover { border-color: rgba(232, 198, 107, 0.55); transform: translateY(-3px); }
.goods-card.owned { border-color: rgba(111, 233, 255, 0.35); }
.goods-card.worn { border-color: rgba(232, 198, 107, 0.75); box-shadow: 0 0 18px rgba(232, 198, 107, 0.12); }

.goods-ico {
  position: absolute;
  top: 14px;
  right: 16px;
  width: 42px;
  height: 42px;
  display: grid;
  place-items: center;
  border: 1px solid rgba(232, 198, 107, 0.5);
  border-radius: 10px;
  color: var(--gold);
  font-size: 22px;
  background: rgba(232, 198, 107, 0.08);
}

.goods-kind { font-size: 12px; letter-spacing: 1px; color: #7d89ad; }
.goods-name {
  margin-top: 4px;
  font-family: var(--font-display);
  font-size: 21px;
  letter-spacing: 2px;
  color: #f2e6c8;
}
.goods-desc { margin-top: 8px; min-height: 60px; font-size: 13px; line-height: 1.7; color: #a7b2d4; }

.goods-bonus {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-top: 4px;
  font-size: 12px;
  color: var(--cyan);
}
.goods-bonus i {
  width: 18px;
  height: 1px;
  background: linear-gradient(90deg, transparent, var(--cyan));
}
.goods-power {
  margin-left: auto;
  font-style: normal;
  color: var(--gold);
  opacity: 0.9;
}

/* ---------- 属性加成碎片 ---------- */
.goods-stats {
  display: flex;
  align-items: center;
  gap: 6px;
  flex-wrap: wrap;
  margin-top: 8px;
  min-height: 22px;
}
.stat-chip {
  padding: 2px 9px;
  border: 1px solid rgba(111, 233, 255, 0.28);
  border-radius: 999px;
  background: rgba(111, 233, 255, 0.07);
  font-size: 12px;
  color: #bfeaff;
  white-space: nowrap;
}

.goods-foot {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: 12px;
}
.goods-price { color: var(--cyan); font-size: 15px; }

.goods-buy {
  padding: 7px 22px;
  border: 1px solid rgba(232, 198, 107, 0.6);
  border-radius: 999px;
  background: linear-gradient(135deg, rgba(232, 198, 107, 0.22), rgba(232, 198, 107, 0.08));
  color: var(--gold);
  font-size: 14px;
  letter-spacing: 2px;
  cursor: pointer;
  transition: background 0.2s, transform 0.15s;
}
.goods-buy:hover:not(:disabled) { background: rgba(232, 198, 107, 0.3); transform: scale(1.04); }
.goods-buy:disabled { opacity: 0.4; cursor: not-allowed; }

.goods-buy.equip {
  border-color: rgba(111, 233, 255, 0.6);
  background: linear-gradient(135deg, rgba(111, 233, 255, 0.18), rgba(111, 233, 255, 0.06));
  color: var(--cyan);
}
.goods-buy.equip:hover { background: rgba(111, 233, 255, 0.28); }

.goods-worn {
  font-size: 13px;
  letter-spacing: 2px;
  color: var(--gold);
}

.shop-tip {
  margin-top: 24px;
  text-align: center;
  font-size: 12px;
  color: #6f7a9c;
}
</style>

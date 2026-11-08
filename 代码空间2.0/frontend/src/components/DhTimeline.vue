<script setup lang="ts">
const steps = [
  { actor: 'Alice', text: '发送公开参数 g, p, A=g^a mod p' },
  { actor: '攻击者', text: "拦截 A，替换为 A'=g^m mod p", danger: true },
  { actor: 'Bob', text: "收到 A'，回送 B=g^b mod p" },
  { actor: '攻击者', text: "拦截 B，替换为 B'=g^m mod p", danger: true },
  { actor: 'Alice', text: "协商密钥 K_A = A'^a = g^(ma)" },
  { actor: 'Bob', text: "协商密钥 K_B = B'^b = g^(mb)" },
  { actor: '攻击者', text: '与两端分别共享密钥，可解密所有悄悄话', danger: true }
]
</script>

<template>
  <div class="card">
    <h3>DH 密钥交换时序演示（SVG）</h3>
    <svg
      width="100%"
      height="280"
      viewBox="0 0 520 280"
      style="background: #131d19; border: 1px solid var(--border); border-radius: 8px"
    >
      <line x1="60" y1="20" x2="60" y2="260" stroke="#3ef2b0" stroke-width="1.5" />
      <line x1="260" y1="20" x2="260" y2="260" stroke="#ff6b79" stroke-width="1.5" />
      <line x1="460" y1="20" x2="460" y2="260" stroke="#3ef2b0" stroke-width="1.5" />
      <text x="40" y="15" font-size="12" fill="#3ef2b0" font-family="monospace">Alice</text>
      <text x="235" y="15" font-size="12" fill="#ff6b79" font-family="monospace">攻击者</text>
      <text x="440" y="15" font-size="12" fill="#3ef2b0" font-family="monospace">Bob</text>
      <g v-for="(s, i) in steps" :key="i">
        <text
          x="10"
          :y="50 + i * 30"
          font-size="10"
          font-family="monospace"
          :fill="s.danger ? '#ff6b79' : '#e8f2ec'"
        >
          {{ i + 1 }}. {{ s.actor }}: {{ s.text }}
        </text>
      </g>
    </svg>
    <p class="muted" style="font-size: 12px">
      简化演示：未认证的公开信道可被中间人完全接管；数字证书/签名是补齐身份认证的关键。
    </p>
  </div>
</template>
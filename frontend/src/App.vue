<script setup>
import { onMounted, ref } from 'vue'

const project = ref(null)
const applications = ref([])
const message = ref('')

async function load() {
  const p = await fetch('/api/project')
  project.value = await p.json()
  const a = await fetch('/api/applications')
  applications.value = await a.json()
}

async function submit(id) {
  const res = await fetch(`/api/applications/${id}/submit`, { method: 'POST' })
  const body = await res.json()
  message.value = res.ok ? '提交成功' : body.message
  await load()
}

onMounted(load)
</script>

<template>
  <h1>经费申请</h1>
  <div v-if="project">
    可用：{{ project.availableAmount }} ｜ 冻结：{{ project.frozenAmount }} ｜
    已使用：{{ project.usedAmount }} ｜ 总预算：{{ project.totalAmount }}
  </div>
  <p v-if="message">{{ message }}</p>
  <table>
    <thead>
      <tr><th>编号</th><th>用途</th><th>金额</th><th>状态</th><th>操作</th></tr>
    </thead>
    <tbody>
      <tr v-for="a in applications" :key="a.id">
        <td>{{ a.id }}</td>
        <td>{{ a.purpose }}</td>
        <td>{{ a.amount }}</td>
        <td>{{ a.status }}</td>
        <td><button v-if="a.status === 'DRAFT'" @click="submit(a.id)">提交</button></td>
      </tr>
    </tbody>
  </table>
</template>
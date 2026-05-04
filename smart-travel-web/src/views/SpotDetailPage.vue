<template>
  <div class="spot-detail-page">
    <el-page-header @back="router.back()" content="景点详情" />

    <el-card class="placeholder-card">
      <el-result
        icon="info"
        title="详情页开发中"
        :sub-title="`当前景点 ID: ${id}，详情接口对接将在后续步骤完成`"
      >
        <template #extra>
          <el-button type="primary" @click="router.back()">返回列表</el-button>
        </template>
      </el-result>
    </el-card>
  </div>
</template>

<script setup>
import { useRoute, useRouter } from 'vue-router'

const route = useRoute()
const router = useRouter()
const id = route.params.id
</script>

<style scoped>
.spot-detail-page {
  padding: 10px;
}
.placeholder-card {
  margin-top: 20px;
}
</style>

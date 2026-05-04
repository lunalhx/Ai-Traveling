<template>
  <el-container class="layout-container">
    <el-aside width="200px" class="aside">
      <AppSidebar />
    </el-aside>
    <el-container>
      <el-header class="header">
        <AppHeader />
      </el-header>
      <el-main class="main-content">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import AppSidebar from '../components/AppSidebar.vue'
import AppHeader from '../components/AppHeader.vue'
</script>

<style scoped>
.layout-container {
  height: 100vh;
}
.aside {
  background-color: #545c64;
}
.header {
  background-color: #409eff;
  padding: 0 20px;
}
.main-content {
  background-color: #f0f2f5;
  padding: 20px;
}
</style>

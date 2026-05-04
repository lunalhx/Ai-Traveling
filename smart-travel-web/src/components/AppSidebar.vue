<template>
  <el-menu
    :default-active="$route.path"
    class="app-sidebar-menu"
    router
    background-color="#545c64"
    text-color="#fff"
    active-text-color="#ffd04b"
  >
    <el-menu-item index="/dashboard">
      <span>首页大盘</span>
    </el-menu-item>
    <el-menu-item index="/spots">
      <span>景点浏览</span>
    </el-menu-item>
    <el-menu-item index="/nearby">
      <span>附近景点</span>
    </el-menu-item>
    <el-menu-item index="/checkin">
      <span>用户打卡</span>
    </el-menu-item>
    <el-menu-item index="/checkin-records">
      <span>打卡记录</span>
    </el-menu-item>
    <el-menu-item index="/seckill">
      <span>秒杀抢购</span>
    </el-menu-item>
    <el-menu-item index="/orders">
      <span>我的订单</span>
    </el-menu-item>
    <el-menu-item index="/admin-tools">
      <span>后台工具</span>
    </el-menu-item>
  </el-menu>
</template>

<style scoped>
.app-sidebar-menu {
  height: 100%;
  border-right: none;
}
</style>

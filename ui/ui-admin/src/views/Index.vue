<script setup>
  //只导入模板里真正用到的图标
  import {Shop, User, Crop, EditPen, SwitchButton, CaretBottom} from '@element-plus/icons-vue'
  import avatar from '@/assets/default.png'
  //条目被点击后,调用的函数
  import {useRouter} from 'vue-router'
  const router = useRouter();
  import adminApi from "@/api/admin/admin.js";
  import {useTokenStore} from '@/store/token.js'
  const tokenStore = useTokenStore();
  import {useAdminInfoStore} from '@/store/adminInfo.js'
  import {ref} from "vue";
  import {ElMessage, ElMessageBox} from "element-plus";
  const adminInfoStore = useAdminInfoStore();

  const dialogFormVisible = ref(false)
  const admin = ref({})

  const handleCommand = (command) => {
    //判断指令
    if (command === 'logout') {
      //退出登录前先二次确认，防止误点
      ElMessageBox.confirm(
          '您确认要退出登录么?',
          '提示',
          {
            confirmButtonText: '确认',
            cancelButtonText: '取消',
            type: 'warning',
            lockScroll: false //防止抖动
          }
      ).then(() => {
        //清掉token和缓存的用户信息，和"重置密码"成功后的处理保持一致
        tokenStore.removeToken();
        adminInfoStore.removeAdminInfo();
        //提示挂载在body上，跳转路由后依然会显示在登录页顶部
        ElMessage.success('退出登录成功')
        router.push('/login')
      }).catch(() => {
        //点了取消会走这里：ElMessageBox 取消时是 reject，不兜住控制台会有 unhandled rejection
      })
    } else if (command === 'updateAdminInfo') {
      dialogFormVisible.value = true
      //admin.value = adminInfoStore.admin
      Object.assign(admin.value, adminInfoStore.admin)
    } else if (command === 'resetPassword'){
      dialogResetPasswordDialog.value = true
      adminPasswordDTO.value = {}
      //resetForm.value.resetFields()
    } else {
      //路由
      router.push('/admin/' + command)
    }
  }

  //获取用户信息
  const getAdminInfo = () => {
    adminApi.adminInfo().then(result => {
      if (result.code == 1) {
        adminInfoStore.setAdminInfo(result.data.admin)
      }
    })
  }
  getAdminInfo()

  //上传头像
  const handleAvatarSuccess = (result) => {
    console.log(result)
    admin.value.avatar = result.data
  }

  const updateAdminInfo = () => {
    adminApi.update(admin.value.id, admin.value).then(result => {
      if (result.code == 1) {
        ElMessage.success(result.msg)
        dialogFormVisible.value = false
        getAdminInfo()
      }
    })
  }

  //重置密码
  const adminPasswordDTO = ref({
    'oldPassword': '',
    'newPassword': ''
  });
  const dialogResetPasswordDialog = ref(false)

  //自定义确认密码的校验函数
  const rePasswordValid = (rule, value, callback) => {
    if (value == null || value === '') {
      return callback(new Error('请再次确认密码'))
    }
    //响应式对象要：registerData.value才能拿到值
    if (adminPasswordDTO.value.newPassword !== value) {
      return callback(new Error('两次输入密码不一致'))
    }

    callback()
  }

  const rules = ref({
    oldPassword: [
      {required: true, message: '请输入密码', trigger: 'blur'},
      {min: 3, max: 16, message: '密码长度必须为3~16位', trigger: 'blur'}
    ],
    newPassword: [
      {required: true, message: '请输入密码', trigger: 'blur'},
      {min: 3, max: 16, message: '密码长度必须为3~16位', trigger: 'blur'}
    ],
    reNewPassword: [
      {required: true, message: '请输入密码', trigger: 'blur'},
      {validator: rePasswordValid, trigger: 'blur' }
    ]
  })
  const resetForm = ref()
  const resetPassword = async (formEl) => {
    if (!formEl) return
    await formEl.validate((valid, fields) => {
      if (valid) {
        adminApi.resetPassword(adminPasswordDTO.value).then(result => {
          if (result.code === 1) {
            ElMessage.success(result.msg)
            dialogResetPasswordDialog.value = false
            tokenStore.removeToken();
            adminInfoStore.removeAdminInfo();
            // 跳转到登录
            router.push('/login')
          } else {
            ElMessage.error(result.msg)
          }
        })
      } else {
        ElMessage.error('表单验证失败');
      }
    })
  }

  // 菜单  用户管理， 分类管理， 商品管理
  const menuData = ref([
    {name: '管理员管理', icon: 'Notebook', path: "/admin"},
    {
      name: '商品中心', icon: 'TrendCharts', children: [
        {name: '商品管理', icon: 'TrendCharts', path: "/product"},
        {name: '分类管理', icon: 'DataAnalysis', path: "/category"},
      ]
    },
    {
      name: '权限管理', icon: 'GobletFull', children: [
        {name: '管理员管理', icon: 'GobletSquareFull', path: "/admin"},
        {name: '角色管理', icon: 'User', path: "/role"},
        {name: '权限管理', icon: 'Lock', path: "/permission"},
      ]
    },
    {
      name: '个人中心', icon: 'User', children: [
        {name: '基本资料', icon: 'Document', path: "/admin/info"},
      ]
    }
  ]);


</script>

<template>
  <!-- element-plus中的容器 -->
  <el-container class="layout-container">
    <!-- 左侧菜单 -->
    <el-aside width="210px">
      <!-- 侧边栏品牌区 -->
      <div class="sidebar-brand">
        <el-icon class="brand-icon">
          <Shop/>
        </el-icon>
        <div class="brand-text">
          <p class="brand-name">电商管理系统</p>
          <p class="brand-en">MALL ADMIN</p>
        </div>
      </div>
      <!-- element-plus的菜单标签；配色统一交给下面 style 里的 CSS 变量接管 -->
      <el-menu router>
        <!-- 动态生成菜单 -->
        <template v-for="(menu, index) in menuData" :index="index.toString()">
          <el-sub-menu v-if="menu.children?.length>0" :index="menu.name">
            <template #title>
              <component
                  class="icons"
                  :is="menu.icon"
                  style="width: 1em; height: 1em; margin-right: 8px" >
              </component>
              <span>{{ menu.name }}</span>
            </template>
            <el-menu-item v-for="(child, ind) in menu.children" :index="child.path">
              <el-icon><component :is="child.icon"></component></el-icon>
              <span>{{ child.name }}</span>
            </el-menu-item>
          </el-sub-menu>
          <el-menu-item v-else :index="menu.path">
            <el-icon><component :is="menu.icon"></component></el-icon>
            <span>{{ menu.name }}</span>
          </el-menu-item>
        </template>
      </el-menu>

    </el-aside>
    <!-- 右侧主区域 -->
    <el-container>
      <!-- 头部区域 -->
      <el-header>
        <div class="header-title">电商后台管理系统</div>
        <!-- 下拉菜单 -->
        <!-- command: 条目被点击后会触发,在事件函数上可以声明一个参数,接收条目对应的指令 -->
        <el-dropdown placement="bottom-end" @command="handleCommand">
                    <span class="el-dropdown__box">
                        <el-avatar :size="32" :src="adminInfoStore.admin.avatar?adminInfoStore.admin.avatar:avatar"/>
                        <span class="dropdown-name">{{ adminInfoStore.admin.name }}</span>
                        <el-icon>
                            <CaretBottom/>
                        </el-icon>
                    </span>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="info" :icon="User">基本资料</el-dropdown-item>
              <el-dropdown-item command="updateAdminInfo" :icon="Crop">修改个人信息</el-dropdown-item>
              <el-dropdown-item command="resetPassword" :icon="EditPen">重置密码</el-dropdown-item>
              <el-dropdown-item command="logout" :icon="SwitchButton">退出登录</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </el-header>
      <!-- 中间区域 -->
      <el-main>
        <router-view></router-view>
      </el-main>
      <!-- 底部区域 -->
      <el-footer>后台管理 ©2024 Created by Gao</el-footer>
    </el-container>
  </el-container>

  <el-dialog v-model="dialogFormVisible" title="修改个人信息" width="500" :lock-scroll="false">
    <el-form :model="admin">
      <el-form-item label="名字" :label-width="60">
        <el-input v-model="admin.name" autocomplete="off" />
      </el-form-item>
      <el-form-item label="邮箱" :label-width="60">
        <el-input v-model="admin.email" autocomplete="off" />
      </el-form-item>
      <el-form-item label="手机号" :label-width="60">
        <el-input v-model="admin.phone" autocomplete="off" />
      </el-form-item>
      <el-form-item label="头像" :label-width="60">
        <el-upload
            class="avatar-uploader"
            action="/api/service/upload"
            :show-file-list="false"
            :on-success="handleAvatarSuccess"
            :headers="{Authorization: tokenStore.token}"
            name="file"
            accept="image/*">
          <img v-if="admin.avatar" :src="admin.avatar" class="avatar" />
          <el-icon v-else class="avatar-uploader-icon"><Plus /></el-icon>
        </el-upload>
      </el-form-item>
    </el-form>
    <template #footer>
      <div class="dialog-footer">
        <el-button @click="dialogFormVisible = false">取消</el-button>
        <el-button type="primary" @click="updateAdminInfo">
          确认
        </el-button>
      </div>
    </template>
  </el-dialog>

  <el-dialog  v-model="dialogResetPasswordDialog" title="重置密码" width="500" :lock-scroll="false">
    <el-form ref="resetForm" :rules="rules" :model="adminPasswordDTO">
      <el-form-item prop="oldPassword" label="原密码" :label-width="100">
        <el-input v-model="adminPasswordDTO.oldPassword" autocomplete="off"/>
      </el-form-item>
      <el-form-item prop="newPassword" label="新密码" :label-width="100">
        <el-input v-model="adminPasswordDTO.newPassword" autocomplete="off"/>
      </el-form-item>
      <el-form-item prop="reNewPassword" label="重复新密码" :label-width="100">
        <el-input v-model="adminPasswordDTO.reNewPassword" autocomplete="off"/>
      </el-form-item>
    </el-form>
    <template #footer>
      <div class="dialog-footer">
        <el-button @click="dialogResetPasswordDialog = false">取消</el-button>
        <el-button type="primary" @click="resetPassword(resetForm)">
          确认
        </el-button>
      </div>
    </template>
  </el-dialog>
</template>

<style lang="scss" scoped>
.layout-container {
  height: 100vh;

  /* 左侧菜单 */
  .el-aside {
    display: flex;
    flex-direction: column;
    /* 深墨青，和后端登录页的品牌色是一套 */
    background: linear-gradient(180deg, #10403c 0%, #0c2c2a 100%);

    .sidebar-brand {
      flex: none;
      display: flex;
      align-items: center;
      gap: 10px;
      height: 60px;
      padding: 0 16px;
      border-bottom: 1px solid rgba(255, 255, 255, .08);

      .brand-icon {
        flex: none;
        width: 34px;
        height: 34px;
        border-radius: 10px;
        font-size: 19px;
        color: #5eead4;
        background-color: rgba(94, 234, 212, .13);
        border: 1px solid rgba(94, 234, 212, .22);
      }

      .brand-text {
        min-width: 0;

        .brand-name {
          margin: 0;
          font-size: 15px;
          font-weight: 600;
          letter-spacing: .5px;
          white-space: nowrap;
          color: #fff;
        }

        .brand-en {
          margin: 2px 0 0;
          font-size: 10px;
          letter-spacing: 1.5px;
          color: rgba(255, 255, 255, .42);
        }
      }
    }

    .el-menu {
      flex: 1;
      min-height: 0;
      overflow-y: auto;
      padding: 8px 0;
      border-right: none;
      /* 原来配色写在组件 props 上，会以内联样式注入，优先级高于这里的样式，所以改成用 CSS 变量接管 */
      --el-menu-bg-color: transparent;
      --el-menu-text-color: rgba(255, 255, 255, .7);
      --el-menu-hover-text-color: #fff;
      --el-menu-hover-bg-color: rgba(255, 255, 255, .07);
      --el-menu-active-color: #5eead4;
      --el-menu-base-level-padding: 14px;
      --el-menu-level-padding: 16px;
      --el-menu-item-height: 44px;
      --el-menu-sub-item-height: 40px;

      :deep(.el-menu-item),
      :deep(.el-sub-menu__title) {
        margin: 2px 10px;
        border-radius: 8px;
        transition: background-color .2s, color .2s;
      }

      :deep(.el-menu-item.is-active) {
        position: relative;
        font-weight: 600;
        background-color: rgba(20, 184, 166, .16);

        /* 左侧高亮竖条 */
        &::before {
          content: '';
          position: absolute;
          top: 50%;
          left: 0;
          width: 3px;
          height: 18px;
          border-radius: 0 3px 3px 0;
          background-color: #5eead4;
          transform: translateY(-50%);
        }
      }

      :deep(.el-sub-menu.is-active > .el-sub-menu__title) {
        color: #5eead4;
      }
    }
  }

  /* 顶部栏 */
  .el-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    height: 60px;
    padding: 0 20px;
    background-color: #fff;
    border-bottom: 1px solid #eef1f3;

    .header-title {
      display: flex;
      align-items: center;
      font-size: 16px;
      font-weight: 600;
      color: #1f2d3d;

      &::before {
        content: '';
        width: 4px;
        height: 16px;
        margin-right: 10px;
        border-radius: 2px;
        background: linear-gradient(180deg, #14b8a6, #0f766e);
      }
    }

    .el-dropdown__box {
      display: flex;
      align-items: center;
      gap: 8px;
      padding: 4px 10px;
      border-radius: 8px;
      cursor: pointer;
      outline: none;
      transition: background-color .2s;

      &:hover {
        background-color: #f2f5f6;
      }

      .dropdown-name {
        font-size: 14px;
        color: #46555f;
      }

      .el-icon {
        font-size: 14px;
        color: #98a4ae;
      }
    }
  }

  /* 内容区：淡灰底，让 el-card 浮起来 */
  .el-main {
    padding: 20px;
    background-color: #f0f2f5;

    /* 页面卡片：去描边、轻投影、大圆角，统一后台质感 */
    :deep(.el-card) {
      border: none;
      border-radius: 10px;
      box-shadow: 0 1px 2px rgba(0, 0, 0, .04), 0 4px 16px rgba(31, 45, 61, .06);
    }

    :deep(.el-card__header) {
      padding: 16px 20px;
      border-bottom: 1px solid #f0f2f5;
    }

    :deep(.el-card__body) {
      padding: 20px;
    }

    /* 表格：统一淡灰表头 */
    :deep(.el-table) {
      --el-table-header-bg-color: #f7f9fb;
      --el-table-header-text-color: #46555f;

      th.el-table__cell {
        font-weight: 600;
      }
    }
  }

  /* 底部 */
  .el-footer {
    display: flex;
    align-items: center;
    justify-content: center;
    height: 48px;
    font-size: 12px;
    color: #94a3b8;
    background-color: #fff;
    border-top: 1px solid #eef1f3;
  }
}

.avatar-uploader .avatar {
  width: 178px;
  height: 178px;
  display: block;
}

/* el-upload 内部元素不带 scoped 的 data-v 属性，必须用 :deep() 才能生效 */
.avatar-uploader :deep(.el-upload) {
  border: 1px dashed var(--el-border-color);
  border-radius: 6px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  transition: var(--el-transition-duration-fast);

  &:hover {
    border-color: var(--el-color-primary);
  }
}

.el-icon.avatar-uploader-icon {
  font-size: 28px;
  color: #8c939d;
  width: 178px;
  height: 178px;
  text-align: center;
}
</style>

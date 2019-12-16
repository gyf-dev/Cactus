## [点我下载demo](https://www.pgyer.com/Cactus)(密码：cactus)

## 使用 
> android studio
   ```groovy
   implementation 'com.gyf.cactus:cactus:1.1.3-beta05'
   ```

## 用法（具体api请参考api说明）
#### java用法

- 注册
   ```java
    Cactus.getInstance()
          .isDebug(true)
          .setPendingIntent(pendingIntent)
          .addCallback(new CactusCallback())
          ... //其他api等
          ...
          .register(this)
   ```
- 注销
  ```java
    Cactus.getInstance().unregister(this)
   ```  
- 重启 
   ```java
    Cactus.getInstance().restart(this)
   ```   
#### kotlin用法

- 注册 
   ```kotlin
    cactus {
       setPendingIntent(pendingIntent)
       setMusicId(R.raw.main)
       isDebug(true)
       ... //其他api等
       ...
       addCallback({
          //onStop回调，可以省略
       }) { 
          //doWork回调
       }
    }
   ```
- 注销 
   ```kotlin
    cactusUnregister()
   ``` 
- 重启 
   ```kotlin
    cactusRestart()
   ```      
## 混淆规则(proguard-rules.pro)
   ```
    -keep class com.gyf.cactus.entity.* {*;} 
   ```

## api说明
  | api | 说明 | api | 说明 |
  | :-------------: |:-------------:| :-------------:| :-------------:|
  | setNotification | 设置notification，非必传，如果不传，将使用用户根据其他api设置的信息构建Notification | setNotificationChannel |设置NotificationChannel，非必传，如果不传，将使用默认的NotificationChannel |
  | hideNotification | 是否隐藏通知栏，经测试，除了android 7.1手机之外都可以隐藏，默认隐藏，非必传 | hideNotificationAfterO |是否隐藏Android 8.0以上通知栏，默认隐藏 |
  | setPendingIntent | 设置PendingIntent，用来处理通知栏点击事件，非必传 | setServiceId |服务Id，默认是1到Int.MAX_VALUE随机数，非必传 |
  | setChannelId | 渠道Id，默认是Cactus，建议用户修改，非必传 | setChannelName | 渠道名，用于设置里通知渠道展示，默认是Cactus，建议用户修改，非必传 |
  | setTitle | 通知栏标题，默认是Cactus，建议用户修改，非必传 | setContent |通知栏内容，默认是Cactus is running，建议用户修改，非必传 |
  | setRemoteViews | 设置RemoteViews（自定义布局），非必传 | setBigRemoteViews |设置BigRemoteViews（自定义布局），非必传 |
  | setSmallIcon | 通知栏小图标，默认是库里的图标，建议用户修改，非必传 | setLargeIcon  | 通知栏大图标，默认没有大图标，非必传 |
  | setMusicEnabled | 是否可以播放音乐，默认可以播放音乐，非必传 | setBackgroundMusicEnabled  | 后台是否可以播放音乐，默认不可以后台播放音乐，非必传 |
  | setMusicId | 设置自定义音乐，默认是无声音乐，该api只要在isDebug为true才会有生效，非必传 | setMusicInterval  | 设置音乐间隔时间，时间间隔越长，越省电，默认间隔时间是0，非必传 |
  | setOnePixEnabled | 是否可以使用一像素，默认可以使用，只有在android p以下可以使用，非必传 | isDebug  | 是否Debug模式，默认没有调试信息，非必传 |
  | addCallback | 增加回调，用于处理一些额外的工作，非必传 | addBackgroundCallback  | 前后台切换回调，用于处理app前后台切换，非必传 |
  | register | 必须调用，建议在Application里初始化，使用Kotlin扩展函数不需要调用此方法 | unregister  | 注销，并不会立马停止，而是在1s之后停止，非必须调用，比如可以在app完全退出的时候可以调用，根据你的需求调用 |
  | restart | 重启，与register区别在于不会重新配置CactusConfig信息，而是使用上一次配置的信息 | isRunning  | 是否在运行 |
   
## 流程图
![框架流程图](cactus.png)

## 保活效果，仅供参考(数字代码oom_adj优先级，优先级数字越小越不容易被杀)
  | 维度 | android 6.0以下虚拟机 | android 7.1虚拟机 | android 7/8/8.1/9/10虚拟机 | vovo x23 (android 9) | 华为 mate20 /OnePlus (android 9) | 华为 mate30 pro (android 10) | 
  | :-------------: |:-------------:| :-------------:| :-------------:| :-------------:|:-------------:|:-------------:|
  | 前台 | 0 | 0 |0 |0 |0 |0 |
  | 后台（优化前） | 6 | 立马死了 |11 |8 |11 |11 |
  | 后台（优化后） | 1 | 3 |3 |4 |3 |0 |
  | 息屏（优化前） | 6 | 立马死了 |11 |9 |11 |11 |
  | 息屏（优化后） | 0 | 3 |3 |4 |3 |0 |
- 说明：oom_adj优先级数字越小越不容易被杀

  | oom_adj | 说明 | oom_adj | 说明 |
  | :-------------: |:-------------:| :-------------:| :-------------:|
  | 0 | 前台进程 | 1 |可见进程 |
  | 2 | 可感知的进程，比如那种播放音乐 | 3 |正在备份的进程 |
  | 4 | 高权重进程 | 5 |有Service的进程 |
  | 6 | 与Home交互的进程 | 7 |切换进程 |
  | 8 | 不活跃的进程 | 9 |缓存进程，也就是空进程 |
  | 11 | 缓存进程，也就是空进程 | 15 |缓存进程，空进程，在内存不足的情况下就会优先被kill |
  | 16 | 预留的最低级别，一般对于缓存的进程才有可能设置成这个级别 |  | |

## 更新说明
#### 1.1.2
- 增加注销和重启功能
- 增加判断服务是否是在运行中
- 增加hideNotificationAfterO方法(是否隐藏Android 8.0以上通知栏)
- 优化代码

#### 1.1.1
- 重点：修复1.1.0版本由于新增设置渠道api(setNotificationChannel)忘记做渠道判断，导致在8.0以下手机奔溃，1.0.8版本不受影响

#### 1.1.0
- 除了android7.1手机都可以隐藏通知栏了
- 增加一些通知栏相关api，比如可以自定义view了
- 优化代码

#### 1.0.8
- 解决设置后台可以播放音乐，奔溃重启后无法继续播放音乐的问题

#### 1.0.7
- 增加前后台切换监听
- 增加设置后台是否可以播放音乐的api

## 联系我 ##
- QQ群 314360549（问题交流）
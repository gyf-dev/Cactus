## [点我下载demo](https://www.pgyer.com/1osg)

## 使用 
> android studio
   ```groovy
   implementation 'com.gyf.cactus:cactus:1.1.0'
   ```

## 用法（具体api请参考注释，这里就不一一列出来了）
- java用法

   ```java
    Cactus.getInstance()
          .isDebug(true)
          .setPendingIntent(pendingIntent)
          .addCallback(new CactusaddCallback())
          .register(this)
   ```
- kotlin用法
 
   ```kotlin
    cactus {
       setPendingIntent(pendingIntent)
       setMusicId(R.raw.main)
       isDebug(true)
       addCallback(object : CactusCallback {
              override fun doWork(times:Int) {
                           
              }
       
              override fun onStop() {
                          
              }
       })
    }
   ```
## 混淆规则(proguard-rules.pro)
   ```
    -keep class com.gyf.cactus.entity.* {*;} 
   ```

## 保活效果，仅供参考(数字代码oom_adj优先级，优先级数字越小越不容易被杀)
  | 维度 | android 6.0以下虚拟机 | android 7.1虚拟机 | android 7/8/8.1/9/10虚拟机 | vovo x23 android 9 | 
  | :-------------: |:-------------:| :-------------:| :-------------:| :-------------:|
  | 前台 | 0 | 0 |0 |0 |
  | 后台（优化前） | 6 | 立马死了 |11 |8 |
  | 后台（优化后） | 1 | 3 |3 |4 |
  | 息屏（优化前） | 6 | 立马死了 |11 |9 |
  | 息屏（优化后） | 0 | 3 |3 |4 |
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
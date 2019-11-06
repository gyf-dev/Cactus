## [点我下载demo](https://www.pgyer.com/1osg)

## 使用 
> android studio
   ```groovy
   implementation 'com.gyf.cactus:cactus:1.1.0-beta02'
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

## 更新说明
#### 1.0.8
- 解决设置后台可以播放音乐，奔溃重启后无法继续播放音乐的问题

#### 1.0.7
- 增加前后台切换监听
- 增加设置后台是否可以播放音乐的api

## 联系我 ##
- QQ群 314360549（问题交流）
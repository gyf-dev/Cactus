## 使用 
> android studio
   ```groovy
   implementation 'com.gyf.cactus:cactus:1.0.7'
   ```

## 用法（api请参考注释）
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
#### 1.0.7
- 增加前后台切换监听
- 增加设置后台是否可以播放音乐的api

## 联系我 ##
- QQ群 314360549（问题交流）
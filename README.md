## 使用 
> android studio
   ```groovy
   // 基础依赖包，必须要依赖
   implementation 'com.gyf.cactus:cactus:1.0.1'
   ```

## Api详解（api请参考注释）
- java用法

   ```java
    Cactus.getInstance().isDebug(true).setPendingIntent(pendingIntent).addCallback(new CactusaddCallback()).register(this)
   ```
- kotlin用法
 
   ```kotlin
    cactus {
       setPendingIntent(pendingIntent)
       setMusicId(R.raw.main)
       isDebug(true)
       addCallback(this@App)
    }
   ```

## 联系我 ##
- QQ群 314360549（问题交流）
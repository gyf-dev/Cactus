// ICactusInterface.aidl
package com.gyf.cactus.entity;

import com.gyf.cactus.entity.CactusConfig;

interface ICactusInterface {
    void wakeup(in CactusConfig config);
    void connectionTimes(in int time);
}

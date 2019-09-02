// ICactusInterface.aidl
package com.gyf.cactus;

import com.gyf.cactus.CactusConfig;
import com.gyf.cactus.NotificationConfig;
import com.gyf.cactus.DefaultConfig;

interface ICactusInterface {
    void wakeup(in CactusConfig config);
}

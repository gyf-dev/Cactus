// ICactusInterface.aidl
package com.gyf.cactus;

import com.gyf.cactus.CactusConfig;
import com.gyf.cactus.NotificationConfig;

interface ICactusInterface {
    void wakeup(in CactusConfig config);
}

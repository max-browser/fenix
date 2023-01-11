package org.mozilla.fenix.setdefaultbrowser

import com.max.browser.core.RemoteConfigManager
import com.max.browser.core.base.BaseViewModel

class DefaultBrowserViewModel : BaseViewModel() {
    val remoteConfigInited = RemoteConfigManager.getInstance().remoteConfigInited
}

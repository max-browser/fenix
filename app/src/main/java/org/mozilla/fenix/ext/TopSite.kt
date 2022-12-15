/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.fenix.ext

import mozilla.components.feature.top.sites.TopSite
import org.mozilla.fenix.home.topsites.TopSitePagerViewHolder
import org.mozilla.fenix.settings.SupportUtils

/**
 * Returns the type name of the [TopSite].
 */
fun TopSite.name(): String = when (this) {
    is TopSite.Default -> "DEFAULT"
    is TopSite.Frecent -> "FRECENT"
    is TopSite.Pinned -> "PINNED"
    is TopSite.Provided -> "PROVIDED"
}

/**
 * Returns a sorted list of [TopSite] with the default Google top site always appearing
 * as the first item.
 */
fun List<TopSite>.sort(): List<TopSite> {
    return this.sortedBy {
        when (it) {
            is TopSite.Default -> {
                when (it.url) {
                    SupportUtils.MAX_STATUS_SAVER_URL -> 0L
                    SupportUtils.GOOGLE_URL -> 1L
                    SupportUtils.WIKIPEDIA_URL -> 2L
                    SupportUtils.FACEBOOK_URL -> 3L
                    SupportUtils.INSTAGRAM_URL -> 4L
                    SupportUtils.TWITTER_URL -> 5L
                    SupportUtils.YOUTUBE_URL -> 6L
                    else -> 7L
                }
            }
            else -> it.createdAt ?: Long.MAX_VALUE
        }
    }
}

/**
 * Returns a list of [TopSite] with the added Status Saver item if needed.
 */
fun List<TopSite>.checkToAddStatusSaverTopSite(): List<TopSite> {
    val result = this.toMutableList()
    var has = false
    result.forEach {
        if (it.url == SupportUtils.MAX_STATUS_SAVER_URL) {
            has = true
        }
    }
    if (!has) {
        result.add(0, TopSitePagerViewHolder.TOP_SITE_STATUS_SAVER)
    }
    return result
}

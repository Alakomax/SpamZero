package com.alakomax.spamzero.util

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

object SmsDeduplicator {
    private val mutex = Mutex()
    private val recentHashes = LinkedHashMap<String, Long>()

    suspend fun isDuplicateAndMark(sender: String, body: String, timeWindowMs: Long = 10000L): Boolean {
        return mutex.withLock {
            val now = System.currentTimeMillis()
            val iterator = recentHashes.entries.iterator()
            while (iterator.hasNext()) {
                val entry = iterator.next()
                if (now - entry.value > timeWindowMs) {
                    iterator.remove()
                }
            }

            val key = "$sender::$body"
            if (recentHashes.containsKey(key)) {
                true
            } else {
                recentHashes[key] = now
                false
            }
        }
    }
}

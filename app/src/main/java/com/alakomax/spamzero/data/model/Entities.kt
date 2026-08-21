package com.alakomax.spamzero.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "spam_rules")
data class RuleEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val pattern: String,
    val description: String,
    val category: String = "Llamadas Nacionales",
    val title: String = "",
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "quarantine_logs")
data class QuarantineLogEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val rawPhoneNumber: String,
    val normalizedPhoneNumber: String,
    val matchedPattern: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isReviewed: Boolean = false
)

@Entity(tableName = "sms_quarantine_logs")
data class SmsQuarantineLogEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val senderPhoneNumber: String,
    val messageBody: String,
    val matchedPattern: String,
    val timestamp: Long = System.currentTimeMillis()
)

package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tutor_history")
data class HistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val englishTerm: String,
    val englishDefinition: String,
    val malayalamExplanation: String,
    val analogyTitle: String,
    val analogyDetails: String,
    val tutorEncouragement: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isBookmarked: Boolean = false
)

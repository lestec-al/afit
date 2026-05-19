package com.yurhel.alex.afit.ui.screen_main.upbar

data class LevelObj(
    val title: String,
    val descriptions: List<String>,
    val currentLevel: String,
    val nextLevel: String,
    val progress: Float
)
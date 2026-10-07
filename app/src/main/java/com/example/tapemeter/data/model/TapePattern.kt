package com.example.tapemeter.data.model

enum class TapePattern(
    val title: String,
    val subtitle: String,
    val description: String
) {
    TOP_ONLY(
        title = "Top Only",
        subtitle = "One center seam",
        description = "One center seam across box width."
    ),
    TOP_BOTTOM(
        title = "Top + Bottom",
        subtitle = "Center seam on top and bottom",
        description = "Center seam on top and bottom across box width."
    ),
    TOP_BOTTOM_SIDE(
        title = "Top + Bottom + Side",
        subtitle = "Top, bottom, and one side seam",
        description = "Top and bottom center seams, plus one side seam along box height."
    );

    companion object {
        fun fromName(name: String?): TapePattern {
            return entries.firstOrNull { it.name.equals(name, ignoreCase = true) } ?: TOP_BOTTOM
        }
    }
}

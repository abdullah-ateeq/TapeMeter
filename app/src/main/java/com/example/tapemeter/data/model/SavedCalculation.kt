package com.example.tapemeter.data.model

import java.math.BigDecimal
import java.util.UUID

data class SavedCalculation(
    val id: String = UUID.randomUUID().toString(),
    val projectName: String,
    val lengthMm: String,
    val widthMm: String,
    val heightMm: String,
    val dimensionUnit: String = MeasurementUnit.CENTIMETER.symbol,
    val boxCount: Int,
    val tapePattern: String,
    val overlapMm: String,
    val wastePercent: String,
    val useRollCalculator: Boolean,
    val rollLengthMm: String?,
    val rollUnit: String = MeasurementUnit.METER.symbol,
    val tapePerBoxMm: String,
    val baseTapeMm: String,
    val totalTapeMm: String,
    val rollsRequired: Int?,
    val timestampMillis: Long = System.currentTimeMillis(),
    val isFavorite: Boolean = false
) {
    fun toInput(): TapeMeterInput {
        return TapeMeterInput(
            projectName = projectName,
            lengthMm = BigDecimal(lengthMm),
            widthMm = BigDecimal(widthMm),
            heightMm = BigDecimal(heightMm),
            boxCount = boxCount,
            tapePattern = TapePattern.fromName(tapePattern),
            overlapMm = BigDecimal(overlapMm),
            wastePercent = BigDecimal(wastePercent),
            useRollCalculator = useRollCalculator,
            rollLengthMm = rollLengthMm?.let { BigDecimal(it) }
        )
    }

    companion object {
        fun escape(str: String): String {
            return buildString {
                for (c in str) {
                    when (c) {
                        '\\' -> append("\\\\")
                        '"' -> append("\\\"")
                        '\b' -> append("\\b")
                        '\u000C' -> append("\\f")
                        '\n' -> append("\\n")
                        '\r' -> append("\\r")
                        '\t' -> append("\\t")
                        else -> append(c)
                    }
                }
            }
        }

        fun toJson(item: SavedCalculation): String {
            return buildString {
                append("{")
                append("\"id\":\"").append(escape(item.id)).append("\",")
                append("\"projectName\":\"").append(escape(item.projectName)).append("\",")
                append("\"lengthMm\":\"").append(escape(item.lengthMm)).append("\",")
                append("\"widthMm\":\"").append(escape(item.widthMm)).append("\",")
                append("\"heightMm\":\"").append(escape(item.heightMm)).append("\",")
                append("\"dimensionUnit\":\"").append(escape(item.dimensionUnit)).append("\",")
                append("\"boxCount\":").append(item.boxCount).append(",")
                append("\"tapePattern\":\"").append(escape(item.tapePattern)).append("\",")
                append("\"overlapMm\":\"").append(escape(item.overlapMm)).append("\",")
                append("\"wastePercent\":\"").append(escape(item.wastePercent)).append("\",")
                append("\"useRollCalculator\":").append(item.useRollCalculator).append(",")
                append("\"rollLengthMm\":").append(if (item.rollLengthMm != null) "\"${escape(item.rollLengthMm)}\"" else "null").append(",")
                append("\"rollUnit\":\"").append(escape(item.rollUnit)).append("\",")
                append("\"tapePerBoxMm\":\"").append(escape(item.tapePerBoxMm)).append("\",")
                append("\"baseTapeMm\":\"").append(escape(item.baseTapeMm)).append("\",")
                append("\"totalTapeMm\":\"").append(escape(item.totalTapeMm)).append("\",")
                append("\"rollsRequired\":").append(item.rollsRequired?.toString() ?: "null").append(",")
                append("\"timestampMillis\":").append(item.timestampMillis).append(",")
                append("\"isFavorite\":").append(item.isFavorite)
                append("}")
            }
        }

        fun listToJson(list: List<SavedCalculation>): String {
            return list.joinToString(separator = ",", prefix = "[", postfix = "]") { toJson(it) }
        }

        fun listFromJson(jsonStr: String): List<SavedCalculation> {
            val trimmed = jsonStr.trim()
            if (!trimmed.startsWith("[") || !trimmed.endsWith("]")) return emptyList()
            val inner = trimmed.substring(1, trimmed.length - 1).trim()
            if (inner.isEmpty()) return emptyList()

            val items = mutableListOf<SavedCalculation>()
            var depth = 0
            var inQuote = false
            var isEscaped = false
            val currentObject = StringBuilder()

            for (i in inner.indices) {
                val c = inner[i]
                if (isEscaped) {
                    currentObject.append(c)
                    isEscaped = false
                    continue
                }
                if (c == '\\') {
                    currentObject.append(c)
                    isEscaped = true
                    continue
                }
                if (c == '"') {
                    inQuote = !inQuote
                    currentObject.append(c)
                    continue
                }
                if (!inQuote) {
                    if (c == '{') {
                        depth++
                        currentObject.append(c)
                    } else if (c == '}') {
                        depth--
                        currentObject.append(c)
                        if (depth == 0) {
                            val parsed = fromJsonObject(currentObject.toString())
                            if (parsed != null) {
                                items.add(parsed)
                            }
                            currentObject.clear()
                        }
                    } else if (c == ',' && depth == 0) {
                        // separator between objects
                    } else {
                        currentObject.append(c)
                    }
                } else {
                    currentObject.append(c)
                }
            }
            return items
        }

        private fun unescape(str: String): String {
            val sb = StringBuilder()
            var i = 0
            while (i < str.length) {
                val c = str[i]
                if (c == '\\' && i + 1 < str.length) {
                    when (val next = str[i + 1]) {
                        '\\' -> { sb.append('\\'); i += 2 }
                        '"' -> { sb.append('"'); i += 2 }
                        'n' -> { sb.append('\n'); i += 2 }
                        'r' -> { sb.append('\r'); i += 2 }
                        't' -> { sb.append('\t'); i += 2 }
                        'b' -> { sb.append('\b'); i += 2 }
                        'f' -> { sb.append('\u000C'); i += 2 }
                        else -> { sb.append(next); i += 2 }
                    }
                } else {
                    sb.append(c)
                    i++
                }
            }
            return sb.toString()
        }

        fun fromJsonObject(objStr: String): SavedCalculation? {
            try {
                val trimmed = objStr.trim()
                if (!trimmed.startsWith("{") || !trimmed.endsWith("}")) return null
                val inner = trimmed.substring(1, trimmed.length - 1)

                val map = mutableMapOf<String, String>()
                var inQuote = false
                var isEscaped = false
                val key = StringBuilder()
                val value = StringBuilder()
                var parsingKey = true

                var i = 0
                while (i < inner.length) {
                    val c = inner[i]
                    if (isEscaped) {
                        if (parsingKey) key.append(c) else value.append(c)
                        isEscaped = false
                        i++
                        continue
                    }
                    if (c == '\\') {
                        if (parsingKey) key.append(c) else value.append(c)
                        isEscaped = true
                        i++
                        continue
                    }
                    if (c == '"') {
                        inQuote = !inQuote
                        if (parsingKey) key.append(c) else value.append(c)
                        i++
                        continue
                    }
                    if (!inQuote) {
                        if (c == ':') {
                            parsingKey = false
                            i++
                            continue
                        } else if (c == ',') {
                            val cleanKey = key.toString().trim().removeSurrounding("\"")
                            val cleanVal = value.toString().trim()
                            map[cleanKey] = cleanVal
                            key.clear()
                            value.clear()
                            parsingKey = true
                            i++
                            continue
                        }
                    }
                    if (parsingKey) key.append(c) else value.append(c)
                    i++
                }
                if (key.isNotEmpty()) {
                    val cleanKey = key.toString().trim().removeSurrounding("\"")
                    val cleanVal = value.toString().trim()
                    map[cleanKey] = cleanVal
                }

                fun parseStr(k: String, default: String = ""): String {
                    val raw = map[k] ?: return default
                    val trimmedVal = raw.trim()
                    return if (trimmedVal.startsWith("\"") && trimmedVal.endsWith("\"")) {
                        unescape(trimmedVal.substring(1, trimmedVal.length - 1))
                    } else if (trimmedVal == "null") {
                        default
                    } else {
                        trimmedVal
                    }
                }

                fun parseNullableStr(k: String): String? {
                    val raw = map[k]?.trim() ?: return null
                    return if (raw == "null") null
                    else if (raw.startsWith("\"") && raw.endsWith("\"")) {
                        unescape(raw.substring(1, raw.length - 1))
                    } else raw
                }

                return SavedCalculation(
                    id = parseStr("id", UUID.randomUUID().toString()),
                    projectName = parseStr("projectName", "Untitled Packaging"),
                    lengthMm = parseStr("lengthMm", "400"),
                    widthMm = parseStr("widthMm", "300"),
                    heightMm = parseStr("heightMm", "250"),
                    dimensionUnit = parseStr("dimensionUnit", "cm"),
                    boxCount = map["boxCount"]?.trim()?.toIntOrNull() ?: 10,
                    tapePattern = parseStr("tapePattern", TapePattern.TOP_BOTTOM.name),
                    overlapMm = parseStr("overlapMm", "100"),
                    wastePercent = parseStr("wastePercent", "10"),
                    useRollCalculator = map["useRollCalculator"]?.trim()?.toBoolean() ?: false,
                    rollLengthMm = parseNullableStr("rollLengthMm"),
                    rollUnit = parseStr("rollUnit", "m"),
                    tapePerBoxMm = parseStr("tapePerBoxMm", "800"),
                    baseTapeMm = parseStr("baseTapeMm", "8000"),
                    totalTapeMm = parseStr("totalTapeMm", "8800"),
                    rollsRequired = map["rollsRequired"]?.trim()?.takeIf { it != "null" }?.toIntOrNull(),
                    timestampMillis = map["timestampMillis"]?.trim()?.toLongOrNull() ?: System.currentTimeMillis(),
                    isFavorite = map["isFavorite"]?.trim()?.toBoolean() ?: false
                )
            } catch (e: Exception) {
                return null
            }
        }
    }
}

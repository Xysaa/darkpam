package com.example.nutriscan.domain.model

enum class Disease(val displayName: String) {
    DIABETES("Diabetes"),
    HYPERTENSION("Hipertensi"),
    OBESITY("Obesitas"),
    HEART_DISEASE("Penyakit Jantung"),
    KIDNEY_DISEASE("Penyakit Ginjal");

    companion object {
        fun fromString(value: String): Disease? =
            entries.find { it.name == value }

        fun fromCsv(csv: String): List<Disease> =
            csv.split(",")
                .map { it.trim() }
                .filter { it.isNotBlank() }
                .mapNotNull { fromString(it) }

        fun toCsv(diseases: List<Disease>): String =
            diseases.joinToString(",") { it.name }
    }
}

package com.mod5.evalfinal_gestareav4.data

enum class TaskStatus(val displayName: String) {
    PENDIENTE("Pendiente"),
    COMPLETADA("Completada"),
    CANCELADA("Cancelada");

    companion object {
        // Mapea el String del Spinner/CSV al Enum
        fun fromDisplayName(name: String): TaskStatus? = values().find { it.displayName == name }
    }
}
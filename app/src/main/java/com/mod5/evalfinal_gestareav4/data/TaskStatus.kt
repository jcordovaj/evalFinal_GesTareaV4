package com.mod5.evalfinal_gestareav4.data

enum class TaskStatus(val displayName: String) {
    // Los 'displayName' DEBEN COINCIDIR con los valores del array.xml
    PENDIENTE("Pendiente"),
    COMPLETADA("Completada"),
    CANCELADA("Cancelada");

    companion object {
        // Función auxiliar para mapear el String del Spinner/CSV al Enum
        fun fromDisplayName(name: String): TaskStatus? = values().find { it.displayName == name }
    }
}
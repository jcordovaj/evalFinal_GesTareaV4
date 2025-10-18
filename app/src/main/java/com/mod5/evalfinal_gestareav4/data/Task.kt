package com.mod5.evalfinal_gestareav4.data

// Modelo de Datos para la Tarea (Task).
data class Task(
    val id           : String,
    val name         : String,
    val description  : String,
    val status       : String, // Pendiente, Completada, Cancelada
    val date         : String, // DD/MM/YYYY
    val time         : String, // HH:MM
    val category     : String,
    val requiresAlarm: Boolean
) {
    // Convierte cada objeto Task en una línea del CSV.
    fun toCsvString(): String {
        return "$id,$name,$description,$status,$date,$time,$category,$requiresAlarm"
    }

    companion object {
        // Crea un objeto Task a partir de una línea de CSV.
        fun fromCsvString(csvString: String): Task? {
            return try {
                val parts         = csvString.split(',')
                if (parts.size != 8) return null // Comprueba el número de campos
                val requiresAlarm = parts[7].toBoolean() // Convierte el booleano
                val statusEnum = TaskStatus.fromDisplayName(parts[3]) ?: TaskStatus.PENDIENTE
                Task(
                    id            = parts[0],
                    name          = parts[1],
                    description   = parts[2],
                    status        = parts[3],
                    date          = parts[4],
                    time          = parts[5],
                    category      = parts[6],
                    requiresAlarm = requiresAlarm
                    //status        = statusEnum,
                )
            } catch (e: Exception) {
                println("Error leyendo el CSV para la Tarea: $csvString - ${e.message}")
                null
            }
        }
    }
}
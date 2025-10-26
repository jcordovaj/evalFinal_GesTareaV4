package com.mod5.evalfinal_gestareav4.data

import android.app.Application
import java.io.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class TaskRepository(application: Application) {

    private val context  = application
    private val fileName = "tareas.csv"
    private val file     = File(context.getExternalFilesDir(null), fileName)

    init {
        // Aseguramos que el archivo exista
        if (!file.exists()) {
            file.createNewFile()
        }
    }

    // Se adaptó para leer todas las tareas del archivo CSV en forma asíncrona mediante 'Dispatchers.IO'
    suspend fun readAllTasks(): List<Task> = withContext(Dispatchers.IO) {
        val tasks = mutableListOf<Task>()
        if (!file.exists()) return@withContext tasks

        try {
            BufferedReader(FileReader(file)).use { reader ->
                reader.forEachLine { line ->
                    // Usa la función fromCsvString de la clase Task
                    Task.fromCsvString(line)?.let { tasks.add(it) }
                }
            }
        } catch (e: Exception) {
            println("Error al leer el archivo CSV: ${e.message}")
        }
        // Ordena por fecha y hora (lógica pendiente de refinar)
        tasks.sortedWith(compareByDescending<Task> { it.date }
            .thenByDescending { it.time })
    }

    // Guarda la lista completa de tareas en el CSV (Función auxiliar privada).
    private fun saveAllTasks(tasks: List<Task>): Boolean {
        return try {
            BufferedWriter(FileWriter(file)).use { writer ->
                tasks.forEach { task ->
                    // Usa la función toCsvString() de la clase Task
                    writer.write(task.toCsvString())
                    writer.newLine()
                }
            }
            true
        } catch (e: Exception) {
            println("Error al escribir en el archivo CSV: ${e.message}")
            false
        }
    }

    // Guarda una nueva tarea.
    suspend fun saveTaskToCSV(task: Task): Boolean = withContext(Dispatchers.IO) {
        // Se añade la nueva tarea y se guarda
        val tasks = readAllTasks().toMutableList()
        tasks.add(task)
        saveAllTasks(tasks)
    }

    // Actualiza una tarea existente.
    suspend fun updateTaskInCSV(updatedTask: Task): Boolean = withContext(Dispatchers.IO) {
        // Leemos todo para encontrar la tarea a actualizar
        val tasks = readAllTasks().toMutableList()
        val index = tasks.indexOfFirst { it.id == updatedTask.id }

        return@withContext if (index != -1) {
            tasks[index] = updatedTask
            saveAllTasks(tasks)
        } else {
            // Si no se encuentra, la guardamos como nueva (lógica de fallback)
            saveTaskToCSV(updatedTask)
        }
    }

    // Elimina una tarea por ID.
    suspend fun deleteTaskById(id: String): Boolean = withContext(Dispatchers.IO) {
        val tasks = readAllTasks().toMutableList()
        val originalSize = tasks.size
        // Eliminamos la tarea por ID
        tasks.removeIf { it.id == id }

        return@withContext if (tasks.size < originalSize) {
            // Si el tamaño cambió, guardamos la nueva lista
            saveAllTasks(tasks)
        } else {
            false
        }
    }
}
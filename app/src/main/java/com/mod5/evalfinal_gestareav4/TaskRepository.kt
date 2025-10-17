package com.mod5.evalfinal_gestareav4

import android.content.Context
import java.io.File

/**
 * Repository para manejar la persistencia de tareas en el archivo CSV.
 * Centraliza la lógica de negocio de los datos.
 */
class TaskRepository(private val context: Context) {

    private val fileName        = "tareas.csv"
    private fun getFile(): File = File(context.getExternalFilesDir(null),
        fileName)

    // Carga todas las tareas del archivo CSV.
    fun readAllTasks(): List<Task> {
        val file     = getFile()
        val taskList = mutableListOf<Task>()
        if (file.exists()) {
            file.forEachLine { line ->
                // Omite las líneas vacías o con problemas de estructura
                if (line.isBlank() || !line.contains(',')) return@forEachLine

                val parts = line.split(",")
                if (parts.size >= 8) {
                    val requiresAlarm = parts[7].toBoolean()
                    val task = Task(parts[0],
                                    parts[1],
                                    parts[2],
                                    parts[3],
                                    parts[4],
                                    parts[5],
                                    parts[6],
                                    requiresAlarm)
                    taskList.add(task)
                }
            }
        }
        return taskList
    }

    // Guarda una nueva tarea.
    fun saveTaskToCSV(task: Task): Boolean {
        return try {
            val file = getFile()
            val newLine = "${task.id}," +
                          "${task.name}," +
                          "${task.description}," +
                          "${task.status}," +
                          "${task.date}," +
                          "${task.time}," +
                          "${task.category}," +
                          "${task.requiresAlarm}\n"
            file.appendText(newLine)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    // Actualiza una tarea existente.
    fun updateTaskInCSV(updatedTask: Task): Boolean {
        return try {
            val file = getFile()
            val tempFile = File(context.getExternalFilesDir(null),
                "tareas_temp.csv")
            val lines = file.readLines()

            tempFile.bufferedWriter().use { writer ->
                lines.forEach { line ->
                    val parts = line.split(",")
                    if (parts.isNotEmpty() && parts[0] == updatedTask.id) {
                        // Escribe la línea actualizada
                        writer.write("${updatedTask.id}," +
                                           "${updatedTask.name}," +
                                           "${updatedTask.description}," +
                                           "${updatedTask.status}," +
                                           "${updatedTask.date}," +
                                           "${updatedTask.time}," +
                                           "${updatedTask.category}," +
                                           "${updatedTask.requiresAlarm}\n")
                    } else {
                        // Mantiene la línea del archivo sin cambios
                        writer.write(line + "\n")
                    }
                }
            }

            // Reemplaza el archivo original con el swap (temporal)
            if (file.delete()) {
                tempFile.renameTo(file)
            } else {
                tempFile.delete()
                return false
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    // Elimina una tarea por su ID asegurando consistencia (reescribe el archivo).
    fun deleteTaskById(taskId: String): Boolean {
        return try {
            val file = getFile()
            val tempFile = File(context.getExternalFilesDir(null),
                "tareas_temp_delete.csv")
            val lines = file.readLines()

            tempFile.bufferedWriter().use { writer ->
                lines.forEach { line ->
                    val parts = line.split(",")
                    // Se escribirá la línea sólo si el Id es distinto  del Id a eliminar
                    if (parts.isNotEmpty() && parts[0] != taskId) {
                        writer.write(line + "\n")
                    }
                }
            }

            // Reemplaza el archivo original con el swap (temporal)
            if (file.delete()) {
                tempFile.renameTo(file)
            } else {
                tempFile.delete()
                return false
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
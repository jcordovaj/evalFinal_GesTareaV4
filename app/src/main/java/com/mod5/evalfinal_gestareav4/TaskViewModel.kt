package com.mod5.evalfinal_gestareav4

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay //Para simular los temporizadores
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID

// ViewModel que gestiona las tareas. Usa LiveData para notificar a la interfaz.
class TaskViewModel(application: Application) : AndroidViewModel(application) {

    // Inicialización del Repository
    private val repository = TaskRepository(application)

    // Livedata para indicar si la aplicación está cargando datos en forma asíncrona (emula)
    private val _isLoading = MutableLiveData<Boolean>(false)
    val isLoading: LiveData<Boolean> get() = _isLoading

    // LiveData principal que contiene todas las tareas.
    private val _allTasks = MutableLiveData<List<Task>>()
    val allTasks: LiveData<List<Task>> get() = _allTasks

    // LiveData para notificar mensajes de estado a la interfaz.
    private val _statusMessage = MutableLiveData<String?>()
    val statusMessage: LiveData<String?> get() = _statusMessage

    init {
        loadTasks() // Carga inicial
    }

    // Carga las tareas desde el repo csv y actualiza el LiveData.
    fun loadTasks() {
        // Ejecuta la lectura de datos en un hilo de fondo (IO)
        viewModelScope.launch(Dispatchers.IO) {

            // Lógica del estado de carga
            withContext(Dispatchers.Main) {
                _isLoading.value     = true
                _statusMessage.value = "Cargando datos..."
            }

            // Aquí simulamos la carga asíncrona con un delay
            delay(3000L)

            val tasks = repository.readAllTasks()
            _allTasks.postValue(tasks) // Actualiza LiveData

            // Una vez finalizado, cambia el estado y notifica
            withContext(Dispatchers.Main) {
                _isLoading.value     = false
                _statusMessage.value = "Datos actualizados correctamente."
            }
        }
    }

    fun saveOrUpdateTask(
        id           : String?,
        name         : String,
        description  : String,
        status       : String,
        date         : String,
        time         : String,
        category     : String,
        requiresAlarm: Boolean
    ) {
        // Validación de campos
        if (name.isBlank()) {
            _statusMessage.postValue("ERROR: El nombre de la actividad no puede estar vacío.")
            return
        }
        if (description.isBlank()) {
            _statusMessage.postValue("ERROR: La descripción de la actividad no puede estar vacía.")
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            val isEditing = id != null
            // Heredado de los proyectos previos, usamos UUID para ID si es nueva tarea
            val taskId = id ?: UUID.randomUUID().toString()

            // Asumiendo que la clase Task existe y tiene estos 8 campos
            val task = Task(taskId, name, description, status, date, time, category, requiresAlarm)

            val success = if (isEditing) {
                repository.updateTaskInCSV(task)
            } else {
                repository.saveTaskToCSV(task)
            }

            if (success) {
                _statusMessage.postValue("Tarea ${if (isEditing) "actualizada" else "guardada"} correctamente")
                loadTasks() // Recarga para que los observadores actualicen los Fragments.
            } else {
                _statusMessage.postValue("Error al guardar la tarea")
            }
        }
    }

    // Actualiza estado al marcar una tarea como completada.
    fun markTaskAsCompleted(task: Task) {
        val completedTask = task.copy(status = "Completada")
        viewModelScope.launch(Dispatchers.IO) {
            val success = repository.updateTaskInCSV(completedTask)
            if (success) {
                _statusMessage.postValue("Tarea marcada como 'Completada'")
                loadTasks() // Recarga para actualizar la vista
            } else {
                _statusMessage.postValue("Error al marcar la tarea")
            }
        }
    }

    // Llama al repositorio para eliminar una tarea.
    fun deleteTask(task: Task) {
        viewModelScope.launch(Dispatchers.IO) {
            val success = repository.deleteTaskById(task.id)
            if (success) {
                _statusMessage.postValue("Tarea '${task.name}' eliminada (actualiza el CSV)")
                loadTasks() // Recarga la lista.
            } else {
                _statusMessage.postValue("Error al eliminar la tarea")
            }
        }
    }

    fun clearStatusMessage() {
        _statusMessage.value = null
    }
}
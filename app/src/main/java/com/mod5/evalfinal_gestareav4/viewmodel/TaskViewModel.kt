package com.mod5.evalfinal_gestareav4.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.mod5.evalfinal_gestareav4.data.Task
import com.mod5.evalfinal_gestareav4.data.TaskRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
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

    // Carga las tareas desde el repositorio CSV y actualiza el LiveData.
    fun loadTasks() {
        // Lanzamos la corrutina en el ámbito del ViewModel, por defecto, 'Main Dispatcher'
        viewModelScope.launch {

            // Mostrar estado de carga en el hilo principal
            _isLoading.value     = true
            _statusMessage.value = "Cargando datos..."

            // Simula el delay de carga del proceso asíncrono
            delay(2000L)

            // Llama al Repositorio. La función gestiona su propio I/O para impedir bloqueos.
            val tasks = repository.readAllTasks()

            // Actualiza LiveData en Main/viewModelScope
            _allTasks.postValue(tasks)

            // Finaliza el estado de carga
            _isLoading.value     = false
            _statusMessage.value = "Datos actualizados correctamente."
        }
    }

    // Guarda/Actualiza una tarea.
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
        // Valida campos obligatorios (lógica de negocio en ViewModel)
        if (name.isBlank() ||
            description.isBlank() ||
            date.isBlank() ||
            time.isBlank()) {
            _statusMessage.postValue("ERROR: Falta completar campos obligatorios.")
            return
        }

        // Ejecución asíncrona de la persistencia
        viewModelScope.launch { // Se lanza en el hilo principal, pero llama a suspend functions
            val isEditing = id != null
            val taskId = id ?: UUID.randomUUID().toString()

            val task = Task(taskId, name, description, status, date, time, category, requiresAlarm)

            val success = if (isEditing) {
                repository.updateTaskInCSV(task)
            } else {
                repository.saveTaskToCSV(task)
            }

            if (success) {
                _statusMessage.postValue(
                    "Tarea ${if (isEditing) "actualizada" else "guardada"} correctamente")
                loadTasks() // Recarga los datos y el balance
            } else {
                _statusMessage.postValue("Error al guardar la tarea")
            }
        }
    }

    // Actualiza el estado de una tarea a 'Completada'.
    fun markTaskAsCompleted(task: Task) {
        val completedTask = task.copy(status = "Completada")
        viewModelScope.launch {
            val success = repository.updateTaskInCSV(completedTask)
            if (success) {
                _statusMessage.postValue("Tarea marcada como 'Completada'")
                loadTasks() // Recarga para actualizar la vista
            } else {
                _statusMessage.postValue("Error al intentar cambiar el estado")
            }
        }
    }

    // Elimina una tarea por ID.
    fun deleteTask(task: Task) {
        viewModelScope.launch {
            val success = repository.deleteTaskById(task.id)
            if (success) {
                _statusMessage.postValue("Tarea '${task.name}' eliminada.")
                loadTasks() // Reload de la lista.
            } else {
                _statusMessage.postValue("Error al eliminar la tarea")
            }
        }
    }

    // Limpia el mensaje
    fun clearStatusMessage() {
        _statusMessage.value = null
    }
}
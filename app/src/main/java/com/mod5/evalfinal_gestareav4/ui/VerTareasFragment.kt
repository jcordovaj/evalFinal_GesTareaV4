package com.mod5.evalfinal_gestareav4.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.mod5.evalfinal_gestareav4.MainActivity
import com.mod5.evalfinal_gestareav4.R
import com.mod5.evalfinal_gestareav4.adapter.TaskAdapter
import com.mod5.evalfinal_gestareav4.data.Task
import com.mod5.evalfinal_gestareav4.viewmodel.TaskViewModel

/**
 * Fragmento encargado de mostrar la lista de tareas 'Pendientes'.
 * Observa el LiveData del TaskViewModel para actualizar la UI en tiempo real.
 */
class VerTareasFragment : Fragment() {

    private lateinit var recyclerView        : RecyclerView
    private lateinit var textViewEmptyMessage: TextView
    private lateinit var taskAdapter         : TaskAdapter
    private lateinit var progressBarLoading  : ProgressBar
    private lateinit var taskViewModel       : TaskViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(
            R.layout.ver_tareas,
            container,
            false)

        // Inicializa el ViewModel, usando requireActivity() para compartir la instancia
        taskViewModel = ViewModelProvider(requireActivity()).get(TaskViewModel::class.java)

        // Inicialización de Vistas
        recyclerView               = view.findViewById(R.id.recyclerViewTasks)
        textViewEmptyMessage       = view.findViewById(R.id.textViewEmptyListMessage)
        progressBarLoading         = view.findViewById(R.id.progressBarLoading)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        // Inicializa el adaptador con callbacks de interacción
        taskAdapter = TaskAdapter(
            tasks = emptyList(), // Empieza vacío
            onItemClick = { task ->
                // Llama al método de edición definido en MainActivity
                (activity as? MainActivity)?.startTaskEdit(task)
            },
            onDeleteClick = { task ->
                // Muestra diálogo de confirmación
                confirmAndDeleteTask(task)
            }
        )
        recyclerView.adapter = taskAdapter

        // Observadores de LiveData

        // Observe para la lista de tareas
        taskViewModel.allTasks.observe(viewLifecycleOwner) { allTasks ->
            // Filtra sólo las tareas 'Pendientes' para esta vista
            // manteniendo la lógica de las versiones anteriores (v1, v2, v3)
            val pendingTasks = allTasks.filter { it.status == "Pendiente" }

            // Actualiza el adaptador
            taskAdapter.updateTasks(pendingTasks)

            // Caso Lista vacía
            if (pendingTasks.isEmpty() && progressBarLoading.visibility != View.VISIBLE) {
                textViewEmptyMessage.visibility = View.VISIBLE
                recyclerView.visibility         = View.GONE
            } else {
                textViewEmptyMessage.visibility = View.GONE
            }
        }

        // Observa el estado de carga (simulación asíncrona)
        taskViewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            progressBarLoading.visibility = if (isLoading) View.VISIBLE else View.GONE

            // Si está cargando, ocultamos la lista. Si termina, mostramos la lista si hay items.
            recyclerView.visibility = if (isLoading) View.GONE else {
                if (taskAdapter.itemCount > 0) View.VISIBLE else View.GONE
            }

            // Asegura que el mensaje de lista vacía se oculte durante la carga
            if (isLoading) textViewEmptyMessage.visibility = View.GONE
        }

        // 3. Observa los mensajes de estado (éxito/error)
        taskViewModel.statusMessage.observe(viewLifecycleOwner) { message ->
            message?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
                // Limpia el mensaje en el ViewModel para evitar que se muestre de nuevo
                taskViewModel.clearStatusMessage()
            }
        }
        return view
    }

    /**
     * Muestra un diálogo de confirmación antes de llamar al método de eliminación del ViewModel.
     */
    private fun confirmAndDeleteTask(task: Task) {
        AlertDialog.Builder(requireContext())
            .setTitle("Eliminar Tarea")
            .setMessage("¿Estás seguro de eliminar la tarea permanentemente: '${task.name}'?")
            .setPositiveButton("Eliminar") { _, _ ->
                // Delega la operación de I/O al ViewModel
                taskViewModel.deleteTask(task)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    // Crea una nueva instancia
    companion object {
        @JvmStatic
        fun newInstance() = VerTareasFragment()
    }
}
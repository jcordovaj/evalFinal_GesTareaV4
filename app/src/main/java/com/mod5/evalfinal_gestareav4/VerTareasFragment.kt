package com.mod5.evalfinal_gestareav4

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.lifecycle.ViewModelProvider
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class VerTareasFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var textViewEmptyMessage: TextView
    private lateinit var taskAdapter : TaskAdapter
    // Referencia al ViewModel
    private lateinit var taskViewModel: TaskViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.ver_tareas,
                                    container,
                                    false)

        // Inicializa el ViewModel
        taskViewModel = ViewModelProvider(requireActivity()).get(TaskViewModel::class.java)

        recyclerView               = view.findViewById(R.id.recyclerViewTasks)
        textViewEmptyMessage       = view.findViewById(R.id.textViewEmptyListMessage)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        // Inicializa el adaptador
        taskAdapter = TaskAdapter(
            tasks = emptyList(), // Empieza vacío
            onItemClick = { task ->
                (activity as? MainActivity)?.startTaskEdit(task)
            },
            onDeleteClick = { task ->
                // Muestra diálogo de confirmación antes de eliminar tarea
                confirmAndDeleteTask(task)
            }
        )
        recyclerView.adapter = taskAdapter

        // LiveData activa la actualización de la UI (Observer).
        taskViewModel.allTasks.observe(viewLifecycleOwner, { allTasks ->
            // Filtra sólo las tareas 'Pendientes' para la vista
            val pendingTasks = allTasks.filter { it.status == "Pendiente" }

            // Actualiza la lista en el adaptador y redibuja la lista
            taskAdapter.updateTasks(pendingTasks)

            // Control de Visibilidad
            if (pendingTasks.isEmpty()) {
                textViewEmptyMessage.visibility = View.VISIBLE
                recyclerView.visibility = View.GONE
            } else {
                textViewEmptyMessage.visibility = View.GONE
                recyclerView.visibility = View.VISIBLE
            }
        })

        return view
    }

    // Muestra un diálogo de confirmación antes de eliminar una tarea.
    private fun confirmAndDeleteTask(task: Task) {
        AlertDialog.Builder(requireContext())
            .setTitle("Eliminar Tarea")
            .setMessage("¿Eliminar la tarea '${task.name}'?")
            .setPositiveButton("Eliminar") { _, _ ->
                // Llama al ViewModel para ejecutar la eliminación
                taskViewModel.deleteTask(task)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }
}
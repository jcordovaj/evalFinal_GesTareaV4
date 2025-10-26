package com.mod5.evalfinal_gestareav4.ui


import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
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
    private lateinit var mainActivity        : MainActivity



    // Muestra un diálogo de confirmación antes de llamar al método de eliminación del ViewModel.
    private fun confirmAndDeleteTask(task: Task) {
        AlertDialog.Builder(requireContext())
            .setTitle("Eliminar Tarea")
            .setMessage("¿Estás seguro de eliminar la tarea: '${task.name}'?")
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
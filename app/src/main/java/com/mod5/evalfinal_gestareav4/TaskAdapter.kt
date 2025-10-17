package com.mod5.evalfinal_gestareav4

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

data class Task(
    val id             : String,
    val name           : String,
    val description    : String,
    val status         : String,
    val date           : String,
    val time           : String,
    val category       : String,
    val requiresAlarm  : Boolean
)

class TaskAdapter(
    private var tasks        : List<Task>,
    private val onItemClick  : (Task) -> Unit,
    // Callback para manejar el evento de eliminación
    private val onDeleteClick: (Task) -> Unit
) : RecyclerView.Adapter<TaskAdapter.TaskViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_task, parent, false)
        return TaskViewHolder(view)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        val task = tasks[position]
        // Callback de eliminación
        holder.bind(task, onItemClick, onDeleteClick, position + 1)
    }

    override fun getItemCount(): Int = tasks.size

    /**
     * Permite que el Fragment/ViewModel actualice la lista.
     * Esto se llama cuando el LiveData en el ViewModel cambia.
     */
    fun updateTasks(newTasks: List<Task>) {
        this.tasks = newTasks
        notifyDataSetChanged() // lanza la notificación a RecyclerView que los datos han cambiado
    }

    class TaskViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // Ref layout
        private val taskIdOrdinal  : TextView = itemView.findViewById(R.id.textViewTaskIdOrdinal)
        private val taskIdUuid     : TextView = itemView.findViewById(R.id.textViewTaskIdUuid)
        private val taskName       : TextView = itemView.findViewById(R.id.textViewTaskName)
        private val taskDescription: TextView = itemView.findViewById(R.id.textViewTaskDescription)
        private val taskDateTime   : TextView = itemView.findViewById(R.id.textViewTaskDateTime)
        private val taskStatus     : TextView = itemView.findViewById(R.id.textViewTaskStatus)
        private val taskCategory   : TextView = itemView.findViewById(R.id.textViewTaskCategory)
        private val taskAlarm      : TextView = itemView.findViewById(R.id.textViewTaskAlarm)

        // Botón eliminación
        private val buttonDelete: Button = itemView.findViewById(R.id.buttonDeleteTask)

        fun bind(task: Task, onItemClick: (Task) -> Unit, onDeleteClick: (Task) -> Unit, ordinalId: Int) {
            taskIdOrdinal.text   = "ID  : #$ordinalId"
            taskIdUuid.text      = "UUID: ${task.id}"
            taskName.text        = task.name
            taskDescription.text = task.description
            taskDateTime.text    = "Fecha y Hora: ${task.date} - ${task.time}"
            taskStatus.text      = "Estado: ${task.status}"
            taskCategory.text    = "Tipo: ${task.category}"
            taskAlarm.text       = if (task.requiresAlarm) "Alarma: ✅ ON" else "Alarma: ❌ OFF"

            // Listener para Edición
            itemView.setOnClickListener { onItemClick(task) }

            // Listener para Eliminación
            buttonDelete.setOnClickListener { onDeleteClick(task) }
        }
    }
}
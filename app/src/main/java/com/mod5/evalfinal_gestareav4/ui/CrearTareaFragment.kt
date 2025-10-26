package com.mod5.evalfinal_gestareav4.ui

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import java.util.Calendar
import android.widget.*
import com.mod5.evalfinal_gestareav4.R
import com.mod5.evalfinal_gestareav4.MainActivity
import com.mod5.evalfinal_gestareav4.viewmodel.TaskViewModel

class CrearTareaFragment : Fragment() {

    // Variables
    private lateinit var taskViewModel: TaskViewModel

    // Elementos de la vista
    private lateinit var editTextTaskName       : EditText
    private lateinit var editTextTaskDescription: EditText
    private lateinit var editTextTaskDate       : EditText
    private lateinit var editTextTaskTime       : EditText
    private lateinit var spinnerStatus          : Spinner
    private lateinit var spinnerCategory        : Spinner
    private lateinit var checkBoxRequiresAlarm  : CheckBox

    // Variables para cambio de Estado
    private var taskId      : String? = null
    private var isEditing   : Boolean = false
    private var selectedDate: String  = ""
    private var selectedTime: String  = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.crear_tarea,
            container,
            false)

        // Inicializa el ViewModel
        taskViewModel = ViewModelProvider(requireActivity()).get(TaskViewModel::class.java)

        // Inicialización los elementos del layout
        editTextTaskName        = view.findViewById(R.id.editTextTaskName)
        editTextTaskDescription = view.findViewById(R.id.editTextTaskDescription)
        editTextTaskDate        = view.findViewById(R.id.editTextTaskDate)
        editTextTaskTime        = view.findViewById(R.id.editTextTaskTime)
        spinnerStatus           = view.findViewById(R.id.spinnerStatus)
        spinnerCategory         = view.findViewById(R.id.spinnerCategory)
        checkBoxRequiresAlarm   = view.findViewById(R.id.checkBoxRequiresAlarm)

        // objeto botón 'Grabar'
        val buttonGrabar: Button = view.findViewById(R.id.buttonSaveTask)

        // Seteo de Pickers para fecha y hora
        editTextTaskDate.setOnClickListener { showDatePickerDialog() }
        editTextTaskTime.setOnClickListener { showTimePickerDialog() }

        // Carga datos de la tarea escogida si es edición
        arguments?.let { args ->
            isEditing    = true
            taskId       = args.getString(TASK_ID_KEY)
            editTextTaskName.setText(args.getString(TASK_NAME_KEY))
            editTextTaskDescription.setText(args.getString(TASK_DESCRIPTION_KEY))
            selectedDate = args.getString(TASK_DATE_KEY) ?: ""
            selectedTime = args.getString(TASK_TIME_KEY) ?: ""
            editTextTaskDate.setText(selectedDate)
            editTextTaskTime.setText(selectedTime)
            checkBoxRequiresAlarm.isChecked = args.getBoolean(TASK_ALARM_KEY)

            // Setea los Spinners para estado y categoría
            val statusAdapter = spinnerStatus.adapter as? ArrayAdapter<String>
            statusAdapter?.getPosition(args.getString(TASK_STATUS_KEY))
                ?.let { spinnerStatus.setSelection(it) }

            val categoryAdapter = spinnerCategory.adapter as? ArrayAdapter<String>
            categoryAdapter?.getPosition(args.getString(TASK_CATEGORY_KEY))
                ?.let { spinnerCategory.setSelection(it) }

            buttonGrabar.text = "Actualizar Tarea" // Cambia el texto para edición
        } ?: run {
            buttonGrabar.text = "Guardar Tarea" // Texto por defecto para creación
        }

        // Listener del botón
        buttonGrabar.setOnClickListener {
            // Captura datos y valida campos
            val taskName        = editTextTaskName.text.toString().trim()
            val taskDescription = editTextTaskDescription.text.toString().trim()
            val taskStatus      = spinnerStatus.selectedItem.toString()
            val taskCategory    = spinnerCategory.selectedItem.toString()
            val requiresAlarm   = checkBoxRequiresAlarm.isChecked

            // Valida campos obligatorios
            if (taskName.isEmpty() || selectedDate.isEmpty() || selectedTime.isEmpty()) {
                Toast.makeText(requireContext(),
                    "Debe completar campos obligatorios.",
                    Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            // Validación de permisos para alarma
            if (requiresAlarm &&
                android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU &&
                requireContext().checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS)
                != android.content.pm.PackageManager.PERMISSION_GRANTED)
            {
                requestPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
                Toast.makeText(requireContext(),
                    "Falta autorizar permiso de notificación. Intente de nuevo.",
                    Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            // Si la validación es exitosa, llama al ViewModel
            taskViewModel.saveOrUpdateTask(
                id            = taskId,
                name          = taskName,
                description   = taskDescription,
                status        = taskStatus,
                date          = selectedDate,
                time          = selectedTime,
                category      = taskCategory,
                requiresAlarm = requiresAlarm
            )
            // Vuelve a la vista
            (activity as? MainActivity)?.loadFragment(VerTareasFragment())
            resetFormFields()
        }
        return view
    }

    // Métodos auxiliares (DatePicker, TimePicker, etc.)
    // *************************************************

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            Toast.makeText(requireContext(),
                "Permiso de notificación concedido. Intente grabar de nuevo.",
                Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(requireContext(),
                "ALERTA: Falta autorizar permiso, la alarma no funcionará.",
                Toast.LENGTH_LONG).show()
        }
    }

    // Limpia el formulario
    private fun resetFormFields() {
        editTextTaskName.setText("")
        editTextTaskDescription.setText("")
        editTextTaskDate.setText("")
        editTextTaskTime.setText("")
        checkBoxRequiresAlarm.isChecked = false
        selectedDate = ""
        selectedTime = ""

        // Resetea los Spinners
        (spinnerStatus.adapter as? ArrayAdapter<String>)?.let { adapter ->
            spinnerStatus.setSelection(adapter.getPosition("Pendiente"))
        }
        (spinnerCategory.adapter as? ArrayAdapter<String>)?.let { adapter ->
            spinnerCategory.setSelection(adapter.getPosition("Evento"))
        }

        taskId    = null
        isEditing = false
    }

    private fun showDatePickerDialog() {
        val calendar   = Calendar.getInstance()
        val year       = calendar.get(Calendar.YEAR)
        val month      = calendar.get(Calendar.MONTH)
        val day        = calendar.get(Calendar.DAY_OF_MONTH)
        val datePicker = DatePickerDialog(requireContext(),
            { _, selectedYear,
              selectedMonth,
              selectedDay ->
                // Guarda la fecha
                updateDateTimeFields(selectedYear,
                    selectedMonth,
                    selectedDay,
                    -1,
                    -1)
            }, year, month, day)
        datePicker.show()
    }

    private fun showTimePickerDialog() {
        val calendar = Calendar.getInstance()
        val hour     = calendar.get(Calendar.HOUR_OF_DAY)
        val minute   = calendar.get(Calendar.MINUTE)

        val timePicker = TimePickerDialog(requireContext(),
            { _, selectedHour, selectedMinute ->
                updateDateTimeFields(-1, -1, -1, selectedHour, selectedMinute)
            }, hour, minute, true) // true para formato de 24 horas

        timePicker.show()
    }

    private fun updateDateTimeFields(year: Int, month: Int, day: Int, hour: Int, minute: Int) {
        if (year != -1) {
            selectedDate = String.format("%02d/%02d/%d", day, month + 1, year)
            editTextTaskDate.setText(selectedDate)
        }
        if (hour != -1) {
            // Actualiza la hora (formato HH:MM)
            selectedTime = String.format("%02d:%02d", hour, minute)
            editTextTaskTime.setText(selectedTime)
        }
    }

    companion object {
        const val TASK_ID_KEY          = "task_id"
        const val TASK_NAME_KEY        = "task_name"
        const val TASK_DESCRIPTION_KEY = "task_description"
        const val TASK_STATUS_KEY      = "task_status"
        const val TASK_DATE_KEY        = "task_date"
        const val TASK_TIME_KEY        = "task_time"
        const val TASK_CATEGORY_KEY    = "task_category"
        const val TASK_ALARM_KEY       = "task_alarm"

        @JvmStatic
        fun newInstanceForEditing(taskId: String,
                                  taskName: String,
                                  taskDescription: String,
                                  taskStatus: String,
                                  taskDate: String,
                                  taskTime: String,
                                  taskCategory: String,
                                  requiresAlarm: Boolean): CrearTareaFragment {
            val fragment = CrearTareaFragment()
            val args = Bundle().apply {
                putString(TASK_ID_KEY, taskId)
                putString(TASK_NAME_KEY, taskName)
                putString(TASK_DESCRIPTION_KEY, taskDescription)
                putString(TASK_STATUS_KEY, taskStatus)
                putString(TASK_DATE_KEY, taskDate)
                putString(TASK_TIME_KEY, taskTime)
                putString(TASK_CATEGORY_KEY, taskCategory)
                putBoolean(TASK_ALARM_KEY, requiresAlarm)
            }
            fragment.arguments = args
            return fragment
        }

        // Método para crear una tarea.
        fun newInstanceForCreation(): CrearTareaFragment {
            return CrearTareaFragment()
        }
    }
}
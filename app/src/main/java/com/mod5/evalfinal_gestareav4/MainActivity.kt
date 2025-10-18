package com.mod5.evalfinal_gestareav4

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.mod5.evalfinal_gestareav4.data.Task
import com.mod5.evalfinal_gestareav4.ui.VerTareasFragment
import com.mod5.evalfinal_gestareav4.ui.CrearTareaFragment
import com.mod5.evalfinal_gestareav4.viewmodel.TaskViewModel

class MainActivity : AppCompatActivity() {
    private lateinit var mainContentLayout: LinearLayout
    // Crea el ViewMode
    private lateinit var taskViewModel: TaskViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inicializa el ViewModel
        taskViewModel = ViewModelProvider(this).get(TaskViewModel::class.java)

        // Carga el layout del splash
        setContentView(R.layout.main_splash)

        val buttonStartApp: Button = findViewById(R.id.buttonStartApp)
        buttonStartApp.setOnClickListener {
            setupMainLayout()
        }
    }

    private fun setupMainLayout() {
        setContentView(R.layout.main)

        mainContentLayout            = findViewById(R.id.mainContentLayout)
        mainContentLayout.visibility = View.VISIBLE

        val bottomNavigationView: BottomNavigationView = findViewById(R.id.bottom_navigation)

        // Inicialmente, carga el fragmento de ver tareas
        loadFragment(VerTareasFragment())
        bottomNavigationView.selectedItemId = R.id.nav_view_tasks

        // Listener para la barra de navegación inferior
        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_view_tasks -> {
                    loadFragment(VerTareasFragment())
                    true
                }
                R.id.nav_add_task -> {
                    loadFragment(CrearTareaFragment.newInstanceForCreation())
                    true
                }
                else -> false
            }
        }

        // Observador del ViewModel
        taskViewModel.statusMessage.observe(this, { message ->
            if (!message.isNullOrBlank()) {
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
                // Notifica al ViewModel que ya consumió el mensaje.
                taskViewModel.clearStatusMessage()
            }
        })
    }

    // Método para editar una tarea
    fun startTaskEdit(task: Task) {
        val fragment = CrearTareaFragment.newInstanceForEditing(
            taskId          = task.id,
            taskName        = task.name,
            taskDescription = task.description,
            taskStatus      = task.status,
            taskDate        = task.date,
            taskTime        = task.time,
            taskCategory    = task.category,
            requiresAlarm   = task.requiresAlarm
        )
        loadFragment(fragment)
    }

    // NUEVO: Método para navegar a la lista de tareas (usado por CrearTareaFragment después de guardar)
    fun navigateToTaskList() {
        // Carga de nuevo el fragmento de lista para forzar el refresco de datos.
        loadFragment(VerTareasFragment())
        // Actualiza la selección visual en la barra de navegación inferior.
        val bottomNavigationView: BottomNavigationView = findViewById(R.id.bottom_navigation)
        bottomNavigationView.selectedItemId = R.id.nav_view_tasks
    }

    fun loadFragment(fragment: Fragment) {
        val fragmentManager    : FragmentManager     = supportFragmentManager
        val fragmentTransaction: FragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.navigationFragmentContainer, fragment)
        fragmentTransaction.commit()
    }
}
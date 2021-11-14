package com.example.doit

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.core.view.GravityCompat
import androidx.databinding.DataBindingUtil
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.findNavController
import androidx.navigation.ui.NavigationUI
import com.example.doit.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var mainBinding: ActivityMainBinding
    private lateinit var drawer: DrawerLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mainBinding = DataBindingUtil.setContentView(
            this, R.layout.activity_main
        )

        setSupportActionBar(mainBinding.myActionBar)

        val navController = findNavController(R.id.myNavHost)

        drawer = mainBinding.drawer
        val navView = mainBinding.navView

        NavigationUI.setupActionBarWithNavController(this, navController, drawer)
        NavigationUI.setupWithNavController(navView, navController)

        val toggle = ActionBarDrawerToggle(
            this, drawer, R.string.drawer_open, R.string.drawer_close
        )
        drawer.addDrawerListener(toggle)

        if (savedInstanceState != null) {
            with(navController) {
                val sd = savedInstanceState.getInt("KEY")
                if (sd != graph.startDestination
                    || currentDestination?.id == graph.startDestination
                ) {
                    graph = graph.apply { startDestination = sd }
                    toggle.syncState()
                }
            }
        }

        navController.addOnDestinationChangedListener { controller, destination, _ ->
            if (controller.graph.startDestination == destination.id)
                drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)
            else drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
        }

        navView.setNavigationItemSelectedListener { menuItem ->
            with(navController) {
                when (menuItem.itemId) {
                    R.id.todos -> {
                        if (currentDestination?.id != R.id.todoListFragment) {
                            graph = graph.apply {
                                startDestination = R.id.todoListFragment
                            }
                            toggle.syncState()
                        }
                        drawer.closeDrawer(GravityCompat.START)
                        true
                    }
                    R.id.finishedTodo -> {
                        if (currentDestination?.id != R.id.completedTodoListFragment) {
                            graph = graph.apply {
                                startDestination = R.id.completedTodoListFragment
                            }
                            toggle.syncState()
                        }
                        drawer.closeDrawer(GravityCompat.START)
                        true
                    }
                    else -> {
                        drawer.closeDrawer(GravityCompat.START)
                        false
                    }
                }
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        return NavigationUI.navigateUp(findNavController(R.id.myNavHost), drawer)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putInt("KEY", findNavController(R.id.myNavHost).graph.startDestination)
        super.onSaveInstanceState(outState)
    }
}
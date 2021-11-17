package com.example.doit

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.core.view.GravityCompat
import androidx.databinding.DataBindingUtil
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.NavOptions
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
                if (currentDestination?.id == graph.startDestination || sd != graph.startDestination
                ) {
                    graph.startDestination = sd
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
                        if (graph.startDestination != R.id.todoListFragment) {
                            val navOptions = NavOptions.Builder().setPopUpTo(
                                currentDestination!!.id, true
                            ).build()
                            navigate(
                                R.id.action_completedTodoListFragment_to_todoListFragment,
                                savedInstanceState, navOptions
                            )
                            toggle.syncState()
                            graph.startDestination = R.id.todoListFragment
                        }

                        drawer.closeDrawer(GravityCompat.START)
                        true
                    }
                    R.id.finishedTodo -> {
                        if (graph.startDestination != R.id.completedTodoListFragment) {
                            val navOptions = NavOptions.Builder().setPopUpTo(
                                currentDestination!!.id, true
                            ).build()
                            navigate(
                                R.id.action_todoListFragment_to_completedTodoListFragment,
                                savedInstanceState, navOptions
                            )
                            toggle.syncState()
                            graph.startDestination = R.id.completedTodoListFragment
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

    override fun onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) drawer.closeDrawer(GravityCompat.START)
        else super.onBackPressed()
    }
}
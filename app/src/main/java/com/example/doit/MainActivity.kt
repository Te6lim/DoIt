package com.example.doit

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.core.view.GravityCompat
import androidx.databinding.DataBindingUtil
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavOptions
import androidx.navigation.findNavController
import androidx.navigation.ui.NavigationUI
import com.example.doit.databinding.ActivityMainBinding
import com.google.android.material.navigation.NavigationView

class MainActivity : AppCompatActivity() {

    private lateinit var mainBinding: ActivityMainBinding
    private lateinit var drawer: DrawerLayout
        private set
    private lateinit var navView: NavigationView

    lateinit var mainViewModel: MainViewModel
        private set

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mainBinding = DataBindingUtil.setContentView(
            this, R.layout.activity_main
        )

        setSupportActionBar(mainBinding.myActionBar)

        val navController = findNavController(R.id.myNavHost)

        val viewModelFactory = MainViewModelFactory(navController.graph.startDestination)
        mainViewModel = ViewModelProvider(
            this, viewModelFactory
        )[MainViewModel::class.java]

        drawer = mainBinding.drawer
        navView = mainBinding.navView

        NavigationUI.setupActionBarWithNavController(this, navController, drawer)
        NavigationUI.setupWithNavController(navView, navController)

        val toggle = ActionBarDrawerToggle(
            this, drawer, R.string.drawer_open, R.string.drawer_close
        )

        drawer.addDrawerListener(toggle)

        drawer.addDrawerListener(object : DrawerLayout.DrawerListener {
            override fun onDrawerSlide(drawerView: View, slideOffset: Float) {

            }

            override fun onDrawerOpened(drawerView: View) {

            }

            override fun onDrawerClosed(drawerView: View) {

            }

            override fun onDrawerStateChanged(newState: Int) {
                navView.checkedItem?.let {
                    it.isChecked = false
                    it.isChecked = true
                }
            }

        })

        with(mainViewModel) {
            activeStartDestination.observe(this@MainActivity) {
                when (it) {
                    R.id.todoListFragment -> {
                        drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)
                        navView.setCheckedItem(R.id.todos_navView)
                        toggle.syncState()
                    }
                    R.id.completedTodoListFragment -> {
                        drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)
                        navView.setCheckedItem(R.id.finished_todos_navView)
                        toggle.syncState()
                    }
                    else -> {
                        drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
                    }
                }
            }

            contextActionbarActive.observe(this@MainActivity) { isActive ->
                if (isActive) drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
                else if (
                    navController.currentDestination!!.id == navController.graph.startDestination
                ) drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)
            }
        }

        if (savedInstanceState != null) {
            with(navController) {
                val sd = savedInstanceState.getInt("KEY")
                if (currentDestination?.id == graph.startDestination || sd != graph.startDestination
                ) {
                    graph.startDestination = sd
                    mainViewModel.setActiveStartDestination(sd)
                }
            }
        }

        navController.addOnDestinationChangedListener { _, destination, _ ->
            mainViewModel.setActiveStartDestination(destination.id)
        }

        navView.setNavigationItemSelectedListener { menuItem ->
            with(navController) {
                when (menuItem.itemId) {
                    R.id.todos_navView -> {
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

                    R.id.finished_todos_navView -> {
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
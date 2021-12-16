package com.example.doit

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.core.view.GravityCompat
import androidx.databinding.DataBindingUtil
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.navigation.ui.NavigationUI
import com.example.doit.databinding.ActivityMainBinding
import com.google.android.material.navigation.NavigationView

class MainActivity : AppCompatActivity() {

    private lateinit var mainBinding: ActivityMainBinding
    private lateinit var drawer: DrawerLayout
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
                    R.id.finishedTodoListFragment -> {
                        drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)
                        navView.setCheckedItem(R.id.finished_todos_navView)
                        toggle.syncState()
                    }

                    R.id.summaryFragment -> {
                        drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)
                        navView.setCheckedItem(R.id.summary_navView)
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
                        if (currentDestination!!.id != R.id.todoListFragment) {
                            graph = graph.apply {
                                startDestination = R.id.todoListFragment
                                supportActionBar?.subtitle = null
                            }
                            toggle.syncState()
                        }

                        drawer.closeDrawer(GravityCompat.START)
                        true
                    }

                    R.id.finished_todos_navView -> {
                        if (currentDestination!!.id != R.id.finishedTodoListFragment) {
                            graph = graph.apply {
                                startDestination = R.id.finishedTodoListFragment
                                supportActionBar?.subtitle = null
                            }
                            toggle.syncState()
                        }

                        drawer.closeDrawer(GravityCompat.START)
                        true
                    }

                    R.id.summary_navView -> {
                        if (currentDestination!!.id != R.id.summaryFragment) {
                            graph = graph.apply {
                                startDestination = R.id.summaryFragment
                                supportActionBar?.subtitle = null
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

    override fun onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) drawer.closeDrawer(GravityCompat.START)
        else super.onBackPressed()
    }
}
package com.asamm.locus.api.sample

import android.app.Activity
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.preference.PreferenceManager
import android.view.Gravity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.FrameLayout
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import com.asamm.locus.api.sample.pages.PagePointsFragment
import com.asamm.locus.api.sample.pages.PageTracksFragment
import com.asamm.locus.api.sample.pages.PageUtilsFragment
import com.asamm.locus.api.sample.pages.PageWelcomeFragment
import com.asamm.locus.api.sample.utils.BasicAdapter
import com.asamm.locus.api.sample.utils.BasicAdapterItem
import com.asamm.locus.api.sample.utils.Utils
import locus.api.utils.Logger
import java.io.File
import java.util.*

class MainActivity : AppCompatActivity() {

    // top toolbar
    private var mToolbar: ActionBar? = null
    // drawer itself
    private var mDrawerLayout: DrawerLayout? = null
    // toggle drawer
    private var mToggle: ActionBarDrawerToggle? = null
    // ID of currently selected item
    private var mCurrentSelectedItemId: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // set content
        setContentView(R.layout.activity_main)

        // setup toolbar
        if (!initializeToolbar()) {
            finish()
            return
        }

        // initialize drawer menu
        enableDrawer()

        // restore state
        mCurrentSelectedItemId = PreferenceManager.getDefaultSharedPreferences(this).getInt(KEY_I_SELECTED_ITEM_ID, ITEM_ID_WELCOME)
        setFragment(mCurrentSelectedItemId)

        // finally check intent that started this sample
        MainIntentHandler.handleStartIntent(this, intent)
    }

    /**
     * Initialize top toolbar.
     *
     * @return `true` if toolbar is correctly initialized
     */
    private fun initializeToolbar(): Boolean {
        setSupportActionBar(findViewById(R.id.toolbar_top))
        mToolbar = supportActionBar
        if (mToolbar == null) {
            Logger.logD(TAG, "initializeToolbar(), " + "problem with initializing of toolbar")
            return false
        }

        // set basics
        mToolbar!!.title = "Sample"
        mToolbar!!.displayOptions = ActionBar.DISPLAY_SHOW_HOME or
                ActionBar.DISPLAY_HOME_AS_UP or
                ActionBar.DISPLAY_SHOW_TITLE
        mToolbar!!.setDisplayHomeAsUpEnabled(true)
        mToolbar!!.setHomeButtonEnabled(true)

        // add went well
        return true
    }

    /**
     * Enable whole drawer system for current activity. This method should be called in end of
     * onCreate method to have drawer ready as soon as possible.
     */
    private fun enableDrawer() {
        // initialize drawer and drawer toggle button
        mDrawerLayout = findViewById(R.id.drawer_layout)
        val drawerContent = findViewById<FrameLayout>(R.id.frame_layout_drawer)

        // set a custom shadow that overlays the main content when the drawer opens
        mDrawerLayout!!.setDrawerShadow(R.drawable.drawer_shadow, Gravity.LEFT)

        // set change listener
        mDrawerLayout!!.addDrawerListener(object : DrawerLayout.DrawerListener {

            override fun onDrawerStateChanged(arg0: Int) {}

            override fun onDrawerSlide(arg0: View, arg1: Float) {}

            override fun onDrawerOpened(drawerView: View) {
                // refresh action bar
                invalidateOptionsMenu()
            }

            override fun onDrawerClosed(drawerView: View) {
                // refresh action bar
                invalidateOptionsMenu()
            }
        })

        // setup drawer icon
        mToggle = ActionBarDrawerToggle(this, mDrawerLayout,
                R.string.app_name, R.string.app_name)
        mDrawerLayout!!.addDrawerListener(mToggle!!)

        // refresh content
        setContentDrawer(drawerContent)
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mToggle!!.syncState()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        mToggle!!.onConfigurationChanged(newConfig)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.activity_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Pass the event to ActionBarDrawerToggle, if it returns
        // true, then it has handled the app icon touch event
        if (mToggle!!.onOptionsItemSelected(item)) {
            return true
        } else if (item.itemId == R.id.menu_settings) {
            Toast.makeText(this, "Not implemented", Toast.LENGTH_SHORT).show()
            return true
        }

        // Handle your other action bar items...
        return super.onOptionsItemSelected(item)
    }

    /**
     * Set content of drawer itself.
     */
    private fun setContentDrawer(drawerContainer: FrameLayout) {
        val items = ArrayList<BasicAdapterItem>()
        items.add(BasicAdapterItem(ITEM_ID_WELCOME,
                getTitleById(ITEM_ID_WELCOME)))
        items.add(BasicAdapterItem(ITEM_ID_POINTS,
                getTitleById(ITEM_ID_POINTS)))
        items.add(BasicAdapterItem(ITEM_ID_TRACKS,
                getTitleById(ITEM_ID_TRACKS)))
        items.add(BasicAdapterItem(ITEM_ID_UTILS,
                getTitleById(ITEM_ID_UTILS)))

        val lv = ListView(this)
        val adapter = BasicAdapter(this, items)
        lv.adapter = adapter
        lv.setOnItemClickListener { _, _, position, _ ->
            mCurrentSelectedItemId = items[position].id

            // refresh content
            setFragment(mCurrentSelectedItemId)
            mDrawerLayout!!.closeDrawers()

            // store value
            PreferenceManager.getDefaultSharedPreferences(this@MainActivity).edit().putInt(KEY_I_SELECTED_ITEM_ID, mCurrentSelectedItemId).apply()

        }
        drawerContainer.addView(lv)
    }

    /**
     * Display certain item.
     *
     * @param itemId ID of selected item
     */
    private fun setFragment(itemId: Int) {
        // update the main content by replacing fragments
        val fragmentManager = supportFragmentManager
        fragmentManager.beginTransaction().replace(FRAGMENT_CONTAINER_ID, getContentById(itemId)!!).commit()

        // set page title
        mToolbar?.title = getTitleById(itemId)

        // refresh toolbar
        invalidateOptionsMenu()
    }

    /**
     * Get new content for certain selected item.
     *
     * @param itemId selected item ID
     * @return prepared fragment to display
     */
    private fun getContentById(itemId: Int): Fragment? {
        return when (itemId) {
            ITEM_ID_WELCOME -> PageWelcomeFragment()
            ITEM_ID_POINTS -> PagePointsFragment()
            ITEM_ID_TRACKS -> PageTracksFragment()
            ITEM_ID_UTILS -> PageUtilsFragment()
            else -> null
        }
    }

    /**
     * Get title for certain page.
     *
     * @param itemId ID of item
     * @return title for certain page
     */
    private fun getTitleById(itemId: Int): CharSequence {
        return when (itemId) {
            ITEM_ID_WELCOME -> "Welcome page"
            ITEM_ID_POINTS -> "Points"
            ITEM_ID_TRACKS -> "Tracks"
            ITEM_ID_UTILS -> "Utils"
            else -> ""
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        Logger.logD(TAG, "onActivityResult($requestCode, $resultCode, $data)")
        if (requestCode == RC_A) {
            // pick file
            if (resultCode == Activity.RESULT_OK && data != null) {
                val file = File(data.data!!.path)
                Toast.makeText(this, "Process successful\n\nFile:" + file.absolutePath +
                        ", exists:" + file.exists(), Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(this, "Process unsuccessful", Toast.LENGTH_SHORT).show()
            }
        } else if (requestCode == RC_B) {
            // pick directory
            if (resultCode == Activity.RESULT_OK && data != null) {
                val dir = File(data.data!!.path)
                Toast.makeText(this, "Process successful\n\nDir:" + dir.name +
                        ", exists:" + dir.exists(), Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(this, "Process unsuccessful", Toast.LENGTH_SHORT).show()
            }
        } else if (requestCode == RC_GET_TRACK_IN_FORMAT) {
            if (resultCode == Activity.RESULT_OK && data != null && data.data != null) {
                try {
                    val targetFile = createTempFile(System.currentTimeMillis().toString(), "gpx")
                    Utils.copy(this, data.data!!, targetFile)
                    Toast.makeText(this, "Process successful\n\nDir:" + targetFile.name +
                            ", exists:" + targetFile.exists(), Toast.LENGTH_LONG).show()
                } catch (e: Exception) {
                    Logger.logE(TAG, "onActivityResult($requestCode, $resultCode, $data)", e)
                }
            } else {
                Toast.makeText(this, "Process unsuccessful", Toast.LENGTH_SHORT).show()
            }
        }
    }

    companion object {

        // tag for logger
        private const val TAG = "MainActivity"

        const val ITEM_ID_WELCOME = 0
        const val ITEM_ID_POINTS = 1
        const val ITEM_ID_TRACKS = 2
        const val ITEM_ID_UTILS = 3

        // ID of fragment container
        private const val FRAGMENT_CONTAINER_ID = R.id.frame_layout_main_container

        // remember the position of the selected item.
        private const val KEY_I_SELECTED_ITEM_ID = "KEY_I_SELECTED_ITEM_ID"

        // request IDs
        const val RC_A = 0
        const val RC_B = 1
        const val RC_GET_TRACK_IN_FORMAT = 3
    }
}

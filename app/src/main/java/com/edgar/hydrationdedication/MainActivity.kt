package com.edgar.hydrationdedication

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var acct: GoogleSignInAccount

    private var userWeight = User().weight
    private var userWorkout = User().workout
    private var isUserActive = User().active
    private var isUserMale = User().male
    private var isItHot = User().heat

    private var userCalc = User().calculation(userWeight, userWorkout, isUserActive, isUserMale, isItHot).toInt()
    private var waterGoal = userCalc
    private var waterDrinkDisplay = 0

    private val database = FirebaseDatabase.getInstance()
    private var myRef = database.getReference("Hydration Dedication")

    override fun onResume() {
        super.onResume()
        title = "Welcome Back ${acct.givenName}"
        userCalc = User().calculation(userWeight, userWorkout, isUserActive, isUserMale, isItHot).toInt()
        waterGoal = userCalc
        savePreferences()
        updateProgressbarUI()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        auth = FirebaseAuth.getInstance()
        acct = GoogleSignIn.getLastSignedInAccount(this)!!

        loadPreferences()

        val intent = intent
        userWeight = intent.getIntExtra("newWeight", userWeight)
        userWorkout = intent.getIntExtra("newWorkout", userWorkout)
        isUserMale = intent.getBooleanExtra("newGender", isUserMale)

        savePreferences()

        if (savedInstanceState != null) {
            waterDrinkDisplay = savedInstanceState.getInt("val")
        }

        button33cl.setOnClickListener {
            waterDrinkDisplay += 33
            title = "Good Job ${acct.givenName}"
            savePreferences()
            updateProgressbarUI()
        }
        button50cl.setOnClickListener {
            waterDrinkDisplay += 50
            title = "Amazing Job ${acct.givenName}"
            savePreferences()
            updateProgressbarUI()
        }
        button100cl.setOnClickListener {
            waterDrinkDisplay += 100
            title = "Incredible Job ${acct.givenName}"
            savePreferences()
            updateProgressbarUI()
        }

        // Workout button UX
        if (!isUserActive) {
            buttonWorkout.background.alpha = 128
        } else {
            buttonWorkout.background.alpha = 255
        }

        buttonWorkout.setOnClickListener {
            isUserActive = !isUserActive
            if(isUserActive){
                buttonWorkout.background.alpha = 255
                updateProgressbarUI()
            } else {
                buttonWorkout.background.alpha = 128
                updateProgressbarUI()
            }
            userCalc = User().calculation(userWeight, userWorkout, isUserActive, isUserMale, isItHot).toInt()
            waterGoal = userCalc

            savePreferences()
            updateProgressbarUI()
        }

        // Heat button UX
        if (!isItHot) {
            buttonHeat.background.alpha = 128
        } else {
            buttonHeat.background.alpha = 255
        }

        buttonHeat.setOnClickListener {
            isItHot = !isItHot
            if (isItHot) {
                buttonHeat.background.alpha = 255
                updateProgressbarUI()
            } else {
                buttonHeat.background.alpha = 128
                updateProgressbarUI()
            }

            userCalc = User().calculation(userWeight, userWorkout, isUserActive, isUserMale, isItHot).toInt()
            waterGoal = userCalc

            savePreferences()
            updateProgressbarUI()
        }
        updateProgressbarUI()


    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.options_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.mnuSave -> resetApp()
            R.id.mnuConfig -> userSettings()
            R.id.mnuLogOut -> userCheck()
            else -> return super.onOptionsItemSelected(item)
        }
        return super.onOptionsItemSelected(item)
    }

    @SuppressLint("SetTextI18n")
    private fun updateProgressbarUI() {
        progressBar.max = waterGoal
        progressBar.progress = (((waterDrinkDisplay + waterGoal) - waterGoal))
        goal_text.text = "$waterDrinkDisplay / $waterGoal cl"
        writeNewValue(waterDrinkDisplay, waterGoal)

        progressBarCustom1.progress = 70
        progressBarCustom2.progress = 30
        progressBarCustom3.progress = 100
        progressBarCustom4.progress = 80
        progressBarCustom5.progress = 100
        progressBarCustom6.progress = 20
        progressBarCustom7.progress = 50
    }

    class StoreValues(
        var water: Int = 0,
        var goal: Int = 0
    )

    private fun writeNewValue(water: Int, goal: Int) {
        val userId = acct.id!!
        val newValue = StoreValues(water, goal)
        myRef.child(userId).child("mainEntry").setValue(newValue)
    }

    private fun newDay(water: Int, goal: Int){
        val userId = acct.id!!
        val oldWater = StoreValues(water, goal)
        myRef.child(userId).child("oldEntries").push().setValue(oldWater)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        outState.putInt("val", waterDrinkDisplay)
        outState.putInt("userWeight", userWeight)
        outState.putInt("userWorkout", userWorkout)
        outState.putBoolean("isUserActive", isUserActive)
        outState.putBoolean("isUserMale", isUserMale)
        outState.putBoolean("isItHot", isItHot)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle?) {
        super.onRestoreInstanceState(savedInstanceState)

        if (savedInstanceState != null) {
            waterDrinkDisplay = savedInstanceState.getInt("val")
            userWeight = savedInstanceState.getInt("userWeight")
            userWorkout = savedInstanceState.getInt("userWorkout")
            isUserActive = savedInstanceState.getBoolean("isUserActive")
            isUserMale = savedInstanceState.getBoolean("isUserMale")
            isItHot = savedInstanceState.getBoolean("isItHot")
        }
    }

    private fun savePreferences() {
        val sharedPreferences =
            getPreferences(Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putInt("val", waterDrinkDisplay)
        editor.putInt("userWeight", userWeight)
        editor.putInt("userWorkout", userWorkout)
        editor.putBoolean("isUserActive", isUserActive)
        editor.putBoolean("isUserMale", isUserMale)
        editor.putBoolean("isItHot", isItHot)
        editor.apply()
    }

    private fun loadPreferences() {
        val sharedPreferences =
            getPreferences(Context.MODE_PRIVATE)
        val state = sharedPreferences.getInt("val", waterDrinkDisplay)
        val getUserWeight = sharedPreferences.getInt("userWeight", userWeight)
        val getUserWorkout = sharedPreferences.getInt("userWorkout", userWorkout)
        val getUserActivity = sharedPreferences.getBoolean("isUserActive", isUserActive)
        val getUserGender = sharedPreferences.getBoolean("isUserMale", isUserMale)
        val getHeatInfo = sharedPreferences.getBoolean("isItHot", isItHot)

        waterDrinkDisplay = state
        userWeight = getUserWeight
        userWorkout = getUserWorkout
        isUserActive = getUserActivity
        isUserMale = getUserGender
        isItHot = getHeatInfo
    }

    private fun resetApp() {
        newDay(waterDrinkDisplay, waterGoal)
        waterGoal = userCalc
        waterDrinkDisplay = 0
        updateProgressbarUI()
    }

    override fun onBackPressed() {
        savePreferences()
        loadPreferences()
//        super.onBackPressed()
    }

    private fun userCheck() {
        savePreferences()
        auth.signOut()
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
    }

    private fun userSettings() {
        savePreferences()
        val intent = Intent(this, SettingsActivity::class.java)
        intent.putExtra("oldWeight", userWeight)
        intent.putExtra("oldWorkout", userWorkout)
        intent.putExtra("oldGender", isUserMale)
        startActivity(intent)
    }

}

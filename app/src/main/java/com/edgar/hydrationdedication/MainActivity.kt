package com.edgar.hydrationdedication

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.LightingColorFilter
import android.graphics.PorterDuff
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.drawable.DrawableCompat
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.activity_main.*
import kotlin.collections.ArrayList

class MainActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var acct: GoogleSignInAccount

    private var userWeight = User().weight
    private var userWorkout = User().workout
    private var isUserActive = User().active
    private var isUserMale = User().male
    private var isItHot = User().heat

    private var userCalc =
        User().calculation(userWeight, userWorkout, isUserActive, isUserMale, isItHot).toInt()
    private var waterGoal = userCalc
    private var waterDrinkDisplay = 0

    private val database = FirebaseDatabase.getInstance()
    private var myRef = database.getReference("Hydration Dedication")

    override fun onResume() {
        super.onResume()
        title = "Welcome Back ${acct.givenName}"
        userCalc =
            User().calculation(userWeight, userWorkout, isUserActive, isUserMale, isItHot).toInt()
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

            title = if (waterGoal < waterDrinkDisplay) {
                "Ok ${acct.givenName}, That's enough for today!"
            } else {
                "Good Job ${acct.givenName}"
            }

            savePreferences()
            updateProgressbarUI()
        }
        button50cl.setOnClickListener {
            waterDrinkDisplay += 50

            title = if (waterGoal < waterDrinkDisplay) {
                "Ok ${acct.givenName}, That's enough for today!"
            } else {
                "Amazing Job ${acct.givenName}"
            }

            savePreferences()
            updateProgressbarUI()
        }
        button100cl.setOnClickListener {
            waterDrinkDisplay += 100

            title = if (waterGoal < waterDrinkDisplay) {
                "Ok ${acct.givenName}, That's enough for today!"
            } else {
                "Incredible Job ${acct.givenName}"
            }

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
            if (isUserActive) {
                buttonWorkout.background.alpha = 255
                updateProgressbarUI()
            } else {
                buttonWorkout.background.alpha = 128
                updateProgressbarUI()
            }
            userCalc =
                User().calculation(userWeight, userWorkout, isUserActive, isUserMale, isItHot)
                    .toInt()
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

            userCalc =
                User().calculation(userWeight, userWorkout, isUserActive, isUserMale, isItHot)
                    .toInt()
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

        val userId = acct.id!!
        val newRef = myRef.child(userId).child("oldEntries")

        newRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val posts = ArrayList<StoreValues>()

                for (snapshot in dataSnapshot.children) {
                    val post = snapshot.getValue(StoreValues::class.java)
                    posts.add(post!!)
                }
                //posts.reverse()

                if (posts.isNotEmpty()) {
                    val dayOneWater = posts[0].water
                    val dayOneGoal = posts[0].goal
                    progressBarCustom1.max = dayOneGoal
                    progressBarCustom1.progress = dayOneWater
                } else {
                    progressBarCustom1.progress = 0
                }

                if (posts.size > 1) {
                    val dayTwoWater = posts[1].water
                    val dayTwoGoal = posts[1].goal
                    progressBarCustom2.max = dayTwoGoal
                    progressBarCustom2.progress = dayTwoWater
                } else {
                    progressBarCustom2.progress = 0
                }

                if (posts.size > 2) {
                    val dayThreeWater = posts[2].water
                    val dayThreeGoal = posts[2].goal
                    progressBarCustom3.max = dayThreeGoal
                    progressBarCustom3.progress = dayThreeWater
                } else {
                    progressBarCustom3.progress = 0
                }

                if (posts.size > 3) {
                    val dayFourWater = posts[3].water
                    val dayFourGoal = posts[3].goal
                    progressBarCustom4.max = dayFourGoal
                    progressBarCustom4.progress = dayFourWater
                } else {
                    progressBarCustom4.progress = 0
                }

                if (posts.size > 4) {
                    val dayFiveWater = posts[4].water
                    val dayFiveGoal = posts[4].goal
                    progressBarCustom5.max = dayFiveGoal
                    progressBarCustom5.progress = dayFiveWater
                } else {
                    progressBarCustom5.progress = 0
                }

                if (posts.size > 5) {
                    val daySixWater = posts[5].water
                    val daySixGoal = posts[5].goal
                    progressBarCustom6.max = daySixGoal
                    progressBarCustom6.progress = daySixWater
                } else {
                    progressBarCustom6.progress = 0
                }

                if (posts.size > 6) {
                    val daySevenWater = posts[6].water
                    val daySevenGoal = posts[6].goal
                    progressBarCustom7.max = daySevenGoal
                    progressBarCustom7.progress = daySevenWater
                } else {
                    progressBarCustom7.progress = 0
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                println("The read failed: " + databaseError.code)
            }
        })
    }

    data class StoreValues(
        var water: Int = 0,
        var goal: Int = 0
    )

    private fun writeNewValue(water: Int, goal: Int) {
        val userId = acct.id!!
        val newValue = StoreValues(water, goal)
        myRef.child(userId).child("mainEntry").setValue(newValue)
    }

    private fun newDay(water: Int, goal: Int) {
        val userId = acct.id!!
        val oldVal = StoreValues(water, goal)
        val baseRef = myRef.child(userId).child("oldEntries")

        val one = baseRef.child("1")
        val two = baseRef.child("2")
        val three = baseRef.child("3")
        val four = baseRef.child("4")
        val five = baseRef.child("5")
        val six = baseRef.child("6")
        val seven = baseRef.child("7")

        // A hack to only hold 7 values inside the Realtime Database by moving the values from previous day forward once and deleting the last value.
        baseRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {}
            override fun onDataChange(p0: DataSnapshot) {
                when {
                    p0.hasChild("1") -> {
                        if (p0.hasChild("2")) {
                            if (p0.hasChild("3")) {
                                if (p0.hasChild("4")) {
                                    if (p0.hasChild("5")) {
                                        if (p0.hasChild("6")) {
                                            if (p0.hasChild("7")) {

                                                val sixVal = p0.child("6").value
                                                val fiveVal = p0.child("5").value
                                                val fourVal = p0.child("4").value
                                                val threeVal = p0.child("3").value
                                                val twoVal = p0.child("2").value
                                                val oneVal = p0.child("1").value

                                                seven.removeValue()
                                                seven.setValue(sixVal)
                                                six.removeValue()
                                                six.setValue(fiveVal)
                                                five.removeValue()
                                                five.setValue(fourVal)
                                                four.removeValue()
                                                four.setValue(threeVal)
                                                three.removeValue()
                                                three.setValue(twoVal)
                                                two.removeValue()
                                                two.setValue(oneVal)
                                                one.removeValue()
                                                one.setValue(oldVal)
                                            } else {
                                                seven.setValue(oldVal)
                                            }
                                        } else {
                                            six.setValue(oldVal)
                                        }
                                    } else {
                                        five.setValue(oldVal)
                                    }
                                } else {
                                    four.setValue(oldVal)
                                }
                            } else {
                                three.setValue(oldVal)
                            }
                        } else {
                            two.setValue(oldVal)
                        }
                    }
                    else -> {
                        one.setValue(oldVal)
                    }
                }
            }
        })

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
        title = "Welcome Back ${acct.givenName}"
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

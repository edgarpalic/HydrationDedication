package com.edgar.hydrationdedication

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import kotlinx.android.synthetic.main.activity_settings.*

class SettingsActivity : Activity() {

    private var userWeight = User().weight
    private var userWorkout = User().workout
    private var isUserMale = User().male

    override fun onStart() {
        super.onStart()
        weightText.setText(userWeight.toString())
        trainingText.setText(userWorkout.toString())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        // Info from Main Activity
        val intent = intent
        userWeight = intent.getIntExtra("oldWeight", userWeight)
        userWorkout = intent.getIntExtra("oldWorkout", userWorkout)
        isUserMale = intent.getBooleanExtra("oldGender", isUserMale)

        // Popup window start
        val dm = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(dm)
        val width = dm.widthPixels
        val height = dm.heightPixels

        window.setLayout((width*.8).toInt(), (height*.8).toInt())
        this.window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT));
        // Popup window end

        // Gender switch start
        if (!isUserMale){
            genderSwitch.isChecked = true
            genderText.text = "Female"
        }
        if (isUserMale){
            genderSwitch.isChecked = false
            genderText.text = "Male"
        }

        genderSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked){
                isUserMale = false
                genderText.text = "Female"
            } else {
                isUserMale = true
                genderText.text = "Male"
            }
        }
        // Gender switch end

        saveButton.setOnClickListener {
            var inputWeight = weightText.text
            var inputTraining = trainingText.text
            userWeight = inputWeight.toString().toInt()
            userWorkout = inputTraining.toString().toInt()

            saveSettings()
        }
    }

    private fun saveSettings() {
        val intent = Intent(this, MainActivity::class.java)
        intent.putExtra("newWeight", userWeight)
        intent.putExtra("newWorkout", userWorkout)
        intent.putExtra("newGender", isUserMale)
        startActivity(intent)
    }
}

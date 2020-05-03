package com.edgar.hydrationdedication

class User {
    var weight = 70
    var workout = 30
    var active = true
    var male = true
    var heat = false

    fun calculation(weight: Int, workout: Int, active: Boolean, male: Boolean, heat: Boolean): Double {
        val pound = weight * 2.205 //Kg to Pound
        val ounce = pound * 0.667 //Pounds * 2/3 (water calculation)
        val liter = ounce / 33.814 //Ounce to Liter
        var centiLiter = liter * 100 //Liter to Centiliter

        val workoutCal =
            (workout / 30) * 35 // Working out increases water demand by 35cl every 30min

        if (!male) {
            centiLiter -= 100
        }
        if (heat) {
            centiLiter += 100
        }
        return if(active){
            centiLiter + workoutCal
        } else {
            centiLiter
        }

    }
}
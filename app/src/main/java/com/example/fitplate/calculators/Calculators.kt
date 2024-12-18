package com.example.fitplate.calculators

class Calculators {
    companion object {
        /**
         * BMI Calculator
         * @param weight Weight in kilograms
         * @param height Height in centimeters
         * @return BMI value formatted to 1 decimal place, or null for invalid inputs
         */
        fun calculateBMI(weight: Double?, height: Double?): Double? {
            if (weight!! <= 0 || height!! <= 0) return null
            val heightInMeters = height / 100
            val bmi = weight / (heightInMeters * heightInMeters)
            return bmi
        }

        /**
         * BMR Calculator (Mifflin-St Jeor Equation)
         * @param gender "male" or "female"
         * @param weight Weight in kilograms
         * @param height Height in centimeters
         * @param age Age in years
         * @return BMR value as Double
         */
        fun calculateBMR(gender: String, weight: Double, height: Double, age: Int): Double {
            val baseBMR = (10 * weight) + (6.25 * height) - (5 * age)
            return if (gender.lowercase() == "pria") baseBMR + 5 else baseBMR - 161
        }

        /**
         * TDEE Calculator
         * @param bmr Basal Metabolic Rate
         * @param activityLevel Activity factor (1.2 to 1.9)
         * @return TDEE value as Double
         */
        fun calculateTDEE(bmr: Double, activityLevel: String?): Double {
            return when (activityLevel?.lowercase()) {
                "tidak pernah" -> bmr * 1.2
                "1-2 hari/minggu" -> bmr * 1.375
                "3-5 hari/minggu" -> bmr * 1.55
                "6-7 hari/minggu" -> bmr * 1.725
                else -> bmr * 1.9
            }
        }

        /**
         * Adjusted TDEE Calculator
         * @param tdee Basal Metabolic Rate
         * @param dietGoal "weight_loss", "weight_gain", or "maintenance"
         * @return Adjusted TDEE value as Double
         */
        fun calculateAdjustedTDEE(tdee: Double, dietGoal: String?, level: String): Double {
            val adjustment = when (level.lowercase()) {
                "beginner" -> 300
                "intermediate" -> 500
                "advanced" -> 700
                else -> 0 // Default adjustment for unknown levels
            }

            return when (dietGoal?.lowercase()) {
                "turun berat badan" -> tdee - adjustment  // Calorie deficit based on level
                "tambah berat badan" -> tdee + adjustment  // Calorie surplus based on level
                else -> tdee  // Maintenance
            }
        }


        /**
         * Water Intake Calculator
         * @param weight Weight in kilograms
         * @param exerciseMinutes Daily exercise in minutes
         * @return Recommended water intake in milliliters
         */
        fun calculateWaterIntake(weight: Double?, exerciseMinutes: Int): Double? {
            val baseWater = weight?.times(35) // 35 ml per kg
            val exerciseWater = (exerciseMinutes / 30) * 500 // 500 ml for every 30 mins
            return (baseWater?.plus(exerciseWater))
        }

        /**
         * Macronutrient Calculator
         * @param tdee Total Daily Energy Expenditure
         * @param goal "weight_loss", "weight_gain", or "maintenance"
         * @return Macronutrient breakdown as a Map
         */
        fun calculateMacronutrients(tdee: Double, goal: String?): Map<String, Double> {
            val proteinPercent: Double
            val fatPercent: Double
            val carbPercent: Double

            when (goal?.lowercase()) {
                "turun berat badan" -> {
                    proteinPercent = 0.35; fatPercent = 0.25; carbPercent = 0.4
                }
                "tambah berat badan" -> {
                    proteinPercent = 0.25; fatPercent = 0.3; carbPercent = 0.45
                }
                else -> {
                    proteinPercent = 0.3; fatPercent = 0.25; carbPercent = 0.45
                }
            }

            return mapOf(
                "protein" to (tdee * proteinPercent / 4), // 4 kcal/g
                "fat" to (tdee * fatPercent / 9),        // 9 kcal/g
                "carbs" to (tdee * carbPercent / 4)      // 4 kcal/g
            )
        }
    }
}

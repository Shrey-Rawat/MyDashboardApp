package com.mydashboardapp.data.sample

import com.mydashboardapp.data.entities.Exercise

object SampleTrainingData {
    
    val sampleExercises = listOf(
        // Strength Training - Chest
        Exercise(
            id = 1,
            name = "Bench Press",
            category = "Strength",
            muscleGroup = "Chest",
            equipment = "Barbell",
            description = "The bench press is a compound exercise that primarily targets the chest muscles.",
            instructions = "Lie on a bench with feet flat on the floor. Grip the barbell slightly wider than shoulder-width. Lower the bar to your chest, then press it back up.",
            difficulty = "Intermediate",
            caloriesPerMinute = 6.0,
            isCustom = false
        ),
        Exercise(
            id = 2,
            name = "Push-ups",
            category = "Strength",
            muscleGroup = "Chest",
            equipment = "Bodyweight",
            description = "A classic bodyweight exercise that targets chest, shoulders, and triceps.",
            instructions = "Start in a plank position with hands slightly wider than shoulders. Lower your body until your chest nearly touches the floor, then push back up.",
            difficulty = "Beginner",
            caloriesPerMinute = 8.0,
            isCustom = false
        ),
        Exercise(
            id = 3,
            name = "Dumbbell Flyes",
            category = "Strength",
            muscleGroup = "Chest",
            equipment = "Dumbbells",
            description = "An isolation exercise that targets the chest muscles with a wide arc motion.",
            instructions = "Lie on a bench holding dumbbells above your chest. Lower weights in an arc motion, then bring them back together.",
            difficulty = "Intermediate",
            caloriesPerMinute = 5.0,
            isCustom = false
        ),
        
        // Strength Training - Back
        Exercise(
            id = 4,
            name = "Deadlift",
            category = "Strength",
            muscleGroup = "Back",
            equipment = "Barbell",
            description = "A compound exercise that targets the entire posterior chain.",
            instructions = "Stand with feet hip-width apart, grip the barbell with hands just outside your legs. Lift by extending hips and knees simultaneously.",
            difficulty = "Advanced",
            caloriesPerMinute = 8.0,
            isCustom = false
        ),
        Exercise(
            id = 5,
            name = "Pull-ups",
            category = "Strength",
            muscleGroup = "Back",
            equipment = "Pull-up Bar",
            description = "A bodyweight exercise that primarily targets the latissimus dorsi.",
            instructions = "Hang from a pull-up bar with palms facing away. Pull your body up until your chin clears the bar, then lower with control.",
            difficulty = "Advanced",
            caloriesPerMinute = 10.0,
            isCustom = false
        ),
        Exercise(
            id = 6,
            name = "Bent-over Row",
            category = "Strength",
            muscleGroup = "Back",
            equipment = "Barbell",
            description = "A compound pulling exercise that targets the middle and upper back.",
            instructions = "Hinge at the hips with a slight knee bend. Pull the barbell to your lower chest, squeezing shoulder blades together.",
            difficulty = "Intermediate",
            caloriesPerMinute = 6.0,
            isCustom = false
        ),
        
        // Strength Training - Legs
        Exercise(
            id = 7,
            name = "Squats",
            category = "Strength",
            muscleGroup = "Legs",
            equipment = "Barbell",
            description = "A fundamental compound movement that targets the quadriceps, glutes, and hamstrings.",
            instructions = "Stand with feet shoulder-width apart. Lower your body by bending at the hips and knees until thighs are parallel to the floor.",
            difficulty = "Beginner",
            caloriesPerMinute = 8.0,
            isCustom = false
        ),
        Exercise(
            id = 8,
            name = "Lunges",
            category = "Strength",
            muscleGroup = "Legs",
            equipment = "Bodyweight",
            description = "A unilateral exercise that targets the quadriceps, glutes, and hamstrings.",
            instructions = "Step forward into a lunge position, lowering until both knees are at 90 degrees. Push back to starting position.",
            difficulty = "Beginner",
            caloriesPerMinute = 7.0,
            isCustom = false
        ),
        Exercise(
            id = 9,
            name = "Leg Press",
            category = "Strength",
            muscleGroup = "Legs",
            equipment = "Machine",
            description = "A machine-based exercise that safely targets the leg muscles.",
            instructions = "Sit on the leg press machine with feet on the platform. Lower the weight by bending your knees, then press back up.",
            difficulty = "Beginner",
            caloriesPerMinute = 5.0,
            isCustom = false
        ),
        
        // Strength Training - Arms
        Exercise(
            id = 10,
            name = "Bicep Curls",
            category = "Strength",
            muscleGroup = "Arms",
            equipment = "Dumbbells",
            description = "An isolation exercise that targets the biceps.",
            instructions = "Hold dumbbells at your sides with palms facing forward. Curl the weights up by contracting your biceps.",
            difficulty = "Beginner",
            caloriesPerMinute = 4.0,
            isCustom = false
        ),
        Exercise(
            id = 11,
            name = "Tricep Dips",
            category = "Strength",
            muscleGroup = "Arms",
            equipment = "Bodyweight",
            description = "A bodyweight exercise that targets the triceps.",
            instructions = "Position hands on a bench or chair behind you. Lower your body by bending your elbows, then push back up.",
            difficulty = "Intermediate",
            caloriesPerMinute = 6.0,
            isCustom = false
        ),
        
        // Strength Training - Shoulders
        Exercise(
            id = 12,
            name = "Overhead Press",
            category = "Strength",
            muscleGroup = "Shoulders",
            equipment = "Barbell",
            description = "A compound movement that targets the shoulders and triceps.",
            instructions = "Hold the barbell at shoulder height. Press the weight overhead until arms are fully extended, then lower with control.",
            difficulty = "Intermediate",
            caloriesPerMinute = 6.0,
            isCustom = false
        ),
        Exercise(
            id = 13,
            name = "Lateral Raises",
            category = "Strength",
            muscleGroup = "Shoulders",
            equipment = "Dumbbells",
            description = "An isolation exercise that targets the side deltoids.",
            instructions = "Hold dumbbells at your sides. Raise them out to the sides until arms are parallel to the floor, then lower slowly.",
            difficulty = "Beginner",
            caloriesPerMinute = 4.0,
            isCustom = false
        ),
        
        // Cardio
        Exercise(
            id = 14,
            name = "Running",
            category = "Cardio",
            muscleGroup = "Full Body",
            equipment = "None",
            description = "A high-impact cardiovascular exercise.",
            instructions = "Maintain a steady pace with proper running form. Land on the balls of your feet and maintain an upright posture.",
            difficulty = "Beginner",
            caloriesPerMinute = 12.0,
            isCustom = false
        ),
        Exercise(
            id = 15,
            name = "Cycling",
            category = "Cardio",
            muscleGroup = "Legs",
            equipment = "Bicycle",
            description = "A low-impact cardiovascular exercise that's easy on the joints.",
            instructions = "Maintain a steady cadence and adjust resistance as needed. Keep a slight bend in your knees.",
            difficulty = "Beginner",
            caloriesPerMinute = 10.0,
            isCustom = false
        ),
        Exercise(
            id = 16,
            name = "Jump Rope",
            category = "Cardio",
            muscleGroup = "Full Body",
            equipment = "Jump Rope",
            description = "A high-intensity cardio exercise that improves coordination.",
            instructions = "Keep your elbows close to your sides and jump on the balls of your feet. Maintain a steady rhythm.",
            difficulty = "Intermediate",
            caloriesPerMinute = 15.0,
            isCustom = false
        ),
        
        // Flexibility
        Exercise(
            id = 17,
            name = "Yoga Flow",
            category = "Flexibility",
            muscleGroup = "Full Body",
            equipment = "Yoga Mat",
            description = "A sequence of yoga poses that improve flexibility and balance.",
            instructions = "Follow a sequence of poses, focusing on breathing and smooth transitions between movements.",
            difficulty = "Beginner",
            caloriesPerMinute = 3.0,
            isCustom = false
        ),
        Exercise(
            id = 18,
            name = "Static Stretching",
            category = "Flexibility",
            muscleGroup = "Full Body",
            equipment = "None",
            description = "Holding stretches to improve muscle flexibility and range of motion.",
            instructions = "Hold each stretch for 15-30 seconds without bouncing. Focus on relaxing into the stretch.",
            difficulty = "Beginner",
            caloriesPerMinute = 2.0,
            isCustom = false
        ),
        
        // Core
        Exercise(
            id = 19,
            name = "Plank",
            category = "Strength",
            muscleGroup = "Core",
            equipment = "Bodyweight",
            description = "An isometric exercise that strengthens the entire core.",
            instructions = "Hold a push-up position with forearms on the ground. Keep your body in a straight line from head to heels.",
            difficulty = "Beginner",
            caloriesPerMinute = 5.0,
            isCustom = false
        ),
        Exercise(
            id = 20,
            name = "Crunches",
            category = "Strength",
            muscleGroup = "Core",
            equipment = "Bodyweight",
            description = "An isolation exercise that targets the abdominal muscles.",
            instructions = "Lie on your back with knees bent. Lift your shoulders off the ground by contracting your abs, then lower slowly.",
            difficulty = "Beginner",
            caloriesPerMinute = 4.0,
            isCustom = false
        )
    )
}

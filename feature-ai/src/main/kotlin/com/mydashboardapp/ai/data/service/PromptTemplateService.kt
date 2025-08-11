package com.mydashboardapp.ai.data.service

import android.content.Context
import com.mydashboardapp.ai.data.models.PromptTemplate
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Service for managing prompt templates stored in local JSON
 */
@Singleton
class PromptTemplateService @Inject constructor(
    @ApplicationContext private val context: Context
) {
    
    private val json = Json { ignoreUnknownKeys = true }
    private val fileName = "prompt_templates.json"
    
    suspend fun loadBuiltInTemplates(): List<PromptTemplate> = withContext(Dispatchers.IO) {
        try {
            val jsonString = context.assets.open("ai/$fileName").bufferedReader().use { it.readText() }
            json.decodeFromString<TemplateCollection>(jsonString).templates
        } catch (e: IOException) {
            getDefaultTemplates()
        }
    }
    
    suspend fun saveUserTemplates(templates: List<PromptTemplate>) = withContext(Dispatchers.IO) {
        val file = context.getFileStreamPath("user_$fileName")
        val collection = TemplateCollection(templates)
        file.writeText(json.encodeToString(collection))
    }
    
    suspend fun loadUserTemplates(): List<PromptTemplate> = withContext(Dispatchers.IO) {
        try {
            val file = context.getFileStreamPath("user_$fileName")
            if (file.exists()) {
                val jsonString = file.readText()
                json.decodeFromString<TemplateCollection>(jsonString).templates
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    suspend fun getAllTemplates(): List<PromptTemplate> = withContext(Dispatchers.IO) {
        loadBuiltInTemplates() + loadUserTemplates()
    }
    
    suspend fun getTemplatesByCategory(category: String): List<PromptTemplate> = withContext(Dispatchers.IO) {
        getAllTemplates().filter { it.category.equals(category, ignoreCase = true) }
    }
    
    suspend fun addUserTemplate(template: PromptTemplate) = withContext(Dispatchers.IO) {
        val userTemplates = loadUserTemplates().toMutableList()
        userTemplates.add(template.copy(isBuiltIn = false))
        saveUserTemplates(userTemplates)
    }
    
    suspend fun updateUserTemplate(template: PromptTemplate) = withContext(Dispatchers.IO) {
        val userTemplates = loadUserTemplates().toMutableList()
        val index = userTemplates.indexOfFirst { it.id == template.id }
        if (index != -1) {
            userTemplates[index] = template.copy(isBuiltIn = false)
            saveUserTemplates(userTemplates)
        }
    }
    
    suspend fun deleteUserTemplate(templateId: String) = withContext(Dispatchers.IO) {
        val userTemplates = loadUserTemplates().toMutableList()
        userTemplates.removeIf { it.id == templateId }
        saveUserTemplates(userTemplates)
    }
    
    fun processTemplate(template: PromptTemplate, variables: Map<String, String>): String {
        var result = template.content
        variables.forEach { (key, value) ->
            result = result.replace("{{$key}}", value)
        }
        return result
    }
    
    fun extractVariables(content: String): List<String> {
        val pattern = "\\{\\{([^}]+)\\}\\}".toRegex()
        return pattern.findAll(content).map { it.groupValues[1] }.distinct().toList()
    }
    
    private fun getDefaultTemplates(): List<PromptTemplate> = listOf(
        PromptTemplate(
            title = "Task Organization",
            description = "Help organize and prioritize tasks",
            content = "Please help me organize and prioritize my tasks for {{timeframe}}. Consider urgency, importance, and dependencies. My tasks are:\n\n{{tasks}}",
            category = "Productivity",
            variables = listOf("timeframe", "tasks"),
            isBuiltIn = true
        ),
        PromptTemplate(
            title = "Weekly Summary",
            description = "Generate a productivity summary",
            content = "Based on my productivity data, please provide a weekly summary focusing on:\n- Completed tasks and achievements\n- Time spent on different activities\n- Areas for improvement\n- Recommendations for next week\n\nData: {{productivity_data}}",
            category = "Analytics",
            variables = listOf("productivity_data"),
            isBuiltIn = true
        ),
        PromptTemplate(
            title = "Goal Setting",
            description = "Help set SMART goals",
            content = "Help me create SMART goals (Specific, Measurable, Achievable, Relevant, Time-bound) for {{area}}. My current situation is: {{current_situation}}\n\nMy desired outcome is: {{desired_outcome}}",
            category = "Planning",
            variables = listOf("area", "current_situation", "desired_outcome"),
            isBuiltIn = true
        ),
        PromptTemplate(
            title = "Meeting Preparation",
            description = "Prepare for meetings effectively",
            content = "Help me prepare for a {{meeting_type}} meeting about {{topic}} scheduled for {{date}}. \n\nAttendees: {{attendees}}\nObjectives: {{objectives}}\n\nPlease suggest:\n1. Agenda items\n2. Key questions to ask\n3. Materials to prepare\n4. Follow-up actions",
            category = "Communication",
            variables = listOf("meeting_type", "topic", "date", "attendees", "objectives"),
            isBuiltIn = true
        ),
        PromptTemplate(
            title = "Workout Plan",
            description = "Create a personalized workout routine",
            content = "Create a {{duration}} workout plan for {{fitness_goal}}. \n\nMy current fitness level: {{fitness_level}}\nAvailable equipment: {{equipment}}\nTime per session: {{session_time}}\nFrequency: {{frequency}}\n\nPlease include exercises, sets, reps, and progression tips.",
            category = "Fitness",
            variables = listOf("duration", "fitness_goal", "fitness_level", "equipment", "session_time", "frequency"),
            isBuiltIn = true
        ),
        PromptTemplate(
            title = "Meal Planning",
            description = "Plan healthy meals based on preferences",
            content = "Create a {{duration}} meal plan with the following requirements:\n\nDietary restrictions: {{restrictions}}\nCalorie target: {{calories}}\nMeal count per day: {{meal_count}}\nPreferred cuisines: {{cuisines}}\nCooking time preference: {{cooking_time}}\n\nInclude shopping list and prep instructions.",
            category = "Nutrition",
            variables = listOf("duration", "restrictions", "calories", "meal_count", "cuisines", "cooking_time"),
            isBuiltIn = true
        ),
        PromptTemplate(
            title = "Budget Analysis",
            description = "Analyze spending patterns and suggest improvements",
            content = "Analyze my spending patterns and provide financial advice:\n\nMonthly income: {{income}}\nFixed expenses: {{fixed_expenses}}\nVariable spending: {{variable_spending}}\nSavings goal: {{savings_goal}}\n\nPlease provide:\n1. Spending analysis\n2. Budget recommendations\n3. Savings strategies\n4. Areas to optimize",
            category = "Finance",
            variables = listOf("income", "fixed_expenses", "variable_spending", "savings_goal"),
            isBuiltIn = true
        ),
        PromptTemplate(
            title = "Learning Plan",
            description = "Create a structured learning plan",
            content = "Create a learning plan for {{subject}} with the following details:\n\nCurrent knowledge level: {{level}}\nTime available per day: {{daily_time}}\nTarget completion: {{target_date}}\nPreferred learning style: {{learning_style}}\nGoal: {{learning_goal}}\n\nInclude resources, milestones, and assessment methods.",
            category = "Education",
            variables = listOf("subject", "level", "daily_time", "target_date", "learning_style", "learning_goal"),
            isBuiltIn = true
        ),
        PromptTemplate(
            title = "Habit Building",
            description = "Build positive habits systematically",
            content = "Help me build the habit of {{habit}}. \n\nCurrent routine: {{current_routine}}\nMotivation: {{motivation}}\nObstacles: {{obstacles}}\nAvailable time: {{available_time}}\n\nProvide a step-by-step plan with:\n1. Start small approach\n2. Trigger and reward system\n3. Progress tracking method\n4. Common pitfalls to avoid",
            category = "Self-Improvement",
            variables = listOf("habit", "current_routine", "motivation", "obstacles", "available_time"),
            isBuiltIn = true
        ),
        PromptTemplate(
            title = "Decision Making",
            description = "Make informed decisions using structured analysis",
            content = "Help me make a decision about {{decision_topic}}.\n\nOptions:\n{{options}}\n\nCriteria that matter to me:\n{{criteria}}\n\nConstraints:\n{{constraints}}\n\nPlease provide:\n1. Pros and cons for each option\n2. Risk assessment\n3. Recommendation with reasoning\n4. Implementation steps",
            category = "Problem-Solving",
            variables = listOf("decision_topic", "options", "criteria", "constraints"),
            isBuiltIn = true
        )
    )
    
    @kotlinx.serialization.Serializable
    private data class TemplateCollection(
        val templates: List<PromptTemplate>
    )
}

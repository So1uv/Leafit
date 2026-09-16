package com.example.fitnesstracker.ui.screens.workout

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.fitnesstracker.export.SharePlatform
import com.example.fitnesstracker.export.WorkoutCardExporter
import com.example.fitnesstracker.export.WorkoutData
import com.example.fitnesstracker.export.WorkoutShareButton
import com.example.fitnesstracker.haptics.HapticFeedback
import com.example.fitnesstracker.notifications.RestTimerNotification
import com.example.fitnesstracker.widgets.MD3ProgressWidget
import com.example.fitnesstracker.widgets.MD3StatsCard
import com.example.fitnesstracker.widgets.MD3WorkoutWidget
import com.example.fitnesstracker.widgets.WorkoutStat

@Composable
fun EnhancedWorkoutScreen(
    modifier: Modifier = Modifier,
    onHapticFeedback: (type: String) -> Unit = {}
) {
    val context = LocalContext.current
    var remainingRestTime by remember { mutableStateOf(0) }
    var isResting by remember { mutableStateOf(false) }
    var totalDistance by remember { mutableStateOf(5.2) }
    var totalCalories by remember { mutableStateOf(380) }
    
    val restTimerNotification = remember { RestTimerNotification(context) }
    val workoutCardExporter = remember { WorkoutCardExporter(context) }
    
    LaunchedEffect(isResting) {
        if (isResting && remainingRestTime > 0) {
            while (remainingRestTime > 0) {
                kotlinx.coroutines.delay(1000)
                remainingRestTime--
                restTimerNotification.updateRestTimer(
                    remainingRestTime,
                    60,
                    "Morning Run"
                )
            }
            if (remainingRestTime == 0) {
                restTimerNotification.showTimerCompletedNotification("Morning Run")
                HapticFeedback.successPattern(context)
                isResting = false
            }
        }
    }
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            "Enhanced Workout",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        MD3WorkoutWidget(
            title = "Morning Run",
            distance = "5.2 km",
            pace = "5:45/km",
            calories = "380 kcal",
            isRunning = !isResting
        )
        
        MD3StatsCard(
            stats = listOf(
                WorkoutStat("Total Distance", "5.2 km"),
                WorkoutStat("Average Pace", "5:45 min/km"),
                WorkoutStat("Total Calories", "380 kcal"),
                WorkoutStat("Duration", "29 min 54 sec"),
                WorkoutStat("Date", "Sep 15, 2026")
            )
        )
        
        MD3ProgressWidget(
            label = "Weekly Distance Goal",
            progress = 0.65f,
            target = "50 km",
            current = "32.5 km"
        )
        
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    "Rest Timer Controls",
                    style = MaterialTheme.typography.titleMedium
                )
                
                if (!isResting) {
                    Button(
                        onClick = {
                            remainingRestTime = 60
                            isResting = true
                            HapticFeedback.lightTap(context)
                            restTimerNotification.showRestTimer(60, 60, "Morning Run")
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Text("Start 1-Minute Rest")
                    }
                } else {
                    Button(
                        onClick = {
                            isResting = false
                            remainingRestTime = 0
                            restTimerNotification.cancelRestTimer()
                            HapticFeedback.warningPattern(context)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Text("Stop Rest (${remainingRestTime}s remaining)")
                    }
                }
            }
        }
        
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    "Haptic Feedback Demo",
                    style = MaterialTheme.typography.titleMedium
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            HapticFeedback.lightTap(context)
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors()
                    ) {
                        Text("Light", style = MaterialTheme.typography.labelSmall)
                    }
                    Button(
                        onClick = {
                            HapticFeedback.mediumTap(context)
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors()
                    ) {
                        Text("Medium", style = MaterialTheme.typography.labelSmall)
                    }
                    Button(
                        onClick = {
                            HapticFeedback.heavyTap(context)
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors()
                    ) {
                        Text("Heavy", style = MaterialTheme.typography.labelSmall)
                    }
                }
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            HapticFeedback.successPattern(context)
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors()
                    ) {
                        Text("Success", style = MaterialTheme.typography.labelSmall)
                    }
                    Button(
                        onClick = {
                            HapticFeedback.warningPattern(context)
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors()
                    ) {
                        Text("Warning", style = MaterialTheme.typography.labelSmall)
                    }
                    Button(
                        onClick = {
                            HapticFeedback.errorPattern(context)
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors()
                    ) {
                        Text("Error", style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        }
        
        WorkoutShareButton(
            workoutData = WorkoutData(
                name = "Morning Run",
                distance = "5.2 km",
                pace = "5:45/km",
                calories = "380 kcal",
                duration = "29 min 54 sec",
                date = "Sep 15, 2026"
            ),
            onShare = { platform ->
                HapticFeedback.successPattern(context)
                workoutCardExporter.shareWorkoutCard(
                    WorkoutData(
                        name = "Morning Run",
                        distance = "5.2 km",
                        pace = "5:45/km",
                        calories = "380 kcal",
                        duration = "29 min 54 sec",
                        date = "Sep 15, 2026"
                    ),
                    platform
                )
            }
        )
        
        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun RoundedCornerShape(dp: Int) = androidx.compose.foundation.shape.RoundedCornerShape(dp.dp)

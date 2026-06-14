package com.example.fitnesstracker.utils;

import java.text.SimpleDateFormat;
import java.util.*;

@kotlin.Metadata(mv = {2, 3, 0}, k = 1, xi = 48, d1 = {"\u0000*\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0003\n\u0002\u0010\u0007\n\u0002\b\u0003\n\u0002\u0010\u000e\n\u0002\b\u0002\n\u0002\u0010\b\n\u0002\b\u0003\n\u0002\u0010\t\n\u0000\b\u00c6\u0002\u0018\u00002\u00020\u0001B\t\b\u0002\u00a2\u0006\u0004\b\u0002\u0010\u0003J\u0016\u0010\u0004\u001a\u00020\u00052\u0006\u0010\u0006\u001a\u00020\u00052\u0006\u0010\u0007\u001a\u00020\u0005J\u000e\u0010\b\u001a\u00020\t2\u0006\u0010\u0004\u001a\u00020\u0005J&\u0010\n\u001a\u00020\u00052\u0006\u0010\u0006\u001a\u00020\u00052\u0006\u0010\u0007\u001a\u00020\u00052\u0006\u0010\u000b\u001a\u00020\f2\u0006\u0010\r\u001a\u00020\tJ\u0016\u0010\u000e\u001a\u00020\u00052\u0006\u0010\u0006\u001a\u00020\u00052\u0006\u0010\u000f\u001a\u00020\u0010\u00a8\u0006\u0011"}, d2 = {"Lcom/example/fitnesstracker/utils/HealthCalc;", "", "<init>", "()V", "bmi", "", "weightKg", "heightCm", "bmiCategory", "", "dailyCalories", "age", "", "gender", "workoutCalories", "durationSeconds", "", "app_debug"})
public final class HealthCalc {
    @org.jetbrains.annotations.NotNull()
    public static final com.example.fitnesstracker.utils.HealthCalc INSTANCE = null;
    
    private HealthCalc() {
        super();
    }
    
    /**
     * ІМТ
     */
    public final float bmi(float weightKg, float heightCm) {
        return 0.0F;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String bmiCategory(float bmi) {
        return null;
    }
    
    /**
     * Денна квота калорій (формула Міффліна–Сан Жеора, седентарний коефіцієнт 1.2)
     */
    public final float dailyCalories(float weightKg, float heightCm, int age, @org.jetbrains.annotations.NotNull()
    java.lang.String gender) {
        return 0.0F;
    }
    
    /**
     * Приблизні калорії за тренування (МЕТ ходьба 3.5)
     */
    public final float workoutCalories(float weightKg, long durationSeconds) {
        return 0.0F;
    }
}
package com.example.fitnesstracker.data.dao;

import androidx.room.*;
import com.example.fitnesstracker.data.entities.Meal;
import kotlinx.coroutines.flow.Flow;

@kotlin.Metadata(mv = {2, 3, 0}, k = 1, xi = 48, d1 = {"\u0000(\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\t\n\u0002\b\u0005\n\u0002\u0010\u0002\n\u0002\b\u0004\bg\u0018\u00002\u00020\u0001J$\u0010\u0002\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00050\u00040\u00032\u0006\u0010\u0006\u001a\u00020\u00072\u0006\u0010\b\u001a\u00020\u0007H\'J\u0016\u0010\t\u001a\u00020\u00072\u0006\u0010\n\u001a\u00020\u0005H\u00a7@\u00a2\u0006\u0002\u0010\u000bJ\u0016\u0010\f\u001a\u00020\r2\u0006\u0010\n\u001a\u00020\u0005H\u00a7@\u00a2\u0006\u0002\u0010\u000bJ\u0014\u0010\u000e\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00070\u00040\u0003H\'J\u000e\u0010\u000f\u001a\u00020\rH\u00a7@\u00a2\u0006\u0002\u0010\u0010\u00a8\u0006\u0011\u00c0\u0006\u0003"}, d2 = {"Lcom/example/fitnesstracker/data/dao/MealDao;", "", "getMealsForDay", "Lkotlinx/coroutines/flow/Flow;", "", "Lcom/example/fitnesstracker/data/entities/Meal;", "dayStart", "", "dayEnd", "insert", "meal", "(Lcom/example/fitnesstracker/data/entities/Meal;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "delete", "", "getAvailableDates", "deleteAll", "(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "app_debug"})
@androidx.room.Dao()
public abstract interface MealDao {
    
    @androidx.room.Query(value = "SELECT * FROM meals WHERE date >= :dayStart AND date < :dayEnd ORDER BY date ASC")
    @org.jetbrains.annotations.NotNull()
    public abstract kotlinx.coroutines.flow.Flow<java.util.List<com.example.fitnesstracker.data.entities.Meal>> getMealsForDay(long dayStart, long dayEnd);
    
    @androidx.room.Insert(onConflict = 1)
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object insert(@org.jetbrains.annotations.NotNull()
    com.example.fitnesstracker.data.entities.Meal meal, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.lang.Long> $completion);
    
    @androidx.room.Delete()
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object delete(@org.jetbrains.annotations.NotNull()
    com.example.fitnesstracker.data.entities.Meal meal, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion);
    
    @androidx.room.Query(value = "SELECT DISTINCT date FROM meals ORDER BY date DESC")
    @org.jetbrains.annotations.NotNull()
    public abstract kotlinx.coroutines.flow.Flow<java.util.List<java.lang.Long>> getAvailableDates();
    
    @androidx.room.Query(value = "DELETE FROM meals")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object deleteAll(@org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion);
}
package com.example.fitnesstracker.ui.navigation;

import androidx.compose.material.icons.Icons;
import androidx.compose.material.icons.filled.*;
import androidx.compose.ui.graphics.vector.ImageVector;

@kotlin.Metadata(mv = {2, 3, 0}, k = 1, xi = 48, d1 = {"\u00000\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u000b\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\b6\u0018\u00002\u00020\u0001:\u0007\b\t\n\u000b\f\r\u000eB\u0011\b\u0004\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0004\b\u0004\u0010\u0005R\u0011\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0006\u0010\u0007\u0082\u0001\u0007\u000f\u0010\u0011\u0012\u0013\u0014\u0015\u00a8\u0006\u0016"}, d2 = {"Lcom/example/fitnesstracker/ui/navigation/Screen;", "", "route", "", "<init>", "(Ljava/lang/String;)V", "getRoute", "()Ljava/lang/String;", "Onboarding", "Home", "Workout", "Nutrition", "Sleep", "Settings", "AppSettings", "Lcom/example/fitnesstracker/ui/navigation/Screen$AppSettings;", "Lcom/example/fitnesstracker/ui/navigation/Screen$Home;", "Lcom/example/fitnesstracker/ui/navigation/Screen$Nutrition;", "Lcom/example/fitnesstracker/ui/navigation/Screen$Onboarding;", "Lcom/example/fitnesstracker/ui/navigation/Screen$Settings;", "Lcom/example/fitnesstracker/ui/navigation/Screen$Sleep;", "Lcom/example/fitnesstracker/ui/navigation/Screen$Workout;", "app_debug"})
public abstract class Screen {
    @org.jetbrains.annotations.NotNull()
    private final java.lang.String route = null;
    
    private Screen(java.lang.String route) {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getRoute() {
        return null;
    }
    
    @kotlin.Metadata(mv = {2, 3, 0}, k = 1, xi = 48, d1 = {"\u0000\f\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0003\b\u00c6\u0002\u0018\u00002\u00020\u0001B\t\b\u0002\u00a2\u0006\u0004\b\u0002\u0010\u0003\u00a8\u0006\u0004"}, d2 = {"Lcom/example/fitnesstracker/ui/navigation/Screen$AppSettings;", "Lcom/example/fitnesstracker/ui/navigation/Screen;", "<init>", "()V", "app_debug"})
    public static final class AppSettings extends com.example.fitnesstracker.ui.navigation.Screen {
        @org.jetbrains.annotations.NotNull()
        public static final com.example.fitnesstracker.ui.navigation.Screen.AppSettings INSTANCE = null;
        
        private AppSettings() {
        }
    }
    
    @kotlin.Metadata(mv = {2, 3, 0}, k = 1, xi = 48, d1 = {"\u0000\f\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0003\b\u00c6\u0002\u0018\u00002\u00020\u0001B\t\b\u0002\u00a2\u0006\u0004\b\u0002\u0010\u0003\u00a8\u0006\u0004"}, d2 = {"Lcom/example/fitnesstracker/ui/navigation/Screen$Home;", "Lcom/example/fitnesstracker/ui/navigation/Screen;", "<init>", "()V", "app_debug"})
    public static final class Home extends com.example.fitnesstracker.ui.navigation.Screen {
        @org.jetbrains.annotations.NotNull()
        public static final com.example.fitnesstracker.ui.navigation.Screen.Home INSTANCE = null;
        
        private Home() {
        }
    }
    
    @kotlin.Metadata(mv = {2, 3, 0}, k = 1, xi = 48, d1 = {"\u0000\f\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0003\b\u00c6\u0002\u0018\u00002\u00020\u0001B\t\b\u0002\u00a2\u0006\u0004\b\u0002\u0010\u0003\u00a8\u0006\u0004"}, d2 = {"Lcom/example/fitnesstracker/ui/navigation/Screen$Nutrition;", "Lcom/example/fitnesstracker/ui/navigation/Screen;", "<init>", "()V", "app_debug"})
    public static final class Nutrition extends com.example.fitnesstracker.ui.navigation.Screen {
        @org.jetbrains.annotations.NotNull()
        public static final com.example.fitnesstracker.ui.navigation.Screen.Nutrition INSTANCE = null;
        
        private Nutrition() {
        }
    }
    
    @kotlin.Metadata(mv = {2, 3, 0}, k = 1, xi = 48, d1 = {"\u0000\f\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0003\b\u00c6\u0002\u0018\u00002\u00020\u0001B\t\b\u0002\u00a2\u0006\u0004\b\u0002\u0010\u0003\u00a8\u0006\u0004"}, d2 = {"Lcom/example/fitnesstracker/ui/navigation/Screen$Onboarding;", "Lcom/example/fitnesstracker/ui/navigation/Screen;", "<init>", "()V", "app_debug"})
    public static final class Onboarding extends com.example.fitnesstracker.ui.navigation.Screen {
        @org.jetbrains.annotations.NotNull()
        public static final com.example.fitnesstracker.ui.navigation.Screen.Onboarding INSTANCE = null;
        
        private Onboarding() {
        }
    }
    
    @kotlin.Metadata(mv = {2, 3, 0}, k = 1, xi = 48, d1 = {"\u0000\f\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0003\b\u00c6\u0002\u0018\u00002\u00020\u0001B\t\b\u0002\u00a2\u0006\u0004\b\u0002\u0010\u0003\u00a8\u0006\u0004"}, d2 = {"Lcom/example/fitnesstracker/ui/navigation/Screen$Settings;", "Lcom/example/fitnesstracker/ui/navigation/Screen;", "<init>", "()V", "app_debug"})
    public static final class Settings extends com.example.fitnesstracker.ui.navigation.Screen {
        @org.jetbrains.annotations.NotNull()
        public static final com.example.fitnesstracker.ui.navigation.Screen.Settings INSTANCE = null;
        
        private Settings() {
        }
    }
    
    @kotlin.Metadata(mv = {2, 3, 0}, k = 1, xi = 48, d1 = {"\u0000\f\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0003\b\u00c6\u0002\u0018\u00002\u00020\u0001B\t\b\u0002\u00a2\u0006\u0004\b\u0002\u0010\u0003\u00a8\u0006\u0004"}, d2 = {"Lcom/example/fitnesstracker/ui/navigation/Screen$Sleep;", "Lcom/example/fitnesstracker/ui/navigation/Screen;", "<init>", "()V", "app_debug"})
    public static final class Sleep extends com.example.fitnesstracker.ui.navigation.Screen {
        @org.jetbrains.annotations.NotNull()
        public static final com.example.fitnesstracker.ui.navigation.Screen.Sleep INSTANCE = null;
        
        private Sleep() {
        }
    }
    
    @kotlin.Metadata(mv = {2, 3, 0}, k = 1, xi = 48, d1 = {"\u0000\f\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0003\b\u00c6\u0002\u0018\u00002\u00020\u0001B\t\b\u0002\u00a2\u0006\u0004\b\u0002\u0010\u0003\u00a8\u0006\u0004"}, d2 = {"Lcom/example/fitnesstracker/ui/navigation/Screen$Workout;", "Lcom/example/fitnesstracker/ui/navigation/Screen;", "<init>", "()V", "app_debug"})
    public static final class Workout extends com.example.fitnesstracker.ui.navigation.Screen {
        @org.jetbrains.annotations.NotNull()
        public static final com.example.fitnesstracker.ui.navigation.Screen.Workout INSTANCE = null;
        
        private Workout() {
        }
    }
}
package com.example.groceryping.viewmodel;

import android.app.Application;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

public class ReminderViewModelFactory implements ViewModelProvider.Factory {
    private final Application application;

    public ReminderViewModelFactory(Application application) {
        this.application = application;
    }

    @Override
    public <T extends ViewModel> T create(Class<T> modelClass) {
        if (modelClass.isAssignableFrom(ReminderViewModel.class)) {
            return (T) new ReminderViewModel(application);
        }
        throw new IllegalArgumentException("Unknown ViewModel class");
    }
} 
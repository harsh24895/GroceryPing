package com.example.groceryping.viewmodel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

public class StoreViewModelFactory implements ViewModelProvider.Factory {
    private final Application application;

    public StoreViewModelFactory(Application application) {
        this.application = application;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(StoreViewModel.class)) {
            return (T) new StoreViewModel(application);
        }
        throw new IllegalArgumentException("Unknown ViewModel class");
    }
} 
package com.example.lab9

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore

val Context.storeDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "store_preferences"
)
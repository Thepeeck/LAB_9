package com.example.lab9

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
sealed interface StoreNavKey : NavKey {

    @Serializable
    data object Catalog : StoreNavKey

    @Serializable
    data class Detail(val productId: String) : StoreNavKey

    @Serializable
    data class Profile(val profileId: String) : StoreNavKey
}

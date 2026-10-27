package com.example.lab9

import android.content.Context
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class AppContainer(
    context: Context
) {

    private val applicationContext = context.applicationContext

    private val storeDao = StoreDatabase
        .getDatabase(applicationContext)
        .storeDao()

    val favoritesRepository: FavoritesRepository =
        RoomFavoritesRepository(storeDao)

    val cartRepository: CartRepository =
        RoomCartRepository(storeDao)

    val catalogPreferencesRepository: CatalogPreferencesRepository =
        DataStoreCatalogPreferencesRepository(
            applicationContext.storeDataStore
        )

    val ordersRepository: OrdersRepository =
        RetrofitOrdersRepository(
            OrderApiClient.api
        )

    val sessionRepository: SessionRepository =
        FirebaseSessionRepository(
            FirebaseAuth.getInstance()
        )

    val productRepository: ProductRepository =
        FirestoreProductRepository(
            firestore = FirebaseFirestore.getInstance(),
            sessionRepository = sessionRepository
        )
}
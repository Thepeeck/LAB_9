package com.example.lab9

import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.MetadataChanges
import java.io.IOException
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull

class FirestoreProductRepository(
    private val firestore: FirebaseFirestore,
    private val sessionRepository: SessionRepository
) : ProductRepository {

    override fun observeProducts(): Flow<List<Product>> {
        return flow {
            val sessionResult = withTimeoutOrNull(15_000L) {
                sessionRepository.ensureAnonymousSession()
            } ?: throw IOException(
                "Authentication timed out."
            )

            sessionResult.getOrThrow()

            emitAll(observeFirestoreProducts())
        }
    }

    private fun observeFirestoreProducts(): Flow<List<Product>> {
        return callbackFlow {
            val initialLoadTimeout = launch {
                delay(15_000L)

                close(
                    IOException(
                        "The initial catalog load timed out."
                    )
                )
            }

            val registration = firestore
                .collection("products")
                .addSnapshotListener(
                    MetadataChanges.INCLUDE
                ) { snapshot, exception ->

                    if (exception != null) {
                        close(exception)
                        return@addSnapshotListener
                    }

                    if (snapshot == null) {
                        close(
                            IllegalStateException(
                                "The catalog snapshot is missing."
                            )
                        )
                        return@addSnapshotListener
                    }

                    if (
                        snapshot.metadata.isFromCache &&
                        snapshot.isEmpty
                    ) {
                        return@addSnapshotListener
                    }

                    try {
                        val products = snapshot.documents.map { document ->
                            document.toProduct()
                        }

                        initialLoadTimeout.cancel()

                        trySend(products)
                    } catch (exception: Exception) {
                        close(exception)
                    }
                }

            awaitClose {
                initialLoadTimeout.cancel()
                registration.remove()
            }
        }.conflate()
    }

    private fun DocumentSnapshot.toProduct(): Product {
        val price = requireNotNull(getDouble("precio")) {
            "Product $id has an invalid price."
        }

        val stock = requireNotNull(get("stock") as? Long) {
            "Product $id must have integer stock."
        }

        require(price.isFinite() && price >= 0.0) {
            "Product $id has an invalid price."
        }

        require(stock in 0L..Int.MAX_VALUE.toLong()) {
            "Product $id has invalid stock."
        }

        return Product(
            id = id,
            name = requiredString("nombre"),
            description = requiredString("descripcion"),
            price = price,
            brandId = requiredString("marcaId"),
            stock = stock.toInt(),
            imageUrl = requiredString("imageUrl")
        )
    }

    private fun DocumentSnapshot.requiredString(
        field: String
    ): String {
        return requireNotNull(getString(field)) {
            "Product $id is missing the field $field."
        }
    }
}
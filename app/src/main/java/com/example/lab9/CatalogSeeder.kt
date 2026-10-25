
package com.example.lab9

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

suspend fun seedCatalog(products: List<Instrumento>) {
    val auth = FirebaseAuth.getInstance()
    if (auth.currentUser == null) {
        auth.signInAnonymously().await()
    }
    val firestore = FirebaseFirestore.getInstance()
    products.chunked(100).forEach { chunk ->
        val batch = firestore.batch()
        chunk.forEach { product ->
            val document = firestore.collection("products").document(product.id)
            batch.set(
                document,
                mapOf(
                    "id" to product.id,
                    "nombre" to product.nombre,
                    "descripcion" to product.descripcion,
                    "precio" to product.precio,
                    "marcaId" to product.marcaId,
                    "stock" to product.stock,
                    "imageUrl" to product.imageUrl
                )
            )
        }
        batch.commit().await()
    }
}
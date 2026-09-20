package com.example.lab9.Pantalla

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade

private sealed interface ImageState {
    data object Loading : ImageState
    data object Success : ImageState
    data object Error : ImageState
}

@Composable
fun ProductImage(
    imageUrl: String,
    modifier: Modifier = Modifier
) {
    var imageState by remember(imageUrl) {
        mutableStateOf<ImageState>(ImageState.Loading)
    }

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        when (imageState) {
            ImageState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                )
            }
            ImageState.Error -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Imagen no disponible")
                }
            }
            ImageState.Success -> {
                // El AsyncImage de abajo ya muestra la imagen
            }
        }

        AsyncImage(
            model = ImageRequest.Builder(androidx.compose.ui.platform.LocalContext.current)
                .data(imageUrl)
                .crossfade(true)
                .build(),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            onLoading = { imageState = ImageState.Loading },
            onSuccess = { imageState = ImageState.Success },
            onError = { imageState = ImageState.Error }
        )
    }
}
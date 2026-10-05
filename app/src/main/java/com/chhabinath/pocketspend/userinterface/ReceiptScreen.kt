package com.chhabinath.pocketspend.userinterface

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import android.graphics.BitmapFactory

@Composable
fun ReceiptScreen(
    onBack: () -> Unit,
    onAnalyzeWithAI: (String) -> Unit
) {

    val context = LocalContext.current

    var selectedImageUri by remember {
        mutableStateOf<Uri?>(null)
    }

    var selectedBitmap by remember {
        mutableStateOf<android.graphics.Bitmap?>(null)
    }

    var extractedText by remember {
        mutableStateOf("")
    }

    var isProcessing by remember {
        mutableStateOf(false)
    }

    var errorMessage by remember {
        mutableStateOf("")
    }

    val imagePickerLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.GetContent()
        ) { uri: Uri? ->

            if (uri != null) {

                selectedImageUri = uri
                extractedText = ""
                errorMessage = ""

                try {

                    context.contentResolver
                        .openInputStream(uri)
                        ?.use { inputStream ->

                            selectedBitmap =
                                BitmapFactory.decodeStream(inputStream)
                        }

                } catch (e: Exception) {

                    errorMessage =
                        "Could not load image: ${e.message}"
                }
            }
        }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {

        // Header

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {

            TextButton(
                onClick = onBack
            ) {
                Text("Back")
            }

            Text(
                text = "Scan Receipt",
                style = MaterialTheme.typography.titleLarge
            )

            Spacer(
                modifier = Modifier.size(48.dp)
            )
        }

        Spacer(
            modifier = Modifier.height(24.dp)
        )

        if (selectedImageUri == null) {

            // Empty state

            Text(
                text = "Select a receipt photo",
                style = MaterialTheme.typography.headlineSmall
            )

            Spacer(
                modifier = Modifier.height(12.dp)
            )

            Text(
                text = "The receipt will be processed locally on your device."
            )

            Spacer(
                modifier = Modifier.height(24.dp)
            )

            Button(
                onClick = {
                    imagePickerLauncher.launch("image/*")
                },
                modifier = Modifier.fillMaxWidth()
            ) {

                Text("Choose Receipt")
            }

        } else {

            // Receipt selected

            Text(
                text = "Receipt Preview",
                style = MaterialTheme.typography.headlineSmall
            )

            Spacer(
                modifier = Modifier.height(12.dp)
            )

            selectedBitmap?.let { bitmap ->

                Image(
                    bitmap = bitmap.asImageBitmap(),
                    contentDescription = "Receipt",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp),
                    contentScale = ContentScale.Fit
                )
            }

            Spacer(
                modifier = Modifier.height(16.dp)
            )

            // OCR button

            Button(
                onClick = {

                    val uri = selectedImageUri

                    if (uri == null) {
                        return@Button
                    }

                    isProcessing = true
                    errorMessage = ""
                    extractedText = ""

                    try {

                        val image =
                            InputImage.fromFilePath(
                                context,
                                uri
                            )

                        val recognizer =
                            TextRecognition.getClient(
                                TextRecognizerOptions.DEFAULT_OPTIONS
                            )

                        recognizer.process(image)
                            .addOnSuccessListener { visionText ->

                                extractedText =
                                    visionText.text

                                isProcessing = false

                                recognizer.close()
                            }
                            .addOnFailureListener { exception ->

                                errorMessage =
                                    "OCR failed: ${exception.message}"

                                isProcessing = false

                                recognizer.close()
                            }

                    } catch (e: Exception) {

                        errorMessage =
                            "Could not process receipt: ${e.message}"

                        isProcessing = false
                    }

                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isProcessing
            ) {

                if (isProcessing) {

                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp)
                    )

                } else {

                    Text("Extract Text")
                }
            }

            Spacer(
                modifier = Modifier.height(16.dp)
            )

            // OCR result

            if (extractedText.isNotBlank()) {

                Text(
                    text = "Detected Text",
                    style = MaterialTheme.typography.titleLarge
                )

                Spacer(
                    modifier = Modifier.height(8.dp)
                )

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {

                    Column(
                        modifier = Modifier
                            .padding(16.dp)
                            .verticalScroll(
                                rememberScrollState()
                            )
                    ) {

                        Text(
                            text = extractedText,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }

                Spacer(
                    modifier = Modifier.height(12.dp)
                )

                Button(
                    onClick = {
                        if (extractedText.isNotBlank()) {
                            onAnalyzeWithAI(extractedText)
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {

                    Text("Analyze with Local AI")
                }

            } else {

                if (errorMessage.isNotBlank()) {

                    Text(
                        text = errorMessage,
                        color = MaterialTheme.colorScheme.error
                    )

                    Spacer(
                        modifier = Modifier.height(12.dp)
                    )
                }

                Text(
                    text = "Tap Extract Text to run on-device OCR."
                )
            }

            Spacer(
                modifier = Modifier.height(8.dp)
            )

            TextButton(
                onClick = {
                    imagePickerLauncher.launch("image/*")
                },
                modifier = Modifier.fillMaxWidth()
            ) {

                Text("Choose Another Receipt")
            }
        }
    }
}
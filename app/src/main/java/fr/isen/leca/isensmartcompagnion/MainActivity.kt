package fr.isen.leca.isensmartcompagnion

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import fr.isen.leca.isensmartcompagnion.ui.theme.ISENSmartCompagnionTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ISENSmartCompagnionTheme {
                AssistantScreen()
            }
        }
    }
}

@Composable
fun AssistantScreen() {
    var question by remember { mutableStateOf("") }
    var response by remember { mutableStateOf("Pose-moi une question !") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Logo et Titre
        Image(
            painter = painterResource(id = R.drawable.isen), // Remplace par ton logo
            contentDescription = "Logo ISEN",
            modifier = Modifier.size(120.dp)
        )
        Text(
            text = "Smart Companion",
            fontSize = 24.sp,
            modifier = Modifier.padding(top = 8.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Champ de saisie
        OutlinedTextField(
            value = question,
            onValueChange = { question = it },
            label = { Text("Pose ta question...") },
            singleLine = true,
            keyboardOptions = KeyboardOptions.Default.copy(
                imeAction = ImeAction.Done
            ),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Bouton Envoyer
        Button(
            onClick = { response = "Je ne sais pas encore répondre, mais bientôt ! 😉" }
        ) {
            Text("Envoyer")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Réponse de l'IA
        Text(
            text = response,
            fontSize = 18.sp,
            modifier = Modifier.padding(16.dp)
        )
    }
}

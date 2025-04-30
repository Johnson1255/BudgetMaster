package com.senlin.budgetmaster.ui.splash

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.senlin.budgetmaster.R // Assuming R is generated in this package
import com.senlin.budgetmaster.ui.theme.BudgetMasterTheme

@Composable
fun SplashScreen(
    onLanguageSelected: (String) -> Unit // Callback for when a language is chosen
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Welcome to BudgetMaster!", // Placeholder - will use stringResource
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        Text(
            text = "Please select your language:", // Placeholder
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(onClick = { onLanguageSelected("en") }) {
                Text("English") // Placeholder
            }
            Button(onClick = { onLanguageSelected("es") }) {
                Text("Español") // Placeholder
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SplashScreenPreview() {
    BudgetMasterTheme {
        SplashScreen(onLanguageSelected = {})
    }
}

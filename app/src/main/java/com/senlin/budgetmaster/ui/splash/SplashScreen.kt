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
import com.senlin.budgetmaster.R
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
            text = stringResource(id = R.string.splash_welcome),
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        Text(
            text = stringResource(id = R.string.splash_select_language),
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(onClick = { onLanguageSelected("en") }) {
                Text(stringResource(id = R.string.language_english))
            }
            Button(onClick = { onLanguageSelected("es") }) {
                Text(stringResource(id = R.string.language_spanish))
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

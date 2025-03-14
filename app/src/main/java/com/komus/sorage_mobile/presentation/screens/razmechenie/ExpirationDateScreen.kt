package com.komus.sorage_mobile.presentation.screens.razmechenie
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.komus.sorage_mobile.domain.viewModel.ExpirationDateViewModel

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ExpirationDateScreen(
    navController: NavController,
    viewModel: ExpirationDateViewModel = hiltViewModel()
) {
    var startDate by remember { mutableStateOf("") }
    var days by remember { mutableStateOf("") }
    var months by remember { mutableStateOf("") }
    var endDate by remember { mutableStateOf("") }
    var condition by remember { mutableStateOf<String?>(null) }

    // Автоматический расчет конечной даты
    LaunchedEffect(startDate, days, months) {
        if (startDate.isNotEmpty() && (days.isNotEmpty() || months.isNotEmpty())) {
            endDate = viewModel.calculateEndDate(startDate, days, months)
        }
    }

    Column(modifier = Modifier.padding(16.dp)) {
        Text(text = "Введите срок годности", style = MaterialTheme.typography.h6)

        TextField(
            value = startDate,
            onValueChange = { startDate = it },
            label = { Text("Начальная дата (dd.MM.yyyy)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))

        Row {
            TextField(
                value = days,
                onValueChange = { days = it },
                label = { Text("Дни") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(8.dp))
            TextField(
                value = months,
                onValueChange = { months = it },
                label = { Text("Месяцы") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))

        TextField(
            value = endDate,
            onValueChange = {},
            label = { Text("Конечная дата") },
            enabled = false,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))

        Row {
            Checkbox(
                checked = condition == "Кондиция",
                onCheckedChange = { if (it) condition = "Кондиция" }
            )
            Text("Кондиция")
            Spacer(modifier = Modifier.width(16.dp))
            Checkbox(
                checked = condition == "Некондиция",
                onCheckedChange = { if (it) condition = "Некондиция" }
            )
            Text("Некондиция")
        }
        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                viewModel.saveExpirationData(startDate, days, months, condition ?: "")
                navController.popBackStack()
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = startDate.isNotEmpty() && condition != null
        ) {
            Text("Сохранить")
        }
    }
}
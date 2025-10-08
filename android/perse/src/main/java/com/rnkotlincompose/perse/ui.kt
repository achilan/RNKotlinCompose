package com.rnkotlincompose.perse.ui

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.rnkotlincompose.core.PageState
import com.rnkotlincompose.core.User
import com.rnkotlincompose.core.UsersRepository
import kotlinx.coroutines.launch

@Composable
fun UsersScreen(
    repositoryFactory: () -> UsersRepository,
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    var page by remember { mutableStateOf(1) }
    val perPage = 10
    var state by remember { mutableStateOf<PageState<User>>(PageState.Loading) }

    val repo = remember { repositoryFactory() }

    fun load() {
        state = PageState.Loading
        scope.launch {
            val result = repo.getUsers(page, perPage)
            state = result.fold(
                onSuccess = {
                    val (items, totalPages) = it
                    if (items.isEmpty()) PageState.Empty else PageState.Data(items, page, totalPages)
                },
                onFailure = { PageState.Error(it.message ?: "Error") }
            )
        }
    }

    LaunchedEffect(page) { load() }

    var name by remember { mutableStateOf(TextFieldValue("")) }
    var email by remember { mutableStateOf(TextFieldValue("")) }
    var gender by remember { mutableStateOf("male") }
    var status by remember { mutableStateOf("active") }
    var logger by remember { mutableStateOf("") }

    Column(modifier.padding(16.dp)) {
        Text("Crear usuario", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Nombre") }, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Email") }, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Género:")
            Spacer(Modifier.width(8.dp))
            FilterChip(selected = gender == "male", onClick = { gender = "male" }, label = { Text("Male") })
            Spacer(Modifier.width(8.dp))
            FilterChip(selected = gender == "female", onClick = { gender = "female" }, label = { Text("Female") })
        }
        Spacer(Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Estado:")
            Spacer(Modifier.width(8.dp))
            FilterChip(selected = status == "active", onClick = { status = "active" }, label = { Text("Active") })
            Spacer(Modifier.width(8.dp))
            FilterChip(selected = status == "inactive", onClick = { status = "inactive" }, label = { Text("Inactive") })
        }
        Spacer(Modifier.height(8.dp))
        Button(onClick = {
            val nameStr = name.text.trim()
            val emailStr = email.text.trim()
            val valid = nameStr.isNotEmpty() && emailStr.matches(Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$"))
            if (!valid) {
                Toast.makeText(context, "Datos inválidos", Toast.LENGTH_SHORT).show()
                return@Button
            }
            scope.launch {
                val res = repo.createUser(User(name = nameStr, email = emailStr, gender = gender, status = status))
                res.fold(
                    onSuccess = {
                        Toast.makeText(context, "Creado: ${it.id}", Toast.LENGTH_SHORT).show()
                        load()
                    },
                    onFailure = {
                        Toast.makeText(context, "Error: ${it.message}", Toast.LENGTH_LONG).show()
                        logger = "Error: ${it.message}"
                    }
                )
            }
        }) { Text("Crear") }

        Spacer(Modifier.height(16.dp))
        Text("Usuarios", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(8.dp))
        if (logger.isNotEmpty()) {
            Text(logger, color = MaterialTheme.colorScheme.error)
            Spacer(Modifier.height(8.dp))
        }

        when (val s = state) {
            PageState.Loading -> Box(Modifier.fillMaxWidth().height(100.dp), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            PageState.Empty -> Text("Sin datos")
            is PageState.Error -> Text("Error: ${s.message}")
            is PageState.Data -> {
                LazyColumn(Modifier.weight(1f, fill = false)) {
                    items(s.items) { u ->
                        ListItem(headlineContent = { Text(u.name) }, supportingContent = { Text(u.email) })
                        Divider()
                    }
                }
                Spacer(Modifier.height(8.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Button(onClick = { if (page > 1) page -= 1 }, enabled = page > 1) { Text("Anterior") }
                    Text("Página $page")
                    Button(onClick = { if (page < s.totalPages) page += 1 }, enabled = page < s.totalPages) { Text("Siguiente") }
                }
            }
        }
    }
}

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

    Column(modifier.padding(16.dp)) {
    var pendingDelete by remember { mutableStateOf<User?>(null) }

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
                        Toast.makeText(context, "Error: ${it.message}", Toast.LENGTH_SHORT).show()
                    }
                )
            }
        }) { Text("Crear") }

        Spacer(Modifier.height(16.dp))
        Text("Usuarios", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(8.dp))

        when (val s = state) {
            PageState.Loading -> Box(Modifier.fillMaxWidth().height(100.dp), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            PageState.Empty -> Text("Sin datos")
            is PageState.Error -> Text("Error: ${s.message}")
            is PageState.Data -> {
                LazyColumn(Modifier.weight(1f)) {
                    items(s.items) { u ->
                        var editing by remember { mutableStateOf(false) }
                        var nameEdit by remember { mutableStateOf(TextFieldValue(u.name)) }
                        var emailEdit by remember { mutableStateOf(TextFieldValue(u.email)) }

                        if (editing) {
                            Column(Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
                                OutlinedTextField(value = nameEdit, onValueChange = { nameEdit = it }, label = { Text("Nombre") }, modifier = Modifier.fillMaxWidth())
                                Spacer(Modifier.height(6.dp))
                                OutlinedTextField(value = emailEdit, onValueChange = { emailEdit = it }, label = { Text("Email") }, modifier = Modifier.fillMaxWidth())
                                Spacer(Modifier.height(6.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Button(onClick = {
                                        val valid = nameEdit.text.isNotBlank() && emailEdit.text.matches(Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$"))
                                        if (!valid) {
                                            Toast.makeText(context, "Datos inválidos", Toast.LENGTH_SHORT).show()
                                            return@Button
                                        }
                                        scope.launch {
                                            val res = repo.updateUser(
                                                u.copy(name = nameEdit.text.trim(), email = emailEdit.text.trim())
                                            )
                                            res.fold(
                                                onSuccess = {
                                                    Toast.makeText(context, "Actualizado", Toast.LENGTH_SHORT).show()
                                                    editing = false
                                                    load()
                                                },
                                                onFailure = { Toast.makeText(context, "Error: ${it.message}", Toast.LENGTH_SHORT).show() }
                                            )
                                        }
                                    }) { Text("Guardar") }
                                    OutlinedButton(onClick = { editing = false }) { Text("Cancelar") }
                                }
                            }
                        } else {
                            ListItem(
                                headlineContent = { Text(u.name) },
                                supportingContent = { Text(u.email) },
                                trailingContent = {
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        TextButton(onClick = { editing = true }) { Text("Editar") }
                                        TextButton(onClick = { pendingDelete = u }) { Text("Eliminar") }
                                    }
                                }
                            )
                            Divider()
                        }
                    }
                }
                Spacer(Modifier.height(8.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Button(onClick = { if (page > 1) page -= 1 }, enabled = page > 1) { Text("Anterior") }
                    Text("Página $page")
                    Button(onClick = { if (page < s.totalPages) page += 1 }, enabled = page < s.totalPages) { Text("Siguiente") }
                }

                if (pendingDelete != null) {
                    AlertDialog(
                        onDismissRequest = { pendingDelete = null },
                        title = { Text("Confirmar eliminación") },
                        text = { Text("¿Seguro que deseas eliminar a ${pendingDelete!!.name}?") },
                        confirmButton = {
                            TextButton(onClick = {
                                val id = pendingDelete!!.id
                                if (id == null) {
                                    Toast.makeText(context, "Sin ID", Toast.LENGTH_SHORT).show()
                                    pendingDelete = null
                                } else {
                                    scope.launch {
                                        val res = repo.deleteUser(id)
                                        res.fold(
                                            onSuccess = {
                                                Toast.makeText(context, "Eliminado", Toast.LENGTH_SHORT).show()
                                                pendingDelete = null
                                                load()
                                            },
                                            onFailure = {
                                                Toast.makeText(context, "Error: ${it.message}", Toast.LENGTH_SHORT).show()
                                                pendingDelete = null
                                            }
                                        )
                                    }
                                }
                            }) { Text("Eliminar") }
                        },
                        dismissButton = { TextButton(onClick = { pendingDelete = null }) { Text("Cancelar") } }
                    )
                }
            }
        }
    }
}

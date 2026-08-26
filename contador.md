Vinícius Borges - RA: 078448

O Compose monitora a variável `const` através da função `mutableStateof(0)`.
Para o `remember` garante que o valor fica guardado na memória. Sempre que o 
compose altera o valor em `cont` e o compose percebe esta mudança e atualiza
na tela usando o comando `Text`.

package com.example.contador

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.contador.ui.theme.ContadorTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ContadorTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Box(modifier = Modifier.padding(innerPadding)) {
                        ContadorBG()
                    }
                }
            }
        }
    }
}

@Composable
fun ContadorBG() {
    var cont by remember { mutableStateOf(0) }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "$cont",
            fontSize = 32.sp
        )

        Spacer(modifier = Modifier.height(32.dp))

        Row {
            Button(onClick = {
                if (cont > 0) {
                    cont--

                }
            }) {
                Text("Diminuir")

            }

            Spacer(modifier = Modifier.width(32.dp))

            Button(onClick = { cont++ }) {
                Text("Aumentar")
            }
        }
    }
}

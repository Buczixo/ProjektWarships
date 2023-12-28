@file:Suppress("DEPRECATION")

package com.example.myapplication

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.myapplication.ui.theme.MyApplicationTheme
import java.util.logging.Logger.global

var Player = 0;
var Opponent = 0;

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyApplicationTheme {
                Surface(
                    color = MaterialTheme.colorScheme.background,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        ScreenNavigation()
                    }
                }
            }
        }
    }
}

@Composable
fun ScreenNavigation() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "PLAYER"
    ) {
        composable("PLAYER") {
            PlayerSelector(navController)
        }
        composable("PLACING") {
            ShipPlacing(navController)
        }
        composable("GAME") {
            WarshipsGame()
        }
    }
}

@Composable
fun PlayerSelector(navController: NavController) {
Column {
    Button(
        onClick = {
            navController.navigate("PLACING")
            Player = 1
            Opponent = 2 },
        modifier = Modifier
            .width(250.dp)
            .height(100.dp)
            .padding(16.dp)
    ) {
        Text(text = "Choose Player 1")
    }
    Button(
        onClick = {navController.navigate("PLACING")
            Player = 2
            Opponent = 1 },
        modifier = Modifier
            .width(250.dp)
            .height(100.dp)
            .padding(16.dp)
    ) {
        Text(text = "Choose Player 2")
    }
}


    }


@Composable
fun ShipPlacing(navController: NavController) {
    var PlayerShips by remember { mutableStateOf(List(10 * 10) { CellState.Empty }) }
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        LazyColumn {
            items(10) { row ->
                Row {
                    for (col in 0 until 10) {
                        val index = row * 10 + col
                        WarshipCell(PlayerShips[index]) {
                                PlayerShips = PlayerShips.toMutableList().apply {
                                    if (this[index] == CellState.Empty) {
                                        this[index] = CellState.Ship
                                    }
                                }
                            }
                        }
                    }
                }
            }


        Button(
            onClick = {
                navController.navigate("GAME")

            },
            modifier = Modifier
                .width(250.dp)
                .height(100.dp)
                .padding(16.dp)
        ) {
            Text(text = "Finish ship placing")
        }
    }}

@Composable
fun WarshipsGame() {
    var placingShip by remember { mutableStateOf(false) }
    var cells by remember { mutableStateOf(List(10 * 10) { CellState.Empty }) }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        LazyColumn {

            items(10) { row ->
                Row {
                    for (col in 0 until 10) {
                        val index = row * 10 + col
                        WarshipCell(cells[index]) {
                            if (placingShip) {
                                // Place a ship in the selected cell
                                cells = cells.toMutableList().apply {
                                    if (this[index] == CellState.Empty) {
                                        this[index] = CellState.Ship
                                    }
                                }
                            } else {
                                cells = cells.toMutableList().apply {
                                    if (this[index] == CellState.Ship) {
                                        this[index] = CellState.ShipHit
                                    } else if (this[index] == CellState.Empty) {
                                        this[index] = CellState.EmptyHit
                                    }

                                }
                            }
                        }
                    }
                }
            }
        }

        Button(
            onClick = {
                // Toggle ship placing mode
                placingShip = !placingShip
            },
            modifier = Modifier
                .width(250.dp)
                .height(100.dp)
                .padding(16.dp)
        ) {
            Text(text = if (placingShip) "Player $Player" else "Opponent $Opponent")
        }
    }
}

@Composable
fun WarshipCell(state: CellState, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(20.dp)
            .padding(2.dp)
            .clip(CircleShape.copy(all = CornerSize(4.dp)))
            .background(getCellColor(state))
            .clickable(onClick = onClick)
    ) {}
}

@Composable
fun getCellColor(state: CellState): Color {
    return when (state) {
        CellState.Empty -> Color.Gray
        CellState.EmptyHit -> Color.Blue
        CellState.Ship -> Color.Green
        CellState.ShipHit -> Color.Red
    }
}

enum class CellState {
    Empty,
    EmptyHit,
    Ship,
    ShipHit
}

@Preview(showBackground = true)
@Composable
fun PreviewWarshipsGame() {
    WarshipsGame()
}

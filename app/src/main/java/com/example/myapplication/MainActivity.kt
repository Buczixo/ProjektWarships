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
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener


val firebaseDatabase = FirebaseDatabase.getInstance()
val databaseReference = firebaseDatabase.reference

var Player = ""
var Opponent = ""
var Turn = ""
var PlayerShips = List(10 * 10) { CellState.Empty }
var Battlefield = List(10 * 10) { CellState.Empty }


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

fun dataInput(player : String, field : String): DatabaseReference {
    return databaseReference.child("Warships").child(player).child(field)
}

fun DataDownload(player: String) {
    // Download from Firebase
    databaseReference.child("Warships").child(player).child("PlayerShips")
        .addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val ShipsFromFirebase = snapshot.value
                PlayerShips = (ShipsFromFirebase as List<*>).map {
                    when (it) {
                        "Empty" -> CellState.Empty
                        "EmptyHit" -> CellState.EmptyHit
                        "Ship" -> CellState.Ship
                        "ShipHit" -> CellState.ShipHit
                        else -> CellState.Empty
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })

    databaseReference.child("Warships").child(player).child("battlefield")
        .addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val ShipsFromFirebase = snapshot.value
                Battlefield = (ShipsFromFirebase as List<*>).map {
                    when (it) {
                        "Empty" -> CellState.Empty
                        "EmptyHit" -> CellState.EmptyHit
                        "Ship" -> CellState.Ship
                        "ShipHit" -> CellState.ShipHit
                        else -> CellState.Empty
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })

    databaseReference.child("Warships").child("Game").child("Turn")
        .addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val FirebaseTurn = snapshot.value
                Turn = FirebaseTurn.toString()

                }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
}

@Composable
fun PlayerSelector(navController: NavController) {

    Column {
        Button(
            onClick = {
                Player = "Player1"
                Opponent = "Player2"
                DataDownload(Player)
                navController.navigate("PLACING")
            },
            modifier = Modifier
                .width(250.dp)
                .height(100.dp)
                .padding(16.dp)
        ) {
            Text(text = "Choose Player 1")
        }

        Button(
            onClick = {
                Player = "Player2"
                Opponent = "Player1"
                DataDownload(Player)
                navController.navigate("PLACING")
            },
            modifier = Modifier
                .width(250.dp)
                .height(100.dp)
                .padding(16.dp)
        ) {
            Text(text = "Choose Player 2")
        }

        Button(
            onClick = {
                // Reset both PlayerShips and battlefield to empty
                // Upload the empty lists to Firebase
                val Reset = List(10 * 10) { CellState.Empty }
                databaseReference.child("Warships").child("Player1").child("battlefield").setValue(Reset)
                databaseReference.child("Warships").child("Player2").child("battlefield").setValue(Reset)
                databaseReference.child("Warships").child("Player1").child("PlayerShips").setValue(Reset)
                databaseReference.child("Warships").child("Player2").child("PlayerShips").setValue(Reset)

                databaseReference.child("Warships").child("Game").child("Player1Ships").setValue(0)
                databaseReference.child("Warships").child("Game").child("Player2Ships").setValue(0)
                databaseReference.child("Warships").child("Game").child("Turn").setValue("Player1")
            },
            modifier = Modifier
                .width(250.dp)
                .height(100.dp)
                .padding(16.dp)
        ) {
            Text(text = "Reset Gameplay")
        }
    }
}





@Composable
fun ShipPlacing(navController: NavController) {
    var playerShips by remember { mutableStateOf(List(100) { CellState.Empty }) }
    playerShips = PlayerShips

    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        LazyColumn {
            items(10) { row ->
                Row {
                    for (col in 0 until 10) {
                        val index = row * 10 + col
                        Box {
                            PlaceCell(playerShips[index]) {
                                playerShips = playerShips.toMutableList().apply {
                                    if(this[index] == CellState.Empty) {
                                        this[index] = CellState.Ship
                                    }
                                }
                                dataInput(Player, "PlayerShips").setValue(playerShips)
                                dataInput(Opponent, "battlefield").setValue(playerShips)

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
    var battlefield by remember { mutableStateOf(List(100) { CellState.Empty }) }
    battlefield = Battlefield
    var turn by remember { mutableStateOf("")}
    turn = Turn
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        LazyColumn {
            items(10) { row ->
                Row {
                    for (col in 0 until 10) {
                        val index = row * 10 + col
                        GameCell(battlefield[index]) {
                                // Place a ship in the selected cell
                            battlefield = battlefield.toMutableList().apply {
                                    if (this[index] == CellState.Ship) {
                                        this[index] = CellState.ShipHit
                                    } else if (this[index] == CellState.Empty) {
                                        this[index] = CellState.EmptyHit
                                    }

                                }
                            dataInput(Player, "battlefield").setValue(battlefield)
                        }
                    }
                }
            }
        }

        Button(
            onClick = {},
            modifier = Modifier
                .width(250.dp)
                .height(100.dp)
                .padding(16.dp)
        ) {
            Text(text = "Player $Player and Opponent $Opponent")
        }
    }
}

@Composable
fun PlaceCell(state: CellState, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(20.dp)
            .padding(2.dp)
            .clip(CircleShape.copy(all = CornerSize(4.dp)))
            .background(placeCellColor(state))
            .clickable(onClick = onClick)
    ) {}
}

@Composable
fun placeCellColor(state: CellState): Color {
    return when (state) {
        CellState.Empty -> Color.Gray
        CellState.EmptyHit -> Color.Blue
        CellState.Ship -> Color.Green
        CellState.ShipHit -> Color.Red
    }
}

@Composable
fun GameCell(state: CellState, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(20.dp)
            .padding(2.dp)
            .clip(CircleShape.copy(all = CornerSize(4.dp)))
            .background(GameCellColor(state))
            .clickable(onClick = onClick)
    ) {}
}

@Composable
fun GameCellColor(state: CellState): Color {
    return when (state) {
        CellState.Empty -> Color.Gray
        CellState.EmptyHit -> Color.Blue
        CellState.Ship -> Color.Gray
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

@file:Suppress("DEPRECATION")

package com.example.myapplication

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
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
import androidx.compose.ui.res.painterResource
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

//works

val firebaseDatabase = FirebaseDatabase.getInstance()
val databaseReference = firebaseDatabase.reference

var Player = ""
var Opponent = ""
var Turn = "Player1"
var PlayerShipCount = 0
var EnemyHit = 0
var PlayerHit = 0
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
            WarshipsGame(navController)
        }
        composable("WIN"){
            YouWinScreen(navController)
        }
        composable("LOOSE"){
            YouLooseScreen(navController)
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
                val BattleFromFirebase = snapshot.value
                Battlefield = (BattleFromFirebase as List<*>).map {
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

    databaseReference.child("Warships").child("Game").child(player)
        .addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                PlayerShipCount = snapshot.value.toString().toInt()

            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })

    databaseReference.child("Warships").child("Game").child(player+"hit")
        .addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
               PlayerHit = snapshot.value.toString().toInt()

            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
    var opponent = ""
    if(player == "Player1"){
        opponent = "Player2"
    }else{
        opponent = "Player1"
    }
    databaseReference.child("Warships").child("Game").child(opponent+"hit")
        .addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                EnemyHit = snapshot.value.toString().toInt()

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

                databaseReference.child("Warships").child("Game").child("Player1").setValue(0)
                databaseReference.child("Warships").child("Game").child("Player2").setValue(0)
                databaseReference.child("Warships").child("Game").child("Player1hit").setValue(0)
                databaseReference.child("Warships").child("Game").child("Player2hit").setValue(0)
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
    var playerShips by remember { mutableStateOf(PlayerShips) }
    var visi by remember { mutableIntStateOf(0) }
    var playerShipCountLocal by remember { mutableIntStateOf(0) }
    playerShipCountLocal = PlayerShipCount

    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("$Player")


        LazyColumn {
            items(10) { row ->
                Row {
                    for (col in 0 until 10) {
                        val index = row * 10 + col
                        Box {
                            PlaceCell(playerShips[index]) {
                                playerShips = playerShips.toMutableList().apply {
                                    if(this[index] == CellState.Empty && playerShipCountLocal < 20) {
                                        this[index] = CellState.Ship
                                        playerShipCountLocal++
                                        PlayerShipCount = playerShipCountLocal
                                    }
                                }
                                dataInput(Player, "PlayerShips").setValue(playerShips)
                                dataInput(Opponent, "battlefield").setValue(playerShips)
                                dataInput("Game", Player).setValue(playerShipCountLocal)
                                visi++
                            }
                        }
                    }
                }
            }
        }

        if(PlayerShipCount == 20) {
            visi ++
            Button(
                onClick = {
                    navController.navigate("GAME")
                },
                modifier = Modifier
                    .width(250.dp)
                    .height(100.dp)
                    .padding(16.dp)
            ) {
                Text(text = "Placed $playerShipCountLocal ships ! Finish ship placing")
            }
        }else{
            visi++
            Button(
                onClick = {
                },
                modifier = Modifier

                    .width(250.dp)
                    .height(100.dp)
                    .padding(16.dp)
            ) {
                Text(text = "You have ${(20 - playerShipCountLocal)} ships left to place !")
            }
        }
    }}

@Composable
fun WarshipsGame(navController: NavController) {
    var battlefield by remember { mutableStateOf(Battlefield) }
    var turn by remember { mutableStateOf(Turn)}
    var player by remember { mutableStateOf(Player)}
    var visi by remember { mutableIntStateOf(0) }
    var hit by remember { mutableIntStateOf(PlayerHit) }
    var Ehit by remember { mutableIntStateOf(EnemyHit) }
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("$Player")

        LazyColumn {
            items(10) { row ->
                Row {
                    for (col in 0 until 10) {
                        val index = row * 10 + col
                        GameCell(battlefield[index]) {
                            // Place a ship in the selected cell
                            battlefield = battlefield.toMutableList().apply {
                                if(hit < 20) {
                                    if (this[index] == CellState.Ship) {
                                        if(Turn == player){
                                            this[index] = CellState.ShipHit
                                            Turn = Opponent
                                            hit++
                                        }
                                    } else if (this[index] == CellState.Empty) {
                                        if(Turn == player) {
                                            this[index] = CellState.EmptyHit
                                            Turn = Opponent
                                        }
                                    }
                                    visi++
                                    PlayerHit = hit
                                    turn = Turn
                                }
                            }
                            dataInput(Player, "battlefield").setValue(battlefield)
                            dataInput("Game", player+"hit").setValue(hit)
                            dataInput("Game", "Turn").setValue(turn)

                            if(hit == 20){
                                navController.navigate("WIN")
                            }
                            if(EnemyHit == 20){
                                navController.navigate("LOOSE")
                            }
                            visi++

                        }
                    }
                }
            }
        }

        Button(
            onClick = {},
            modifier = Modifier
                .width(400.dp)
                .height(200.dp)
                .padding(16.dp)
        ) {
            Text(text = "Turn of $Turn\nYou have ${20- EnemyHit} ships left and you hit $hit")
            visi++

        }
    }
}

@Composable
fun PlaceCell(state: CellState, onClick: () -> Unit) {

    Box(
        modifier = Modifier
            .size(35.dp)
            .padding(2.dp)
            .clip(CircleShape.copy(all = CornerSize(4.dp)))
            .background(placeCellColor(state))
            .clickable(onClick = onClick)
    ) {
        Image(
            painter = painterResource(placeCellGraphics(state)),
            contentDescription = null,
            modifier = Modifier
                .fillMaxSize()
        )
    }
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
fun placeCellGraphics(state: CellState): Int {
    return when (state) {
        CellState.Ship -> R.drawable.ship
        CellState.ShipHit -> R.drawable.hit
        CellState.Empty -> R.drawable.empty
        CellState.EmptyHit -> R.drawable.emptyhit
    }
}

@Composable
fun GameCell(state: CellState, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(35.dp)
            .padding(2.dp)
            .clip(CircleShape.copy(all = CornerSize(4.dp)))
            .background(GameCellColor(state))
            .clickable(onClick = onClick)
    ) {
        Image(
            painter = painterResource(GameCellGraphics(state)),
            contentDescription = null,
            modifier = Modifier
                .fillMaxSize()
        )
    }
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

@Composable
fun GameCellGraphics(state: CellState): Int {
    return when (state) {
        CellState.Ship -> R.drawable.empty
        CellState.ShipHit -> R.drawable.hit
        CellState.Empty -> R.drawable.empty
        CellState.EmptyHit -> R.drawable.emptyhit
    }
}

enum class CellState {
    Empty,
    EmptyHit,
    Ship,
    ShipHit
}

@Composable
fun YouWinScreen(navController: NavController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Display a "You Win" message
        Text(
            text = "You Win!",
            style = MaterialTheme.typography.displayLarge,
            color = MaterialTheme.colorScheme.primary
        )

        Button(
            onClick = { navController.navigate("PLAYER") },
            modifier = Modifier
                .padding(16.dp)
        ) {
            Text(text = "Play Again")
        }

    }
}

@Composable
fun YouLooseScreen(navController: NavController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Display a "You Win" message
        Text(
            text = "You Loose!",
            style = MaterialTheme.typography.displayLarge,
            color = MaterialTheme.colorScheme.primary
        )

        Button(
            onClick = { navController.navigate("PLAYER") },
            modifier = Modifier
                .padding(16.dp)
        ) {
            Text(text = "Play Again")
        }

    }
}

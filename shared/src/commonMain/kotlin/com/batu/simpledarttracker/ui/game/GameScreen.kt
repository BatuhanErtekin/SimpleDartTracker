package com.batu.simpledarttracker.ui.game

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.batu.simpledarttracker.domain.engine.X01Engine
import com.batu.simpledarttracker.domain.model.GameConfig
import com.batu.simpledarttracker.domain.model.X01GameState
import com.batu.simpledarttracker.ui.common.BackIcon
import com.batu.simpledarttracker.ui.players.PlayersScreen
import org.jetbrains.compose.resources.stringResource
import simpledarttracker.shared.generated.resources.Res
import simpledarttracker.shared.generated.resources.cd_back
import simpledarttracker.shared.generated.resources.out_double
import simpledarttracker.shared.generated.resources.out_straight
import simpledarttracker.shared.generated.resources.rules_out_mode
import simpledarttracker.shared.generated.resources.x01_title

/**
 * Game page. Opens with the chosen [startingScore] and asks the remaining questions in
 * order: first the finish rule (double/straight out), then the players. Once both are
 * answered the engine creates the game and the interactive [GameBoard] takes over.
 */
@Composable
fun GameScreen(
    startingScore: Int,
    onExit: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var config by remember { mutableStateOf<GameConfig?>(null) }
    var gameState by remember { mutableStateOf<X01GameState?>(null) }

    val state = gameState
    val cfg = config
    when {
        state != null -> GameBoard(
            state = state,
            onStateChange = { gameState = it },
            onExit = onExit,
            modifier = modifier,
        )

        cfg == null -> OutModeStep(
            startingScore = startingScore,
            onBack = onExit,
            onSelected = { doubleOut ->
                config = GameConfig(startingScore = startingScore, doubleOut = doubleOut)
            },
            modifier = modifier,
        )

        else -> PlayersScreen(
            onStart = { players -> gameState = X01Engine.newGame(cfg, players) },
            onBack = { config = null }, // back to the finish-rule question
            modifier = modifier,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun OutModeStep(
    startingScore: Int,
    onBack: () -> Unit,
    onSelected: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("${stringResource(Res.string.x01_title)} · $startingScore") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        BackIcon(
                            color = MaterialTheme.colorScheme.onSurface,
                            contentDescription = stringResource(Res.string.cd_back),
                            modifier = Modifier.size(24.dp),
                        )
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = stringResource(Res.string.rules_out_mode),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            ChoiceCard(text = stringResource(Res.string.out_double), onClick = { onSelected(true) })
            ChoiceCard(text = stringResource(Res.string.out_straight), onClick = { onSelected(false) })
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ChoiceCard(text: String, onClick: () -> Unit) {
    Card(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Text(
            text = text,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.fillMaxWidth().padding(20.dp),
            textAlign = TextAlign.Center,
        )
    }
}

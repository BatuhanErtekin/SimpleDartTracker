package com.batu.simpledarttracker.ui.game

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import com.batu.simpledarttracker.ui.theme.Brand
import org.jetbrains.compose.resources.stringResource
import simpledarttracker.shared.generated.resources.Res
import simpledarttracker.shared.generated.resources.action_continue
import simpledarttracker.shared.generated.resources.action_home
import simpledarttracker.shared.generated.resources.game_continue_question
import simpledarttracker.shared.generated.resources.game_winner

/** Shown when a game finishes: play the same match again, or go back to the menu. */
@Composable
fun WinnerDialog(
    winnerName: String,
    onPlayAgain: () -> Unit,
    onHome: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = {},
        containerColor = Brand.Slate2,
        titleContentColor = Brand.Chalk,
        textContentColor = Brand.Chalk,
        confirmButton = {
            TextButton(
                onClick = onPlayAgain,
                colors = ButtonDefaults.textButtonColors(contentColor = Brand.Spruce),
            ) {
                Text(stringResource(Res.string.action_continue))
            }
        },
        dismissButton = {
            TextButton(
                onClick = onHome,
                colors = ButtonDefaults.textButtonColors(contentColor = Brand.Chalk),
            ) {
                Text(stringResource(Res.string.action_home))
            }
        },
        title = { Text("🎯 $winnerName ${stringResource(Res.string.game_winner)}") },
        text = { Text(stringResource(Res.string.game_continue_question)) },
    )
}

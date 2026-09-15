package com.batu.simpledarttracker.ui.game

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import com.batu.simpledarttracker.ui.theme.Brand
import org.jetbrains.compose.resources.stringResource
import simpledarttracker.shared.generated.resources.Res
import simpledarttracker.shared.generated.resources.action_cancel
import simpledarttracker.shared.generated.resources.action_leave
import simpledarttracker.shared.generated.resources.game_leave_message
import simpledarttracker.shared.generated.resources.game_leave_title

/**
 * Asks before abandoning a match in progress. Nothing is stored yet, so leaving really does
 * throw the game away — worth a confirmation.
 */
@Composable
fun LeaveGameDialog(
    onLeave: () -> Unit,
    onCancel: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onCancel,
        containerColor = Brand.Panel,
        titleContentColor = Brand.Chalk,
        textContentColor = Brand.Chalk,
        title = { Text(stringResource(Res.string.game_leave_title)) },
        text = { Text(stringResource(Res.string.game_leave_message)) },
        confirmButton = {
            TextButton(
                onClick = onLeave,
                colors = ButtonDefaults.textButtonColors(contentColor = Brand.Ember),
            ) {
                Text(stringResource(Res.string.action_leave))
            }
        },
        dismissButton = {
            TextButton(
                onClick = onCancel,
                colors = ButtonDefaults.textButtonColors(contentColor = Brand.Chalk),
            ) {
                Text(stringResource(Res.string.action_cancel))
            }
        },
    )
}

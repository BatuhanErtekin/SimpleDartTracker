package com.batu.simpledarttracker.ui.platform

import androidx.compose.runtime.Composable

/**
 * Keeps the screen awake for as long as this composable is in the tree.
 *
 * A scoring app is used with the phone on a table and a dart in your hand: the whole point is
 * that you are not touching it. Letting the display time out mid-leg and drop to the lock screen
 * means unlocking the phone between throws, which is exactly what the app is there to save you.
 *
 * The flag is released again when the composable leaves, so the screen behaves normally
 * everywhere else — including when the app goes to the background.
 */
@Composable
expect fun KeepScreenAwake()

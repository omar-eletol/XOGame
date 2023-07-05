/*
 * Copyright (c) 2022 Razeware LLC
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * Notwithstanding the foregoing, you may not use, copy, modify, merge, publish,
 * distribute, sublicense, create a derivative work, and/or sell copies of the
 * Software in any work that is designed, intended, or marketed for pedagogical or
 * instructional purposes related to programming, coding, application development,
 * or information technology.  Permission for such use, copying, modification,
 * merger, publication, distribution, sublicensing, creation of derivative works,
 * or sale is expressly withheld.
 *
 * This project and source code may use libraries or frameworks that are
 * released under various Open-Source licenses. Use of those libraries and
 * frameworks are governed by their own individual licenses.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package com.yourcompany.android.tictactoe.ui.screens

import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.yourcompany.android.tictactoe.domain.model.GameState
import com.yourcompany.android.tictactoe.viewmodel.TicTacToeViewModel

@Composable
fun GameScreen(viewModel: TicTacToeViewModel) {
  val state: GameState by viewModel.state.observeAsState(GameState.Uninitialized)

  BackHandler(onBack = {
    viewModel.goToHome()
  })

  if (state.isOver) {
    GameOverScreen(
      playerWon = state.playerWon,
      onNewGameClick = { viewModel.newGame() }
    )
  } else {
    OngoingGameScreen(
      localPlayer = state.localPlayer,
      playerTurn = state.playerTurn,
      board = state.board,
      onBucketClick = { position -> viewModel.play(position) }
    )
  }
}

@Composable
fun GameOverScreen(
  playerWon: Int,
  onNewGameClick: () -> Unit
) {
  Column(
    modifier = Modifier
      .fillMaxSize()
      .padding(16.dp),
    verticalArrangement = Arrangement.Center,
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    Text(text = "Game over")
    Text(
      text = if (playerWon > 0) "Player $playerWon won!" else "It's a tie!",
      fontWeight = FontWeight.Bold
    )
    Button(
      modifier = Modifier
        .fillMaxWidth()
        .padding(0.dp, 16.dp),
      onClick = onNewGameClick
    ) {
      Text(text = "New game!")
    }
  }
}

@Composable
fun OngoingGameScreen(
  localPlayer: Int,
  playerTurn: Int,
  board: List<List<Int>>,
  onBucketClick: (position: Pair<Int, Int>) -> Unit
) {
  Column(
    modifier = Modifier.fillMaxSize(),
    verticalArrangement = Arrangement.Center,
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    Row(verticalAlignment = Alignment.CenterVertically) {
      Text(text = "You're player $localPlayer")
      Box(
        modifier = Modifier
          .padding(4.dp, 0.dp)
          .size(10.dp)
          .background(color = getPlayerColor(localPlayer))
      )
    }
    Text(
      text = if (localPlayer == playerTurn) "Your turn!" else "Waiting for player $playerTurn...",
      fontWeight = FontWeight.Bold
    )
    Board(
      board = board,
      onBucketClick = { position -> onBucketClick(position) }
    )
  }
}

@Composable
fun Board(
  board: List<List<Int>>,
  onBucketClick: (position: Pair<Int, Int>) -> Unit
) {
  Row(
    modifier = Modifier.fillMaxSize()
  ) {
    for (i in board.indices) {
      Column(modifier = Modifier.weight(1f)) {
        for (j in board.indices) {
          Bucket(
            modifier = Modifier
              .fillMaxSize()
              .weight(1f),
            player = board[i][j],
            onClick = { onBucketClick(i to j) }
          )
        }
      }
    }
  }
}

@Composable
fun Bucket(
  modifier: Modifier,
  player: Int,
  onClick: () -> Unit
) {
  OutlinedButton(
    modifier = modifier,
    colors = ButtonDefaults.buttonColors(getPlayerColor(player)),
    onClick = onClick
  ) {}
}

private fun getPlayerColor(player: Int): Color {
  Log.e("TicTacToeVM", "player: " + player )
  return when (player) {
    0 -> Color.White
    1 -> Color.Red
    2 -> Color.Green
    else -> throw IllegalArgumentException("Missing color for player $player")
  }
}

@Preview
@Composable
fun GameOverPlayerWonScreenPreview() {
  GameOverScreen(playerWon = 1, onNewGameClick = {})
}

@Preview
@Composable
fun GameOverTieScreenPreview() {
  GameOverScreen(playerWon = 0, onNewGameClick = {})
}

@Preview
@Composable
fun OngoingGameScreenPreview() {
  OngoingGameScreen(
    localPlayer = 1,
    playerTurn = 2,
    board = listOf(listOf(0, 0, 0), listOf(0, 0, 0), listOf(0, 0, 0)),
    onBucketClick = {}
  )
}
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

package com.yourcompany.android.tictactoe.domain.model

class TicTacToe(private val players: Int = 2, private val boardSize: Int = 3, private val winningCount: Int = 3) {
  var playerTurn = 1
    private set

  var playerWon = 0
    private set

  var isOver = false
    private set

  val board = List(boardSize) {
    MutableList(boardSize) { 0 }
  }

  fun play(player: Int, position: Pair<Int, Int>) {
    if (player < 1 || player > players) throw IllegalArgumentException("Invalid player")
    if (player != playerTurn) throw IllegalArgumentException("Wrong turn")
    if (isOver) throw IllegalArgumentException("Game over")
    if (board[position.first][position.second] > 0) throw IllegalArgumentException("Already played")

    board[position.first][position.second] = player
    if (hasPlayerWon(playerTurn)) {
      playerWon = playerTurn
      isOver = true
    } else if (!anyNonPlayedBucket()) {
      isOver = true
    }
    playerTurn = (player % players) + 1
  }

  private fun anyNonPlayedBucket() = board.flatten().any { it == 0 }

  fun isPlayedBucket(position: Pair<Int, Int>) = board[position.first][position.second] != 0

  private fun hasPlayerWon(player: Int): Boolean {
    if (player < 1 || player > players) return false

    var count: Int
    for (i in 0 until boardSize) {
      for (j in 0 until boardSize) {
        if (board[i][j] == player) {
          // Check horizontal
          if (j + winningCount <= boardSize) {
            count = 1
            for (J in j + 1 until j + winningCount) {
              if (board[i][J] != player) break
              count++
              if (count == winningCount) return true
            }
          }

          // Check vertical
          if (i + winningCount <= boardSize) {
            count = 1
            for (I in i + 1 until i + winningCount) {
              if (board[I][j] != player) break
              count++
              if (count == winningCount) return true
            }
          }

          // Check diagonal 1
          if (j + winningCount <= boardSize && i + winningCount <= boardSize) {
            count = 1
            for ((I, J) in (i + 1 until i + winningCount).zip(j + 1 until j + winningCount)) {
              if (board[I][J] != player) break
              count++
              if (count == winningCount) return true
            }
          }

          // Check diagonal 2
          if (j - winningCount + 1 >= 0 && i + winningCount <= boardSize) {
            count = 1
            for ((I, J) in (i + 1 until i + winningCount).zip(j - 1 downTo 0)) {
              if (board[I][J] != player) break
              count++
              if (count == winningCount) return true
            }
          }
        }
      }
    }

    return false
  }

  override fun toString(): String {
    return board.joinToString("\n") { it.joinToString(",") }
  }
}
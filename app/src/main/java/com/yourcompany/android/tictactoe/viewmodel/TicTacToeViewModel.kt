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

package com.yourcompany.android.tictactoe.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.gms.nearby.connection.AdvertisingOptions
import com.google.android.gms.nearby.connection.ConnectionInfo
import com.google.android.gms.nearby.connection.ConnectionLifecycleCallback
import com.google.android.gms.nearby.connection.ConnectionResolution
import com.google.android.gms.nearby.connection.ConnectionsClient
import com.google.android.gms.nearby.connection.ConnectionsStatusCodes
import com.google.android.gms.nearby.connection.DiscoveredEndpointInfo
import com.google.android.gms.nearby.connection.DiscoveryOptions
import com.google.android.gms.nearby.connection.EndpointDiscoveryCallback
import com.google.android.gms.nearby.connection.Payload
import com.google.android.gms.nearby.connection.PayloadCallback
import com.google.android.gms.nearby.connection.PayloadTransferUpdate
import com.google.android.gms.nearby.connection.Strategy
import com.yourcompany.android.tictactoe.BuildConfig
import com.yourcompany.android.tictactoe.domain.model.GameState
import com.yourcompany.android.tictactoe.domain.model.TicTacToe
import com.yourcompany.android.tictactoe.routing.Screen
import com.yourcompany.android.tictactoe.routing.TicTacToeRouter
import java.util.*
import kotlin.text.Charsets.UTF_8

class TicTacToeViewModel(private val connectionsClient: ConnectionsClient) : ViewModel() {
  private val localUsername = UUID.randomUUID().toString()
  private var localPlayer: Int = 0
  private var opponentPlayer: Int = 0
  private var opponentEndpointId: String = ""

  private var game = TicTacToe()

  private val _state = MutableLiveData(GameState.Uninitialized)
  val state: LiveData<GameState> = _state

  private val payloadCallback: PayloadCallback = object : PayloadCallback() {
    override fun onPayloadReceived(endpointId: String, payload: Payload) {
      Log.d(TAG, "onPayloadReceived")

      if (payload.type == Payload.Type.BYTES) {
        val position = payload.toPosition()
        Log.d(TAG, "Received [${position.first},${position.second}] from $endpointId")
        play(opponentPlayer, position)
      }
    }

    override fun onPayloadTransferUpdate(endpointId: String, update: PayloadTransferUpdate) {
      Log.d(TAG, "onPayloadTransferUpdate")
    }
  }

  private val endpointDiscoveryCallback = object : EndpointDiscoveryCallback() {
    override fun onEndpointFound(endpointId: String, info: DiscoveredEndpointInfo) {
      Log.d(TAG, "onEndpointFound")

      Log.d(TAG, "Requesting connection...")
      connectionsClient.requestConnection(
        localUsername,
        endpointId,
        connectionLifecycleCallback
      ).addOnSuccessListener {
        Log.d(TAG, "Successfully requested a connection")
      }.addOnFailureListener {
        Log.d(TAG, "Failed to request the connection")
      }
    }

    override fun onEndpointLost(endpointId: String) {
      Log.d(TAG, "onEndpointLost")
    }
  }

  private val connectionLifecycleCallback = object : ConnectionLifecycleCallback() {
    override fun onConnectionInitiated(endpointId: String, info: ConnectionInfo) {
      Log.d(TAG, "onConnectionInitiated")

      Log.d(TAG, "Accepting connection...")
      connectionsClient.acceptConnection(endpointId, payloadCallback)
    }

    override fun onConnectionResult(endpointId: String, resolution: ConnectionResolution) {
      Log.d(TAG, "onConnectionResult")

      when (resolution.status.statusCode) {
        ConnectionsStatusCodes.STATUS_OK -> {
          Log.d(TAG, "ConnectionsStatusCodes.STATUS_OK")

          connectionsClient.stopAdvertising()
          connectionsClient.stopDiscovery()
          opponentEndpointId = endpointId
          Log.d(TAG, "opponentEndpointId: $opponentEndpointId")
          newGame()
          TicTacToeRouter.navigateTo(Screen.Game)
        }
        ConnectionsStatusCodes.STATUS_CONNECTION_REJECTED -> {
          Log.d(TAG, "ConnectionsStatusCodes.STATUS_CONNECTION_REJECTED")
        }
        ConnectionsStatusCodes.STATUS_ERROR -> {
          Log.d(TAG, "ConnectionsStatusCodes.STATUS_ERROR")
        }
        else -> {
          Log.d(TAG, "Unknown status code ${resolution.status.statusCode}")
        }
      }
    }

    override fun onDisconnected(endpointId: String) {
      Log.d(TAG, "onDisconnected")
      goToHome()
    }
  }

  fun startHosting() {
    Log.d(TAG, "Start advertising...")
    TicTacToeRouter.navigateTo(Screen.Hosting)
    val advertisingOptions = AdvertisingOptions.Builder().setStrategy(STRATEGY).build()

    connectionsClient.startAdvertising(
      localUsername,
      BuildConfig.APPLICATION_ID,
      connectionLifecycleCallback,
      advertisingOptions
    ).addOnSuccessListener {
      Log.d(TAG, "Advertising...")
      localPlayer = 1
      opponentPlayer = 2
    }.addOnFailureListener {
      Log.d(TAG, "Unable to start advertising")
      TicTacToeRouter.navigateTo(Screen.Home)
    }
  }

  fun startDiscovering() {
    Log.d(TAG, "Start discovering...")
    TicTacToeRouter.navigateTo(Screen.Discovering)
    val discoveryOptions = DiscoveryOptions.Builder().setStrategy(STRATEGY).build()

    connectionsClient.startDiscovery(
      BuildConfig.APPLICATION_ID,
      endpointDiscoveryCallback,
      discoveryOptions
    ).addOnSuccessListener {
      Log.d(TAG, "Discovering...")
      localPlayer = 2
      opponentPlayer = 1
    }.addOnFailureListener {
      Log.d(TAG, "Unable to start discovering")
      TicTacToeRouter.navigateTo(Screen.Home)
    }
  }

  fun newGame() {
    Log.d(TAG, "Starting new game")
    game = TicTacToe()
    _state.value = GameState(localPlayer, game.playerTurn, game.playerWon, game.isOver, game.board)
  }

  fun play(position: Pair<Int, Int>) {
    if (game.playerTurn != localPlayer) return
    if (game.isPlayedBucket(position)) return

    play(localPlayer, position)
    sendPosition(position)
  }

  private fun play(player: Int, position: Pair<Int, Int>) {
    Log.d(TAG, "Player $player played [${position.first},${position.second}]")

    game.play(player, position)
    _state.value = GameState(localPlayer, game.playerTurn, game.playerWon, game.isOver, game.board)
  }

  private fun sendPosition(position: Pair<Int, Int>) {
    Log.d(TAG, "Sending [${position.first},${position.second}] to $opponentEndpointId")
    connectionsClient.sendPayload(
      opponentEndpointId,
      position.toPayLoad()
    )
  }

  override fun onCleared() {
    stopClient()
    super.onCleared()
  }

  fun goToHome() {
    stopClient()
    TicTacToeRouter.navigateTo(Screen.Home)
  }

  private fun stopClient() {
    Log.d(TAG, "Stop advertising, discovering, all endpoints")
    connectionsClient.stopAdvertising()
    connectionsClient.stopDiscovery()
    connectionsClient.stopAllEndpoints()
    localPlayer = 0
    opponentPlayer = 0
    opponentEndpointId = ""
  }

  private companion object {
    const val TAG = "TicTacToeVM"
    val STRATEGY = Strategy.P2P_POINT_TO_POINT
  }
}

fun Pair<Int, Int>.toPayLoad() = Payload.fromBytes("$first,$second".toByteArray(UTF_8))

fun Payload.toPosition(): Pair<Int, Int> {
  val positionStr = String(asBytes()!!, UTF_8)
  val positionArray = positionStr.split(",")
  return positionArray[0].toInt() to positionArray[1].toInt()
}
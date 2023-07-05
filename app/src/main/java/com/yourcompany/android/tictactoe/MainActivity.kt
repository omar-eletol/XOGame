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

package com.yourcompany.android.tictactoe

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.core.content.ContextCompat
import com.google.android.gms.nearby.Nearby
import com.yourcompany.android.tictactoe.routing.Screen
import com.yourcompany.android.tictactoe.routing.TicTacToeRouter
import com.yourcompany.android.tictactoe.ui.screens.DiscoveringScreen
import com.yourcompany.android.tictactoe.ui.screens.GameScreen
import com.yourcompany.android.tictactoe.ui.screens.HomeScreen
import com.yourcompany.android.tictactoe.ui.screens.HostingScreen
import com.yourcompany.android.tictactoe.ui.theme.TicTacToeTheme
import com.yourcompany.android.tictactoe.viewmodel.TicTacToeViewModel
import com.yourcompany.android.tictactoe.viewmodel.TicTacToeViewModelFactory

class MainActivity : ComponentActivity() {

  private val viewModel: TicTacToeViewModel by viewModels {
    TicTacToeViewModelFactory(Nearby.getConnectionsClient(applicationContext))
  }

  private val requestMultiplePermissions = registerForActivityResult(
    ActivityResultContracts.RequestMultiplePermissions()
  ) { permissions ->
    if (permissions.entries.any { !it.value }) {
      Toast.makeText(this, "Required permissions needed", Toast.LENGTH_LONG).show()
      finish()
    } else {
      recreate()
    }
  }

  override fun onStart() {
    super.onStart()
    if (!hasPermissions(this, REQUIRED_PERMISSIONS)) {
      requestMultiplePermissions.launch(
        REQUIRED_PERMISSIONS
      )
    }
  }

  private fun hasPermissions(context: Context, permissions: Array<String>): Boolean {
    return permissions.isEmpty() || permissions.all {
      ContextCompat.checkSelfPermission(
        context,
        it
      ) == PackageManager.PERMISSION_GRANTED
    }
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    setContent {
      TicTacToeTheme {
        MainActivityScreen()
      }
    }
  }

  @Composable
  private fun MainActivityScreen() {
    Surface {
      when (TicTacToeRouter.currentScreen) {
        is Screen.Home -> HomeScreen(viewModel)
        is Screen.Hosting -> HostingScreen(viewModel)
        is Screen.Discovering -> DiscoveringScreen(viewModel)
        is Screen.Game -> GameScreen(viewModel)
      }
    }
  }

  private companion object {
    val REQUIRED_PERMISSIONS =
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        arrayOf(
          Manifest.permission.BLUETOOTH_SCAN,
          Manifest.permission.BLUETOOTH_ADVERTISE,
          Manifest.permission.BLUETOOTH_CONNECT,
          Manifest.permission.ACCESS_FINE_LOCATION
        )
      } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
      } else {
        arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION)
      }
  }
}
package com.hoc081098.demo_lazylayout_compose

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Stable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.hoc081098.demo_lazylayout_compose.demo.Timetable
import com.hoc081098.demo_lazylayout_compose.ui.theme.DemoLazyLayoutComposeTheme
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import java.time.temporal.ChronoUnit

@Stable
private val Colors: ImmutableList<Color> = persistentListOf(
  Color.Red,
  Color.Green,
  Color.Blue,
  Color.Yellow,
  Color.Magenta,
  Color.Cyan,
  Color(0xFF26A69A),
  Color(0xFFFFCA28),
  Color(0xFFD4E157),
)

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    val timetableEventList = getTimetableEventItemsList()


    setContent {
      DemoLazyLayoutComposeTheme(
        dynamicColor = false,
      ) {
        // A surface container using the 'background' color from the theme
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
          Scaffold(
            topBar = {
              CenterAlignedTopAppBar(
                title = {
                  Text(text = "Timetable")
                }
              )
            },
          ) { padding ->
            val shape = RoundedCornerShape(size = 12.dp)

            Timetable(
              modifier = Modifier
                .consumeWindowInsets(padding)
                .padding(padding)
                .fillMaxSize(),
              timetableEventList = timetableEventList
            ) { index, item ->
              Column(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(8.dp)
                  .border(
                    color = Colors[index % Colors.size],
                    width = 2.dp,
                    shape = shape,
                  )
                  .background(
                    color = Colors[index % Colors.size].copy(alpha = 0.2f),
                    shape = shape,
                  )
                  .padding(8.dp),
              ) {
                Text(
                  modifier = Modifier
                    .fillMaxWidth(),
                  text = item.title,
                  style = MaterialTheme.typography.titleLarge,
                )

                Spacer(modifier = Modifier.height(12.dp))

                val duration = ChronoUnit.MINUTES.between(item.startsAt, item.endsAt)
                Text(
                  modifier = Modifier
                    .fillMaxWidth(),
                  text = "Duration: $duration minutes",
                  style = MaterialTheme.typography.bodyMedium
                )
              }
            }
          }
        }
      }
    }
  }
}

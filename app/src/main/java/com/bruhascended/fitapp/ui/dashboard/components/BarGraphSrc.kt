package com.bruhascended.fitapp.ui.dashboard.components

import android.content.Context
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupPositionProvider
import com.bruhascended.fitapp.util.BarGraphData
import java.text.DateFormat

val diff = 1000f // defining height of bar after goal

@Composable
fun BarGraph(data: List<BarGraphData>, context: Context, unit: String?, goal: Long) {
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .drawBehind {
                val height = 60.dp.toPx()
                val pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                var goalHeight = goal / (goal + diff)
                goalHeight = if (goalHeight <= 1f) (1f - goalHeight) * height
                else height

                drawLine(
                    color = Color.LightGray,
                    strokeWidth = 4f,
                    start = Offset(0f, goalHeight),
                    end = Offset(size.width, goalHeight),
                    pathEffect = pathEffect
                )
            },
        horizontalArrangement = Arrangement.Center
    ) {
        items(items = data, itemContent = { item ->
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Bar(item, context = context, unit ?: "", goal)
                Text(text = item.x, fontSize = 12.sp)
            }
        })
    }
}


@Composable
fun Bar(item: BarGraphData, context: Context, unit: String, goal: Long) {
    var offset by remember {
        mutableStateOf(Offset(x = 0f, y = 0f))
    }

    var height by remember {
        mutableStateOf(0f)
    }

    val perc = item.height / (goal + diff)

    val y by animateFloatAsState(
        targetValue = height, animationSpec = tween(500)
    )

    var popUpShown by remember {
        mutableStateOf(false)
    }
    if (popUpShown) {
        Popup(
            onDismissRequest = { popUpShown = false },
            popupPositionProvider = popUpPosProvider(offset)
        ) {

            Card(
                elevation = 6.dp,
                backgroundColor = MaterialTheme.colors.primaryVariant,
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(Modifier.padding(vertical = 8.dp, horizontal = 16.dp)) {
                    val dateFormat =
                        DateFormat.getDateInstance().format(item.startTime)
                    Text(
                        text = "${item.height.toInt()} $unit on $dateFormat",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }

    Canvas(modifier = Modifier
        .size(
            width = 32.dp,
            height = 60.dp
        )
        .onGloballyPositioned { coordinates ->
            offset = coordinates.positionInWindow()
        }
        .pointerInput(Unit) {
            detectTapGestures(
                onPress = {
                    popUpShown = true
                    popUpShown = try {
                        this.awaitRelease()
                        false
                    } catch (e: GestureCancellationException) {
                        false
                    }
                }
            )
        },
        onDraw = {
            height = if (perc <= 1f) perc * size.height
            else size.height
            drawRect(
                color = Color.Blue,
                size = Size(width = size.width / 2f, height = y),
                topLeft = Offset(
                    x = size.width / 4f,
                    y = size.height - y
                )
            )
            if (popUpShown) {
                val pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                drawLine(
                    color = Color.LightGray,
                    strokeWidth = 8f,
                    start = Offset(size.width / 2f, 0f),
                    end = Offset(size.width / 2f, size.height),
                    pathEffect = pathEffect
                )
            }
        })
}

fun popUpPosProvider(offset: Offset): PopupPositionProvider {
    return object : PopupPositionProvider {
        override fun calculatePosition(
            anchorBounds: IntRect,
            windowSize: IntSize,
            layoutDirection: LayoutDirection,
            popupContentSize: IntSize
        ): IntOffset {
            return IntOffset(
                x = offset.x.toInt() - (popupContentSize.width / 2),
                y = offset.y.toInt() - popupContentSize.height
            )
        }
    }
}
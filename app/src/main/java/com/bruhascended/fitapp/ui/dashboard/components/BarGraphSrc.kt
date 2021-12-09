package com.bruhascended.fitapp.ui.dashboard.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun BarGraph(height: Float) {
    LazyRow(
        modifier = Modifier
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        items(7, itemContent = {
            Bar(height)
        })
    }
}


@Composable
fun Bar(height: Float = 0f) {
    Column(
        modifier = Modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {


        val y by animateFloatAsState(
            targetValue = height, animationSpec = tween(500)
        )

        Box(modifier = Modifier
            .size(
                width = 16.dp,
                height = 60.dp
            ) // this is a fixed height, all values must scale
            .clip(RoundedCornerShape(topStart = 2.dp, topEnd = 2.dp))
            .drawBehind {
                drawRect(
                    color = Color.Blue,
                    size = Size(width = size.width, height = y),
                    topLeft = Offset(
                        x = 0f,
                        y = size.height - y
                    )
                )
            })

        Text(text = "M", fontSize = 12.sp)
    }
}

@Composable
@Preview(showBackground = true)
fun BarGraphPreview() {
    BarGraph(50f)
}
package com.bruhascended.fitapp.ui.dashboard.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bruhascended.fitapp.R

@Composable
fun OverViewCard(value: Float) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(Color.White)
            .padding(16.dp)
    ) {
        FirstRow("Steps")

        Text(
            text = "Last 7 days",
            fontSize = 12.sp,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        ThirdRow(value)
    }
    Spacer(modifier = Modifier.size(24.dp))
}

@Composable
fun FirstRow(title: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = title,
            fontWeight = FontWeight.Medium,
            style = MaterialTheme.typography.subtitle1
        )
        Image(
            painter = painterResource(id = R.drawable.ic_arrow_forward),
            contentDescription = "Click Here",
            Modifier.size(14.dp)
        )
    }
}

@Composable
fun ThirdRow(value: Float) {
    Row(
        modifier = Modifier
            .fillMaxWidth(),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        FirstOfThirdRow()
        BarGraph(height = value)
    }
}

@Composable
fun FirstOfThirdRow() {
    Column(modifier = Modifier.fillMaxWidth(0.25f)) {
        Text(
            text = "127",
            style = MaterialTheme.typography.h5,
            fontWeight = FontWeight.Medium
        )

        Text(
            text = "Today",
            fontSize = 12.sp
        )
    }
}

@Composable
@Preview(showBackground = true)
fun OverViewCardPreview() {
    OverViewCard(50f)
}
package com.bruhascended.fitapp.ui.dashboard.components

import android.content.Context
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bruhascended.fitapp.R
import com.bruhascended.fitapp.util.BarGraphData

@Composable
fun OverViewCard(
    data: List<BarGraphData>,
    context: Context,
    s: String,
    unit: String?,
    repo: Long
) {
    Card(
        elevation = 6.dp,
        backgroundColor = Color.White,
        shape = RoundedCornerShape(10.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            FirstRow(s)

            Text(
                text = "Last 7 days",
                fontSize = 12.sp,
                modifier = Modifier.padding(bottom = 18.dp)
            )

            ThirdRow(data, context, unit,repo)
        }
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
fun ThirdRow(data: List<BarGraphData>, context: Context, unit: String?, goal: Long) {
    Row(
        modifier = Modifier
            .fillMaxWidth(),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        FirstOfThirdRow(data, unit)
        BarGraph(data, context,unit,goal)
    }
}

@Composable
fun FirstOfThirdRow(data: List<BarGraphData>, unit: String?) {
    Column(modifier = Modifier.fillMaxWidth(0.25f)) {
        Row {
            Text(
                modifier = Modifier
                    .padding(end = 2.dp)
                    .alignByBaseline(),
                text = data.last().height.toInt().toString(),
                style = MaterialTheme.typography.h5,
                fontWeight = FontWeight.Medium
            )
            if (unit != null) {
                Text(
                    modifier = Modifier.alignByBaseline(),
                    text = unit,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        Text(
            text = "Today",
            fontSize = 12.sp
        )
    }
}

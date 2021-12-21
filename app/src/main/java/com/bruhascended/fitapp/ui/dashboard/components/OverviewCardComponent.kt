package com.bruhascended.fitapp.ui.dashboard.components

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
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
    repo: Long,
    color: Color
) {
    Card(
        elevation = 6.dp,
        backgroundColor = MaterialTheme.colors.surface,
        shape = RoundedCornerShape(10.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            FirstRow(s)

            Text(
                color = MaterialTheme.colors.onSurface,
                text = "Last 7 days",
                fontSize = 12.sp,
                modifier = Modifier.padding(bottom = 18.dp)
            )

            ThirdRow(data, context, unit, repo,color)
        }
    }
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
            fontWeight = FontWeight.SemiBold,
            style = MaterialTheme.typography.subtitle1, color = MaterialTheme.colors.onSurface
        )
        Icon(
            tint = MaterialTheme.colors.onSurface,
            painter = painterResource(id = R.drawable.ic_arrow_forward),
            contentDescription = "Click Here",
            modifier = Modifier.size(12.dp)
        )
    }
}

@Composable
fun ThirdRow(data: List<BarGraphData>, context: Context, unit: String?, goal: Long,color:Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth(),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        FirstOfThirdRow(data, unit)
        BarGraph(data, context, unit, goal,color)
    }
}

@Composable
fun FirstOfThirdRow(data: List<BarGraphData>, unit: String?) {
    Column(modifier = Modifier.fillMaxWidth(0.25f)) {
        Row {
            Text(
                color = MaterialTheme.colors.onSurface,
                modifier = Modifier
                    .padding(end = 2.dp)
                    .alignByBaseline(),
                text = data.last().height.toInt().toString(),
                style = MaterialTheme.typography.h6,
                fontWeight = FontWeight.Medium
            )
            if (unit != null) {
                Text(
                    color = MaterialTheme.colors.onSurface,
                    modifier = Modifier.alignByBaseline(),
                    text = unit,
                    fontSize = 12.sp
                )
            }
        }

        Text(
            color = MaterialTheme.colors.onSurface,
            text = "Today",
            fontSize = 12.sp
        )
    }
}

package com.bruhascended.fitapp.ui.dashboard.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bruhascended.db.food.entities.DayEntry
import com.bruhascended.db.food.types.NutrientType
import com.bruhascended.fitapp.ui.theme.Blue100
import com.bruhascended.fitapp.ui.theme.Purple200
import com.bruhascended.fitapp.ui.theme.Yellow500
import kotlin.math.ceil
import kotlin.math.floor

@Composable
fun NutrientCard(todayNutrientData: DayEntry) {
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
            Text(
                text = "Nutrient Intake",
                fontWeight = FontWeight.SemiBold,
                style = MaterialTheme.typography.subtitle1, color = MaterialTheme.colors.onSurface
            )
            Text(
                color = MaterialTheme.colors.onSurface,
                text = "Today",
                fontSize = 12.sp,
                modifier = Modifier.padding(bottom = 18.dp)
            )
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                NutrientItem(
                    "Protein",
                    dataFormatter(todayNutrientData.nutrientInfo[NutrientType.Protein]),
                    Purple200
                )
                NutrientItem(
                    "Carbs",
                    dataFormatter(todayNutrientData.nutrientInfo[NutrientType.Carbs]),
                    Yellow500
                )
                NutrientItem(
                    "Fat",
                    dataFormatter(todayNutrientData.nutrientInfo[NutrientType.Fat]),
                    Blue100
                )
            }
        }
    }
}

@Composable
fun NutrientItem(text: String, value: String,color:Color) {
    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            color = MaterialTheme.colors.onSurface,
            text = text,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Text(
            color = color,
            text = "$value g",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 18.dp)
        )
    }
}

fun dataFormatter(value: Double?): String {
    return if (value == null) "0"
    else {
        if (ceil(value) == floor(value)) {
            return value.toInt().toString()
        } else
            String.format("%.2f", value)
    }
}
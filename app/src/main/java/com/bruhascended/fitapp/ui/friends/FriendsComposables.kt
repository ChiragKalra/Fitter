package com.bruhascended.fitapp.ui.friends

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bruhascended.db.food.types.QuantityType.Companion.doubleToString
import com.bruhascended.fitapp.R
import com.bruhascended.fitapp.ui.friends.types.FriendStatistics
import com.bruhascended.fitapp.ui.friends.types.Statistic
import com.bruhascended.fitapp.ui.friends.types.TimePeriod


@Composable
fun TimePeriodTabRow(
	timePeriod: TimePeriod,
	onUpdate: (timePeriod: TimePeriod) -> Unit
) {
	TabRow(
		selectedTabIndex = TimePeriod.values().indexOf(timePeriod)
	) {
		TimePeriod.values().forEach {
			Tab(
				selected = it == timePeriod,
				onClick = { onUpdate(it) },
				modifier = Modifier
					.background(MaterialTheme.colors.background)
			) {
				Text(
					text = stringResource(it.stringRes),
					fontSize = 24.sp,
					modifier = Modifier
						.padding(vertical = 12.dp),
				)
			}
		}
	}
}

@Composable
fun StatisticTypeRow(
	statistic: Statistic,
	onUpdate: (statistic: Statistic) -> Unit
) {
	Row (
		verticalAlignment = Alignment.CenterVertically,
		horizontalArrangement = Arrangement.Center,
		modifier = Modifier
			.padding(12.dp)
			.fillMaxWidth()
	) {
		Text(
			text = stringResource(R.string.sort_by),
			fontSize = 18.sp,
			fontWeight = FontWeight.ExtraLight,
			modifier = Modifier
				.padding(horizontal = 32.dp, vertical = 12.dp),
			color = MaterialTheme.colors.onSurface
		)
		Statistic.values().forEach {
			Button(
				onClick = { onUpdate(it) },
				modifier = Modifier
					.padding(4.dp),
				shape = RoundedCornerShape(100),
				colors = ButtonDefaults.buttonColors(
					backgroundColor = if (statistic == it) MaterialTheme.colors.secondaryVariant
											else MaterialTheme.colors.secondary,
					contentColor = MaterialTheme.colors.primaryVariant
				)
			) {
				Text(
					text = stringResource(it.stringRes),
					fontSize = 12.sp,
				)
			}
		}
	}
}

@Composable
fun FriendStatisticsRow(
	rank: Int,
	statistics: FriendStatistics,
	statistic: Statistic
) {
	Row(
		modifier = Modifier.padding(horizontal = 18.dp, vertical = 10.dp),
		verticalAlignment = Alignment.CenterVertically,
	) {
		Rank(rank)
		Text(
			text = statistics.username,
			modifier = Modifier
				.weight(1f)
				.padding(horizontal = 28.dp),
			fontSize = 16.sp,
			fontWeight = FontWeight.SemiBold,
			color = MaterialTheme.colors.onSurface,
		)
		StatisticText(statistic, statistics)
	}
}

@Composable
fun StatisticText(statistic: Statistic, statistics: FriendStatistics) {
	val context = LocalContext.current
	Text(
		text = when(statistic) {
			Statistic.DistanceCovered -> {
				context.getString(
					com.bruhascended.db.R.string.distance_km_count,
					doubleToString(statistics.totalDistance.toDouble())
				)
			}
			Statistic.StepsTaken -> {
				context.getString(
					com.bruhascended.db.R.string.steps_count,
					statistics.totalSteps.toString()
				)
			}
			Statistic.TimeActive -> {
				context.getString(
					com.bruhascended.db.R.string.move_min_count,
					statistics.totalDuration.toString()
				)
			}
		},
		modifier = Modifier
			.padding(end = 36.dp),
		fontSize = 16.sp,
		color = MaterialTheme.colors.onSurface,
	)
}

@Composable
fun Rank(rank: Int) {
	val rankInfo = arrayOf(
		R.drawable.rank_gold to R.string.first_rank,
		R.drawable.rank_silver to R.string.second_rank,
		R.drawable.rank_bronze to R.string.third_rank,
	)
	if (rank <= rankInfo.size) {
		Image(
			painter = painterResource(rankInfo[rank - 1].first),
			contentDescription = stringResource(rankInfo[rank - 1].second),
			modifier = Modifier
				.size(48.dp)
		)
	} else {
		Text(
			text = rank.toString(),
			modifier = Modifier
				.size(48.dp)
				.padding(12.dp),
			textAlign = TextAlign.Center,
			fontSize = 24.sp,
			fontWeight = FontWeight.Black,
			color = MaterialTheme.colors.secondaryVariant
		)
	}
}

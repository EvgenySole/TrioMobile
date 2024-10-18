package com.example.testandro6

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import co.yml.charts.axis.AxisData
import co.yml.charts.common.model.Point
import co.yml.charts.ui.linechart.LineChart
import co.yml.charts.ui.linechart.model.*
import java.time.LocalDateTime

class ChartDraw {

    @Preview
    @SuppressLint("SuspiciousIndentation")
    @Composable
    fun LineChartScreen() {
        var xPos = 0f
        var time = LocalDateTime.now().minusMinutes(10080)
        var pointsData = ArrayList<Point>()
        var datesData = ArrayList<String>()
        for (i in 0..2016){
            var timeTarget = time.plusMinutes(i*5.toLong()).toString().split('T')
                .get(1).split('.').get(0)
            var dateTarget = time.plusMinutes(i*5.toLong()).toString().split('T')
                .get(0).split('-').get(2) + "." +
                    time.plusMinutes(i*5.toLong()).toString().split('T').get(0).split('-').get(1)
            datesData.add(timeTarget + "\n" + dateTarget)
        }

        for (i in 1..2016 ){
            val randomYFloat = (1..100).random().toFloat()
            pointsData.add(Point(x = xPos, y = randomYFloat))
            xPos += 1f
        }
        val steps = 5
        val xAxisData = AxisData.Builder()
            .axisStepSize(10.dp)
            .backgroundColor(Color.White)
            .steps(1)

            .labelAndAxisLinePadding(15.dp)
            .build()

        val yAxisData = AxisData.Builder()
            .steps(steps)
            .backgroundColor(Color.White)
            .labelAndAxisLinePadding(20.dp)
            .labelData { i ->
                val yScale = 100 / steps
                (i * yScale).toString()
            }.build()

        val lineChartData = LineChartData(
            linePlotData = LinePlotData(
                lines = listOf(
                    Line(
                        dataPoints = pointsData,
                        LineStyle(color = colorResource(R.color.blue_trio_light)),
                        IntersectionPoint(radius = 0.dp),
                        SelectionHighlightPoint(color = colorResource(R.color.purple_trio)),
                        ShadowUnderLine(color = colorResource(R.color.blue_trio_dark)),
                        SelectionHighlightPopUp(popUpLabel = { x, y ->
                            "Нагрузка ${y}%\nДата ${datesData[x.toInt()]}"
                        }
                        )
                    )
                ),
            ),
            xAxisData = xAxisData,
            yAxisData = yAxisData,
            gridLines = GridLines()
        )
            Surface (
                modifier = with (Modifier){
                    fillMaxSize().padding(10.dp, 50.dp, 10.dp, 50.dp)
                },
                color = colorResource(R.color.blue_trio_light)
            ){
                LineChart(
                    modifier = Modifier.fillMaxWidth(),
                    lineChartData = lineChartData
                )
        }
    }
}
package com.example.healthconnectjetpackcompose.ui.view.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.healthconnectjetpackcompose.R
import com.example.healthconnectjetpackcompose.ui.theme.HealthConnectJetpackComposeTheme

@Composable
fun HealthCard(
    icon: Int,
    title: String,
    latestData: String,
    latestDate: String,
    modifier: Modifier = Modifier,
    cardWidth: Modifier = Modifier.width(200.dp), // Set the desired width here
    titleStyle: TextStyle = TextStyle(fontSize = 16.sp, color = Color.Black) // Default style for the title
) {
    Box(modifier = modifier.then(cardWidth)) {
        Column(
            modifier = Modifier
                .background(color = Color.LightGray, shape = RoundedCornerShape(8.dp))
                .padding(16.dp)
                .then(cardWidth),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(painterResource(icon), "content description")
            Text(
                text = title,
                modifier = Modifier.padding(top = 12.dp),
                style = titleStyle,
                maxLines = 2 // Ensure title stays within the bounds
            )
            Text(
                text = latestData,
                modifier = Modifier.padding(top = 12.dp),
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1 // Ensure latestData stays within the bounds
            )
            Text(
                text = latestDate,
                modifier = Modifier.padding(top = 12.dp),
                style = MaterialTheme.typography.bodySmall,
                maxLines = 2 // Ensure latestDate stays within the bounds
            )
        }
    }
}

@Preview
@Composable
fun HealthCardPreview() {
    HealthConnectJetpackComposeTheme {
        HealthCard(
            icon = R.drawable.ic_body_temparature,
            title = "Body Temperature",
            latestData = "36.6°C",
            latestDate = "2024-07-11",
            cardWidth = Modifier.width(200.dp), // Example width
            titleStyle = TextStyle(fontSize = 18.sp, color = Color.Black), // Example title style
            modifier = Modifier.padding(16.dp)
        )
    }
}

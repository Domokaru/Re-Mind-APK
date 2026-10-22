package com.hallauer.remind.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hallauer.remind.data.local.entity.GroupBucket
import com.hallauer.remind.ui.theme.toColor

@Composable
fun BucketBadge(
    bucket: GroupBucket,
    modifier: Modifier = Modifier,
    isDarkTheme: Boolean = isSystemInDarkTheme()
) {
    val bucketColor = bucket.toColor(isDarkTheme)

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(bucketColor.copy(alpha = 0.18f))
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Text(
            text = bucket.displayName,
            color = bucketColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

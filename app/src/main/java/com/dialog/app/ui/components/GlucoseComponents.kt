package com.dialog.app.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingFlat
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dialog.app.data.model.RiskLevel
import com.dialog.app.ui.theme.GlucoseBorderline
import com.dialog.app.ui.theme.GlucoseHigh
import com.dialog.app.ui.theme.GlucoseLow
import com.dialog.app.ui.theme.GlucoseNormal

/**
 * Returns the color associated with a risk level.
 */
@Composable
fun getRiskLevelColor(riskLevel: RiskLevel): Color {
    return when (riskLevel) {
        RiskLevel.NORMAL -> GlucoseNormal
        RiskLevel.BORDERLINE -> GlucoseBorderline
        RiskLevel.HIGH -> GlucoseHigh
        RiskLevel.LOW -> GlucoseLow
    }
}

/**
 * A circular indicator showing the current risk level.
 */
@Composable
fun RiskLevelIndicator(
    riskLevel: RiskLevel,
    modifier: Modifier = Modifier,
    size: Int = 12
) {
    val color by animateColorAsState(
        targetValue = getRiskLevelColor(riskLevel),
        label = "risk_color"
    )
    
    Box(
        modifier = modifier
            .size(size.dp)
            .clip(CircleShape)
            .background(color)
    )
}

/**
 * Large glucose display card with risk level indicator.
 */
@Composable
fun GlucoseDisplayCard(
    glucoseLevel: Int,
    riskLevel: RiskLevel,
    labelName: String?,
    modifier: Modifier = Modifier
) {
    val riskColor by animateColorAsState(
        targetValue = getRiskLevelColor(riskLevel),
        label = "risk_color"
    )
    
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = riskColor.copy(alpha = 0.1f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Risk level badge
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = riskColor.copy(alpha = 0.2f)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    RiskLevelIndicator(riskLevel = riskLevel)
                    Text(
                        text = riskLevel.displayName,
                        style = MaterialTheme.typography.labelMedium,
                        color = riskColor
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Glucose value
            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = glucoseLevel.toString(),
                    fontSize = 72.sp,
                    fontWeight = FontWeight.Bold,
                    color = riskColor
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "mg/dL",
                    style = MaterialTheme.typography.titleMedium,
                    color = riskColor.copy(alpha = 0.7f),
                    modifier = Modifier.padding(bottom = 12.dp)
                )
            }
            
            // Label
            if (labelName != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = labelName,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/**
 * Compact glucose card for history list.
 */
@Composable
fun GlucoseRecordCard(
    glucoseLevel: Int,
    riskLevel: RiskLevel,
    labelName: String?,
    time: String,
    date: String,
    notes: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val riskColor = getRiskLevelColor(riskLevel)
    
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Risk level indicator bar
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(56.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(riskColor)
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // Glucose value
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.width(80.dp)
            ) {
                Text(
                    text = glucoseLevel.toString(),
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = riskColor
                )
                Text(
                    text = "mg/dL",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // Details
            Column(
                modifier = Modifier.weight(1f)
            ) {
                if (labelName != null) {
                    Text(
                        text = labelName,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Medium
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "$date • $time",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (!notes.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = notes,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1
                    )
                }
            }
            
            // Trend icon based on risk
            Icon(
                imageVector = when (riskLevel) {
                    RiskLevel.HIGH -> Icons.Default.TrendingUp
                    RiskLevel.LOW -> Icons.Default.TrendingDown
                    else -> Icons.Default.TrendingFlat
                },
                contentDescription = null,
                tint = riskColor,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

/**
 * Statistics card for dashboard.
 */
@Composable
fun StatCard(
    title: String,
    value: String,
    subtitle: String? = null,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

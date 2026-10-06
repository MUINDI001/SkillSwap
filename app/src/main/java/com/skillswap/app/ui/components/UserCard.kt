package com.skillswap.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.skillswap.app.model.User

@Composable
fun UserCard(
    user: User,
    onSwapClick: () -> Unit
) {

    Card(
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(8.dp),
        modifier = Modifier
            .width(240.dp)
            .padding(end = 12.dp)
    ) {

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {

            // 👤 Avatar + Name
            Row(verticalAlignment = Alignment.CenterVertically) {

                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = user.name.first().toString(),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = user.name,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = user.location,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // ⭐ Rating
            AssistChip(
                onClick = {},
                label = { Text("⭐ ${user.rating}") }
            )

            Spacer(modifier = Modifier.height(12.dp))

            // 🎯 Skills Offered
            Text("Offers", style = MaterialTheme.typography.labelMedium)

            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.padding(top = 4.dp)
            ) {
                user.skillsOffered.take(2).forEach {
                    AssistChip(onClick = {}, label = { Text(it) })
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 🎯 Skills Wanted
            Text("Wants", style = MaterialTheme.typography.labelMedium)

            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.padding(top = 4.dp)
            ) {
                user.skillsWanted.take(2).forEach {
                    AssistChip(onClick = {}, label = { Text(it) })
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 🔘 CTA Button
            Button(
                onClick = onSwapClick,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Request Swap")
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
fun UserCardPreview() {

    val fakeUser = User(
        name = "Ruth Muindi",
        location = "Nairobi",
        skillsOffered = listOf("Kotlin", "UI Design"),
        skillsWanted = listOf("Photography", "Business"),
        rating = 4.8,
        bio = "Passionate about building apps",
        profileImageUrl = null,
        id = "1"
    )

    UserCard(
        user = fakeUser,
        onSwapClick = {}
    )
}
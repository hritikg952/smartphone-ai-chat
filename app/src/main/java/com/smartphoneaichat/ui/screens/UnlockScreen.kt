package com.smartphoneaichat.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.LockOpen
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.smartphoneaichat.R
import com.smartphoneaichat.ui.theme.AccentBlue
import com.smartphoneaichat.ui.theme.DarkBackground
import com.smartphoneaichat.ui.theme.TextPrimary
import com.smartphoneaichat.ui.theme.TextSecondary

/** Process-local prototype unlock entry point; cryptographic auth belongs to MG-03. */
@Composable
fun UnlockScreen(
    onUnlock: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            imageVector = Icons.Outlined.LockOpen,
            contentDescription = null,
            tint = AccentBlue,
        )
        Text(
            text = stringResource(R.string.unlock_title),
            modifier = Modifier.padding(top = 24.dp),
            color = TextPrimary,
            style = MaterialTheme.typography.displayLarge,
            textAlign = TextAlign.Center,
        )
        Text(
            text = stringResource(R.string.unlock_subtitle),
            modifier = Modifier.padding(top = 12.dp, bottom = 28.dp),
            color = TextSecondary,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
        )
        Button(
            onClick = onUnlock,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = AccentBlue,
                contentColor = DarkBackground,
            ),
        ) {
            Text(stringResource(R.string.unlock_action))
        }
    }
}

package com.example.izinmu.view.komponen

import com.example.izinmu.R
import androidx.compose.ui.res.stringResource
import com.example.izinmu.ui.theme.Dimens
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Inbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.izinmu.model.LeaveStatus
import com.example.izinmu.model.Role
import com.example.izinmu.model.User
import com.example.izinmu.ui.theme.IzinBlueContainer
import com.example.izinmu.ui.theme.IzinDanger
import com.example.izinmu.ui.theme.IzinDangerContainer
import com.example.izinmu.ui.theme.IzinInstructor
import com.example.izinmu.ui.theme.IzinInstructorContainer
import com.example.izinmu.ui.theme.IzinMuted
import com.example.izinmu.ui.theme.IzinOutline
import com.example.izinmu.ui.theme.IzinSuccess
import com.example.izinmu.ui.theme.IzinSuccessContainer
import com.example.izinmu.ui.theme.IzinSurface
import com.example.izinmu.ui.theme.IzinText
import com.example.izinmu.ui.theme.IzinWarning
import com.example.izinmu.ui.theme.IzinWarningContainer

@Composable
fun IzinTopBar(
    title: String,
    subtitle: String? = null,
    onBack: (() -> Unit)? = null,
    actions: @Composable () -> Unit = {},
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Dimens.s20, vertical = Dimens.s12),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (onBack != null) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                    contentDescription = stringResource(R.string.common_back),
                    tint = IzinText,
                )
            }
            Spacer(Modifier.width(Dimens.s4))
        }
        Column(Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                color = IzinText,
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = IzinMuted,
                )
            }
        }
        actions()
    }
}

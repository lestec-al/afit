package com.yurhel.alex.afit.ui.screen_settings.components

import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.core.os.LocaleListCompat
import com.yurhel.alex.afit.R
import com.yurhel.alex.afit.ui.screen_settings.languages
import androidx.compose.ui.platform.LocalLocale

@Composable
fun LanguageSelector() {
    val appLocales = AppCompatDelegate.getApplicationLocales()
    val currentLocale = appLocales.get(0)?.language ?: LocalLocale.current.platformLocale.language

    Column(
        modifier = Modifier.padding(bottom = 5.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        languages.forEachIndexed { idx, obj ->
            if (idx != 0) HorizontalDivider()
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        val localeList = LocaleListCompat.forLanguageTags(obj.second)
                        AppCompatDelegate.setApplicationLocales(localeList)
                    },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = obj.first,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp)
                )
                if (currentLocale != null && currentLocale == obj.second) {
                    Icon(
                        painter = painterResource(R.drawable.ic_ok),
                        contentDescription = null,
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp)
                    )
                }
            }
        }
    }
}
package com.example.androidweather.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.androidweather.data.model.GeoResult
import com.example.androidweather.viewModel.WeatherViewModel

@Composable
fun AddCityScreen(
    onDismiss: () -> Unit,
    onCitySelected: (GeoResult) -> Unit,
    viewModel: WeatherViewModel
) {
    var searchQuery by remember { mutableStateOf("") }
    val searchResults by viewModel.searchResults.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(28.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        title = {
            Text(
                text = "添加城市",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Color.DarkGray
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp)
            ) {
                // Search input
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = {
                        searchQuery = it
                        if (it.length >= 2) {
                            viewModel.searchCity(it)
                        }
                    },
                    placeholder = {
                        Text("搜索城市名称")
                    },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = null)
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Close, contentDescription = "清除")
                            }
                        }
                    },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Search
                    ),
                    keyboardActions = KeyboardActions(
                        onSearch = {
                            if (searchQuery.length >= 2) {
                                viewModel.searchCity(searchQuery)
                            }
                        }
                    ),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Results or loading
                if (isLoading) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                } else if (searchResults.isNotEmpty()) {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                    ) {
                        items(searchResults, key = { it.id ?: it.name.hashCode() }) { result ->
                            SearchResultItem(
                                result = result,
                                onClick = onCitySelected
                            )
                        }
                    }
                } else if (searchQuery.isNotEmpty() && searchResults.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "未找到相关城市",
                            color = Color.Gray,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}

@Composable
fun SearchResultItem(
    result: GeoResult,
    onClick: (GeoResult) -> Unit
) {
    val location = buildString {
        append(result.name ?: "")
        if (!result.admin1.isNullOrEmpty()) {
            append(", ${result.admin1}")
        }
        if (!result.country.isNullOrEmpty()) {
            append(" ${result.country}")
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick(result) }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = result.name ?: "",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = Color.DarkGray
            )
            if (!result.admin1.isNullOrEmpty() || !result.country.isNullOrEmpty()) {
                Text(
                    text = location,
                    fontSize = 13.sp,
                    color = Color.Gray
                )
            }
        }
        Icon(
            Icons.Default.Search,
            contentDescription = null,
            tint = Color.Gray
        )
    }
}

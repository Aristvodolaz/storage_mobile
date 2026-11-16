package com.komus.sorage_mobile.presentation.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.komus.sorage_mobile.domain.viewModel.PlacementFlowViewModel

/** Шапка с товаром, который сейчас размещается: виден на всех шагах потока. */
@Composable
fun PlacementProductHeader(placementFlow: PlacementFlowViewModel = hiltViewModel()) {
    val draft by placementFlow.draft.collectAsStateWithLifecycle()
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        elevation = 1.dp,
        shape = RoundedCornerShape(4.dp)
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            Text(
                text = draft.productName.ifEmpty { "Товар не выбран" },
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            if (draft.article.isNotEmpty()) {
                Text(text = "Артикул: ${draft.article}", fontSize = 12.sp)
            }
        }
    }
}

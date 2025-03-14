package com.komus.sorage_mobile.domain.usecase

import com.komus.sorage_mobile.data.response.SearchItem
import com.komus.sorage_mobile.domain.repository.SearchRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class SearchUseCase @Inject constructor(
    private val repository: SearchRepository
) {
    suspend fun execute(shk: String?, article: String?): Flow<Result<List<SearchItem>>> {
        return repository.searchItem(shk, article)
    }
}
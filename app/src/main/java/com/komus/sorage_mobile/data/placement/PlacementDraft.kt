package com.komus.sorage_mobile.data.placement

import com.komus.sorage_mobile.util.SPHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Черновик размещения: товар, единица, срок годности, состояние и ячейка.
 * Заполняется по шагам экранов и хранится до подтверждения размещения.
 */
data class PlacementDraft(
    val productId: String = "",
    val article: String = "",
    val shk: String = "",
    val productName: String = "",
    val brief: String = "",          // код единицы (prunitId)
    val typeBrief: String = "",      // название единицы
    val productQnt: Int = 0,         // штук в одной упаковке
    val fullQnt: Int = 0,            // итоговое количество в штуках
    val exPalletType: String = "",
    val expirationDate: String = "", // ISO yyyy-MM-dd
    val condition: String = "Кондиция",
    val reason: String = "",
    val bufferLocation: String = "",
    val wrShk: String = ""
)

/**
 * Единственное место, где живёт черновик размещения.
 * Экраны и ViewModel читают и меняют его только через [update], а не через разрозненные ключи SPHelper.
 * Значения сохраняются в SPHelper, поэтому черновик переживает закрытие приложения.
 */
@Singleton
class PlacementDraftStore @Inject constructor(
    private val spHelper: SPHelper
) {
    private val _draft = MutableStateFlow(load())
    val draft: StateFlow<PlacementDraft> = _draft.asStateFlow()

    fun update(transform: (PlacementDraft) -> PlacementDraft) {
        val updated = transform(_draft.value)
        _draft.value = updated
        persist(updated)
    }

    /** Сбрасывает данные о товаре после успешного размещения; склад не трогаем. */
    fun clear() {
        update { PlacementDraft() }
    }

    private fun load() = PlacementDraft(
        productId = spHelper.getProductId(),
        article = spHelper.getArticle(),
        shk = spHelper.getShk(),
        productName = spHelper.getProductName(),
        brief = spHelper.getBrief(),
        typeBrief = spHelper.getTypeBrief(),
        productQnt = spHelper.getProductQnt(),
        fullQnt = spHelper.getFullQnt(),
        exPalletType = spHelper.getExPalletType(),
        expirationDate = spHelper.getSrokGodnosti(),
        condition = spHelper.getCondition().ifEmpty { "Кондиция" },
        reason = spHelper.getReason(),
        bufferLocation = spHelper.getBufferLocation(),
        wrShk = spHelper.getWrShk()
    )

    private fun persist(d: PlacementDraft) {
        spHelper.saveProductId(d.productId)
        spHelper.saveArticle(d.article)
        spHelper.saveShk(d.shk)
        spHelper.saveProductName(d.productName)
        spHelper.saveBrief(d.brief)
        spHelper.saveTypeBrief(d.typeBrief)
        spHelper.saveProductQnt(d.productQnt)
        spHelper.saveFullQnt(d.fullQnt)
        spHelper.saveExPalletType(d.exPalletType)
        spHelper.saveSrokGodnosti(d.expirationDate)
        spHelper.saveCondition(d.condition)
        spHelper.saveReason(d.reason)
        spHelper.saveBufferLocation(d.bufferLocation)
        spHelper.saveWrShk(d.wrShk)
    }
}

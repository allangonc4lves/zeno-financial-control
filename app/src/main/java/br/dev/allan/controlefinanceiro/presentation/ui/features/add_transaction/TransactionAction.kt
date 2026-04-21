package br.dev.allan.controlefinanceiro.presentation.ui.features.add_transaction

import br.dev.allan.controlefinanceiro.utils.constants.TransactionCategory
import br.dev.allan.controlefinanceiro.utils.constants.TransactionDirection
import br.dev.allan.controlefinanceiro.utils.constants.TransactionType

sealed class TransactionAction {
    data class TitleChanged(val value: String) : TransactionAction()
    data class AmountChanged(val value: String) : TransactionAction()
    data class CategoryChanged(val value: TransactionCategory) : TransactionAction()
    data class DateChanged(val millis: String) : TransactionAction()
    data class DirectionChanged(val dir: TransactionDirection) : TransactionAction()
    data class TypeChanged(val type: TransactionType) : TransactionAction()
    data class InstallmentCountChanged(val count: Int) : TransactionAction()
    data class CardSelected(val cardId: String?) : TransactionAction()
    data class PaidChanged(val paid: Boolean) : TransactionAction()
    data class CreditCardToggle(val isCreditCard: Boolean) : TransactionAction()
    object Save : TransactionAction()
    object Delete : TransactionAction()
}
package br.dev.allan.controlefinanceiro.utils

import br.dev.allan.controlefinanceiro.constants.TransactionCategory

import br.dev.allan.controlefinanceiro.R

data class ValidationResult(
    val successful: Boolean,
    val errorMessageRes: Int? = null
)

class ValidateText {
    fun execute(title: String): ValidationResult {
        if (title.isBlank()) {
            return ValidationResult(successful = false, errorMessageRes = R.string.required_field)
        }

        val titleRegex = """^[\p{L} ]+$""".toRegex()

        if (!title.matches(titleRegex)) {
            return ValidationResult(
                successful = false,
                errorMessageRes = R.string.title_validation_error
            )
        }

        return ValidationResult(successful = true)
    }
}

class ValidateAmount {
    fun execute(amount: String): ValidationResult {
        if (amount.isBlank()) {
            return ValidationResult(
                successful = false,
                errorMessageRes = R.string.required_field
            )
        }

        val cleanValue = amount
            .replace(Regex("[^0-9]"), "")

        val value = cleanValue.toDoubleOrNull()?.div(100) ?: 0.0

        if (value <= 0.0) {
            return ValidationResult(
                successful = false,
                errorMessageRes = R.string.amount_validation_error
            )
        }

        return ValidationResult(successful = true)
    }
}


class ValidateCategory {
    fun execute(category: TransactionCategory?): ValidationResult {
        if (category == null) {
            return ValidationResult(successful = false, errorMessageRes = null)
        }
        return ValidationResult(successful = true)
    }
}

class ValidateLastDigitsCreditCard{
    fun execute(input: String): ValidationResult {
        if (input.isBlank()) {
            return ValidationResult(
                successful = false,
                errorMessageRes = R.string.required_field
            )
        }

        val regex = Regex("^\\d{4}$")

        if (!regex.matches(input)) {
            return ValidationResult(
                successful = false,
                errorMessageRes = R.string.card_last_digits_error
            )
        }

        return ValidationResult(successful = true)
    }
}

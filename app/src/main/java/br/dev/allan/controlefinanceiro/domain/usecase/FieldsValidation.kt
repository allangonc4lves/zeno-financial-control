package br.dev.allan.controlefinanceiro.domain.usecase

import br.dev.allan.controlefinanceiro.domain.model.TransactionCategory

data class ValidationResult(
    val successful: Boolean,
    val errorMessage: String? = null
)

class ValidateText {
    fun execute(title: String): ValidationResult {
        if (title.isBlank()) {
            return ValidationResult(successful = false, errorMessage = "Campo obrigatório")
        }

        val titleRegex = """^[\p{L} ]+$""".toRegex()

        if (!title.matches(titleRegex)) {
            return ValidationResult(
                successful = false,
                errorMessage = "O título deve conter apenas letras e espaços"
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
                errorMessage = "Campo obrigatório"
            )
        }

        val cleanValue = amount
            .replace("R$", "")
            .replace(Regex("[^0-9,]"), "")
            .replace(",", ".")

        val value = cleanValue.toDoubleOrNull()

        if (value == null || value <= 0.0) {
            return ValidationResult(
                successful = false,
                errorMessage = "Insira um valor válido maior que zero"
            )
        }

        return ValidationResult(successful = true)
    }
}


class ValidateCategory {
    fun execute(category: TransactionCategory?): ValidationResult {
        if (category == null) {
            return ValidationResult(successful = false, errorMessage = "")
        }
        return ValidationResult(successful = true)
    }
}

class ValidateLastDigitsCreditCard{
    fun execute(input: String): ValidationResult {
        if (input.isBlank()) {
            return ValidationResult(
                successful = false,
                errorMessage = "Campo obrigatório"
            )
        }

        // Regex: apenas dígitos e exatamente 4 caracteres
        val regex = Regex("^\\d{4}$")

        if (!regex.matches(input)) {
            return ValidationResult(
                successful = false,
                errorMessage = "O campo deve conter exatamente 4 números"
            )
        }

        return ValidationResult(successful = true)
    }
}

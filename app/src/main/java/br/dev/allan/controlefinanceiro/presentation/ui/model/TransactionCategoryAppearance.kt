package br.dev.allan.controlefinanceiro.presentation.ui.model

import androidx.compose.material.icons.Icons
import androidx.compose.ui.graphics.vector.ImageVector
import br.dev.allan.controlefinanceiro.domain.model.TransactionCategory
import br.dev.allan.controlefinanceiro.domain.model.TransactionINorEX
import androidx.compose.material.icons.outlined.AccountBalance
import androidx.compose.material.icons.outlined.AirportShuttle
import androidx.compose.material.icons.outlined.Analytics
import androidx.compose.material.icons.outlined.AreaChart
import androidx.compose.material.icons.outlined.ArrowDownward
import androidx.compose.material.icons.outlined.ArrowUpward
import androidx.compose.material.icons.outlined.AttachMoney
import androidx.compose.material.icons.outlined.Build
import androidx.compose.material.icons.outlined.BusinessCenter
import androidx.compose.material.icons.outlined.CardGiftcard
import androidx.compose.material.icons.outlined.CompareArrows
import androidx.compose.material.icons.outlined.CreditCard
import androidx.compose.material.icons.outlined.DirectionsCar
import androidx.compose.material.icons.outlined.ElectricalServices
import androidx.compose.material.icons.outlined.Fastfood
import androidx.compose.material.icons.outlined.FreeBreakfast
import androidx.compose.material.icons.outlined.GasMeter
import androidx.compose.material.icons.outlined.HealthAndSafety
import androidx.compose.material.icons.outlined.House
import androidx.compose.material.icons.outlined.LocalAirport
import androidx.compose.material.icons.outlined.LocalBar
import androidx.compose.material.icons.outlined.LocalGroceryStore
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material.icons.outlined.MonetizationOn
import androidx.compose.material.icons.outlined.MoneyOffCsred
import androidx.compose.material.icons.outlined.Pets
import androidx.compose.material.icons.outlined.SafetyDivider
import androidx.compose.material.icons.outlined.Savings
import androidx.compose.material.icons.outlined.ShoppingBag
import androidx.compose.material.icons.outlined.Subscriptions
import androidx.compose.material.icons.outlined.TheaterComedy
import androidx.compose.material.icons.outlined.WaterDrop

data class CategoryAppearance(
    val displayName: String,
    val icon: ImageVector,
    val type: TransactionINorEX
)

// Função de extensão que mapeia a lógica de negócio para a visual
fun TransactionCategory.getAppearance(): CategoryAppearance {
    return when (this) {
        TransactionCategory.CREDIT_CARD_PAYMENT -> CategoryAppearance("Credit Card", Icons.Outlined.CreditCard, TransactionINorEX.EXPENSE)
        TransactionCategory.SAVINGS -> CategoryAppearance("Savings", Icons.Outlined.Savings, TransactionINorEX.EXPENSE)
        TransactionCategory.HOUSING -> CategoryAppearance("Housing", Icons.Outlined.House, TransactionINorEX.EXPENSE)
        TransactionCategory.ENERGY_BILL -> CategoryAppearance("Energy bill", Icons.Outlined.ElectricalServices, TransactionINorEX.EXPENSE)
        TransactionCategory.WATER_BILL -> CategoryAppearance("Water bill", Icons.Outlined.WaterDrop, TransactionINorEX.EXPENSE)
        TransactionCategory.GAS_BILL -> CategoryAppearance("Gas bill", Icons.Outlined.GasMeter, TransactionINorEX.EXPENSE)
        TransactionCategory.FOOD -> CategoryAppearance("Food", Icons.Outlined.Fastfood, TransactionINorEX.EXPENSE)
        TransactionCategory.DRINK -> CategoryAppearance("Drink", Icons.Outlined.LocalBar, TransactionINorEX.EXPENSE)
        TransactionCategory.TRANSPORTATION -> CategoryAppearance("Transportation", Icons.Outlined.AirportShuttle, TransactionINorEX.EXPENSE)
        TransactionCategory.UBER -> CategoryAppearance("Uber", Icons.Outlined.DirectionsCar, TransactionINorEX.EXPENSE)
        TransactionCategory.GROCERIES -> CategoryAppearance("Groceries", Icons.Outlined.LocalGroceryStore, TransactionINorEX.EXPENSE)
        TransactionCategory.ENTERTAINMENT -> CategoryAppearance("Entertainment & Leisure", Icons.Outlined.TheaterComedy, TransactionINorEX.EXPENSE)
        TransactionCategory.SHOPPING -> CategoryAppearance("Shopping", Icons.Outlined.ShoppingBag, TransactionINorEX.EXPENSE)
        TransactionCategory.HEALTH_PERSONAL_CARE -> CategoryAppearance("Health & Personal Care", Icons.Outlined.HealthAndSafety, TransactionINorEX.EXPENSE)
        TransactionCategory.EDUCATION -> CategoryAppearance("Education", Icons.Outlined.MenuBook, TransactionINorEX.EXPENSE)
        TransactionCategory.SUBSCRIPTIONS -> CategoryAppearance("Subscriptions & Services", Icons.Outlined.Subscriptions, TransactionINorEX.EXPENSE)
        TransactionCategory.DEBT_REPAYMENT -> CategoryAppearance("Debt & Loans", Icons.Outlined.AccountBalance, TransactionINorEX.EXPENSE)
        TransactionCategory.INVESTMENT_OUT -> CategoryAppearance("Investment Contribution", Icons.Outlined.BusinessCenter, TransactionINorEX.EXPENSE)
        TransactionCategory.TRAVEL -> CategoryAppearance("Travel", Icons.Outlined.LocalAirport, TransactionINorEX.EXPENSE)
        TransactionCategory.PETS -> CategoryAppearance("Pets", Icons.Outlined.Pets, TransactionINorEX.EXPENSE)
        TransactionCategory.GIFTS_DONATION -> CategoryAppearance("Gifts & Donations", Icons.Outlined.CardGiftcard, TransactionINorEX.EXPENSE)
        TransactionCategory.MAINTENANCE -> CategoryAppearance("Home/Auto Maintenance", Icons.Outlined.Build, TransactionINorEX.EXPENSE)
        TransactionCategory.TAXES -> CategoryAppearance("Taxes & Fees", Icons.Outlined.MoneyOffCsred, TransactionINorEX.EXPENSE)
        TransactionCategory.INSURANCE -> CategoryAppearance("Insurance", Icons.Outlined.Lock, TransactionINorEX.EXPENSE)
        TransactionCategory.OTHERS_EXPENSE -> CategoryAppearance("Other Expenses", Icons.Outlined.ArrowDownward, TransactionINorEX.EXPENSE)

        TransactionCategory.SALARY -> CategoryAppearance("Salary", Icons.Outlined.AttachMoney, TransactionINorEX.INCOME)
        TransactionCategory.FREELANCE -> CategoryAppearance("Freelance & Side Hustles", Icons.Outlined.FreeBreakfast, TransactionINorEX.INCOME)
        TransactionCategory.INVESTMENTS -> CategoryAppearance("Investment Returns/Dividends", Icons.Outlined.BusinessCenter, TransactionINorEX.INCOME)
        TransactionCategory.GIFTS_RECEIVED -> CategoryAppearance("Gifts Received", Icons.Outlined.CardGiftcard, TransactionINorEX.INCOME)
        TransactionCategory.RENTAL_INCOME -> CategoryAppearance("Rental Income", Icons.Outlined.AreaChart, TransactionINorEX.INCOME)
        TransactionCategory.REFUNDS -> CategoryAppearance("Refunds & Reimbursements", Icons.Outlined.CompareArrows, TransactionINorEX.INCOME)
        TransactionCategory.BONUS -> CategoryAppearance("Bonuses", Icons.Outlined.MonetizationOn, TransactionINorEX.INCOME)
        TransactionCategory.GRANTS -> CategoryAppearance("Grants & Scholarships", Icons.Outlined.SafetyDivider, TransactionINorEX.INCOME)
        TransactionCategory.SALES -> CategoryAppearance("Sales (Selling Items)", Icons.Outlined.Analytics, TransactionINorEX.INCOME)
        TransactionCategory.OTHERS_INCOME -> CategoryAppearance("Other Income", Icons.Outlined.ArrowUpward, TransactionINorEX.INCOME)
    }
}
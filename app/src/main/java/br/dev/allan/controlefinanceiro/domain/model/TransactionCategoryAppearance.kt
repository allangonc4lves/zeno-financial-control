package br.dev.allan.controlefinanceiro.domain.model

import androidx.compose.material.icons.Icons
import androidx.compose.ui.graphics.vector.ImageVector
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
import androidx.compose.ui.graphics.Color

data class CategoryAppearance(
    val displayName: String,
    val icon: ImageVector,
    val type: TransactionDirection,
    val color: Color
)

fun TransactionCategory.getAppearance(): CategoryAppearance {
    return when (this) {
        TransactionCategory.CREDIT_CARD_PAYMENT -> CategoryAppearance("Credit Card", Icons.Outlined.CreditCard, TransactionDirection.EXPENSE, Color(0xFFFF0000))
        TransactionCategory.SAVINGS -> CategoryAppearance("Savings", Icons.Outlined.Savings, TransactionDirection.EXPENSE, Color(0xFF22FF00))
        TransactionCategory.HOUSING -> CategoryAppearance("Housing", Icons.Outlined.House, TransactionDirection.EXPENSE, Color(0xFFD000FF))
        TransactionCategory.ENERGY_BILL -> CategoryAppearance("Energy bill", Icons.Outlined.ElectricalServices, TransactionDirection.EXPENSE, Color(0xFF5E35B1))
        TransactionCategory.WATER_BILL -> CategoryAppearance("Water bill", Icons.Outlined.WaterDrop, TransactionDirection.EXPENSE, Color(0xFF3949AB))
        TransactionCategory.GAS_BILL -> CategoryAppearance("Gas bill", Icons.Outlined.GasMeter, TransactionDirection.EXPENSE, Color(0xFF1E88E5))
        TransactionCategory.FOOD -> CategoryAppearance("Food", Icons.Outlined.Fastfood, TransactionDirection.EXPENSE, Color(0xFF039BE5))
        TransactionCategory.DRINK -> CategoryAppearance("Drink", Icons.Outlined.LocalBar, TransactionDirection.EXPENSE, Color(0xFF00ACC1))
        TransactionCategory.TRANSPORTATION -> CategoryAppearance("Transportation", Icons.Outlined.AirportShuttle, TransactionDirection.EXPENSE, Color(0xFF00897B))
        TransactionCategory.UBER -> CategoryAppearance("Uber", Icons.Outlined.DirectionsCar, TransactionDirection.EXPENSE, Color(0xFF43A047))
        TransactionCategory.GROCERIES -> CategoryAppearance("Groceries", Icons.Outlined.LocalGroceryStore, TransactionDirection.EXPENSE, Color(0xFF7CB342))
        TransactionCategory.ENTERTAINMENT -> CategoryAppearance("Entertainment & Leisure", Icons.Outlined.TheaterComedy, TransactionDirection.EXPENSE, Color(0xFFC0CA33))
        TransactionCategory.SHOPPING -> CategoryAppearance("Shopping", Icons.Outlined.ShoppingBag, TransactionDirection.EXPENSE, Color(0xFFFDD835))
        TransactionCategory.HEALTH_PERSONAL_CARE -> CategoryAppearance("Health & Personal Care", Icons.Outlined.HealthAndSafety, TransactionDirection.EXPENSE, Color(0xFF95FF00))
        TransactionCategory.EDUCATION -> CategoryAppearance("Education", Icons.Outlined.MenuBook, TransactionDirection.EXPENSE, Color(0xFFFB8C00))
        TransactionCategory.SUBSCRIPTIONS -> CategoryAppearance("Subscriptions & Services", Icons.Outlined.Subscriptions, TransactionDirection.EXPENSE, Color(0xFF3700FF))
        TransactionCategory.DEBT_REPAYMENT -> CategoryAppearance("Debt & Loans", Icons.Outlined.AccountBalance, TransactionDirection.EXPENSE, Color(0xFF6D4C41))
        TransactionCategory.INVESTMENT_OUT -> CategoryAppearance("Investment Contribution", Icons.Outlined.BusinessCenter, TransactionDirection.EXPENSE, Color(0xFF757575))
        TransactionCategory.TRAVEL -> CategoryAppearance("Travel", Icons.Outlined.LocalAirport, TransactionDirection.EXPENSE, Color(0xFF546E7A))
        TransactionCategory.PETS -> CategoryAppearance("Pets", Icons.Outlined.Pets, TransactionDirection.EXPENSE, Color(0xFF26A69A))
        TransactionCategory.GIFTS_DONATION -> CategoryAppearance("Gifts & Donations", Icons.Outlined.CardGiftcard, TransactionDirection.EXPENSE, Color(0xFF9CCC65))
        TransactionCategory.MAINTENANCE -> CategoryAppearance("Home/Auto Maintenance", Icons.Outlined.Build, TransactionDirection.EXPENSE, Color(0xFFFF7043))
        TransactionCategory.TAXES -> CategoryAppearance("Taxes & Fees", Icons.Outlined.MoneyOffCsred, TransactionDirection.EXPENSE, Color(0xFFAB47BC))
        TransactionCategory.INSURANCE -> CategoryAppearance("Insurance", Icons.Outlined.Lock, TransactionDirection.EXPENSE, Color(0xFF29B6F6))
        TransactionCategory.OTHERS_EXPENSE -> CategoryAppearance("Other Expenses", Icons.Outlined.ArrowDownward, TransactionDirection.EXPENSE, Color(0xFF1C1A1A))


        TransactionCategory.SALARY -> CategoryAppearance("Salary", Icons.Outlined.AttachMoney, TransactionDirection.INCOME, Color(0xFF2E7D32))
        TransactionCategory.FREELANCE -> CategoryAppearance("Freelance & Side Hustles", Icons.Outlined.FreeBreakfast, TransactionDirection.INCOME, Color(0xFF388E3C))
        TransactionCategory.INVESTMENTS -> CategoryAppearance("Investment Returns/Dividends", Icons.Outlined.BusinessCenter, TransactionDirection.INCOME, Color(0xFF00796B))
        TransactionCategory.GIFTS_RECEIVED -> CategoryAppearance("Gifts Received", Icons.Outlined.CardGiftcard, TransactionDirection.INCOME, Color(0xFF8E24AA))
        TransactionCategory.RENTAL_INCOME -> CategoryAppearance("Rental Income", Icons.Outlined.AreaChart, TransactionDirection.INCOME, Color(0xFF1976D2))
        TransactionCategory.REFUNDS -> CategoryAppearance("Refunds & Reimbursements", Icons.Outlined.CompareArrows, TransactionDirection.INCOME, Color(0xFF0288D1))
        TransactionCategory.BONUS -> CategoryAppearance("Bonuses", Icons.Outlined.MonetizationOn, TransactionDirection.INCOME, Color(0xFFFBC02D))
        TransactionCategory.GRANTS -> CategoryAppearance("Grants & Scholarships", Icons.Outlined.SafetyDivider, TransactionDirection.INCOME, Color(0xFF6D4C41))
        TransactionCategory.SALES -> CategoryAppearance("Sales (Selling Items)", Icons.Outlined.Analytics, TransactionDirection.INCOME, Color(0xFF43A047))
        TransactionCategory.OTHERS_INCOME -> CategoryAppearance("Other Income", Icons.Outlined.ArrowUpward, TransactionDirection.INCOME, Color(0xFF8BC34A))
    }
}

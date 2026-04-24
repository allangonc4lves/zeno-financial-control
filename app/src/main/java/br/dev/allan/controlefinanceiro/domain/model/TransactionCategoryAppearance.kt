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
import br.dev.allan.controlefinanceiro.utils.constants.TransactionCategory
import br.dev.allan.controlefinanceiro.utils.constants.TransactionDirection

import androidx.annotation.StringRes
import br.dev.allan.controlefinanceiro.R

data class CategoryAppearance(
    @StringRes val displayNameRes: Int,
    val icon: ImageVector,
    val type: TransactionDirection,
    val color: Color
)

fun TransactionCategory.getAppearance(): CategoryAppearance {
    return when (this) {
        //TransactionCategory.CREDIT_CARD_PAYMENT -> CategoryAppearance(R.string.credit_card_label, Icons.Outlined.CreditCard, TransactionDirection.EXPENSE, Color(0xFFFF0000))
        TransactionCategory.SAVINGS -> CategoryAppearance(R.string.cat_savings, Icons.Outlined.Savings, TransactionDirection.EXPENSE, Color(0xFF22FF00))
        TransactionCategory.HOUSING -> CategoryAppearance(R.string.cat_housing, Icons.Outlined.House, TransactionDirection.EXPENSE, Color(0xFFD000FF))
        TransactionCategory.ENERGY_BILL -> CategoryAppearance(R.string.cat_energy_bill, Icons.Outlined.ElectricalServices, TransactionDirection.EXPENSE, Color(0xFF5E35B1))
        TransactionCategory.WATER_BILL -> CategoryAppearance(R.string.cat_water_bill, Icons.Outlined.WaterDrop, TransactionDirection.EXPENSE, Color(0xFF3949AB))
        TransactionCategory.GAS_BILL -> CategoryAppearance(R.string.cat_gas_bill, Icons.Outlined.GasMeter, TransactionDirection.EXPENSE, Color(0xFF1E88E5))
        TransactionCategory.FOOD -> CategoryAppearance(R.string.cat_food, Icons.Outlined.Fastfood, TransactionDirection.EXPENSE, Color(0xFF039BE5))
        TransactionCategory.DRINK -> CategoryAppearance(R.string.cat_drink, Icons.Outlined.LocalBar, TransactionDirection.EXPENSE, Color(0xFF00ACC1))
        TransactionCategory.TRANSPORTATION -> CategoryAppearance(R.string.cat_transportation, Icons.Outlined.AirportShuttle, TransactionDirection.EXPENSE, Color(0xFF00897B))
        TransactionCategory.UBER -> CategoryAppearance(R.string.cat_uber, Icons.Outlined.DirectionsCar, TransactionDirection.EXPENSE, Color(0xFF43A047))
        TransactionCategory.GROCERIES -> CategoryAppearance(R.string.cat_groceries, Icons.Outlined.LocalGroceryStore, TransactionDirection.EXPENSE, Color(0xFF7CB342))
        TransactionCategory.ENTERTAINMENT -> CategoryAppearance(R.string.cat_entertainment, Icons.Outlined.TheaterComedy, TransactionDirection.EXPENSE, Color(0xFFC0CA33))
        TransactionCategory.SHOPPING -> CategoryAppearance(R.string.cat_shopping, Icons.Outlined.ShoppingBag, TransactionDirection.EXPENSE, Color(0xFFFDD835))
        TransactionCategory.HEALTH_PERSONAL_CARE -> CategoryAppearance(R.string.cat_health_personal_care, Icons.Outlined.HealthAndSafety, TransactionDirection.EXPENSE, Color(0xFF95FF00))
        TransactionCategory.EDUCATION -> CategoryAppearance(R.string.cat_education, Icons.Outlined.MenuBook, TransactionDirection.EXPENSE, Color(0xFFFB8C00))
        TransactionCategory.SUBSCRIPTIONS -> CategoryAppearance(R.string.cat_subscriptions, Icons.Outlined.Subscriptions, TransactionDirection.EXPENSE, Color(0xFF3700FF))
        TransactionCategory.DEBT_REPAYMENT -> CategoryAppearance(R.string.cat_debt_repayment, Icons.Outlined.AccountBalance, TransactionDirection.EXPENSE, Color(0xFF6D4C41))
        TransactionCategory.INVESTMENT_OUT -> CategoryAppearance(R.string.cat_investment_out, Icons.Outlined.BusinessCenter, TransactionDirection.EXPENSE, Color(0xFF757575))
        TransactionCategory.TRAVEL -> CategoryAppearance(R.string.cat_travel, Icons.Outlined.LocalAirport, TransactionDirection.EXPENSE, Color(0xFF546E7A))
        TransactionCategory.PETS -> CategoryAppearance(R.string.cat_pets, Icons.Outlined.Pets, TransactionDirection.EXPENSE, Color(0xFF26A69A))
        TransactionCategory.GIFTS_DONATION -> CategoryAppearance(R.string.cat_gifts_donation, Icons.Outlined.CardGiftcard, TransactionDirection.EXPENSE, Color(0xFF9CCC65))
        TransactionCategory.MAINTENANCE -> CategoryAppearance(R.string.cat_maintenance, Icons.Outlined.Build, TransactionDirection.EXPENSE, Color(0xFFFF7043))
        TransactionCategory.TAXES -> CategoryAppearance(R.string.cat_taxes, Icons.Outlined.MoneyOffCsred, TransactionDirection.EXPENSE, Color(0xFFAB47BC))
        TransactionCategory.INSURANCE -> CategoryAppearance(R.string.cat_insurance, Icons.Outlined.Lock, TransactionDirection.EXPENSE, Color(0xFF29B6F6))
        TransactionCategory.OTHERS_EXPENSE -> CategoryAppearance(R.string.cat_others_expense, Icons.Outlined.ArrowDownward, TransactionDirection.EXPENSE, Color(0xFF1C1A1A))


        TransactionCategory.SALARY -> CategoryAppearance(R.string.cat_salary, Icons.Outlined.AttachMoney, TransactionDirection.INCOME, Color(0xFF2E7D32))
        TransactionCategory.FREELANCE -> CategoryAppearance(R.string.cat_freelance, Icons.Outlined.FreeBreakfast, TransactionDirection.INCOME, Color(0xFF388E3C))
        TransactionCategory.INVESTMENTS -> CategoryAppearance(R.string.cat_investments, Icons.Outlined.BusinessCenter, TransactionDirection.INCOME, Color(0xFF00796B))
        TransactionCategory.GIFTS_RECEIVED -> CategoryAppearance(R.string.cat_gifts_received, Icons.Outlined.CardGiftcard, TransactionDirection.INCOME, Color(0xFF8E24AA))
        TransactionCategory.RENTAL_INCOME -> CategoryAppearance(R.string.cat_rental_income, Icons.Outlined.AreaChart, TransactionDirection.INCOME, Color(0xFF1976D2))
        TransactionCategory.REFUNDS -> CategoryAppearance(R.string.cat_refunds, Icons.Outlined.CompareArrows, TransactionDirection.INCOME, Color(0xFF0288D1))
        TransactionCategory.BONUS -> CategoryAppearance(R.string.cat_bonus, Icons.Outlined.MonetizationOn, TransactionDirection.INCOME, Color(0xFFFBC02D))
        TransactionCategory.GRANTS -> CategoryAppearance(R.string.cat_grants, Icons.Outlined.SafetyDivider, TransactionDirection.INCOME, Color(0xFF6D4C41))
        TransactionCategory.SALES -> CategoryAppearance(R.string.cat_sales, Icons.Outlined.Analytics, TransactionDirection.INCOME, Color(0xFF43A047))
        TransactionCategory.OTHERS_INCOME -> CategoryAppearance(R.string.cat_others_income, Icons.Outlined.ArrowUpward, TransactionDirection.INCOME, Color(0xFF8BC34A))
    }
}

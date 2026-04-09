package br.dev.allan.controlefinanceiro.domain.model

import androidx.compose.material.icons.Icons
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
import androidx.compose.ui.graphics.vector.ImageVector

enum class TransactionCategory (val type: TransactionINorEX, val displayName: String, val icon: ImageVector) {

    // --- EXPENSES (Saídas) ---
    CREDIT_CARD_PAYMENT(TransactionINorEX.EXPENSE, "Credit Card", Icons.Outlined.CreditCard),
    SAVINGS(TransactionINorEX.EXPENSE, "Savings", Icons.Outlined.Savings),
    HOUSING(TransactionINorEX.EXPENSE, "Housing", Icons.Outlined.House),
    ENERGY_BILL(TransactionINorEX.EXPENSE, "Energy bill", Icons.Outlined.ElectricalServices),
    WATER_BILL(TransactionINorEX.EXPENSE, "Water bill", Icons.Outlined.WaterDrop),
    GAS_BILL(TransactionINorEX.EXPENSE, "Gas bill", Icons.Outlined.GasMeter),
    FOOD(TransactionINorEX.EXPENSE, "Food", Icons.Outlined.Fastfood),
    DRINK(TransactionINorEX.EXPENSE, "Drink", Icons.Outlined.LocalBar),
    TRANSPORTATION(TransactionINorEX.EXPENSE, "Transportation", Icons.Outlined.AirportShuttle),
    UBER(TransactionINorEX.EXPENSE, "Uber", Icons.Outlined.DirectionsCar),
    GROCERIES(TransactionINorEX.EXPENSE, "Groceries", Icons.Outlined.LocalGroceryStore),
    ENTERTAINMENT(TransactionINorEX.EXPENSE, "Entertainment & Leisure", Icons.Outlined.TheaterComedy),
    SHOPPING(TransactionINorEX.EXPENSE, "Shopping", Icons.Outlined.ShoppingBag),
    HEALTH_PERSONAL_CARE(TransactionINorEX.EXPENSE, "Health & Personal Care", Icons.Outlined.HealthAndSafety),
    EDUCATION(TransactionINorEX.EXPENSE, "Education", Icons.Outlined.MenuBook),
    SUBSCRIPTIONS(TransactionINorEX.EXPENSE, "Subscriptions & Services", Icons.Outlined.Subscriptions),
    DEBT_REPAYMENT(TransactionINorEX.EXPENSE, "Debt & Loans", Icons.Outlined.AccountBalance),
    INVESTMENT_OUT(TransactionINorEX.EXPENSE, "Investment Contribution", Icons.Outlined.BusinessCenter),
    TRAVEL(TransactionINorEX.EXPENSE, "Travel", Icons.Outlined.LocalAirport),
    PETS(TransactionINorEX.EXPENSE, "Pets", Icons.Outlined.Pets),
    GIFTS_DONATION(TransactionINorEX.EXPENSE, "Gifts & Donations", Icons.Outlined.CardGiftcard),
    MAINTENANCE(TransactionINorEX.EXPENSE, "Home/Auto Maintenance", Icons.Outlined.Build),
    TAXES(TransactionINorEX.EXPENSE, "Taxes & Fees", Icons.Outlined.MoneyOffCsred),
    INSURANCE(TransactionINorEX.EXPENSE, "Insurance", Icons.Outlined.Lock),
    OTHERS_EXPENSE(TransactionINorEX.EXPENSE, "Other Expenses", Icons.Outlined.ArrowDownward),

    // --- INCOME (Entradas) ---
    SALARY(TransactionINorEX.INCOME, "Salary", Icons.Outlined.AttachMoney),
    FREELANCE(TransactionINorEX.INCOME, "Freelance & Side Hustles", Icons.Outlined.FreeBreakfast),
    INVESTMENTS(TransactionINorEX.INCOME, "Investment Returns/Dividends", Icons.Outlined.BusinessCenter),
    GIFTS_RECEIVED(TransactionINorEX.INCOME, "Gifts Received", Icons.Outlined.CardGiftcard),
    RENTAL_INCOME(TransactionINorEX.INCOME, "Rental Income", Icons.Outlined.AreaChart),
    REFUNDS(TransactionINorEX.INCOME, "Refunds & Reimbursements", Icons.Outlined.CompareArrows),
    BONUS(TransactionINorEX.INCOME, "Bonuses", Icons.Outlined.MonetizationOn),
    GRANTS(TransactionINorEX.INCOME, "Grants & Scholarships", Icons.Outlined.SafetyDivider),
    SALES(TransactionINorEX.INCOME, "Sales (Selling Items)", Icons.Outlined.Analytics),
    OTHERS_INCOME(TransactionINorEX.INCOME, "Other Income", Icons.Outlined.ArrowUpward)
}
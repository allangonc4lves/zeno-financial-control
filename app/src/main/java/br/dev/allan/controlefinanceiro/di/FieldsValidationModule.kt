package br.dev.allan.controlefinanceiro.di

import br.dev.allan.controlefinanceiro.utils.ValidateAmount
import br.dev.allan.controlefinanceiro.utils.ValidateCategory
import br.dev.allan.controlefinanceiro.utils.ValidateLastDigitsCreditCard
import br.dev.allan.controlefinanceiro.utils.ValidateText
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent

@Module
@InstallIn(ViewModelComponent::class)
object ValidationModule {

    @Provides
    fun provideValidateTitle(): ValidateText = ValidateText()

    @Provides
    fun provideValidateAmount(): ValidateAmount = ValidateAmount()

    @Provides
    fun provideValidateCategory(): ValidateCategory = ValidateCategory()

    @Provides
    fun provideValidateLastDigitsCreditCard(): ValidateLastDigitsCreditCard = ValidateLastDigitsCreditCard()
}
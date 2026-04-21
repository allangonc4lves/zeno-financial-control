package br.dev.allan.controlefinanceiro.presentation.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.InputMode
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.dev.allan.controlefinanceiro.utils.constants.InputModeCustomTextField

@Composable
fun CustomOutlinedTextField(
    modifier: Modifier = Modifier,
    value: String = "",
    label: String = "",
    forceCursorAtEnd: Boolean = false,
    isReadOnly: Boolean = false,
    isError: Boolean = false,
    inputMode: InputModeCustomTextField = InputModeCustomTextField.TEXT,
    maxLength: Int = 30,
    errorMessage: String = "",
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardType: KeyboardType = KeyboardType.Text,
    imeAction: ImeAction = ImeAction.Next,
    capitalization: KeyboardCapitalization = KeyboardCapitalization.Sentences,
    onValueChange: (String) -> Unit,
    trailingIcon: @Composable (() -> Unit)? = null,
    leadingIcon: @Composable (() -> Unit)? = null
) {
    val focusManager = LocalFocusManager.current

    var textFieldValueState by remember {
        mutableStateOf(TextFieldValue(text = value, selection = TextRange(value.length)))
    }

    LaunchedEffect(value) {
        if (textFieldValueState.text != value) {
            textFieldValueState = textFieldValueState.copy(
                text = value,
                selection = if (forceCursorAtEnd) {
                    TextRange(value.length)
                } else {
                    TextRange(textFieldValueState.selection.start.coerceAtMost(value.length))
                }
            )
        }
    }

    OutlinedTextField(
        shape = RoundedCornerShape(32.dp),
        modifier = modifier.fillMaxWidth().heightIn(min = 56.dp),
        readOnly = isReadOnly,
        textStyle = TextStyle(
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
        ),
        colors = TextFieldDefaults.colors(
            disabledTextColor = Color.Gray,
            focusedIndicatorColor = MaterialTheme.colorScheme.onPrimaryContainer,
            unfocusedIndicatorColor = MaterialTheme.colorScheme.onSecondaryContainer,
        ),
        value = textFieldValueState,
        visualTransformation = visualTransformation,
        onValueChange = { newValue ->
            val filteredText = if (inputMode == InputModeCustomTextField.DIGITS) {
                newValue.text.filter { it.isDigit() }.take(maxLength)
            } else {
                newValue.text.take(maxLength)
            }

            textFieldValueState = if (forceCursorAtEnd) {
                TextFieldValue(
                    text = filteredText,
                    selection = TextRange(filteredText.length)
                )
            } else {
                newValue.copy(text = filteredText)
            }

            onValueChange(filteredText)
        },
        singleLine = true,
        label = {
            Text(
                text = label,
                fontWeight = FontWeight.Normal,
                fontSize = 12.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
            isError = isError,
        keyboardOptions = KeyboardOptions(
            keyboardType = keyboardType,
            imeAction = imeAction,
            capitalization = capitalization,
        ),
        keyboardActions = KeyboardActions(
            onNext = { focusManager.moveFocus(FocusDirection.Down) }
        ),
        trailingIcon = trailingIcon,
        leadingIcon = leadingIcon
    )
    /*
    if (isError) {
        Text(
            text = errorMessage,
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.bodySmall,
        )
    }
    */

}


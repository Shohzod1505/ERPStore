package ru.itis.kpfu.ermstore.ui.util

import ru.itis.kpfu.ermstore.domain.model.CashFilter
import ru.itis.kpfu.ermstore.domain.model.CashOperationType
import ru.itis.kpfu.ermstore.domain.model.DocumentStatus
import ru.itis.kpfu.ermstore.domain.model.DocumentType
import ru.itis.kpfu.ermstore.domain.model.PostingError

fun documentTypeLabel(type: DocumentType): String = when (type) {
	DocumentType.PURCHASE -> "Закупка"
	DocumentType.SALE -> "Продажа"
	DocumentType.INVENTORY -> "Инвентаризация"
}

fun documentStatusLabel(status: DocumentStatus): String = when (status) {
	DocumentStatus.DRAFT -> "Черновик"
	DocumentStatus.POSTED -> "Проведён"
	DocumentStatus.CANCELLED -> "Отменён"
}

fun cashTypeLabel(type: CashOperationType): String = when (type) {
	CashOperationType.INCOME -> "Доход"
	CashOperationType.EXPENSE -> "Расход"
	CashOperationType.DEBT_GIVEN -> "Выдали в долг"
	CashOperationType.DEBT_RECEIVED -> "Получили в долг"
	CashOperationType.DEBT_REPAID_TO_US -> "Вернули долг"
	CashOperationType.DEBT_REPAID_BY_US -> "Вернул долг"
}

fun cashFilterLabel(filter: CashFilter): String = when (filter) {
	CashFilter.ALL -> "Все"
	CashFilter.INCOME -> "Доходы"
	CashFilter.EXPENSE -> "Расходы"
	CashFilter.DEBT -> "Долги"
}

fun postingErrorText(error: PostingError): String = when (error) {
	PostingError.DocumentNotFound -> "Документ не найден"
	PostingError.DocumentIsNotDraft -> "Провести можно только черновик"
	PostingError.EmptyDocument -> "В документе нет строк"
	PostingError.InvalidLocation -> "Операции проводятся только по подскладам"
	is PostingError.InsufficientStock ->
		"Недостаточно товара <${error.productName}>: доступно ${error.availableUnits} шт., нужно ${error.requestedUnits} шт."
	is PostingError.InvalidLine -> "Некорректная строка документа: ${error.productName}"
	is PostingError.InsufficientCash ->
		"Недостаточно денег в кассе: доступно ${formatMoney(error.availableKopecks)}, нужно ${formatMoney(error.requestedKopecks)}"
}

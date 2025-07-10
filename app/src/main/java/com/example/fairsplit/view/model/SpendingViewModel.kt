package com.example.fairsplit.view.model

import android.util.Log

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.fairsplit.composeUi.screens.SplitMode
import com.example.fairsplit.data.TransferType
import com.example.fairsplit.data.User
import com.example.fairsplit.model.Firebase
import java.util.UUID
import kotlin.math.roundToLong

class SpendingViewModel(db: Firebase) : ViewModel() {

    var spendingTitle by mutableStateOf("")
    var totalAmount by mutableDoubleStateOf(0.0) //Ausgelegter Betrag
    var amountString by mutableStateOf("")
    var splitMode by mutableStateOf(SplitMode.EVENLY)
    var creditorId by mutableStateOf("")
    var groupId by mutableStateOf("")

    // Berechneter Wert: Summe der Beträge aller Personen
    var sumOfAmounts by mutableStateOf(0.0)
        private set

    // Differenz zwischen Gesamtbetrag und Summe der Beträge
    var difference by mutableStateOf(0.0)
        private set

    private val db = db
    var members = mutableStateListOf<UserModel>()
        private set

    fun getUsersInGroup(
        groupId: String,
        onSuccess: (List<User>) -> Unit,
        onFailure: (Exception) -> Unit,
    ) {
        db.getUsersInGroup(
            groupId = groupId,
            onSuccess = { users -> onSuccess(users) },
            onFailure = {}
        )
    }

    fun initAddSpending(activeUser: User?, groupId: String) {
        this.groupId = groupId
        updateTitle("")
        updateAmount("0,00")
        amountString = ""
        updateSplitMode(SplitMode.EVENLY)

        getUsersInGroup(
            groupId,
            onSuccess = { users ->
                members.clear()
                members.addAll(users.map {
                    UserModel(
                        name = it.name,
                        isSelected = true,
                        hasGreenCircle = it.id == activeUser?.id,
                        amount = 0.0,
                        id = it.id,
                        icon = it.icon
                    )
                })
                creditorId = activeUser?.id ?: ""
                recalculateSplit()
            },
            onFailure = {
                Log.d("AddSpendingScreen", "Error loading users")
            }
        )
    }

    fun updateTitle(newTitle: String) {
        spendingTitle = newTitle
    }

    // Eingabe als String, umwandeln in Double
    fun updateAmount(newAmountStr: String) {
        val parsed = newAmountStr.replace(',', '.').toDoubleOrNull()
        totalAmount = parsed ?: 0.0
        recalculateSplit()
        updateTotals()
    }

    fun updateTotalAmount(newTotalAmount: Long){

    }

    fun updateSplitMode(mode: SplitMode) {
        splitMode = mode
        recalculateSplit()
        updateTotals()
    }

    fun toggleUser(index: Int) {
        val person = members[index]
        members[index] = person.copy(isSelected = !person.isSelected, amount = 0.0, amountString = "0,00")
        recalculateSplit()
    }

    // Eingabe als String, speichern als Double
    fun updateIndividualAmount(index: Int, newAmountStr: String) {
        val parsed = newAmountStr.replace(',', '.').toDoubleOrNull() ?: 0.0
        members[index] = members[index].copy(amount = parsed, amountString = newAmountStr)
        updateTotals()
    }

    fun changeCreditor(index: Int) {
        val newCreditor = members[index]
        if (creditorId == newCreditor.id) return

        val oldIndex = members.indexOfFirst { it.id == creditorId }
        if (oldIndex != -1) {
            members[oldIndex] = members[oldIndex].copy(hasGreenCircle = false)
        }

        members[index] = newCreditor.copy(hasGreenCircle = true)
        creditorId = newCreditor.id
    }

    private fun recalculateSplit() {
        if (splitMode != SplitMode.EVENLY) return

        val totalCents = (totalAmount * 100).toInt()
        val selected = members.filter { it.isSelected }
        if (selected.isEmpty()) return

        val base = totalCents / selected.size
        val remainder = totalCents % selected.size
        val shuffled = selected.shuffled()

        val updated = members.map {
            if (!it.isSelected) {
                it.copy(amount = 0.0, amountString = "0,00")
            } else {
                val index = shuffled.indexOf(it)
                val centAmount = base + if (index < remainder) 1 else 0
                val euro = centAmount / 100.0
                val display = "%d,%02d".format(centAmount / 100, centAmount % 100)
                it.copy(amount = euro, amountString = display)
            }
        }

        members.clear()
        members.addAll(updated)
    }

    fun updateTotals() {
        val sum = members.sumOf { it.amount }
        sumOfAmounts = sum
        val diff = totalAmount - sum
        difference = if (kotlin.math.abs(diff) < 0.005) 0.0 else diff
    }

    fun createSettleUpSpending(sUcreditor: String, sUamount: Long, sUgroupId: String, sUdebtor: String, ) {
        val spendingId = UUID.randomUUID().toString()
        db.createSpending(
            title = "Begleichung",
            date = System.currentTimeMillis(),
            creditor = sUcreditor,
            individualAmount = sUamount,
            groupId = sUgroupId,
            debtor = sUdebtor,
            isPaid = false,
            totalAmount = sUamount,
            batchId = spendingId,
            transfer = TransferType.SETTLEUP,
            onFailure = {},
            onSuccess = {}
        )
    }
    fun createRegularSpendings(onSuccess: () -> Unit) {
        val spendingId = UUID.randomUUID().toString()

        members.forEach { debtor ->
            if (debtor.isSelected && debtor.amount != 0.0) {
                db.createSpending(
                    title = spendingTitle,
                    date = System.currentTimeMillis(),
                    creditor = creditorId,
                    individualAmount = (debtor.amount * 100).roundToLong(),
                    groupId = groupId,
                    debtor = debtor.id,
                    onSuccess = {},
                    onFailure = {},
                    isPaid = debtor.id == creditorId,
                    totalAmount = (totalAmount * 100).roundToLong(),
                    batchId = spendingId,
                    transfer = TransferType.REGULAR
                )
            }
        }
        onSuccess()
    }

    data class UserModel(
        val name: String,
        val isSelected: Boolean,
        val hasGreenCircle: Boolean,
        val amount: Double = 0.0,
        var amountString: String = "",
        val id: String,
        val icon: Int
    )

    sealed class ValidationResult {
        object Success : ValidationResult()
        data class Error(val message: String) : ValidationResult()
    }

    fun validateSpending(): ValidationResult {
        if (spendingTitle.isBlank()) {
            return ValidationResult.Error("Keine Bezeichnung angegeben")
        }

        if (totalAmount == 0.0) {
            return ValidationResult.Error("Ausgabe von 0 Euro ist ungültig.")
        }

        if (difference != 0.0) {
            return ValidationResult.Error("Die Beträge sind nicht korrekt aufgeteilt.")
        }


        return ValidationResult.Success
    }
}
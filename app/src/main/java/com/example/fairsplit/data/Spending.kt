package com.example.fairsplit.data

import java.util.Date

data class Spending(
    val title: String = "",
    val date: Date = Date(),
    val creditor: String = "",
    val debtor: String = "",
    val individualAmount: Long = 0, //Betrag in Cent angegeben
    val groupID: String = "",
    val spendingId: String = "",
    var isPaid: Boolean = false,
    var totalAmount: Long = 0,
    var batchId: String = "",   //ID um Spendings für dieselbe Sache zusammenzuhalten (z.B. Spendings fürs Kino -> Pro Person wird ein Spending erstellt)
    var transfer: TransferType = TransferType.REGULAR
)


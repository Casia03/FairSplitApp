package com.example.fairsplit.view.model

import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.fairsplit.data.Group
import com.example.fairsplit.data.GroupPreview
import com.example.fairsplit.data.Spending
import com.example.fairsplit.data.User
import com.example.fairsplit.model.Firebase
import com.example.fairsplit.session.SessionManager

class GroupViewModel(private val db: Firebase, private val sm: SessionManager) : ViewModel() {

    val currentUserId: State<String?> = derivedStateOf { sm.currentUser.value?.id }

    // General group-related state
    var groups by mutableStateOf<List<Group>>(emptyList())
        private set
    var group by mutableStateOf<Group?>(null)
    var groupBalanceMap = mutableStateMapOf<String, Long>()

    var transferList = mutableStateListOf<Triple<String,String,Long>>()
    val userNameMap = mutableStateMapOf<String, String>()
    var spendings by mutableStateOf<List<Spending>>(emptyList())
        private set
    var filterdSpendingsForCurrentUser by mutableStateOf<List<Spending>>(emptyList())
        private set
    var spendingsFromAllUserGroups by mutableStateOf<List<Spending>>(emptyList())
        private set

    var isLoading by mutableStateOf(false)
    var errorMessage by mutableStateOf<String?>(null)
        private set
    var totalBalance by mutableStateOf<Pair<Long, Long>?>(null)
        private set

    // Form data
    var groupName by mutableStateOf("")
    var groupDescription by mutableStateOf("")
    var creatorId: String? = ""
    var picture: Int = 1
    var memberIDs = mutableStateListOf<String>()

    lateinit var groupId: String
        private set

    fun setGroupId(id: String) {
        groupId = id
    }

    // UI State
    var showEditGroupDialog by mutableStateOf(false)
    var showGroupInvitationLink by mutableStateOf(false)
    var showJoinGroupDialog by mutableStateOf(false)

    //Confirm Dialog----------------------------------
    var showConfirmDialog by mutableStateOf(false)
        private set

    var confirmDialogMessage by mutableStateOf("")
        private set

    private var pendingAction: (() -> Unit)? = null

    fun requestConfirmation(message: String, action: () -> Unit) {
        confirmDialogMessage = message
        pendingAction = action
        showConfirmDialog = true
    }

    fun confirmAction() {
        pendingAction?.invoke()
        pendingAction = null
        showConfirmDialog = false
    }

    fun cancelAction() {
        pendingAction = null
        showConfirmDialog = false
    }
//--------------------------------

    fun checkIfUserIsMemberOrShouldJoin(groupId: String) {
        getUsersInGroup(
            groupId = groupId,
            onSuccess = { users ->
                showJoinGroupDialog = users.none { it.id == currentUserId.value }
            },
            onFailure = { e -> Log.e("JoinGroupDebug", "Fehler beim Laden der Nutzer: ${e.message}") }
        )
    }

    //Hat der User keine offenen Zahlungen mehr und ist er nicht der Creator der Gruppe?
    fun UserSaveToDelete(user: User): Boolean{
        var save = false
        if ((groupBalanceMap[user.id] == 0L || groupBalanceMap[user.id] == null) && user.id != group!!.creator) {
            save = true
        }
        return save
    }

    // Selected users
    val selectedUsers = mutableStateListOf<User>()

    // ---- User selection ----
    fun addUser(user: User) {
        if (selectedUsers.none { it.id == user.id }) {
            selectedUsers.add(user)
        }
    }

    fun getUsersInGroup(
        groupId: String,
        onSuccess: (List<User>) -> Unit,
        onFailure: (Exception) -> Unit
    ){
        db.getUsersInGroup(
            groupId,
            onSuccess = { users ->
                onSuccess(users)},
            onFailure = { e -> Log.e("JoinGroupDebug", "getUsersInGroup failed: ${e.message}")
                onFailure(e)}
        )
    }

    fun getUserNameById(
        userId: String,
        onSuccess: (String) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        db.getNameById(userId,
            onSuccess = { fetchedUser ->
                onSuccess(fetchedUser)

            },
            onFailure = { getUserByIdError ->
                onFailure(getUserByIdError)
            }
        )
    }

    fun removeUserWhileCreatingGroup(user: User) {
        selectedUsers.remove(user)
    }

    fun removeUsers(
        groupID: String,
        membersToRemove: MutableList<String>,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ){
        db.removeMembersFromGroup(
            groupId = groupID,
            membersToRemove = membersToRemove,
            onSuccess = onSuccess,
            onFailure = onFailure
        )
    }

    // ---- Group creation ----


    fun initGroupDetail(
        groupId: String,
        onSuccess: (Group) -> Unit,
        onFailure: (Exception) -> Unit
    ) {

        setGroupId(groupId)

        isLoading = true
        db.getGroupById(
            groupId = groupId,
            onSuccess = { fetchedGroup ->
                group = fetchedGroup
                db.getSpendingsByGroupId(
                    groupId = groupId,
                    onSuccess = { fetchedSpendings ->
                        spendings = fetchedSpendings

                        currentUserId.let { currentUserId ->
                            filterdSpendingsForCurrentUser =
                                filterSpendingsToDisplay(fetchedSpendings)
                        }
                        db.getUsersInGroup(
                            groupId = groupId,
                            onSuccess = { userList ->
                                groupBalanceMap.clear()
                                userList.forEach {
                                    groupBalanceMap[it.id] = 0L
                                }
                                initBalanceMap(groupId) {
                                    cacheUserNamesForGroup(groupId){
                                        transferList.clear()
                                        transferList.addAll(calculateTransfers())
                                        logTransfers(transferList)

                                        checkBalance()
                                    }
                                }
                            },
                            onFailure = { error ->
                                errorMessage = error.message
                            }
                        )

                        isLoading = false
                        onSuccess(fetchedGroup)
                    },
                    onFailure = { spendingsError ->
                        isLoading = false
                        onFailure(spendingsError)
                    }
                )
            },
            onFailure = { groupError ->
                isLoading = false
                onFailure(groupError)
            }
        )
    }


    fun createGroup(onSuccess: () -> Unit) {
        memberIDs.clear()
        selectedUsers.forEach { memberIDs.add(it.id) }

        db.createGroup(
            name = groupName,
            picture = picture,
            description = groupDescription,
            creatorId = creatorId,
            members = memberIDs,
            onSuccess = {
                onSuccess()
                resetAllValues()
            },
            onFailure = { e -> errorMessage = e.message }
        )
    }

    fun getAllUserGroups(
        onSuccess: (MutableList<Group>) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        db.getGroupsByUser(
            currentUserId.value,
            onSuccess = { group ->
                val groups = group.toMutableList()
                onSuccess(groups)
            },
            onFailure = { e ->
                onFailure(e)
            }
        )
    }

    fun getGroupPreviewsForUser(
        onSuccess: (List<GroupPreview>) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        Log.d("SessionManager", "Current User in GetGroupPreviewsForUser: $currentUserId.value")
        db.getGroupsByUser(
            currentUserId.value,
            onSuccess = { group ->
                Log.d("GroupViewModel","current user ins group: " + currentUserId.value)
                val previews = mutableListOf<GroupPreview>()
                val totalGroups = group.size
                var processedGroups = 0

                if (totalGroups == 0) {
                    onSuccess(emptyList())
                    return@getGroupsByUser
                }

                group.forEach { group ->
                    // Fetch last 3 spendings for each group
                    db.getSpendingsByGroupId(
                        groupId = group.id,
                        onSuccess = { spendings ->

                            // Sort spendings by date descending (assuming Spending has date field)
                            val sortedSpendings = spendings.sortedByDescending { it.date }
                            val lastThree = sortedSpendings.take(3)

                            previews.add(
                                GroupPreview(
                                    id = group.id,
                                    name = group.name,
                                    lastSpendings = lastThree
                                )
                            )

                            processedGroups++
                            if (processedGroups == totalGroups) {
                                onSuccess(previews)
                            }
                        },
                        onFailure = { e ->
                            processedGroups++
                            if (processedGroups == totalGroups) {
                                onSuccess(previews) // returning what we got so far if failed prematurely
                            }
                        }
                    )
                }
            },
            onFailure = { e ->
                onFailure(e)
            }
        )
    }

//    fun getTotalBalanceForUser(){
//
//        for()
//    }


    fun formatSpending(
        spending: Spending,
        onFormatted: (String) -> Unit,
        onError: (Exception) -> Unit
    ) {

        Log.d("GroupViewModel", "Formatting spending: $spending for currentUserId: $currentUserId")

        db.getUserById(spending.creditor, onSuccess = { creditorUser ->
            Log.d("GroupViewModel", "Creditor user fetched: ${creditorUser.name} (id=${spending.creditor})")

            db.getUserById(spending.debtor, onSuccess = { debtorUser ->
                Log.d("GroupViewModel", "Debtor user fetched: ${debtorUser.name} (id=${spending.debtor})")

                val amountEuro = spending.individualAmount / 100.0
                val creditorName = creditorUser.name
                val debtorName = debtorUser.name

                val formatted = when (currentUserId.value) {
                    spending.creditor -> "$debtorName schuldet dir %.2f €".format(amountEuro)
                    spending.debtor -> "Du schuldest $creditorName %.2f €".format(amountEuro)
                    else -> "$debtorName schuldet $creditorName %.2f €".format(amountEuro)
                }

                Log.d("GroupViewModel", "Formatted spending text: $formatted")
                onFormatted(formatted)

            }, onFailure = { e ->
                Log.e("GroupViewModel", "Failed to fetch debtor user: ${e.message}", e)
                onError(e)
            })

        }, onFailure = { e ->
            Log.e("GroupViewModel", "Failed to fetch creditor user: ${e.message}", e)
            onError(e)
        })
    }

    fun deleteGroup(onSuccess: () -> Unit, onFailure: (Exception) -> Unit){
        db.deleteGroup(groupID = groupId, onSuccess = onSuccess, onFailure = onFailure)
    }

    fun getGroupByID(
        groupID: String,
        onSuccess: (Group) -> Unit
    ){
        db.getGroupById(
            groupId = groupID,
            onSuccess = { fetchedGroup -> onSuccess(fetchedGroup)},
            onFailure = { e -> errorMessage = e.message })
    }

    // ---- Group member management ----
    fun addMembersToGroup(
        membersToAdd: MutableList<String>,
        onSuccess: () -> Unit
    ) {
        db.addMembersToGroup(
            groupId,
            membersToAdd,
            onSuccess = onSuccess,
            onFailure = { e -> errorMessage = e.message }
        )
    }

    fun removeMemberFromGroup(
        groupId: String,
        member: String,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        db.leaveGroup(
            groupId,
            member,
            onSuccess = onSuccess,
            onFailure = onFailure
        )
    }

    fun leaveGroup(
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        db.leaveGroup(
            groupId,
            currentUserId.value,
            onSuccess = onSuccess,
            onFailure = onFailure
        )
    }

    fun changeGroupName(
        groupId: String,
        newName: String,
        onSuccess: () -> Unit
    ) {
        db.changeGroupName(
            groupId,
            newName,
            onSuccess = onSuccess,
            onFailure = { e -> errorMessage = e.message }
        )
    }

    fun changeGroupDescription(
        groupId: String,
        newDescription: String?,
        onSuccess: () -> Unit
    ) {
        db.changeGroupDescription(
            groupId,
            newDescription,
            onSuccess = onSuccess,
            onFailure = { e -> errorMessage = e.message }
        )
    }

    fun editGroup(
        newName: String,
        newDescription: String
    ) {
        group = group?.copy(
            name = newName,
            description = newDescription
        )

        changeGroupName(groupId = group!!.id, newName = newName, onSuccess = {})
        changeGroupDescription(groupId = group!!.id, newDescription = newDescription, onSuccess = {})
    }

    fun changeGroupPicture(
        groupId: String,
        newPicture: String?,
        onSuccess: () -> Unit
    ) {
        db.changeGroupPicture(
            groupId,
            newPicture,
            onSuccess = onSuccess,
            onFailure = { e -> errorMessage = e.message }
        )
    }

    // ---- Utilities ----
    fun centToEuro(cents: Long): String {
        val euros = cents / 100.0
        return "%.2f€".format(euros)
    }

    sealed class ValidationResult {
        object Success : ValidationResult()
        data class Error(val message: String) : ValidationResult()
    }

    fun validateGroup(): ValidationResult {
        if (groupName.isBlank()) {
            return ValidationResult.Error("Kein Gruppenname angegeben")
        }
        return ValidationResult.Success
    }

    fun resetAllValues() {
        selectedUsers.clear()
        spendings = emptyList()
        isLoading = false
        groupName = ""
        groupDescription = ""
        creatorId = currentUserId.value
        picture = 1
        memberIDs.clear()
    }

    fun getFriendUsers(
        query: String,
        friendList: MutableList<User>,
        onSuccess: (List<User>) -> Unit
    ) {
        val result = friendList.filter { friend ->
            friend.name.contains(query, ignoreCase = true)
        }
        onSuccess(result)
    }


    fun filterSpendingsToDisplay(spendings: List<Spending>): List<Spending> {

        val groupedBySpendingBatchId = spendings.groupBy { it.batchId }
        val result = mutableListOf<Spending>()

        for ((batchId, spendingsWithSameId) in groupedBySpendingBatchId) {
            val isInvolved = spendingsWithSameId.any {
                it.debtor == currentUserId.value || it.creditor == currentUserId.value
            }
            val isInvolvedAsCreditor = spendingsWithSameId.any {
                it.debtor == currentUserId.value && it.creditor == currentUserId.value
            }

            /*Log.d("SpendingFilter", "SpendingDate: $batchId")
            Log.d("SpendingFilter", "Spendings in Gruppe: ${spendingsWithSameId.size}")
            Log.d("SpendingFilter", "isInvolved: $isInvolved")
            Log.d("SpendingFilter", "isInvolvedAsCreditor: $isInvolvedAsCreditor")*/

            if (isInvolvedAsCreditor) {
                val filtered = spendingsWithSameId.filter {
                    it.debtor == currentUserId.value && it.creditor == currentUserId.value
                }
                result += filtered
//                Log.d("SpendingFilter", "User ist Gläubiger & Schuldner → Hinzugefügt: ${filtered.size}")
            } else if (isInvolved) {
                val filtered = spendingsWithSameId.filter {
                    it.debtor == currentUserId.value || it.creditor == currentUserId.value
                }
                result += filtered
//                Log.d("SpendingFilter", "User ist beteiligt → Hinzugefügt: ${filtered.size}")
            } else {

                result += spendingsWithSameId.first()
//                Log.d("SpendingFilter", "User nicht beteiligt → Nur erstes Spending hinzugefügt")
            }
        }

        Log.d("SpendingFilter", "Finale Ergebnisliste: ${result.size} Spendings")
        result.forEach {
            Log.d("SpendingFilter", "→ ${it.batchId} | ${it.title} | Debtor: ${it.debtor} | Creditor: ${it.creditor}")
        }

        return result
    }

    fun spendingInvolved(batchId: String): Boolean =
        spendings.any { it.batchId == batchId && (it.debtor == currentUserId.value || it.creditor == currentUserId.value) }

    fun initBalanceMap(groupId: String, onComplete: () -> Unit){
        var groupSpendings: List<Spending>
        db.getSpendingsOfGroup(
            groupId = groupId,
            onSuccess = { spendingList ->
                groupSpendings = spendingList
                val groupedByPaid = groupSpendings.groupBy { it.isPaid }
                val unpaidSpendings = groupedByPaid[false] ?: emptyList()
                for(unpayedSpending in unpaidSpendings){
//                    Log.d("balanceMap", "In for Schleife ${unpayedSpending.individualAmount}, SpendingId ${unpayedSpending.spendingId}")
                    Log.d("balance","unpaid Spending: ${unpayedSpending}")
                    setBalanceMapValues(unpayedSpending.debtor,-unpayedSpending.individualAmount)
                    Log.d("balance"," unpayed Spending: ${unpayedSpending.individualAmount}")
                    setBalanceMapValues(unpayedSpending.creditor,unpayedSpending.individualAmount)
                    Log.d("balance"," unpayed Spending: ${unpayedSpending.individualAmount}")

                }
                onComplete()
            },
            onFailure = { exception ->
                errorMessage = exception.message
            }
        )
    }

    fun getTotalBalanceForUser(
        onComplete: (Pair<Long, Long>) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        getAllUserGroups(
            onSuccess = { allUserGroups ->
                if (allUserGroups.isEmpty()) {
                    onComplete(Pair(0L, 0L))
                    return@getAllUserGroups
                }

                val userId = currentUserId.value
                var totalPositiveBalance = 0L
                var totalNegativeBalance = 0L
                var groupsProcessed = 0

                allUserGroups.forEach { group ->
                    db.getSpendingsOfGroup(
                        groupId = group.id,
                        onSuccess = { spendings ->
                            val unpaidSpendings = spendings.filter { !it.isPaid }

                            // Calculate user's balance directly in this group
                            var groupBalance = 0L
                            unpaidSpendings.forEach { spending ->
                                if (spending.debtor == userId) {
                                    groupBalance -= spending.individualAmount
                                }
                                if (spending.creditor == userId) {
                                    groupBalance += spending.individualAmount
                                }
                            }

                            // Accumulate into totals
                            if (groupBalance > 0) {
                                totalPositiveBalance += groupBalance
                            } else {
                                totalNegativeBalance += groupBalance
                            }

                            groupsProcessed++
                            if (groupsProcessed == allUserGroups.size) {
                                onComplete(Pair(totalPositiveBalance, totalNegativeBalance))
                            }
                        },
                        onFailure = { e ->
                            onFailure(e)
                        }
                    )
                }
            },
            onFailure = { e ->
                onFailure(e)
            }
        )
    }


    fun setBalanceMapValues(userId: String, amount: Long){
        Log.d("balance"," Amount of added balance ${amount}")
        groupBalanceMap[userId] = groupBalanceMap[userId]?.plus(amount)?: 0L
    }

    fun calculateTransfers(): List<Triple<String,String,Long>> {
        val result = mutableListOf<Triple<String,String,Long>>()

        // Gläubiger = +balance, Schuldner = -balance
        val debtors = groupBalanceMap.filter { it.value < 0 }.toList().toMutableList()
        val creditors = groupBalanceMap.filter { it.value > 0 }.toList().toMutableList()

        for ((debtorId, debtorValue) in debtors) {
            var remainingDebt = -debtorValue  // z. B. -(-667) = 667 Cent

            var i = 0
            while (remainingDebt > 0 && i < creditors.size) {
                val (creditorId, creditorValue) = creditors[i]

                if (creditorValue == 0L) {
                    i++
                    continue
                }

                val amount = minOf(remainingDebt, creditorValue)

                // Übertrag speichern
                result.add(Triple(debtorId, creditorId, amount))

                // Local values aktualisieren
                remainingDebt -= amount
                creditors[i] = creditorId to creditorValue - amount
            }
        }

        return result
    }

    fun logTransfers(transfers: List<Triple<String, String, Long>>) {
        if (transfers.isEmpty()) {
            Log.d("Transfers", "Keine Ausgleichszahlungen notwendig.")
            return
        }

        transfers.forEach { (debtor, creditor, amount) ->
            Log.d("Transfers", "$debtor zahlt $amount an $creditor")
        }
    }

    fun cacheUserNamesForGroup(groupId: String, onComplete: () -> Unit) {
        db.getUsersInGroup(
            groupId = groupId,
            onSuccess = { userList ->
                userList.forEach { user ->
                    userNameMap[user.id] = user.name
                }
                onComplete()
            },
            onFailure = {
                // Fehlerbehandlung optional
                onComplete()
            }
        )
    }

    fun checkBalance(){ //Checkt ob balance <= 0 ist -> dann alle Spendings wo user Schuldet auf paid setzen
        Log.d("SetPaid","In CheckBalance")
        val balanceBalanced = groupBalanceMap.filter { it.value == 0L  }.keys
        for (userID in balanceBalanced) {
            spendings.filter { (it.creditor == userID || it.debtor == userID) && !it.isPaid }.forEach { db.setSpendingPaid(
                spendingId = it.spendingId,
                isPaid = true,
                onSuccess = { Log.d("setPaid", "For ${userID} - SpendingID: ${it.spendingId} - Title ${it.title} - Group ID ${it.groupID}Successfully set on true") },
                onFailure = {error -> errorMessage = error.message})
            }
        }
    }



    fun loadTotalBalance() {
        totalBalance = null
        getTotalBalanceForUser(
            onComplete = { balance ->
                totalBalance = balance
                Log.d("GroupViewModel","TotalBalance is: ${totalBalance} --------------------")

            },
            onFailure = {
                // optional: Fehlerbehandlung
            }
        )
    }

    fun loadAllSpendingsForUser() {
        getAllUserGroups(
            onSuccess = { allUserGroups ->
                val allSpendings = mutableListOf<Spending>()
                var remainingGroups = allUserGroups.size

                for (group in allUserGroups) {
                    db.getSpendingsOfGroup(
                        groupId = group.id,
                        onSuccess = { spendings ->
                            allSpendings.addAll(filterSpendingsToDisplay(spendings))
                            remainingGroups--

                            if (remainingGroups == 0) {
                                spendingsFromAllUserGroups = allSpendings.sortedByDescending { it.date }
                            }
                        },
                        onFailure = {
                            remainingGroups--
                            if (remainingGroups == 0) {
                                spendingsFromAllUserGroups = allSpendings.sortedByDescending { it.date }
                            }
                        }
                    )
                }
            },
            onFailure = {
                spendingsFromAllUserGroups = emptyList()
            }
        )
    }

    fun getGroupNameById(
        groupId: String,
        onSuccess: (String) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        db.getGroupNameById(
            groupId = groupId,
            onSuccess = { groupName ->
                onSuccess(groupName)
            },
            onFailure = { exception ->
                onFailure(exception)
            }
        )
    }
}

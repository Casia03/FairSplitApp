package com.example.fairsplit.model


import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
//Commit failed, comment to redo the commit

//Default stuff, materials, compose objects and so on
import android.util.Log
import com.example.fairsplit.data.Spending
import com.example.fairsplit.data.User
import com.example.fairsplit.data.Group
import com.example.fairsplit.data.TransferType
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.QuerySnapshot


class Firebase() {
    //Firebase connection
    //Alle datenbank anfragen, einträge usw passieren durch diese Klasse

    private val db = FirebaseFirestore.getInstance() // Database
    private val auth = FirebaseAuth.getInstance()

    /*
    Funktionen:
    - addFriendsToUser
    - addMembersToGroup
    - changeGroupDescription
    - changeGroupName
    - changeGroupPicture
    - checkIfAllIsPaid : Überprüft ob alle Spendings einer gegebenen Liste bezahlt sind
    - createGroup
    - createSpending
    - createUser
    - deleteGroup
    - getGroupByID
    - getGroupNameByID
    - getGroupsByUser : Alle Gruppen eines Users
    - getNameByID: Name von User
    - getSpendingByGroupID
    - getSpendingsOfGroup
    - getUserByID
    - getUserByIDLogin
    - getUsers : Alle existierenden User
    - getUsersByID
    - getUsersInGroup
    - leaveGroup : Selber die Gruppe verlassen
    - loginUser
    - removeFriendsFromUser
    - removeMembersFromGroup : Als ersteller der Gruppe jemand aus der Gruppe entfernen:
    - setSpendingPaid
    */


    fun createGroup(
        name: String,
        picture: Int,
        description: String,
        creatorId: String?,
        members: MutableList<String>,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit,
    ) {
        Log.d("Firebase", "Entered Funciton")
        val groupCollection = db.collection("groups")
        // Generate a new document reference with unique ID
        val newGroupRef = groupCollection.document()  // Firestore auto-generates an ID
        val groupId = newGroupRef.id

        val group = Group(
            name = name,
            picture = picture,
            description = description,
            creator = creatorId,
            members = mutableListOf<String>().apply {
                if (creatorId != null) {
                    add(creatorId)
                }          // Creator is first member by default
                addAll(members)
            },
            id = groupId
        )
        db.collection("groups")
            //
            .document(groupId)
            .set(group)
            .addOnSuccessListener {
                Log.d("Firebase", "Goup created Succesfully")
                onSuccess()
            }
            .addOnFailureListener { error ->
                Log.d("Firebase", "Error: ${error}")
            }
    }

    fun getSpendingsOfGroup(
        groupId: String,
        onSuccess: (List<Spending>) -> Unit,
        onFailure: (Exception) -> Unit,
    ) {
        db.collection("spendings")
            .whereEqualTo("groupID", groupId)
            .get()
            .addOnSuccessListener { querySnapshot ->
                val spendings =
                    querySnapshot.documents.mapNotNull { it.toObject(Spending::class.java) }
                onSuccess(spendings)
            }
            .addOnFailureListener { e ->
                onFailure(e)
            }
    }

    fun getNameById(
        userId: String,
        onSuccess: (String) -> Unit,
        onFailure: (Exception) -> Unit,
    ) {
        val userRef = db.collection("users").document(userId)
        userRef.get().addOnSuccessListener { snapshot ->
            if (snapshot.exists()) {
                val name = snapshot.getString("name") ?: "Unknown"
                onSuccess(name)
            } else {
                onFailure(Exception("User not found"))
            }
        }.addOnFailureListener { exception ->
            onFailure(exception)
        }
    }

    fun addMembersToGroup(
        groupId: String,
        membersToAdd: MutableList<String>,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit,
    ) {
        val groupDocRef = db.collection("groups").document(groupId)

        groupDocRef.get()
            .addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot.exists()) {
                    val currentMembers =
                        documentSnapshot.get("members") as? MutableList<String> ?: mutableListOf()

                    // Add new members without duplicates
                    for (member in membersToAdd) {
                        if (!currentMembers.contains(member)) {
                            currentMembers.add(member)
                        }
                    }

                    groupDocRef.update("members", currentMembers)
                        .addOnSuccessListener {
                            Log.d("Firebase", "Members added successfully")
                            onSuccess()
                        }
                        .addOnFailureListener { e ->
                            Log.e("Firebase", "Failed to update members", e)
                            onFailure(e)
                        }
                } else {
                    Log.e("Firebase", "Group document not found")
                    onFailure(Exception("Group not found"))
                }
            }
            .addOnFailureListener { e ->
                Log.e("Firebase", "Failed to get group document", e)
                onFailure(e)
            }
    }

    fun removeMembersFromGroup(
        groupId: String,
        membersToRemove: MutableList<String>,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit,
    ) {
        val groupDocRef = db.collection("groups").document(groupId)

        groupDocRef.get()
            .addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot.exists()) {
                    val currentMembers =
                        documentSnapshot.get("members") as? MutableList<String> ?: mutableListOf()
                    val creator = documentSnapshot.getString("creator") ?: ""

                    // Creator nicht entfernen
                    val updatedMembers = currentMembers.filter { member ->
                        !(membersToRemove.contains(member) && member != creator)
                    }.toMutableList()

                    // Update
                    groupDocRef.update("members", updatedMembers)
                        .addOnSuccessListener {
                            Log.d("Firebase", "Members removed successfully")
                            onSuccess()
                        }
                        .addOnFailureListener { e ->
                            Log.e("Firebase", "Failed to update members", e)
                            onFailure(e)
                        }
                } else {
                    Log.e("Firebase", "Group document not found")
                    onFailure(Exception("Group not found"))
                }
            }
            .addOnFailureListener { e ->
                Log.e("Firebase", "Failed to get group document", e)
                onFailure(e)
            }
    }

    fun leaveGroup(
        groupId: String,
        currentUser: String?,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit,
    ) {
        val groupDocRef = db.collection("groups").document(groupId)

        groupDocRef.get()
            .addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot.exists()) {
                    val members =
                        documentSnapshot.get("members") as? MutableList<String> ?: mutableListOf()
                    val creator = documentSnapshot.getString("creator") ?: ""

                    // Optional: prevent creator from leaving
                    if (currentUser == creator) {
                        onFailure(Exception("Creator cannot leave the group."))
                        return@addOnSuccessListener
                    }

                    // Get spendings of the group
                    getSpendingsOfGroup(
                        groupId,
                        onSuccess = { spendings ->

                            val hasOpenSpending = spendings.any { spending ->
                                spending.debtor == currentUser && !spending.isPaid
                            }
                            val stillGetsMoney = spendings.any { spending ->
                                spending.creditor == currentUser && !spending.isPaid
                            }

                            if (hasOpenSpending) {
                                onFailure(Exception("Begleiche deine Schulden, bevor du die Gruppe verlässt."))

                            } else if (stillGetsMoney) {
                                onFailure(Exception("Jemand schuldet dir noch Geld."))

                            } else {
                                // Safe to remove
                                val updatedMembers =
                                    members.filter { it != currentUser }.toMutableList()
                                groupDocRef.update("members", updatedMembers)
                                    .addOnSuccessListener {
                                        Log.d("Firebase", "User removed from group successfully")
                                        onSuccess()
                                    }
                                    .addOnFailureListener { e ->
                                        Log.e("Firebase", "Failed to remove user from group", e)
                                        onFailure(e)
                                    }
                            }
                        },
                        onFailure = { e ->
                            onFailure(e)
                        }
                    )
                } else {
                    onFailure(Exception("Group not found"))
                }
            }
            .addOnFailureListener { e ->
                onFailure(e)
            }
    }

    fun changeGroupName(
        groupId: String,
        newName: String,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit,
    ) {
        if (newName.isBlank()) {
            onFailure(IllegalArgumentException("Group name cannot be empty or blank"))
            return
        }

        val groupDocRef = db.collection("groups").document(groupId)
        groupDocRef.update("name", newName)
            .addOnSuccessListener {
                onSuccess()
            }
            .addOnFailureListener { e ->
                onFailure(e)
            }
    }

    fun changeGroupDescription(
        groupId: String,
        newDescription: String?,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit,
    ) {
        val groupDocRef = db.collection("groups").document(groupId)
        val updatedDescription = if (newDescription.isNullOrBlank()) null else newDescription

        groupDocRef.update("description", updatedDescription)
            .addOnSuccessListener {
                onSuccess()
            }
            .addOnFailureListener { e ->
                onFailure(e)
            }
    }

    fun changeGroupPicture(
        groupId: String,
        newPicture: String?,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit,
    ) {
        val groupDocRef = db.collection("groups").document(groupId)
        val updatedPicture = if (newPicture.isNullOrBlank()) null else newPicture

        groupDocRef.update("picture", updatedPicture)
            .addOnSuccessListener {
                onSuccess()
            }
            .addOnFailureListener { e ->
                onFailure(e)
            }
    }

    fun getGroupById(
        groupId: String,
        onSuccess: (Group) -> Unit,
        onFailure: (Exception) -> Unit,
    ) {

        val groupRef = db.collection("groups").document(groupId)
        groupRef.get()
            .addOnSuccessListener { snapshot ->
                if (snapshot != null) {
                    val group = snapshot.toObject(Group::class.java)
                    Log.d("getGroupById", "Data =  ${snapshot.data}")
                    if (group != null) {
                        onSuccess(group)
                    } else {
                        onFailure(Exception("Group data could not be parsed"))
                    }
                } else {
                    Log.d("getGroupById", "No Group with this ID found")
                }
            }
            .addOnFailureListener { exception ->
                Log.d("getGroupById", exception.toString())
            }
    }

    fun getGroupsByUser(
        userId: String?,
        onSuccess: (List<Group>) -> Unit,
        onFailure: (Exception) -> Unit,
    ) {
        if (userId == null) {
            onFailure(Exception("User ID is null"))
            return
        }
        val groups = mutableListOf<Group>()

        Log.d("GroupViewModel", "Starting queries for userId: $userId")

        val creatorQuery = db.collection("groups")
            .whereEqualTo("creator", userId)
            .get()

        val memberQuery = db.collection("groups")
            .whereArrayContains("members", userId)
            .get()

        Tasks.whenAllSuccess<QuerySnapshot>(creatorQuery, memberQuery)
            .addOnSuccessListener { snapshots ->
                Log.d("GroupViewModel", "Both queries succeeded. Processing results.")

                snapshots.forEach { snapshot ->
                    Log.d("GroupViewModel", "Snapshot size: ${snapshot.size()}")

                    for (document in snapshot.documents) {
                        val group = document.toObject(Group::class.java)
                        if (group != null) {
                            if (!groups.any { it.id == group.id }) {
                                groups.add(group)
                                Log.d(
                                    "GroupViewModel",
                                    "Added group: ${group.name} with id: ${group.id}"
                                )
                            } else {
                                Log.d("GroupViewModel", "Group already added: ${group.id}")
                            }
                        } else {
                            Log.d(
                                "GroupViewModel",
                                "Failed to parse group document: ${document.id}"
                            )
                        }
                    }
                }
                Log.d("GroupViewModel", "Total groups found: ${groups.size}")
                onSuccess(groups)
            }
            .addOnFailureListener { e ->
                Log.d("GroupViewModel", "Query failed with exception: ${e.message}")
                onFailure(e)
            }
    }


    fun getSpendingsByGroupId(
        groupId: String,
        onSuccess: (List<Spending>) -> Unit,
        onFailure: (Exception) -> Unit,
    ) {
        val spendingsRef = db.collection("spendings")
        spendingsRef
            .whereEqualTo("groupID", groupId)
            .get()
            .addOnSuccessListener { querySnapshot ->
                if (querySnapshot != null && !querySnapshot.isEmpty) {
                    val spendings =
                        querySnapshot.documents.mapNotNull { it.toObject(Spending::class.java) }
                    Log.d("getSpendingsByGroupId", "Found ${spendings.size} spendings")
                    onSuccess(spendings)
                } else {
                    Log.d("getSpendingsByGroupId", "No spendings found for groupId=$groupId")
                    onSuccess(emptyList()) // Optional: oder onFailure, je nach gewünschtem Verhalten
                }
            }
            .addOnFailureListener { exception ->
                Log.e("getSpendingsByGroupId", "Error loading spendings", exception)
                onFailure(exception)
            }
    }

    fun createSpending(
        title: String,
        date: Long,
        creditor: String,
        debtor: String,
        individualAmount: Long,
        groupId: String,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit,
        isPaid: Boolean,
        totalAmount: Long,
        batchId: String,
        transfer: TransferType,
    ) {
        val spendingCollection = db.collection("spendings")
        val newSpendingRef = spendingCollection.document()  // Firestore auto-generates an ID
        val spendingId = newSpendingRef.id

        val spending = Spending(
            title = title,
            date = java.sql.Date(date), //TODO read the Time in ViewModel
            creditor = creditor,
            debtor = debtor,
            individualAmount = individualAmount,
            spendingId = spendingId,
            groupID = groupId,
            isPaid = isPaid,
            totalAmount = totalAmount,
            batchId = batchId,
            transfer = transfer
        )
        db.collection("spendings")
            .document(spendingId)
            .set(spending)
            .addOnSuccessListener {
                onSuccess()
            }
            .addOnFailureListener { exception ->
                onFailure(exception)
            }
    }

    fun setSpendingPaid(
        spendingId: String,
        isPaid: Boolean,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit,
    ) {
        val spendingDocRef = db.collection("spendings").document(spendingId)

        spendingDocRef.update("paid", isPaid)
            .addOnSuccessListener {
                onSuccess()
            }
            .addOnFailureListener { e ->
                onFailure(e)
            }
    }

    fun createUser(
        name: String,
        email: String,
        password: String,
        onSuccess: (User) -> Unit,
        onFailure: (Exception) -> Unit,
    ) {
        Log.d("Firestore", "Starting createUser")

        val auth = FirebaseAuth.getInstance()
        val db = FirebaseFirestore.getInstance()

        auth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener { authResult ->
                val userId = authResult.user?.uid
                if (userId == null) {
                    Log.e("Firestore", "User ID is null after creation")
                    onFailure(Exception("User ID is null after creation"))
                    return@addOnSuccessListener
                }

                val user = User(
                    id = userId,
                    name = name,
                    name_lowercase = name.lowercase(),
                    email = email
                )

                db.collection("users")
                    .document(userId)
                    .set(user)
                    .addOnSuccessListener {
                        Log.d("Firestore", "User successfully created with ID: $userId")
                        onSuccess(user)
                    }
                    .addOnFailureListener { e ->
                        Log.e("Firestore", "Error saving user to Firestore", e)
                        onFailure(e)
                    }
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "User creation failed", e)
                onFailure(e)
            }
    }


    fun loginUser(
        email: String,
        password: String,
        onSuccess: (User) -> Unit,
        onFailure: (Exception) -> Unit,
    ) {
        if (email.isBlank() || password.isBlank()) {
            onFailure(Exception("Email and password must not be empty"))
            return
        }
        auth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener { authResult ->
                val uid = authResult.user?.uid ?: ""
                Log.d("LoginDebug", "Firebase auth success, uid: $uid")

                db.collection("users").document(uid)
                    .get()
                    .addOnSuccessListener { document ->
                        if (document.exists()) {
                            val user = document.toObject(User::class.java)
                            if (user != null) {
                                Log.d("LoginDebug", "User profile loaded successfully")
                                onSuccess(user)
                            } else {
                                Log.e("LoginDebug", "User profile conversion failed")
                                onFailure(Exception("Failed to parse user profile"))
                            }
                        } else {
                            Log.e("LoginDebug", "User document does not exist")
                            onFailure(Exception("No user profile found."))
                        }
                    }
                    .addOnFailureListener { e ->
                        Log.e("LoginDebug", "Failed to fetch user profile from Firestore", e)
                        onFailure(Exception("Error fetching user profile: ${e.localizedMessage}"))
                    }
            }
            .addOnFailureListener { e ->
                Log.e("LoginDebug", "Firebase auth failed", e)

                // Fehlermeldung
                val message = when (e) {
                    is FirebaseAuthInvalidUserException -> "No account found with this email."
                    is FirebaseAuthInvalidCredentialsException -> "Invalid password."
                    else -> e.localizedMessage ?: "Login failed due to an unknown error."
                }
                onFailure(Exception(message))
            }
    }

    fun getUserById(
        userId: String,
        onSuccess: (User) -> Unit,
        onFailure: (Exception) -> Unit,
    ) {
        db.collection("users")
            .document(userId)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val user = document.toObject(User::class.java)
                    if (user != null) {
                        onSuccess(user)
                    } else {
                        onFailure(Exception("Failed to parse user document"))
                    }
                } else {
                    onFailure(Exception("User with ID $userId does not exist"))
                }
            }
            .addOnFailureListener { e ->
                onFailure(e)
            }
    }

    //Get alle existierenden User
    fun getUsers(
        // User suche, fur freunde finden
        query: String,
        onSuccess: (List<User>) -> Unit,
        onFailure: (Exception) -> Unit,
    ) {
        Log.d("SearchUsers", "Starting search for query: '$query'")

        db.collection("users")
            .orderBy("name_lowercase")
            .startAt(query.lowercase())
            .endAt(query.lowercase() + '\uf8ff')
            .limit(20)
            .get()
            .addOnSuccessListener { snapshot ->
                Log.d(
                    "SearchUsers",
                    "Query successful. Number of documents: ${snapshot.size()}"
                ) //TODO ohne angabe

                val users = snapshot.documents.mapNotNull { doc ->
                    try {
                        val user = doc.toObject(User::class.java)
                        Log.d("SearchUsers", "Parsed user from doc '${doc.id}': $user")
                        user
                    } catch (e: Exception) {
                        Log.e("SearchUsers", "Failed to parse user from doc '${doc.id}'", e)
                        null
                    }
                }

                Log.d("SearchUsers", "Total users parsed: ${users.size}")
                onSuccess(users)
            }
            .addOnFailureListener { exception ->
                Log.e("SearchUsers", "Search failed with exception", exception)
                onFailure(exception)
            }
    }


    fun getUsersInGroup(
        groupId: String,
        onSuccess: (List<User>) -> Unit,
        onFailure: (Exception) -> Unit,
    ) {
        val groupsRef = db.collection("groups").document(groupId)

        groupsRef.get()
            .addOnSuccessListener { groupSnapshot ->
                if (!groupSnapshot.exists()) {
                    onFailure(Exception("Group not found"))
                    return@addOnSuccessListener
                }

                val group = groupSnapshot.toObject(Group::class.java)
                val memberIds = group?.members ?: emptyList()

                if (memberIds.isEmpty()) {
                    onSuccess(emptyList())
                    return@addOnSuccessListener
                }

                // Chunks von je max 10 IDs
                val chunks = memberIds.chunked(10)
                val users = mutableListOf<User>()
                var chunksLoaded = 0

                chunks.forEach { chunk ->
                    db.collection("users")
                        .whereIn(FieldPath.documentId(), chunk)
                        .get()
                        .addOnSuccessListener { snapshot ->

                            val newUsers = snapshot.documents.mapNotNull {
                                val user = it.toObject(User::class.java)
                                if (user == null) Log.w(
                                    "Firebase",
                                    "User konnte nicht vom doc erstellt werden ${it.id}"
                                )
                                user
                            }
                            users += newUsers
                            chunksLoaded++

                            if (chunksLoaded == chunks.size) {
                                onSuccess(users)
                            }
                        }

                }
            }
            .addOnFailureListener { e ->
                onFailure(e)
            }
    }

    fun getGroupNameById(
        groupId: String,
        onSuccess: (String) -> Unit,
        onFailure: (Exception) -> Unit,
    ) {
        db.collection("groups")
            .document(groupId)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val groupName = document.getString("name")
                    if (groupName != null) {
                        onSuccess(groupName)
                    } else {
                        onFailure(Exception("Group name is null for group ID $groupId"))
                    }
                } else {
                    onFailure(Exception("Group with ID $groupId does not exist"))
                }
            }
            .addOnFailureListener { e ->
                onFailure(e)
            }
    }

    fun getUserByIdLogin(
        uid: String,
        callback: (User?) -> Unit
    ) {
        db.collection("users").document(uid).get()
            .addOnSuccessListener { doc ->
                callback(doc.toObject(User::class.java))
            }
            .addOnFailureListener {
                callback(null)
            }
    }

    fun deleteGroup(
        groupID: String,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit,
    ) {
        val groupCollection = db.collection("groups")

        getSpendingsOfGroup(
            groupID,
            onSuccess = { spendings ->
                if (checkIfAllIsPaid(spendings)) {
                    groupCollection.document(groupID)
                        .delete()
                        .addOnSuccessListener {
                            Log.d("Firebase", "Group deleted Succesfully")
                            onSuccess()
                        }
                } else {
                    onFailure(Exception("Es stehen noch Zahlungen aus."))
                }
            },
            onFailure = { e -> onFailure(e) }
        )

    }

    //Überprüft ob alle Spendings einer gegebenen Liste bezahlt sind
    fun checkIfAllIsPaid(spendings: List<Spending>): Boolean {
        var allIsPaid = true
        for (spending in spendings) {
            if (!spending.isPaid) {
                allIsPaid = false
                break
            }
        }
        return allIsPaid
    }

    fun addFriendsToUser(
        userId: String,
        friendId: String,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit,
    ) {
        val usersRef = db.collection("users")

        db.runTransaction { transaction ->
            val userDoc = usersRef.document(userId)
            val friendDoc = usersRef.document(friendId)

            val userSnapshot = transaction.get(userDoc)
            val friendSnapshot = transaction.get(friendDoc)

            val userFriends =
                (userSnapshot.get("friends") as? List<String>)?.toMutableList() ?: mutableListOf()
            val friendFriends =
                (friendSnapshot.get("friends") as? List<String>)?.toMutableList() ?: mutableListOf()

            if (!userFriends.contains(friendId)) userFriends.add(friendId)
            if (!friendFriends.contains(userId)) friendFriends.add(userId)

            transaction.update(userDoc, "friends", userFriends)
            transaction.update(friendDoc, "friends", friendFriends)
        }.addOnSuccessListener { onSuccess() }
            .addOnFailureListener { e ->
                onFailure(e)
            }
    }

    fun removeFriendsFromUser(
        userId: String,
        friendId: String,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit,
    ) {
        val usersRef = db.collection("users")

        db.runTransaction { transaction ->
            val userDoc = usersRef.document(userId)
            val friendDoc = usersRef.document(friendId)

            val userSnapshot = transaction.get(userDoc)
            val friendSnapshot = transaction.get(friendDoc)

            val userFriends = userSnapshot.get("friends") as? MutableList<String> ?: mutableListOf()
            val friendFriends =
                friendSnapshot.get("friends") as? MutableList<String> ?: mutableListOf()

            userFriends.remove(friendId)
            friendFriends.remove(userId)

            transaction.update(userDoc, "friends", userFriends)
            transaction.update(friendDoc, "friends", friendFriends)
        }
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { e -> onFailure(e) }
    }
}




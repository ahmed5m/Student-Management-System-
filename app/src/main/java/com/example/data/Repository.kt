package com.example.data

import kotlinx.coroutines.flow.Flow

class EducationRepository(private val dao: EducationDao) {

    // Teacher
    suspend fun getTeacher(): Teacher? = dao.getTeacher()
    suspend fun saveTeacher(teacher: Teacher) = dao.insertTeacher(teacher)

    // Branches (السناتر)
    val allBranchesFlow: Flow<List<Branch>> = dao.getAllBranchesFlow()
    suspend fun getAllBranches(): List<Branch> = dao.getAllBranches()
    suspend fun getBranchById(id: Int): Branch? = dao.getBranchById(id)
    suspend fun insertBranch(branch: Branch) = dao.insertBranch(branch)
    suspend fun deleteBranch(id: Int) = dao.deleteBranch(id)

    // Assistants
    val allAssistantsFlow: Flow<List<Assistant>> = dao.getAllAssistantsFlow()
    suspend fun getAllAssistants(): List<Assistant> = dao.getAllAssistants()
    suspend fun insertAssistant(assistant: Assistant) = dao.insertAssistant(assistant)
    suspend fun deleteAssistant(id: Int) = dao.deleteAssistant(id)

    // Groups
    val allGroupsFlow: Flow<List<Group>> = dao.getAllGroupsFlow()
    suspend fun getAllGroups(): List<Group> = dao.getAllGroups()
    suspend fun getGroupById(id: Int): Group? = dao.getGroupById(id)
    suspend fun insertGroup(group: Group) = dao.insertGroup(group)
    suspend fun deleteGroup(id: Int) = dao.deleteGroup(id)

    // Students
    val allStudentsFlow: Flow<List<Student>> = dao.getAllStudentsFlow()
    suspend fun getAllStudents(): List<Student> = dao.getAllStudents()
    suspend fun getStudentByBarcode(barcode: String): Student? = dao.getStudentByBarcode(barcode)
    suspend fun insertStudent(student: Student) = dao.insertStudent(student)
    suspend fun updateStudent(student: Student) = dao.updateStudent(student)
    suspend fun deleteStudent(barcode: String) = dao.deleteStudent(barcode)

    // Attendance
    val allAttendanceFlow: Flow<List<Attendance>> = dao.getAllAttendanceFlow()
    suspend fun getAllAttendance(): List<Attendance> = dao.getAllAttendance()
    suspend fun getAttendanceForStudent(barcode: String): List<Attendance> = dao.getAttendanceForStudent(barcode)
    suspend fun getAttendanceForStudentInGroup(barcode: String, groupId: Int): Attendance? =
        dao.getAttendanceForStudentInGroup(barcode, groupId)
    suspend fun insertAttendance(attendance: Attendance) = dao.insertAttendance(attendance)
    suspend fun deleteAttendance(id: Int) = dao.deleteAttendance(id)

    // Materials
    val allMaterialsFlow: Flow<List<Material>> = dao.getAllMaterialsFlow()
    suspend fun getAllMaterials(): List<Material> = dao.getAllMaterials()
    suspend fun getMaterialsForBranch(branchId: Int): List<Material> = dao.getMaterialsForBranch(branchId)
    suspend fun insertMaterial(material: Material) = dao.insertMaterial(material)
    suspend fun updateMaterial(material: Material) = dao.updateMaterial(material)
    suspend fun deleteMaterial(id: Int) = dao.deleteMaterial(id)

    // Transactions
    val allTransactionsFlow: Flow<List<Transaction>> = dao.getAllTransactionsFlow()
    suspend fun getAllTransactions(): List<Transaction> = dao.getAllTransactions()
    suspend fun getTransactionsForStudent(barcode: String): List<Transaction> = dao.getTransactionsForStudent(barcode)
    suspend fun insertTransaction(transaction: Transaction) = dao.insertTransaction(transaction)
    suspend fun deleteTransaction(id: Int) = dao.deleteTransaction(id)
}

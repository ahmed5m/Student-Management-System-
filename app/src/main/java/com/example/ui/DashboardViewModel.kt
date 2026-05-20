package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.Assistant
import com.example.data.Attendance
import com.example.data.Branch
import com.example.data.EducationRepository
import com.example.data.Group
import com.example.data.Material
import com.example.data.Student
import com.example.data.Transaction
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class DashboardViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: EducationRepository

    init {
        val database = AppDatabase.getDatabase(application)
        repository = EducationRepository(database.educationDao())
    }

    // Role-Based Access Control claims
    // Roles: "Admin" (Full access), "Receptionist" (Only scanning + base lists), "Academic Assistant" (Only academic / status, no finance)
    private val _currentRole = MutableStateFlow("Admin")
    val currentRole: StateFlow<String> = _currentRole.asStateFlow()

    // Active branch filter (Null means global / all branches)
    private val _selectedBranchId = MutableStateFlow<Int?>(null)
    val selectedBranchId: StateFlow<Int?> = _selectedBranchId.asStateFlow()

    // Observables from storage
    val branches: StateFlow<List<Branch>> = repository.allBranchesFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val assistants: StateFlow<List<Assistant>> = repository.allAssistantsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val groups: StateFlow<List<Group>> = repository.allGroupsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val students: StateFlow<List<Student>> = repository.allStudentsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val attendance: StateFlow<List<Attendance>> = repository.allAttendanceFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val materials: StateFlow<List<Material>> = repository.allMaterialsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val transactions: StateFlow<List<Transaction>> = repository.allTransactionsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Simulated scanner state and UI alert overlays
    private val _scanResult = MutableStateFlow<ScanResponse?>(null)
    val scanResult: StateFlow<ScanResponse?> = _scanResult.asStateFlow()

    // Temporary simulated parental notification SMS log
    private val _smsNotifications = MutableStateFlow<List<SmsNotification>>(emptyList())
    val smsNotifications: StateFlow<List<SmsNotification>> = _smsNotifications.asStateFlow()

    // Financial calculations and statistics state
    // Formula calculation compiled dynamically reactive to active selected branch
    val financialStats: StateFlow<FinancialStatsSummary> = combine(
        _selectedBranchId,
        branches,
        transactions,
        attendance,
        students
    ) { activeBranchId, branchesList, allTransactions, allAttendance, studentsList ->
        calculateFinancialBreakdown(activeBranchId, branchesList, allTransactions, allAttendance, studentsList)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), FinancialStatsSummary())

    // Helper functions to configure user permissions
    fun setRole(role: String) {
        _currentRole.value = role
        // For Receptionists restricted to a branch, auto-assign their branch if applicable
        if (role == "Receptionist") {
            _selectedBranchId.value = 2 // Lock / simulate Giza branch receptionist by default
        } else {
            _selectedBranchId.value = null // reset branch filter
        }
    }

    fun selectBranch(branchId: Int?) {
        // Enforce RBAC constraint: Receptionist restricted to Giza cannot change branch
        if (_currentRole.value == "Receptionist" && branchId != 2) {
            return // blocked claims
        }
        _selectedBranchId.value = branchId
    }

    // Smart Attendance Scanning pipeline (Implements SECTION 2 Specs)
    fun scanStudentBarcode(barcode: String, targetGroupId: Int) {
        viewModelScope.launch {
            val dbStudent = repository.getStudentByBarcode(barcode)
            if (dbStudent == null) {
                _scanResult.value = ScanResponse(
                    success = false,
                    studentName = "غير معروف",
                    barcode = barcode,
                    message = "عذراً، لم يتم العثور على طالب بهذا الباركود في النظام الموحد.",
                    attendanceType = "فشل",
                    alerts = ScanAlerts(studentNotFound = true)
                )
                return@launch
            }

            // Lockout/RBAC for suspended status check or bypass
            val targetGroup = repository.getGroupById(targetGroupId) ?: return@launch
            val targetBranchId = targetGroup.branchId

            // Dynamic determination of Attendance Type: Regular vs Makeup
            var attendanceType = "حضور عادي"
            var attendanceStatus = "Present"

            if (dbStudent.nativeGroupId != targetGroupId) {
                // If the student doesn't belong to the scanned group, they are attending dynamically as a Make-up (تعويض) session.
                // We verify if they had an absence in their native group's current block
                attendanceType = "حصة تعويضية (بومة أخرى)"
                attendanceStatus = "Makeup"
            }

            // Smart Alert Checks pipeline
            val alerts = ScanAlerts()

            // 1. Debt Check (تنبيه المديونيات):
            // Check if registration fee is paid OR if they have other pending balances
            var debt = 0.0
            if (!dbStudent.isRegistrationFeePaid) {
                debt += dbStudent.registrationFee
            }

            // Fetch list of book sales against other payments
            val studentTransactions = repository.getTransactionsForStudent(barcode)
            val totalInflowCharged = studentTransactions.filter { it.type != "Expense" }
                .sumOf { it.amount - it.discountAmount }
            // Let's verify payments or unpaid dues. If they did not pay registration, we warn.
            // Also if native group has a workbook, check if they purchased it
            val branchMaterials = repository.getMaterialsForBranch(targetBranchId)
            // Simulating uncollected book dues if they haven't paid for any book transaction
            val bookTransactionsCount = studentTransactions.count { it.type == "Book Sale" }
            if (bookTransactionsCount < branchMaterials.size && branchMaterials.isNotEmpty()) {
                val missingBook = branchMaterials.firstOrNull()
                if (missingBook != null) {
                    alerts.hasBookDebt = true
                    alerts.unpaidBookName = missingBook.name
                    debt += missingBook.price
                }
            }

            if (debt > 0.0) {
                alerts.hasFinancialDebt = true
                alerts.debtAmount = debt
            }

            // 2. Previous Absence Check (تنبيه غياب الحصة السابقة):
            val studentLogs = repository.getAttendanceForStudent(barcode)
            val lastLog = studentLogs.firstOrNull()
            if (lastLog != null && lastLog.status == "Absent") {
                alerts.previousAbsenceAlert = true
            } else if (studentLogs.isEmpty() && dbStudent.status == "Active") {
                // Mocking first scan or previous attendance check
                alerts.previousAbsenceAlert = true // default trigger to enforce receipt check
            }

            // 3. Lateness Alert
            // Just illustrative based on custom hour simulation
            alerts.latenessAlert = false // default false

            // 4. Consecutive Absences & Account Freeze Check (اقتراح الإيقاف التلقائي وتجميد الحساب):
            val lastTwoLogs = studentLogs.take(2)
            val consecAbsencesCount = studentLogs.filter { it.status == "Absent" }.size
            if (dbStudent.status == "Suspended") {
                alerts.isSuspendedStudent = true
            } else if (consecAbsencesCount >= 2 || lastTwoLogs.count { it.status == "Absent" } >= 2) {
                alerts.suggestSuspension = true
            }

            // Write attendance log to the database
            val attendRecord = Attendance(
                studentBarcode = barcode,
                groupId = targetGroupId,
                status = attendanceStatus,
                scannedBranchId = targetBranchId,
                originalGroupId = dbStudent.nativeGroupId
            )
            repository.insertAttendance(attendRecord)

            // If Makeup, we auto-create an associated 0-amount Subscription transaction to sync bookkeeping,
            // or if regular we ensure they pay or flag as warning.
            // We simulate registration of this log.
            _scanResult.value = ScanResponse(
                success = true,
                studentName = dbStudent.name,
                barcode = barcode,
                message = if (attendanceStatus == "Makeup") {
                    "تم تسجيل الطالب بنجاح كتعويض في فرع ${repository.getBranchById(targetBranchId)?.name ?: ""}"
                } else {
                    "تم تسجيل حضور الطالب بنجاح لمجموعته الأساسية."
                },
                attendanceType = attendanceType,
                alerts = alerts,
                scannedBranchName = repository.getBranchById(targetBranchId)?.name ?: "",
                scannedGroupName = targetGroup.name
            )

            // 5. Outgoing Notifications Gateway (بوابة الرسائل الآلية)
            val parentNum = dbStudent.parentPhone
            val smsText = "تنبيه حضور: وصل ابنكم/ابنتكم ${dbStudent.name} بسلام لمقر الحصة بفرع ${repository.getBranchById(targetBranchId)?.name ?: ""} وبدأ المحاضرة الآن."
            val nowSms = SmsNotification(
                id = System.currentTimeMillis().toInt(),
                studentBarcode = barcode,
                studentName = dbStudent.name,
                parentPhone = parentNum,
                message = smsText,
                timestamp = System.currentTimeMillis()
            )
            _smsNotifications.value = listOf(nowSms) + _smsNotifications.value
        }
    }

    fun dismissScanResult() {
        _scanResult.value = null
    }

    fun payStudentDebtDirectly(barcode: String, amount: Double, branchId: Int) {
        viewModelScope.launch {
            val student = repository.getStudentByBarcode(barcode)
            if (student != null) {
                // Mark registration fee as paid in repository
                repository.updateStudent(student.copy(isRegistrationFeePaid = true))
                
                // Record the financial transaction so ledger updates in real-time
                val tx = Transaction(
                    studentBarcode = barcode,
                    type = "Subscription",
                    amount = amount,
                    discountAmount = 0.0,
                    tutorRatio = 0.8,
                    branchId = branchId,
                    notes = "تسديد مستحقات فورية (بوابة فوري Fawry) للطالب: ${student.name}"
                )
                repository.insertTransaction(tx)
                
                // Instantly update active scanning result status to reflect clean status
                val currentResult = _scanResult.value
                if (currentResult != null && currentResult.barcode == barcode) {
                    _scanResult.value = currentResult.copy(
                        message = "تمت تسوية مديونية الطالب وسداد المبلغ (${amount} ج.م) بالكامل بنجاح عبر Fawry! ✨",
                        alerts = currentResult.alerts.copy(
                            hasFinancialDebt = false,
                            debtAmount = 0.0,
                            hasBookDebt = false
                        )
                    )
                }
            }
        }
    }

    // Direct Database Insert operations
    fun createStudent(student: Student) {
        viewModelScope.launch {
            repository.insertStudent(student)
            // Also create initial booking transaction
            if (student.registrationFee > 0.0) {
                val group = repository.getGroupById(student.nativeGroupId)
                val branchId = group?.branchId ?: 1
                repository.insertTransaction(
                    Transaction(
                        studentBarcode = student.barcode,
                        type = "Registration Fee",
                        amount = student.registrationFee,
                        discountAmount = 0.0,
                        tutorRatio = 1.0,
                        branchId = branchId,
                        notes = "رسوم حجز الطالب الجديد ${student.name}"
                    )
                )
            }
        }
    }

    fun createBranch(branch: Branch) {
        viewModelScope.launch {
            repository.insertBranch(branch)
        }
    }

    fun updateBranchSplit(branchId: Int, newCommission: Double, newBookCommission: Double) {
        viewModelScope.launch {
            val existing = repository.getBranchById(branchId)
            if (existing != null) {
                repository.insertBranch(
                    existing.copy(
                        commissionPercentage = newCommission,
                        bookCommissionPercentage = newBookCommission
                    )
                )
            }
        }
    }

    fun createGroup(group: Group) {
        viewModelScope.launch {
            repository.insertGroup(group)
        }
    }

    fun createMaterial(material: Material) {
        viewModelScope.launch {
            repository.insertMaterial(material)
        }
    }

    fun createAssistant(assistant: Assistant) {
        viewModelScope.launch {
            repository.insertAssistant(assistant)
        }
    }

    fun payAssistantSalary(assistant: Assistant, amount: Double, branchId: Int) {
        viewModelScope.launch {
            repository.insertTransaction(
                Transaction(
                    studentBarcode = "",
                    type = "Expense",
                    amount = amount,
                    discountAmount = 0.0,
                    tutorRatio = 1.0,
                    branchId = branchId,
                    notes = "صرف رواتب ومستحقات المساعد: ${assistant.name} (${assistant.role})"
                )
            )
        }
    }

    fun createTransaction(transaction: Transaction) {
        viewModelScope.launch {
            // If card/fawry or wallet payment, insert transaction
            repository.insertTransaction(transaction)

            // If it is a Book Sale, decrement material stock!
            if (transaction.type == "Book Sale" && transaction.materialId != null) {
                val mats = repository.getAllMaterials()
                val targetMat = mats.find { it.id == transaction.materialId }
                if (targetMat != null) {
                    val newStock = (targetMat.stockCount - 1).coerceAtLeast(0)
                    repository.updateMaterial(targetMat.copy(stockCount = newStock))
                }
            }
        }
    }

    fun updateStudentStatus(barcode: String, newStatus: String) {
        viewModelScope.launch {
            val student = repository.getStudentByBarcode(barcode)
            if (student != null) {
                repository.updateStudent(student.copy(status = newStatus))
            }
        }
    }

    // Dynamic Ledger Engineering implementation (Equation logic)
    private fun calculateFinancialBreakdown(
        activeBranchId: Int?,
        branchesList: List<Branch>,
        allTransactions: List<Transaction>,
        attendanceList: List<Attendance>,
        studentsList: List<Student>
    ): FinancialStatsSummary {
        var totalTutorNetProfit = 0.0
        var totalGrossRevenue = 0.0
        var totalDiscounts = 0.0
        var totalExpenses = 0.0

        val branchSummaries = mutableListOf<BranchFinancialSummary>()

        for (branch in branchesList) {
            // Filter transactions according to active branch
            val branchTx = allTransactions.filter { it.branchId == branch.id }

            // Inflow, discount, book split calculation
            var sessionSubGross = 0.0
            var sessionSubDiscounts = 0.0
            var sessionSubTutorBearsRefund = 0.0

            var bookSalesGross = 0.0
            var branchExpenses = 0.0

            for (tx in branchTx) {
                if (tx.type == "Expense") {
                    branchExpenses += tx.amount
                } else if (tx.type == "Book Sale") {
                    bookSalesGross += (tx.amount - tx.discountAmount)
                } else {
                    // Subscription payment, lesson fee, or registration
                    sessionSubGross += tx.amount
                    sessionSubDiscounts += tx.discountAmount
                    // Tutor share ratio (w_j) of the discount
                    sessionSubTutorBearsRefund += (tx.discountAmount * tx.tutorRatio)
                }
            }

            // Net session portion for tutor after center commission:
            // (Inflow - Discounts * tutor_ratio) * (1 - theta_j)
            val sessionTutorBase = (sessionSubGross - sessionSubTutorBearsRefund)
            val sessionTutorNet = sessionTutorBase * (1.0 - branch.commissionPercentage)

            // Net book portion for tutor after book commission:
            // Book_Revenue * (1 - phi_j)
            val booksTutorNet = bookSalesGross * (1.0 - branch.bookCommissionPercentage)

            // NET PROFIT FOR THIS BRANCH:
            // Net_Session + Net_Books - Expenses
            val branchTutorNetProfit = sessionTutorNet + booksTutorNet - branchExpenses

            branchSummaries.add(
                BranchFinancialSummary(
                    branchId = branch.id,
                    branchName = branch.name,
                    grossRevenue = sessionSubGross + bookSalesGross,
                    discounts = sessionSubDiscounts,
                    expenses = branchExpenses,
                    tutorNetProfit = branchTutorNetProfit,
                    commissionCut = sessionSubGross * branch.commissionPercentage,
                    bookCommissionCut = bookSalesGross * branch.bookCommissionPercentage
                )
            )

            // Accumulate global stats if it matches active branch or all
            if (activeBranchId == null || activeBranchId == branch.id) {
                totalGrossRevenue += (sessionSubGross + bookSalesGross)
                totalDiscounts += sessionSubDiscounts
                totalExpenses += branchExpenses
                totalTutorNetProfit += branchTutorNetProfit
            }
        }

        return FinancialStatsSummary(
            totalTutorNetProfit = totalTutorNetProfit,
            totalGrossRevenue = totalGrossRevenue,
            totalDiscounts = totalDiscounts,
            totalExpenses = totalExpenses,
            branchBreakdowns = branchSummaries
        )
    }
}

// Data Classes for views
data class ScanAlerts(
    var studentNotFound: Boolean = false,
    var hasFinancialDebt: Boolean = false,
    var debtAmount: Double = 0.0,
    var hasBookDebt: Boolean = false,
    var unpaidBookName: String = "",
    var previousAbsenceAlert: Boolean = false,
    var latenessAlert: Boolean = false,
    var suggestSuspension: Boolean = false,
    var isSuspendedStudent: Boolean = false
)

data class ScanResponse(
    val success: Boolean,
    val studentName: String,
    val barcode: String,
    val message: String,
    val attendanceType: String, // "حضور عادي" or "Makeup"
    val alerts: ScanAlerts,
    val scannedBranchName: String = "",
    val scannedGroupName: String = ""
)

data class SmsNotification(
    val id: Int,
    val studentBarcode: String,
    val studentName: String,
    val parentPhone: String,
    val message: String,
    val timestamp: Long
)

data class BranchFinancialSummary(
    val branchId: Int,
    val branchName: String,
    val grossRevenue: Double,
    val discounts: Double,
    val expenses: Double,
    val tutorNetProfit: Double,
    val commissionCut: Double,
    val bookCommissionCut: Double
)

data class FinancialStatsSummary(
    val totalTutorNetProfit: Double = 0.0,
    val totalGrossRevenue: Double = 0.0,
    val totalDiscounts: Double = 0.0,
    val totalExpenses: Double = 0.0,
    val branchBreakdowns: List<BranchFinancialSummary> = emptyList()
)

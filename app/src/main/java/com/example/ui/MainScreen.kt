package com.example.ui

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.*
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(viewModel: DashboardViewModel) {
    // Force Left-to-Right layout container to look highly professional or RTL for native Arabic
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        val currentRole by viewModel.currentRole.collectAsStateWithLifecycle()
        val selectedBranchId by viewModel.selectedBranchId.collectAsStateWithLifecycle()

        val branches by viewModel.branches.collectAsStateWithLifecycle()
        val groups by viewModel.groups.collectAsStateWithLifecycle()
        val students by viewModel.students.collectAsStateWithLifecycle()
        val attendanceList by viewModel.attendance.collectAsStateWithLifecycle()
        val materials by viewModel.materials.collectAsStateWithLifecycle()
        val transactions by viewModel.transactions.collectAsStateWithLifecycle()
        val financialSummary by viewModel.financialStats.collectAsStateWithLifecycle()
        val scanResult by viewModel.scanResult.collectAsStateWithLifecycle()
        val smsNotifications by viewModel.smsNotifications.collectAsStateWithLifecycle()
        val assistants by viewModel.assistants.collectAsStateWithLifecycle()

        var currentTab by remember { mutableStateOf(0) }

        // Dialog states
        var showAddStudentDialog by remember { mutableStateOf(false) }
        var showAddBranchDialog by remember { mutableStateOf(false) }
        var showAddGroupDialog by remember { mutableStateOf(false) }
        var showAddMaterialDialog by remember { mutableStateOf(false) }
        var showAddExpenseDialog by remember { mutableStateOf(false) }
        var showAddAssistantDialog by remember { mutableStateOf(false) }
        var showBackupSuccessDialog by remember { mutableStateOf(false) }
        var showAddIncomeDialog by remember { mutableStateOf(false) }

        Scaffold(
            topBar = {
                Surface(
                    color = HeaderBackground,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .statusBarsPadding()
                            .padding(horizontal = 16.dp, vertical = 14.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                SYTriangleLogo()
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "نظام الإدارة الرقمي",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = DeepTealPrimary
                                    )
                                    Text(
                                        text = "الأستاذ أحمد الشافعي • سنتر النور",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MintSecondary
                                    )
                                }
                            }

                            // Role simulation switcher (Calm, precise styling)
                            var showRoleMenu by remember { mutableStateOf(false) }
                            Box {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color.White)
                                        .border(1.dp, OutlineBorderColor, RoundedCornerShape(8.dp))
                                        .clickable { showRoleMenu = true }
                                        .padding(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Security,
                                        contentDescription = null,
                                        modifier = Modifier.size(14.dp),
                                        tint = MintSecondary
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = when(currentRole) {
                                            "Admin" -> "مدير / مدرس 👑"
                                            "Receptionist" -> "موظف استقبال 🔑"
                                            else -> "مساعد أكاديمي 📝"
                                        },
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = DeepTealPrimary
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Icon(
                                        imageVector = Icons.Default.ArrowDropDown,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp),
                                        tint = MintSecondary
                                    )
                                }
                                DropdownMenu(
                                    expanded = showRoleMenu,
                                    onDismissRequest = { showRoleMenu = false }
                                ) {
                                    DropdownMenuItem(
                                        text = { Text("المدرس والمشرف العام (ADMIN)", fontWeight = FontWeight.Bold, fontSize = 12.sp) },
                                        onClick = {
                                            viewModel.setRole("Admin")
                                            showRoleMenu = false
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("سكرتارية واستقبال (Receptionist)", fontSize = 12.sp) },
                                        onClick = {
                                            viewModel.setRole("Receptionist")
                                            showRoleMenu = false
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("مساعد أكاديمي ومقيّم (Academic)", fontSize = 12.sp) },
                                        onClick = {
                                            viewModel.setRole("Academic Assistant")
                                            showRoleMenu = false
                                        }
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Branch Context Selector (Clean horizontal layout)
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "الفرع النشط:",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = DeepTealPrimary
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Row(
                                modifier = Modifier
                                    .weight(1f)
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                val allBranchesSelected = selectedBranchId == null
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(20.dp))
                                        .background(if (allBranchesSelected) DeepTealPrimary else Color.White)
                                        .border(
                                            1.dp,
                                            if (allBranchesSelected) Color.Transparent else OutlineBorderColor,
                                            RoundedCornerShape(20.dp)
                                        )
                                        .clickable { viewModel.selectBranch(null) }
                                        .padding(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = "جميع الفروع جغرافياً",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (allBranchesSelected) Color.White else MintSecondary
                                    )
                                }

                                branches.forEach { branch ->
                                    val isSelected = selectedBranchId == branch.id
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(20.dp))
                                            .background(if (isSelected) DeepTealPrimary else Color.White)
                                            .border(
                                                1.dp,
                                                if (isSelected) Color.Transparent else OutlineBorderColor,
                                                RoundedCornerShape(20.dp)
                                            )
                                            .clickable { viewModel.selectBranch(branch.id) }
                                            .padding(horizontal = 12.dp, vertical = 6.dp)
                                    ) {
                                        Text(
                                            text = branch.name,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isSelected) Color.White else MintSecondary
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            },
            bottomBar = {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 8.dp,
                    windowInsets = WindowInsets.navigationBars
                ) {
                    NavigationBarItem(
                        selected = currentTab == 0,
                        onClick = { currentTab = 0 },
                        label = { Text("تحضير الباركود", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                        icon = { Icon(imageVector = Icons.Default.QrCodeScanner, contentDescription = null) }
                    )
                    NavigationBarItem(
                        selected = currentTab == 1,
                        onClick = { currentTab = 1 },
                        label = { Text("الطلاب والمجموعات", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                        icon = { Icon(imageVector = Icons.Default.People, contentDescription = null) }
                    )
                    NavigationBarItem(
                        selected = currentTab == 2,
                        onClick = { currentTab = 2 },
                        label = { Text("الحسابات والماليات", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                        icon = { Icon(imageVector = Icons.Default.Payments, contentDescription = null) }
                    )
                    NavigationBarItem(
                        selected = currentTab == 3,
                        onClick = { currentTab = 3 },
                        label = { Text("الفروع والمخاطر", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                        icon = { Icon(imageVector = Icons.Default.Business, contentDescription = null) }
                    )
                }
            }
        ) { padding ->

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .background(MaterialTheme.colorScheme.background)
            ) {
                when(currentTab) {
                    0 -> AttendanceScannerView(
                        viewModel = viewModel,
                        groups = groups,
                        branches = branches,
                        students = students,
                        smsNotifications = smsNotifications,
                        scanResult = scanResult,
                        financialSummary = financialSummary,
                        attendanceList = attendanceList,
                        onTabSelect = { currentTab = it }
                    )
                    1 -> StudentsAndGroupsView(
                        viewModel = viewModel,
                        students = students,
                        groups = groups,
                        branches = branches,
                        currentRole = currentRole,
                        onAddStudent = { showAddStudentDialog = true },
                        onAddGroup = { showAddGroupDialog = true }
                    )
                    2 -> FinancialLedgerView(
                        viewModel = viewModel,
                        financialSummary = financialSummary,
                        selectedBranchId = selectedBranchId,
                        branches = branches,
                        assistants = assistants,
                        attendanceList = attendanceList,
                        currentRole = currentRole,
                        onPaySalary = { assistant ->
                            // Open direct expense payout for assistant
                            val branchId = assistant.assignedBranchId ?: selectedBranchId ?: 1
                            viewModel.payAssistantSalary(assistant, assistant.salaryValue, branchId)
                        },
                        onAddExpense = { showAddExpenseDialog = true },
                        onAddIncome = { showAddIncomeDialog = true }
                    )
                    3 -> BranchesAndResilienceView(
                        viewModel = viewModel,
                        branches = branches,
                        materials = materials,
                        assistants = assistants,
                        currentRole = currentRole,
                        onAddBranch = { showAddBranchDialog = true },
                        onAddMaterial = { showAddMaterialDialog = true },
                        onAddAssistant = { showAddAssistantDialog = true },
                        onRunBackup = { showBackupSuccessDialog = true }
                    )
                }
            }
        }

        // DIALOGS POPUPS IMPLEMENTATION
        if (showAddStudentDialog) {
            AddStudentDialog(
                groups = groups,
                branches = branches,
                onDismiss = { showAddStudentDialog = false },
                onAdd = { newStudent ->
                    viewModel.createStudent(newStudent)
                    showAddStudentDialog = false
                }
            )
        }

        if (showAddBranchDialog) {
            AddBranchDialog(
                onDismiss = { showAddBranchDialog = false },
                onAdd = { newBranch ->
                    viewModel.createBranch(newBranch)
                    showAddBranchDialog = false
                }
            )
        }

        if (showAddGroupDialog) {
            AddGroupDialog(
                branches = branches,
                onDismiss = { showAddGroupDialog = false },
                onAdd = { newGroup ->
                    viewModel.createGroup(newGroup)
                    showAddGroupDialog = false
                }
            )
        }

        if (showAddMaterialDialog) {
            AddMaterialDialog(
                branches = branches,
                onDismiss = { showAddMaterialDialog = false },
                onAdd = { newMaterial ->
                    viewModel.createMaterial(newMaterial)
                    showAddMaterialDialog = false
                }
            )
        }

        if (showAddAssistantDialog) {
            AddAssistantDialog(
                branches = branches,
                onDismiss = { showAddAssistantDialog = false },
                onAdd = { newAssistant ->
                    viewModel.createAssistant(newAssistant)
                    showAddAssistantDialog = false
                }
            )
        }

        if (showAddExpenseDialog) {
            AddExpenseDialog(
                branches = branches,
                onDismiss = { showAddExpenseDialog = false },
                onAdd = { tx ->
                    viewModel.createTransaction(tx)
                    showAddExpenseDialog = false
                }
            )
        }

        if (showAddIncomeDialog) {
            AddIncomeDialog(
                branches = branches,
                students = students,
                materials = materials,
                onDismiss = { showAddIncomeDialog = false },
                onAdd = { tx ->
                    viewModel.createTransaction(tx)
                    showAddIncomeDialog = false
                }
            )
        }

        if (showBackupSuccessDialog) {
            AlertDialog(
                onDismissRequest = { showBackupSuccessDialog = false },
                confirmButton = {
                    Button(onClick = { showBackupSuccessDialog = false }) {
                        Text("حسناً")
                    }
                },
                title = { Text("المزامنة والنسخ الاحتياطي الهجين ⚡", fontWeight = FontWeight.Bold, color = DeepTealPrimary) },
                text = {
                    Column {
                        Text("تم إجراء عملية نسخ احتياطي محلي مشفر (Encrypted Local Database Backup) بنجاح على وسائط التخزين.")
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("تم مزامنة 100% من بيانات الفروع، كشوف الحضور، والمعاملات المالية سحابياً مع خوادم السحابة الهجينة الآمنة.", fontWeight = FontWeight.SemiBold)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("النظام يعمل بكفاءة تامة لحمايتك من فيروسات الفدية والأعطال المفاجئة للأجهزة في السناتر التعليمية.", fontSize = 12.sp, color = Color.Gray)
                    }
                }
            )
        }
    }
}

// ==========================================
// TABS EXPERIENCES
// ==========================================

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AttendanceScannerView(
    viewModel: DashboardViewModel,
    groups: List<Group>,
    branches: List<Branch>,
    students: List<Student>,
    smsNotifications: List<SmsNotification>,
    scanResult: ScanResponse?,
    financialSummary: FinancialStatsSummary,
    attendanceList: List<Attendance>,
    onTabSelect: (Int) -> Unit
) {
    var selectedGroupIdx by remember { mutableStateOf(0) }
    var inputBarcode by remember { mutableStateOf("") }
    var customTemplateStudentId by remember { mutableStateOf<String?>(null) }
    var selectedTemplateIdx by remember { mutableStateOf(0) }
    var customTemplateMessageSuccess by remember { mutableStateOf<String?>(null) }
    var searchStudentQuery by remember { mutableStateOf("") }
    val context = androidx.compose.ui.platform.LocalContext.current

    val activeGroup = if (groups.isNotEmpty()) groups[selectedGroupIdx] else null

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // High Density Financial & Attendance KPIs (from High Density layout templates)
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 2.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Metric 1: Net profit
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, OutlineBorderColor)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(
                            text = "صافي الربح اليوم",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF49454F)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Row(
                            verticalAlignment = Alignment.Bottom,
                            horizontalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Text(
                                text = String.format("%,.0f", financialSummary.totalTutorNetProfit),
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = DeepTealPrimary
                            )
                            Text(
                                text = "ج.م",
                                fontSize = 8.sp,
                                color = Color(0xFF49454F)
                            )
                        }
                    }
                }

                // Metric 2: Total attendance
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, OutlineBorderColor)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(
                            text = "عدد الحضور الكلي",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF49454F)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Row(
                            verticalAlignment = Alignment.Bottom,
                            horizontalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Text(
                                text = "${attendanceList.size}",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = DeepTealPrimary
                            )
                            Text(
                                text = "طالب",
                                fontSize = 8.sp,
                                color = Color(0xFF49454F)
                            )
                        }
                    }
                }
            }
        }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFFEADDFF)),
                shape = RoundedCornerShape(12.dp, 12.dp, 4.dp, 4.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "مراقبة الحضور (الباركود)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = Color(0xFF21005D)
                    )
                    Box(
                        modifier = Modifier
                            .background(DeepTealPrimary, RoundedCornerShape(50.dp))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "نشط الآن",
                            color = Color.White,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Active selection
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, OutlineBorderColor.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = "المجموعة والفرع المستهدف حالياً:", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(8.dp))

                    if (groups.isEmpty()) {
                        Text("برجاء تهيئة أو إضافة مجموعة تعليمية أولاً.", color = Color.Red, fontSize = 12.sp)
                    } else {
                        var expandedGroup by remember { mutableStateOf(false) }
                        Box {
                            OutlinedButton(
                                onClick = { expandedGroup = true },
                                modifier = Modifier.fillMaxWidth(),
                                border = BorderStroke(1.dp, OutlineBorderColor)
                            ) {
                                val currentGroup = groups[selectedGroupIdx]
                                val brName = branches.find { it.id == currentGroup.branchId }?.name ?: ""
                                Row(
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(text = "${currentGroup.name} ($brName)", fontWeight = FontWeight.Bold)
                                    Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                                }
                            }
                            DropdownMenu(
                                expanded = expandedGroup,
                                onDismissRequest = { expandedGroup = false },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                groups.forEachIndexed { index, group ->
                                    val bName = branches.find { it.id == group.branchId }?.name ?: ""
                                    DropdownMenuItem(
                                        text = { Text("${group.name} - $bName (${group.timeSlots})") },
                                        onClick = {
                                            selectedGroupIdx = index
                                            expandedGroup = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Scanner visual
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, OutlineBorderColor.copy(alpha = 0.5f))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .background(DeepTealPrimary.copy(alpha = 0.1f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.QrCodeScanner,
                            contentDescription = null,
                            tint = DeepTealPrimary,
                            modifier = Modifier.size(40.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "محاكاة قارئ الباركود الرقمي",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                    Text(
                        text = "اكتب الكود يدوياً أو اضغط على أحد الأزرار الجاهزة للمحاكاة الفورية لمسح كروت الطلاب البلاستيكية.",
                        fontSize = 11.sp,
                        color = Color.Gray,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = inputBarcode,
                            onValueChange = { inputBarcode = it },
                            placeholder = { Text("أدخل رمز باركود الطالب (مثل 1001)") },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = DeepTealPrimary,
                                unfocusedBorderColor = Color.Gray
                            )
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                if (inputBarcode.isNotEmpty() && activeGroup != null) {
                                    viewModel.scanStudentBarcode(inputBarcode, activeGroup.id)
                                    inputBarcode = ""
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = DeepTealPrimary),
                            modifier = Modifier.height(56.dp)
                        ) {
                            Text("مسح الكود")
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Simulated scenario trigger shortcuts
                    Text(
                        text = "سيناريوهات سريعة للتجربة لطلقاء المنشأة التعليمية:",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.align(Alignment.Start)
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        students.forEach { s ->
                            AssistChip(
                                onClick = {
                                    if (activeGroup != null) {
                                        viewModel.scanStudentBarcode(s.barcode, activeGroup.id)
                                    }
                                },
                                label = {
                                    Text(
                                        text = "${s.name} (${s.barcode})",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                },
                                leadingIcon = {
                                    Icon(
                                        Icons.Default.Fingerprint,
                                        contentDescription = null,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            )
                        }

                        // Suspicious / unregistered test
                        AssistChip(
                            onClick = {
                                if (activeGroup != null) {
                                    viewModel.scanStudentBarcode("9999", activeGroup.id)
                                }
                            },
                            label = { Text("رمز مفقود (9999)", fontSize = 11.sp, color = StatusDebtRed) },
                            leadingIcon = { Icon(Icons.Default.ErrorOutline, null, tint = StatusDebtRed, modifier = Modifier.size(14.dp)) }
                        )
                    }
                }
            }
        }

        // Live alert overlay when scan completes
        if (scanResult != null) {
            item {
                ScanAlertResultCard(scanResult = scanResult!!, viewModel = viewModel, onDismiss = { viewModel.dismissScanResult() })
            }
        }

        // Parent SMS notification logs (بوابة التنبيه الآلية لأولياء الأمور)
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, OutlineBorderColor.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "💬 بوابة التنبيه الذكية لأولياء الأمور (WhatsApp/SMS)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = DeepTealPrimary
                        )
                        Box(
                            modifier = Modifier
                                .background(Color(0xFFF0FDF4), RoundedCornerShape(4.dp))
                                .border(1.dp, StatusGreenBorder.copy(alpha = 0.4f), RoundedCornerShape(4.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text("مثبّت تلقائياً ✅", color = StatusGreenText, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    if (smsNotifications.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp)
                                .border(1.dp, OutlineBorderColor, RoundedCornerShape(8.dp))
                                .padding(12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "لم يتم إطلاق أي رسائل حضور حتى الآن. استخدم محاكي مسح الباركود.",
                                fontSize = 11.sp,
                                color = Color.Gray,
                                textAlign = TextAlign.Center
                            )
                        }
                    } else {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 200.dp)
                                .verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            smsNotifications.forEach { sms ->
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(LightStatusGreenBack, RoundedCornerShape(8.dp))
                                        .border(1.dp, StatusGreenBorder.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                                        .padding(8.dp)
                                ) {
                                    Column {
                                        Row(
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Text(
                                                text = "موجّه لولي أمر الطالب: ${sms.studentName}",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 11.sp,
                                                color = StatusGreenText
                                            )
                                            Text(
                                                text = "رقم: ${sms.parentPhone}",
                                                fontSize = 10.sp,
                                                color = MintSecondary
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = sms.message,
                                            fontSize = 11.sp,
                                            color = Color.Black
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "تم الإرسال فوري بعد رصد الباركود بـ 1 ثانية ⚡",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = StatusGreenText
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Premium WhatsApp Quick Broadcast Center for Admin & Receptionists
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, OutlineBorderColor.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "📱 مركز بث قوالب الـ WhatsApp لأولياء الأمور",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = DeepTealPrimary
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "اختر الطالب والقالب المطلوب لفتح محادثة واتساب فورية مجهزة بنص مخصص بنقرة واحدة:",
                        fontSize = 11.sp,
                        color = MintSecondary
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    // 1. Fully Fluid Digital Search for Student Selection
                    val currentSelStudent = students.find { it.barcode == customTemplateStudentId }

                    OutlinedTextField(
                        value = searchStudentQuery,
                        onValueChange = { searchStudentQuery = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("ابحث باسم الطالب أو رقم الباركود لتحديده...", fontSize = 10.sp) },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(16.dp), tint = MintSecondary) },
                        trailingIcon = {
                            if (searchStudentQuery.isNotEmpty()) {
                                IconButton(onClick = { searchStudentQuery = "" }) {
                                    Icon(Icons.Default.Close, contentDescription = "مسح", modifier = Modifier.size(14.dp), tint = MintSecondary)
                                }
                            }
                        },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ForestAccent,
                            unfocusedBorderColor = OutlineBorderColor,
                            focusedContainerColor = Color(0xFFF8FAFC),
                            unfocusedContainerColor = Color(0xFFF8FAFC)
                        ),
                        shape = RoundedCornerShape(8.dp)
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    // Live matching dynamic horizontal scrollable row with high fidelity selection chips
                    val matchedStudents = if (searchStudentQuery.isEmpty()) {
                        students.take(5) // Show first 5 students by default for quick access
                    } else {
                        students.filter { it.name.contains(searchStudentQuery, ignoreCase = true) || it.barcode.contains(searchStudentQuery) }
                    }

                    if (matchedStudents.isNotEmpty()) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState())
                        ) {
                            matchedStudents.forEach { st ->
                                val isSelected = st.barcode == customTemplateStudentId
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(if (isSelected) MintSecondary.copy(alpha = 0.15f) else Color.White)
                                        .border(1.dp, if (isSelected) ForestAccent else OutlineBorderColor, RoundedCornerShape(6.dp))
                                        .clickable {
                                            customTemplateStudentId = st.barcode
                                        }
                                        .padding(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(6.dp)
                                                .background(if (isSelected) ForestAccent else Color.Gray.copy(alpha = 0.4f), CircleShape)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = st.name,
                                            fontSize = 9.sp,
                                            fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium,
                                            color = if (isSelected) DeepTealPrimary else Color.DarkGray
                                        )
                                    }
                                }
                            }
                        }
                    } else {
                        Text(
                            text = "⚠️ لم يتم العثور على طلاب مطابقة لنتائج البحث.",
                            fontSize = 10.sp,
                            color = Color.Red,
                            modifier = Modifier.padding(horizontal = 4.dp)
                        )
                    }

                    if (currentSelStudent != null) {
                        Spacer(modifier = Modifier.height(6.6.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(LightStatusGreenBack, RoundedCornerShape(6.dp))
                                .border(1.dp, StatusGreenBorder.copy(alpha = 0.2f), RoundedCornerShape(6.dp))
                                .padding(8.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = StatusGreenText, modifier = Modifier.size(13.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "تم التحديد: ${currentSelStudent.name} (كود: ${currentSelStudent.barcode} | ولي الأمر: ${currentSelStudent.parentPhone})",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = StatusGreenText
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // 2. Custom Templates Row Selection
                    val templateList = listOf(
                        "🌟 تهنئة وتفوق",
                        "⚠️ إنذار غياب",
                        "📝 رصد تقييم",
                        "💳 مطالبة مالية"
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        templateList.forEachIndexed { idx, title ->
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (selectedTemplateIdx == idx) ForestAccent else Color(0xFFF8FAFC))
                                    .border(1.dp, if (selectedTemplateIdx == idx) Color.Transparent else OutlineBorderColor)
                                    .clickable { selectedTemplateIdx = idx }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = title,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (selectedTemplateIdx == idx) Color.White else MintSecondary
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // 3. Render Pre-populated text preview for confirmation
                    currentSelStudent?.let { activeSt ->
                        val textPreview = when(selectedTemplateIdx) {
                            0 -> "السلام عليكم ورحمة الله وبركاته، نود إعلامكم بتفوق ابنكم البطل ${activeSt.name} اليوم في أكاديمية الأستاذ أحمد الشافعي بنسبة ممتازة! بارك الله فيه وفي مجهوداته الرائعة 🌟."
                            1 -> "تنبيه غياب عاجل من سنتر النور: الطالب ${activeSt.name} قد غاب/تأخر عن الحضور للحصة الأساسية في موعدها المقرّر اليوم. يرجى المتابعة معنا فوراً للتحقق ⚠️."
                            2 -> "متابعة التقارير الأكاديمية: تم اليوم رصد واختبار مستوى الطالب ${activeSt.name} وكانت النتيجة رائعة والواجبات مكتملة تماماً، نثمن حرصه واجتهاده الدائم 📝."
                            else -> "تنبيه إدارة السنتر الأكاديمي: نذكركم بضرورة تسوية الرسوم المستحقة للطالب ${activeSt.name} وقدرها ${activeSt.registrationFee} ج.م لتجنب تعطل حسابه بالباركود. كود خدمة فوري: 71203، رقم الدفع: 908412${activeSt.barcode} 💳."
                        }

                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                            border = BorderStroke(1.dp, OutlineBorderColor),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text("معاينة نص الرسالة قبل الإرسال:", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = MintSecondary)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = textPreview,
                                    fontSize = 11.sp,
                                    color = DeepTealPrimary,
                                    lineHeight = 15.sp
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                Button(
                                    onClick = {
                                        try {
                                            val encoded = java.net.URLEncoder.encode(textPreview, "UTF-8")
                                            val intent = android.content.Intent(android.content.Intent.ACTION_VIEW).apply {
                                                data = android.net.Uri.parse("https://api.whatsapp.com/send?phone=${activeSt.parentPhone}&text=$encoded")
                                            }
                                            context.startActivity(intent)
                                            customTemplateMessageSuccess = "تم توليد وتوجيه الرسالة بنجاح لـ ${activeSt.name}! 📱"
                                        } catch (e: Exception) {}
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.fillMaxWidth().height(36.dp)
                                ) {
                                    Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color.White)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("بث وتوجيه عبر WhatsApp الرقمي ✨", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                }
                            }
                        }
                    } ?: run {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFFF8FAFC), RoundedCornerShape(8.dp))
                                .border(1.dp, OutlineBorderColor)
                                .padding(12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "💡 يرجى اختيار أحد الطلاب من القائمة أعلاه للبدء بالبث الفوري.",
                                fontSize = 10.sp,
                                color = MintSecondary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    AnimatedVisibility(visible = customTemplateMessageSuccess != null) {
                        customTemplateMessageSuccess?.let { txt ->
                            Spacer(modifier = Modifier.height(8.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(LightStatusGreenBack, RoundedCornerShape(6.dp))
                                    .border(1.dp, StatusGreenBorder.copy(alpha = 0.3f), RoundedCornerShape(6.dp))
                                    .padding(8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = txt, color = StatusGreenText, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        // High Density Quick Management Options from the HTML template
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, OutlineBorderColor),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "خيارات الإدارة السريعة للسنتر",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = Color(0xFF1D1B20),
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val actions = listOf(
                            Triple("المذكرات", Icons.Default.Book, 3), // Tab 3: Materials
                            Triple("الطلاب", Icons.Default.People, 1),    // Tab 1: Students
                            Triple("الحسابات", Icons.Default.Payments, 2),  // Tab 2: Financials
                            Triple("المساعدين", Icons.Default.AssignmentInd, 3) // Tab 3: Assistants
                        )
                        actions.forEach { (title, icon, targetTab) ->
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { onTabSelect(targetTab) }
                                    .padding(vertical = 4.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .background(
                                            color = QuickActionBadgeBackground,
                                            shape = RoundedCornerShape(12.dp)
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = icon,
                                        contentDescription = title,
                                        tint = QuickActionBadgeIconColor,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = title,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF49454F)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SYTriangleLogo(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(44.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(DeepTealPrimary)
            .padding(4.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val path = androidx.compose.ui.graphics.Path().apply {
                moveTo(w / 2f, 4f)
                lineTo(w - 4f, h - 4f)
                lineTo(4f, h - 4f)
                close()
            }
            drawPath(
                path = path,
                color = ForestAccent,
                style = androidx.compose.ui.graphics.drawscope.Fill
            )
            drawPath(
                path = path,
                color = Color.White.copy(alpha = 0.85f),
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.5.dp.toPx())
            )
        }
        Text(
            text = "SY",
            fontWeight = FontWeight.Black,
            color = Color.White,
            fontSize = 12.sp,
            letterSpacing = 0.5.sp,
            modifier = Modifier.align(Alignment.Center).padding(top = 10.dp)
        )
    }
}

@Composable
fun ScanAlertResultCard(scanResult: ScanResponse, viewModel: DashboardViewModel, onDismiss: () -> Unit) {
    val cardBackColor = if (scanResult.success) Color(0xFFF8FAFC) else Color(0xFFF9DEDC)
    val cardBorderColor = if (scanResult.success) ForestAccent else StatusDebtRed
    val cardTextColor = if (scanResult.success) DeepTealPrimary else Color(0xFF410E0B)

    var whatsAppSuccessMessage by remember { mutableStateOf<String?>(null) }
    val context = androidx.compose.ui.platform.LocalContext.current

    Card(
        colors = CardDefaults.cardColors(
            containerColor = cardBackColor
        ),
        modifier = Modifier
            .fillMaxWidth()
            .drawBehind {
                val strokeWidth = 5.dp.toPx()
                drawLine(
                    color = cardBorderColor,
                    start = androidx.compose.ui.geometry.Offset(size.width - strokeWidth / 2, 0f),
                    end = androidx.compose.ui.geometry.Offset(size.width - strokeWidth / 2, size.height),
                    strokeWidth = strokeWidth
                )
            }
            .border(
                1.dp,
                OutlineBorderColor.copy(alpha = 0.5f),
                RoundedCornerShape(12.dp)
            ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(start = 12.dp, end = 20.dp, top = 16.dp, bottom = 16.dp)) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (scanResult.success) Icons.Default.CheckCircle else Icons.Default.Cancel,
                        contentDescription = null,
                        tint = if (scanResult.success) ForestAccent else StatusDebtRed,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "بطاقة التحقق الذكية: ${scanResult.studentName}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = cardTextColor
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "اغلاق", tint = MintSecondary)
                }
            }

            Text(
                text = "الرمز المسحي: ${scanResult.barcode} | نوع الفحص: ${scanResult.attendanceType}",
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = MintSecondary
            )

            Spacer(modifier = Modifier.height(10.dp))
            Divider(color = OutlineBorderColor)
            Spacer(modifier = Modifier.height(10.dp))

            // ALERTS ENGINE LOGIC TO DISPLAY
            val alerts = scanResult.alerts

            if (scanResult.success) {
                if (!alerts.hasFinancialDebt && !alerts.previousAbsenceAlert && !alerts.suggestSuspension && !alerts.isSuspendedStudent) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(LightStatusGreenBack, RoundedCornerShape(8.dp))
                            .border(1.dp, StatusGreenBorder.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                            .padding(10.dp)
                    ) {
                        Text(
                            text = "✅ مكتمل الإجراءات الخضراء: الطالب ملتزم بكافة المدفوعات والحضور، ومصرح بدخوله الحصة فورياً.",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = StatusGreenText
                        )
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        if (alerts.isSuspendedStudent) {
                            AlertItem(
                                title = "🔴 غلق الدخول: الطالب مجمّد الحساب!",
                                desc = "حساب هذا الطالب موقوف حالياً. يجب تفعيل الحساب يدوياً من قائمة الطلاب أولاً بطلب ولي أمره.",
                                color = StatusDebtRed,
                                backColor = LightStatusDebtBack
                            )
                        }

                        if (alerts.hasFinancialDebt) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(LightStatusDebtBack)
                                    .border(1.dp, StatusDebtRed.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                                    .padding(12.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Warning, contentDescription = null, tint = StatusDebtRed, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "متأخرات مالية مستحقة فورياً: ${alerts.debtAmount} ج.م",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = StatusDebtRed
                                    )
                                }
                                Text(
                                    text = if(alerts.hasBookDebt) "رسومات متأخرة تشمل: اشتراك الشهر الجاري + استلام كتاب المذكرات (${alerts.unpaidBookName})" else "مستحقات متأخرة يرجى تحصيلها بمعرفة الاستقبال والسنتر.",
                                    fontSize = 11.sp,
                                    color = Color.DarkGray,
                                    modifier = Modifier.padding(vertical = 4.dp)
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                // FAWRY VOUCHER SUB-CARD
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFDE7)),
                                    border = BorderStroke(1.dp, Color(0xFFFBC02D)),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(8.dp)) {
                                        Row(
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(8.dp)
                                                        .background(Color(0xFFFFD54F), CircleShape)
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(
                                                    text = "بوابة فوري Fawry للدفع السريع",
                                                    fontWeight = FontWeight.ExtraBold,
                                                    fontSize = 10.sp,
                                                    color = Color(0xFF5D4037)
                                                )
                                            }
                                            Text(
                                                text = "مؤمّن وموثق 🔒",
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF388E3C)
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "• كود الخدمة الموحد: 71203\n• رقم الدفع المرجعي لأولياء الأمور: 908 412 ${scanResult.barcode}\n• القيمة الكلية للفاتورة: ${alerts.debtAmount} ج.م",
                                            fontSize = 10.sp,
                                            color = Color.DarkGray,
                                            lineHeight = 14.sp
                                        )
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Button(
                                            onClick = {
                                                viewModel.payStudentDebtDirectly(
                                                    barcode = scanResult.barcode,
                                                    amount = alerts.debtAmount,
                                                    branchId = 1 // standard branch code
                                                )
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFBC02D), contentColor = Color.Black),
                                            shape = RoundedCornerShape(6.dp),
                                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                            modifier = Modifier.fillMaxWidth().height(32.dp)
                                        ) {
                                            Icon(Icons.Default.Payment, contentDescription = null, modifier = Modifier.size(12.dp))
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text("تسوية ودفع فوري الآن 💳", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                // WHATSAPP DIRECT SENDER
                                Button(
                                    onClick = {
                                        whatsAppSuccessMessage = "تم توليد وتوجيه رابط الـ WhatsApp لولي الأمر بنجاح! 📱"
                                        try {
                                            val messageText = "السلام عليكم ورحمة الله وبركاته، نحيطكم علماً بأن ابنكم البطل ${scanResult.studentName} قد وصل بسلام إلى السنتر، ولديه مستحقات ماليّة قدرها ${alerts.debtAmount} ج.م تشمل المتأخرات والمذكرات الفائتة. يرجى سداد المبلغ فورياً لضمان استمراره دون انقطاع. شكراً لكم."
                                            val encodedText = java.net.URLEncoder.encode(messageText, "UTF-8")
                                            val whatsappUrl = "https://api.whatsapp.com/send?phone=201234567890&text=$encodedText"
                                            val intent = android.content.Intent(android.content.Intent.ACTION_VIEW).apply {
                                                data = android.net.Uri.parse(whatsappUrl)
                                            }
                                            context.startActivity(intent)
                                        } catch (e: Exception) {
                                            // Handle fallback silently
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366), contentColor = Color.White),
                                    shape = RoundedCornerShape(6.dp),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                    modifier = Modifier.fillMaxWidth().height(30.dp)
                                ) {
                                    Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(12.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("توجيه إنذار فوري عبر WhatsApp 💬", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        if (alerts.previousAbsenceAlert) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(LightStatusAbsenceBack)
                                    .border(1.dp, StatusAbsenceYellow.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                                    .padding(12.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.AssignmentLate, contentDescription = null, tint = StatusAbsenceYellow, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "غائب في المحاضرة السابقة!",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = StatusAbsenceYellow
                                    )
                                }
                                Text(
                                    text = "الطالب تغيب عن المحاضرة الماضية. يجب تسليمه كتيب المراجعة والواجب الفائت قبل التوجه للقاعة لضمان المتابعة الأكاديمية.",
                                    fontSize = 11.sp,
                                    color = Color.DarkGray,
                                    modifier = Modifier.padding(vertical = 4.dp)
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Button(
                                    onClick = {
                                        try {
                                            val messageText = "تنبيه متابعة: ابنكم ${scanResult.studentName} غاب في الحصة الماضية وحضر اليوم ومعه الواجبات المتأخرة. تم تسليمه ملخص المراجعة للتعويض."
                                            val encodedText = java.net.URLEncoder.encode(messageText, "UTF-8")
                                            val intent = android.content.Intent(android.content.Intent.ACTION_VIEW).apply {
                                                data = android.net.Uri.parse("https://api.whatsapp.com/send?phone=201234567890&text=$encodedText")
                                            }
                                            context.startActivity(intent)
                                            whatsAppSuccessMessage = "تم نسخ وتوجيه رسالة الغياب لولي الأمر عبر واتساب بنجاح!"
                                        } catch (e: Exception) {}
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366), contentColor = Color.White),
                                    shape = RoundedCornerShape(6.dp),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                                    modifier = Modifier.wrapContentSize().height(28.dp)
                                ) {
                                    Text("تواصل مع ولي الأمر 💬", fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        if (alerts.suggestSuspension) {
                            AlertItem(
                                title = "⚠️ تنبيه الإيقاف التلقائي وتجميد العضوية",
                                desc = "تغيب الطالب مرتين متتاليتين دون العذر المقبول. يقترح النظام تجميد حسابه لحضور ولي أمره.",
                                color = StatusAbsenceYellow,
                                backColor = LightStatusAbsenceBack
                            )
                        }
                    }
                }
            } else {
                Text(
                    text = scanResult.message,
                    color = StatusDebtRed,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }

            AnimatedVisibility(visible = whatsAppSuccessMessage != null) {
                whatsAppSuccessMessage?.let { text ->
                    Spacer(modifier = Modifier.height(8.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(LightStatusGreenBack, RoundedCornerShape(6.dp))
                            .border(1.dp, StatusGreenBorder.copy(alpha = 0.3f), RoundedCornerShape(6.dp))
                            .padding(8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Check, contentDescription = null, tint = StatusGreenText, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = text, color = StatusGreenText, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AlertItem(title: String, desc: String, color: Color, backColor: Color) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(backColor, RoundedCornerShape(8.dp))
            .border(1.dp, OutlineBorderColor.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
            .drawBehind {
                val strokeWidth = 5.dp.toPx()
                // Since LayoutDirection is RTL, draw right border at x = size.width
                drawLine(
                    color = color,
                    start = androidx.compose.ui.geometry.Offset(size.width - strokeWidth / 2, 0f),
                    end = androidx.compose.ui.geometry.Offset(size.width - strokeWidth / 2, size.height),
                    strokeWidth = strokeWidth
                )
            }
            .padding(start = 12.dp, end = 20.dp, top = 12.dp, bottom = 12.dp)
    ) {
        Column {
            Text(text = title, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = color)
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = desc, fontSize = 10.sp, color = Color.DarkGray, lineHeight = 14.sp)
        }
    }
}

// ------------------------------------------
// STUDENTS & GROUPS VIEW
// ------------------------------------------

@Composable
fun StudentsAndGroupsView(
    viewModel: DashboardViewModel,
    students: List<Student>,
    groups: List<Group>,
    branches: List<Branch>,
    currentRole: String,
    onAddStudent: () -> Unit,
    onAddGroup: () -> Unit
) {
    val isReadOnly = currentRole == "Academic Assistant"
    val context = androidx.compose.ui.platform.LocalContext.current

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    Text(
                        text = "👥 شؤون الطلاب والمجموعات المجمعة",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = DeepTealPrimary
                    )
                    Text(
                        text = "الإدارة الأكاديمية وصالح الحضور ونقل وقيد المنتسبين.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }

                if (!isReadOnly) {
                    Button(
                        onClick = onAddStudent,
                        colors = ButtonDefaults.buttonColors(containerColor = DeepTealPrimary)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("طالب جديد", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Schedule summary cards
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, OutlineBorderColor.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(text = "📅 المجموعات والمواعيد بالجداول:", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        if (currentRole == "Admin") {
                            IconButton(onClick = onAddGroup) {
                                Icon(Icons.Default.AddCircle, contentDescription = null, tint = DeepTealPrimary)
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))

                    if (groups.isEmpty()) {
                        Text("لا يوجد مجموعات حالياً بالسناتر.", color = Color.Gray, fontSize = 12.sp)
                    } else {
                        groups.forEach { group ->
                            val b = branches.find { it.id == group.branchId }
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .background(Color(0xFFF4F7F7), RoundedCornerShape(8.dp))
                                    .border(1.dp, OutlineBorderColor.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                                    .padding(8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(text = group.name, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    Text(text = "السنتر المستضيف: ${b?.name ?: ""}", fontSize = 11.sp, color = Color.Gray)
                                }
                                Box(
                                    modifier = Modifier
                                        .background(DeepTealPrimary.copy(alpha = 0.1f), RoundedCornerShape(4.dp))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(text = group.timeSlots, fontSize = 10.sp, color = DeepTealPrimary, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }

        // Students roster list
        item {
            Text(
                text = "قائمة الطلاب المسجلين بالكامل:",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = ForestAccent
            )
        }

        if (students.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp), contentAlignment = Alignment.Center
                ) {
                    Text("لا يوجد طلاب مسجلين بالنظام.")
                }
            }
        } else {
            items(students) { student ->
                val group = groups.find { it.id == student.nativeGroupId }
                val br = branches.find { it.id == (group?.branchId ?: -1) }

                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, OutlineBorderColor.copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .background(DeepTealPrimary.copy(alpha = 0.1f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = student.name.take(1),
                                        fontWeight = FontWeight.Bold,
                                        color = DeepTealPrimary
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = student.name,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )

                                Spacer(modifier = Modifier.width(8.dp))

                                // Status indicator
                                Box(
                                    modifier = Modifier
                                        .background(
                                            when (student.status) {
                                                "Active" -> Color(0xFFE8F5E9)
                                                "Suspended" -> LightStatusDebtBack
                                                else -> LightStatusAbsenceBack
                                            },
                                            RoundedCornerShape(4.dp)
                                        )
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = when (student.status) {
                                            "Active" -> "نشط"
                                            "Suspended" -> "موقوف"
                                            else -> "تأجيل"
                                        },
                                        color = when (student.status) {
                                            "Active" -> Color(0xFF2E7D32)
                                            "Suspended" -> StatusDebtRed
                                            else -> StatusAbsenceYellow
                                        },
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = "المجموعة الأساسية: ${group?.name ?: "غير محددة"} (${br?.name ?: ""})",
                                fontSize = 11.sp,
                                color = Color.DarkGray
                            )
                            Spacer(modifier = Modifier.height(3.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = "الباركود: ${student.barcode}",
                                    fontSize = 10.sp,
                                    color = Color.Gray,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "|  هاتف ولي الأمر: ${student.parentPhone}",
                                    fontSize = 11.sp,
                                    color = Color.DarkGray
                                )
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFFE8F5E9))
                                        .clickable {
                                            try {
                                                val msg = "مرحباً يا فندم، نود التواصل معكم من سنتر الأستاذ أحمد الشافعي بخصوص الطالب ${student.name} لمتابعة مستواه التعليمي."
                                                val encoded = java.net.URLEncoder.encode(msg, "UTF-8")
                                                val intent = android.content.Intent(android.content.Intent.ACTION_VIEW).apply {
                                                    data = android.net.Uri.parse("https://api.whatsapp.com/send?phone=${student.parentPhone}&text=$encoded")
                                                }
                                                context.startActivity(intent)
                                            } catch (e: Exception) {}
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Share,
                                        contentDescription = "اتصل واتساب",
                                        tint = Color(0xFF25D366),
                                        modifier = Modifier.size(13.dp)
                                    )
                                }
                            }

                            // Interactive Fawry ledger settling directly on student card
                            Spacer(modifier = Modifier.height(6.dp))
                            if (!student.isRegistrationFeePaid) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .background(LightStatusDebtBack, RoundedCornerShape(4.dp))
                                            .border(1.dp, StatusDebtRed.copy(alpha = 0.3f), RoundedCornerShape(4.dp))
                                            .padding(horizontal = 6.dp, vertical = 3.dp)
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.CreditCardOff, null, tint = StatusDebtRed, modifier = Modifier.size(11.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("رسوم التسجيل متأخرة (${student.registrationFee} ج.م)", color = StatusDebtRed, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(Color(0xFFFFFDE7))
                                            .border(1.dp, Color(0xFFFBC02D), RoundedCornerShape(4.dp))
                                            .clickable {
                                                viewModel.payStudentDebtDirectly(student.barcode, student.registrationFee, br?.id ?: 1)
                                            }
                                            .padding(horizontal = 8.dp, vertical = 3.dp)
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.Payment, null, tint = Color(0xFFC2410C), modifier = Modifier.size(10.dp))
                                            Spacer(modifier = Modifier.width(3.dp))
                                            Text("دفع فوري سريع 💳", color = Color(0xFF7C2D12), fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            } else {
                                Box(
                                    modifier = Modifier
                                        .background(LightStatusGreenBack, RoundedCornerShape(4.dp))
                                        .border(1.dp, StatusGreenBorder.copy(alpha = 0.3f), RoundedCornerShape(4.dp))
                                        .padding(horizontal = 6.dp, vertical = 3.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.CheckCircle, null, tint = StatusGreenText, modifier = Modifier.size(11.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("رسوم التسجيل مسددة بنجاح ✅ (Fawry)", color = StatusGreenText, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }

                        // Suspended toggle (RBAC - Receptionist and Academic assistant blocked)
                        if (currentRole == "Admin") {
                            Button(
                                onClick = {
                                    val targetStatus = if (student.status == "Active") "Suspended" else "Active"
                                    viewModel.updateStudentStatus(student.barcode, targetStatus)
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (student.status == "Active") StatusDebtRed.copy(alpha = 0.9f) else Color(0xFF2E7D32)
                                ),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = if (student.status == "Active") "تجميد حساب" else "تنشيط فوري",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// ------------------------------------------
// FINANCIAL LEDGER VIEW ($P_net implementation)
// ------------------------------------------

@Composable
fun FinancialLedgerView(
    viewModel: DashboardViewModel,
    financialSummary: FinancialStatsSummary,
    selectedBranchId: Int?,
    branches: List<Branch>,
    assistants: List<Assistant>,
    attendanceList: List<Attendance>,
    currentRole: String,
    onPaySalary: (Assistant) -> Unit,
    onAddExpense: () -> Unit,
    onAddIncome: () -> Unit
) {
    // ENFORCE ACCESS CONTROL DIRECTIVE FROM SECTION 3
    if (currentRole == "Receptionist" || currentRole == "Academic Assistant") {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Card(
                colors = CardDefaults.cardColors(containerColor = LightStatusDebtBack),
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = null,
                        tint = StatusDebtRed,
                        modifier = Modifier.size(56.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "جدار الصلاحيات وحوكمة البيانات 🛡️",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = StatusDebtRed,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "عذراً، بموجب سياسات الإدارة والأمان الرقمي القائم على الأدوار (RBAC)، فإن الحسابات المالية وصافي توزيع الأرباح للمدرس (P_net) محجوبة بالكامل عن حسابك الحالي دور [ $currentRole ].",
                        fontSize = 12.sp,
                        color = Color.DarkGray,
                        textAlign = TextAlign.Center,
                        lineHeight = 18.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "يتوجب التبديل لدور المدرس / المشرف العام بالعلوي للاطلاع.",
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = Color.Black
                    )
                }
            }
        }
        return
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    Text(
                        text = "📊 الهندسة المالية وحساب أرباح المدرس الصافية",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = DeepTealPrimary
                    )
                    Text(
                        text = "تسوية وتوزيع الحصص والنسب بناءً على معادلة الإيرادات والمصروفات المستقبة.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }
            }
        }

        // Formula visualization
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "معادلة صافي ربح المدرس (P_net):",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = ForestAccent
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "P_net = Σ [ (I_ij - D_ij * w_j) * (1 - θ_j) + B_j * (1 - φ_j) - C_j ]",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth(),
                        color = ForestAccent
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "حيث θ تمثل نسبة السنتر، w تحمل الخصومات، B مبيعات الكتب، C المصروفات، I الاشتراكات والمبيعات الصافية.",
                        fontSize = 10.sp,
                        color = Color.DarkGray,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        // Global KPI Cards
        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = DeepTealPrimary),
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(text = "صافي ربح المعلم P_net", color = Color.White.copy(alpha = 0.82f), fontSize = 11.sp, fontWeight = FontWeight.Medium)
                        Text(text = "${String.format("%.2f", financialSummary.totalTutorNetProfit)} ج.م", fontWeight = FontWeight.Black, fontSize = 18.sp, color = Color.White)
                    }
                }
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, OutlineBorderColor.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(text = "إجمالي الإيرادات الخام", fontSize = 11.sp, color = Color(0xFF49454F), fontWeight = FontWeight.Medium)
                        Text(text = "${String.format("%.2f", financialSummary.totalGrossRevenue)} ج.م", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF1D1B20))
                    }
                }
            }
        }

        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, OutlineBorderColor.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(text = "إجمالي الخصومات المعطاة", fontSize = 11.sp, color = Color(0xFF49454F), fontWeight = FontWeight.Medium)
                        Text(text = "${String.format("%.2f", financialSummary.totalDiscounts)} ج.م", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = StatusAbsenceYellow)
                    }
                }
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, OutlineBorderColor.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(text = "المصروفات والأجور C_j", fontSize = 11.sp, color = Color(0xFF49454F), fontWeight = FontWeight.Medium)
                        Text(text = "${String.format("%.2f", financialSummary.totalExpenses)} ج.م", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = StatusDebtRed)
                    }
                }
            }
        }

        // Quick transactional control buttons
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onAddIncome,
                    colors = ButtonDefaults.buttonColors(containerColor = DeepTealPrimary),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Add, null)
                    Spacer(Modifier.width(4.dp))
                    Text("تدفق وارد", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = onAddExpense,
                    colors = ButtonDefaults.buttonColors(containerColor = StatusDebtRed),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Remove, null)
                    Spacer(Modifier.width(4.dp))
                    Text("صرف مصاريف C_j", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Branch lists breakdown details
        item {
            Text(
                text = "تحليل الأرباح المفصل لكل سنتر فرعي:",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = ForestAccent
            )
        }

        items(financialSummary.branchBreakdowns) { summary ->
            val b = branches.find { it.id == summary.branchId }
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, OutlineBorderColor.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(text = summary.branchName, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = DeepTealPrimary)
                        Text(text = "صافي المدرس: ${String.format("%.2f", summary.tutorNetProfit)} ج.م", fontWeight = FontWeight.Black, fontSize = 13.sp, color = ForestAccent)
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "نسبة سنتر ط θ_j: ${if (b != null) (b.commissionPercentage*100).toInt() else 30}%", fontSize = 11.sp, color = Color.Gray)
                        Text(text = "العمولة المحصلة للسنتر: ${String.format("%.2f", summary.commissionCut)} ج.م", fontSize = 11.sp, color = Color.Gray)
                    }
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(text = "مجموع الكتب المستلمة في السنتر:", fontSize = 11.sp, color = Color.Gray)
                        Text(text = "خصومات السنتر المتناصفة: ${String.format("%.2f", summary.discounts)} ج.م", fontSize = 11.sp, color = Color.Gray)
                    }
                }
            }
        }

        // Operational assistants salary checklist
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, OutlineBorderColor.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "👥 حساب أجور ورواتب الطاقم والمساعدين:",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = ForestAccent
                    )
                    Text(
                        text = "يقوم النظام باحتساب مستحقات السكرتارية وخصمها تلفائياً عند الدفع وتسجيلها بالقيد تشغيلي.",
                        fontSize = 11.sp,
                        color = Color.Gray
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    if (assistants.isEmpty()) {
                        Text("لا يوجد طاقم مساعد في قاعدة البيانات.", color = Color.Gray, fontSize = 12.sp)
                    } else {
                        assistants.forEach { assistant ->
                            val linkedBranch = branches.find { it.id == assistant.assignedBranchId }

                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp)
                                    .border(1.dp, Color.LightGray.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                                    .padding(8.dp)
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column {
                                        Text(text = assistant.name, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                        Text(text = "الدور: ${assistant.role} | النطاق: ${linkedBranch?.name ?: "إشراف عام"}", fontSize = 11.sp, color = Color.Gray)
                                    }
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            text = "${assistant.salaryValue} ج.م / ${when(assistant.salaryType) {
                                                "Daily" -> "يومية الحضور"
                                                "Per Student" -> "عن كل طالب"
                                                else -> "راتب شهري"
                                            }}",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = DeepTealPrimary
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Button(
                                    onClick = { onPaySalary(assistant) },
                                    colors = ButtonDefaults.buttonColors(containerColor = DeepTealPrimary.copy(alpha = 0.8f)),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                    modifier = Modifier.align(Alignment.End)
                                ) {
                                    Icon(Icons.Default.Payment, contentDescription = null, modifier = Modifier.size(12.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("صرف تذكرة راتب وتسجيل المصروف C_j", fontSize = 10.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ------------------------------------------
// BRANCHES & RESILIENCE / REGULATORY ALIGNMENT
// ------------------------------------------

@Composable
fun BranchesAndResilienceView(
    viewModel: DashboardViewModel,
    branches: List<Branch>,
    materials: List<Material>,
    assistants: List<Assistant>,
    currentRole: String,
    onAddBranch: () -> Unit,
    onAddMaterial: () -> Unit,
    onAddAssistant: () -> Unit,
    onRunBackup: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    Text(
                        text = "🛡️ إدارة السناتر، المخازن، ومواجهة المخاطر",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = DeepTealPrimary
                    )
                    Text(
                        text = "حوكمة الأصول، مخزون الملازم الشريحة، والنسخ الاحتياطي والامتثال للوائح الوزارية.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }
            }
        }

        // Backup and resilience control panel (Section 5)
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = DeepDarkSurface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "🔒 استراتيجية الاسترداد ومواجهة فيروسات الفدية والأعطال",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = DarkTealPrimary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "يقوم النظام بنسخ البيانات مشفرة محلياً بجانب المزامنة السحابية اللحظية الهجينة (Hybrid Cloud-Local Backup) لحماية سجلات الطلاب من أي فقدان.",
                        fontSize = 11.sp,
                        color = Color.LightGray
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = onRunBackup,
                        colors = ButtonDefaults.buttonColors(containerColor = DarkMintSecondary, contentColor = DeepDarkAccent),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.CloudSync, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("تفعيل المزامنة المباشرة وأخذ نسخة احتياطية الآن", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Regulatory Alignment info (Section 5)
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, OutlineBorderColor.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "⚖️ التوافق التنظيمي والتحول الرقمي التعليمي الهجين",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = ForestAccent
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "1. التكامل مع مجموعات الدعم والتقوية المدرسية: يوفر ملفات متكاملة متوافقة مع تسعير الحصص الرسمي للوزارة (100 - 150 ج.م) واللوائح المحاسبية.",
                        fontSize = 11.sp,
                        color = Color.DarkGray
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "2. منصة التدريس الهجين (Blended Learning): تشغيل أكواد شحن مرتبطة لفتح الحصص المسجلة والملازم الرقمية الآمنة مشفرة لحماية الملكية الفكرية وسلامة الإيرادات.",
                        fontSize = 11.sp,
                        color = Color.DarkGray
                    )
                }
            }
        }

        // Branch parameters
        item {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "🏘️ الفروع والسناتر المسجلة:",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = ForestAccent
                )
                if (currentRole == "Admin") {
                    IconButton(onClick = onAddBranch) {
                        Icon(Icons.Default.AddCircle, contentDescription = "اضافة سنتر", tint = DeepTealPrimary)
                    }
                }
            }
        }

        items(branches) { branch ->
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, OutlineBorderColor.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = branch.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text(text = "العنوان: ${branch.location}", fontSize = 11.sp, color = Color.Gray)
                    Spacer(modifier = Modifier.height(6.dp))
                    Divider(color = Color.LightGray.copy(alpha = 0.3f))
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(text = "عمولة السنتر θ_j: ${(branch.commissionPercentage*100).toInt()}%", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = DeepTealPrimary)
                        Text(text = "عمولة الكتب φ_j: ${(branch.bookCommissionPercentage*100).toInt()}%", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = DeepTealPrimary)
                    }
                }
            }
        }

        // Materials & Book Store Inventory
        item {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "📚 مخزن الكتب والملازم الورقية:",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = ForestAccent
                )
                if (currentRole == "Admin") {
                    IconButton(onClick = onAddMaterial) {
                        Icon(Icons.Default.AddCircle, contentDescription = "مذكرة جديدة", tint = DeepTealPrimary)
                    }
                }
            }
        }

        items(materials) { mat ->
            val branch = branches.find { it.id == mat.branchId }
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, OutlineBorderColor.copy(alpha = 0.5f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(text = mat.name, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text(text = "الفرع المستقِر به: ${branch?.name ?: ""}", fontSize = 11.sp, color = Color.Gray)
                        Text(text = "سعر البيع للطالب: ${mat.price} ج.م", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = DeepTealPrimary)
                    }

                    // Stock Tag
                    Box(
                        modifier = Modifier
                            .background(
                                if (mat.stockCount > 20) Color(0xFFE8F5E9) else Color(0xFFFFEBEE),
                                RoundedCornerShape(4.dp)
                            )
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "المخزون: ${mat.stockCount} نسخة",
                            color = if (mat.stockCount > 20) Color(0xFF2E7D32) else StatusDebtRed,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Assistants Addition Section
        item {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "💼 هيكل المساعدين وصلاحيات السكرتارية:",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = ForestAccent
                )
                if (currentRole == "Admin") {
                    IconButton(onClick = onAddAssistant) {
                        Icon(Icons.Default.AddCircle, contentDescription = "مساعد جديد", tint = DeepTealPrimary)
                    }
                }
            }
        }

        items(assistants) { asst ->
            val branch = branches.find { it.id == asst.assignedBranchId }
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, OutlineBorderColor.copy(alpha = 0.5f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(text = asst.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text(text = "هاتف: ${asst.phone} | طبيعة العمل والتحضير: ${asst.role}", fontSize = 11.sp, color = Color.Gray)
                        Text(text = "مكاني: ${branch?.name ?: "كل الفروع"}", fontSize = 11.sp, color = Color.DarkGray)
                    }
                    Box(
                        modifier = Modifier
                            .background(DeepTealPrimary.copy(alpha = 0.1f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = asst.role,
                            color = DeepTealPrimary,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

// ==========================================
// SUB-DIALOG COMPONENTS
// ==========================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddStudentDialog(
    groups: List<Group>,
    branches: List<Branch>,
    onDismiss: () -> Unit,
    onAdd: (Student) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var barcode by remember { mutableStateOf("") }
    var studentPhone by remember { mutableStateOf("") }
    var parentPhone by remember { mutableStateOf("") }
    var selectedGroupIdx by remember { mutableStateOf(0) }
    var registrationFee by remember { mutableStateOf("100") }
    var isRegistrationFeePaid by remember { mutableStateOf(true) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "تسجيل طالب جديد في النظام המוחד 👤",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = DeepTealPrimary
                )

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("اسم الطالب بالكامل") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = barcode,
                    onValueChange = { barcode = it },
                    label = { Text("كود الباركود الفريد (مثال: 1006)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = studentPhone,
                    onValueChange = { studentPhone = it },
                    label = { Text("هاتف الطالب") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = parentPhone,
                    onValueChange = { parentPhone = it },
                    label = { Text("هاتف ولي الأمر للحضور والغائب") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier.fillMaxWidth()
                )

                if (groups.isNotEmpty()) {
                    var expandedGroup by remember { mutableStateOf(false) }
                    Text("المجموعة الأساسية:", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    Box {
                        OutlinedButton(
                            onClick = { expandedGroup = true },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            val activeG = groups[selectedGroupIdx]
                            val b = branches.find { it.id == activeG.branchId }
                            Text("${activeG.name} - ${b?.name ?: ""}")
                        }
                        DropdownMenu(expanded = expandedGroup, onDismissRequest = { expandedGroup = false }) {
                            groups.forEachIndexed { idx, group ->
                                val b = branches.find { it.id == group.branchId }
                                DropdownMenuItem(
                                    text = { Text("${group.name} - ${b?.name ?: ""}") },
                                    onClick = {
                                        selectedGroupIdx = idx
                                        expandedGroup = false
                                    }
                                )
                            }
                        }
                    }
                }

                OutlinedTextField(
                    value = registrationFee,
                    onValueChange = { registrationFee = it },
                    label = { Text("رسوم جدولة الحجز المبدئي (ج.م)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(checked = isRegistrationFeePaid, onCheckedChange = { isRegistrationFeePaid = it })
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("تم دفع رسوم الحجز فور تسجيله بالاستقبال ✅", fontSize = 12.sp)
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) { Text("إلغاء") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (name.isNotEmpty() && barcode.isNotEmpty() && groups.isNotEmpty()) {
                                onAdd(
                                    Student(
                                        barcode = barcode,
                                        name = name,
                                        phone = studentPhone,
                                        parentPhone = parentPhone,
                                        nativeGroupId = groups[selectedGroupIdx].id,
                                        status = "Active",
                                        registrationFee = registrationFee.toDoubleOrNull() ?: 0.0,
                                        isRegistrationFeePaid = isRegistrationFeePaid
                                    )
                                )
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = DeepTealPrimary)
                    ) {
                        Text("تسجيل وحفظ")
                    }
                }
            }
        }
    }
}

// Other Dialog imports to facilitate inputs

@Composable
fun AddBranchDialog(
    onDismiss: () -> Unit,
    onAdd: (Branch) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var commission by remember { mutableStateOf("30") }
    var bookCommission by remember { mutableStateOf("5") }

    Dialog(onDismissRequest = onDismiss) {
        Card(shape = RoundedCornerShape(16.dp)) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("إضافة سنتر / فرع جديد 🏘️", fontWeight = FontWeight.Bold, color = DeepTealPrimary)

                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("اسم السنتر (مثالي: سنتر النخبة)") })
                OutlinedTextField(value = location, onValueChange = { location = it }, label = { Text("الموقع واللوكيشن الجغرافي") })
                OutlinedTextField(
                    value = commission,
                    onValueChange = { commission = it },
                    label = { Text("نسبة السنتر من الحصص والاشتراكات θ (%)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                OutlinedTextField(
                    value = bookCommission,
                    onValueChange = { bookCommission = it },
                    label = { Text("نسبة السنتر من مبيعات الملازم φ (%)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("إلغاء") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (name.isNotEmpty()) {
                                onAdd(
                                    Branch(
                                        name = name,
                                        location = location,
                                        commissionPercentage = (commission.toDoubleOrNull() ?: 30.0) / 100.0,
                                        bookCommissionPercentage = (bookCommission.toDoubleOrNull() ?: 5.0) / 100.0
                                    )
                                )
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = DeepTealPrimary)
                    ) {
                        Text("حفظ السنتر")
                    }
                }
            }
        }
    }
}

@Composable
fun AddGroupDialog(
    branches: List<Branch>,
    onDismiss: () -> Unit,
    onAdd: (Group) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var selectedBranchIdx by remember { mutableStateOf(0) }
    var timeSlots by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Card(shape = RoundedCornerShape(16.dp)) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("تأسيس مجموعة تعليمية للمدرس 📅", fontWeight = FontWeight.Bold, color = DeepTealPrimary)

                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("اسم المجموعة (مثل: السبت ٤ عصراً)") })
                OutlinedTextField(value = timeSlots, onValueChange = { timeSlots = it }, label = { Text("اليوم والتوقيت الدقيق بالفروع") })

                if (branches.isNotEmpty()) {
                    var expanded by remember { mutableStateOf(false) }
                    Text("السنتر المستضيف:", fontWeight = FontWeight.Bold)
                    Box {
                        OutlinedButton(onClick = { expanded = true }, modifier = Modifier.fillMaxWidth()) {
                            Text(branches[selectedBranchIdx].name)
                        }
                        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                            branches.forEachIndexed { idx, branch ->
                                DropdownMenuItem(
                                    text = { Text(branch.name) },
                                    onClick = {
                                        selectedBranchIdx = idx
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("إلغاء") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (name.isNotEmpty() && branches.isNotEmpty()) {
                                onAdd(
                                    Group(
                                        name = name,
                                        branchId = branches[selectedBranchIdx].id,
                                        timeSlots = timeSlots
                                    )
                                )
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = DeepTealPrimary)
                    ) {
                        Text("حفظ وتأسيس")
                    }
                }
            }
        }
    }
}

@Composable
fun AddMaterialDialog(
    branches: List<Branch>,
    onDismiss: () -> Unit,
    onAdd: (Material) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("75") }
    var stockCount by remember { mutableStateOf("100") }
    var selectedBranchIdx by remember { mutableStateOf(0) }

    Dialog(onDismissRequest = onDismiss) {
        Card(shape = RoundedCornerShape(16.dp)) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("إضافة كتاب / مذكرة دراسية للمخازن 📚", fontWeight = FontWeight.Bold, color = DeepTealPrimary)

                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("عنوان المذكرة أو الكتاب") })
                OutlinedTextField(value = price, onValueChange = { price = it }, label = { Text("سعر البيع للطالب") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                OutlinedTextField(value = stockCount, onValueChange = { stockCount = it }, label = { Text("الكمية المتوفرة بالمقر") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))

                if (branches.isNotEmpty()) {
                    var expanded by remember { mutableStateOf(false) }
                    Text("مقر السنتر المودَع فيه المخزون:", fontWeight = FontWeight.Bold)
                    Box {
                        OutlinedButton(onClick = { expanded = true }, modifier = Modifier.fillMaxWidth()) {
                            Text(branches[selectedBranchIdx].name)
                        }
                        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                            branches.forEachIndexed { idx, branch ->
                                DropdownMenuItem(
                                    text = { Text(branch.name) },
                                    onClick = {
                                        selectedBranchIdx = idx
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("إلغاء") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (name.isNotEmpty() && branches.isNotEmpty()) {
                                onAdd(
                                    Material(
                                        name = name,
                                        price = price.toDoubleOrNull() ?: 0.0,
                                        stockCount = stockCount.toIntOrNull() ?: 0,
                                        branchId = branches[selectedBranchIdx].id
                                    )
                                )
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = DeepTealPrimary)
                    ) {
                        Text("حفظ وتخزين")
                    }
                }
            }
        }
    }
}

@Composable
fun AddAssistantDialog(
    branches: List<Branch>,
    onDismiss: () -> Unit,
    onAdd: (Assistant) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var role by remember { mutableStateOf("Receptionist") }
    var salaryType by remember { mutableStateOf("Daily") }
    var salaryValue by remember { mutableStateOf("150") }
    var selectedBranchIdx by remember { mutableStateOf(0) }
    var limitedToBranch by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(shape = RoundedCornerShape(16.dp)) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text("إدراج مساعد أو موظف سكرتارية جديد 💼", fontWeight = FontWeight.Bold, color = DeepTealPrimary)

                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("الاسم بالكامل للمساعد") })
                OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("رقم الهاتف") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone))

                Text("الدور الوظيفي والصلاحيات:", fontWeight = FontWeight.Bold)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(selected = role == "Receptionist", onClick = { role = "Receptionist" }, label = { Text("استقبال") })
                    FilterChip(selected = role == "Academic Assistant", onClick = { role = "Academic Assistant" }, label = { Text("مساعد أكاديمي") })
                    FilterChip(selected = role == "Admin", onClick = { role = "Admin" }, label = { Text("مشرف عام") })
                }

                Text("طريقة حساب المستحقات والرواتب:", fontWeight = FontWeight.Bold)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    FilterChip(selected = salaryType == "Daily", onClick = { salaryType = "Daily" }, label = { Text("يومية") })
                    FilterChip(selected = salaryType == "Monthly", onClick = { salaryType = "Monthly" }, label = { Text("راتب شهري") })
                    FilterChip(selected = salaryType == "Per Student", onClick = { salaryType = "Per Student" }, label = { Text("بالطالب") })
                }

                OutlinedTextField(value = salaryValue, onValueChange = { salaryValue = it }, label = { Text("قيمة الراتب أو العمولة (ج.م)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = limitedToBranch, onCheckedChange = { limitedToBranch = it })
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("تقييد صلاحياته وسجلاته بسنتر محدد 🏘️", fontSize = 11.sp)
                }

                if (limitedToBranch && branches.isNotEmpty()) {
                    var expanded by remember { mutableStateOf(false) }
                    Box {
                        OutlinedButton(onClick = { expanded = true }, modifier = Modifier.fillMaxWidth()) {
                            Text(branches[selectedBranchIdx].name)
                        }
                        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                            branches.forEachIndexed { idx, branch ->
                                DropdownMenuItem(
                                    text = { Text(branch.name) },
                                    onClick = {
                                        selectedBranchIdx = idx
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("إلغاء") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (name.isNotEmpty()) {
                                onAdd(
                                    Assistant(
                                        name = name,
                                        phone = phone,
                                        role = role,
                                        salaryType = salaryType,
                                        salaryValue = salaryValue.toDoubleOrNull() ?: 0.0,
                                        assignedBranchId = if (limitedToBranch && branches.isNotEmpty()) branches[selectedBranchIdx].id else null
                                    )
                                )
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = DeepTealPrimary)
                    ) {
                        Text("حفظ المساعد")
                    }
                }
            }
        }
    }
}

@Composable
fun AddExpenseDialog(
    branches: List<Branch>,
    onDismiss: () -> Unit,
    onAdd: (Transaction) -> Unit
) {
    var amount by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var selectedBranchIdx by remember { mutableStateOf(0) }

    Dialog(onDismissRequest = onDismiss) {
        Card(shape = RoundedCornerShape(16.dp)) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("صرف مصروف تشغيلي جاري (C_j) 💸", fontWeight = FontWeight.Bold, color = StatusDebtRed)

                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text("قيمة المصروف الكلي (ج.م)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("الملاحظات وبند الصرف (مثال: طباعة أوراق)") }
                )

                if (branches.isNotEmpty()) {
                    var expanded by remember { mutableStateOf(false) }
                    Text("يُحسب ويُخصم من إيراد سنتر:", fontWeight = FontWeight.Bold)
                    Box {
                        OutlinedButton(onClick = { expanded = true }, modifier = Modifier.fillMaxWidth()) {
                            Text(branches[selectedBranchIdx].name)
                        }
                        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                            branches.forEachIndexed { idx, branch ->
                                DropdownMenuItem(
                                    text = { Text(branch.name) },
                                    onClick = {
                                        selectedBranchIdx = idx
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("إلغاء") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (amount.isNotEmpty() && branches.isNotEmpty()) {
                                onAdd(
                                    Transaction(
                                        studentBarcode = "",
                                        type = "Expense",
                                        amount = amount.toDoubleOrNull() ?: 0.0,
                                        discountAmount = 0.0,
                                        tutorRatio = 1.0,
                                        branchId = branches[selectedBranchIdx].id,
                                        notes = notes
                                    )
                                )
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = StatusDebtRed)
                    ) {
                        Text("تسجيل المصرف")
                    }
                }
            }
        }
    }
}

@Composable
fun AddIncomeDialog(
    branches: List<Branch>,
    students: List<Student>,
    materials: List<Material>,
    onDismiss: () -> Unit,
    onAdd: (Transaction) -> Unit
) {
    var selectedType by remember { mutableStateOf("Monthly Subscription") }
    var studentBarcode by remember { mutableStateOf("") }
    var selectedMaterialIdx by remember { mutableStateOf(0) }
    var amount by remember { mutableStateOf("200") }
    var discount by remember { mutableStateOf("0") }
    var tutorRatio by remember { mutableStateOf(1.0f) } // slider for discount bearing
    var selectedBranchIdx by remember { mutableStateOf(0) }
    var notes by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Card(shape = RoundedCornerShape(16.dp)) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text("تسجيل تدفق مالي وارد للمدرس 💰", fontWeight = FontWeight.Bold, color = DeepTealPrimary)

                Text("بند التحصيل:", fontWeight = FontWeight.Bold)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    FilterChip(selected = selectedType == "Monthly Subscription", onClick = { selectedType = "Monthly Subscription"; amount="200" }, label = { Text("اشتراك شهر") })
                    FilterChip(selected = selectedType == "Book Sale", onClick = { selectedType = "Book Sale"; amount="75" }, label = { Text("بيع كتاب") })
                    FilterChip(selected = selectedType == "Per Lesson Fee", onClick = { selectedType = "Per Lesson Fee"; amount="50" }, label = { Text("حصة يومية") })
                }

                if (students.isNotEmpty()) {
                    var expandedSt by remember { mutableStateOf(false) }
                    var stNameChosen by remember { mutableStateOf("اختر الطالب") }
                    Text("الطالب الدافع:", fontWeight = FontWeight.Bold)
                    Box {
                        OutlinedButton(onClick = { expandedSt = true }, modifier = Modifier.fillMaxWidth()) {
                            Text(stNameChosen)
                        }
                        DropdownMenu(expanded = expandedSt, onDismissRequest = { expandedSt = false }) {
                            students.forEach { s ->
                                DropdownMenuItem(
                                    text = { Text("${s.name} (${s.barcode})") },
                                    onClick = {
                                        studentBarcode = s.barcode
                                        stNameChosen = s.name
                                        expandedSt = false
                                    }
                                )
                            }
                        }
                    }
                } else {
                    OutlinedTextField(value = studentBarcode, onValueChange = { studentBarcode = it }, label = { Text("رمز باركود الطالب") })
                }

                if (selectedType == "Book Sale" && materials.isNotEmpty()) {
                    var expandedMat by remember { mutableStateOf(false) }
                    Text("الكتاب / المذكرة المستلمة المسجلة:", fontWeight = FontWeight.Bold)
                    Box {
                        OutlinedButton(onClick = { expandedMat = true }, modifier = Modifier.fillMaxWidth()) {
                            Text(materials[selectedMaterialIdx].name)
                        }
                        DropdownMenu(expanded = expandedMat, onDismissRequest = { expandedMat = false }) {
                            materials.forEachIndexed { idx, mat ->
                                DropdownMenuItem(
                                    text = { Text("${mat.name} - ${mat.price} ج.م") },
                                    onClick = {
                                        selectedMaterialIdx = idx
                                        amount = mat.price.toString()
                                        expandedMat = false
                                    }
                                )
                            }
                        }
                    }
                }

                OutlinedTextField(value = amount, onValueChange = { amount = it }, label = { Text("القيمة المحصلة الإجمالية (ج.م)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                OutlinedTextField(value = discount, onValueChange = { discount = it }, label = { Text("قيمة الخصم المالي الممنوح (ج.م)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))

                if ((discount.toDoubleOrNull() ?: 0.0) > 0.0) {
                    Text(text = "معامل توزيع الخصم w_j (نسبة تحمل المعلم للخصم):", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Slider(
                            value = tutorRatio,
                            onValueChange = { tutorRatio = it },
                            valueRange = 0.0f..1.0f,
                            steps = 1,
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = when(tutorRatio) {
                                1.0f -> "100% المدرس"
                                0.5f -> "50/50 مناصفة"
                                else -> "100% السنتر"
                            },
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                if (branches.isNotEmpty()) {
                    var expandedBr by remember { mutableStateOf(false) }
                    Text("يُودع في خزنة سنتر:", fontWeight = FontWeight.Bold)
                    Box {
                        OutlinedButton(onClick = { expandedBr = true }, modifier = Modifier.fillMaxWidth()) {
                            Text(branches[selectedBranchIdx].name)
                        }
                        DropdownMenu(expanded = expandedBr, onDismissRequest = { expandedBr = false }) {
                            branches.forEachIndexed { idx, branch ->
                                DropdownMenuItem(
                                    text = { Text(branch.name) },
                                    onClick = {
                                        selectedBranchIdx = idx
                                        expandedBr = false
                                    }
                                )
                            }
                        }
                    }
                }

                OutlinedTextField(value = notes, onValueChange = { notes = it }, label = { Text("ملاحظات") })

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("إلغاء") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (amount.isNotEmpty() && branches.isNotEmpty()) {
                                onAdd(
                                    Transaction(
                                        studentBarcode = studentBarcode,
                                        materialId = if (selectedType == "Book Sale" && materials.isNotEmpty()) materials[selectedMaterialIdx].id else null,
                                        type = selectedType,
                                        amount = amount.toDoubleOrNull() ?: 0.0,
                                        discountAmount = discount.toDoubleOrNull() ?: 0.0,
                                        tutorRatio = tutorRatio.toDouble(),
                                        branchId = branches[selectedBranchIdx].id,
                                        notes = notes
                                    )
                                )
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = DeepTealPrimary)
                    ) {
                        Text("تسجيل وحفظ")
                    }
                }
            }
        }
    }
}

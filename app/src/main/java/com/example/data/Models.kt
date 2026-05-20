package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "teachers")
data class Teacher(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val subject: String,
    val phone: String
)

@Entity(tableName = "branches")
data class Branch(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,     // Name of the center (e.g., Heliopolis, Giza, etc.)
    val location: String,
    val commissionPercentage: Double,    // theta_j - Center percentage fee on session/monthly (e.g. 0.3 for 30%)
    val bookCommissionPercentage: Double // phi_j - Center percentage fee on study books (e.g. 0.05 for 5%)
)

@Entity(tableName = "assistants")
data class Assistant(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val phone: String,
    val role: String, // "Admin", "Receptionist", "Academic Assistant"
    val salaryType: String, // "Monthly", "Daily", "Per Student"
    val salaryValue: Double,
    val assignedBranchId: Int? = null // Null if global, else limited to a specific branch
)

@Entity(tableName = "groups")
data class Group(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,       // Group name (e.g. Saturday 4 PM)
    val branchId: Int,      // Which branch does this group belong to
    val timeSlots: String   // Schedule text description
)

@Entity(tableName = "students")
data class Student(
    @PrimaryKey val barcode: String, // UNIQUE Global barcode
    val name: String,
    val phone: String,
    val parentPhone: String,
    val nativeGroupId: Int,  // Primary group registered to
    val status: String,      // "Active", "Paused", "Suspended"
    val registrationFee: Double = 0.0,
    val isRegistrationFeePaid: Boolean = false
)

@Entity(tableName = "attendance")
data class Attendance(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val studentBarcode: String,
    val groupId: Int,         // Group scanned into (could be an alternate group for Makeup)
    val timestamp: Long = System.currentTimeMillis(),
    val status: String,        // "Present", "Absent", "Makeup"
    val scannedBranchId: Int, // Branch where they scanned
    val originalGroupId: Int  // Student's original native group at study time
)

@Entity(tableName = "materials")
data class Material(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,         // Book / Study booklet name
    val price: Double,
    val stockCount: Int,
    val branchId: Int         // Material stock is branch-specific
)

@Entity(tableName = "transactions")
data class Transaction(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val studentBarcode: String,       // Empty/Null if it's a general expense
    val materialId: Int? = null,      // Link to bought book (nullable)
    val type: String,                 // "Monthly Subscription", "Per Lesson Fee", "Book Sale", "Registration Fee", "Expense"
    val amount: Double,
    val discountAmount: Double = 0.0,
    val tutorRatio: Double = 1.0,     // w_j: Ratio borne by tutor. 1.0 = tutor bears 100%, 0.5 = 50%, 0.0 = center bears 100%
    val branchId: Int,                // Branch where transaction took place
    val timestamp: Long = System.currentTimeMillis(),
    val createdByAssistantId: Int? = null,
    val notes: String = ""
)

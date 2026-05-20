package com.example.data

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.Update
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.flow.Flow
import java.util.concurrent.Executors

@Dao
interface EducationDao {

    // Teacher
    @Query("SELECT * FROM teachers LIMIT 1")
    suspend fun getTeacher(): Teacher?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTeacher(teacher: Teacher)

    // Branches (السناتر)
    @Query("SELECT * FROM branches ORDER BY id ASC")
    fun getAllBranchesFlow(): Flow<List<Branch>>

    @Query("SELECT * FROM branches")
    suspend fun getAllBranches(): List<Branch>

    @Query("SELECT * FROM branches WHERE id = :id")
    suspend fun getBranchById(id: Int): Branch?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBranch(branch: Branch): Long

    @Query("DELETE FROM branches WHERE id = :id")
    suspend fun deleteBranch(id: Int)

    // Assistants (المساعدون)
    @Query("SELECT * FROM assistants ORDER BY id ASC")
    fun getAllAssistantsFlow(): Flow<List<Assistant>>

    @Query("SELECT * FROM assistants")
    suspend fun getAllAssistants(): List<Assistant>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAssistant(assistant: Assistant): Long

    @Query("DELETE FROM assistants WHERE id = :id")
    suspend fun deleteAssistant(id: Int)

    // Groups (المجموعات)
    @Query("SELECT * FROM groups ORDER BY id ASC")
    fun getAllGroupsFlow(): Flow<List<Group>>

    @Query("SELECT * FROM groups")
    suspend fun getAllGroups(): List<Group>

    @Query("SELECT * FROM groups WHERE id = :id")
    suspend fun getGroupById(id: Int): Group?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGroup(group: Group): Long

    @Query("DELETE FROM groups WHERE id = :id")
    suspend fun deleteGroup(id: Int)

    // Students
    @Query("SELECT * FROM students ORDER BY name ASC")
    fun getAllStudentsFlow(): Flow<List<Student>>

    @Query("SELECT * FROM students")
    suspend fun getAllStudents(): List<Student>

    @Query("SELECT * FROM students WHERE barcode = :barcode")
    suspend fun getStudentByBarcode(barcode: String): Student?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStudent(student: Student)

    @Update
    suspend fun updateStudent(student: Student)

    @Query("DELETE FROM students WHERE barcode = :barcode")
    suspend fun deleteStudent(barcode: String)

    // Attendance
    @Query("SELECT * FROM attendance ORDER BY timestamp DESC")
    fun getAllAttendanceFlow(): Flow<List<Attendance>>

    @Query("SELECT * FROM attendance")
    suspend fun getAllAttendance(): List<Attendance>

    @Query("SELECT * FROM attendance WHERE studentBarcode = :barcode ORDER BY timestamp DESC")
    suspend fun getAttendanceForStudent(barcode: String): List<Attendance>

    @Query("SELECT * FROM attendance WHERE studentBarcode = :barcode AND groupId = :groupId ORDER BY timestamp DESC LIMIT 1")
    suspend fun getAttendanceForStudentInGroup(barcode: String, groupId: Int): Attendance?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAttendance(attendance: Attendance): Long

    @Query("DELETE FROM attendance WHERE id = :id")
    suspend fun deleteAttendance(id: Int)

    // Materials (الكتب والمذكرات)
    @Query("SELECT * FROM materials ORDER BY name ASC")
    fun getAllMaterialsFlow(): Flow<List<Material>>

    @Query("SELECT * FROM materials")
    suspend fun getAllMaterials(): List<Material>

    @Query("SELECT * FROM materials WHERE branchId = :branchId")
    suspend fun getMaterialsForBranch(branchId: Int): List<Material>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMaterial(material: Material): Long

    @Update
    suspend fun updateMaterial(material: Material)

    @Query("DELETE FROM materials WHERE id = :id")
    suspend fun deleteMaterial(id: Int)

    // Transactions (الماليات)
    @Query("SELECT * FROM transactions ORDER BY timestamp DESC")
    fun getAllTransactionsFlow(): Flow<List<Transaction>>

    @Query("SELECT * FROM transactions")
    suspend fun getAllTransactions(): List<Transaction>

    @Query("SELECT * FROM transactions WHERE studentBarcode = :barcode")
    suspend fun getTransactionsForStudent(barcode: String): List<Transaction>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: Transaction): Long

    @Query("DELETE FROM transactions WHERE id = :id")
    suspend fun deleteTransaction(id: Int)
}

@Database(
    entities = [
        Teacher::class,
        Branch::class,
        Assistant::class,
        Group::class,
        Student::class,
        Attendance::class,
        Material::class,
        Transaction::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun educationDao(): EducationDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "education_erp_db"
                )
                    .addCallback(DatabaseSeederCallback())
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseSeederCallback : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                // Seed data on a background thread
                Executors.newSingleThreadExecutor().execute {
                    // We seed directly into database with simple SQLite statements for speed and first-boot reliability
                    
                    // 1. Teacher
                    db.execSQL("INSERT INTO teachers (id, name, subject, phone) VALUES (1, 'الأستاذ أحمد الشافعي', 'الفيزياء للثانوية العامة', '01012345678')")

                    // 2. Branches (السناتر)
                    db.execSQL("INSERT INTO branches (id, name, location, commissionPercentage, bookCommissionPercentage) VALUES (1, 'سنتر النخبة - مصر الجديدة', 'مصر الجديدة، القاهرة', 0.30, 0.05)")
                    db.execSQL("INSERT INTO branches (id, name, location, commissionPercentage, bookCommissionPercentage) VALUES (2, 'سنتر التفوق - الهرم', 'شارع الهرم، الجيزة', 0.25, 0.00)")
                    db.execSQL("INSERT INTO branches (id, name, location, commissionPercentage, bookCommissionPercentage) VALUES (3, 'مجموعات الدعم - المدرسة الثانوية', 'مجموعات الدعم الرسمية بالهرم', 0.10, 0.00)")

                    // 3. Assistants
                    db.execSQL("INSERT INTO assistants (id, name, phone, role, salaryType, salaryValue, assignedBranchId) VALUES (1, 'مريم أحمد', '01123456789', 'Receptionist', 'Daily', 150.0, 2)")
                    db.execSQL("INSERT INTO assistants (id, name, phone, role, salaryType, salaryValue, assignedBranchId) VALUES (2, 'م. أحمد يحيى', '01234567890', 'Academic Assistant', 'Monthly', 3000.0, NULL)")
                    db.execSQL("INSERT INTO assistants (id, name, phone, role, salaryType, salaryValue, assignedBranchId) VALUES (3, 'عبد الرحمن علي', '01511112222', 'Admin', 'Per Student', 5.0, 1)")

                    // 4. Groups
                    db.execSQL("INSERT INTO groups (id, name, branchId, timeSlots) VALUES (1, 'مجموعة السبت ٤ عصراً (شرح)', 1, 'السبت 04:00م - 06:00م')")
                    db.execSQL("INSERT INTO groups (id, name, branchId, timeSlots) VALUES (2, 'مجموعة الأحد ٦ مساءً (شرح)', 2, 'الأحد 06:00م - 08:00م')")
                    db.execSQL("INSERT INTO groups (id, name, branchId, timeSlots) VALUES (3, 'مجموعة الثلاثاء ٢ ظهراً (مراجعة)', 1, 'الثلاثاء 02:00م - 04:00م')")
                    db.execSQL("INSERT INTO groups (id, name, branchId, timeSlots) VALUES (4, 'مجموعة الأربعاء ٥ مساءً (دعم)', 3, 'الأربعاء 05:00م - 07:00م')")

                    // 5. Materials
                    db.execSQL("INSERT INTO materials (id, name, price, stockCount, branchId) VALUES (1, 'كتاب الشرح: الباب الأول (الكهربية)', 75.0, 120, 1)")
                    db.execSQL("INSERT INTO materials (id, name, price, stockCount, branchId) VALUES (2, 'كتيب الأسئلة والتدريبات العامة', 50.0, 85, 1)")
                    db.execSQL("INSERT INTO materials (id, name, price, stockCount, branchId) VALUES (3, 'كتاب الشرح: الباب الأول (الكهربية)', 75.0, 90, 2)")
                    db.execSQL("INSERT INTO materials (id, name, price, stockCount, branchId) VALUES (4, 'كتيب الأسئلة والتدريبات العامة', 50.0, 60, 2)")

                    // 6. Students
                    // Barcodes are kept simple for scanner simulation
                    db.execSQL("INSERT INTO students (barcode, name, phone, parentPhone, nativeGroupId, status, registrationFee, isRegistrationFeePaid) VALUES ('1001', 'يوسف أحمد حسن', '01011112222', '01211112222', 1, 'Active', 100.0, 1)")
                    db.execSQL("INSERT INTO students (barcode, name, phone, parentPhone, nativeGroupId, status, registrationFee, isRegistrationFeePaid) VALUES ('1002', 'فاطمة محمد علي', '01033334444', '01233334444', 2, 'Active', 100.0, 0)")
                    db.execSQL("INSERT INTO students (barcode, name, phone, parentPhone, nativeGroupId, status, registrationFee, isRegistrationFeePaid) VALUES ('1003', 'عمر خالد محمود', '01055556666', '01255556666', 1, 'Suspended', 100.0, 0)")
                    db.execSQL("INSERT INTO students (barcode, name, phone, parentPhone, nativeGroupId, status, registrationFee, isRegistrationFeePaid) VALUES ('1004', 'سارة محمود عبد العزيز', '01577778888', '01177778888', 3, 'Active', 100.0, 1)")
                    db.execSQL("INSERT INTO students (barcode, name, phone, parentPhone, nativeGroupId, status, registrationFee, isRegistrationFeePaid) VALUES ('1005', 'أحمد كمال الشافعي', '01088889999', '01288889999', 4, 'Active', 50.0, 1)")

                    // 7. Transactions
                    // Seed some initial subscription payments to show math calculation
                    // Format: studentBarcode, materialId, type, amount, discountAmount, tutorRatio, branchId, timestamp, notes
                    db.execSQL("INSERT INTO transactions (studentBarcode, materialId, type, amount, discountAmount, tutorRatio, branchId, timestamp, notes) VALUES ('1001', NULL, 'Monthly Subscription', 200.0, 0.0, 1.0, 1, strftime('%s','now')*1000 - 86400000, 'اشتراك شهر مايو')")
                    db.execSQL("INSERT INTO transactions (studentBarcode, materialId, type, amount, discountAmount, tutorRatio, branchId, timestamp, notes) VALUES ('1001', 1, 'Book Sale', 75.0, 0.0, 1.0, 1, strftime('%s','now')*1000 - 86400000, 'استلام كتاب الكهربية')")
                    db.execSQL("INSERT INTO transactions (studentBarcode, materialId, type, amount, discountAmount, tutorRatio, branchId, timestamp, notes) VALUES ('1002', NULL, 'Monthly Subscription', 150.0, 30.0, 0.5, 2, strftime('%s','now')*1000 - 172800000, 'خصم تفوق مالي مشترَك')")
                    db.execSQL("INSERT INTO transactions (studentBarcode, materialId, type, amount, discountAmount, tutorRatio, branchId, timestamp, notes) VALUES ('1004', NULL, 'Monthly Subscription', 200.0, 0.0, 1.0, 1, strftime('%s','now')*1000 - 259200000, 'اشتراك شهر مايو')")
                    db.execSQL("INSERT INTO transactions (studentBarcode, materialId, type, amount, discountAmount, tutorRatio, branchId, timestamp, notes) VALUES ('', NULL, 'Expense', 120.0, 0.0, 1.0, 1, strftime('%s','now')*1000 - 345600000, 'طباعة ورق تدريبات إضافي')")
                    db.execSQL("INSERT INTO transactions (studentBarcode, materialId, type, amount, discountAmount, tutorRatio, branchId, timestamp, notes) VALUES ('', NULL, 'Expense', 150.0, 0.0, 1.0, 2, strftime('%s','now')*1000 - 172800000, 'يومية سكرتارية مريم')")
                }
            }
        }
    }
}

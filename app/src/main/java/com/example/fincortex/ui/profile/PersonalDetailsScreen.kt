package com.example.fincortex.ui.profile

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.edit
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.fincortex.R
import com.example.fincortex.ui.theme.DarkAccent
import com.example.fincortex.ui.theme.DarkBackground
import com.example.fincortex.ui.theme.DarkPrimary
import com.example.fincortex.ui.theme.DarkText
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PersonalDetailsScreen(navController: NavController) {
    val context = LocalContext.current
    val sharedPreferences = remember { context.getSharedPreferences("user_details", Context.MODE_PRIVATE) }
    
    var fullName by remember { mutableStateOf(sharedPreferences.getString("full_name", "Aryan Ganesh Patil") ?: "Aryan Ganesh Patil") }
    var dob by remember { mutableStateOf(sharedPreferences.getString("dob", "**/ **/ 2006") ?: "**/ **/ 2006") }
    var mobileNumber by remember { mutableStateOf(sharedPreferences.getString("mobile_number", "***** 73767") ?: "***** 73767") }
    var email by remember { mutableStateOf(sharedPreferences.getString("email", "pat*********9@gmail.com") ?: "pat*********9@gmail.com") }
    var panNumber by remember { mutableStateOf(sharedPreferences.getString("pan_number", "****** 489N") ?: "****** 489N") }
    var gender by remember { mutableStateOf(sharedPreferences.getString("gender", "Male") ?: "Male") }
    var maritalStatus by remember { mutableStateOf(sharedPreferences.getString("marital_status", "Single") ?: "Single") }
    var profileImageUri by remember { mutableStateOf<Uri?>(null) }
    
    var showBottomSheet by remember { mutableStateOf(false) }
    var showNameDialog by remember { mutableStateOf(false) }
    var showMobileDialog by remember { mutableStateOf(false) }
    var showEmailDialog by remember { mutableStateOf(false) }
    var showPanDialog by remember { mutableStateOf(false) }
    var showGenderDialog by remember { mutableStateOf(false) }
    var showMaritalDialog by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    
    var newName by remember { mutableStateOf(fullName) }
    var newMobile by remember { mutableStateOf(mobileNumber) }
    var newEmail by remember { mutableStateOf(email) }
    var newPan by remember { mutableStateOf(panNumber) }
    
    val datePickerState = rememberDatePickerState()
    val sheetState = rememberModalBottomSheetState()

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri ->
            uri?.let { profileImageUri = it }
            showBottomSheet = false
        }
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding())
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = DarkText)
            }
            Text(
                text = "Personal details",
                color = DarkText,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 8.dp)
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            // Profile Picture Section
            Box(contentAlignment = Alignment.BottomCenter) {
                if (profileImageUri != null) {
                    AsyncImage(
                        model = profileImageUri,
                        contentDescription = "Profile Picture",
                        modifier = Modifier
                            .size(140.dp)
                            .clip(CircleShape)
                            .background(DarkPrimary)
                            .clickable { showBottomSheet = true },
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Image(
                        painter = painterResource(id = R.drawable.fincortex_logo),
                        contentDescription = "Profile Picture",
                        modifier = Modifier
                            .size(140.dp)
                            .clip(CircleShape)
                            .background(DarkPrimary)
                            .clickable { showBottomSheet = true },
                        contentScale = ContentScale.Crop
                    )
                }
                Surface(
                    modifier = Modifier
                        .offset(y = 12.dp)
                        .clickable { showBottomSheet = true },
                    shape = RoundedCornerShape(20.dp),
                    color = Color.Black.copy(alpha = 0.6f),
                    border = IconButtonDefaults.outlinedIconButtonBorder(true)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CameraAlt,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "Edit", color = Color.White, fontSize = 14.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(48.dp))

            // Detail Fields
            DetailItem(
                label = "Full name (as on PAN card)", 
                value = fullName, 
                showEdit = true, 
                onClick = { 
                    newName = fullName
                    showNameDialog = true 
                }
            )
            DetailItem(
                label = "Date of Birth", 
                value = dob, 
                showEdit = true,
                onClick = { showDatePicker = true }
            )
            DetailItem(
                label = "Mobile Number", 
                value = mobileNumber, 
                showEdit = true,
                onClick = {
                    newMobile = mobileNumber
                    showMobileDialog = true
                }
            )
            DetailItem(
                label = "Email", 
                value = email, 
                showEdit = true,
                onClick = {
                    newEmail = email
                    showEmailDialog = true
                }
            )
            DetailItem(
                label = "PAN number", 
                value = panNumber, 
                showEdit = true,
                onClick = {
                    newPan = panNumber
                    showPanDialog = true
                }
            )
            DetailItem(
                label = "Gender", 
                value = gender, 
                showEdit = true,
                onClick = { showGenderDialog = true }
            )
            DetailItem(
                label = "Marital Status", 
                value = maritalStatus, 
                showEdit = true,
                onClick = { showMaritalDialog = true }
            )
            DetailItem(label = "Occupation", value = "Student", showEdit = true)
            DetailItem(label = "Father's Name", value = "Ganesh Patil", showEdit = true)

            Spacer(modifier = Modifier.height(32.dp))
        }
    }

    if (showNameDialog) {
        AlertDialog(
            onDismissRequest = { showNameDialog = false },
            title = { Text("Update Name") },
            text = {
                TextField(
                    value = newName,
                    onValueChange = { newName = it },
                    label = { Text("Full name (as on PAN card)") },
                    singleLine = true
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    fullName = newName
                    sharedPreferences.edit { putString("full_name", newName) }
                    showNameDialog = false
                }) {
                    Text("Update")
                }
            },
            dismissButton = {
                TextButton(onClick = { showNameDialog = false }) {
                    Text("Cancel")
                }
            },
            containerColor = DarkPrimary,
            titleContentColor = DarkText,
            textContentColor = DarkText
        )
    }

    if (showMobileDialog) {
        AlertDialog(
            onDismissRequest = { showMobileDialog = false },
            title = { Text("Update Mobile Number") },
            text = {
                TextField(
                    value = newMobile,
                    onValueChange = { newMobile = it },
                    label = { Text("Mobile Number") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    mobileNumber = newMobile
                    sharedPreferences.edit { putString("mobile_number", newMobile) }
                    showMobileDialog = false
                }) {
                    Text("Update")
                }
            },
            dismissButton = {
                TextButton(onClick = { showMobileDialog = false }) {
                    Text("Cancel")
                }
            },
            containerColor = DarkPrimary,
            titleContentColor = DarkText,
            textContentColor = DarkText
        )
    }

    if (showEmailDialog) {
        AlertDialog(
            onDismissRequest = { showEmailDialog = false },
            title = { Text("Update Email") },
            text = {
                TextField(
                    value = newEmail,
                    onValueChange = { newEmail = it },
                    label = { Text("Email") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    email = newEmail
                    sharedPreferences.edit { putString("email", newEmail) }
                    showEmailDialog = false
                }) {
                    Text("Update")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEmailDialog = false }) {
                    Text("Cancel")
                }
            },
            containerColor = DarkPrimary,
            titleContentColor = DarkText,
            textContentColor = DarkText
        )
    }

    if (showPanDialog) {
        AlertDialog(
            onDismissRequest = { showPanDialog = false },
            title = { Text("Update PAN Number") },
            text = {
                TextField(
                    value = newPan,
                    onValueChange = { newPan = it.uppercase() },
                    label = { Text("PAN Number") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters)
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    panNumber = newPan
                    sharedPreferences.edit { putString("pan_number", newPan) }
                    showPanDialog = false
                }) {
                    Text("Update")
                }
            },
            dismissButton = {
                TextButton(onClick = { showPanDialog = false }) {
                    Text("Cancel")
                }
            },
            containerColor = DarkPrimary,
            titleContentColor = DarkText,
            textContentColor = DarkText
        )
    }

    if (showGenderDialog) {
        val genders = listOf("Male", "Female", "Other")
        AlertDialog(
            onDismissRequest = { showGenderDialog = false },
            title = { Text("Select Gender") },
            text = {
                Column {
                    genders.forEach { option ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    gender = option
                                    sharedPreferences.edit { putString("gender", option) }
                                    showGenderDialog = false
                                }
                                .padding(vertical = 12.dp)
                        ) {
                            RadioButton(
                                selected = (option == gender),
                                onClick = null,
                                colors = RadioButtonDefaults.colors(selectedColor = DarkAccent)
                            )
                            Text(
                                text = option,
                                color = DarkText,
                                modifier = Modifier.padding(start = 16.dp)
                            )
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showGenderDialog = false }) {
                    Text("Cancel")
                }
            },
            containerColor = DarkPrimary,
            titleContentColor = DarkText,
            textContentColor = DarkText
        )
    }

    if (showMaritalDialog) {
        AlertDialog(
            onDismissRequest = { showMaritalDialog = false },
            title = { 
                Text(
                    text = "What's your marital status?",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                ) 
            },
            text = {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    val statuses = listOf("Single", "Married")
                    statuses.forEach { status ->
                        OutlinedButton(
                            onClick = {
                                maritalStatus = status
                                sharedPreferences.edit { putString("marital_status", status) }
                                showMaritalDialog = false
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(56.dp),
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, if (maritalStatus == status) DarkAccent else Color.Gray.copy(alpha = 0.5f)),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = if (maritalStatus == status) DarkAccent else DarkText
                            )
                        ) {
                            Text(text = status)
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showMaritalDialog = false }) {
                    Text("Cancel")
                }
            },
            containerColor = DarkPrimary,
            titleContentColor = DarkText,
            textContentColor = DarkText
        )
    }

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val sdf = SimpleDateFormat("dd/ MM/ yyyy", Locale.getDefault())
                        val date = sdf.format(Date(millis))
                        dob = date
                        sharedPreferences.edit { putString("dob", date) }
                    }
                    showDatePicker = false
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel")
                }
            },
            colors = DatePickerDefaults.colors(containerColor = DarkPrimary)
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (showBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = { showBottomSheet = false },
            sheetState = sheetState,
            containerColor = DarkPrimary,
            contentColor = DarkText
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp)
            ) {
                Text(
                    text = "Change profile picture",
                    modifier = Modifier.padding(16.dp),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                
                ListItem(
                    headlineContent = { Text("Select from gallery") },
                    leadingContent = { Icon(Icons.Default.Image, contentDescription = null) },
                    trailingContent = { Icon(Icons.Default.ChevronRight, contentDescription = null) },
                    modifier = Modifier.clickable { galleryLauncher.launch("image/*") },
                    colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                )
                
                ListItem(
                    headlineContent = { Text("Remove Picture") },
                    leadingContent = { Icon(Icons.Default.Delete, contentDescription = null) },
                    modifier = Modifier.clickable { 
                        profileImageUri = null
                        showBottomSheet = false
                    },
                    colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                )
            }
        }
    }
}

@Composable
fun DetailItem(label: String, value: String, showEdit: Boolean, onClick: (() -> Unit)? = null) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp)
            .clickable(enabled = onClick != null) { onClick?.invoke() }
    ) {
        Text(text = label, color = DarkText.copy(alpha = 0.6f), fontSize = 14.sp)
        Spacer(modifier = Modifier.height(4.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = value,
                color = DarkText,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )
            if (showEdit) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Edit",
                    tint = DarkText.copy(alpha = 0.8f),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
        HorizontalDivider(color = DarkText.copy(alpha = 0.1f), thickness = 1.dp)
    }
}

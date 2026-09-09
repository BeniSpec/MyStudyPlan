package com.example.mystudyplan

import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MaterialTheme {
                val context = LocalContext.current
                CourseScreen(context)
            }
        }
    }
}

// ---------- Top-level screen: form + search + list, combined ----------
@Composable
fun CourseScreen(context: Context) {
    val dbHandler: DBHandler = remember { DBHandler(context) }

    var refreshTrigger by remember { mutableIntStateOf(0) }
    var courseList by remember { mutableStateOf(listOf<Course>()) }
    var searchQuery by remember { mutableStateOf("") }
    var editingCourse by remember { mutableStateOf<Course?>(null) }

    LaunchedEffect(refreshTrigger) {
        courseList = dbHandler.getAllCourses()
    }

    val visibleCourses = courseList.filter {
        it.name.contains(searchQuery, ignoreCase = true)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(start = 20.dp, end = 20.dp, top = 70.dp, bottom = 20.dp), // 👈 pushed further down
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AddDataToDatabase(
            context = context,
            dbHandler = dbHandler,
            editingCourse = editingCourse,
            onCourseSaved = {
                refreshTrigger++
                editingCourse = null
            },
            onCancelEdit = { editingCourse = null }
        )

        Spacer(modifier = Modifier.height(20.dp))
        HorizontalDivider()
        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = "Saved Courses",
            color = Color.Black, fontSize = 18.sp, fontWeight = FontWeight.Bold
        )

        Text(
            text = "${courseList.size} course(s) saved",
            fontSize = 13.sp,
            color = Color.Gray
        )
        Spacer(modifier = Modifier.height(10.dp))

        TextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search courses...") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        Spacer(modifier = Modifier.height(10.dp))

        CourseListView(
            courses = visibleCourses,
            onDelete = { id ->
                dbHandler.deleteCourse(id)
                refreshTrigger++
                if (editingCourse?.id == id) editingCourse = null
            },
            onEdit = { course -> editingCourse = course }
        )
    }
}

@Composable
fun AddDataToDatabase(
    context: Context,
    dbHandler: DBHandler,
    editingCourse: Course?,
    onCourseSaved: () -> Unit,
    onCancelEdit: () -> Unit
) {
    val courseName = remember { mutableStateOf(TextFieldValue()) }
    val courseDuration = remember { mutableStateOf(TextFieldValue()) }
    val courseTracks = remember { mutableStateOf(TextFieldValue()) }
    val courseDescription = remember { mutableStateOf(TextFieldValue()) }

    LaunchedEffect(editingCourse) {
        if (editingCourse != null) {
            courseName.value = TextFieldValue(editingCourse.name)
            courseDuration.value = TextFieldValue(editingCourse.duration)
            courseTracks.value = TextFieldValue(editingCourse.tracks)
            courseDescription.value = TextFieldValue(editingCourse.description)
        } else {
            courseName.value = TextFieldValue()
            courseDuration.value = TextFieldValue()
            courseTracks.value = TextFieldValue()
            courseDescription.value = TextFieldValue()
        }
    }

    Column(
        modifier = Modifier.fillMaxWidth().padding(all = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = if (editingCourse == null) "SQLite Database in Android"
                   else "Editing: ${editingCourse.name}",
            color = Color.Black, fontSize = 20.sp, fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(20.dp))
        TextField(
            value = courseName.value,
            onValueChange = { courseName.value = it },
            placeholder = { Text(text = "Enter your course name") },
            modifier = Modifier.fillMaxWidth(),
            textStyle = TextStyle(color = Color.Black, fontSize = 15.sp),
            singleLine = true,
        )
        Spacer(modifier = Modifier.height(20.dp))
        TextField(
            value = courseDuration.value,
            onValueChange = { courseDuration.value = it },
            placeholder = { Text(text = "Enter your course duration") },
            modifier = Modifier.fillMaxWidth(),
            textStyle = TextStyle(color = Color.Black, fontSize = 15.sp),
            singleLine = true,
        )
        Spacer(modifier = Modifier.height(20.dp))
        TextField(
            value = courseTracks.value,
            onValueChange = { courseTracks.value = it },
            placeholder = { Text(text = "Enter your course tracks") },
            modifier = Modifier.fillMaxWidth(),
            textStyle = TextStyle(color = Color.Black, fontSize = 15.sp),
            singleLine = true,
        )
        Spacer(modifier = Modifier.height(20.dp))
        TextField(
            value = courseDescription.value,
            onValueChange = { courseDescription.value = it },
            placeholder = { Text(text = "Enter your course description") },
            modifier = Modifier.fillMaxWidth(),
            textStyle = TextStyle(color = Color.Black, fontSize = 15.sp),
            singleLine = true,
        )
        Spacer(modifier = Modifier.height(15.dp))

        Row(horizontalArrangement = Arrangement.Center) {
            Button(onClick = {
                if (courseName.value.text.isBlank()) {
                    Toast.makeText(context, "Course name is required", Toast.LENGTH_SHORT).show()
                } else if (editingCourse == null) {
                    dbHandler.addNewCourse(
                        courseName.value.text,
                        courseDuration.value.text,
                        courseDescription.value.text,
                        courseTracks.value.text
                    )
                    Toast.makeText(context, "Course Added to Database", Toast.LENGTH_SHORT).show()
                    onCourseSaved()
                } else {
                    dbHandler.updateCourse(
                        editingCourse.id,
                        courseName.value.text,
                        courseDuration.value.text,
                        courseDescription.value.text,
                        courseTracks.value.text
                    )
                    Toast.makeText(context, "Course Updated", Toast.LENGTH_SHORT).show()
                    onCourseSaved()
                }
            }) {
                Text(
                    text = if (editingCourse == null) "Add Course to Database" else "Update Course",
                    color = Color.White
                )
            }

            if (editingCourse != null) {
                Spacer(modifier = Modifier.width(10.dp))
                OutlinedButton(onClick = onCancelEdit) {
                    Text(text = "Cancel")
                }
            }
        }
    }
}

// ---------- The list view ----------
@Composable
fun CourseListView(
    courses: List<Course>,
    onDelete: (Int) -> Unit,
    onEdit: (Course) -> Unit
) {
    if (courses.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxWidth().padding(20.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(text = "No courses found.", color = Color.Gray, fontSize = 14.sp)
        }
        return
    }
    LazyColumn(modifier = Modifier.fillMaxWidth()) {
        items(courses, key = { it.id }) { course ->
            CourseRow(
                course = course,
                onDelete = { onDelete(course.id) },
                onEdit = { onEdit(course) }
            )
            Spacer(modifier = Modifier.height(10.dp))
        }
    }
}

@Composable
fun CourseRow(course: Course, onDelete: () -> Unit, onEdit: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = course.name, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = "Duration: ${course.duration}", fontSize = 13.sp, color = Color.DarkGray)
                Text(text = "Tracks: ${course.tracks}", fontSize = 13.sp, color = Color.DarkGray)
                Text(text = course.description, fontSize = 13.sp, color = Color.Gray)
            }
            Row {
                IconButton(onClick = onEdit) {
                    Icon(
                        imageVector = Icons.Filled.Edit,
                        contentDescription = "Edit course",
                        tint = Color(0xFF3F51B5)
                    )
                }
                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Filled.Delete,
                        contentDescription = "Delete course",
                        tint = Color.Red
                    )
                }
            }
        }
    }
}
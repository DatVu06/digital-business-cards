package cs446.dbc.views

import android.app.DatePickerDialog
import android.content.Context
import android.util.Log
import android.widget.DatePicker
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.Interaction
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import cs446.dbc.R
import cs446.dbc.viewmodels.AppViewModel
import cs446.dbc.viewmodels.CreateEditViewModel
import cs446.dbc.viewmodels.EventViewModel
import java.text.DateFormatSymbols
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateEventScreen(createEditViewModel: CreateEditViewModel, eventViewModel: EventViewModel, appViewModel: AppViewModel, appContext: Context, navController: NavHostController, eventId: String? = null) {
    val events by eventViewModel.events.collectAsStateWithLifecycle()

    // TODO: ensure eventId gets set back to null afterwards
    //  even if the user leaves the page and doesn't save their changes
    appViewModel.updateScreenTitle("${if (eventId != null) "Edit" else "Create"} Event${if (eventId != null) ": " + events.find { it.id == eventId }?.name else ""}")

    var name by remember {
        mutableStateOf(TextFieldValue(""))
    }
    var location by remember {
        mutableStateOf(TextFieldValue(""))
    }
    var numUsers by remember {
        mutableIntStateOf(0)
    }
    val defaultMaxUsers = 1000
    var maxUsers by remember {
        mutableIntStateOf(defaultMaxUsers)
    }
    var maxUsersSet by remember {
        mutableStateOf(false)
    }
    var startDate by rememberSaveable {
        mutableLongStateOf(Date().time)
    }
    val threeDays: Long = 1000 * 60 * 60 * 24 * 3
    var endDate by rememberSaveable {
        mutableLongStateOf(Date().time + threeDays)
    }

    LaunchedEffect (key1 = "observeNameChange", name){
       createEditViewModel.createEditEvent.value.name = name.text
    }
    LaunchedEffect (key1 = "observeLocationChange", location){
        createEditViewModel.createEditEvent.value.location = location.text
    }
    LaunchedEffect (key1 = "observeNumUsersChange", numUsers){
        createEditViewModel.createEditEvent.value.numUsers = numUsers
    }
    LaunchedEffect (key1 = "observeMaxUsersChange", maxUsers){
        createEditViewModel.createEditEvent.value.maxUsers = maxUsers
    }
    LaunchedEffect (key1 = "observeMaxUsersSetChange", maxUsersSet){
        if (maxUsersSet) {
            createEditViewModel.createEditEvent.value.maxUsersSet = true
        }
    }
    LaunchedEffect (key1 = "observeStartDateChange", startDate){
        createEditViewModel.createEditEvent.value.startDate = Date(startDate).time.toString()
        //Log.d("start date", startDate.toString())
    }
    LaunchedEffect (key1 = "observeEndDateChange", endDate){
        createEditViewModel.createEditEvent.value.endDate = Date(endDate).time.toString()
    }

    //val dateFormat = SimpleDateFormat(, Locale.getDefault())

    // TODO: Technically the FAB should be deciding this, right...
    LaunchedEffect(key1 = "event_examples") {
        if (eventId != null) {
            // TODO: create a new event
            // TODO: send cards to event (pick which ones?)
            // TODO: Allow cards to be autoshared
            val currEvent = events.find { it.id == eventId }!!
            name = TextFieldValue(currEvent.name)
            location = TextFieldValue(currEvent.location)
            numUsers = currEvent.numUsers
            maxUsers = currEvent.maxUsers
            startDate = Date(currEvent.startDate.toLong()).time
            endDate = Date(currEvent.endDate.toLong()).time
            // TODO: Add in the dates
        }
    }

    Box (
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp, 16.dp)
    ){
        Column (
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {

            
            OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text(text = "Event Name") },
                placeholder = {
                Text(text = "e.g. Deep Learning Summit")
            }, modifier = Modifier.fillMaxSize())

            Spacer(modifier = Modifier.padding(4.dp))

            OutlinedTextField(value = location, onValueChange = { location = it }, label = { Text(text = "Event Location") },
                placeholder = {
                    Text(text = "e.g. Toronto, Ontario")
            }, modifier = Modifier.fillMaxSize())

            Spacer(modifier = Modifier.padding(4.dp))

            // TODO: Don't allow end date to be longer than 3 days from start date
            DateTextField("Start Date", Date(startDate)) {
                startDate = it
                Log.d("startdate changed", startDate.toString())
            }

            Spacer(modifier = Modifier.padding(4.dp))

            DateTextField("End Date", Date(endDate)) { endDate = it }

            Spacer(modifier = Modifier.padding(4.dp))

            OutlinedTextField(value = maxUsers.toString(), onValueChange = {
                // Ensure value is non-negative
                maxUsers = if ((it.toIntOrNull() ?: 0) < 1) 1
                else it.toIntOrNull() ?: defaultMaxUsers
                maxUsersSet = maxUsers != defaultMaxUsers
               },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
                label = { Text(text = "Max Number of Participants")})
        }
    }

    // TODO: now we need to put all of this information into somewhere so the save button can work!!!
}

//@OptIn(ExperimentalMaterial3Api::class)
@ExperimentalMaterial3Api
@Composable
fun DateTextField(textFieldLabel: String, currDate: Date, date: (Long) -> Unit) {
    val source = remember { MutableInteractionSource() }
    val isPressed: Boolean by source.collectIsPressedAsState()
    val customThemeResId = R.style.CustomDatePickerDialogTheme
    var isDatePickerDialogOpen by remember {
        mutableStateOf(false)
    }


    val currentDate = currDate.toFormattedString()
    val calendar = Calendar.getInstance()
    val year: Int = calendar.get(Calendar.YEAR)
    val month: Int = calendar.get(Calendar.MONTH)
    val day: Int = calendar.get(Calendar.DAY_OF_MONTH)
    calendar.time = Date()

    val context = LocalContext.current
    var selectedDate by rememberSaveable { mutableStateOf(currentDate) }

    //val datePickerDialog =
//        DatePickerDialog(context, customThemeResId, { _: DatePicker, yr: Int, month: Int, dayOfMonth: Int ->
//            val newDate = Calendar.getInstance()
//            newDate.set(yr, month, dayOfMonth)
//            Log.d("newDate set", newDate.toString())
//            selectedDate = "${month.toMonthName()} $dayOfMonth, $yr"
//            Log.d("selectedDate changed", selectedDate)
//            date(newDate.timeInMillis)
//        }, year, month, day)
    // TODO: we'll add the textfield as read only, and then add in the
    Row {
        OutlinedTextField(
            readOnly = true,
            enabled = false,
            value = selectedDate,
            onValueChange = {},
            trailingIcon = { Icons.Default.DateRange },
            //interactionSource = source,
            modifier = Modifier
                .clickable { isDatePickerDialogOpen = true }
                .fillMaxWidth(),
            label = { Text(text = textFieldLabel) },
            colors = OutlinedTextFieldDefaults.colors(
                disabledTextColor = MaterialTheme.colorScheme.onSurface,
                disabledBorderColor = MaterialTheme.colorScheme.outline,
                disabledPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant,
                disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                disabledTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant
            )
        )
//        IconButton(onClick = { isDatePickerDialogOpen = true }) {
//            Icon(imageVector = Icons.Default.CalendarMonth, contentDescription = "Calendar")
//        }
    }

    // TODO: we need 2 variables to be altered, one for start date and another for end date
    //if (isPressed) {
//        datePickerDialog()
      if (isDatePickerDialogOpen) {
          CustomDatePickerDialog(initialValue = currDate.time,
              onConfirm = { newDateLong: Long? ->
                  val cal = Calendar.getInstance(TimeZone.getTimeZone("GMT-5:00"))
                  cal.time = Date(newDateLong!!)
                  var month = cal.get(Calendar.MONTH)
//                  var (correctedMonth, dayOfMonth) = correctDayOfMonth(month, cal.get(Calendar.DAY_OF_MONTH))
//                  month += correctedMonth
                  //val newDate = Date(newDateLong!!)
                  //selectedDate = newDate.toFormattedString()
                  //val month =
                  selectedDate = "${
                      month.toMonthName()
                  } ${cal.get(Calendar.DAY_OF_MONTH)}, ${cal.get(Calendar.YEAR)}"
                  date(newDateLong)
                  isDatePickerDialogOpen = false
              }
          ) {
              isDatePickerDialogOpen = false
          }
      }
}

@ExperimentalMaterial3Api
@Composable
private fun CustomDatePickerDialog(
    initialValue: Long,
    onConfirm: (Long?) -> Unit,
    onDismiss: () -> Unit
) {
    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = initialValue)
    DatePickerDialog(
        onDismissRequest = { onDismiss() },
        confirmButton = {
            Button(onClick = { onConfirm(datePickerState.selectedDateMillis)}) {
                Text(text = "Confirm")
            }
        },
        dismissButton = {
            Button(onClick = { onDismiss() }) {
                Text(text = "Cancel")
            }
        }
    ){
        DatePicker(state = datePickerState)
    }
}

private fun Int.toMonthName(): String {
    return DateFormatSymbols().months[this]
}

private fun Date.toFormattedString(): String {
    val simpleDateFormat = SimpleDateFormat("LLLL dd, yyyy", Locale.getDefault())
    return simpleDateFormat.format(this)
}

private fun correctDayOfMonth(month: Int, dayOfMonth: Int): Pair<Int, Int> {
    // Correcting for February 29th
    return if (month == 1 && dayOfMonth == 28) {
        Pair<Int, Int>(1, 1)
    }
    else {
        Pair<Int, Int>(0, dayOfMonth + 1)
    }
}

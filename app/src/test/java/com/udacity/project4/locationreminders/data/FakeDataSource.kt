package com.udacity.project4.locationreminders.data

import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result

//Use FakeDataSource that acts as a test double to the LocalDataSource
class FakeDataSource(var reminders: MutableList<ReminderDTO>? = mutableListOf()) :
    ReminderDataSource {

    private var returnError: Boolean = false

    override suspend fun getReminders(): Result<List<ReminderDTO>> {
        if (returnError)
            return Result.Error("test error")
        reminders?.let {
            return Result.Success(ArrayList(it))
        }
        return Result.Error("data error or not founf")
    }

    override suspend fun saveReminder(reminder: ReminderDTO) {
        reminders?.add(reminder)
    }

    override suspend fun getReminder(id: String): Result<ReminderDTO> {
        if (returnError)
            return Result.Error("test error")
        reminders?.forEach {
            if (it.id == id)
                return Result.Success(it)
        }
        return Result.Error("no reminder with id = $id")
    }

    override suspend fun deleteAllReminders() {
        reminders?.clear()
    }

    fun setError(error: Boolean) {
        returnError = error
    }


}
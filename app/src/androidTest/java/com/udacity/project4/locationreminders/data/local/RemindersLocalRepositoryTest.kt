package com.udacity.project4.locationreminders.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.udacity.project4.MainCoroutineRule
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.instanceOf
import org.hamcrest.MatcherAssert
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
//Medium Test to test the repository
@MediumTest
class RemindersLocalRepositoryTest {

    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    private lateinit var database: RemindersDatabase
    private lateinit var remindersDAO: RemindersDao
    private lateinit var repository: RemindersLocalRepository

    @Before
    fun init() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            RemindersDatabase::class.java
        ).build()
        remindersDAO = database.reminderDao()
        repository = RemindersLocalRepository(remindersDAO, Dispatchers.Main)
    }
    @After
    fun closeDb() {
        database.close()
    }
    @Test
    fun insertReminderAndGetById() = runBlockingTest {
        val reminderDTO = ReminderDTO("google", "Google Company", "USA", 20.0, 10.0)
        repository.saveReminder(reminderDTO)
        val getRemindersFromDb = repository.getReminder(reminderDTO.id) as Result.Success<ReminderDTO>
        val loadedReminder = getRemindersFromDb.data
        assertThat(
            loadedReminder.id,
            Matchers.`is`(reminderDTO.id)
        )
        assertThat(
            loadedReminder.title,
            Matchers.`is`(reminderDTO.title)
        )
        assertThat(
            loadedReminder.description,
            Matchers.`is`(reminderDTO.description)
        )
        assertThat(
            loadedReminder.location,
            Matchers.`is`(reminderDTO.location)
        )
        assertThat(
            loadedReminder.latitude,
            Matchers.`is`(reminderDTO.latitude)
        )
        assertThat(
            loadedReminder.longitude,
            Matchers.`is`(reminderDTO.longitude)
        )
    }
    @Test
    fun noIdFound() = runBlockingTest {
        val getReminderByID = repository.getReminder("100") as Result.Error
        MatcherAssert.assertThat(
            getReminderByID.message,
            Matchers.`is`(CoreMatchers.notNullValue())
        )
    }

    @Test
    fun deleteAll() = runBlockingTest {
        val reminderDTO = ReminderDTO("google", "Google Company", "USA", 20.0, 10.0)
        repository.saveReminder(reminderDTO)
        repository.deleteAllReminders()
        repository.getReminders()
        val getAllReminders = repository.getReminders() as Result.Success<List<ReminderDTO>>
        val result = getAllReminders.data
        MatcherAssert.assertThat(
            result.isEmpty(),
            Matchers.`is`(true)
        )
    }
}
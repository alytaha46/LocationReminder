package com.udacity.project4.locationreminders.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.SmallTest;
import com.udacity.project4.locationreminders.data.dto.ReminderDTO

import org.junit.Before;
import org.junit.Rule;
import org.junit.runner.RunWith;

import kotlinx.coroutines.ExperimentalCoroutinesApi;
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.notNullValue
import org.hamcrest.MatcherAssert
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers
import org.junit.After
import org.junit.Test

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
//Unit test the DAO
@SmallTest
class RemindersDaoTest {
    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var database: RemindersDatabase

    @Before
    fun initDb() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            RemindersDatabase::class.java
        ).build()
    }

    @After
    fun closeDb() {
        database.close()
    }

    @Test
    fun insertReminderAndGetById() = runBlockingTest {
        val reminderDTO = ReminderDTO("google", "Google Company", "USA", 20.0, 10.0)
        database.reminderDao().saveReminder(reminderDTO)
        val loadedReminder = database.reminderDao().getReminderById(reminderDTO.id)
        MatcherAssert.assertThat(
            loadedReminder?.id,
            Matchers.`is`(reminderDTO.id)
        )
        MatcherAssert.assertThat(
            loadedReminder?.title,
            Matchers.`is`(reminderDTO.title)
        )
        MatcherAssert.assertThat(
            loadedReminder?.description,
            Matchers.`is`(reminderDTO.description)
        )
        MatcherAssert.assertThat(
            loadedReminder?.location,
            Matchers.`is`(reminderDTO.location)
        )
        MatcherAssert.assertThat(
            loadedReminder?.latitude,
            Matchers.`is`(reminderDTO.latitude)
        )
        MatcherAssert.assertThat(
            loadedReminder?.longitude,
            Matchers.`is`(reminderDTO.longitude)
        )
    }

    @Test
    fun noIdFound() = runBlockingTest {
        val loadedReminder = database.reminderDao().getReminderById("100")
        MatcherAssert.assertThat(
            loadedReminder,
            Matchers.`is`(CoreMatchers.nullValue())
        )
    }

    @Test
    fun deleteAll() = runBlockingTest {
        val reminderDTO = ReminderDTO("google", "Google Company", "USA", 20.0, 10.0)
        database.reminderDao().saveReminder(reminderDTO)
        database.reminderDao().deleteAllReminders()
        val result = database.reminderDao().getReminders()
        MatcherAssert.assertThat(
            result.isEmpty(),
            Matchers.`is`(true)
        )
    }
}
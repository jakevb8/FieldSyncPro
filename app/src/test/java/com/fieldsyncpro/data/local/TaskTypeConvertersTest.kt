package com.fieldsyncpro.data.local

import com.fieldsyncpro.domain.model.TaskStatus
import com.fieldsyncpro.domain.model.TaskVibe
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class TaskTypeConvertersTest {

    private lateinit var converters: TaskTypeConverters

    @Before
    fun setup() {
        converters = TaskTypeConverters()
    }

    // ── TaskStatus converters ─────────────────────────────────────────────────

    @Test
    fun `fromTaskStatus returns correct string for each status`() {
        assertEquals("PENDING",   converters.fromTaskStatus(TaskStatus.PENDING))
        assertEquals("SYNCING",   converters.fromTaskStatus(TaskStatus.SYNCING))
        assertEquals("COMPLETED", converters.fromTaskStatus(TaskStatus.COMPLETED))
        assertEquals("CONFLICT",  converters.fromTaskStatus(TaskStatus.CONFLICT))
    }

    @Test
    fun `toTaskStatus returns correct enum for each string`() {
        assertEquals(TaskStatus.PENDING,   converters.toTaskStatus("PENDING"))
        assertEquals(TaskStatus.SYNCING,   converters.toTaskStatus("SYNCING"))
        assertEquals(TaskStatus.COMPLETED, converters.toTaskStatus("COMPLETED"))
        assertEquals(TaskStatus.CONFLICT,  converters.toTaskStatus("CONFLICT"))
    }

    @Test
    fun `TaskStatus round-trips through converters`() {
        TaskStatus.values().forEach { status ->
            assertEquals(status, converters.toTaskStatus(converters.fromTaskStatus(status)))
        }
    }

    @Test(expected = IllegalArgumentException::class)
    fun `toTaskStatus throws for unknown string`() {
        converters.toTaskStatus("UNKNOWN")
    }

    // ── TaskVibe converters ───────────────────────────────────────────────────

    @Test
    fun `fromTaskVibe returns correct string for each vibe`() {
        assertEquals("Hype",   converters.fromTaskVibe(TaskVibe.Hype))
        assertEquals("Steady", converters.fromTaskVibe(TaskVibe.Steady))
        assertEquals("Chill",  converters.fromTaskVibe(TaskVibe.Chill))
    }

    @Test
    fun `toTaskVibe returns correct object for each string`() {
        assertTrue(converters.toTaskVibe("Hype")   is TaskVibe.Hype)
        assertTrue(converters.toTaskVibe("Steady") is TaskVibe.Steady)
        assertTrue(converters.toTaskVibe("Chill")  is TaskVibe.Chill)
    }

    @Test
    fun `TaskVibe round-trips through converters`() {
        listOf(TaskVibe.Hype, TaskVibe.Steady, TaskVibe.Chill).forEach { vibe ->
            assertEquals(
                vibe::class,
                converters.toTaskVibe(converters.fromTaskVibe(vibe))::class
            )
        }
    }

    @Test(expected = IllegalArgumentException::class)
    fun `toTaskVibe throws for unknown string`() {
        converters.toTaskVibe("UNKNOWN_VIBE")
    }
}

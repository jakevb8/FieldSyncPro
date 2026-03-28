package com.fieldsyncpro.data.repository

import com.fieldsyncpro.data.local.entity.TaskEntity
import com.fieldsyncpro.data.remote.dto.TaskDto
import com.fieldsyncpro.data.repository.TaskMapper.toDomain
import com.fieldsyncpro.data.repository.TaskMapper.toDto
import com.fieldsyncpro.data.repository.TaskMapper.toEntity
import com.fieldsyncpro.domain.model.FieldTask
import com.fieldsyncpro.domain.model.TaskStatus
import com.fieldsyncpro.domain.model.TaskVibe
import org.junit.Assert.*
import org.junit.Test

class TaskMapperTest {

    private val sampleTask = FieldTask(
        id          = "task-1",
        title       = "Fix Transformer",
        description = "Replace blown fuse on grid-sector 9",
        status      = TaskStatus.PENDING,
        vibe        = TaskVibe.Hype,
        lastSynced  = 1_000_000L,
        isLocalOnly = false
    )

    // ── FieldTask → TaskEntity ────────────────────────────────────────────────

    @Test
    fun `toEntity maps all fields correctly`() {
        val entity = sampleTask.toEntity()
        assertEquals(sampleTask.id,          entity.id)
        assertEquals(sampleTask.title,       entity.title)
        assertEquals(sampleTask.description, entity.description)
        assertEquals(sampleTask.status,      entity.status)
        assertEquals("Hype",                 entity.vibe)
        assertEquals(sampleTask.lastSynced,  entity.lastSynced)
        assertEquals(sampleTask.isLocalOnly, entity.isLocalOnly)
    }

    // ── TaskEntity → FieldTask ────────────────────────────────────────────────

    @Test
    fun `toDomain from entity maps all fields correctly`() {
        val entity = TaskEntity(
            id          = "task-2",
            title       = "Inspect Valve",
            description = "Routine inspection",
            status      = TaskStatus.COMPLETED,
            vibe        = "Chill",
            lastSynced  = 2_000_000L,
            isLocalOnly = true
        )
        val domain = entity.toDomain()
        assertEquals(entity.id,          domain.id)
        assertEquals(entity.title,       domain.title)
        assertEquals(entity.description, domain.description)
        assertEquals(TaskStatus.COMPLETED, domain.status)
        assertTrue(domain.vibe is TaskVibe.Chill)
        assertEquals(entity.lastSynced,  domain.lastSynced)
        assertTrue(domain.isLocalOnly)
    }

    // ── FieldTask → TaskDto ───────────────────────────────────────────────────

    @Test
    fun `toDto maps all fields correctly`() {
        val dto = sampleTask.toDto()
        assertEquals(sampleTask.id,          dto.id)
        assertEquals(sampleTask.title,       dto.title)
        assertEquals(sampleTask.description, dto.description)
        assertEquals("PENDING",              dto.status)
        assertEquals("Hype",                 dto.vibe)
        assertEquals(sampleTask.lastSynced,  dto.lastSynced)
    }

    // ── TaskDto → FieldTask ───────────────────────────────────────────────────

    @Test
    fun `toDomain from dto maps all fields correctly`() {
        val dto = TaskDto(
            id          = "task-3",
            title       = "Check Pressure",
            description = "Monitor line pressure",
            status      = "SYNCING",
            vibe        = "Steady",
            lastSynced  = 3_000_000L
        )
        val domain = dto.toDomain()
        assertEquals(dto.id,          domain.id)
        assertEquals(dto.title,       domain.title)
        assertEquals(dto.description, domain.description)
        assertEquals(TaskStatus.SYNCING, domain.status)
        assertTrue(domain.vibe is TaskVibe.Steady)
        assertEquals(dto.lastSynced,  domain.lastSynced)
        assertFalse(domain.isLocalOnly)
    }

    @Test
    fun `toDomain from dto with unknown status defaults to PENDING`() {
        val dto = TaskDto("id", "t", "d", "UNKNOWN_STATUS", "Steady", 0L)
        assertEquals(TaskStatus.PENDING, dto.toDomain().status)
    }

    @Test
    fun `toDomain from dto with unknown vibe defaults to Steady`() {
        val dto = TaskDto("id", "t", "d", "PENDING", "WEIRD_VIBE", 0L)
        assertTrue(dto.toDomain().vibe is TaskVibe.Steady)
    }

    // ── vibeToString / stringToVibe round-trip ────────────────────────────────

    @Test
    fun `vibeToString and stringToVibe are inverses for all vibes`() {
        listOf(TaskVibe.Hype, TaskVibe.Steady, TaskVibe.Chill).forEach { vibe ->
            assertEquals(vibe::class, TaskMapper.stringToVibe(TaskMapper.vibeToString(vibe))::class)
        }
    }

    // ── isLocalOnly forced to false from dto ──────────────────────────────────

    @Test
    fun `TaskDto toEntity sets isLocalOnly to false`() {
        val dto = TaskDto("id", "title", "desc", "PENDING", "Hype", 0L)
        assertFalse(dto.toEntity().isLocalOnly)
    }
}

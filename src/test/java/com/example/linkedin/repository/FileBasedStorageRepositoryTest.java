package com.example.linkedin.repository;

import com.example.linkedin.model.Interaction;
import com.example.linkedin.model.ResponseStatus;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for FileBasedStorageRepository.
 */
class FileBasedStorageRepositoryTest {
    
    @TempDir
    Path tempDir;
    
    private FileBasedStorageRepository repository;
    
    @BeforeEach
    void setUp() {
        repository = new FileBasedStorageRepository();
        
        // Set test directories using reflection
        ReflectionTestUtils.setField(repository, "storageDirectory", tempDir.toString());
        ReflectionTestUtils.setField(repository, "archiveDirectory", tempDir.resolve("archive").toString());
        ReflectionTestUtils.setField(repository, "interactionsFile", "interactions.json");
        ReflectionTestUtils.setField(repository, "processedCommentsFile", "processed-comments.json");
        ReflectionTestUtils.setField(repository, "maxCapacity", 10);
        
        repository.initialize();
    }
    
    @AfterEach
    void tearDown() throws IOException {
        // Clean up test files
        Files.walk(tempDir)
            .sorted((a, b) -> -a.compareTo(b))
            .forEach(path -> {
                try {
                    Files.deleteIfExists(path);
                } catch (IOException e) {
                    // Ignore cleanup errors
                }
            });
    }
    
    @Test
    void testSaveAndRetrieveInteraction() {
        // Create test interaction
        Interaction interaction = createTestInteraction("int-1", "post-1", "comment-1");
        
        // Save interaction
        repository.saveInteraction(interaction);
        
        // Retrieve and verify
        List<Interaction> history = repository.getInteractionHistory(null, null, null);
        assertEquals(1, history.size());
        assertEquals("int-1", history.get(0).getId());
        assertEquals("post-1", history.get(0).getPostId());
    }
    
    @Test
    void testMarkCommentProcessed() {
        String commentId = "comment-123";
        
        // Initially not processed
        assertFalse(repository.isCommentProcessed(commentId));
        
        // Mark as processed
        repository.markCommentProcessed(commentId);
        
        // Verify it's now processed
        assertTrue(repository.isCommentProcessed(commentId));
    }
    
    @Test
    void testFilterByPostId() {
        // Save interactions for different posts
        repository.saveInteraction(createTestInteraction("int-1", "post-1", "comment-1"));
        repository.saveInteraction(createTestInteraction("int-2", "post-2", "comment-2"));
        repository.saveInteraction(createTestInteraction("int-3", "post-1", "comment-3"));
        
        // Filter by post-1
        List<Interaction> filtered = repository.getInteractionHistory("post-1", null, null);
        
        assertEquals(2, filtered.size());
        assertTrue(filtered.stream().allMatch(i -> "post-1".equals(i.getPostId())));
    }
    
    @Test
    void testFilterByDateRange() {
        Instant now = Instant.now();
        Instant yesterday = now.minusSeconds(86400);
        Instant tomorrow = now.plusSeconds(86400);
        
        // Create interactions with different timestamps
        Interaction old = createTestInteraction("int-1", "post-1", "comment-1");
        old.setTimestamp(yesterday);
        
        Interaction current = createTestInteraction("int-2", "post-1", "comment-2");
        current.setTimestamp(now);
        
        repository.saveInteraction(old);
        repository.saveInteraction(current);
        
        // Filter by date range
        List<Interaction> filtered = repository.getInteractionHistory(null, now.minusSeconds(3600), tomorrow);
        
        assertEquals(1, filtered.size());
        assertEquals("int-2", filtered.get(0).getId());
    }
    
    @Test
    void testExportToJson() throws IOException {
        // Save some interactions
        repository.saveInteraction(createTestInteraction("int-1", "post-1", "comment-1"));
        repository.saveInteraction(createTestInteraction("int-2", "post-2", "comment-2"));
        
        // Export to JSON
        String exportPath = repository.exportHistory("json");
        
        // Verify file exists and is valid JSON
        assertTrue(Files.exists(Path.of(exportPath)));
        String content = Files.readString(Path.of(exportPath));
        assertTrue(content.contains("int-1"));
        assertTrue(content.contains("int-2"));
    }
    
    @Test
    void testExportToCsv() throws IOException {
        // Save some interactions
        repository.saveInteraction(createTestInteraction("int-1", "post-1", "comment-1"));
        
        // Export to CSV
        String exportPath = repository.exportHistory("csv");
        
        // Verify file exists and has CSV structure
        assertTrue(Files.exists(Path.of(exportPath)));
        String content = Files.readString(Path.of(exportPath));
        assertTrue(content.contains("ID,Post ID,Comment ID")); // Header
        assertTrue(content.contains("int-1"));
    }
    
    @Test
    void testArchivalOnCapacity() {
        // Fill to capacity (10 interactions)
        for (int i = 0; i < 10; i++) {
            repository.saveInteraction(createTestInteraction("int-" + i, "post-1", "comment-" + i));
        }
        
        // Add one more to trigger archival
        repository.saveInteraction(createTestInteraction("int-10", "post-1", "comment-10"));
        
        // Verify active storage is below capacity
        List<Interaction> history = repository.getInteractionHistory(null, null, null);
        assertTrue(history.size() < 10);
        
        // Verify archive directory has files
        Path archiveDir = Path.of(tempDir.toString(), "archive");
        assertTrue(Files.exists(archiveDir));
    }
    
    @Test
    void testInvalidExportFormat() {
        assertThrows(IllegalArgumentException.class, () -> {
            repository.exportHistory("xml");
        });
    }
    
    @Test
    void testNullCommentId() {
        assertThrows(IllegalArgumentException.class, () -> {
            repository.isCommentProcessed(null);
        });
        
        assertThrows(IllegalArgumentException.class, () -> {
            repository.markCommentProcessed(null);
        });
    }
    
    private Interaction createTestInteraction(String id, String postId, String commentId) {
        Interaction interaction = new Interaction();
        interaction.setId(id);
        interaction.setPostId(postId);
        interaction.setCommentId(commentId);
        interaction.setCommenterName("Test User");
        interaction.setCommentText("Test comment");
        interaction.setGeneratedResponse("Test response");
        interaction.setTimestamp(Instant.now());
        interaction.setStatus(ResponseStatus.GENERATED);
        return interaction;
    }
}

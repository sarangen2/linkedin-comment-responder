package com.example.linkedin.repository;

import com.example.linkedin.model.Interaction;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import jakarta.annotation.PostConstruct;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * File-based implementation of StorageRepository using JSON serialization.
 * Provides interaction history storage with capacity management and archival.
 */
@Repository
public class FileBasedStorageRepository implements StorageRepository {
    
    private static final Logger logger = LoggerFactory.getLogger(FileBasedStorageRepository.class);
    
    private final ObjectMapper objectMapper;
    private final Set<String> processedComments;
    private final List<Interaction> interactions;
    
    @Value("${storage.directory:./data}")
    private String storageDirectory;
    
    @Value("${storage.interactions.file:interactions.json}")
    private String interactionsFile;
    
    @Value("${storage.processed.file:processed-comments.json}")
    private String processedCommentsFile;
    
    @Value("${storage.max.capacity:1000}")
    private int maxCapacity;
    
    @Value("${storage.archive.directory:./data/archive}")
    private String archiveDirectory;
    
    public FileBasedStorageRepository() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
        this.objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        this.objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        
        this.processedComments = ConcurrentHashMap.newKeySet();
        this.interactions = Collections.synchronizedList(new ArrayList<>());
    }
    
    @PostConstruct
    public void initialize() {
        try {
            createDirectories();
            loadProcessedComments();
            loadInteractions();
            logger.info("Storage repository initialized successfully");
        } catch (IOException e) {
            logger.error("Failed to initialize storage repository", e);
            throw new RuntimeException("Storage initialization failed", e);
        }
    }
    
    private void createDirectories() throws IOException {
        Files.createDirectories(Paths.get(storageDirectory));
        Files.createDirectories(Paths.get(archiveDirectory));
    }
    
    @Override
    public void saveInteraction(Interaction interaction) {
        if (interaction == null) {
            throw new IllegalArgumentException("Interaction cannot be null");
        }
        
        synchronized (interactions) {
            // Check capacity and archive if needed
            if (interactions.size() >= maxCapacity) {
                archiveOldestInteractions();
            }
            
            interactions.add(interaction);
            persistInteractions();
            logger.debug("Saved interaction: {}", interaction.getId());
        }
    }
    
    @Override
    public boolean isCommentProcessed(String commentId) {
        if (commentId == null || commentId.isBlank()) {
            throw new IllegalArgumentException("Comment ID cannot be null or blank");
        }
        return processedComments.contains(commentId);
    }
    
    @Override
    public void markCommentProcessed(String commentId) {
        if (commentId == null || commentId.isBlank()) {
            throw new IllegalArgumentException("Comment ID cannot be null or blank");
        }
        
        processedComments.add(commentId);
        persistProcessedComments();
        logger.debug("Marked comment as processed: {}", commentId);
    }
    
    @Override
    public List<Interaction> getInteractionHistory(String postId, Instant startDate, Instant endDate) {
        synchronized (interactions) {
            return interactions.stream()
                .filter(interaction -> postId == null || postId.equals(interaction.getPostId()))
                .filter(interaction -> startDate == null || !interaction.getTimestamp().isBefore(startDate))
                .filter(interaction -> endDate == null || !interaction.getTimestamp().isAfter(endDate))
                .collect(Collectors.toList());
        }
    }
    
    @Override
    public String exportHistory(String format) {
        if (format == null || format.isBlank()) {
            throw new IllegalArgumentException("Export format cannot be null or blank");
        }
        
        String normalizedFormat = format.toLowerCase();
        if (!normalizedFormat.equals("json") && !normalizedFormat.equals("csv")) {
            throw new IllegalArgumentException("Export format must be 'json' or 'csv'");
        }
        
        try {
            String timestamp = DateTimeFormatter.ISO_INSTANT.format(Instant.now()).replace(":", "-");
            String filename = "history-export-" + timestamp + "." + normalizedFormat;
            Path exportPath = Paths.get(storageDirectory, filename);
            
            if (normalizedFormat.equals("json")) {
                exportToJson(exportPath);
            } else {
                exportToCsv(exportPath);
            }
            
            logger.info("Exported history to: {}", exportPath);
            return exportPath.toString();
        } catch (IOException e) {
            logger.error("Failed to export history", e);
            throw new RuntimeException("Export failed", e);
        }
    }
    
    private void loadProcessedComments() throws IOException {
        Path path = Paths.get(storageDirectory, processedCommentsFile);
        if (Files.exists(path)) {
            String json = Files.readString(path);
            Set<String> loaded = objectMapper.readValue(json, 
                objectMapper.getTypeFactory().constructCollectionType(Set.class, String.class));
            processedComments.addAll(loaded);
            logger.info("Loaded {} processed comments", processedComments.size());
        }
    }
    
    private void persistProcessedComments() {
        try {
            Path path = Paths.get(storageDirectory, processedCommentsFile);
            String json = objectMapper.writeValueAsString(processedComments);
            Files.writeString(path, json);
        } catch (IOException e) {
            logger.error("Failed to persist processed comments", e);
            throw new RuntimeException("Failed to save processed comments", e);
        }
    }
    
    private void loadInteractions() throws IOException {
        Path path = Paths.get(storageDirectory, interactionsFile);
        if (Files.exists(path)) {
            String json = Files.readString(path);
            List<Interaction> loaded = objectMapper.readValue(json,
                objectMapper.getTypeFactory().constructCollectionType(List.class, Interaction.class));
            interactions.addAll(loaded);
            logger.info("Loaded {} interactions", interactions.size());
        }
    }
    
    private void persistInteractions() {
        try {
            Path path = Paths.get(storageDirectory, interactionsFile);
            String json = objectMapper.writeValueAsString(interactions);
            Files.writeString(path, json);
        } catch (IOException e) {
            logger.error("Failed to persist interactions", e);
            throw new RuntimeException("Failed to save interactions", e);
        }
    }
    
    private void archiveOldestInteractions() {
        try {
            // Calculate how many to archive (archive 20% when at capacity)
            int archiveCount = Math.max(1, maxCapacity / 5);
            
            // Sort by timestamp and get oldest
            List<Interaction> toArchive = interactions.stream()
                .sorted(Comparator.comparing(Interaction::getTimestamp))
                .limit(archiveCount)
                .collect(Collectors.toList());
            
            // Create archive file
            String timestamp = DateTimeFormatter.ISO_INSTANT.format(Instant.now()).replace(":", "-");
            String archiveFilename = "archive-" + timestamp + ".json";
            Path archivePath = Paths.get(archiveDirectory, archiveFilename);
            
            // Write to archive
            String json = objectMapper.writeValueAsString(toArchive);
            Files.writeString(archivePath, json);
            
            // Remove from active storage
            interactions.removeAll(toArchive);
            
            logger.info("Archived {} interactions to {}", archiveCount, archivePath);
        } catch (IOException e) {
            logger.error("Failed to archive interactions", e);
            throw new RuntimeException("Archival failed", e);
        }
    }
    
    private void exportToJson(Path exportPath) throws IOException {
        synchronized (interactions) {
            String json = objectMapper.writeValueAsString(interactions);
            Files.writeString(exportPath, json);
        }
    }
    
    private void exportToCsv(Path exportPath) throws IOException {
        synchronized (interactions) {
            try (BufferedWriter writer = Files.newBufferedWriter(exportPath)) {
                // Write CSV header
                writer.write("ID,Post ID,Comment ID,Commenter Name,Comment Text,Generated Response,Posted Response,Timestamp,Status,Metadata\n");
                
                // Write data rows
                for (Interaction interaction : interactions) {
                    writer.write(escapeCsv(interaction.getId()));
                    writer.write(",");
                    writer.write(escapeCsv(interaction.getPostId()));
                    writer.write(",");
                    writer.write(escapeCsv(interaction.getCommentId()));
                    writer.write(",");
                    writer.write(escapeCsv(interaction.getCommenterName()));
                    writer.write(",");
                    writer.write(escapeCsv(interaction.getCommentText()));
                    writer.write(",");
                    writer.write(escapeCsv(interaction.getGeneratedResponse()));
                    writer.write(",");
                    writer.write(escapeCsv(interaction.getPostedResponse()));
                    writer.write(",");
                    writer.write(interaction.getTimestamp().toString());
                    writer.write(",");
                    writer.write(interaction.getStatus().toString());
                    writer.write(",");
                    writer.write(escapeCsv(formatMetadata(interaction.getMetadata())));
                    writer.write("\n");
                }
            }
        }
    }
    
    private String escapeCsv(String value) {
        if (value == null) {
            return "";
        }
        // Escape quotes and wrap in quotes if contains comma, quote, or newline
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
    
    private String formatMetadata(Map<String, String> metadata) {
        if (metadata == null || metadata.isEmpty()) {
            return "";
        }
        return metadata.entrySet().stream()
            .map(entry -> entry.getKey() + "=" + entry.getValue())
            .collect(Collectors.joining("; "));
    }
}

package com.secure.pro.tele_prompter.data.local.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

/**
 * Room Database Entity representing a teleprompter script.
 */
@Entity(tableName = "scripts")
public class ScriptEntity {

    @PrimaryKey(autoGenerate = true)
    private long id;

    @ColumnInfo(name = "title")
    private String title;

    @ColumnInfo(name = "content")
    private String content;

    @ColumnInfo(name = "category")
    private String category;

    @ColumnInfo(name = "font_size")
    private int fontSize;

    @ColumnInfo(name = "scroll_speed")
    private int scrollSpeed;

    @ColumnInfo(name = "line_spacing")
    private float lineSpacing;

    @ColumnInfo(name = "is_mirrored")
    private boolean isMirrored;

    @ColumnInfo(name = "word_count")
    private int wordCount;

    @ColumnInfo(name = "est_reading_time_seconds")
    private int estReadingTimeSeconds;

    @ColumnInfo(name = "created_at")
    private long createdAt;

    @ColumnInfo(name = "updated_at")
    private long updatedAt;

    public ScriptEntity() {
        this.fontSize = 32;
        this.scrollSpeed = 3;
        this.lineSpacing = 1.4f;
        this.isMirrored = false;
        long now = System.currentTimeMillis();
        this.createdAt = now;
        this.updatedAt = now;
        this.category = "General";
    }

    @Ignore
    public ScriptEntity(String title, String content, String category) {
        this();
        this.title = title;
        this.content = content;
        this.category = category;
    }

    // Getters and Setters
    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public int getFontSize() { return fontSize; }
    public void setFontSize(int fontSize) { this.fontSize = fontSize; }

    public int getScrollSpeed() { return scrollSpeed; }
    public void setScrollSpeed(int scrollSpeed) { this.scrollSpeed = scrollSpeed; }

    public float getLineSpacing() { return lineSpacing; }
    public void setLineSpacing(float lineSpacing) { this.lineSpacing = lineSpacing; }

    public boolean isMirrored() { return isMirrored; }
    public void setMirrored(boolean mirrored) { isMirrored = mirrored; }

    public int getWordCount() { return wordCount; }
    public void setWordCount(int wordCount) { this.wordCount = wordCount; }

    public int getEstReadingTimeSeconds() { return estReadingTimeSeconds; }
    public void setEstReadingTimeSeconds(int estReadingTimeSeconds) { this.estReadingTimeSeconds = estReadingTimeSeconds; }

    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }

    public long getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(long updatedAt) { this.updatedAt = updatedAt; }
}

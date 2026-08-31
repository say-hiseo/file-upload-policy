package com.assignment.fileuploadpolicy.domain.upload.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

@Entity
@Table(name = "uploaded_file")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode(of = "id")
public class UploadedFile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "original_filename", nullable = false, length = 255)
    private String originalFilename;

    @Column(name = "stored_filename", unique = true, length = 255)
    private String storedFilename;

    @Column(length = 20)
    private String extension;

    @Column(name = "detected_mime", length = 100)
    private String detectedMime;

    @Column(name = "magic_byte_matched")
    private Boolean magicByteMatched;

    @Column(name = "size_bytes", nullable = false)
    private long sizeBytes;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private UploadStatus status;

    @Column(name = "reject_reason", length = 255)
    private String rejectReason;

    @Column(name = "uploaded_by_member_id")
    private Long uploadedByMemberId;

    @Column(name = "uploaded_by_username", length = 50)
    private String uploadedByUsername;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "deleted_at")
    private OffsetDateTime deletedAt;

    private UploadedFile(String originalFilename, String storedFilename, String extension,
                          String detectedMime, Boolean magicByteMatched, long sizeBytes,
                          UploadStatus status, String rejectReason,
                          Long uploadedByMemberId, String uploadedByUsername) {
        this.originalFilename = originalFilename;
        this.storedFilename = storedFilename;
        this.extension = extension;
        this.detectedMime = detectedMime;
        this.magicByteMatched = magicByteMatched;
        this.sizeBytes = sizeBytes;
        this.status = status;
        this.rejectReason = rejectReason;
        this.uploadedByMemberId = uploadedByMemberId;
        this.uploadedByUsername = uploadedByUsername;
    }

    public static UploadedFile success(String originalFilename, String storedFilename, String extension,
                                        String detectedMime, boolean magicByteMatched, long sizeBytes,
                                        Long uploadedByMemberId, String uploadedByUsername) {
        return new UploadedFile(originalFilename, storedFilename, extension, detectedMime,
                magicByteMatched, sizeBytes, UploadStatus.SUCCESS, null,
                uploadedByMemberId, uploadedByUsername);
    }

    public static UploadedFile rejected(String originalFilename, String extension, String detectedMime,
                                         Boolean magicByteMatched, long sizeBytes, String rejectReason,
                                         Long uploadedByMemberId, String uploadedByUsername) {
        return new UploadedFile(originalFilename, null, extension, detectedMime,
                magicByteMatched, sizeBytes, UploadStatus.REJECTED, rejectReason,
                uploadedByMemberId, uploadedByUsername);
    }
}
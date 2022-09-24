package com.example.appfileuploaddownload.repository;

import com.example.appfileuploaddownload.entity.Attachment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AttachmentRepo extends JpaRepository<Attachment,Integer> {
}

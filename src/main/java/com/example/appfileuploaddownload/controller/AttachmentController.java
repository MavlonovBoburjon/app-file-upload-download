package com.example.appfileuploaddownload.controller;

import com.example.appfileuploaddownload.entity.Attachment;
import com.example.appfileuploaddownload.entity.AttachmentContent;
import com.example.appfileuploaddownload.repository.AttachmentContentRepo;
import com.example.appfileuploaddownload.repository.AttachmentRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletResponse;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/attachment")
public class AttachmentController {

    @Autowired
    AttachmentRepo attachmentRepo;
    @Autowired
    AttachmentContentRepo attachmentContentRepo;

    private static final String uploadDirectory="yuklanganlar";
    @PostMapping("/uploadDb")
    public String uploadFileToDb(MultipartHttpServletRequest request) throws IOException {
        Iterator<String> fileNames = request.getFileNames();
        MultipartFile file= request.getFile(fileNames.next());
        if(file!=null){
            String originalFilename = file.getOriginalFilename();
            long size = file.getSize();
            String contentType = file.getContentType();
            Attachment attachment=new Attachment();
            attachment.setFileOriginalName(originalFilename);
            attachment.setSize(size);
            attachment.setContentType(contentType);
            Attachment savedAttachment = attachmentRepo.save(attachment);
            AttachmentContent attachmentContent = new AttachmentContent();
            attachmentContent.setBasicContent(file.getBytes());
            attachmentContent.setAttachment(savedAttachment);
            attachmentContentRepo.save(attachmentContent);
            return "File Uploaded  Id:"+savedAttachment.getId();
        }
        return "Error";
    }

    @PostMapping("/uploadSystem")
    public String uploadFileToSystem(@RequestParam("file") MultipartFile file) throws IOException {
        if(file!=null){
            String originalFilename = file.getOriginalFilename();
            Attachment attachment = new Attachment();
            attachment.setFileOriginalName(originalFilename);
            attachment.setSize(file.getSize());
            attachment.setContentType(file.getContentType());

            String[] split = originalFilename.split("\\.");

            String s = UUID.randomUUID().toString() +"."+split[split.length-1];
            attachment.setName(s);
            attachmentRepo.save(attachment);
            Path path= Paths.get(uploadDirectory+"/"+s);
            Files.copy(file.getInputStream(),path);
            return "Fayl Saqlandi Id si:"+attachment.getId();
        }
        return "Error";
    }

    @GetMapping("/getFile/{id}")
    public void getFile(@PathVariable Integer id, HttpServletResponse response) throws IOException {
        Optional<Attachment> optionalAttachment = attachmentRepo.findById(id);
        if(optionalAttachment.isPresent()){
            Attachment attachment = optionalAttachment.get();
            Optional<AttachmentContent> contentOptional = attachmentContentRepo.findByAttachmentId(id);
            if(contentOptional.isPresent()){
                AttachmentContent attachmentContent = contentOptional.get();
                response.setHeader("Content-Disposition",
                        "attachment; filename=\""+attachment.getFileOriginalName()+"\"");
                response.setContentType(attachment.getContentType());

                FileCopyUtils.copy(attachmentContent.getBasicContent(),response.getOutputStream());

            }
        }
    }

    @GetMapping("getFileFromSystem/{id}")
    public void getFileSystem(@PathVariable Integer id,HttpServletResponse response) throws IOException {
        Optional<Attachment> optionalAttachment = attachmentRepo.findById(id);
        if (optionalAttachment.isPresent()){
            Attachment attachment = optionalAttachment.get();
            response.setHeader("Content-Disposition",
                    "inline; filename=\""+attachment.getFileOriginalName()+"\"");
            response.setContentType(attachment.getContentType());

            FileInputStream fileInputStream=new FileInputStream(uploadDirectory+"/"+attachment.getName());

            FileCopyUtils.copy(fileInputStream,response.getOutputStream());
        }
    }
}

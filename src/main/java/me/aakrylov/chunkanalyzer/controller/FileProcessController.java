package me.aakrylov.chunkanalyzer.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.aakrylov.chunkanalyzer.model.FileOperationResult;
import me.aakrylov.chunkanalyzer.service.api.FileService;
import me.aakrylov.chunkanalyzer.type.FileOperationStatus;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;

@RestController
@RequestMapping("/file-process")
@Slf4j
@RequiredArgsConstructor
public class FileProcessController {


    private final FileService fileService;

    @PostMapping("/split")
    public ResponseEntity<FileOperationResult> split(@RequestParam("file") MultipartFile file,
                                        @RequestParam(name = "chunkSize", defaultValue = "5120") int chunkSize) {
        FileOperationResult result = fileService.split(file, chunkSize);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @PostMapping("/analyze")
    public ResponseEntity<FileOperationResult> analyze(@RequestParam("file") MultipartFile file) {
        FileOperationResult result = fileService.analyze(file);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @PostMapping("/analyze/all")
    public ResponseEntity<FileOperationResult> analyzeAll(@RequestParam("dir") Path directory) {
        FileOperationResult result = fileService.analyzeAll(directory);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @GetMapping("/assemble")
    public ResponseEntity<FileOperationResult> assemble(@RequestParam("dir") Path directory,
                                                        @RequestParam(name = "deleteSource", defaultValue = "false") boolean deleteSource) {
        FileOperationResult result = fileService.assemble(directory, deleteSource);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }
}

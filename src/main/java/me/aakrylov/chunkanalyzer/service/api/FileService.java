package me.aakrylov.chunkanalyzer.service.api;

import me.aakrylov.chunkanalyzer.model.FileOperationResult;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;

public interface FileService {

    FileOperationResult split(MultipartFile file, int chunkSize);

    FileOperationResult analyze(MultipartFile file);

    FileOperationResult assemble(Path directory, boolean deleteSource);

    FileOperationResult analyzeAll(Path directory);
}

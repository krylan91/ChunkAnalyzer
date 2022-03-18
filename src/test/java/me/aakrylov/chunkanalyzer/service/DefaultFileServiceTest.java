package me.aakrylov.chunkanalyzer.service;

import me.aakrylov.chunkanalyzer.model.FileOperationResult;
import me.aakrylov.chunkanalyzer.service.api.FileService;
import me.aakrylov.chunkanalyzer.type.FileOperationStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class DefaultFileServiceTest {

    private final FileService fileService = new DefaultFileService("C:\\splitFiles\\");
    private static final int DEFAULT_CHUNK_SIZE = 1024;

    @Test
    void whenSplitFile_andFileIsEmpty_thenReturnNoContentResult() {
        MockMultipartFile mockFile = new MockMultipartFile(
                "file",
                "empty.txt",
                "text/plain",
                new byte[0]
        );

        FileOperationResult result = fileService.split(mockFile, DEFAULT_CHUNK_SIZE);

        assertEquals(FileOperationStatus.NO_CONTENT, result.getStatus());
    }

}
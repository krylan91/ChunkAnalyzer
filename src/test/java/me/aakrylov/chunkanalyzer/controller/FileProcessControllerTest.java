package me.aakrylov.chunkanalyzer.controller;

import me.aakrylov.chunkanalyzer.service.api.FileService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(FileProcessController.class)
class FileProcessControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private FileService fileService;

    @Test
    void whenSplitFile_thenReturnOkResult() throws Exception {
        byte[] content = new byte[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9};
        MockMultipartFile file = new MockMultipartFile("file", "filled.txt", MediaType.TEXT_PLAIN_VALUE, content);

        mockMvc.perform(multipart("/file-process/split")
                        .file(file)
                        .param("chunkSize", "1"))
                .andExpect(status().isOk());
    }

    @Test
    void whenAnalyzeFile_thenReturnOkResult() throws Exception {
        byte[] content = new byte[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9};
        MockMultipartFile file = new MockMultipartFile("file", "filled.txt", MediaType.TEXT_PLAIN_VALUE, content);

        mockMvc.perform(multipart("/file-process/analyze")
                        .file(file))
                .andExpect(status().isOk());
    }

    @Test
    void whenAnalyzeAll_thenReturnOkResult() throws Exception {
        mockMvc.perform(post("/file-process/analyze/all")
                .param("dir", "some-directory-path"))
                .andExpect(status().isOk());
    }

    @Test
    void whenAssemble_thenReturnOkResult() throws Exception {
        mockMvc.perform(get("/file-process/assemble")
                        .param("dir", "some-directory-path"))
                .andExpect(status().isOk());
    }
}
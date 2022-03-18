package me.aakrylov.chunkanalyzer.controller;

import me.aakrylov.chunkanalyzer.service.api.FileService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(FileProcessController.class)
class FileProcessControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private FileService fileService;

    @Test
    void shouldReturnOk() throws Exception {
        byte[] content = new byte[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9};
        MockMultipartFile file = new MockMultipartFile("file", "filled.txt", MediaType.TEXT_PLAIN_VALUE, content);

        mockMvc.perform(multipart("/file-process/split")
                        .file(file)
                        .param("chinkSize", "1"))
                .andExpect(status().isOk())
                .andExpect(content().string("File successfully split into 0 parts."));
    }

    @Test
    void shouldReturnNoContent() throws Exception {
        byte[] content = new byte[0];
        MockMultipartFile file = new MockMultipartFile("file", "empty.txt", MediaType.TEXT_PLAIN_VALUE, content);

        mockMvc.perform(multipart("/file-process/split")
                        .file(file)
                        .param("chinkSize", "1"))
                .andExpect(status().is(204))
                .andExpect(content().string("File is empty."));
    }
}
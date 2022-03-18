package me.aakrylov.chunkanalyzer.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import me.aakrylov.chunkanalyzer.type.FileOperationStatus;

@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder(setterPrefix = "set")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class FileOperationResult {

    private FileOperationStatus status;
    private String description;
}

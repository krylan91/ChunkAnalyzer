package me.aakrylov.chunkanalyzer.service;

import lombok.extern.slf4j.Slf4j;
import me.aakrylov.chunkanalyzer.annotation.Loggable;
import me.aakrylov.chunkanalyzer.component.Pair;
import me.aakrylov.chunkanalyzer.model.FileOperationResult;
import me.aakrylov.chunkanalyzer.service.api.FileService;
import me.aakrylov.chunkanalyzer.type.FileOperationStatus;
import me.aakrylov.chunkanalyzer.util.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.*;
import java.util.Comparator;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static java.lang.Math.ceil;
import static java.lang.Math.round;

@Service
@Slf4j
public class DefaultFileService implements FileService {

    private static final int RESERVED_BYTES = 400;
    private static final String INCORRECT_SYMBOL_MESSAGE = "Incorrect symbol [%s] at index [%d]";
    private final Set<Character> number = StringUtils.convertToSet("1234567890");
    private final Set<Character> latin = StringUtils.convertToSet("abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ");
    private final Set<Character> special = StringUtils.convertToSet(" !\"#$%&'()*+,-./:;<=>?@[\\]^_`{|}~\r\n\t");

    @Value("${files.split.default.location}")
    private String defaultFileLocation;

    @Override
    @Loggable
    public FileOperationResult split(MultipartFile file, int chunkSize) {
        String fileName = file.getOriginalFilename();
        log.info("[{}] Start splitting file.", fileName);
        Integer bufferSize = chunkSize * 1024 - RESERVED_BYTES;
        int partCounter = 1;
        byte[] buffer = new byte[bufferSize];
        long chunkCount = round(ceil(file.getSize() / bufferSize.doubleValue()));
        log.info("[{}] File size: {}", fileName, file.getSize());
        log.debug("Chunk size: {}", bufferSize);
        log.info("[{}] Split into {} chunks by {} bytes", fileName, chunkCount, bufferSize);
        try {
            try (BufferedInputStream bis = new BufferedInputStream(file.getInputStream())) {
                int bytesAmount;
                while ((bytesAmount = bis.read(buffer)) > 0) {
                    String filePartName = String.format("%s.%03d", fileName, partCounter++);
                    String destinationDir = defaultFileLocation + fileName.replaceAll("\\.", "_");
                    createDirIfNotExists(destinationDir);
                    File newFile = new File(destinationDir, filePartName);
                    try (FileOutputStream out = new FileOutputStream(newFile)) {
                        int counter = 1;
                        log.trace("Splitting file {}, part {}", fileName, counter++);
                        out.write(buffer, 0, bytesAmount);
                    }
                }
            }
        } catch (IOException e) {
            log.error("[{}] Error splitting file into parts: ", fileName, e);
            return errorResult(e);
        }
        return FileOperationResult.builder()
                .setStatus(FileOperationStatus.SUCCESS)
                .setDescription(String.format("File [%s] was split into %s parts", fileName, chunkCount))
                .build();
    }

    @Override
    @Loggable
    public FileOperationResult analyze(MultipartFile file) {
        log.info("[{}] Start file analysis.", file.getOriginalFilename());
        try {
            try (BufferedInputStream bis = new BufferedInputStream(file.getInputStream())) {
                return isCorrectContent(bis.readAllBytes());
            }
        } catch (IOException e) {
            log.error("[{}] Error analyzing file: ", file.getOriginalFilename(), e);
            return errorResult(e);
        }
    }

    @Override
    @Loggable
    public FileOperationResult assemble(Path directory, boolean deleteSource) {
        String fileName = getFileNameFromDirPath(directory);
        log.info("Build file [{}] from chunks", fileName);
        try (DirectoryStream<Path> files = Files.newDirectoryStream(directory)) {
            if (dirIsEmpty(directory)) {
                return FileOperationResult.builder()
                        .setStatus(FileOperationStatus.ERROR)
                        .setDescription(String.format("Directory [%s] is empty", directory))
                        .build();
            }
            Path targetPath = directory.getParent().resolve(fileName);
            if (Files.exists(targetPath)) {
                Files.delete(targetPath);
            }
            Path targetFile = Files.createFile(targetPath);
            AtomicInteger counter = new AtomicInteger(1);
            StreamSupport.stream(files.spliterator(), false)
                    .sequential()
                    .sorted(Comparator.comparing(Path::toString))
                    .forEach(file -> {
                        try {
                            log.trace("Building file, part {}", counter.getAndIncrement());
                            Files.write(targetFile, Files.readAllBytes(file), StandardOpenOption.APPEND);
                            if (deleteSource) {
                                Files.delete(file);
                            }
                        } catch (IOException e) {
                            log.error("Error writing file part [{}] to common file:", file.getFileName(), e);
                        }
                    });
        } catch (IOException e) {
            log.error("Error building file from path [{}]:", directory, e);
            return errorResult(e);
        }
        return FileOperationResult.builder()
                .setStatus(FileOperationStatus.SUCCESS)
                .build();
    }

    @Override
    @Loggable
    public FileOperationResult analyzeAll(Path directory) {
        log.info("Analyzing all files in {} folder", directory);
        StringBuilder sb = new StringBuilder();
        String errorString = "Error in file [%s]: %s";
        try (DirectoryStream<Path> files = Files.newDirectoryStream(directory)) {
            StreamSupport.stream(files.spliterator(), false)
                    .sequential()
                    .sorted(Comparator.comparing(Path::toString))
                    .map(filePath -> {
                        log.trace("Analyzing file {}", filePath);
                        try {
                            return Pair.of(filePath, isCorrectContent(Files.readAllBytes(filePath)));
                        } catch (IOException e) {
                            return Pair.of(filePath, errorResult(e));
                        }
                    })
                    .filter(pair -> FileOperationStatus.ERROR.equals(pair.getRight().getStatus()))
                    .forEach(pair -> sb.append(String.format(errorString, pair.getLeft(), pair.getRight().getDescription()))
                            .append("\r\n"));
            return FileOperationResult.builder()
                    .setStatus(FileOperationStatus.SUCCESS)
                    .setDescription(sb.toString().equals("") ? sb.toString() : null)
                    .build();
        } catch (IOException e) {
            log.error("Error analyzing files in [{}]:", directory, e);
            return errorResult(e);
        }
    }

    private boolean dirIsEmpty(Path dir) {
        if (Files.isDirectory(dir)) {
            try (Stream<Path> entries = Files.list(dir)) {
                return entries.findFirst().isEmpty();
            } catch (IOException e) {
                return false;
            }
        }
        return false;
    }

    private String getFileNameFromDirPath(Path dirPath) {
        String dirPathString = dirPath.toString();
        int indexOfLastUnderscore = dirPathString.lastIndexOf("_");
        return dirPathString.substring(0, indexOfLastUnderscore) +
                "." +
                dirPathString.substring(indexOfLastUnderscore + 1);
    }

    private void createDirIfNotExists(String dirPath) throws IOException {
        Path dir = Paths.get(dirPath);
        if (!Files.exists(dir)) {
            log.debug("Created directory {}", dir.toAbsolutePath());
            Files.createDirectories(dir);
        }
    }

    private FileOperationResult isCorrectContent(byte[] content) {
        String stringForm = new String(content);
        log.trace("String representation: {}", stringForm);
        boolean analysisResult = false;
        StringBuilder sb = new StringBuilder();
        for (int indx = 0; indx < stringForm.toCharArray().length; indx++) {
            char symbol = stringForm.toCharArray()[indx];
            String hex = Integer.toHexString(symbol);
            String bin = Integer.toBinaryString(symbol);
            log.trace("Symbol: [{}], Hex: [{}], Binary: [{}]", symbol, hex, bin);
            analysisResult = isCorrectSymbol(symbol);
            if (!analysisResult) {
                sb.append(String.format(INCORRECT_SYMBOL_MESSAGE, symbol, indx));
                sb.append("\r\n");
            }
        }
        FileOperationResult result = FileOperationResult.builder()
                .setStatus(FileOperationStatus.ANALYSIS_COMPLETE)
                .setDescription(sb.toString())
                .build();
        log.trace("Analysis result: {}", result);
        return result;
    }

    private boolean isCorrectSymbol(char symbol) {
        return latin.contains(symbol) ||
                number.contains(symbol) ||
                special.contains(symbol) ||
                (symbol > 31 && symbol < 128);
    }

    private FileOperationResult errorResult(IOException e) {
        return FileOperationResult.builder()
                .setStatus(FileOperationStatus.ERROR)
                .setDescription(e.toString())
                .build();
    }
}

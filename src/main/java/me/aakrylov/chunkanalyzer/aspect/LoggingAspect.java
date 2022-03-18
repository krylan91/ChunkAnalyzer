package me.aakrylov.chunkanalyzer.aspect;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.stream.Collectors;

@Aspect
@Component
@Slf4j
@RequiredArgsConstructor
public class LoggingAspect {

    private final ObjectMapper objectMapper;

    @Around("@annotation(me.aakrylov.chunkanalyzer.annotation.Loggable)")
    public Object loggableCall(ProceedingJoinPoint joinPoint) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String args = Arrays.stream(joinPoint.getArgs())
                .map(this::toString)
                .collect(Collectors.joining(","));

        StringBuilder sb = new StringBuilder();
        sb.append("{Class: ");
        sb.append(joinPoint.getTarget().getClass().getSimpleName());
        sb.append(", Method: ");
        sb.append(signature.getMethod().getName());
        sb.append(", Args: [");
        sb.append(args);
        sb.append("]}");

        log.info(sb.toString());

        try {
            Object retval = joinPoint.proceed();
            log.info("Finished executing {}", signature.getMethod().toGenericString());
            return retval;
        } catch (Throwable e) {
            log.error("Error executing {}", signature.getMethod().toGenericString(), e);
            return null;
        }
    }

    private String toString(Object object) {
        try {
            if (object instanceof MultipartFile file) {
                return String.format("File: %s", file.getOriginalFilename());
            } else {
                return objectMapper.writeValueAsString(object);
            }
        } catch (JsonProcessingException e) {
            log.error("Error converting object to string.");
            log.error(e.getMessage());
            return null;
        }
    }
}

package org.example.aicareernav1.service.testService;

import lombok.extern.slf4j.Slf4j;
import org.example.aicareernav1.dto.testDto.CodeExecutionResult;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class CodeExecutionService {

    // Увеличим до 10 секунд, так как Docker стартует не мгновенно
    private static final long TIMEOUT_SECONDS = 10;
    private static final String MEMORY_LIMIT = "128m";
    private static final String CPU_LIMIT = "0.5";

    public CodeExecutionResult executeJavaCode(String code) {
        Path tempDir = null;
        try {
            tempDir = Files.createTempDirectory("code_exec_");
            Path sourceFile = tempDir.resolve("Main.java");
            Files.writeString(sourceFile, code);

            String[] command = {
              "docker", "run", "--rm",
              "--network", "none",
              "--memory", MEMORY_LIMIT,
              "--cpus", CPU_LIMIT,
              // Используем :rw, так как javac нужно записать скомпилированный файл в папку
              "-v", tempDir.toAbsolutePath() + ":/app:rw",
              "my-java-sandbox", // ИСПОЛЬЗУЕМ ТВОЙ НОВЫЙ ОБРАЗ
              "sh", "-c", "javac Main.java && java Main" // В твоем Dockerfile WORKDIR уже /app
            };

            ProcessBuilder pb = new ProcessBuilder(command);
            Process process = pb.start();

            // ВАЖНО: Закрываем ввод, чтобы процесс не висел в ожидании
            process.getOutputStream().close();

            // Читаем потоки асинхронно, чтобы не забить буфер процесса
            CompletableFuture<String> stdoutFuture = CompletableFuture.supplyAsync(() -> readStream(process.getInputStream()));
            CompletableFuture<String> stderrFuture = CompletableFuture.supplyAsync(() -> readStream(process.getErrorStream()));

            boolean finished = process.waitFor(TIMEOUT_SECONDS, TimeUnit.SECONDS);

            if (!finished) {
                process.destroyForcibly();
                log.warn("Docker process timed out for task in {}", tempDir);
                return CodeExecutionResult.builder()
                  .isTimeout(true)
                  .stderr("Превышено время выполнения (" + TIMEOUT_SECONDS + " сек)")
                  .build();
            }

            // Получаем результаты чтения потоков
            String stdout = stdoutFuture.get(1, TimeUnit.SECONDS);
            String stderr = stderrFuture.get(1, TimeUnit.SECONDS);

            return CodeExecutionResult.builder()
              .stdout(stdout)
              .stderr(stderr)
              .exitCode(process.exitValue())
              .isTimeout(false)
              .build();

        } catch (Exception e) {
            log.error("Критическая ошибка исполнения: {}", e.getMessage());
            return CodeExecutionResult.builder().stderr("Ошибка системы: " + e.getMessage()).build();
        } finally {
            if (tempDir != null) {
                deleteDirectory(tempDir.toFile());
            }
        }
    }

    private String readStream(InputStream stream) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }
            return sb.toString();
        } catch (IOException e) {
            return "Ошибка чтения: " + e.getMessage();
        }
    }

    private void deleteDirectory(File directoryToBeDeleted) {
        File[] allContents = directoryToBeDeleted.listFiles();
        if (allContents != null) {
            for (File file : allContents) {
                deleteDirectory(file);
            }
        }
        directoryToBeDeleted.delete();
    }
}
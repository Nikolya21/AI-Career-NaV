package org.example.aicareernav1.service.testService;

import lombok.extern.slf4j.Slf4j;
import org.example.aicareernav1.dto.testDto.CodeExecutionResult;
import org.springframework.stereotype.Service;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.concurrent.*;

@Service
@Slf4j
public class CodeExecutionService {

    // Выносим имя тома в константу, чтобы не ошибиться
    private static final String DOCKER_VOLUME_NAME = "my_global_code_storage";

    public CodeExecutionResult execute(String code, String lang) {
        String cleanLang = lang.replace("[", "").replace("]", "").toLowerCase().trim();

        if ("none".equals(cleanLang) || cleanLang.isEmpty()) {
            return CodeExecutionResult.builder()
                    .stderr("Compiler not available for this role")
                    .detectedLanguage("none")
                    .build();
        }

        String fileName;
        String imageName;
        String runCmd;

        switch (cleanLang) {
            case "java" -> { fileName = "Main.java"; imageName = "compiler-java"; runCmd = "javac Main.java && java Main"; }
            case "python" -> { fileName = "main.py"; imageName = "compiler-python"; runCmd = "python3 main.py"; }
            case "javascript", "js" -> { fileName = "main.js"; imageName = "compiler-js"; runCmd = "node main.js"; }
            case "cpp" -> { fileName = "main.cpp"; imageName = "compiler-cpp"; runCmd = "g++ main.cpp -o main && ./main"; }
            case "ruby" -> { fileName = "main.rb"; imageName = "compiler-ruby"; runCmd = "ruby main.rb"; }
            default -> {
                log.warn("⚠️ Неподдерживаемый язык: {}", cleanLang);
                return CodeExecutionResult.builder().stderr("Unsupported language: " + cleanLang).detectedLanguage("none").build();
            }
        }

        return runInDocker(code, fileName, imageName, runCmd, cleanLang);
    }

    private CodeExecutionResult runInDocker(String code, String file, String img, String cmd, String lang) {
        Path tempDir = null;
        try {
            Path baseDir = Paths.get("/codes");
            if (!Files.exists(baseDir)) {
                Files.createDirectories(baseDir);
            }

            tempDir = Files.createTempDirectory(baseDir, "sandbox_");
            log.info("📁 Папка создана: {}", tempDir.toAbsolutePath());
            Path filePath = tempDir.resolve(file);

            // Пишем файл с принудительным сбросом на диск
            try (FileOutputStream fos = new FileOutputStream(filePath.toFile())) {
                fos.write(code.getBytes(StandardCharsets.UTF_8));
                fos.flush();
                fos.getFD().sync();
                log.info("📄 Файл записан в Volume: {}, размер: {} байт",
                        filePath.getFileName(), filePath.toFile().length());
            }

            // Выставляем права, чтобы компилятор (который может быть под другим пользователем) всё прочитал
            tempDir.toFile().setWritable(true, false); // <--- ДОБАВЬ ЭТО
            tempDir.toFile().setReadable(true, false);
            tempDir.toFile().setExecutable(true, false);
            filePath.toFile().setReadable(true, false);

            String folderName = tempDir.getFileName().toString();

            // ВНИМАНИЕ: Используем DOCKER_VOLUME_NAME ("my_global_code_storage")
            String[] dockerCmd = {
                    "docker", "run", "--rm",
                    "--network", "none",
                    "--memory", "128m",
                    "-v", DOCKER_VOLUME_NAME + ":/app:rw",
                    "-w", "/app/" + folderName,
                    img, "sh", "-c", cmd
            };

            log.info("Executing: {}", String.join(" ", dockerCmd));

            Process proc = new ProcessBuilder(dockerCmd).start();

            CompletableFuture<String> out = CompletableFuture.supplyAsync(() -> readStream(proc.getInputStream()));
            CompletableFuture<String> err = CompletableFuture.supplyAsync(() -> readStream(proc.getErrorStream()));

            boolean finished = proc.waitFor(15, TimeUnit.SECONDS);

            if (!finished) {
                proc.destroyForcibly();
                return CodeExecutionResult.builder().isTimeout(true).detectedLanguage(lang).build();
            }

            String stdout = out.get(2, TimeUnit.SECONDS);
            String stderr = err.get(2, TimeUnit.SECONDS);

            return CodeExecutionResult.builder()
                    .stdout(stdout)
                    .stderr(stderr)
                    .detectedLanguage(lang)
                    .build();

        } catch (Exception e) {
            log.error("Execution error", e);
            return CodeExecutionResult.builder().stderr(e.getMessage()).build();
        } finally {
            // Оставляем закомментированным для отладки
            // if (tempDir != null) deleteDir(tempDir.toFile());
        }
    }

    private String readStream(InputStream is) {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader r = new BufferedReader(new InputStreamReader(is))) {
            String line;
            while ((line = r.readLine()) != null) {
                sb.append(line).append("\n");
            }
        } catch (IOException e) {
            log.error("Stream error", e);
        }
        return sb.toString();
    }
}
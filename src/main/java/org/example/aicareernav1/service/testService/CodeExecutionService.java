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
            // 1. Используем путь, к которому примонтирован Volume в docker-compose
            Path baseDir = Paths.get("/codes");
            if (!Files.exists(baseDir)) {
                Files.createDirectories(baseDir);
            }

            // 2. Создаем временную подпапку
            tempDir = Files.createTempDirectory(baseDir, "sandbox_");
            String folderName = tempDir.getFileName().toString();
            log.info("📁 Рабочая директория в контейнере: {}", tempDir.toAbsolutePath());

            Path filePath = tempDir.resolve(file);

            // 3. Записываем код
            try (FileOutputStream fos = new FileOutputStream(filePath.toFile())) {
                fos.write(code.getBytes(StandardCharsets.UTF_8));
                fos.flush();
                fos.getFD().sync();
            }

            // Выставляем права, чтобы компилятор в другом контейнере мог прочитать файл
            tempDir.toFile().setWritable(true, false);
            tempDir.toFile().setReadable(true, false);
            tempDir.toFile().setExecutable(true, false);
            filePath.toFile().setReadable(true, false);

            // 4. ФОРМИРУЕМ КОМАНДУ ЧЕРЕЗ --mount
            // Это исключает ошибку "too many colons", так как параметры передаются явно
            String mountSpec = String.format(
                    "type=volume,source=my_global_code_storage,target=/app,volume-subpath=%s",
                    folderName
            );

            String[] dockerCmd = {
                    "docker", "run", "--rm",
                    "--network", "none",
                    "--memory", "128m",
                    "--mount", mountSpec, // Используем --mount вместо -v
                    "-w", "/app",
                    img, "sh", "-c", cmd
            };

            log.info("🚀 Запуск контейнера через mount: {}", String.join(" ", dockerCmd));

            Process proc = new ProcessBuilder(dockerCmd).start();

            CompletableFuture<String> out = CompletableFuture.supplyAsync(() -> readStream(proc.getInputStream()));
            CompletableFuture<String> err = CompletableFuture.supplyAsync(() -> readStream(proc.getErrorStream()));

            boolean finished = proc.waitFor(15, TimeUnit.SECONDS);

            if (!finished) {
                proc.destroyForcibly();
                return CodeExecutionResult.builder().isTimeout(true).detectedLanguage(lang).build();
            }

            return CodeExecutionResult.builder()
                    .stdout(out.get(2, TimeUnit.SECONDS))
                    .stderr(err.get(2, TimeUnit.SECONDS))
                    .detectedLanguage(lang)
                    .build();

        } catch (Exception e) {
            log.error("❌ Ошибка при выполнении кода в Docker", e);
            return CodeExecutionResult.builder().stderr("Internal execution error: " + e.getMessage()).build();
        }
    }

    private String readStream(InputStream is) {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader r = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
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
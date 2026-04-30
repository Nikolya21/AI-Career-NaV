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
            // Исправлено: используем относительный путь от корня проекта для совместимости с macOS/Windows
            Path baseDir = Paths.get(System.getProperty("user.dir"), "codes");
            if (!Files.exists(baseDir)) {
                Files.createDirectories(baseDir);
            }

            // Создаем временную подпапку для конкретного запуска
            tempDir = Files.createTempDirectory(baseDir, "sandbox_");
            log.info("📁 Рабочая директория: {}", tempDir.toAbsolutePath());

            Path filePath = tempDir.resolve(file);

            // Записываем код в файл
            try (FileOutputStream fos = new FileOutputStream(filePath.toFile())) {
                fos.write(code.getBytes(StandardCharsets.UTF_8));
                fos.flush();
                fos.getFD().sync();
                log.info("📄 Файл записан: {}, размер: {} байт",
                  filePath.getFileName(), filePath.toFile().length());
            }

            // Выставляем права доступа, чтобы пользователь 'executor' внутри контейнера мог прочитать файл
            tempDir.toFile().setWritable(true, false);
            tempDir.toFile().setReadable(true, false);
            tempDir.toFile().setExecutable(true, false);
            filePath.toFile().setReadable(true, false);

            // Получаем абсолютный путь для монтирования в Docker
            String absolutePathOnHost = tempDir.toAbsolutePath().toString();

            // Формируем команду запуска контейнера
            // Используем Bind Mount (-v локальный_путь:/app), так как приложение запущено вне Docker
            String[] dockerCmd = {
              "docker", "run", "--rm",
              "--network", "none",
              "--memory", "128m",
              "-v", absolutePathOnHost + ":/app:rw",
              "-w", "/app",
              img, "sh", "-c", cmd
            };

            log.info("🚀 Запуск контейнера: {}", String.join(" ", dockerCmd));

            Process proc = new ProcessBuilder(dockerCmd).start();

            // Читаем потоки вывода асинхронно
            CompletableFuture<String> out = CompletableFuture.supplyAsync(() -> readStream(proc.getInputStream()));
            CompletableFuture<String> err = CompletableFuture.supplyAsync(() -> readStream(proc.getErrorStream()));

            // Ограничиваем время выполнения (защита от бесконечных циклов)
            boolean finished = proc.waitFor(15, TimeUnit.SECONDS);

            if (!finished) {
                proc.destroyForcibly();
                log.error("⏳ Тайм-аут выполнения кода");
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
            log.error("❌ Ошибка при выполнении кода в Docker", e);
            return CodeExecutionResult.builder().stderr("Internal execution error: " + e.getMessage()).build();
        } finally {
            // Для продакшена здесь стоит добавить удаление tempDir,
            // но для отладки пока оставляем файлы на месте
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